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
	
	void process(
			String mid,
			EntityI entity,
			String paragraph,
			List<Triple<String, Integer, Integer>> toks,
			Triple<String, Integer, Integer> sent,
			List<Quadruple<String, String, Integer, Integer>> poss,
			List<Quadruple<String, String, Integer, Integer>> nes,
			Tree tree,
			Tree vec
	) {
		process(namedEntityType, mid, entity, paragraph, toks, sent, poss, nes, tree, vec);
	}
	
	
	public abstract void process(
			String namedEntityType,
			String mid,
			EntityI entity, 
			String paragraph,
			List<Triple<String, Integer, Integer>> toks,
			Triple<String, Integer, Integer> sent,
			List<Quadruple<String, String, Integer, Integer>> tagWords,
			List<Quadruple<String, String, Integer, Integer>> nes,
			Tree tree,
			Tree vec
	);
	
	private final String namedEntityType;
	
	private final String notableTypeId;

}
