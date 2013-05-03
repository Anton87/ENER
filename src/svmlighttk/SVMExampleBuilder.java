package svmlighttk;

import java.util.ArrayList;
import java.util.List;

public class SVMExampleBuilder {
	
	private List<String> trees;
	private List<SVMVector> vectors;
	private boolean positive; 
	
	public SVMExampleBuilder() {
		this.trees = new ArrayList<>();
		this.vectors = new ArrayList<>();
		this.positive = true;
	}
	
	public SVMExampleBuilder addTree(String tree) {
		this.trees.add(tree);
		return this;
	}
	
	public SVMExampleBuilder addVector(SVMVector vector) {
		this.vectors.add(vector);
		return this;
	}
	
	public SVMExampleBuilder positive() {
		this.positive = true;
		return this;
	}
	
	public SVMExampleBuilder negative() {
		this.positive = false;
		return this;
	}
	
	public String build() {
		String example;
		
		if(this.positive) {
			example = "+1";
		} else {
			example = "-1";
		}
		
		for(String tree : this.trees) {
			example += " |BT|";
			if(!tree.equals("")) {
				example += " " + tree;
			}					
		}
		
		example += " |ET|";
		
		for(int i = 0; i < this.vectors.size(); i++) {
			if(i != 0) {
				example += " |BV|";
			}
			String features = this.vectors.get(i).toString();
			if(!features.equals("")) {
				example += " " + features;
			}
		}
		
		if(this.vectors.size() > 0) {
			example += " |EV|";
		}
		
		return example;
	}
}
