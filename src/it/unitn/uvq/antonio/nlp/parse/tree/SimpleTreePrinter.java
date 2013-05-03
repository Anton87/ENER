package it.unitn.uvq.antonio.nlp.parse.tree;

import java.util.List;

public class SimpleTreePrinter implements TreePrinter {

	public static String print(TreeBuilder tree) { 
		if (tree == null) throw new NullPointerException("tree: null");
		return INSTANCE.printTree(tree);
	}
	
	@Override
	public String printTree(Tree tree) { 
		if (tree == null) throw new NullPointerException("tree: null");
		return printTree(tree.builder());
	}
	
	@Override
	public String printTree(TreeBuilder tree) {
		if (tree == null) throw new NullPointerException("tree: null");
		return tree.isLeaf()
				? tree.getText()
				//: tree.getText() + " {" + tree.getNodeNum() + "} ";
				: "(" + tree.getText() + " " + printTrees(tree.getChildren()) + ")";
				//: "(" + tree.getText() + " {" + tree.getNodeNum() + "} " + printTrees(tree.getChildren()) + ")";
	}
	
	private String printTrees(List<TreeBuilder> trees) { 
		assert trees != null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < trees.size(); i++) { 
			TreeBuilder tree = trees.get(i);
			sb.append(printTree(tree) + 
					 (i < trees.size() - 1 ? " " : ""));
		}
		return sb.toString();
	}
	
	private final static SimpleTreePrinter INSTANCE = new SimpleTreePrinter();

}
