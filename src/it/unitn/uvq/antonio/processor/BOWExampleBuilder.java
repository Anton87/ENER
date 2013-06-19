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
		
		String sentenceBOW = buildBOW(sentence);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(sentenceBOW)
			.build();
		
		return svmExample;
	}
	

}
