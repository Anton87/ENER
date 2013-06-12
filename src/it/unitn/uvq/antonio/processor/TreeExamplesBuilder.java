package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.TypeI;
import it.unitn.uvq.antonio.nlp.annotation.Annotation;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Pair;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
import it.unitn.uvq.antonio.util.tuple.SimplePair;
import it.unitn.uvq.antonio.util.tuple.SimpleQuintuple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes.Name;

import svmlighttk.SVMExampleBuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

public class TreeExamplesBuilder extends ExamplesBuilder {
	
	public TreeExamplesBuilder(String namedEntityType, String notableTypeID, int maxExamples, String dest) {
		super(namedEntityType, notableTypeID);
		if (dest == null) throw new NullPointerException("destFile: null");
		if (maxExamples < 0) throw new NullPointerException("maxExamples < 0: " + maxExamples);
		
		this.maxExamples = maxExamples;
		//this.hasType = NEUtils.hasType(namedEntityType);		
		
		try {
			map.put("dat", initWriter(newpath(dest, encode(notableTypeID)) + ".dat"));
			map.put("txt", initWriter(newpath(dest, encode(notableTypeID)) + ".txt"));
			map.put("tsv", initWriter(newpath(dest, encode(notableTypeID)) + ".tsv"));
		} catch (FileNotFoundException e) {
			System.err.println("(EE): Error while initing out streams.");
			System.exit(-1);
		}
	}
	
	private PrintWriter initWriter(String filepath) throws FileNotFoundException {
		assert filepath != null;
		
		PrintWriter writer = null;		
		try {
			writer  = new PrintWriter(filepath);
		} catch (FileNotFoundException e) {
			System.err.println("(EE): File not found: \"" + filepath + "\".");
			throw e;
		}
		return writer;
	}
		
	@SuppressWarnings("deprecation")
	private String encode(String str) {
		assert str != null;
		
		return URLEncoder.encode(str);
	}

	
	@Override
	public void process(String priEntityType, String notableTypeId,
			String mid, EntityI entity, String alias, List<String> acronyms, String paragraph,
			Triple<String, Integer, Integer> sent,
			List<Quadruple<String, String, Integer, Integer>> nes) {
				
		
		//List<String> priEntityNames = getEntityNames(entity);
		
		//NEUtils.ResultSet rs = NEUtils.getInstance().getRankedEntities(nes, priEntityNames, priEntityType);
	
		List<Quintuple<String, String, String, Integer, Integer>> priEntities = new ArrayList<>();
		
		List<Quintuple<String, String, String, Integer, Integer>> sndEntities = new ArrayList<>();
		
		
		
		Tree tree = parse(sent.first());
		
		if (!isWellFormedTree(tree)) {
			System.out.print("X");
			//System.out.println("(EE): X-Tree: " + tree);
			return; 
		}
		
		// The list of primary entities appearing in the sentence.
		priEntities = getMentions(entity, tree, sent.first());
		
		// The lis of additional entities appearing in the sentence.
		List<Quintuple<String, String, String, Integer, Integer>> aes = new ArrayList<>();
		for (Quadruple<String, String, Integer, Integer> ne : nes) { 
			Quintuple<String, String, String, Integer, Integer> ae = 
					new SimpleQuintuple<>(ne.first(), ne.second(), "AE", ne.third(), ne.fourth());
			aes.add(ae);
		}
		
		/* We keep only the additional entities (AE) with the same 
		 * named entity type of our primary entity. */
		aes = new ArrayList<>(
				Collections2.filter(aes, NEUtils.hasType(priEntityType)));
		
		List<Pair<Quintuple<String, String, String, Integer, Integer>,
			      Quintuple<String, String, String, Integer, Integer>>> overlaps = findOverlaps(priEntities, aes);
		
		
		/* Remove the additional entities which overlap primary entities. */
		for (Pair<Quintuple<String, String, String, Integer, Integer>,
				  Quintuple<String, String, String, Integer, Integer>> overlap : overlaps) {
			aes.remove(overlap.second());
		}
			     
		
		/*
		priEntities.addAll(mentions);
		for (Quadruple<String, String, Integer, Integer> ne : nes) {
			if (!ne.second().equals(priEntityType)) { continue; }
			boolean overlap = false;
			for (int i = 0; !overlap && i < priEntities.size(); i++) {
				Quintuple<String, String, String, Integer, Integer> priNe = priEntities.get(i);
				IntRange neSpan = new IntRange(ne.third(), ne.fourth());
				IntRange priSpan = new IntRange(priNe.fourth(), priNe.fifth());
				overlap = neSpan.overlap(priSpan);
			}
			if (!overlap) {
				Quintuple<String, String, String, Integer, Integer> sndNe = 
						new SimpleQuintuple<String, String, String, Integer, Integer>(ne.first(), ne.second(), "AE", ne.third(), ne.fourth());
				sndEntities.add(sndNe);
			}			
		}
		*/
		
		//System.out.println("(EE): Tree: " + tree);		
		//System.out.println("(EE): Sent: " + sent.first());
		//System.out.println("(EE): Entity Mentions: " + mentions);
		
		if (!priEntities.isEmpty()) {
			List<TextAnnotationI> priAnnotations = map(priEntities, toAnnotation);
			List<TextAnnotationI> sndAnnotations = map(sndEntities, toAnnotation);
			
			if (isAtLeastOneAnnotable(tree, priAnnotations)) {
				String bow = buildBOW(sent.first());
				
				Tree aTree = annotate(tree, priAnnotations, sndAnnotations);
				
				String example = buildSVMExample(bow, aTree.toString());
				map.get("dat").println(example);
				
				String priEntityTypes = join(SEPARATOR, getEntityTypes(entity));
				map.get("tsv").println(priEntityTypes);
				
				map.get("txt").println(sent.first());
				examplesNum += 1;
				
				System.out.print(".");				
			}			
		}
		
		if (examplesNum >= maxExamples) {
			ExamplesDownloader.running = false;
			close();
		}
	}
	
