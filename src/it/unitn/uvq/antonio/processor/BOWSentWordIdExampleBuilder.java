package it.unitn.uvq.antonio.processor;

import java.util.List;

import svmlighttk.SVMExampleBuilder;

/**
 * Builds a new model holding:
 *  · the BOW of the paragraph;
 *  · the feature vector with the tf of the word stems appearing in the sentence
 *   
 * @author Antonio Uva 145683
 *
 */
public class BOWSentWordIdExampleBuilder extends BOWWordIdExampleBuilder {
	
	/**
	 * Builds a new sent word id examples builder.
	 * 
	 * @param indexFilepath The filepath of the word2Id dict
	 */
	public BOWSentWordIdExampleBuilder(String indexFilepath) {
		super(indexFilepath);
	}

	@Override
	public String build() {
		
		List<String> paragraphTokens = tokenize(paragraph);
		
		String bow = buildBOW(paragraphTokens);
		
		List<String> sentenceTokens = tokenize(sentence);
		
		String sentWordIdsVec = buildWordIdsVec(sentenceTokens);
		
		String infoString = buildInfoString(notableTypes, notableFor);
		
		String svmExample = new SVMExampleBuilder()
			.addTree(bow)
			.build();
		
		svmExample += " " + sentWordIdsVec + "|EV| " + infoString;
		
		return svmExample;		
		
	}

}
