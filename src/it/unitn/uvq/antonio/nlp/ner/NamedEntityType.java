package it.unitn.uvq.antonio.nlp.ner;

public enum NamedEntityType {
	
	PERSON("PER"),
	LOCATION("LOC"),
	ORGANIZATION("ORG");
	
	private NamedEntityType(String abbr) {
		this.abbr = abbr;
	}
	
	String abbr() { return abbr; }
	
	private final String abbr;

}
