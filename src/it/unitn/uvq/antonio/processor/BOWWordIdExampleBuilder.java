package it.unitn.uvq.antonio.processor;

import java.util.List;

import svmlighttk.SVMExampleBuilder;

public class BOWWordIdExampleBuilder extends WordIdExampleBuilder {
	
	public BOWWordIdExampleBuilder(String indexFilepath) {
		super(indexFilepath);
	}

	@Override
	public String build() {
		
		List<String> tokens = tokenize(paragraph);
		
		String bow = buildBOW(tokens);
		
		String wordIdsVec = buildWordIdsVec(tokens);
		
		String infoString = buildInfoString(notableTypes, notableFor);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(bow)
			.build();
		
		svmExample += " " + wordIdsVec + "|EV| " + infoString;
		
		return svmExample;
		
	}

}
