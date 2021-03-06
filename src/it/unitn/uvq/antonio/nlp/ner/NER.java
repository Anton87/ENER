package it.unitn.uvq.antonio.nlp.ner;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.Annotator;
import it.unitn.uvq.antonio.nlp.annotation.NeAnnotation;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuadruple;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This NER (Named Entity Recognizer) labels sequence of words in a text
 *  which are the name of things such as person, organization and location names. 
 *
 * @author Antonio Uva 145683
 *
 */
public class NER implements Annotator {
	
	/**
	 * Returns the singleton instance of the NER.
	 * 
	 * @return This NER
	 */
	public static NER getInstance() { 
		return INSTANCE;
	}
	
	@Override
	public List<AnnotationI> annotate(String str) {
		if (str == null) throw new NullPointerException("str: null");
		/*
		
		this.annotations = new ArrayList<>();
		for (Quadruple<String, String, Integer, Integer> ne : classify(str)) {
			AnnotationI a  = toAnnotation(ne);
			this.annotations.add(a);
		}
		return this.annotations;
		*/
		classify(str);
		return this.annotations;
	}
	
	public List<AnnotationI> getAnnotations() { return this.annotations; }
		
	/**
	 * Returns a list of triples holding the info about NEs found.
	 * 
	 * @param str A string holding the text to search for named entities
	 * @return The list of triples holding the NE's names and boundaries
	 * @throws NullPointerException if (str == null)
	 */
	public List<Quadruple<String, String, Integer, Integer>> classify(final String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		return classifyToCharacterOffsets(str);
	}
	
	private List<Quadruple<String, String, Integer, Integer>> classifyToCharacterOffsets(final String str) { 
		assert str != null;
		
		this.annotations = new ArrayList<>();
		List<Quadruple<String, String, Integer, Integer>> qples = new ArrayList<>();
		for (edu.stanford.nlp.util.Triple<String, Integer, Integer> triple : classifier.classifyToCharacterOffsets(str)) {
			String name = str.substring(triple.second, triple.third);
			Quadruple<String, String, Integer, Integer> qple = new SimpleQuadruple<>(name, triple.first, triple.second, triple.third);
			AnnotationI a = new NeAnnotation("NE", NamedEntityType.valueOf(triple.first), new IntRange(triple.second, triple.third));
			qples.add(qple);	
			this.annotations.add(a);
		}
		return qples;
	}

	private final static NER INSTANCE = new NER();
	
	private final static String SEQUENCE_MODEL = "stanford-ner/classifiers/english.all.3class.distsim.crf.ser.gz";
	
	private List<AnnotationI> annotations = new ArrayList<>();	
	
	private static AbstractSequenceClassifier <CoreLabel> classifier = 
			CRFClassifier.getClassifierNoExceptions(SEQUENCE_MODEL);
	
}
