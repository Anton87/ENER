package it.unitn.uvq.antonio.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.base.Joiner;

public class FileUtils {
	
	public static Object readObject(String inFile) { 
		if (inFile == null) throw new NullPointerException("inFile: null");
		
		Object o = null;
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(
					new FileInputStream(inFile));
			o = in.readObject();
		} catch (ClassNotFoundException e) { 
			logger.warning("Class not found.");
		} catch (FileNotFoundException e) {
			logger.warning("File not found: \"" + inFile + "\".");
		} catch (IOException e) {
			logger.warning("File reading error: \"" + inFile + "\".");
		} finally { 
			try {
				in.close();
			} catch (IOException e) { }
		}
		return o;
	}
	
	public static void writeObject(Object o, String outFile) { 
		if (o == null) throw new NullPointerException("o: null");
		if (outFile == null) throw new NullPointerException("outFile: null");
		
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(
					new FileOutputStream(outFile));
			out.writeObject(o);
		} catch (FileNotFoundException e) { 
			logger.warning("File not found: \"" + outFile + "\".");
		} catch (IOException e) { 
			logger.warning("I/O error.");
		} finally { 
			try {
				out.close();
			} catch (IOException e) { }
		}
	}
	
	public static void writeText(String text, String filepath) {
		if (text == null) throw new NullPointerException("text: null");
		if (filepath == null) throw new NullPointerException("filepath: null");
		
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(
					new FileWriter(filepath));
			out.write(text);
		} catch (FileNotFoundException e) {
			logger.warning("File not found: \"" + filepath + "\".");
		} catch (IOException e) { 
			logger.warning("I/O error.");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				logger.warning("I/O error.");
			}
		}	
	}
	
	public static <E> void writeElements(List<E> objects, String filepath) {
		if (objects == null) throw new NullPointerException("objects: null");
		if (filepath == null) throw new NullPointerException("filepath: null");
		
		writeObjects(objects, filepath);
	}
	
	private static void writeObjects(List<? extends Object> objects, String filepath) {
		assert objects != null;
		assert filepath != null;
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(
					new BufferedWriter(
							new FileWriter(filepath)));
			for (Object obj : objects) {
				out.println(obj);
			}
		} catch (IOException e) { 
			logger.warning("I/O error.");
		} finally {
			out.close();
		}
	}
	
	public static void writeLines(List<String> lines, String filepath) {
		if (lines == null) throw new NullPointerException("lines: null");
		if (filepath == null) throw new NullPointerException("filepath: null");
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(
					new BufferedWriter(
							new FileWriter(filepath)));
			for (String line : lines) {
				out.println(line);
			}
		} catch (IOException e) {
			logger.warning("I/O error.");
		} finally {
			out.close();
		}		
	}
	
	public static String readText(String filepath) {
		if (filepath == null) throw new NullPointerException("filepath: null");
		
		List<String> lines = readLines(filepath);
		return join(NEW_LINE, lines);
	}
	
	public static List<String> readLines(String filepath) {
		if (filepath == null) throw new NullPointerException("filepath: null");
		
		List<String> lines = new ArrayList<>();
		BufferedReader in = null;
		try {
			in =
					new BufferedReader(
							new FileReader(filepath));
			for (String line = null; (line = in.readLine()) != null; ) {
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
			logger.warning("File not found: \"" + filepath + "\".");
		} catch (IOException e) { 
			logger.warning("I/O error");
		} finally {
			try {
				in.close();
			} catch (IOException e) { 
				logger.warning("I/O error");
			}
		}
		return Collections.unmodifiableList(lines);		
	}
	
	private static String join(String delim, Collection<String> parts) {
		assert delim != null;
		assert parts != null;
		
		return Joiner.on(delim).join(parts);
	}
	
	private final static String NEW_LINE = System.getProperty("line.separator");
	
	private static Logger logger = Logger.getLogger(FileUtils.class.getName());

}
