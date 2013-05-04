package it.unitn.uvq.antonio.nlp.bow;

import it.unitn.uvq.antonio.util.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple Bag of Words (BOW) class.
 * 
 * @author Antonio Uva 14683
 *
 */
public class BOW {
	
	/**
	 * Constructs a new bow instance.
	 * 
	 * @param words A list of words
	 * @throws NullPointerException if words is null
	 */
	public BOW(List<String> words) { 
		if (words == null) throw new NullPointerException("words: null");
		
		this.words = words;
	}
	
	/**
	 * Create a new bow instance by using the
	 *  list of words in the words spans.
	 * 
	 * @param wordSpans A list of words spans 
	 * @return The new bow instance
	 * @throws NullPointerExcpetion if wordSpans is null
	 */
	public static BOW newInstance(List<Triple<String, Integer, Integer>> wordSpans) { 
		if (wordSpans == null) throw new NullPointerException("wordSpans: null");
		
		List<String> words = new ArrayList<>();
		for (Triple<String, Integer, Integer> wordSpan : wordSpans) { 
			String word = wordSpan.first();
			words.add(word);			
		}
		return new BOW(words);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(BOW ");
		for (String word : words) { 
			sb.append("(" + word + " *)");
		}
		sb.append(")");
		return sb.toString();
	}
	
	private final List<String> words;

}
