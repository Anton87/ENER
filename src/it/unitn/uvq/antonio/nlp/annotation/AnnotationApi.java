package it.unitn.uvq.antonio.nlp.annotation;

import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;

public interface AnnotationApi {
	
	/**
	 * Returns a boolean indicatings whether the given tree is annotable by
	 *  the specified annotation.
	 *  
	 *  @param a The annotation
	 *  @param tree The tree to annotate
	 *  @return true if the tree is annotable with the specified annotation, false otherwise
	 *  @throws NullPointerException if (data == null) || (tree == null) 
	 */
	boolean isAnnotable(final TextAnnotationI a, final TreeBuilder tree);
	
	/**
	 * Returns the tree annotated with the specified annotation.
	 * 
	 * @param a The annotation
	 * @param tree The tree to annotate
	 * @return The tree annotated with the specified data
	 * @throws NullPointerException if (data == null) || (tree == null)
	 */
	TreeBuilder annotate(final TextAnnotationI a, final TreeBuilder tree);

}
