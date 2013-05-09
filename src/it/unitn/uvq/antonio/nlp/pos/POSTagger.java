package it.unitn.uvq.antonio.nlp.pos;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.Annotator;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Reads a sentence and assigns part-of-speech to each word and
 *  other tokens.
 *  
 * @author Antonio Uva 145683
 *
 */
public class POSTagger implements Annotator {
	
	/**
	 * Constructs a new POSTagger.
	 * 
	 */
	public POSTagger() {
		init();
	}
	
	/**
	 * Returns the singleton POSTagger instance. 
	 *
	 * @return The POSTagger instance
	 */
	public static POSTagger getInstance() { 
		return INSTANCE;
	}

	@Override
	public List<AnnotationI> annotate(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		return this.annotations;
	}
	
	/**
	 * Breaks the text into a list of TaggedWord tokens (i.e. the words with their tags).  
	 * 
	 * @param str A string holding the text to break into tokens
	 * @return The list of TaggedWord appearing in the text
	 * @throws NullPointerException if (str == null)
	 */
	public List<Triple<String, Integer, Integer>> postag(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<HasWord> words = sentTokenize(str);
		this.annotations = new ArrayList<>();
		List<Triple<String, Integer, Integer>> poss = new ArrayList<>();
		for (edu.stanford.nlp.ling.TaggedWord tw : tagger.tagSentence(words)) {
			//Quadruple<String, String, Integer, Integer> tagWord = 
			//		new SimpleQuadruple<String, String, Integer, Integer>(tw.word(), tw.tag(), tw.beginPosition(), tw.endPosition());
			Triple<String, Integer, Integer> pos = new SimpleTriple<>(tw.tag(), tw.beginPosition(), tw.endPosition());
			poss.add(pos);
			AnnotationI a = new TextAnnotation(tw.tag(), tw.beginPosition(), tw.endPosition());
			this.annotations.add(a);
		}
		return poss;
	}
	
	public List<Quadruple<String, String, Integer, Integer>> tag(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<HasWord> words = sentTokenize(str);
		List<Quadruple<String, String, Integer, Integer>> tagWords = new ArrayList<>();
		for (edu.stanford.nlp.ling.TaggedWord tw : tagger.tagSentence(words)) {
			Quadruple<String, String, Integer, Integer> tagWord = new SimpleQuadruple<>(tw.word(), tw.tag(), tw.beginPosition(), tw.endPosition());
			tagWords.add(tagWord);
			AnnotationI a = new TextAnnotation(tw.tag(), tw.beginPosition(), tw.endPosition());
			this.annotations.add(a);
		}
		return tagWords;		 
	}
	
	private List<HasWord> sentTokenize(String str) {
		assert str != null;
		
		List<HasWord> hasWords = new ArrayList<>();
		DocumentPreprocessor dp =
				new DocumentPreprocessor(
						new StringReader(str));
		for (Iterator<List<HasWord>> it = dp.iterator(); it.hasNext(); ) {
			hasWords.addAll(it.next());
		}
		return hasWords;
	}
	
	private void init() {
		tagger = new MaxentTagger(POSTAG_MODEL);
		assert tagger != null;
	}
	
	private final static POSTagger INSTANCE = new POSTagger();
				
	private final static String POSTAG_MODEL = "stanford-postagger/models/english-bidirectional-distsim.tagger";
	
	private MaxentTagger tagger;
	
	private List<AnnotationI> annotations = new ArrayList<>();

	@Override
	public List<AnnotationI> getAnnotations() { return this.annotations; }

}
