package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.entity.TopicsRepository;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.FreebaseDB;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.nlp.sent.SentSplitter;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PERExamplesDownloader {
		
	public PERExamplesDownloader(String notableTypeId, int examplesPerTime, ExamplesBuilder builder) { 
		if (notableTypeId == null) throw new NullPointerException("notableTypeId: null");
		if (examplesPerTime <= 0) throw new IllegalArgumentException("examplesPerTime <= 0: " + examplesPerTime);
		if (builder == null) throw new NullPointerException("builder: null");
		
		this.notableTypeId = notableTypeId;
		this.examplesPerTime = examplesPerTime;		
		this.builder = builder;		
	}
	
	public void run() {
		
		running = true;
		
		int offset = 0;
		
		List<String> mids = retrieveMidsByNotableTypeId(notableTypeId, offset, examplesPerTime);
		offset += mids.size();
		
		while (running && !mids.isEmpty()) {
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
			
			List<Triple<String, Integer, Integer>> sents = ssplit(paragraph);
			
			for (Triple<String, Integer, Integer> sent : sents) { 
				
				String newSent= stripBrackets(sent.first())
						.replaceAll("\\s+", " ")
						.replaceAll("\\s(\\p{Punct})", "$1")
						.replaceAll("([!\"#$%&')*+,-/:;?@\\[\\]^_`{|}~])+", "$1")
						.replaceAll("\\s+", " ");						
				
				Triple<String, Integer, Integer> normalizedSent = new SimpleTriple<>(newSent, sent.second(), sent.third()); 
								
				List<Quadruple<String, String, Integer, Integer>> nes = classify(normalizedSent.first());
				
				if (!nes.isEmpty()) { 
				
					try {
						builder.process(mid, entity, paragraph, normalizedSent, nes);
					} catch (OutOfMemoryError e) {
						System.out.print("x");
					} 
				}			
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
	
	private String stripBrackets(String str) {
		assert str != null;
		
		StringBuilder sb = new StringBuilder();
		for (String part : brPattern.split(str, 0)) { 
			sb.append(part);
		}
		return sb.toString();		
	}
	
	private static FreebaseDB freebaseDB = new FreebaseDB();
	
	private static SentSplitter ssplitter = SentSplitter.getInstance();
	
	private static NER ner = NER.getInstance();
	
	private final ExamplesBuilder builder;
	
	private final int examplesPerTime;
	
	private final String notableTypeId;
	
	private final static String brRegex = "\\([^)]*\\)";
	
	private final static Pattern brPattern = Pattern.compile(brRegex);
	
	static boolean running = false;
	
	private static String encode(String str) {
		assert str != null;
		
		return URLEncoder.encode(str);				
	}
	
	public static void main(String[] args) {
		// String entityTypeId = "/education/academic";
		String priEntityType = "ORGANIZATION";
		int examplesNum = 1000;
		//String destFile = "/home/antonio/Scrivania/sshdir_loc/shallow/data";		
		
		/*
		 * Person notable-types.
		 */
		 	String[] notableTypeIDs = new String[] {
		 	"/architecture/architect",
		 	"/education/academic",
		 	"/en/model",
		 	"/en/physician",
		 	"/en/writer",
		 	"/film/actor",
		 	"/government/politician",
		 	"/music/artist",
		 	"/sports/pro_athlete",			
		 	"/visual_art/visual_artist"
		 };
		 

		for (String entityTypeId : notableTypeIDs) {
			String destFile = "/home/antonio/Scrivania/sshdir_loc/tree/ORG/data/" + encode(entityTypeId);
			if (!new File(destFile).isDirectory()) new File(destFile).mkdirs(); 
			PERTreeExamplesBuilder builder  = new PERTreeExamplesBuilder(priEntityType, entityTypeId, examplesNum, destFile);
			PERExamplesDownloader downloader = new PERExamplesDownloader(entityTypeId, examplesNum, builder);
			downloader.run();
		}
	}

}
