package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.pos.POSTagger;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POSTaggerProcessor extends AbstractProcessor{

	@Override
	public Object process() {
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		outFile = (String) m.get("outFile");
		
		sentence = (String) m.get("sentence");
		annotations = tag(sentence);
		for (int i = 0; i < annotations.size(); i++) { 
			AnnotationI a = annotations.get(i);
			writeObject(a, outFile + "/pos/" + i + "/" + a.hashCode());
		}
		return m;		
	}
	
	private List<AnnotationI> tag(String sent) { 
		assert sent != null;
		
		return ptagger.annotate(sent);
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx != null) throw new NullPointerException("ctx: null");
		
		List<Object> retValues = new ArrayList<>();
		for (int i = 0; i < annotations.size(); i++) {
			TextAnnotationI a = (TextAnnotationI) annotations.get(i);
			Triple<String, Integer, Integer> tagWord = new SimpleTriple<>(a.text(), a.start(), a.end());
		
			Map<String, Object> ret = new HashMap<>();
			ret.put("pos", tagWord);
			ret.put("outFile", outFile + "/pos/" + i);
			retValues.add(ret);
		}
		return retValues;
	}
	
	private static POSTagger ptagger = POSTagger.getInstance();	
	
	private List<AnnotationI> annotations;
	
	private String outFile;
	
	private String sentence;

}
