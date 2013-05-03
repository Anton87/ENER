package it.unitn.uvq.antonio.nlp.annotation;

import it.unitn.uvq.antonio.util.IntRange;

public interface AnnotationI {
	
	IntRange span();
	
	int start(); 
	
	int end();
	
}
