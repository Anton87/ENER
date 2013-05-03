package it.unitn.uvq.antonio.nlp.annotation;

import java.util.ArrayList;
import java.util.List;

import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilderFactory;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.util.IntRange;

public class BasicAnnotationApi implements AnnotationApi {

	@Override
	public boolean isAnnotable(TextAnnotationI a, TreeBuilder tree) {
		if (a == null) throw new NullPointerException("a: null");
		if (tree == null) throw new NullPointerException("tree: null");
		
		TreeBuilder subTree = findBestMatchingSubTree(a, tree);
		if (subTree == null) { return false; }
		return subTreeMatch(a, subTree) ||
			   subTreeChildrenMatchAnnotation(a, subTree);
	}

	@Override
	public TreeBuilder annotate(TextAnnotationI a, TreeBuilder tree) {
		if (a == null) throw new NullPointerException("a: null");
		if (tree == null) throw new NullPointerException("tree: null");
		
		if (isAnnotable(a, tree)) { 
			TreeBuilder subTree = findBestMatchingSubTree(a, tree);
			annotateSubTree(a, subTree);
		}
		return tree;
	}
	
	private TreeBuilder findBestMatchingSubTree(TextAnnotationI a, TreeBuilder tree) {
		assert a != null;
		assert tree != null;
		
		TreeBuilder bestMatch = null;
		for (TreeBuilder subTree : tree.getNodes()) { 
			if (!subTree.isLeaf()) { 
				if (subTree.getSpan().contains(a.span())  &&
				   (bestMatch == null || subTree.getSpan().compareTo(bestMatch.getSpan()) <= 0)) {
				  	bestMatch = subTree;					
				}
			}
		}
		return bestMatch;
	}
	
	private TreeBuilder annotateSubTree(final TextAnnotationI a, final TreeBuilder subTree) { 
		assert a != null;
		assert subTree != null;
		
		if (subTreeMatch(a, subTree)) {
			tagSubTree(a, subTree);
		} else if (subTreeChildrenMatchAnnotation(a, subTree)) {
			tagSubTreePortion(a, subTree);
		}
		return subTree;
	}
	
	private TreeBuilder tagSubTree(TextAnnotationI a, TreeBuilder subTree) {
		assert a != null;
		assert subTree != null;
		
		subTree.setText(a.text());
		return subTree;
	}
	
	private TreeBuilder tagSubTreePortion(TextAnnotationI a, TreeBuilder subTree) { 
		assert a != null;
		assert subTree != null;
		
		List<TreeBuilder> subTreeChildren = getOverlappingChildren(a, subTree);
		int pos = subTree.getChildren().indexOf(subTreeChildren.get(0));
		subTree.removeAll(subTreeChildren);
		TreeBuilder newSubTree = buildNewTree(a, subTreeChildren);
		subTree.addChild(pos, newSubTree);
		return subTree;
	}
	
	private TreeBuilder buildNewTree(TextAnnotationI a, List<TreeBuilder> children) { 
		assert a != null;
		assert children != null;
		
		IntRange span = getSpan(children);
		TreeBuilder newSubTree = TreeBuilderFactory.newInstance(a.text(), 0, span);
		newSubTree.addChildren(children);
		return newSubTree;
	}
	
	private boolean subTreeMatch(TextAnnotationI a, TreeBuilder subTree) {
		assert a != null;
		assert subTree != null;
		
		//System.out.println("st: " + subTree.getSpan() + ", a: " + a.span());
		return subTree.getSpan().equals(a.span());
	}
	
	private boolean subTreeChildrenMatchAnnotation(TextAnnotationI a, TreeBuilder subTree) {
		assert a != null;
		assert subTree != null;
		
		List<TreeBuilder> subTreeChildren = getOverlappingChildren(a, subTree);
		// System.out.print("subTreeChildren: ");
		// System.out.println(subTreeChildren);
		IntRange span = getSpan(subTreeChildren);
		return span.equals(a.span());
	}
	
	private List<TreeBuilder> getOverlappingChildren(TextAnnotationI a, TreeBuilder tree) {
		assert a != null;
		assert tree != null;
		
		List<TreeBuilder> subTrees = new ArrayList<>();
		for (TreeBuilder child : tree.getChildren()) { 
			// System.out.print("Added ");
			if (a.span().contains(child.getSpan())) {
				//System.out.print(child.getSpan() + ", ");
				subTrees.add(child);
			}
			//System.out.println();
		}
		return subTrees;
	}
	
	private IntRange getSpan(List<TreeBuilder> trees) {
		assert trees != null;
		
		int start = trees.get(0).start();
		int end = trees.get(trees.size() - 1).end();
		return new IntRange(start, end);
	}
 
}
