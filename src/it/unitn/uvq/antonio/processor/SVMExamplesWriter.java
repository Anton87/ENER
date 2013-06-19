package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuadruple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

public class SVMExamplesWriter {
	
	SVMExamplesWriter(ExampleBuilder builder) {
		if (builder == null) { 
			throw new NullPointerException("builder is null");
		}
		this.builder = builder;
	}
	
	/**
	 * Write the SVM examples on file.
	 *  
	 * @param inputFile A String holding the example files.
	 * @param outputFile A String holding the svm examples file.
	 */
	public void writeSVMExamples(String inputFile, String outputFile) {
		if (inputFile == null) { 
			throw new NullPointerException("inputFile is null");
		}
		if (outputFile == null) { 
			throw new NullPointerException("outputFile is null");
		}
		
		 /* Create the dest directory if it does not exist. */
		File dest = new File(outputFile).getParentFile();
		if (!dest.isDirectory()) { dest.mkdirs(); }
		
		String tsvFile = getFilepathWithoutExtension(outputFile) + ".tsv";

		PrintWriter out = null;
		try {
			out = new PrintWriter(
					new FileWriter(outputFile));
		} catch (IOException e) { 
			System.err.println("I/O Error: " + e.getMessage());
			System.exit(1);
		}
		
		PrintWriter tsv = null;
		try {
			tsv = new PrintWriter(
					new FileWriter(tsvFile));
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.getMessage());
			System.exit(1);
		}
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new FileReader(inputFile));
			
			int examplesNum = 0;
			for (String line = null; (line = in.readLine()) != null; ) {
				//System.out.print("(" + examplesNum + "): ");
				line = line.trim();
				//System.out.println(line);
				
				if (isValidExample(line)) {
					//System.out.println(line);
					Quadruple values = processLine(line);
					
					String paragraph = (String) values.first();
					String sentence  = (String) values.second();
					IntRange entitySpan = (IntRange) values.third();
					List<String> notableTypes = (List<String>) values.fourth();
					
					builder.setParagraph(paragraph);
					builder.setSentence(sentence);
					builder.setEntitySpan(entitySpan);
					
					String svmExample = null;
					try {
						svmExample = builder.build();
					} catch (OutOfMemoryError e) {
						//System.out.print('*');
					}
					
					if (svmExample != null) {
						// System.out.print(svmExample);
						System.out.print(".");
					
						/* Write SVM example on outputFile file. */
						out.println(svmExample);
					
						/* Write notable types on outputFile.tsv file. */
						tsv.println(Joiner.on(SEPARATOR).join(notableTypes));
					} else {
						System.out.print("n");
					}
				}		
				//System.out.println();
				examplesNum += 1;
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + inputFile);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.getMessage());
			System.exit(1);
		} finally {
			try {
				in.close();
				out.close();
				tsv.close();
			} catch (IOException e) { }
		}
	}
	
	private Quadruple<String, String, IntRange, List<String>> processLine(String line) { 
		assert line != null;
		
		String[] parts = line.split(SEPARATOR);
		String paragraph = parts[0];
		String sentence = parts[1];
		int start = Integer.parseInt(parts[4]);
		int end = Integer.parseInt(parts[5]);
		List<String> notableTypes = Arrays.asList(parts[6].split(","));
		
		IntRange entitySpan = new IntRange(start, end);
		Quadruple<String, String, IntRange, List<String>> values = 				
				new SimpleQuadruple<String, String, IntRange, List<String>>(paragraph, sentence, entitySpan, notableTypes);
					
		return values;	
	}
	
	/**
	 * Examples should have the following format:
	 *  "<paragraph>\t<sentence>\t<mid>\t<title>\t<start>\t<end>\t<notable-types>
	 *  
	 * @param line A string holding a file line
	 * @return True If the example has valid format, false otherwise
	 */
	private boolean isValidExample(String line) {
		assert  line != null;
		
		String[] parts = line.split(SEPARATOR);
		if (parts.length != 7) { return false; }
	
		String paragraph = parts[0];
		
		String sentence = parts[1];
		
		/* Check that the paragraph is not empty. */
		if (paragraph.isEmpty()) { 
			//System.err.println("Error: paragraph is empty.");
			System.out.print("x");
			return false;
		}
		
		if (sentence.isEmpty()) {
			//System.err.println("Error: sentence is empty.");
			System.out.print("x");
			return false;
		}
		
		/* Check that the paragraph contains the sentence. */
		if (!paragraph.contains(sentence)) { 
			//System.err.println("Error: The paragraph does not contain the sentence.");
			System.out.print("x");
			return false;			
		}
	
		int start, end = 0;
		/* Check that start is an integer. */
		try {
			start = Integer.parseInt(parts[4]);
		} catch (NumberFormatException e) {
			System.out.print("x");
			//System.err.println("Number Error: " + parts[4] + " is not a number");
			return false;
		}
		
		/* Check that end is an integer. */
		try {
			end = Integer.parseInt(parts[5]);
		} catch (NumberFormatException e) {
			System.out.print("x");
			// System.err.println("Number Error: " + parts[5] + " is not a number");
			return false;
		}
		
		/* Check that start < end. */
		if (start >= end) {
			System.out.print("x");
			//System.err.println("Range error: " + start + " >= " + end + ".");
			return false;
		}
		
		return true;	
	}
	
	/* Return the filepath with the stripped extension, if any */
	private String getFilepathWithoutExtension(String filepath) { 
		assert filepath != null;
		
		int extPos = filepath.lastIndexOf('.');
		return extPos == -1 
				? filepath
				: filepath.substring(0, extPos);
	}	
	
	private final static String SEPARATOR = "\t";
	
	private final ExampleBuilder builder;
	

	
	public static void main(String[] args) {
		
		String[] notableTypes = {
			/*
			"/automotive/model",
			"/book/book",
			"/book/journal",
			"/book/magazine",
			"/business/business_operation",
			"/computer/computer_scientist",
			"/computer/software",
			"/education/academic",
			"/en/artist",
			"/en/chef",
			"/en/economist",
			"/en/engineer",
			"/en/entrepreneur",
			"/en/lawyer",
			//"/en/winemaker",
			"/en/writer",
			"/film/actor",
			//"/film/film",
			"/food/dish",
			"/food/food",
			"/food/ingredient",
			"/government/government_agency",
			"/government/political_party",
			"/government/politician",
			"/internet/website",
			"/location/citytown",
			"/music/artist",
			"/music/composition",
			"/music/musical_group",
			"/organization/organization",
			"/spaceflight/satellite",			
			"/sports/pro_athlete",
			"/visual_art/visual_artist"
			*/
		};
		
		
		
		String src = "/home/antonio/Scrivania/sshdir_loc/data_fetched";
		String dest = "/home/antonio/Scrivania/sshdir_loc";
		
		//String inputFile = "/home/antonio/Scrivania/sshdir_loc/data_fetched/%2Fautomotive%2Fmodel.tsv";
		//String outputFile = "/home/antonio/Scrivania/sshdir_loc/Tree/Misc/data/%2Fautomotive%2Fmodel.dat";
		
		ExampleBuilder builder = new ShallowExampleBuilder();
		SVMExamplesWriter writer = new SVMExamplesWriter(builder);
		
		String className = builder.getClass().getSimpleName();
		
		String subdir = className.substring(0, className.indexOf("ExampleBuilder"));
		dest = JOINER.join(dest, subdir, "data");
				
		
		//System.out.println("destDir: " + destDir);
		
		for (String notableType : notableTypes) {
			String filenameWithoutExt = notableType.replace("/", "%2F");
			
			String outputDirpath = JOINER.join(dest, filenameWithoutExt);
			
			if (!existsDir(outputDirpath)) { 
				mkdirs(outputDirpath);
			}
			//System.out.println("output dir: " + outputDirpath);
			
			String inputFile = JOINER.join(src, filenameWithoutExt + ".tsv");
			String outputFile = JOINER.join(outputDirpath, filenameWithoutExt + ".dat");
			
			System.out.println("inputFile: " + inputFile);
			System.out.println("outputFile: " + outputFile);
			
			
			//System.out.println("input_file: " + inputFile);
			//System.out.println("output_file: " + outputFile);
			//System.out.println();
			

			if (existsFile(inputFile)) {
				writer.writeSVMExamples(inputFile, outputFile);
				System.out.println();
			} else {
				System.out.println("File not found: " + inputFile);
			}
		}
	}
	
	/* Checks whether teh file exists. */
	private static boolean existsFile(String filepath) { 
		assert filepath != null;
		
		return new File(filepath).isFile();
	}
	
	/* Checks whether the directory exists. */
	private static boolean existsDir(String pathname) {
		assert pathname != null;
		
		return new File(pathname).isDirectory();
	}
	
	/* Create a new directory. */
	private static boolean mkdirs(String pathname) {
		assert pathname != null;
		
		return new File(pathname).mkdirs();
	}
	
	private final static Joiner JOINER = Joiner.on(File.separator);

}
