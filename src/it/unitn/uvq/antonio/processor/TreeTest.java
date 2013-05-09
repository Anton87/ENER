package it.unitn.uvq.antonio.processor;

import java.util.Arrays;
import java.util.List;

import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.util.tuple.Quadruple;

public class TreeTest {
	
	public static void main(String args[]) { 
		String str = "He was born in Dean's Yard, Westminster, to Anthony Samwell, son of Sir William Samwell, Auditor of the Exchequer to Queen Elizabeth I. He was one of the gentleman architects who helped define the architectural style that was fashionable after the Restoration.";

		AnnotationApi annotator = new BasicAnnotationApi();
		
		NER ner = NER.getInstance();
		for (Quadruple<String, String, Integer, Integer> ne : ner.classify(str)) {
			System.out.println(ne);
		}
		
		TextAnnotationI[] annotations = { 
				new TextAnnotation("NE", 15, 19),
				new TextAnnotation("NE", 72, 87),
				new TextAnnotation("NE", 44, 59),
				new TextAnnotation("NE", 123, 138)
		};
		
		List<TextAnnotationI> annotationsAsList = Arrays.asList(annotations);
		
		System.out.println();
		
		Tree tree = Parser.getInstance().parse(str);
		System.out.println(tree);
		System.out.println();
		
		TreeBuilder tb = new TreeBuilder(tree);
		
		for (TextAnnotationI a : annotationsAsList) { 
			System.out.println("isAnnotable: " + a + "? " + annotator.isAnnotable(a, tb) + ".");
			annotator.annotate(a, tb);			
		}
		
		/*
		for (Tree leaf : tree.getLeaves()) { 
			System.out.println(leaf + ", " + leaf.getSpan());
		}
		*/
		System.out.println(tb.toString());
	}

}
