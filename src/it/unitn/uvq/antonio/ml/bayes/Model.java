package it.unitn.uvq.antonio.ml.bayes;

public interface Model {
	
	double prob();
	
	double prob(String word);
	
	String modelName();

}
