package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.tartarus.snowball.SnowballStemmer;

/**
 * Builds a new example based on the inserted parameters.
 * 
 * @author antonio Antonio Uva 145683
 *
 */
public abstract class WordIdExampleBuilder {
	
	/**
	 * Builds a new word id examples builder.
	 * 
	 * @param indexFilepath The filepath of the word2Id dict
	 */
	public WordIdExampleBuilder(String indexFilepath) {
		if (indexFilepath == null) { 
			throw new NullPointerException("indexFilepath is null");
		}
		word2Id = readIndex(indexFilepath);	
	}
	
	/**
	 * Set the example paragraph.
	 * 
	 * @param paragraph A string holding the example paragraph
	 * @return this
	 */
	public WordIdExampleBuilder setParagraph(String paragraph) {
		if (paragraph == null) { 
			throw new NullPointerException("paragraph is null");
		}
		this.paragraph = paragraph;
		return this;
	}
	
	/**
	 * Set the example sentence.
	 * 
	 * @param sentence A string holding the example sentence
	 * @return this
	 */
	public WordIdExampleBuilder setSentence(String sentence) { 
		if (sentence == null) {
			throw new NullPointerException("sentence is null");
		}
		this.sentence = sentence;
		return this;
	}
	
	/**
	 * Set the span of the entity in the sentence.
	 * 
	 * @param entitySpan An IntRange holding the span of the entity
	 * @return this
	 */
	public WordIdExampleBuilder setEntitySpan(IntRange entitySpan) {
		if (entitySpan == null) {
			throw new NullPointerException("entitySpan is null");
		}
		this.entitySpan = entitySpan;
		return this;
	}
	
	/**
	 * Set the entity's notable types.
	 * 
	 * @param notableTypes A list of notable types names
	 * @return this
	 */
	public WordIdExampleBuilder setNotableTypes(List<String> notableTypes) {
		if (notableTypes == null) { 
			throw new NullPointerException("notableTypes is null");
		}
		this.notableTypes = notableTypes;
		return this;
	}
	
	/**
	 * Set the entity notableFor.
	 * 
	 * @param notableFor A string holding the entity notableFor name
	 * @return this
	 */
	public WordIdExampleBuilder setNotableFor(String notableFor) {
		if (notableFor == null) {
			throw new NullPointerException("notableFor is null");
		}
		this.notableFor = notableFor;
		return this;
	}

	/**
	 * Build and return the example.
	 *  If the example cannot be built, null value is returned instead.
	 *  
	 * @return A string holding the build example
	 */
	public abstract String build();
	
	/**
	 * Tokenize the sentence.
	 * 
	 * @param sent A string holding the sentence text
	 * @return The sentence tokens
	 */
	protected List<String> tokenize(String sent) { 
		if (sent == null) { 
			throw new NullPointerException("sent is null");
		}
		
		List<String> tokens = new ArrayList<>();
		for (Triple<String, Integer, Integer> triple : tokenizer.tokenizePTB3Escaping(sent)) {
			String token = triple.first();
			tokens.add(token);
		}
		return tokens;		
	}
	
	/**
	 * Build the word2Ids vec.
	 * 
	 * @param tokens The list of tokens
	 * @return A string holding the tokens vec
	 * @throw NullPointerException if (tokens == null)
	 */
	protected String buildWordIdsVec(List<String> tokens) { 
		if (tokens == null) { 
			throw new NullPointerException("tokens is null");
		}
		
		Map<Integer, String> wordId2Lemma = new HashMap<>();
		List<Integer> wordIds = new ArrayList<>();
		
		List<String> lemmas = new ArrayList<>();
		for (String token : tokens) { 
			// Build the lemmas list
			String lemma = lemmatize(token);
			lemmas.add(lemma);
			
			// Get the wordId for the lemma
			Integer wordId = word2Id.get(lemma);
			
			// Build the wordId -> lemma list
			wordId2Lemma.put(wordId, lemma);
			
			// Get the lemmas' wordIds
			if (wordId != null && !wordIds.contains(wordId)) {
				wordIds.add(wordId);
			}			
		}
		
		Collections.sort(wordIds);
		
		StringBuilder wordIdsVec = new StringBuilder();

		for (Integer wordId : wordIds) { 
			String lemma = wordId2Lemma.get(wordId);
			wordIdsVec.append(wordId + ":" + Collections.frequency(lemmas, lemma) + " ");
		}
		return wordIdsVec.toString();
	}
	
	/**
	 * Build a Bag-of-Words (BOW) Tree from a list of tokens.
	 *  
	 * @param tokens The list of tokens
	 * @return A string holding the BOW Tree
	 */
	protected String buildBOW(List<String> tokens) { 
		if (tokens == null) { 
			throw new NullPointerException("tokens is null");
		}
		StringBuilder sb = new StringBuilder("(BOW ");
		for (String token : tokens) { 
			sb.append("(" + token + " *)");
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Build and returns an info string containing the
	 *  notable types for this example.
	 *   
	 * @param notableTypes The list of notable types
	 * @param notableFor A string holding the type notableFor
	 * @return The new info string
	 */
	protected String buildInfoString(List<String> notableTypes, String notableFor) {
		if (notableTypes == null) { 
			throw new NullPointerException("notableTypes is null");
		} 
		Set<String> notables = new TreeSet<>();
		notables.addAll(notableTypes);
		if (notableFor != null) notables.add(notableFor);
		StringBuilder sb = new StringBuilder("# ");
		for (Iterator<String> it = notables.iterator(); it.hasNext(); ) {
			String notable = it.next();
			sb.append(notable + (it.hasNext() ? "," : ""));
		}
		return sb.toString();
	}
	
	/* Reduce to root form. */
	private String lemmatize(String word) {
		return stem(word.toLowerCase().trim());
	}
	
	/**
	 * Read the index and returns the word2id map.
	 */
	private Map<String, Integer> readIndex(String filepath) {
		assert filepath != null;
		
		Map<String, Integer> word2Id = new HashMap<>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new FileReader(filepath));
			int lineNum = 1;
			for (String line = null; (line = in.readLine()) != null; lineNum++) {
				String word = line.trim();
				word2Id.put(word, lineNum);
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + filepath);
			System.exit(-1);
		} catch (IOException e) { 
			System.err.println("I/O Error: " + filepath);
			System.exit(-1);
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
		return word2Id;
	}
	
	
	/** Returns the stemmed word. */
	private static String stem(String word) { 
		assert word != null;
		
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent(); 
	}
	
	/**
	 * Load and returns the specified Porter Stemmer. 
	 */
	private static SnowballStemmer initStemmer(String lang) { 
		assert lang != null;
		
		@SuppressWarnings("rawtypes")
		Class stemClass = null;
		try {
			stemClass = Class.forName("org.tartarus.snowball.ext." + lang + "Stemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return stemmer;
	}
	
	private final Map<String, Integer> word2Id;
	
	protected String paragraph = null;
	
	protected String sentence = null;
	
	protected IntRange entitySpan = null;
	
	protected List<String> notableTypes = null;
	
	protected String notableFor = null;
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	
	private static SnowballStemmer stemmer = initStemmer("english");

}
