package it.unitn.uvq.antonio.processor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.omg.PortableInterceptor.LOCATION_FORWARD;

import edu.illinois.cs.cogcomp.entityComparison.core.EntityComparison;

import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.NotableType;
import it.unitn.uvq.antonio.freebase.repository.Freebase;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.NeAnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.ner.NamedEntityType;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.util.tuple.Pair;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
import it.unitn.uvq.antonio.util.tuple.SimplePair;
import it.unitn.uvq.antonio.util.tuple.SimpleQuadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleQuintuple;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

public class Loader {
	
	private static Freebase freebase = new Freebase();
	
	private final static NamedEntityType neType = NamedEntityType.PERSON;
	
	public static void main(String[] args) { 
		
		String inFile = "/home/antonio/Scrivania";
		
		NotableType type = NotableType.EN_ECONOMIST;
		
		String typeID = type.getId();
		String thisPath = inFile + typeID + "/m";
		
		for (File file : listFiles(thisPath, DIR_FILTER)) {
			String mid = "/m/" + file.getName();
			
			// Freebase entity
			EntityI entity = freebase.byMid(mid);
			
			Entity e = new Entity(entity, neType.toString());
			
			 
			
			thisPath = inFile + typeID + mid + "/abstract";
			//System.out.println("(II): asbtract's path: " + thisPath);
			for (File file2 : listFiles(thisPath, FILE_FILTER)) { 
				String paragraph = readText(file2.getPath());
				
				thisPath = inFile + typeID + mid + "/abstract/sents";
				
				/* List sentences. */
				for (File file3 : listFiles(thisPath, DIR_FILTER)) { 
					thisPath = inFile + typeID + mid + "/abstract/sents/" + file3.getName();
					Triple<String, Integer, Integer> sent = null;
					
					
					
					for (File file4 : listFiles(file3.getPath(), FILE_FILTER)) { 
						//System.out.println("(II): sent's path:" + file4.getPath());
						AnnotationI a = (AnnotationI) readObject(file4.getPath());
						sent = 
								new SimpleTriple<>(paragraph.substring(a.start(), a.end()), a.start(), a.end());
					}
					System.out.println("(II): mid:  " + mid);
					System.out.println("(II): Topic:" + entity);
					System.out.println("(II): (p):  " + paragraph);
						
					System.out.println("(II): sent: " + sent);
					String sentText = sent.first();
						
					// List NEs						
					List<Quadruple<String, NamedEntityType, Integer, Integer>> nes = new ArrayList<>();
					thisPath = inFile + typeID + mid + "/abstract/sents/" + file3.getName() + "/nes";
						
					for (File file5 : listFiles(thisPath, DIR_FILTER)) {
						Quadruple<String, NamedEntityType, Integer, Integer> ne = null;
						for (File file6 : listFiles(file5.getPath(), FILE_FILTER)) {
							NeAnnotationI ta = (NeAnnotationI) readObject(file6.getPath());
							String entityName = sentText.substring(ta.start(), ta.end());
							ne =new SimpleQuadruple<>(entityName, ta.type(), ta.start(), ta.end());
							
						}
						nes.add(ne);
					}
					System.out.println("(II): NEs:  "  + nes);
					
					// List P-o-S Tags
					List<Triple<String, Integer, Integer>> posTags = new ArrayList<>();
					List<Triple<String, Integer, Integer>> words = new ArrayList<>();
					thisPath = inFile + typeID + mid + "/abstract/sents/" + file3.getName() + "/pos";
					//System.out.println("(II): path: \"" + thisPath + "\"");
						
						
					for (File file5 : listFiles(thisPath, DIR_FILTER)) {
						Triple<String, Integer, Integer> word = null;
						Triple<String, Integer, Integer> pos = null;
						for (File file6 : listFiles(file5.getPath(), FILE_FILTER)) {
							TextAnnotationI ta = (TextAnnotationI) readObject(file6.getPath());
							word = new SimpleTriple<>(sentText.substring(ta.start(), ta.end()), ta.start(), ta.end());
							pos = new SimpleTriple<>(ta.text(), ta.start(), ta.end());
						}
						posTags.add(pos);
						words.add(word);	
					}
					System.out.println("(II): words:" + words);
					System.out.println("(II): pos:  " + posTags);
					
					/* Load tree. */
					Tree tree = null;
					thisPath = inFile + typeID + mid + "/abstract/sents/" + file3.getName() + "/tree";
					for (File file5 : listFiles(thisPath, DIR_FILTER)) {
						//System.out.println("(II): path: \"" + file5.getPath() + "\".");
						tree = Tree.loadTree(file5.getPath());
					}

					System.out.println("(II): tree: " + tree);
						
					/* Load vector. */
					Tree vec = null;
					thisPath = inFile + typeID + mid + "/abstract/sents/" + file3.getName() + "/vec";
					for (File file5 : listFiles(thisPath, DIR_FILTER)) { 
						//System.out.println("(II): path: \"" + file5.getPath() + "\".");
						vec = Tree.loadTree(file5.getPath());
					}			

					System.out.println("(II): vec:  " + vec);
						
						
					Pair<List<Quintuple<String, String, NamedEntityType, Integer, Integer>>, List<Quintuple<String, String, NamedEntityType, Integer, Integer>>> priAndAddEntities = getPrimaryAndSecondaryEntities(e, nes);
					List<Quintuple<String, String, NamedEntityType, Integer, Integer>> priEntities = priAndAddEntities.first();
					List<Quintuple<String, String, NamedEntityType, Integer, Integer>> addEntities = priAndAddEntities.second();
					
					List<Quintuple<String, String, NamedEntityType, Integer, Integer>> addEntitiesWithPriType = filterByType(addEntities, neType);
						
					System.out.println("(II): PEs:  " + priEntities);
					System.out.println("(II): AEs:  " + addEntities);
					
					System.out.println("(II): AEs[type=\"" + neType.name() + "\"]: "  + addEntitiesWithPriType);
					
					List<TextAnnotationI> priAnnotations = toTextAnnotations(priEntities);
					List<TextAnnotationI> addAnnotations = toTextAnnotations(addEntitiesWithPriType);
					
					List<TextAnnotationI> annotations = new ArrayList<>();
					annotations.addAll(priAnnotations);
					annotations.addAll(addAnnotations);
					
					
					// Annotate the tree with primary and secondary entities.
					if (!priAnnotations.isEmpty() && isAnnotatible(tree, priAnnotations)) { 
						TreeBuilder tb = new TreeBuilder(tree);
						annotate(tb, annotations);
						System.out.println("(II): aTree:" + tb);
					}
					
					// Annotate the vector with primary and secondary entities.
					if (!priAnnotations.isEmpty()) { 
						TreeBuilder vb = new TreeBuilder(vec);
						annotate(vb, annotations);
						System.out.println("(II): aVec: " + vb);
					}
					
					System.out.println();					
				}				
			}
		}		
	}
	
