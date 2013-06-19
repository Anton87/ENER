package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.IntRange;
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
	
	protected String paragraph = null;
	
	protected String sentence = null;
	
	protected IntRange entitySpan = null;
	
	private Tokenizer tokenizer = Tokenizer.getInstance();

}
