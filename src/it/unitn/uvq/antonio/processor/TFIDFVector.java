package it.unitn.uvq.antonio.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tartarus.snowball.SnowballStemmer;

import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.Triple;

/**
 * A class representing a simple TFIDF Vector.
 * 
 * @author Antonio Uva 145683.
 *
 */
public class TFIDFVector {
	
	/**
	 * Builds a new TFIDFVEctor instance for this sentence.
	 *  
	 * @param sentence A string holding the sentence
	 * @param tfidfModel A model holding information about the terms frequency distribution in a corpus.
	 * @return The TFIDF vector for this sentence
	 * @throw NullPointerExcepiton if (sentence == null) || (tfidfModel == null)
	 */
	public static TFIDFVector newInstance(String sentence, TFIDFModel tfidfModel) {
		if (sentence == null) {
			throw new NullPointerException("sentence is null");
		}
		if (tfidfModel == null) { 
			throw new NullPointerException("tfidfModel is null");
		}
		List<String> tokens = tokenize(sentence);
		TFIDFVector vector = newInstance(tokens, tfidfModel);
		return vector;
	}
	
	/**
	 * Builds a new TFIDFVector instance for this tokens sequence.
	 * 
	 * @param words A list of words 
	 * @param tfidfModel A model holding information about the terms frequency distribution in a corpus.
	 * @return The TFIDFVector for this sentence
	 * @throw NullPointerException if (tokens == null) || (tfidfModel == null)
	 */
	public static TFIDFVector newInstance(List<String> words, TFIDFModel tfidfModel) {
		if (words == null) { 
			throw new NullPointerException("words is null");
		}
		if (tfidfModel == null) { 
			throw new NullPointerException("tfidfModel is null");
		}
		TFIDFVector tfidfVector = new TFIDFVector(words, tfidfModel);
		return tfidfVector;
	}
	
	/**
	 * Returns the cosine similarity between two vectors.
	 * 
	 * @param other The other TFIDF vector
	 * @return A double holding the similarity score between the two vectors
	 * @throw NullPointerException if other is null
	 */
	public double cos(TFIDFVector other) {
		if (other == null) { 
			throw new NullPointerException("other is null");
		}
		
		List<String> otherUniqueWords = other.uniqueWords();
		
		TFIDFVector a = this;
		TFIDFVector b = other;
		
		if (a.uniqueWords.size() > otherUniqueWords.size()) { 
			a = other;
			b = this;
		}
		
		double sim = 0;
		for (String term : a.uniqueWords) {
			double aTfidf = a.tf(term) * a.idf(term);
			double bTfidf = b.tf(term) * b.idf(term);
			
			sim += aTfidf * bTfidf;			
		}
	
		double aMagnitude = a.magnitude();
		double bMagnitude = b.magnitude();
		
		if (aMagnitude != .0 && bMagnitude != 0) {
			sim /= (aMagnitude * bMagnitude);
		}
		return sim;		
	}
	
	/* Compute the magnitude of the vector. */
	public double magnitude() {
		double sum = 0;
		for (String term : uniqueWords) {
			sum += Math.pow(tf(term)*idf(term), 2);
		}
		return Math.sqrt(sum);
	}
	
	/** Tokenize a sentence. */
	private static List<String> tokenize(String sentence) {
		assert sentence != null;
		
		List<String> tokens = new ArrayList<>();
		for (Triple<String, Integer, Integer> tokenSpan : tokenizer.tokenize(sentence)) {
			String token = tokenSpan.first();
			tokens.add(token);
		}	
		return tokens;
	}
	
	private TFIDFVector(List<String> words, TFIDFModel tfidfModel) {
		this.tfidfModel = tfidfModel;
		this.words = normalize(words); // Pre-process words: lowercase, stem, etc..
		
		word2Count = count(this.words);
		uniqueWords = unique(this.words);		
	}
	
	private List<String> normalize(final List<String> words) {
		assert words != null;
		
		List<String> normalizedWords = new ArrayList<>();
		for (String word : words) {
			String normWord = normalize(word);
			if (tfidfModel.containsTerm(normWord)) { 
				normalizedWords.add(normWord);
			}
		}
		return normalizedWords;
	}
	
