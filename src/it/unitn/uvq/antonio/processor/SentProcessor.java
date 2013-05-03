package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.sent.SentSplitter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SentProcessor extends AbstractProcessor {

	@Override
	public Object process() {		
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		outFile = (String) m.get("outFile");
		paragraph = (String) m.get("abstract");
		
		sentsAnntions = sents_split(paragraph);
		for (int i = 0; i < sentsAnntions.size(); i++) {
			AnnotationI a = sentsAnntions.get(i);
			write(a, outFile + "/sents/" + i + "/" + a.hashCode());
		}
		return m;
	}
	
	private List<AnnotationI> sents_split(String text) {
		assert text != null;
		
		return ssplitter.annotate(text);		
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
		for (int i = 0; i < sentsAnntions.size(); i++) { 
			AnnotationI a = sentsAnntions.get(i);
			String sent = paragraph.substring(a.start(), a.end());
			
			Map<String, Object> ret = new HashMap<>();
			ret.put("sentence", sent);
			ret.put("outFile", outFile + "/sents/" + i);
			retValues.add(ret);
		}		
		return retValues;
	}
	
	private static SentSplitter ssplitter = SentSplitter.getInstance();
		
	private List<AnnotationI> sentsAnntions;
	
	private String paragraph;
	
	private String outFile;

}
