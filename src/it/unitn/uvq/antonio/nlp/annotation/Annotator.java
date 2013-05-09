package it.unitn.uvq.antonio.nlp.annotation;

import java.util.List;

/**
 * Scan a sentence and search for annotations.
 *   
 * @author Antonio Uva 145683
 *
 */
public interface Annotator {
	
	/**
	 * Returns the set of annotations found in the sentence.
	 * 
	 * @param str The sentence to search for annotations
	 * @return The list of annotations found
	 * @throws NullPointerException if (str == null)
	 */
	List<AnnotationI> annotate(String str);
	
	List<AnnotationI> getAnnotations();

}
