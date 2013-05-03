package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.nlp.parse.VectorParser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VecProcessor extends AbstractProcessor {

	@Override
	public Object process() {
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		outFile = (String) m.get("outFile");		
		String sent = (String) m.get("sentence");
		
		Tree vec = parse(sent);
		vec.save(outFile + "/vec/" + vec.hashCode());
		
		return vec;
	}
	
	private Tree parse(String sent) { 
		assert sent != null;
		
		return vecParser.parse(sent);
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		Map<String, Object> m = new HashMap<>();
		m.put("vec", vec);
		m.put("outFile", outFile + "/vec");
		return Arrays.asList((Object) m);
	}
	
	private static VectorParser vecParser = VectorParser.getInstance();
	
	private Tree vec;
	
	private String outFile;

}
