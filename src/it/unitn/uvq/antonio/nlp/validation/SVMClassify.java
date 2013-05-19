package it.unitn.uvq.antonio.nlp.validation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import svmlighttk.SVMLightTK;

import com.google.common.base.Joiner;

public class SVMClassify {
	
	public static void main(String[] args) { 
		
	    String modelFiles = args[0];
	    String testFiles = args[1];
	    String predFiles = args[2];
	    
	    System.out.println("modelFiles:    \"" + modelFiles + "\"");
	    System.out.println("testFiles: \"" + testFiles + "\"");
	    System.out.println("classFiles:\"" + predFiles + "\"");
	    
	    SVMClassify svm_classify = new SVMClassify(modelFiles);
	    svm_classify.classifyFiles(modelFiles, testFiles, predFiles);
	}	
	
	public SVMClassify(String modelFiles) {
		if (modelFiles == null) throw new NullPointerException("modelFiles: null");
		
		this.classifiers = init(modelFiles);
	}
	
	public void classifyFiles(String modelFiles, String testFiles, String predFiles) {
		if (modelFiles == null) throw new NullPointerException("modelFiles: null");
		if (testFiles == null) throw new NullPointerException("testFiles: null");
		if (predFiles == null) throw new NullPointerException("predFiles: null");
	
		if (!exists(predFiles)) { 
			mkdirs(predFiles);
		}
		for (String filename : list(testFiles)) { 
			if (!filename.endsWith("-types.txt")) {
				System.out.println("(II) testFile: " + filename + ".");
				String testFile = pathjoin(testFiles, filename);
				String predFile = pathjoin(predFiles, filename);
				classifyFile(testFile, predFile);
			}
		}
	}
	
	private String pathjoin(String parent, String child) {
		return new File(parent, child).toString();
	}
	
	private boolean exists(String pathname) { 
		assert pathname != null;
		
		return new File(pathname).exists();				
	}
	
	private boolean mkdirs(String pathname) { 
		assert pathname != null;
		
		return new File(pathname).mkdirs();
	}
	
	private List<SVMClassifier> init(String modelFiles) {
		assert modelFiles != null;
		
		List<SVMClassifier> classifiers = new ArrayList<>();
		File file = new File(modelFiles);
		for (String modelFile : file.list()) { 
			String filename  = getFilenameWithoutExt(modelFile);
			String className = urldecode(filename);
			System.out.print("(II): Initing SVMClassifier for class=\"" + className + "\"... ");
			SVMClassifier classifier = new SVMClassifier(className, modelFile);
			classifiers.add(classifier);
			System.out.println("Done.");
		}
		return classifiers;
	}
	
	private List<String> list(String pathname) {
		assert pathname != null;
			
		List<String> filenames = new ArrayList<>();
		File file = new File(pathname);
		for (File child : file.listFiles()) {
			filenames.add(child.getName());
		}
		return filenames;
	}
	
	@SuppressWarnings("deprecation")
	private String urldecode(String str) { 
		assert str != null;
		
		return URLDecoder.decode(str);
	}
	
	private String getFilenameWithoutExt(String filepath) {
		assert filepath != null;
		
		int start = filepath.lastIndexOf(File.separatorChar) + 1;
		int end = filepath.lastIndexOf(".");
		end = end >= 0 ? end : filepath.length();
		return filepath.substring(start, end);
	}
	
	public void classifyFile(String testFile, String predFile) {
		if (testFile == null) throw new NullPointerException("testFile: null");
		if (predFile == null) throw new NullPointerException("predFile: null");
		
		List<String> examples = readLines(testFile);
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(
					new FileOutputStream(predFile));
			for (String example : examples) {
				List<String> predictions = new ArrayList<>();
				for (SVMClassifier classifier : classifiers) {
					double score = classifyExample(classifier, example);
					if (score > 0) { predictions.add(classifier.className); } 
				}
				out.println(join(SEPARATOR, predictions));				
			}
		} catch (FileNotFoundException e) {
			System.out.println("(EE): File not found: \"" + predFile + "\"");
		} finally {
			out.close();
		}
	}
	
	private List<String> readLines(String filepath) { 
		assert filepath != null;
		
		BufferedReader in = null;
		List<String> lines = new ArrayList<>();
		try {
			in = new BufferedReader(
					new FileReader(filepath));
			for (String line = null; (line = in.readLine()) != null; ) {
				lines.add(line);
			}
		} catch (IOException e) {
			System.err.println("(EE): I/O error: \"" + filepath + "\"");
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
		return lines;
	}
	
	public double classifyExample(SVMClassifier classifier, String example) {
		if (classifier == null) throw new NullPointerException("classifier: null");
		if (example == null) throw new NullPointerException("example: null");
		
		return classifier.classify(example);
	}
	
	private static class SVMClassifier {
	
		private SVMClassifier(String className, String modelFile) { 
			assert className != null;
			assert modelFile != null;
		
			this.className = className;
			this.model = new SVMLightTK(modelFile);
		}
		
		double classify(String example) {
			if (example == null) throw new NullPointerException("className: null");
			
			return model.classify(example);
		}
		
		private final String className;
		
		private final SVMLightTK model;
		
	}
	
	private String join(String sep, List<String> parts) {
		assert sep != null;
		assert parts != null;
		
		return Joiner.on(sep).join(parts);		
	}
	
	private final static String SEPARATOR = "\t";
	
	private final List<SVMClassifier> classifiers;

}
