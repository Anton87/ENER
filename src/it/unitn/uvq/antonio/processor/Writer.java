package it.unitn.uvq.antonio.processor;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import it.unitn.uvq.antonio.entity.TopicsRepository;
import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.freebase.db.NotableType;
import it.unitn.uvq.antonio.freebase.db.TypeI;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;
import it.unitn.uvq.antonio.nlp.annotation.AnnotationI;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.VectorParser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.pos.POSTagger;
import it.unitn.uvq.antonio.nlp.sent.SentSplitter;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Triple;

public class Writer {

	private final static int ABSTRACTS_NUM = 100;
	
	private final static String outFile = "/home/antonio/Scrivania";
	
	private final static TypeI TYPE = NotableType.COMPUTER_SCIENTIST;
	
	
	public static void main(String[] args) { 
	
		List<String> mids = TopicsRepository.getMidsByNotableTypeId(TYPE.getId(), ABSTRACTS_NUM);
		
		for (String mid : mids) {

			System.out.println("(II): mid: \"" + mid + "\"");
			
			// Save the wikipedia Abstract text on file.
			String paragraph = getWikiAbstract(mid);
			String pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", paragraph.hashCode() + ".txt");
			if (paragraph != null && !paragraph.isEmpty()) { 
				writeText(paragraph, pathname); 
				

				System.out.println("(II): paragraph: \"" + paragraph + "\"");
				

				
				// Save the paragraph's tokens
				pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", "toks");
				List<Triple<String, Integer, Integer>> toks = tokenize(paragraph);
				List<AnnotationI> toksAnnotations = getToksAnnotations(paragraph);
				writeObjects(toksAnnotations, pathname);
				

				System.out.println("(II): toks: " + toks);
			
				// Save the sents annotations (sent's boundaries) on file.
				pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", "sents");
				List<Triple<String, Integer, Integer>> sents = ssplit(paragraph);
				List<AnnotationI> sentsAnnotations = getSentsAnnotations();
				writeObjects(sentsAnnotations, pathname);
				
				for (int i =  0; i < sents.size(); i++) { 
					Triple<String, Integer, Integer> sent = sents.get(i);

					System.out.println("(II): sent: " + sent);
					
					// Save the P-o-S tags annotations in the sentence.
					pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", "sents", String.valueOf(i), "pos");
					List<Quadruple<String, String, Integer, Integer>> pos = ptag(sent.first());
					List<AnnotationI> posAnnotations = getPOSAnnotations();
					writeObjects(posAnnotations, pathname);
					

					System.out.println("(II): pos: " + pos);

					// Save the NEs annotations in the sentence.
					pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", "sents", String.valueOf(i), "nes");
					List<Quadruple<String, String, Integer, Integer>> nes = classify(sent.first());
					List<AnnotationI> neAnnotations = getNEAnnotations();
					writeObjects(neAnnotations, pathname);

					System.out.println("(II): nes: " + nes);
					
					// Saving the parse tree on file.
					Tree tree = parse(sent.first());

					System.out.println("(II): tree: " + tree);
					
					pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", "sents", String.valueOf(i), "tree", String.valueOf(tree.hashCode()));
					tree.save(pathname);			
					
					// Saving the shallow parse vector on file.
					Tree vec = shallowParse(sent.first());
					

					System.out.println("(II): vec: " + vec);
					
					pathname = joinPath(outFile, TYPE.getId(), mid, "abstract", "sents", String.valueOf(i), "vec", String.valueOf(vec.hashCode()));
					vec.save(pathname);

				}
				System.out.println();
			}
			System.out.println();			
		}
		
	}
	
	private static List<Quadruple<String, String, Integer, Integer>> ptag(String sent) { 
		assert sent != null;
		
		return ptagger.ptag(sent);
	}
	
	private static List<AnnotationI> getPOSAnnotations()  { return ptagger.getAnnotations(); }
	
	private static Tree parse(String sent) { 
		assert sent != null;
		
		return parser.parse(sent);
	}
	
	private static Tree shallowParse(String sent) {
		assert sent != null;
		
		return vecParser.parse(sent);
	}
	
	private static List<Triple<String, Integer, Integer>> tokenize(String text) {
		assert text != null;
		
		return tokenizer.tokenize(text);
	}
	
	private static List<AnnotationI> getToksAnnotations(String text) {
		assert text != null;
		
		return tokenizer.getAnnotations();
	}
 	
	
	private static List<Quadruple<String, String, Integer, Integer>> classify(String sent) {
		assert sent != null;
		
		return ner.classify(sent);		
	}
	
	private static List<AnnotationI> getNEAnnotations() { return ner.getAnnotations(); }
	
	private static List<Triple<String, Integer, Integer>> ssplit(String text) {
		assert text != null;
		
		return ssplitter.ssplit(text);
	}
	
	private static List<AnnotationI> getSentsAnnotations() {
		return ssplitter.getAnnotations();
	}
	
	private static void writeObjects(List<? extends Object> objects, String outFile) {
		assert objects != null;
		assert outFile != null;
		
		for (int i = 0; i < objects.size(); i++) {
			Object o = objects.get(i);
			String pathname = joinPath(outFile, String.valueOf(i), o.hashCode() + ".dat");
			writeObject(o, pathname);
		}
	}
	
	
	private static void writeObject(Object o, String outFile) {
		assert o != null;
		assert outFile != null;
		
		parentFileMkdirs(outFile);
		FileUtils.writeObject(o, outFile);
	}
	
	private static void writeText(String text, String outFile) { 
		assert text != null;
		assert outFile != null;
		
		parentFileMkdirs(outFile);
		FileUtils.writeText(text, outFile);
	}
	
	private static boolean parentFileMkdirs(String pathname) {
		assert pathname != null;
		
		File parentFile = getParentFile(pathname);
		return parentFile.mkdirs();
	}
	
	private static File getParentFile(String pathname) {
		assert pathname != null;
		
		File file = new File(pathname);
		return file.getParentFile();		
	}
	
	private static String joinPath(String part, String... parts) { 
		assert part != null;
		assert parts != null;
		
		String pathname = part;
		for (String thisPart : parts) { 
			pathname +=  (thisPart.charAt(0) == File.separatorChar) 
					? thisPart 
					: File.separatorChar + thisPart;
		}
		return pathname;
	}
	
	private static String getWikiAbstract(String mid) { 
		assert mid != null;
	
		String paragraph = null;
		try {
			paragraph = TopicsRepository.getWikiAbstract(mid);
		} catch (TopicAPIException e) { 
			logger.warning("Abstract retrieving error: mid \"" + mid + "\".");
		}
		return paragraph;
		
	}
	
	private static Logger logger = Logger.getLogger(Writer.class.getName());
	
	private static SentSplitter ssplitter = SentSplitter.getInstance();
	
	private static NER ner = NER.getInstance();
	
	private static Parser parser = Parser.getInstance();
	
	private static POSTagger ptagger = POSTagger.getInstance();
	
	private static VectorParser vecParser = VectorParser.getInstance();
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	

}
