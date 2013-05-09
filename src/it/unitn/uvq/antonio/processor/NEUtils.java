package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuintuple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import edu.illinois.cs.cogcomp.entityComparison.core.EntityComparison;

class NEUtils {
	
	static NEUtils getInstance() { return INSTANCE; }
	
	ResultSet getRankedEntities (
			List<Quadruple<String, String, Integer, Integer>> entities, List<String> priEntityNames, String priEntityType) { 
		if (entities == null) throw new NullPointerException("entities: null");
		if (priEntityType == null) throw new NullPointerException("priEntityType: null");
		if (priEntityNames == null) throw new NullPointerException("priEntityNames: null");
		
		List<Quintuple<String, String, String, Integer, Integer>> rankedEntities = new ArrayList<>();
		for (Quadruple<String, String, Integer, Integer> entity : entities) {
			String entityType = isPrimaryEntity(entity, priEntityNames, priEntityType) ? "NE" : "AE";
			Quintuple<String, String, String, Integer, Integer> rankedEntity = new SimpleQuintuple<>(entity.first(), entity.second(), entityType, entity.third(), entity.fourth());
			rankedEntities.add(rankedEntity);			
		}
		return new ResultSet(rankedEntities);		
	}
	
	private boolean isPrimaryEntity(Quadruple<String, String, Integer, Integer> entity, List<String> priEntityNames, String priEntityType) {
		if (entity == null) throw new NullPointerException("entity: null");
		if (priEntityNames == null) throw new NullPointerException("priEntityNames: null");
		if (priEntityType == null) throw new NullPointerException("priEntityType: null");
		
		String entity1Name  = getAbbrType(entity.second()) + "#" + entity.first();
		Entity entity1 = new Entity(entity1Name);
		
		boolean isPrimary = false;
		for (Iterator<String> it = priEntityNames.iterator(); !isPrimary && it.hasNext(); ) {
			String priEntityName = it.next();
			
			String entity2Name = getAbbrType(priEntityType) + "#" + priEntityName;
			Entity entity2 = new Entity(entity2Name);
			isPrimary = entity1.isSameAs(entity2);
		}		
		return isPrimary;
	}
	
	private static class Entity {
		
		Entity(String name) { 
			if (name == null) throw new NullPointerException("name: null");
			
			this.name = name;
		}
		
		private boolean isSameAs(Entity other) {
			assert other != null;
			
			entityCmp.compare(name, other.name);			
			// System.out.println("sim(" + name + ", " + other.name + ") = " + entityCmp.getScore() + ".");
			return entityCmp.getScore() >= ENTITY_CMP_THRESHOLD;
		}
		
		private final String name;
		
		private static EntityComparison entityCmp = new EntityComparison(); 
		
	}
	
	static class ResultSet {
		

		/*
		private ResultSet prev = null;
		
		private ResultSet next = null;
		*/
		
		private final List<Quintuple<String, String, String, Integer, Integer>> entities;
		
		/*
		ResultSet(List<Quintuple<String, String, String, Integer, Integer>> entities) {
			this(entities, null);
		}
		*/
		ResultSet(List<Quintuple<String, String, String, Integer, Integer>> entities) { 
			if (entities == null) throw new NullPointerException("entities: null");
			
			this.entities = new ArrayList<>(entities);
		}
		
		/*
		ResultSet(List<Quintuple<String, String, String, Integer, Integer>> entities, ResultSet prev) {
			if (entities == null) throw new NullPointerException("entities: null");
			
			//this.prev = prev;
			this.entities = new ArrayList<>(entities);
		}
		
		
		ResultSet prev(ResultSet prev) { 
			if (prev == null) throw new NullPointerException("prev: null");
			
			this.prev = prev;
			return this;
		}
		
		ResultSet next(ResultSet next) {
			if (next == null) throw new NullPointerException("next: null");
			
			this.next = next;
			return this;
		}
		
		ResultSet prev() { 
			return prev;
		}
		
		ResultSet next() {
			return next;
		}
		*/
		
		List<Quintuple<String, String, String, Integer, Integer>> entities() { 
			return entities;
		}
		
		ResultSet filter(Predicate<Quintuple<String, String, String, Integer, Integer>> cond) {
			List<Quintuple<String, String, String, Integer, Integer>> dup  = new ArrayList<>(entities());
			
			dup = new ArrayList<>(Collections2.filter(dup, cond));
			return new ResultSet(dup);
		}		
		
		/*
		boolean hasPrev() {
			return prev != null;
		}
		
		boolean hasNext() { 
			return next != null;
		}
		*/		
		
	};
	
	private String getAbbrType(String str) { 
		assert str != null;
		
		return str.substring(0, 3).toUpperCase();
	}
	
	static HasType hasType(String entityType) { 
		if (entityType == null) throw new NullPointerException("entityType: null");
		
		return new HasType(entityType);
	}
	
	static Predicate<Quintuple<String, String, String, Integer, Integer>> IsPrimary = new Predicate<Quintuple<String, String, String, Integer, Integer>>() {
		
		@Override
		public boolean apply(Quintuple<String, String, String, Integer, Integer> entity) {
			if (entity == null) throw new NullPointerException("entity: null");
			
			return entity.third().equals("NE");					
		}
	};
	
	static Predicate<Quintuple<String, String, String, Integer, Integer>> IsSecondary = new Predicate<Quintuple<String,String,String,Integer,Integer>>() { 
		
		public boolean apply(Quintuple<String, String, String, Integer, Integer> entity) { 
			if (entity == null) throw new NullPointerException("entity: null");
			
			return entity.third().equals("AE");
		}
	};
	
	static class HasType implements Predicate<Quintuple<String, String, String, Integer, Integer>> {
		
		private HasType(String entityType) {
			if (entityType == null) { throw new NullPointerException("entityType: null"); }
			
			this.entityType = entityType;
		}
		
		public boolean apply(Quintuple<String, String, String, Integer, Integer> entity) {
			if (entity == null) throw new NullPointerException("entity: null");
			
			return entity.second().equals(entityType);
		}
		
		
		private final String entityType;
		
	}
	
	private final static double ENTITY_CMP_THRESHOLD = .5;
	
	private final static NEUtils INSTANCE = new NEUtils();

}
