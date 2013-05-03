package it.unitn.uvq.antonio.nlp.annotation;

import it.unitn.uvq.antonio.nlp.ner.NamedEntityType;

public interface NeAnnotationI extends TextAnnotationI {
		
	/**
	 * Returns the type of the Named Entity (e.g. PERSON, LOCATION, ORGANIZATION).
	 * 
	 * @return A string holding the type of the Named Entity
	 */
	NamedEntityType type();

}
