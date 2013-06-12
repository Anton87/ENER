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
	
	void process(String mid, EntityI entity, String paragraph, Triple<String, Integer, Integer> sent, List<Quadruple<String, String, Integer, Integer>> nes) {
		process(namedEntityType, notableTypeId, mid, entity, paragraph, sent, nes);
	}
	
	public abstract void process(
			String namedEntityType,
			String notableTypeId,
			String mid,
			EntityI entity, 
			String paragraph,
			Triple<String, Integer, Integer> sent,
			List<Quadruple<String, String, Integer, Integer>> nes
	);
	
	final String namedEntityType;
	
	final String notableTypeId;

}
