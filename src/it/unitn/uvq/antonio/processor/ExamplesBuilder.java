package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.util.List;

public abstract class ExamplesBuilder {
	
	public ExamplesBuilder(String namedEntityType, String notableTypeId) {
		if (namedEntityType == null) throw new NullPointerException("namedEntityType: null");
		if (notableTypeId == null) throw new NullPointerException("notableTypeId: null");
		
		this.namedEntityType = namedEntityType;
		this.notableTypeId = notableTypeId;
	}
	
	void process(String mid, EntityI entity, String alias, List<String> acronyms, String paragraph, Triple<String, Integer, Integer> sent, List<Quadruple<String, String, Integer, Integer>> nes) {
		process(namedEntityType, notableTypeId, mid, entity, alias, acronyms, paragraph, sent, nes);
	}
	
	public abstract void process(
			// The type of the named entities to tag in the sentence
			String namedEntityType, 
			// The notable type id of the main entities to tag
			String notableTypeId, 	
			// The topic (aka entity) mid
			String mid, 	
			// The entity class instance
			EntityI entity, 
			/* The alternative name used in the entity' wiki-page to refer to the entity 
              (e.g. Green in place of Al Green, Lennon in place of John Lennon) */
			String alias,   
			// List of entity's acronyms (useful for Organizations)
			List<String> acronyms,
			// The entity' wiki-page
			String paragraph, 
			// The current sentence
			Triple<String, Integer, Integer> sent,
			/* The list of named entities found in the sentence
			 * (e.g. PERSON, LOCATION, ORGANIZATION) namesv*/			
			List<Quadruple<String, String, Integer, Integer>> nes
	);
	
	final String namedEntityType;
	
	final String notableTypeId;

}
