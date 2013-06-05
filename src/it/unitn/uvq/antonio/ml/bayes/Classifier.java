package it.unitn.uvq.antonio.ml.bayes;

import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;

public class Classifier implements ClassifierI {
	
	public Classifier(Model model) {
		if (model == null) throw new NullPointerException("model: null");
		
		this.model = model;
	}

	@Override
	public double classify(String text, boolean stem) {
		if (text == null) throw new NullPointerException(text);
		
		double totProb = .0;
		List<String> words = tokenize(text);
		for (String word : words) { 
			word = word.toLowerCase();
			if (stem) { word = stem(word); }
			totProb += model.prob(word);
		}
		return totProb;
	}
	
	public Model model() { return model; }
	
	private String stem(String word) {
		assert word != null;
		
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();	
	}
	
	private List<String> tokenize(String str) { 
		assert str != null;
		
		List<String> toks = new ArrayList<>();
		for (Triple<String, Integer, Integer> triple : tokenizer.tokenize(str)) {
			toks.add(triple.first());
		}
		return toks;
	}
	
	private static SnowballStemmer newStemmer(String lang) { 
		assert lang != null;
		
		SnowballStemmer stemmer = null;		
		try {
			@SuppressWarnings("rawtypes")
			Class stemClass = Class.forName("org.tartarus.snowball.ext." + lang + "Stemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();			
		} catch(InstantiationException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		return stemmer;
	}
	
	private final Model model;
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	
	private static SnowballStemmer stemmer = newStemmer("english");

}
