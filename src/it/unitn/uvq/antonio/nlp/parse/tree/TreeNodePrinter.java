package it.unitn.uvq.antonio.nlp.parse.tree;

import java.util.List;

public class TreeNodePrinter implements TreePrinter {
	
	public static String print(TreeBuilder tree) {
		return INSTANCE.printTree(tree);
	}
	
	public static String print(Tree tree) { 
		return print(tree.tb);
	}
	
	@Override
	public String printTree(Tree tree) {
		return printTree(tree.tb);
	}
	
	public String printTree(TreeBuilder tree) {
		if (tree == null) throw new NullPointerException("tree: null");
		return tree.isLeaf()
				? tree.getText() + " {" + tree.getNodeNum() + "}" 
				: "(" + tree.getText() + " {" + tree.getNodeNum() + "} " + printTrees(tree.getChildren()) + ")";
	}
	
	private String printTrees(List<TreeBuilder> trees) { 
		assert trees != null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < trees.size(); i++) { 
			TreeBuilder tree = trees.get(i);
			sb.append(printTree(tree));
			if (i < trees.size() - 1) sb.append(" ");
		}
		return sb.toString();
	}
	
	
	private final static TreeNodePrinter INSTANCE = new TreeNodePrinter();

}
