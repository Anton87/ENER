package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuadruple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NERProcessor extends AbstractProcessor {

	@Override
	public Object process() {
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		outFile = (String) m.get("outFile");
		
		sentence = (String) m.get("sentence");
		
		annotations = classify(sentence);
		for (int i = 0; i < annotations.size(); i++) { 
			AnnotationI a = annotations.get(i);
			write(a, outFile + "/nes/" + i + "/" + a.hashCode());
		}
		return m;	
	}
	
	private List<AnnotationI> classify(String sent) { 
		assert sent != null;
		
		return ner.annotate(sent);
	}
	
	private void write(Object o, String outFile) { 
		assert o != null;
		assert outFile != null;
		
		getParentFile(outFile).mkdirs();
		FileUtils.writeObject(o, outFile);
	}
	
	private File getParentFile(String pathname) {
		assert pathname != null;
		
		return new File(pathname).getParentFile();
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		List<Object> retValues = new ArrayList<>();
		for (int i = 0; i < annotations.size(); i++)  {
			TextAnnotationI a = (TextAnnotationI) annotations.get(i);
			String name = sentence.substring(a.start(), a.end());
			Quadruple<String, String, Integer, Integer> ne = new SimpleQuadruple<>(name, a.text(), a.start(), a.end()); 
			
			Map<String, Object> ret = new HashMap<>();
			ret.put("ne", ne);
			ret.put("outFile", outFile + "/nes/" + i);
			retValues.add(ret);
		}
		return retValues;		
	}
	
	private static NER ner = NER.getInstance();
				
	private List<AnnotationI> annotations;
	
	private String outFile;
	
	private String sentence;

}
