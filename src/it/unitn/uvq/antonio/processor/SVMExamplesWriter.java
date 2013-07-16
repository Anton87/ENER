package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuintuple;

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
	
	SVMExamplesWriter(WordIdExampleBuilder builder) {
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
		
		//String tsvFile = getFilepathWithoutExtension(outputFile) + ".tsv";

		PrintWriter out = null;
		try {
			out = new PrintWriter(
					new FileWriter(outputFile));
		} catch (IOException e) { 
			System.err.println("I/O Error: " + e.getMessage());
			System.exit(1);
		}
		
		/*
		PrintWriter tsv = null;
		try {
			tsv = new PrintWriter(
					new FileWriter(tsvFile));
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.getMessage());
			System.exit(1);
		}
		*/
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new FileReader(inputFile));
			
			int examplesNum = 0;
			out.println("paragraph\tsentence\tmid\ttitle\tstart\tend\tnotable_types\tnotable_for\tsvm_example");
			in.readLine();
			for (String line = null; (line = in.readLine()) != null; ) {
				//System.out.print("(" + examplesNum + "): ");
				line = line.trim();
				//System.out.println(line);
				
				if (isValidExample(line)) {
					//System.out.println(line);
					Quintuple values = processLine(line);
					
					String paragraph = (String) values.first();
					String sentence  = (String) values.second();
					IntRange entitySpan = (IntRange) values.third();
					List<String> notableTypes = (List<String>) values.fourth();
					String notableFor = (String) values.fifth();
					
					builder.setParagraph(paragraph);
					builder.setSentence(sentence);
					builder.setEntitySpan(entitySpan);
					builder.setNotableTypes(notableTypes);
					builder.setNotableFor(notableFor);
					
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
						out.println(line + '\t' + svmExample);
					
						/* Write notable types on outputFile.tsv file. */
						///tsv.println(Joiner.on(SEPARATOR).join(notableTypes));
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
				// tsv.close();
			} catch (IOException e) { }
		}
	}
	
	private Quintuple<String, String, IntRange, List<String>, String> processLine(String line) { 
		assert line != null;
		
		String[] parts = line.split(SEPARATOR);
		String paragraph = parts[0];
		String sentence = parts[1];
		int start = Integer.parseInt(parts[4]);
		int end = Integer.parseInt(parts[5]);
		List<String> notableTypes = Arrays.asList(parts[6].split(","));
		String notableFor = parts[7];
		
		IntRange entitySpan = new IntRange(start, end);
		Quintuple<String, String, IntRange, List<String>, String> values = 				
				new SimpleQuintuple<>(paragraph, sentence, entitySpan, notableTypes, notableFor);
					
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
		if (parts.length != 8) { return false; }
	
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
	
	private final WordIdExampleBuilder builder;
	
	/**
	 * Returns the list of files in a directory.
	 * 
	 * @param pathname A string holding the file path
	 * @return The list of files in the directory
	 */
	private final static File[] listFiles(String pathname) { 
		assert pathname != null;
		
		if (!existsDir(pathname)) { 
			System.err.println("Dir not found: " + pathname);
			return new File[0];
		}
		return new File(pathname).listFiles();
	}
	
	public static void main(String[] args) {
		
		String src = "/home/antonio/Scrivania/sshdir_loc/data_fetched/Misc";
		String dest = "/home/antonio/Scrivania/sshdir_loc/Misc";
		
		WordIdExampleBuilder builder = new TreeWordIdExampleBuilder(MISC_IX);
		SVMExamplesWriter writer = new SVMExamplesWriter(builder);
		
		String className = builder.getClass().getSimpleName();
		String subdir = className.substring(0, className.indexOf("ExampleBuilder"));
		dest = JOINER.join(dest, subdir, "data");
				
		
		for (File file : listFiles(src)) {
			String filename = file.getName();
			
			if (!existsDir(dest)) { mkdirs(dest); }
			
			String inputFile = JOINER.join(src, filename);
			String outputFile = JOINER.join(dest, filename);
			
			//String inputFile = "/home/antonio/Scrivania/sshdir_loc/data_fetched/PER/%2Ffilm%2Factor.tsv";
			//String outputFile = "/home/antonio/Scrivania/sshdir_loc/PER/Tree/%2Ffilm%2Factor.tsv";
			
			writeFileModel(writer, inputFile, outputFile);
			
		}
	}
			
	public static void writeFileModel(SVMExamplesWriter writer, String inputFile, String outputFile) {
		if (writer == null) { 
			throw new NullPointerException("writer is null");
		}
		if (inputFile == null) { 
			throw new NullPointerException("inputFile is null");
		}
		if (outputFile == null) {
			throw new NullPointerException("outputFile is null");
		}
		  
		File file = new File(inputFile);
				
		if (file.isDirectory() || !file.getPath().endsWith(".tsv")) return;
			
		System.out.println("inputFile: " + inputFile);
		System.out.println("outputFile: " + outputFile);
		
		if (existsFile(inputFile)) {
			writer.writeSVMExamples(inputFile, outputFile);
			System.out.println();
		} else {
			System.out.println("File not found: " + inputFile);
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
	
	private final static String PER_IX = "/home/antonio/Scrivania/sshdir_loc/data_fetched/per.ix";
	
	private final static String ORG_IX = "/home/antonio/Scrivania/sshdir_loc/data_fetched/org.ix";
	
	private final static String MISC_IX = "/home/antonio/Scrivania/sshdir_loc/data_fetched/misc.ix";

}
