package it.unitn.uvq.antonio.processor;

import java.util.List;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.parse.VectorParser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import svmlighttk.SVMExampleBuilder;

public class ShallowWordIdExampleBuilder extends WordIdExampleBuilder {
	
	public ShallowWordIdExampleBuilder(String indexFilepath) {
		super(indexFilepath);
	}

	@Override
	public String build() {
		
		Tree shallowTree = shallowParser.parse(sentence);
		
		TreeBuilder sb = new TreeBuilder(shallowTree);
		
		TextAnnotationI a = new TextAnnotation("NE", entitySpan);
				
		/*
		if (!annotator.isAnnotable(a, sb)) {
			return null;
		}
		*/
		
		TreeBuilder annotatedTree = annotator.annotate(a, sb);
		
		List<String> tokens = tokenize(paragraph);
	
		String paragraphBOW = buildBOW(tokens);
		
		String wordIdsVec = buildWordIdsVec(tokens);
		
		String infoString = buildInfoString(notableTypes, notableFor);
				
		String svmExample = new SVMExampleBuilder()
			.addTree(paragraphBOW)
			.addTree(annotatedTree.toString())
			.build();
		
		svmExample += " " + wordIdsVec + "|EV| " + infoString;
		
		return svmExample;
	}
	
	private static VectorParser shallowParser = VectorParser.getInstance();
	
	private static AnnotationApi annotator = new BasicAnnotationApi();

}
