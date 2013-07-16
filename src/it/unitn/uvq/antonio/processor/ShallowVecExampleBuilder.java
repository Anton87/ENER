package it.unitn.uvq.antonio.processor;

import svmlighttk.SVMExampleBuilder;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.parse.VectorParser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;


public class ShallowVecExampleBuilder extends ExampleBuilder {
		
	@Override
	public String build() {
		
		String paragraphBOW = buildBOW(paragraph);
		
		Tree shallowTree = shallowParser.parse(sentence);
		
		TreeBuilder sb = new TreeBuilder(shallowTree);
		
		TextAnnotationI a = new TextAnnotation("NE", entitySpan);
		
		TreeBuilder annotatedTree = annotator.annotate(a, sb);
		
		String vector = buildVectorOfNotableTypesIds(notableTypes, notableFor);
		
		String infoString = buildInfoStringFollowingFidsSorting(fids, notableTypes, notableFor);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(paragraphBOW)
			.addTree(annotatedTree.toString())
			.build();
		
		svmExample += " " + vector + " " + infoString;
		
		return svmExample;		
	}
	
	
	
	private static VectorParser shallowParser = VectorParser.getInstance();
	
	private static AnnotationApi annotator = new BasicAnnotationApi();

}
