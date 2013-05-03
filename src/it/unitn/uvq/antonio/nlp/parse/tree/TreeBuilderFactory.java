package it.unitn.uvq.antonio.nlp.parse.tree;

import it.unitn.uvq.antonio.util.IntRange;

public class TreeBuilderFactory {
	
	public static TreeBuilder newInstance(String text, int nodeNum, IntRange span) {
		return newInstance(text, nodeNum, span, null);
	}
	
	
	public static TreeBuilder newInstance(String text, int nodeNum, IntRange span, TreeBuilder parent) {
		if (text == null) throw new NullPointerException("text: null");
		if (nodeNum < 0) throw new IllegalArgumentException("nodeNum < 0");
		if (span == null) throw new NullPointerException("span: null");
		return new TreeBuilder(text, nodeNum, span, parent);		
	}

}
