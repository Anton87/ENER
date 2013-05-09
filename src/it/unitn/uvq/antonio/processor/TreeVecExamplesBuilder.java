package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.TypeI;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.ner.NamedEntityType;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.processor.NEUtils.HasType;
import it.unitn.uvq.antonio.util.IntRange;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import svmlighttk.SVMExampleBuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import edu.stanford.nlp.optimization.HasEvaluators;

public class TreeVecExamplesBuilder extends ExamplesBuilder {
	
	public TreeVecExamplesBuilder(String entityType, String notableTypeId, int maxExamples, String destFile) {
		super(entityType, notableTypeId);
		if (destFile == null) throw new NullPointerException("destFile: null");
		if (examplesNum < 0) throw new NullPointerException("examplesNum < 0: " + examplesNum);
		
		this.maxExamples = maxExamples;
		this.hasType = NEUtils.hasType(entityType);		
		
		try {
			
			this.vecOut = new PrintStream(new FileOutputStream(destFile + File.separator + "vec" + File.separator + encode(notableTypeId) + ".txt"));
			this.treeOut = new PrintStream(new FileOutputStream(destFile + File.separator + "tree" + File.separator + encode(notableTypeId) + ".txt"));
			this.typeOut = new PrintStream(new FileOutputStream(destFile + File.separator + "type" + File.separator + encode(notableTypeId) + ".txt"));
			this.paragraphOut = new PrintStream(new FileOutputStream(destFile + File.separator + "abstracts" + File.separator + encode(notableTypeId) + ".txt"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
		
	private String encode(String str) {
		assert str != null;
		
		return URLEncoder.encode(str);
	}
	
	private String stripExt(String filepath) {
		assert filepath != null;
		
		int i = filepath.lastIndexOf(".");
		return i == -1 ? filepath : filepath.substring(0, i);
	}

	@Override
	public void process(
			String priEntityType,  // e.g. {@enum PERSON, LOCATION, ORGANIZATION}
			String mid,
			EntityI entity,
			String paragraph, 
			List<Triple<String, Integer, Integer>> toks,
			Triple<String, Integer, Integer> sent,
			List<Quadruple<String, String, Integer, Integer>> poss,
			List<Quadruple<String, String, Integer, Integer>> nes, Tree tree,
			Tree vec) {
		
		if (paragraph != null && !paragraph.equals(this.paragraph)) {
			this.paragraph = paragraph;
			paragraphOut.println(this.paragraph);
		}
		
		//System.out.println("(II): Entity:" + entity);
		
		String bow = buildBOW(toks);
		
		//System.out.println("(II): BOW: " + bow);
		
		//System.out.println("(II): Sent: " + sent.first());
		
		List<String> priEntityNames = getEntityNames(entity);
		NEUtils.ResultSet rs = NEUtils.getInstance().getRankedEntities(nes, priEntityNames, priEntityType);
					
		List<Quintuple<String, String, String, Integer, Integer>> priEntities = rs.filter(NEUtils.IsPrimary).entities();
		
		// System.out.println("(II): NEs: " + priEntities);
		
		List<Quintuple<String, String, String, Integer, Integer>> sndEntities = rs
				.filter(NEUtils.IsSecondary)
				.filter(hasType)
				.entities();
				
		// System.out.println("(II): AEs[type=\"" + priEntityType + "\"]: " + sndEntities);
		
		
		if (!priEntities.isEmpty()) {
			
			List<TextAnnotationI> priAnnotations = map(priEntities, toAnnotation);
			List<TextAnnotationI> sndAnnotations = map(sndEntities, toAnnotation);
			
			if (examplesNum < maxExamples && isAtLeastOneAnnotable(tree, priAnnotations)) {
				Tree aVec = annotate(vec, priAnnotations, sndAnnotations);
				//System.out.println("(II): aVec: " + aVec);				
				
				String vecExample = buildSVMExample(bow, aVec.toString());				
				//System.out.println("(II): VecEx:  " + vecExample);
				
				Tree aTree = annotate(tree, priAnnotations, sndAnnotations);
				//System.out.println("(II): aTree: " + aTree);
				
				String treeExample = buildSVMExample(bow, aTree.toString());
				//System.out.println("(II): TreeEx: " + treeExample);

				vecOut.println(vecExample);			
				treeOut.println(treeExample);				
				
				String priEntityTypes = join(SEPARATOR, getEntityTypes(entity));
				typeOut.println(priEntityTypes);
				
				examplesNum += 1;			
				System.out.print(".");
			}
			
		}
		
		if (examplesNum >= maxExamples) { stop(); }
	}
	
	private void stop() { 
		closeAll();
		System.exit(0);
	}
	
	private void closeAll() { 
		vecOut.close();
		treeOut.close();
		typeOut.close();
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

	private Tree annotate(Tree tree, List<TextAnnotationI> annotations, List<TextAnnotationI>... otherAnnotations) {
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
	
	private String buildBOW(List<Triple<String, Integer, Integer>> toks) { 
		assert toks != null;
		
		StringBuilder sb = new StringBuilder("(BOW ");
		for (Triple<String, Integer, Integer> tok : toks) { 
			sb.append("(" + tok.first() + " *)");
		}
		return sb.append(")").toString();	
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
		if (entity.getNotableFor() != null && types.contains(entity.getNotableFor().getId())) {
			types.add(entity.getNotableFor().getId());
		}
		return types;		
	}
	
	private void init(PrintStream out, String filepath) {
		assert out != null;
		assert filepath != null;
		
		try {
			out = new PrintStream(
					new FileOutputStream(filepath));
		} catch (FileNotFoundException e) {
			System.err.println("(EE): File not found: \"" + filepath + "\".");
			System.exit(0);
		}
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
	
	private int examplesNum = 0;
	
	private PrintStream vecOut;
	
	private PrintStream treeOut;
	
	private PrintStream typeOut;
	
	private PrintStream paragraphOut;
	
	private final HasType hasType;
	
	private final int maxExamples;
	
	private String paragraph;
	
	
	//private final Logger logger = Logger.getLogger(getClass().getName());
	
}