	private String normalize(String word) { 
		assert word != null;
		
		String newWord = word;
		newWord = word.toLowerCase();
		newWord = stem(word);
		return newWord;		
	}
	
	/*
	private String stem(String word) { 
		assert word != null;
		
		System.out.print("stemming word: " + word + "... ");
		
		Stemmer stemmer = new Stemmer();
		for (int i = 0; i < word.length(); i++) { 
			stemmer.add(word.charAt(i));
		}
		stemmer.stem();
		String stem = new String(stemmer.getResultBuffer());
		System.out.println(stem);
		return stem;
	}
	*/
	
	public double tf(String term) { 
		if (term == null) { throw new NullPointerException("term is null"); }
		
		int tf = word2Count.containsKey(term) 
					? word2Count.get(term)
					: 0;
		return tf;
	}
	
	/**
	 * Returns the idf value of the term.
	 * 
	 * @param term
	 * @return
	 */
	public double idf(String term) {
		if (term == null) { throw new NullPointerException("term is null"); }
		
		return tfidfModel.idf(term);
	}
	
	
	public List<String> words() { 
		return Collections.unmodifiableList(words);
	}
	
	public List<String> uniqueWords() { 
		return Collections.unmodifiableList(uniqueWords);
	}
	
	/** Returns the sorted list of unique words in a dict */
	private List<String> unique(final List<String> words) {
		assert words != null;
		List<String> uniqueWords = new ArrayList<>();
		
		for (String word : words) { 
			if (!uniqueWords.contains(word)) { 
				uniqueWords.add(word);
			}
		}
		Collections.sort(uniqueWords);
		return uniqueWords;
	}
	
	/* Count the words occurrence in a list of words and returns the
	 * word2dict counts dictionary.
	 */
	private Map<String, Integer> count(final List<String> words) { 
		assert words != null;
		
		Map<String, Integer> word2Count = new HashMap<>();
		
		for (String word : words) {
			Integer count = word2Count.get(word);
			count = count == null ? 0 : count;
			word2Count.put(word, count + 1);
		}
		return word2Count;
	}
	
	public String stem(String word) {
		assert word != null;
		
		//System.out.print("stemming word: " + word + "... ");
		stemmer.setCurrent(word);
		stemmer.stem();
		String stem = stemmer.getCurrent();
		//System.out.println("stem: " + stem);
		return stem;
	}
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	
	private final TFIDFModel tfidfModel;
	
	private final List<String> words;
	
	private final List<String> uniqueWords;
	
	private final Map<String, Integer> word2Count;
	
	private static SnowballStemmer initStemmer() {
		SnowballStemmer stemmer = null;
		try {
			stemmer = (SnowballStemmer) Class.forName("org.tartarus.snowball.ext.englishStemmer").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return stemmer;
	}
	
	private static SnowballStemmer stemmer = initStemmer();
	
	public final static void main(String[] args) { 
		/*	
		//String sent = "Iceland is a Welsh supermarket chain operating in the United Kingdom and Ireland. Iceland's primary product lines include frozen foods, such as frozen prepared meals and frozen vegetables. The company has an approximate 1.8% share of the UK food market.";
		String sent2 = "There are a number of budget and discount retailers including three branches of Boyes, Primark, Peacocks, Poundland and Wilkinsons have branches in the city. Hull has a good selection of supermarkets, including several branches of Tesco, Sainsbury's, the Co-operative and budget food stores including Heron Foods and Iceland.";
		
		
		TFIDFModel tfidfModel = TFIDFModel.newInstance("/home/antonio/Scrivania/sshdir_loc/word_counts.csv");
		TFIDFVector aVector = TFIDFVector.newInstance(sent, tfidfModel);
		System.out.println(aVector.uniqueWords);
		TFIDFVector bVector = TFIDFVector.newInstance(sent2, tfidfModel);
		System.out.println(bVector.uniqueWords);
		
		double sim = aVector.cos(bVector);
		System.out.println(sim);
		*/
	}

}
