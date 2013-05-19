package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.TypeI;
import it.unitn.uvq.antonio.ml.bayes.Classifier;
import it.unitn.uvq.antonio.ml.bayes.Models;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.BasicAnnotationApi;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotation;
import it.unitn.uvq.antonio.nlp.annotation.TextAnnotationI;
import it.unitn.uvq.antonio.nlp.parse.VectorParser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.tree.TreeBuilder;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.processor.NEUtils.HasType;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Quintuple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import svmlighttk.SVMExampleBuilder;
import svmlighttk.SVMVector;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

public class ShallowPlusVecExamplesBuilder extends ExamplesBuilder {

	public ShallowPlusVecExamplesBuilder(String namedEntityType, String notableTypeId, int maxExamples, String destFile) {
		super(namedEntityType, notableTypeId);
		if (destFile == null) throw new NullPointerException("destFile: null");
		if (maxExamples < 0) throw new IllegalArgumentException("maxExamples < 0: " + maxExamples);
		
		init(Models.values());
		this.maxExamples = maxExamples;
		this.hasType = NEUtils.hasType(namedEntityType);
		
		String vecFile = pathjoin(File.separator, destFile, "shallow_vec", encode(notableTypeId) + ".txt");
		String typeFile = pathjoin(File.separator, destFile, "shallow_vec", encode(notableTypeId) + ".tsv");
		
		try {		
			this.vecOut = new PrintStream(new FileOutputStream(vecFile));
		} catch (IOException e) { 
			System.err.println("(EE): " + e.getMessage() + ", file=\"" + vecFile + "\".");
			System.exit(1);
		} 
		
		try {
			this.typeOut = new PrintStream(new FileOutputStream(typeFile));
		} catch (IOException e) {
			System.err.println("(EE): " + e.getMessage() + ", file=\"" + typeFile + "\".");
			System.exit(1);
		} 
	}
	
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
		
		
		List<String> priEntityNames = getEntityNames(entity);
		
		NEUtils.ResultSet rs = NEUtils.getInstance().getRankedEntities(nes, priEntityNames, priEntityType);
	
		List<Quintuple<String, String, String, Integer, Integer>> priEntities = rs.filter(NEUtils.IsPrimary).entities();
		
		List<Quintuple<String, String, String, Integer, Integer>> sndEntities = rs
				.filter(NEUtils.IsSecondary)
				.filter(hasType)
				.entities();		
		
		if (!priEntities.isEmpty()) {
			
			List<TextAnnotationI> priAnnotations = map(priEntities, toAnnotation);
			List<TextAnnotationI> sndAnnotations = map(sndEntities, toAnnotation);
			
			Tree vector = shallowParse(sent.first());
			
			if(isAtLeastOneAnnotable(vector, priAnnotations)) {
				String bow = buildBOW(sent.first());
				@SuppressWarnings("unchecked")
				Tree aVec = annotate(vector, priAnnotations, sndAnnotations);
				
				SVMVector probsVec = buildProbsVec(sent.first());				
				String svmExample = buildSVMExample(bow, aVec.toString(), probsVec);
				vecOut.println(svmExample);
				
				List<String> priEntityTypes = getEntityTypes(entity);
				typeOut.println(join(SEPARATOR, priEntityTypes));
				
				examplesNum += 1;			
				System.out.print(".");
			}
		}
		
		if (examplesNum >= maxExamples) { stop(); }
	}
	
	private SVMVector buildProbsVec(String sent) { 
		assert sent != null;
		
		SVMVector vec = new SVMVector();
		for (Classifier classifier : classifiers) { 
			double prob = classifier.classify(sent, true);
			vec.addFeature(prob);
		}
		return vec;
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
	
	private Tree shallowParse(String sent) { 
		assert sent != null;
		
		return VectorParser.getInstance().parse(sent);
	}
	
	private void stop() { 
		vecOut.close();
		typeOut.close();
		System.exit(0);
	}
	
	private String buildSVMExample(String bow, String tree, SVMVector vec) { 
		assert bow != null;
		assert tree != null;
		assert vec != null;
		
		return new SVMExampleBuilder()
			.addTree(bow)
			.addTree(tree)
			.addVector(vec)
			.build();
	}
	
	private static String join(String sep, List<String> parts) { 
		assert sep != null;
		assert parts != null;
		
		return Joiner.on(sep).join(parts);
	}
	
	private static String pathjoin(String... parts) { 
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
	
	
	private static void init(Models[] models) { 
		assert models != null;
		
		classifiers = new ArrayList<>(); 
		for (Models model : models) { 
			Classifier classifier = new Classifier(model);
			classifiers.add(classifier);
		}
	}
	
	private final static String SEPARATOR = "\t";
	
	private static AnnotationApi annotator = new BasicAnnotationApi();
	
	private static List<Classifier> classifiers;
		
	private final HasType hasType;
	
	private final int maxExamples;
	
	private PrintStream vecOut;
	
	private PrintStream typeOut;
	
	private int examplesNum = 0;

}