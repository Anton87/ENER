package it.unitn.uvq.antonio.parse.tree;

import java.util.ArrayList;
import java.util.List;

import it.unitn.uvq.antonio.util.IntRange;
import edu.stanford.nlp.ling.CoreLabel;

public class TreeTransformer {
	
	static TreeBuilder transform(edu.stanford.nlp.trees.Tree tree) {
		if (tree == null) throw new NullPointerException("tree: null");
		IntRange span = getSpan(tree);
		edu.stanford.nlp.trees.Tree root = tree;
		TreeBuilder newTree = new TreeBuilder(tree.value(), tree.nodeNumber(root), span);
		List<TreeBuilder> children = transform(tree.getChildrenAsList(), newTree, root);
		return newTree.addChildren(children);       
	}
	
	private static List<TreeBuilder> transform(List<edu.stanford.nlp.trees.Tree> trees, TreeBuilder parentTree, edu.stanford.nlp.trees.Tree root) { 
		assert trees != null;
		assert parentTree != null;
		List<TreeBuilder> newChildren = new ArrayList<>();
		for (edu.stanford.nlp.trees.Tree tree : trees) {
			IntRange span = getSpan(tree);
			TreeBuilder newChild = new TreeBuilder(tree.value(), tree.nodeNumber(root), span);
			newChild.addChildren(
					transform(tree.getChildrenAsList(), newChild, root));
			
			newChildren.add(newChild);					
		}
		return newChildren;
	}
	
	static IntRange getSpan(edu.stanford.nlp.trees.Tree tree) {
		CoreLabel label = (CoreLabel) tree.label();
		return new IntRange(label.beginPosition(), label.endPosition());
	}
	
	
}
