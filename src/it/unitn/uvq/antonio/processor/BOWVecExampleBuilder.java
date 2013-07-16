package it.unitn.uvq.antonio.processor;

import svmlighttk.SVMExampleBuilder;

public class BOWVecExampleBuilder extends ExampleBuilder {

	@Override
	public String build() {
		
		String paragraphBOW = buildBOW(paragraph);
		
		String notableTypesVec = buildVectorOfNotableTypesIds(notableTypes, notableFor);
		
		String infoString = buildInfoStringFollowingFidsSorting(fids, notableTypes, notableFor);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(paragraphBOW)
			.build();
		
		svmExample += " " + notableTypesVec;
		svmExample += " " + infoString;
		
		return svmExample;
	}

}
