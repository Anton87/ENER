package it.unitn.uvq.antonio.nlp.parse;

import java.util.List;

import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilderFactory;
import it.unitn.uvq.antonio.nlp.pos.POSTagger;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Triple;

public class VectorParser {
	
	public static VectorParser getInstance() { 
		return INSTANCE;
	}
	
	public Tree parse(String str) {
		if (str == null) throw new NullPointerException("str: null");
		
		List<Quadruple<String, String, Integer, Integer>> tagWords = ptagger.tag(str);
		
		int start = tagWords.get(0).third();
		int end = tagWords.get(tagWords.size() - 1).fourth();
		
		int i = 1;
		TreeBuilder root = TreeBuilderFactory.newInstance("ROOT", 1, new IntRange(start, end));
		TreeBuilder s = TreeBuilderFactory.newInstance("S", 2, new IntRange(start, end));
		root.addChild(s);
		
		i = 3;
		for (Quadruple<String, String, Integer, Integer> tagWord : tagWords) { 
			IntRange span = new IntRange(tagWord.third(), tagWord.fourth());	
			TreeBuilder tagTree = TreeBuilderFactory.newInstance(tagWord.second(), i, span);
			TreeBuilder wordTree = TreeBuilderFactory.newInstance(tagWord.first() , i + tagWords.size(), span);
			s.addChild(tagTree);
			tagTree.addChild(wordTree);			
		}
		return root.build();
	}
	
	private final static VectorParser INSTANCE = new VectorParser();
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	
	private static POSTagger ptagger = POSTagger.getInstance();

}
