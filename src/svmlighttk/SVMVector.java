package svmlighttk;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

public class SVMVector {

	private List<String> features;

	public SVMVector() {
		this.features = new ArrayList<>();
	}

	public SVMVector addFeature(String feature) {
		this.features.add(feature);
		return this;
	}
	
	public SVMVector addFeature(double feature) {
		this.features.add(String.valueOf(feature));
		return this;
	}

	public SVMVector addFeatures(List<String> features) {
		this.features.addAll(features);
		return this;
	}
	
	public List<String> getFeatures() {
		return this.features;
	}

	@Override
	public String toString() {
		List<String> feats = new ArrayList<>();
		for (int i = 0; i < this.features.size(); i++) {
			feats.add((i + 1) + ":" + this.features.get(i));
		}
		return Joiner.on(" ").join(feats);
	}
}
