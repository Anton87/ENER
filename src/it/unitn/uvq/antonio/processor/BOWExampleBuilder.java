package it.unitn.uvq.antonio.processor;

import svmlighttk.SVMExampleBuilder;

/**
 * Build SVM examples containing the sentence BOW.
 * 
 * @author antonio Uva 145683
 *
 */
public class BOWExampleBuilder extends ExampleBuilder {
	
	@Override
	public String build() {
		
		//String sentenceBOW = buildBOW(sentence);
		String bow = buildBOW(paragraph);
		
		String infoString = buildInfoString(notableTypes, notableFor);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(bow)
			.build();
		
		svmExample += " " + infoString;
		
		return svmExample;
	}
	

}
