package it.unitn.uvq.antonio.nlp.parse.tree;

import it.unitn.uvq.antonio.nlp.parse.tree.SimpleTreePrinter;
import it.unitn.uvq.antonio.util.IntRange;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class TreeBuilder implements Cloneable, Externalizable {
	
	public TreeBuilder() { }
	
	public TreeBuilder(Tree tree) {
		TreeBuilder tb = tree.builder();
		init(tb.text, tb.nodeNum, tb.span, tb.parent, tb.children);
	}
	
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
		init(text, nodeNum, span, parent, children);
	}
	
	private void init(String text, int nodeNum, IntRange span, TreeBuilder parent, List<TreeBuilder> children) {
		assert text != null;
		assert span != null;
		assert nodeNum >= 0;
		assert parent != null;
		assert children != null;
		this.text = text;
		this.nodeNum = nodeNum;
		this.span = span;
		this.parent = parent;
		this.children = children;
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
		tree.getRoot().restore();  //
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
			restore(); // 
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
		TreeBuilder child = children.get(i);
		removeChild(child);		
		/*
		TreeBuilder tree = children.remove(i);
		unlink(tree, tree.parent);
		*/
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
		
		while (!queue.isEmpty()) {
			TreeBuilder tree = queue.remove(0);
			nodes.add(tree);
			queue.addAll(tree.getChildren());
		}
		return nodes;		
	}
	
	public List<TreeBuilder> getLeaves() { 
		List<TreeBuilder> leaves = new ArrayList<>();
		List<TreeBuilder> queue = new ArrayList<>();
		queue.add(this);
		
		while (!queue.isEmpty()) {
			TreeBuilder tree = queue.remove(0); 
			if (tree.isLeaf()) {
				leaves.add(tree);
			}
			queue.addAll(0, tree.getChildren());
			
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
	
	private void restore() {
		restoreNodeNums();
		restoreSpans();
	}
	
	private void restoreNodeNums() { 
		int i = 1;
		for (TreeBuilder tree : getNodes()) { 
			tree.setNodeNum(i++);			
		}
	}
	
	private void restoreSpans()  {
		for (TreeBuilder tree : getNodes()) {
			if (!tree.isLeaf()) { 
				List<TreeBuilder> leaves = tree.getLeaves();
				int start = leaves.get(0).start();
				int end = leaves.get(leaves.size() - 1).end();
				tree.setSpan(new IntRange(start, end));
			}
		}
	}
	
	
	@Override
	public String toString() {
		return SimpleTreePrinter.print(this);				
	}
	
	int parentNodeNum = 0;
	
	private String text = null;
	
	private int nodeNum = 0;
	
	private IntRange span = null;
	
	private TreeBuilder parent = null;
	
	private List<TreeBuilder> children = null;
	
	/** 
	 * Saves the tree on a file.
	 * 
	 * @param outFile The path where to save the tree
	 * @return The saved tree
	 * @throws NullPointerException if (outFile == null || outFile == null)
	 */
	public static TreeBuilder saveTree(TreeBuilder tree, String outFile) { 
		if (tree == null) throw new NullPointerException("outFile: null");
		if (outFile == null) throw new NullPointerException("outFile: null");
		try {
			TreeMarshaller.marshal(tree, outFile);
		} catch (TreeMarshalException e) {
			logger.warning("Tree marshalling error.");
		}
		return tree;
	}
	
	public TreeBuilder save(String outFile) { 
		if (outFile == null) throw new NullPointerException("pathname: null");
		return saveTree(this, outFile);
	}
	
	public TreeBuilder load(String inFile) { 
		if (inFile == null) throw new NullPointerException("inFile: null");
		TreeBuilder tree = loadTree(inFile);
		if (tree != null) {
			init(tree.text, tree.nodeNum, tree.span, tree.parent, tree.children);
		}
		return this;
	}
	
	public static TreeBuilder loadTree(String inFile) { 
		if (inFile == null) throw new NullPointerException("inFile: null");
		TreeBuilder tree = null;
		try {
			tree = TreeUnmarshaller.unmarshal(inFile);
		} catch (FileNotFoundException e) { 
			logger.warning("File not found: \"" + inFile + "\".");
		} catch (TreeUnmarshalException e) {
			logger.warning("Tree unmarshalling error.");
		}
		return tree;
	}

	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(text);
		out.writeInt(nodeNum);
		out.writeObject(span);
		out.writeInt(parent == null ? 0 : parent.getNodeNum());		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		text = (String) in.readObject();
		nodeNum = in.readInt();
		span = (IntRange) in.readObject();
		parentNodeNum = in.readInt();
		children = new ArrayList<>();
	}
	
	private static Logger logger = Logger.getLogger(TreeBuilder.class.getName());

}
