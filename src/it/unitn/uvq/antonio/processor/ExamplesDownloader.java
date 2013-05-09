package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.entity.TopicsRepository;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.FreebaseDB;
import it.unitn.uvq.antonio.freebase.db.NotableType;
import it.unitn.uvq.antonio.freebase.repository.Freebase;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.nlp.parse.Parser;
import it.unitn.uvq.antonio.nlp.parse.tree.Tree;
import it.unitn.uvq.antonio.nlp.parse.VectorParser;
import it.unitn.uvq.antonio.nlp.pos.POSTagger;
import it.unitn.uvq.antonio.nlp.sent.SentSplitter;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Triple;
import it.unitn.uvq.antonio.util.tuple.Tuple;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ExamplesDownloader {
		
	public ExamplesDownloader(String notableTypeId, int examplesPerTime, ExamplesBuilder builder) { 
		if (notableTypeId == null) throw new NullPointerException("notableTypeId: null");
		if (examplesPerTime <= 0) throw new IllegalArgumentException("examplesPerTime <= 0: " + examplesPerTime);
		if (builder == null) throw new NullPointerException("builder: null");
		
		this.notableTypeId = notableTypeId;
		this.examplesPerTime = examplesPerTime;		
		this.builder = builder;		
	}
	
	public void run() {
		
		int offset = 0;
		
		List<String> mids = retrieveMidsByNotableTypeId(notableTypeId, offset, examplesPerTime);
		offset += mids.size();
		
		while (!mids.isEmpty()) {
			String mid = mids.remove(0);
			
			process(mid);
			
			if (mids.isEmpty()) {
				mids = retrieveMidsByNotableTypeId(notableTypeId, offset, examplesPerTime);
				offset += mids.size();
			}
		}
		
	}
	
	private void process(String mid) { 
		assert mid != null;
		
		EntityI entity = freebaseDB.getEntityById(mid);		
		
		String paragraph = getWikiAbstractByMid(mid);
		
		if (paragraph != null && !paragraph.isEmpty()) { 			
			
			
			List<Triple<String, Integer, Integer>> toks = tokenize(paragraph);
			
			List<Triple<String, Integer, Integer>> sents = ssplit(paragraph);
			
			for (Triple<String, Integer, Integer> sent : sents) { 
				
				/*
				System.out.println("(II): MID: \"" + mid + "\"");
				
				System.out.println("(II): Entity: " + entity);
				
				System.out.println("(II): Paragraph: \"" + paragraph + "\"");

				System.out.println("(II): Tokens: " + toks);
				
				System.out.println("(II): Sent: " + sent);
				*/
				
				List<Quadruple<String, String, Integer, Integer>> poss = postag(sent.first());
				// System.out.println("(II): P-o-S: " + poss);
				
				List<Quadruple<String, String, Integer, Integer>> nes = classify(sent.first());
				// System.out.println("(II): NEs: " + nes);
				
				if (!nes.isEmpty()) { 
				
					
					Tree tree = parse(sent.first());					
					// System.out.println("(II): Tree: " + tree);
				
					Tree vec = shallowParse(sent.first());
					// System.out.println("(II): Vec: " + vec);
					
					builder.process(mid, entity, paragraph, toks, sent, poss, nes, tree, vec);
				  
				}
				
				// System.out.println();
				
				
			}
			
		}
		
		
	}
	
	private List<String> retrieveMidsByNotableTypeId(String id, int offset, int examplesNum) { 
		assert id != null;
		assert offset >=  0;
		assert examplesNum >= 0;
		
		List<String> mids = TopicsRepository.getMidsByNotableTypeId(id, offset, examplesNum);
		return new ArrayList<>(mids);
	}
	
	private String getWikiAbstractByMid(String mid) {
		assert mid != null;
		
		String paragraph = null;
		try {
			paragraph = TopicsRepository.getWikiAbstract(mid);
		} catch (TopicAPIException e) { 
			System.out.println("(EE): Abstract: Error retrieving abstract for mid = \"" + mid + "\".");
		}
		return paragraph;		
	}
	
	private List<Triple<String, Integer, Integer>> tokenize(String sent) { 
		assert sent != null;
		
		return tokenizer.tokenize(sent);
	}
	
	private List<Triple<String, Integer, Integer>> ssplit(String text) { 
		assert text != null;
		
		return ssplitter.ssplit(text);
	}
	
	private List<Quadruple<String, String, Integer, Integer>> postag(String sent) { 
		assert sent != null;
		
		return ptagger.tag(sent);
	}
	
	private List<Quadruple<String, String, Integer, Integer>> classify(String sent) { 
		assert sent != null;
		
		return ner.classify(sent);
	}
	
	private Tree parse(String sent) { 
		assert sent != null;
		
		return parser.parse(sent);			
	}
	
	private Tree shallowParse(String sent) { 
		assert sent != null;
		
		return vecParser.parse(sent);
	}
	
	private static FreebaseDB freebaseDB = new FreebaseDB();
	
	private static SentSplitter ssplitter = SentSplitter.getInstance();
	
	private static Tokenizer tokenizer = Tokenizer.getInstance();
	
	private static POSTagger ptagger = POSTagger.getInstance();
	
	private static VectorParser vecParser = VectorParser.getInstance();
	
	private static Parser parser = Parser.getInstance();
	
	private static NER ner = NER.getInstance();
	
	private final ExamplesBuilder builder;
	
	private final int examplesPerTime;
	
	private final String notableTypeId;
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static void main(String[] args) {
		String entityTypeId = "/film/actor";
		String priEntityType = "PERSON";
		int examplesNum = 100;
		String destFile = "/home/antonio/Scrivania";
		
		ExamplesBuilder builder  = new TreeVecExamplesBuilder(priEntityType, entityTypeId, examplesNum, destFile);
		ExamplesDownloader downloader = new ExamplesDownloader(entityTypeId, examplesNum, builder);
		
		downloader.run();		
	}
	
	private static String encode(String str) { 
		assert str != null;
		
		return URLEncoder.encode(str);		
	}

}
