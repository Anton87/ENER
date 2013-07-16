package it.unitn.uvq.antonio.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds information about words distribution in a document.
 * 
 * @author Antonio Uva 145683
 *
 */
public class TFIDFModel {
	
	/**
	 * Builds a new TFIDF model instance.
	 * 
	 * @param filepath A string holding the model filepath
	 * @return A new TFIDF Model instance
	 * @throw NullPointerException if filepath is null
	 */
	public final static TFIDFModel newInstance(String filepath) {
		if (filepath == null) { 
			throw new NullPointerException("filepath is null");
		}
		
		Map<String, Integer> word2Count = new HashMap<>();
		String docNum = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new FileReader(filepath));
			in.readLine(); // skip docNum header
			docNum = in.readLine(); 
			in.readLine(); // skip word,count header
			for (String line = null; (line = in.readLine()) != null; ) {
				String[] values = line.replaceAll("\\s+$", "").split(CSV_SEPARATOR);
				String word = values[0];
				Integer count = Integer.parseInt(values[1]);
				word2Count.put(word, count);
			}			
		} catch (FileNotFoundException e) {		
			System.err.println("File not found: " + filepath);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("I/O Error while reading file: " + filepath);
			System.exit(-1);
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
		int documentsNumber = Integer.parseInt(docNum);
		return new TFIDFModel(word2Count, documentsNumber);
	}
	
	/**
	 * Returns the term document frequency.
	 * 
	 * @param term
	 * @return
	 */
	public int df(String term) { 
		if (term == null) {
			throw new NullPointerException("term is null");
		}
		int df = word2Count.containsKey(term)
					? 1 + word2Count.get(term)
					: 1;
		return df;
	}
	
	/** Returns the term inverse document frequency.
	 * 
	 * @param term
	 * @return 
	 */
	public double idf(String term) {
		if (term == null) {
			throw new NullPointerException("term is null");
		}
		return Math.log(documentsNumber / df(term));
	}
	
	public int documentsNumber() { 
		return documentsNumber;
	}
	
	boolean containsTerm(String term) { 
		assert term != null;
		
		return word2Count.containsKey(term); 
	}
	
	
	private TFIDFModel(Map<String, Integer> word2Count, int documentsNumber) {
		assert word2Count != null;
		assert documentsNumber >= 0;
		
		this.word2Count = word2Count;
		this.documentsNumber = documentsNumber;
	}

	private final static String CSV_SEPARATOR = ",";
	
	private final int documentsNumber;
	
	private final Map<String, Integer>  word2Count; 
	
	public final static void main(String[] args) { 
		String filepath = "/home/antonio/Scrivania/sshdir_loc/word_counts.csv";
		
		TFIDFModel tfidfModel = TFIDFModel.newInstance(filepath);
		System.out.println("docNum: " + tfidfModel.documentsNumber());
		
		System.out.println("df(film): " + tfidfModel.df("record"));
		System.out.println("idf(film): " + tfidfModel.idf("record"));
	}

}
