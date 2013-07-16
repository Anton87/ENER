package it.unitn.uvq.antonio.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import svmlighttk.SVMVector;

import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Pair;
import it.unitn.uvq.antonio.util.tuple.Triple;

/**
 * Builds a new example based on the  inserted parameters. 
 * 
 * @author antonio Antonio Uva 145683
 *
 */
public abstract class ExampleBuilder {
	
	/**
	 * Set the example paragraph.
	 * 
	 * @param paragraph A string holding the example paragraph
	 * @return this
	 */
	public ExampleBuilder setParagraph(String paragraph) {
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
	public ExampleBuilder setSentence(String sentence) {
		if (sentence == null) {
			throw new NullPointerException("sentence is null");
		}
		this.sentence = sentence;
		return this;
	}
	
	/**
	 * Set the span of the entity in the sentence.
	 * 
	 * @param entitySpan An IntRage holding the span of the entity
	 * @return this
	 */
	public ExampleBuilder setEntitySpan(IntRange entitySpan) {
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
	 * @throw NullPointerException if notableTypes is null
	 */
	public ExampleBuilder setNotableTypes(List<String> notableTypes) { 
		if (notableTypes == null) {
			throw new NullPointerException("notableTypes is null");
		}
		this.notableTypes = notableTypes;
		return this;
	}
	
	/**
	 * Set the entity's notableFor.
	 * 
	 * @param notableFor A string holding the entity notableFor name
	 * @return this
	 */
	public ExampleBuilder setNotableFor(String notableFor) { 
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
	
	
	protected String buildBOW(String text) { 
		assert text != null;
		
		StringBuilder sb = new StringBuilder("(BOW ");
		for (Triple<String, Integer, Integer> triple : tokenizer.tokenizePTB3Escaping(text)) { 
			sb.append("(" + triple.first() + " *)");
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Returns the features vector whith features corresponding to
	 *  notbale_types and notable_for attribute values.
	 *  
	 * @param notableTypes A list of notable types names
	 * @param notableFor A String holding the notableFor name
	 * @return The features vector corresponding to notable types
	 * @throw NullPointerException if (notable_types == null)
	 */
	protected String buildVectorOfNotableTypesIds(List<String> notableTypes, String notableFor) { 
		if (notableTypes == null) throw new NullPointerException("notableTypes is null");
		
		StringBuilder sb = new StringBuilder("|BV| ");
		fids = getFids(notableTypes, notableFor);
		
		for (Integer fid : fids) { 
			sb.append(fid.toString() + ":1 ");
		}
		sb.append("|EV|");
		return sb.toString();
	}
	
	/** Write notable types in the info string following the notable types sorting. */ 
	protected String buildInfoStringFollowingFidsSorting(List<Integer> fids, List<String> notableTypes, String notableFor) {
		assert fids != null;
		
		StringBuilder sb = new StringBuilder();
		// Add info string about the notable types whith fid
		int i = 0;
		while (i < fids.size()) {
			Integer fid = fids.get(i);
			String notable = id2Feature.get(fid);
			sb.append(i++ == 0 ? ("# " + notable) : ("," + notable));
		}
		// Add info string about the remaining notable types (such as /m/... or /user/... )
		for (String notable : unique(notableTypes, notableFor)) {
			if (feature2Id.containsKey(notable)) { continue; }
			sb.append(i++ == 0 ? ("# " + notable) : ("," + notable));
		}
		return sb.toString();
	}
	
	// Returns a list of uniques elements
	private <E> Set<E> unique(List<E> elems, E elem) { 
		assert elems != null;
		
		Set<E> uniques = new HashSet<>(elems);
		if (elem != null) { uniques.add(elem); }
		return uniques;
	}
	
	/**
	 * Builds a new info string.
	 */
	protected String buildInfoString(List<String> notableTypes, String notableFor) {
		assert notableTypes != null;
		
		Set<String> allNotableTypes = new HashSet<>(notableTypes);
		if (notableFor != null) { allNotableTypes.add(notableFor); }
	
		StringBuilder sb = new StringBuilder();
		int  i = 0;
		for (String notableType : allNotableTypes) { 
			sb.append(i++ == 0 ? ("# " + notableType) : ("," + notableType));
		}
		return sb.toString();
	}
	
	
	/**
	 * Returns the feature id (fid) of this feature.
	 * 
	 * @param feature A string holding the feature name
	 * @return An integer holding the feature id
	 * @throw NullPointerException if (feature == null)
	 */
	protected Integer getFid(String feature) { 
		if (feature == null) {
			throw new NullPointerException("feature is null");
		}
		return feature2Id.get(feature);
	}
	
	/**
	 * Returns the list of feature ids (fid)s corrisponging to
	 *  the supplied list of notable_types and notable_for.
	 *  
	 * @param notableTypes A list notable_types names
	 * @param notableFor A string holding the notable_for name
	 * @return The list of integer corresponding 
	 * @throw NullPointerException if (notableTypes == null)
	 */
	protected List<Integer> getFids(List<String> notableTypes, String notableFor) {
		if (notableTypes == null) {
			throw new NullPointerException("notableTypes is null");
		}
		List<Integer> fids = new ArrayList<>();
		
		// Store notable_types feature ids. 
		for (String notableType : notableTypes) { 
			Integer fid = getFid(notableType);
			if (fid != null && !fids.contains(fid)) { fids.add(fid); }
		}
		
		// Store the notable_for feature id. 
		if (notableFor != null) {
			Integer fid = getFid(notableFor);
			if (fid != null && !fids.contains(fid)) { fids.add(fid); }
		}
		Collections.sort(fids);
		return fids;
	}	
	
	private final static BiMap<String, Integer> loadFeatures(String filepath) {
		assert filepath != null;
		
		BiMap<String, Integer> feature2Id = HashBiMap.create();
		if (!existsFile(filepath)) { 
			System.err.println("File not found: " + filepath);
			return feature2Id;
		}
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(
					new FileReader(filepath));
			int fid = 1; // feature id
			for (String line = null; (line = in.readLine()) != null; ) {
				if (!line.startsWith("/m/") && !line.startsWith("/user/")) {
					feature2Id.put(line, fid++);
				}
			}
			return feature2Id;
		} catch (IOException e) {
			System.err.println("I/O Error reading file: " + filepath);
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
		return feature2Id;		 
	}
	
	/* Checks whether the files does exist or not. */
	private static boolean existsFile(String pathname) { 
		assert pathname != null;
		
		return new File(pathname).exists();
	}	
	
	protected String paragraph = null;
	
	protected String sentence = null;
	
	protected IntRange entitySpan = null;
	
	protected List<String> notableTypes = null;
	
	protected List<Integer> fids = null;
	
	protected String notableFor = null;
	
	private final static String NOTABLE_TYPES_FILEPATH = "/home/antonio/Scrivania/notable_types.txt";
	
	private final static BiMap<String, Integer> feature2Id = loadFeatures(NOTABLE_TYPES_FILEPATH);
	
	private final static Map<Integer, String> id2Feature = feature2Id.inverse();
	
	private Tokenizer tokenizer = Tokenizer.getInstance();

}
