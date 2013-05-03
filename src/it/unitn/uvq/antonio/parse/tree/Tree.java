package it.unitn.uvq.antonio.parse.tree;

import java.util.ArrayList;
import java.util.List;

public final class Tree {
	
	Tree(TreeBuilder tb) { 
		this.tb = tb;
	}
	
	public Tree getRoot() { 
		return new Tree(tb.getRoot());
	}
	
	public Tree getParent() { 
		return new Tree(tb.getParent());
	}
	
	public int getNodeNum() { return tb.getNodeNum(); }
	
	public String getText() { return tb.getText(); }
	
	public boolean isLeaf() { return tb.isLeaf(); }
	
	public boolean hasChildren() { return tb.hasChildren(); }
	
	public List<Tree> getChildren() {
		List<Tree> children = new ArrayList<>();
		for (TreeBuilder child : tb.getChildren()) { 
			children.add(new Tree(child));
		}
		return children;
	}
	
	public Tree getChild(int i) { 
		return new Tree(tb.getChild(i));
	}
	
	public List<Tree> getNodes() {
		return toTrees(tb.getNodes());
	}
	
	public List<Tree> getLeaves() { 
		return toTrees(tb.getLeaves());
	}	
	
	private List<Tree> toTrees(List<TreeBuilder> tbs) { 
		List<Tree> trees = new ArrayList<>();
		for (TreeBuilder tb : tbs) { 
			Tree tree = new Tree(tb);
			trees.add(tree);
		}
		return trees;
	}
	
	@Override
	public String toString() {
		return SimpleTreePrinter.print(tb);
	}
	
	private TreeBuilder tb;
	
}
