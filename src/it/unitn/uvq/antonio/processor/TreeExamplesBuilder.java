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
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
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
	
	/*
	private boolean makedirs(String pathname) {
		assert pathname != null;
		
		return new File(pathname).mkdirs();
	}
	
	private boolean existsDir(String pathname) {
		assert pathname != null;
		
		return new File(pathname).isDirectory();
	}
	*/
		
	@SuppressWarnings("deprecation")
	private String encode(String str) {
		assert str != null;
		
		return URLEncoder.encode(str);
	}

	
	@Override
	public void process(String priEntityType, String notableTypeId,
			String mid, EntityI entity, String paragraph,
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
		
		//List<Triple<String, Integer, Integer>> tokens = tokenize(sent.first());
		
		//Set<Tree> terminalsGParents = getTerminalGranParents(tree);
		
		//Set<Quintuple<String, String, String, Integer, Integer>> mentions = getMentions(entity, terminalsGParents, tokens, sent.first());
		Set<Quintuple<String, String, String, Integer, Integer>> mentions = getMentions(entity, tree, sent.first());
		
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
	
	private boolean isWellFormedTree(Tree tree) {
		for (Tree node : tree.getNodes()) {
			if (!node.isLeaf() && node.getText().equals("X")) {
				return false;
			}
		}
		return true;		
	}
	
	private Set<Quintuple<String, String, String, Integer, Integer>> getMentions(EntityI entity, Tree tree, String sent) {
		assert entity != null;
		assert tree != null;
		assert sent != null;
		
		Set<String> names = new HashSet<>(entity.getAliases());
		names.add(entity.getName());
		
		//System.out.println("(EE): names(entity): " + names);
		//System.out.println("(EE): sent: " + sent);
		//System.out.println("(EE): tree: " + tree);
		
		Set<Quintuple<String, String, String, Integer, Integer>> mentions = new HashSet<>();
		
		// Compute the spans of all the name appearing in a dict
		Set<IntRange> nameSpans = new HashSet<>(); 
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
					IntRange otherNameSpan = checkAndReturnOverlap(nameSpan, nameSpans);
					if (otherNameSpan == null) {
						nameSpans.add(nameSpan);
					} else if (nameSpan.len() > otherNameSpan.len()) {
						nameSpans.remove(otherNameSpan);
						nameSpans.add(nameSpan);
					}
				}
			}
		}
		
		
		for (IntRange span : nameSpans) {
			String name = sent.substring(span.start(), span.end());
			Quintuple<String, String, String, Integer, Integer> mention = 
				new SimpleQuintuple<String, String, String, Integer, Integer>(name, "", "NE", span.start(), span.end());
			mentions.add(mention);
		}
		return mentions;		
	}
	
	
	private IntRange checkAndReturnOverlap(IntRange span, Set<IntRange> spans) {
		assert span != null;
		assert spans != null;
		
		for (IntRange otherSpan : spans) { 
			if (otherSpan.overlap(span)) {
				return otherSpan;
			}
		}
		return null;
	}

	/*
	private Set<Quintuple<String, String, String, Integer, Integer>> getMentions(EntityI entity, Set<Tree> terminalsGParents, List<Triple<String, Integer, Integer>> tokens, String sent) {
		assert entity != null;
		assert terminalsGParents != null;
		assert sent != null;
		
		Set<String> names = new HashSet<>(entity.getAliases());
		names.add(entity.getName());
		
		//System.out.println("(EE): names(entity): " + names);
		
		//System.out.println("(EE): sent: " + sent);
		
		Set<Quintuple<String, String, String, Integer, Integer>> mentions = new HashSet<>();
		
		for (Tree gParent : terminalsGParents) {
			IntRange span = gParent.getSpan();
			String text = sent.substring(span.start(), span.end());
			//System.out.println("(EE): constituency: " + text);
			//if (names.contains(text)) {
			List<String> namesInText = getNamesInText(text, names);
			System.out.println("(EE): names found: " + namesInText);
			
			if (!namesInText.isEmpty()) {
				String maxLenName = getLongestName(namesInText);
				System.out.println("(EE): Found name \"" + maxLenName + "\" in subtree: \"" + gParent + "\".");
				
				// Get the span of the portion of subtree containing the name
				IntRange nameSpan = getTreeSubspan(maxLenName, sent, gParent);
				
				if (nameSpan != null) {
					Quintuple<String, String, String, Integer, Integer> mention = 
						new SimpleQuintuple<>(text, "ORGANIZATION", "NE", nameSpan.start(), nameSpan.end());
					// An entity mention found.
					mentions.add(mention);
				}
			}
		}
		return mentions;
	}
	*/
	
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
	
	private String getLongestName(List<String> names) {
		assert names != null;
		
		Collections.sort(names, strLenCmp);
		return names.get(0);
	}
	
	/**
	 * Returns all the names in teh list contains in the specified text.
	 * 
	 */
	private List<String> getNamesInText(String text, Collection<String> names) {
		assert text != null;
		assert names != null;
		
		List<String> namesFound = new ArrayList<>();
 		for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
			String name = it.next();
			if (text.contains(name)) { names.add(name); }
		}
 		return namesFound;
	}
	
	
	
	
	private IntRange find(int beginCh, int endCh, Tree tree) {
		assert beginCh >= 0;
		assert endCh > beginCh;
		assert tree != null;
		
		List<Tree> leaves = new ArrayList<>();
		int startNode = 0;
		int endNode = 0;
		for (int i = 0; i < leaves.size(); i++) {
			Tree leaf = leaves.get(i);
					
			if (leaf.getSpan().start() == beginCh) {
				startNode = i;
			}
			if (leaf.getSpan().end() == endCh) {
				endNode = i + 1;
			}
		}
		return new IntRange(startNode, endNode);		
	}
	
	private Set<Tree> getTerminalGranParents(Tree tree) {
		assert tree != null;
		
		Set<Tree> gParents = new HashSet<>();
		for (Tree leaf : tree.getLeaves()) {
			Tree gParent = leaf.getParent().getParent();
			gParents.add(gParent);
		}
		return gParents;
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
	
	private List<String> getEntityNames(EntityI entity) { 
		assert entity != null;
		
		List<String> names = new ArrayList<>(entity.getAliases());
		if (entity.getName() != null) { names.add(0, entity.getName()); }
		return names;
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
	
	private Comparator<String> strLenCmp = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.length() - o1.length();
		}		
	};
	
}