	/**
	 * Find and return overlaps between two list of named entities.
	 * 
	 * @param ents1 A list of entities 
	 * @param ents2 A list of entities
	 * @return A list hilding the overlapping pairs of entities 
	 */
	private List<Pair<Quintuple<String, String, String, Integer, Integer>, Quintuple<String, String, String, Integer, Integer>>> findOverlaps(
			List<Quintuple<String, String, String, Integer, Integer>> ents1,
			List<Quintuple<String, String, String, Integer, Integer>> ents2) {
		assert ents1 != null;
		assert ents2 != null;
		
		
		List<Pair<Quintuple<String, String, String, Integer, Integer>, Quintuple<String, String, String, Integer, Integer>>> overlaps = new
				ArrayList<>();
		for (Quintuple<String, String, String, Integer, Integer> e1 : ents1) {
			
			IntRange e1Span = new IntRange(e1.fourth(), e1.fifth());
			for (Quintuple<String, String, String, Integer, Integer> e2 : ents2) {
				IntRange e2Span = new IntRange(e2.fourth(), e2.fourth());
				if (e1Span.overlap(e2Span)) {
					Pair<Quintuple<String, String, String, Integer, Integer>,
					     Quintuple<String, String, String, Integer, Integer>> overlap =
					     new SimplePair<>(e1, e2);
					overlaps.add(overlap);
				}
			}			
		}
		return overlaps;
		
		/*
		priEntities.addAll(mentions);
		for (Quadruple<String, String, Integer, Integer> ne : nes) {
			if (!ne.second().equals(priEntityType)) { continue; }
			boolean overlap = false;
			for (int i = 0; !overlap && i < priEntities.size(); i++) {
				Quintuple<String, String, String, Integer, Integer> priNe = priEntities.get(i);
				IntRange neSpan = new IntRange(ne.third(), ne.fourth());
				IntRange priSpan = new IntRange(priNe.fourth(), priNe.fifth());
				overlap = neSpan.overlap(priSpan);
			}
			if (!overlap) {
				Quintuple<String, String, String, Integer, Integer> sndNe = 
						new SimpleQuintuple<String, String, String, Integer, Integer>(ne.first(), ne.second(), "AE", ne.third(), ne.fourth());
				sndEntities.add(sndNe);
			}			
		}
		*/
	}
	
