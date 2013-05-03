package it.unitn.uvq.antonio.parse.tree;

import it.unitn.uvq.antonio.parse.tree.SimpleTreePrinter;
import it.unitn.uvq.antonio.util.IntRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TreeBuilder implements Cloneable {
	
	TreeBuilder(String text, int nodeNum, IntRange span) {
		this(text, nodeNum, span, null);
	}
	
	TreeBuilder(String text, int nodeNum, IntRange span, TreeBuilder parent) { 
		this(text, nodeNum, span, parent, new ArrayList<TreeBuilder>());
	}
	
	public TreeBuilder(String text, int nodeNum, IntRange span, TreeBuilder parent, List<TreeBuilder> children) {
		if (text == null) throw new NullPointerException("tree: null");
		if (nodeNum < 0) throw new IllegalArgumentException("nodeNum < 0");
		if (span == null) throw new IllegalArgumentException("span: null");
		if (children == null) throw new NullPointerException("children: null");
		this.text = text;
		this.nodeNum = nodeNum;
		this.span = span;
		this.parent = parent;
		this.children = new ArrayList<>(children);
	}
	
	public TreeBuilder getRoot() {
		TreeBuilder tree = this;
		while (tree.getParent() != null) { 
			tree = tree.getParent();
		}
		return tree;
	}
	
	public TreeBuilder getParent() { 
		return parent;
	}
	
	public TreeBuilder setParent(TreeBuilder parent) { 
		this.parent = parent;
		return this;
	}
	
	public int getNodeNum() { return nodeNum; }
	
	public TreeBuilder setNodeNum(int nodeNum) {
		if (nodeNum < 0) throw new IllegalArgumentException("nodeNum < 0");
		this.nodeNum = nodeNum; 
		return this;
	} 
	
	public String getText() { return text; }
	
	public TreeBuilder setText(String text) {
		if (text == null) throw new NullPointerException("text: null");
		this.text = text;
		return this;
	}
	
	public boolean hasChildren() { return !children.isEmpty(); }
	
	public List<TreeBuilder> getChildren() {  return children; }
	
	public TreeBuilder addChild(TreeBuilder tree) {
		return addChild(children.size(), tree);
	}
	
	public TreeBuilder addChild(int i, TreeBuilder tree)  {
		if (tree == null) throw new NullPointerException("tree: null");
		if (i < 0 || i > children.size()) throw new IllegalArgumentException("(i < 0 || i > children.size())");
		unlink(tree, tree.parent); 
		children.add(i, tree);
		tree.setParent(this);
		return this;
	}
	
	private void unlink(TreeBuilder child, TreeBuilder parent)  {
		if (parent != null) {
			parent.removeChild(child);
		}
		child.setParent(null);
	}
	
	public TreeBuilder getChild(int i) {
		if (i < 0 || i >= children.size()) throw new IllegalArgumentException("i < 0 || i >= children.size()");
		return children.get(i);
	}
	
	public boolean isLeaf() {
		return !hasChildren();
	}
	
	private boolean hasChild(TreeBuilder child) {
		assert child != null;
		return children.indexOf(child) != -1;		
	}
	
	private boolean containsAtLeastOneChild(Collection<TreeBuilder> trees) {
		assert children != null;
		List<TreeBuilder> lst = new ArrayList<>(children);
		lst.retainAll(trees);
		return !lst.isEmpty();		
	}
	
	public TreeBuilder removeChild(final TreeBuilder child) {
		if (child == null) throw new NullPointerException("child: null");
		if (hasChild(child)) {
			children.remove(child); 
			unlink(child, child.getParent());
		}
		return this;
	}	
	
	public TreeBuilder removeAll(Collection<TreeBuilder> children) {
		if (children == null) throw new NullPointerException("children: null");
		if (containsAtLeastOneChild(children)) {
			for (TreeBuilder child : children) {
				removeChild(child);
			}
		}
		return this;
	}
	
	public TreeBuilder removeChild(int i) {
		TreeBuilder tree = children.remove(i);
		unlink(tree, tree.parent);
		return this;
	}

	
	public TreeBuilder setChildren(List<TreeBuilder> children) {
		if (children == null) throw new NullPointerException("children: null");
		this.children = new ArrayList<>();
		for (TreeBuilder child : children) { 
			addChild(child);
		}
		return this;
	}
	
	public TreeBuilder addChildren(final List<TreeBuilder> children) { 
		if (children == null) throw new NullPointerException("children: null");
		for (TreeBuilder child : children) {
			addChild(child);
		}
		return this;
	}
	
	public List<TreeBuilder> getNodes() { 
		List<TreeBuilder> nodes = new ArrayList<>();
		List<TreeBuilder> queue = new ArrayList<>();
		queue.add(this);
		
		while (queue.isEmpty()) {
			TreeBuilder tree = queue.remove(0);
			nodes.add(tree);
			queue.addAll(tree.getChildren());
		}
		return nodes;		
	}
	
	public List<TreeBuilder> getLeaves() { 
		List<TreeBuilder> leaves = new ArrayList<>();
		for (TreeBuilder tree : getNodes()) { 
			if (tree.isLeaf()) { 
				leaves.add(tree);
			}
		}
		return leaves;
	}
	
	public IntRange getSpan() { 
		return span;
	}
	
	public TreeBuilder setSpan(IntRange span) { 
		this.span = span;
		return this;
	}
	
	public int start() {
		return span.start();
	}
	
	public int end() {
		return span.end();
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		TreeBuilder copy = new TreeBuilder(text, nodeNum, span);		
		for (TreeBuilder child : children)  {
			copy.addChild((TreeBuilder) child.clone());
		}		
		return copy;
	}
	
	public Tree build() { return new Tree(this); }
	
	/*
	public Tree build() {		
		Tree tree = null;
		try {
			TreeBuilder copy = (TreeBuilder) clone();
			tree = new Tree(copy);
		} catch (CloneNotSupportedException e) { 
			// Clone operation is supported. 
		}
		return tree;		
	}
	*/	
	
	@Override
	public String toString() {
		return SimpleTreePrinter.print(this);				
	}
	
	/*
	private TreeBuilder restoreNodeNumbers() { 
		int i = 1;
		for (TreeBuilder tree : getNodes()) { 
			tree.setNodeNum(i);
		}
		return this;
	}
	*/
	
	private String text = null;
	
	private int nodeNum = 0;
	
	private IntRange span = null;
	
	private TreeBuilder parent = null;
	
	private List<TreeBuilder> children = null;

}
