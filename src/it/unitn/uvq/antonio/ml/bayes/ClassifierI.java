package it.unitn.uvq.antonio.ml.bayes;

public interface ClassifierI {
	
	/**
	 * Returns the probability that a given text belongs to this class.
	 * 
	 * @param sent A string holding the text to classify
	 * @return The class probability given the text 
	 */
	public double classify(String text, boolean stem);
	

}
