package it.unitn.uvq.antonio.ml.bayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Models implements Model {

	SPORTS("sports", "Sports", "nb-models/sports.tsv"),
	EDUCATION("education", "Education", "nb-models/education.tsv"),
	MEDICINE("medicine", "Medicine", "nb-models/medicine.tsv"),
	MILITARY("military", "Military", "nb-models/military.tsv"),
	ARCHITECTURE("architecture", "Architecture", "nb-models/architecture.tsv"),
	FASHION("fashion", "Fashion", "nb-models/fashion.tsv"),
	GOVERNMENT("government", "Government", "nb-models/government.tsv"),
	BOOK("book", "Book", "nb-models/book.tsv"),
	FIM("fim", "Fim", "nb-models/fim.tsv"),
	MUSIC("music", "Music", "nb-models/music.tsv");

	Models(String modelId, String modelName, String modelFile) {
		if (modelId == null) throw new NullPointerException("modelId: null");
		if (modelName == null) throw new NullPointerException("modelName: null");
		if (modelFile == null) throw new NullPointerException("modelFile: null");

		this.modelId = modelId;
		this.modelName = modelName;
		this.modelFile = modelFile;
		this.prob = readProb(modelFile);
		word2ProbMap = readProbs(modelFile);
	}

	@Override
	public String modelName() {
		return modelName;
	}

	@Override
	public double prob() {
		return prob;
	}

	@Override
	public double prob(String word) {
		if (word2ProbMap.containsKey(word)) {
			return word2ProbMap.get(word);
		}
		return .0;
	}

	private double readProb(String modelFile) {
		assert modelFile != null;

		BufferedReader in = null;
		double prob = .0;
		try {
			in = new BufferedReader(
				new FileReader(modelFile));
			String line = in.readLine();
			if (line != null) { prob = Double.parseDouble(line); }
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			}catch (IOException e) { }
		}
		return prob;
}

	private Map<String, Double> readProbs(String modelFile) { 
		assert modelFile != null;

		Map<String, Double> probs = new HashMap<>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(
				new FileReader(modelFile));
			String line = in.readLine();
			if (line != null) {
				while ((line = in.readLine()) != null) {
					String[] values = line.split(SEPARATOR);
					String word = values[0];
					double prob = Double.parseDouble(values[1]);
					probs.put(word, prob);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
		return Collections.unmodifiableMap(probs);
	}

	private final static String SEPARATOR = "\t";

	private final String modelId;

	private final String modelName;

	private final String modelFile;

	private final double prob;

	private final Map<String, Double> word2ProbMap;

}
