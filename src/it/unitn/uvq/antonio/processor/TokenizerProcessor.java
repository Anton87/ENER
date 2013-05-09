package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenizerProcessor extends AbstractProcessor {
	
	@Override
	public Object process() {
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		outFile = (String) m.get("outFile");
		
		paragraph = (String) m.get("abstract");
		annotations = tag(paragraph);
		for (int i = 0; i < annotations.size(); i++) { 
			AnnotationI a = annotations.get(i);
			writeObject(a, outFile + "/toks/" + i + "/" + a.hashCode());
		}
		return m;
	}
	
	private List<AnnotationI> tag(String paragraph) { 
		assert paragraph != null;
		
		return tokenizer.annotate(paragraph);
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx != null) throw new NullPointerException("ctx: null");
		
		List<Object> retValues = new ArrayList<>();
		for (int i = 0; i < annotations.size(); i++) { 
			TextAnnotationI a = (TextAnnotationI) annotations.get(i);
			Triple<String, Integer, Integer> tagWord = new SimpleTriple<String, Integer, Integer>(a.text(), a.start(), a.end());
			
			Map<String, Object> ret = new HashMap<>();
			ret.put("pos", tagWord);
			ret.put("outFile", outFile + "/toks/" + i);
			retValues.add(ret);
		}
		return retValues;
	}
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	
	private List<AnnotationI> annotations;
	
	private String outFile;
	
	private String paragraph;
	
}