	private boolean isWellFormedTree(Tree tree) {
		for (Tree node : tree.getNodes()) {
			if (!node.isLeaf() && node.getText().equals("X")) {
				return false;
			}
		}
		return true;		
	}
	
	private List<Quintuple<String, String, String, Integer, Integer>> getMentions(EntityI entity, Tree tree, String sent) {
		assert entity != null;
		assert tree != null;
		assert sent != null;
		
		Set<String> names = new HashSet<>(entity.getAliases());		
		names.add(entity.getName());
		
		//System.out.println("(EE): names(entity): " + names);
		//System.out.println("(EE): sent: " + sent);
		//System.out.println("(EE): tree: " + tree);
		
		Set<Quintuple<String, String, String, Integer, Integer>> mentions = new HashSet<>();
		
		// Compute the spans of all the names appearing in the tree
		List<IntRange> nameSpans = new ArrayList<>(); 
		for (String name : names) {
			if (sent.contains(name)) {
				IntRange nameSpan = getTreeSubspan(name, sent, tree);
				
				/* This may happen when an entity name or alias differs for some letters
				 * from the text in the sentence.
				 */
				if (nameSpan == null) continue;
				
				//System.out.println("(EE): span(\"" + name + "\"): " + nameSpan);
				
				TextAnnotation a = new TextAnnotation("NE", nameSpan.start(), nameSpan.end());
				TreeBuilder tb = new TreeBuilder(tree);
				if (annotator.isAnnotable(a, tb)) {
					IntRange oldNameSpan = checkAndReturnOverlap(nameSpan, nameSpans);
					if (oldNameSpan == null) {
						nameSpans.add(nameSpan);
					/* If a valid mention for another name has already found, 
					 * keep the longest one.
					 */
					} else if (nameSpan.len() > oldNameSpan.len()) {
						Collections.replaceAll(nameSpans, oldNameSpan, nameSpan);
						// nameSpans.remove(otherNameSpan);
						// nameSpans.add(nameSpan);					
					}
				}
			}
		}
		
		
		for (IntRange span : nameSpans) {
			String name = sent.substring(span.start(), span.end());
			Quintuple<String, String, String, Integer, Integer> mention = 
				new SimpleQuintuple<>(name, "", "NE", span.start(), span.end());
			mentions.add(mention);
		}
		return new ArrayList<Quintuple<String, String, String, Integer, Integer>>(mentions);		
	}
	
	
	private IntRange checkAndReturnOverlap(IntRange span, List<IntRange> spans) {
		assert span != null;
		assert spans != null;
		
		for (IntRange oSpan : spans) { 
			if (oSpan.overlap(span)) {
				return oSpan;
			}
		}
		return null;
	}
	
	private IntRange getTreeSubspan(String pattern, String text, Tree tree) {
		assert pattern != null;
		assert text != null;
		assert tree != null;
		
		int start;
		int end;
		
		List<Tree> leaves = tree.getLeaves();
		for (int i = 0; i < leaves.size(); i++) {
			
			// <!-- Inserted code to speed substring computation
			if (!pattern.startsWith(leaves.get(i).getText())) continue;					
			// -->
			start = i;
			for (int j = i + 1; j <= leaves.size(); j++) {
				end = j;
				start = leaves.get(i).getSpan().start();
				end = leaves.get(j - 1).getSpan().end();
				
				if (text.substring(start, end).equals(pattern)) {
					return new IntRange(start, end);					
				}
			}
		}
		return null;		
	}
	
	private void close() {
		for (PrintWriter writer : map.values()) {
			writer.close();
		}
	}
	
	private String buildBOW(String text) { 
		assert text != null;
		
		List<Triple<String, Integer, Integer>> tokens = tokenize(text);
		return buildBOW(tokens);
	}
	
	private String buildBOW(List<Triple<String, Integer, Integer>> tokens) { 
		assert tokens != null;
		
		StringBuilder sb = new StringBuilder("(BOW ");
		for (Triple<String, Integer, Integer> triple : tokens) { 
			sb.append("(" + triple.first() + " *)");
		}
		sb.append(")");
		return sb.toString();
	}
	
