package it.unitn.uvq.antonio.nlp.parse.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractTree {
	
	AbstractTree(String text, int nodeNum) {
		this(text, nodeNum, new ArrayList<AbstractTree>());
	}
	
	AbstractTree(String text, int nodeNum, List<AbstractTree> children) { 
		this(text, nodeNum, null, children);
	}
	
	public AbstractTree(String text, int nodeNum, AbstractTree parent, List<AbstractTree> children) {
		if (text == null) throw new NullPointerException("tree: null");
		if (nodeNum < 0) throw new IllegalArgumentException("nodeNum < 0");
		if (children == null) throw new NullPointerException("children: null");
		this.text = text;
		this.nodeNum = nodeNum;
		this.parent = parent;
		this.children = new ArrayList<>(children);
	}
	
	public AbstractTree getRoot() {
		AbstractTree tree = this;
		while (tree.getParent() != null) { 
			tree = tree.getParent();
		}
		return tree;
	}
	
	public AbstractTree getParent() { 
		return parent;
	}
	
	public AbstractTree setParent(AbstractTree parent) { 
		this.parent = parent;
		return this;
	}
	
	public int getNodeNum() { return nodeNum; }
	
	public AbstractTree setNodeNum(int nodeNum) {
		if (nodeNum < 0) throw new IllegalArgumentException("nodeNum < 0");
		this.nodeNum = nodeNum; 
		return this;
	} 
	
	public String getText() { return text; }
	
	public AbstractTree setText(String text) {
		if (text == null) throw new NullPointerException("text: null");
		this.text = text;
		return this;
	}
	
	public boolean hasChildren() { return !children.isEmpty(); }
	
	public List<AbstractTree> getChildren() {  return children; }
	
	public AbstractTree addChild(AbstractTree tree) {
		return addChild(children.size(), tree);
	}
	
	public AbstractTree addChild(int i, AbstractTree tree)  {
		if (tree == null) throw new NullPointerException("tree: null");
		if (i < 0 || i > children.size()) throw new IllegalArgumentException("(i < 0 || i > children.size())");
		children.add(i, tree);
		tree.setParent(this);
		return this;
	}
	
	private boolean hasChild(AbstractTree child) {
		assert child != null;
		return children.indexOf(child) != -1;		
	}
	
	private boolean containsAtLeastOneChild(Collection<AbstractTree> trees) {
		assert children != null;
		List<AbstractTree> lst = new ArrayList<>(children);
		lst.retainAll(trees);
		return !lst.isEmpty();		
	}
	
	public AbstractTree removeChild(final AbstractTree child) {
		if (child == null) throw new NullPointerException("child: null");
		if (hasChild(child)) {
			children.remove(child); 
			child.setParent(null); 
		}
		return this;
	}	
	
	public AbstractTree removeAll(Collection<AbstractTree> children) {
		if (children == null) throw new NullPointerException("children: null");
		if (containsAtLeastOneChild(children)) {
			for (AbstractTree child : children) {
				removeChild(child);
			}
		}
		return this;
	}
	
	public AbstractTree removeChild(int i) {
		return children.remove(i);
	}

	
	public AbstractTree setChildren(List<AbstractTree> children) {
		if (children == null) throw new NullPointerException("children: null");
		this.children = new ArrayList<>();
		for (AbstractTree child : children) { 
			addChild(child);
		}
		return this;
	}
	
	public AbstractTree addChildren(final List<AbstractTree> children) { 
		if (children == null) throw new NullPointerException("children: null");
		for (AbstractTree child : children) {
			addChild(child);
		}
		return this;
	}
	
	/*
	public Tree build() {
		Tree tree = new Tree(
	}
	
	/*
	private Tree build(AbstractTree tb, Tree parent) {
		List<AbstractTree> children = tb.getChildren();
		Tree tree = new Tree(tb.text, tb.nodeNum, parent, build(children, tree));		
	}
	
	private List<Tree> build(List<AbstractTree> children, Tree parent) { 
		
	}
	*/
	
	
	
	
	private String text = null;
	
	private int nodeNum = 0;
	
	private AbstractTree parent = null;
	
	private List<AbstractTree> children = null;

}
