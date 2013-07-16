package it.unitn.uvq.antonio.processor;

import java.util.List;

import svmlighttk.SVMExampleBuilder;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;

public class TreeWordIdExampleBuilder extends WordIdExampleBuilder {

	public TreeWordIdExampleBuilder(String indexFilepath) {
		super(indexFilepath);
	}

	@Override
	public String build() {		
		
		Tree parse = parser.parse(sentence);
		
		TreeBuilder tb = new TreeBuilder(parse);

		TextAnnotationI a = new TextAnnotation("NE", entitySpan);
		
		if (!isValidSentence(parse)) return null;
		
		TreeBuilder annotatedTree = annotator.annotate(a, tb);
		
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
