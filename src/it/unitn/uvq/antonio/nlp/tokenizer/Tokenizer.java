package it.unitn.uvq.antonio.nlp.tokenizer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.Annotator;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

/**
 * Divides a text into a sequence of tokens 
 *  which correspond to "words."
 *  
 * @author Antonio Uva 145683
 *
 */
public class Tokenizer implements Annotator {
	
	/**
	 * Return the singleton tokenizer instance.
	 * 
	 * @return This tokenizer instance
	 */
	public static Tokenizer getInstance() { return INSTANCE; }
	
	
	@Override
	public List<AnnotationI> annotate(String str) {
		if (str == null) throw new NullPointerException();
		
		return getAnnotations();
	}
	
	public List<Triple<String, Integer, Integer>> tokenize(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		return tokenize(str, false);
	}
	
	public List<Triple<String, Integer, Integer>> tokenizePTB3Escaping(String str) {
		if (str ==  null) throw new NullPointerException("str: null");
		
		return tokenize(str, true);
	}
	
	private List<Triple<String, Integer, Integer>> tokenize(String str, boolean ptb3Escaping) {
		assert str != null;
		
		this.annotations = new ArrayList<>();
		List<Triple<String, Integer, Integer>> tokens = new ArrayList<>();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(str),
				new CoreLabelTokenFactory(), "");
		for (CoreLabel label; ptbt.hasNext(); ) { 
			label = ptbt.next();
			int start = label.beginPosition();
			int end = label.endPosition();
			String word = ptb3Escaping ? label.value() : str.substring(start, end);
			Triple<String, Integer, Integer> token = new SimpleTriple<>(word, start, end);
			tokens.add(token);
			AnnotationI a = new TextAnnotation(word, start, end);
			this.annotations.add(a);
		}
		return tokens;
	}	

	@Override
	public List<AnnotationI> getAnnotations() { return this.annotations; }	
	
	private final static Tokenizer INSTANCE = new Tokenizer();
	
	private List<AnnotationI> annotations = new ArrayList<>();
	
}
