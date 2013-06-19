package it.unitn.uvq.antonio.processor;

import svmlighttk.SVMExampleBuilder;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;

/**
 * Build SVM examples containing the paragraph BOW and 
 *  the sentence parse tree with the main entity annotated. 
 * 
 * @author Antonio Uva 145683
 *
 */
public class TreeExampleBuilder extends ExampleBuilder {

	@Override
	public String build() {		
		
		Tree parse = parser.parse(sentence);
		
		TreeBuilder tb = new TreeBuilder(parse);

		TextAnnotationI a = new TextAnnotation("NE", entitySpan);
		
		if (!isValidSentence(parse) || !annotator.isAnnotable(a, tb)) return null;
		
		String paragraphBOW = buildBOW(paragraph);
		
		TreeBuilder annotatedTree = annotator.annotate(a, tb);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(paragraphBOW)
			.addTree(annotatedTree.toString())
			.build();
		
		return svmExample;				
	}
	
	/* Check that the sentence is valid, by looking at its parse tree. */
	private boolean isValidSentence(Tree parse) {
		assert parse != null;
		
		for (Tree node : parse.getNodes()) {
			if (!node.isLeaf() && node.getText().equals("X")) {
				return false;
			}
		}
		return true;		
	}
	
	private static Parser parser = Parser.getInstance();
	
	private static AnnotationApi annotator = new BasicAnnotationApi();
	
}