	private List<Triple<String, Integer, Integer>> tokenize(String text) { 
		assert text != null;
		
		return Tokenizer.getInstance().tokenizePTB3Escaping(text);
		
	}
	
	private List<Triple<String, Integer, Integer>> tokenizePTB3Escaping(String text) { 
		assert text != null;
		
		return Tokenizer.getInstance().tokenize(text);
	}
	
	private Tree parse(String sent) { 
		assert sent != null;
		
		return Parser.getInstance().parse(sent);
	}
	
	private void stop() { 
		ExamplesDownloader.running = false;
	}
	
	private String buildSVMExample(String bow, String tree) { 
		assert bow != null;
		assert tree != null;
		
		return new SVMExampleBuilder()
			.addTree(bow)
			.addTree(tree)
			.build();
	}
	
	private static String join(String sep, List<String> parts) { 
		assert sep != null;
		assert parts != null;
		
		return Joiner.on(sep).join(parts);
	}
	
	private static String newpath(String... parts) { 
		assert parts != null;
		
		return Joiner.on(File.separator).join(parts);
	}
	
	// Returns true if at least one annotations in the tree can be done.
	private boolean isAtLeastOneAnnotable(Tree tree, List<TextAnnotationI> annotations) {
		assert tree != null;
		assert annotations != null;
		
		TreeBuilder tb = new TreeBuilder(tree);
		boolean isAnnotable = false;
		for (Iterator<TextAnnotationI> it = annotations.iterator(); !isAnnotable && it.hasNext(); ) {
			TextAnnotationI a = it.next();
			isAnnotable = annotator.isAnnotable(a, tb); 
		}
		return isAnnotable;
	}

	private Tree annotate(Tree tree, List<TextAnnotationI> annotations, @SuppressWarnings("unchecked") List<TextAnnotationI>... otherAnnotations) {
		assert tree != null;
		assert annotations != null;
		assert otherAnnotations != null;
		
		TreeBuilder tb = new TreeBuilder(tree);		
		annotate(tb, annotations);
		for (List<TextAnnotationI> oAnnotations : otherAnnotations) {
			annotate(tb, oAnnotations);
		}		
		return tb.build();
	}
	
	private TreeBuilder annotate(TreeBuilder tb, List<TextAnnotationI> annotations) {
		assert tb != null;
		assert annotations != null;
		
		for (TextAnnotationI a : annotations) { 
			annotator.annotate(a, tb);
		}
		return tb;
	}
	
	private List<String> getEntityTypes(EntityI entity) {
		assert entity != null;
		
		List<String> types = new ArrayList<>();
		for (TypeI type : entity.getNotableTypes()) { 
			types.add(type.getId());
		}
		if (entity.getNotableFor() != null && !types.contains(entity.getNotableFor().getId())) {
			types.add(0, entity.getNotableFor().getId());
		}
		return types;		
	}
	
	private static <F, T> List<T> map(List<F> elems, Function<F, T> func) {
		assert elems != null;
		assert func != null;
		
		return new ArrayList<>(Collections2.transform(elems, func));
	}
	
	private static Function<Quintuple<String, String, String, Integer, Integer>, TextAnnotationI> toAnnotation = 
			new Function<Quintuple<String,String,String,Integer,Integer>, TextAnnotationI>() {
	
		public TextAnnotationI apply(Quintuple<String,String,String,Integer,Integer> entity) {
			if (entity == null) throw new NullPointerException("entity: null");
		
			return new TextAnnotation(entity.third(), entity.fourth(), entity.fifth());
		}
	};	

	private final static String SEPARATOR = "\t";
	
	private static AnnotationApi annotator = new BasicAnnotationApi();
	
	// private final HasType hasType;
	
	private final int maxExamples;
	
	private int examplesNum = 0;
	
	private Map<String, PrintWriter> map = new HashMap<>();
	
	/*
	private Comparator<String> strLenCmp = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.length() - o1.length();
		}		
	};
	*/
	
}
