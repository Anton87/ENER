package it.unitn.uvq.antonio.nlp.parse;

import java.util.List;

import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilderFactory;
import it.unitn.uvq.antonio.nlp.pos.POSTagger;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Quadruple;

public class VectorParser {
	
	public static VectorParser getInstance() { 
		return INSTANCE;
	}
	
	public Tree parse(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<Quadruple<String, String, Integer, Integer>> tagWords = ptagger.tag(str);
		
		Quadruple<String, String, Integer, Integer> first = tagWords.get(0);
		Quadruple<String, String, Integer, Integer> last = tagWords.get(tagWords.size() - 1);
		
		int start = first.third();
		int end = last.fourth();
		int i = 1;
		TreeBuilder root = TreeBuilderFactory.newInstance("ROOT", 1, new IntRange(start, end));
		TreeBuilder s = TreeBuilderFactory.newInstance("S", 2, new IntRange(start, end));
		root.addChild(s);
		
		i = 3;
		for (Quadruple<String, String, Integer, Integer> tw : tagWords) { 
			IntRange span = new IntRange(tw.third(), tw.fourth());			
			TreeBuilder tag = TreeBuilderFactory.newInstance(tw.second(), i, span);
			TreeBuilder word = TreeBuilderFactory.newInstance(tw.first(), i + tagWords.size(), span);
			s.addChild(tag);
			tag.addChild(word);			
		}
		return root.build();
	}
	
	private final static VectorParser INSTANCE = new VectorParser();
	
	private POSTagger ptagger = POSTagger.getInstance();

}