	private static TreeBuilder annotate(TreeBuilder tree, List<TextAnnotationI> annotations) { 
		for (TextAnnotationI a : annotations) {
			tree = ANNOTATOR.annotate(a, tree);
		}
		return tree;
	}
	
	private static boolean isAnnotatible(Tree tree, List<TextAnnotationI> annotations) {
		boolean annotable = false;
		
		for (Iterator<TextAnnotationI> it = annotations.iterator(); !annotable && it.hasNext(); ) { 
			TextAnnotationI a = it.next();
			annotable = ANNOTATOR.isAnnotable(a, new TreeBuilder(tree));
		}
		return annotable;
		
		
	}
	
	private static List<TextAnnotationI> toTextAnnotations(List<Quintuple<String, String, NamedEntityType, Integer, Integer>> nes) {
		List<TextAnnotationI> annotations = new ArrayList<>();
		for (Quintuple<String, String, NamedEntityType, Integer, Integer> ne : nes) { 
			TextAnnotationI a = toTextAnnotation(ne);
			annotations.add(a);
		}
		return annotations;
	}
	
	private static TextAnnotationI toTextAnnotation(Quintuple<String, String, NamedEntityType, Integer, Integer> ne) {
		TextAnnotationI a = new TextAnnotation(ne.second(), ne.fourth(), ne.fifth());
		return a;		
	}
	
	private static List<Quintuple<String, String, NamedEntityType, Integer, Integer>> filterByType(List<Quintuple<String, String, NamedEntityType, Integer, Integer>> entities, NamedEntityType neType) {
		List<Quintuple<String, String, NamedEntityType, Integer, Integer>> ret = new ArrayList<>();
	
		for (Quintuple<String, String, NamedEntityType, Integer, Integer> entity : entities) {
			if (entity.third().equals(neType)) { ret.add(entity); }
		}
		return ret;
	}
	
	private static Pair<List<Quintuple<String, String, NamedEntityType, Integer, Integer>>, List<Quintuple<String, String, NamedEntityType, Integer, Integer>>> getPrimaryAndSecondaryEntities(Entity entity, List<Quadruple<String, NamedEntityType, Integer, Integer>> otherEntities) {
		List<Quintuple<String, String, NamedEntityType, Integer, Integer>> priEntities = new ArrayList<>();
		List<Quintuple<String, String, NamedEntityType, Integer, Integer>> addEntities = new ArrayList<>();
		
		for (Iterator<Quadruple<String, NamedEntityType, Integer, Integer>> it = otherEntities.iterator(); it.hasNext(); ) {
			Quadruple<String, NamedEntityType, Integer, Integer> oEntity = it.next();
			
			
			if (isPrimaryEntity(entity, oEntity)) { 
				Quintuple<String, String, NamedEntityType, Integer, Integer> e = new SimpleQuintuple<>(oEntity.first(), "NE", oEntity.second(), oEntity.third(), oEntity.fourth());
				priEntities.add(e);						
			} else {
				Quintuple<String, String, NamedEntityType, Integer, Integer> e = new SimpleQuintuple<String, String, NamedEntityType, Integer, Integer>(oEntity.first(), "AE", oEntity.second(), oEntity.third(), oEntity.fourth());
				addEntities.add(e);
			}		
		}
		Pair<List<Quintuple<String, String, NamedEntityType, Integer, Integer>>, List<Quintuple<String, String, NamedEntityType, Integer, Integer>>> pair = new SimplePair<>(priEntities, addEntities);
		return pair;			
	}
	
