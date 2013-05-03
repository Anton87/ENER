package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeProcessor extends AbstractProcessor {

	@Override
	public Object process() {
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) ctx;
		outFile = (String) m.get("outFile");
		String sent = (String) m.get("sentence");
		
		tree = parse(sent);
		tree.save(outFile + "/tree/" + tree.hashCode());
		
		return m;	
	}
	
	private Tree parse(String sent) {
		assert sent != null;
		
		return parser.parse(sent);
	}

	@Override
	public List<Object> iterate(Object ctx) {
		if (ctx == null) throw new NullPointerException("ctx: null");
		
		Map<String, Object> m = new HashMap<>();
		m.put("tree", tree);
		m.put("outFile", outFile + "/tree");
		return Arrays.asList((Object) m);
	}
	
	private static Parser parser = Parser.getInstance();	

	private Tree tree;
	
	private String outFile;
	
}
