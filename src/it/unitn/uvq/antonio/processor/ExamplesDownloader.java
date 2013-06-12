package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.entity.TopicsRepository;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.FreebaseDB;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;
import it.unitn.uvq.antonio.nlp.ner.NER;
import it.unitn.uvq.antonio.nlp.sent.SentSplitter;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.processor.AliasFinder.AliasInfo;
import it.unitn.uvq.antonio.util.tuple.Quadruple;
import it.unitn.uvq.antonio.util.tuple.SimpleTriple;
import it.unitn.uvq.antonio.util.tuple.Triple;
import it.unitn.uvq.antonio.utils.DefaultMap;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

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
			if (mids.isEmpty()) { System.out.println("(EE): Scanned " + offset + " examples."); }
		}
		
	}
	
	
	
	private void process(String mid) { 
		assert mid != null;
		
		EntityI entity = freebaseDB.getEntityById(mid);	
		
		System.out.println("(EE): e: " + entity.getName() + ", " + entity.getAliases());
		
		String paragraph = getWikiAbstractByMid(mid);
		
		if (paragraph != null && !paragraph.isEmpty()) { 
			
			System.out.println("(EE): P: " + paragraph);
			
			String alias = findAlias(entity.getName(), paragraph);
			//System.out.format("(EE): alias(\"%s\"): \"%s\"%n", entity.getName(), alias);
						
			List<Triple<String, Integer, Integer>> sents = ssplit(paragraph);
			
			String firstSent = sents.get(0).first();
			
			List<String> acronyms = findAcronyms(firstSent);
			
			if (!acronyms.isEmpty()) { 
				System.out.println("(EE): Acronyms: " + acronyms);
			}
			
			entity.getAliases().addAll(acronyms);
			
			for (int sentNum = 0; sentNum < sents.size(); sentNum++) {
				Triple<String, Integer, Integer> sent = sents.get(sentNum);
				
				
				String newSent= stripBrackets(sent.first())
						.replaceAll("\\s+", " ")
						.replaceAll("\\s(\\p{Punct})", "$1")
						.replaceAll("([!\"#$%&')*+,-/:;?@\\[\\]^_`{|}~])+", "$1")
						.replaceAll("\\s+", " ");						
				
				Triple<String, Integer, Integer> normalizedSent = new SimpleTriple<>(newSent, sent.second(), sent.third());
				
				String sentStr = normalizedSent.first(); 
								
				List<Quadruple<String, String, Integer, Integer>> nes = classify(normalizedSent.first());
				
				// Checks if the sentence contains the entity name, a alias, the wiki referring name or an acronym.
				boolean valid = sentStr.contains(alias) ||
								sentStr.contains(entity.getName()) ||
								containsAny(sentStr, acronyms) ||
								containsAny(sentStr, entity.getAliases()); 
				
				System.out.println("(EE): #s(" + sentNum + "): " + (valid ? "o" : "x") + ": " + sentStr);
				
				if (valid) {
					try {
						System.out.println("processing sent...");
						builder.process(mid, entity, alias, acronyms, paragraph, normalizedSent, nes);
					} catch (OutOfMemoryError e) {
						System.out.print("*");
					}
				}			
			}	
		}	
	}
	
	/*
	 * Search for alternative names used in wikipage page to refer to
	 * the main subject.
	 * 
	 * @param entityName A string holdin the entity name
	 * @param entityText A string holding the wiki page text
	 * @return A string holding the name used to refer to the within the article
	 */
	private String findAlias(String entityName, String text) {
		//if (builder.namedEntityType.equals("PERSON")) {
		AliasInfo info = new AliasFinder()
			.setName(entityName)
			.setText(text)
			.compute();
		String alias = info.getTopCandidate();
		//System.out.format("(EE): alias(\"%s\"): \"%s\"%n", entityName, alias);
		return alias;
	}
	
	/**
	 * Search for acronyms in the first sentence of a wikipedia page.
	 * 
	 * @param sent A string holding a sentence
	 * @return A new list of the acronyms found
	 */
	private List<String> findAcronyms(String sent) {
		assert sent != null;
		
		List<String> acronyms = new ArrayList<>();
		List<String> tokens = new ArrayList<>();
		for (Triple<String, Integer, Integer> tokenPos : Tokenizer.getInstance().tokenize(sent)) {
			tokens.add(tokenPos.first());
		}		
		
		Iterator<String> it = tokens.iterator();
		while (it.hasNext() && !it.next().equals("("));
		while (it.hasNext()) {
			String token = it.next();
			if (token.equals(")")) break;
			if (isAllUpperCase(token)) { acronyms.add(token); }			
		}
		return acronyms;
	}
	
	private boolean isAllUpperCase(String str) { 
		assert str != null;
		
		int i = 0;
		for (;
			 i < str.length() && Character.isUpperCase(str.charAt(i));
			 i++);	
		return i == str.length();		
	}
	
	
	
	private boolean containsAny(String sent, List<String> strs) {   
		assert sent != null;
		assert strs != null;
		
		for (String str : strs) {
			if (sent.contains(str)) { return true; }
		}
		return false;
	}
	
	private boolean contains(String sent, List<String> strs) {
		assert sent != null;
		assert strs != null;
		
		for (String str : strs) { 
			if (sent.contains(str)) return true;
		}
		return false;
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
		 *
		 *	String[] notableTypeIDs = new String[] {
		 *	"/architecture/architect",
		 *	"/education/academic",
		 *	"/en/model",
		 *	"/en/physician",
		 *	"/en/writer",
		 *	"/film/actor",
		 *	"/government/politician",
		 *	"/music/artist",
		 *	"/sports/pro_athlete",			
		 *	"/visual_art/visual_artist"
		 *	};
		 */
		
		String[] personTypeIDs = new String[] {
			"/music/artist"
		};

		/* 
		 * Organization notable-types
		 */
		 String[] notableTypeIDs = new String[] {
		 	//"/tv/tv_network",
		 	//"/sports/sports_team",
		 	//"/business/business_operation",
		    //"/business/industry",
		 	"/government/political_party",
			//"/military/armed_force",
			//"/aviation/airline",
			//"/education/school",
			//"/government/government_agency",
			//"/automotive/company",
			//"/base/charities/charity",
			//"/organization/non_profit_organization",
			//"/music/record_label",
			//"/sports/sports_league",
			//"/religion/religious_organization",
			//"/music/musical_group",
			//"/medicine/hospital"
		};

		for (String entityTypeId : notableTypeIDs) {
			String destFile = "/home/antonio/Scrivania/sshdir_loc/tree/ORG/data/" + encode(entityTypeId);
			if (!new File(destFile).isDirectory()) new File(destFile).mkdirs(); 
			ExamplesBuilder builder  = new TreeExamplesBuilder(priEntityType, entityTypeId, examplesNum, destFile);
			ExamplesDownloader downloader = new ExamplesDownloader(entityTypeId, examplesNum, builder);
			downloader.run();
		}
	}

}