	private static boolean isPrimaryEntity(Entity entity, Quadruple<String, NamedEntityType, Integer, Integer> otherEntity) { 
		assert entity != null;
		assert otherEntity != null;
		
		String frmttdOtherEntity = otherEntity.second().name().substring(0, 3) + "#" + otherEntity.first();		
		return entity.isSameAs(frmttdOtherEntity);		
	}

	private static class Entity {
		
		Entity(EntityI entity, String entityType) { 
			if (entity == null) throw new NullPointerException("entity: null");
			if (entityType == null) throw new NullPointerException("entityType: null");
			
			List<String> names = getNames(entity);
			String eType = getEType(entityType);
			this.names = format(names, eType);			
		}
		
		Entity(List<String> names, String entityType) { 
			if (names == null) throw new NullPointerException("names: null");
			if (entityType == null) throw new NullPointerException("entityType: null");
			
			String eType = getEType(entityType);
			this.names = format(names, eType);
		}
		
		private List<String> format(List<String> names, String eType) { 
			assert names != null;
			assert eType != null;
			
			List<String> fmttdNames = new ArrayList<>();
			for (String name : names) { 
				String fmttdName = format(name, eType);
				fmttdNames.add(fmttdName);				
			}
			return fmttdNames;
		}
		
		private String format(String name, String eType) { 
			assert name != null;
			assert eType != null;
			
			return eType == null ? name : eType + "#" + name;
		}
		
		/**
		 * An eType is the short version of the entity type. (e.g. PER for PERSON, LOC for LOCATIOM, ORG for ORGANIZATION)
		 * 
		 * @param entityType A string holding the named entity type.
		 * 
		 */
		private String getEType(String entityType) { 
			assert entityType != null;
			
			return entityType.isEmpty() ? entityType : entityType.toUpperCase().substring(0, 3);
		}
		
		Entity(List<String> names) { 
			if (names == null) throw new NullPointerException("names: null");
			
			this.names = names;
		}
		
		/**
		 * otherNamne must be in this form: PER#Entity, ORG#Entity, LOC#Entity 
		 * 
		 * @param otherName
		 * @return
		 */
		boolean isSameAs(String otherName) { 
			boolean sameAs = false;
			
			for (Iterator<String> it = names.iterator(); !sameAs && it.hasNext(); ) {
				String name = it.next();
				sameAs = compare(name, otherName);
			}
			return sameAs;
		}
		
		private static List<String> getNames(EntityI entity) {
			if (entity == null) throw new NullPointerException("entity: null");
			
			List<String> names = new ArrayList<>(entity.getAliases());
			if (entity.getName() != null) {
				names.add(entity.getName());
			}
			return names;			
		}		

		private boolean compare(String name, String otherName) { 
			assert name != null;
			assert otherName != null;
			
			ENTITY_CMP.compare(name, otherName);
			double score = ENTITY_CMP.getScore();
			
			System.out.println("sim(\"" + name + "\", \"" + otherName + "\") -> " + score + ".");
			return score > THRESHOLD;
		}
		
		private final static EntityComparison ENTITY_CMP = new EntityComparison();
		
		private final static double THRESHOLD = .5;
		
		final List<String> names;
		
	}
		
	private static Object readObject(String inFile) { 
		assert inFile != null;
		
		return FileUtils.readObject(inFile);
	}
	
	private static String readText(String inFile) {
		assert inFile != null;
		
		return FileUtils.readText(inFile);		
	}
	
	private static List<File> listFiles(String pathname, FilenameFilter filter) { 
		assert pathname != null;
		
		File in = new File(pathname);
		File[] files = in.listFiles(filter);
		return files != null ? Arrays.asList(files) : Collections.<File>emptyList();
	}
	
	/*
	private static List<File> listFiles(String pathname) { 
		assert pathname != null;
		
		File in = new File(pathname);
		File[] files = in.listFiles();
		return files != null ? Arrays.asList(files) : Collections.<File>emptyList();
	}
	*/
	
	private final static AnnotationApi ANNOTATOR = new BasicAnnotationApi();
	
	private final static FilenameFilter DIR_FILTER = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory();
		}
	}; 
	
	private final static FilenameFilter FILE_FILTER = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isFile();
		}		
	};

}