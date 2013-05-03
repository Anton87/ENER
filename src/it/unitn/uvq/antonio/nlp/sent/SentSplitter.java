package it.unitn.uvq.antonio.nlp.sent;

import it.unitn.uvq.antonio.nlp.annotation.Annotation;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.Annotator;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;;

/**
 * Breaks a text such as a paragraph into sentences.
 * 
 * @author Antonio Uva 145683
 *
 */
public class SentSplitter implements Annotator {
	
	/**
	 * Returns the singleton SentSplitter instance.
	 * 
	 * @return The this SentSplitter
	 */
	public static SentSplitter getInstance() { 
		return INSTANCE;
	}	
	
	@Override
	public List<AnnotationI> annotate(String str) { 
		if (str == null) throw new NullPointerException("str: null");
		
		List<AnnotationI> annotations = new ArrayList<>();
		List<Triple<String, Integer, Integer>> sentsInfo = getSentsInfo(str);
		for (Triple<String, Integer, Integer> sentInfo : sentsInfo) {
			AnnotationI a = new Annotation(sentInfo.second(), sentInfo.third());
			annotations.add(a);
		}
		return annotations;
	}
	
	/**
	 * Split the this text into sentences.
	 * 
	 * @param str A string holding the text to break 
	 *  into sentences
	 * @return The list of split sentences
	 * @throws NullPointerException if (str == null)
	 */
	public List<String> split(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<String> sentences = new ArrayList<>();
		List<Triple<String, Integer, Integer>> sentsInfo = getSentsInfo(str);
		for (Triple<String, Integer, Integer> sentInfo : sentsInfo) {
			String sent = sentInfo.first();
			sentences.add(sent);
		}
		return sentences;
	}	
	
	private List<Triple<String, Integer, Integer>> getSentsInfo(String str) { 
		assert str != null;
		
		DocumentPreprocessor dp =
				new DocumentPreprocessor(
						new StringReader(str));
		dp.setTokenizerFactory(tokenizerFactory);
		
		List<Triple<String, Integer, Integer>> sentsInfo = new ArrayList<>();
		for (Iterator<List<HasWord>> it = dp.iterator(); it.hasNext(); ) {
			List<HasWord> words = it.next();
			Triple<String, Integer, Integer> sentInfo = getSentInfo(words, str);
			sentsInfo.add(sentInfo);
		}
		return sentsInfo;
	}
	
	private Triple<String, Integer, Integer> getSentInfo(List<HasWord> words, String str) {
		assert words != null;
		assert str != null;
		
		int beginPos = ((CoreLabel) words.get(0)).beginPosition();
		int endPos = ((CoreLabel) words.get(words.size() - 1)).endPosition();
		String sent = str.substring(beginPos, endPos);
		Triple<String, Integer, Integer> sentInfo = new SimpleTriple<>(sent, beginPos, endPos);
		return sentInfo;		
	}
	
	private final static SentSplitter INSTANCE = new SentSplitter();
	
	private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(false, true);

}
