package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.entity.TopicsRepository;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.FreebaseDB;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.nlp.sent.SentSplitter;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

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
			
			
			//List<Triple<String, Integer, Integer>> toks = tokenize(paragraph);
			
			List<Triple<String, Integer, Integer>> sents = ssplit(paragraph);
			
			for (Triple<String, Integer, Integer> sent : sents) { 
				
				/*
				System.out.println("(II): MID: \"" + mid + "\"");
				
				System.out.println("(II): Entity: " + entity);
				
				System.out.println("(II): Paragraph: \"" + paragraph + "\"");

				System.out.println("(II): Tokens: " + toks);
				
				System.out.println("(II): Sent: " + sent);
				*/
				
				//List<Quadruple<String, String, Integer, Integer>> poss = postag(sent.first());
				// System.out.println("(II): P-o-S: " + poss);
				
				List<Quadruple<String, String, Integer, Integer>> nes = classify(sent.first());
				// System.out.println("(II): NEs: " + nes);
				
				if (!nes.isEmpty()) { 
					
					//Tree tree = parse(sent.first());					
					// System.out.println("(II): Tree: " + tree);
				
					//Tree vec = shallowParse(sent.first());
					//// System.out.println("(II): Vec: " + vec);
					
					//builder.process(mid, entity, paragraph, toks, sent, poss, nes, tree, vec);
					builder.process(mid, entity, paragraph, sent, nes);
				  
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
			System.err.println("(EE): Abstract: Error retrieving abstract for mid = \"" + mid + "\".");
		}
		return paragraph;		
	}
	
	private List<Triple<String, Integer, Integer>> ssplit(String text) { 
		assert text != null;
		
		return ssplitter.ssplit(text);
	}
	
	private List<Quadruple<String, String, Integer, Integer>> classify(String sent) { 
		assert sent != null;
		
		return ner.classify(sent);
	}
	
	private static FreebaseDB freebaseDB = new FreebaseDB();
	
	private static SentSplitter ssplitter = SentSplitter.getInstance();
	
	private static NER ner = NER.getInstance();
	
	private final ExamplesBuilder builder;
	
	private final int examplesPerTime;
	
	private final String notableTypeId;
	
	public static void main(String[] args) {
		String entityTypeId = "/architecture/architect";
		String priEntityType = "PERSON";
		int examplesNum = 1000;
		String destFile = "/home/antonio/Scrivania";
		
		ExamplesBuilder builder  = new ShallowPlusVecExamplesBuilder(priEntityType, entityTypeId, examplesNum, destFile);
		ExamplesDownloader downloader = new ExamplesDownloader(entityTypeId, examplesNum, builder);
		
		downloader.run();		
	}

}
