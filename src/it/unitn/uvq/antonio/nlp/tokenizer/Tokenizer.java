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
		
		List<AnnotationI> aList = new ArrayList<>();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(str),
				new CoreLabelTokenFactory(), "");
		for (CoreLabel label; ptbt.hasNext(); ) { 
			label = ptbt.next();
			AnnotationI a = new TextAnnotation(label.value(), label.beginPosition(), label.endPosition());
			aList.add(a);
		}
		return aList;
	}

	/**
	 * Returns the list of tokens in the specified string.
	 * 
	 * @param str The string to tokenize
	 * @return The list of tokens
	 * @throws NullPointerExeptio if str is null
	 */
	public List<String> tokenize(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<String> tokens = new ArrayList<>();
		for (AnnotationI a : annotate(str)) { tokens.add(((TextAnnotationI) a).text()); }
		return tokens;
	}
	
	private final static Tokenizer INSTANCE = new Tokenizer();	

}
