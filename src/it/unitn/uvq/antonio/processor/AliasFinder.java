package it.unitn.uvq.antonio.processor;

import it.unitn.uvq.antonio.file.FileUtils;
import it.unitn.uvq.antonio.nlp.tokenizer.Tokenizer;
import it.unitn.uvq.antonio.util.tuple.Pair;
import it.unitn.uvq.antonio.util.tuple.SimplePair;
import it.unitn.uvq.antonio.util.tuple.Triple;

import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Find the name used in the wiki-abstract to refer to the entity.
 * Usually a subpart of the entity naem is used in a wiki article in order to refer 
 *  to the main entity.
 * e.g.:
 *  · John Winston Ono Lennon appears as "Lennon";
 *  · Britney Jean Spears appears as "Britney";
 *  · Albert Green appears as "Green";
 *  · ...
 *  
 *  This class does not take in consideration acronyms (not yet :)).
 *  
 * 
 * @author antonio Antonio Uva 145683
 *
 */
public class AliasFinder {
	
	public AliasInfo compute() {
		assert name != null;
		assert text != null;
		
		List<Triple<String, Integer, Integer>> tokensPos = Tokenizer.getInstance().tokenize(text);
		
		Map<String, Integer> m = new HashMap<>();
		
		List<String> tokens = new ArrayList<>();
		List<String> buffer = new ArrayList<>();
		
		/* Get the list of tokens and their positions. */
		for (Triple<String, Integer, Integer> tokenPos : tokensPos) {
			tokens.add(tokenPos.first());
		}
		
		int start = 0;
		for (int i = 0; i < tokens.size(); i++) {
			String currToken = tokens.get(i);
			
			int j = i;
			
			// System.out.format("isUpperCase(%s)? %s%n", currToken, isUpperCase(currToken));
			
			/* Checks if current token matches the name's begin or 
			 * it matches the name's end. */
			if (buffer.isEmpty() && 
			   (name.startsWith(currToken) || name.endsWith(currToken))) {
				// System.out.println("start collecting back name tokens...");
				for (;
					 j >= 0 && isUpperCase(tokens.get(j));
					 j--) {
					// System.out.format("(1) token: %s <%s, %s>%n", tokens.get(j), tokenPos.second(), tokenPos.third(), tokensPos);
					buffer.add(0, tokens.get(j));
					start = tokensPos.get(j).second();
				}
			} else if (!buffer.isEmpty()) {
				if (isUpperCase(currToken)) { 
					buffer.add(currToken);
					
					// System.out.format("(2) token: %s <%s, %s>%n", currToken, tokenPos.second(), tokenPos.third());
				} else {
					int end = tokensPos.get(i - 1).third();
					// System.out.format("end: \"%s\" <%s>%n", currToken, tokensPos.get(i - 1).third());
					String alias = text.substring(start, end);
				
					// System.out.println("(3) buffer: " + buffer + ", name: \"" + name + "\", start: " + start + ", end: " + end);
				
					int count = m.containsKey(alias) ? m.get(alias) : 0;
					m.put(alias, count + 1);
					buffer.clear();
				}
			}
		}
		
		// System.out.println(m.keySet());
		
		
		// list of <name, count> pairs sorted by value in asc order
		List<Entry<String, Integer>> entries = sort(new ArrayList<>(m.entrySet()));
		System.out.println(entries);
		
		return new AliasInfo(m, entries);
		
		//return new AliasInfo(topEntry.getKey(), scoreAsFraction, score);
	}
	
	private List<Entry<String, Integer>> sort(List<Entry<String, Integer>> entries) { 
		assert entries != null;
		
		List<Entry<String, Integer>> sorted = new ArrayList<>(entries);
		Collections.sort(sorted, sortEntryByAscValue);
		return sorted;
	}
	
	public AliasFinder setName(String name) {
		if (name == null) throw new NullPointerException("name is null");
		
		this.name = name;
		return this;
	}
	
	public AliasFinder setText(String text) {
		if (text == null) throw new NullPointerException("text is null");
		
		this.text = text;
		return this;
	}
	
	private boolean isUpperCase(String str) { 
		assert str != null;
		
		return Character.isUpperCase(str.charAt(0));
	}
	
	public static void main(String[] args) { 
		test("John Lennon");
		test("Yoko Ono");
		test("Al Green");
		test("Britney Spears");
		test("Silvio Berlusconi");
		test("Lana Del Rey");
	}
	
	static class AliasInfo {
		
		private AliasInfo(Map<String, Integer> candidatesMap, List<Entry<String, Integer>> sortedCandidates) {
			assert candidatesMap != null;
			assert sortedCandidates != null;
			
			int totCount = 0;
			for (Entry<String, Integer> candidate : sortedCandidates) {
				totCount += candidate.getValue();
			}
			this.totCount = totCount;
			this.candidates = sortedCandidates;
			this.candidatesMap = candidatesMap;
		}
		
		public String getTopCandidate() { 
			return candidates.get(0).getKey();
		}
		
		public List<String> getCandidates() {
			List<String> names = new ArrayList<>();			
			for (Entry<String, Integer> candidate : candidates) {  names.add(candidate.getKey()); }
			return names;
		}
		
		public double getScore(String name) {
			Pair<Integer, Integer> frac = getScoreAsFraction(name);
			
			return (double) frac.first() / frac.second();
		}
	
		public Pair<Integer, Integer> getScoreAsFraction(String name) { 
			assert name != null;
			
			return new SimplePair<Integer, Integer>(candidatesMap.get(name), totCount);
		}
		
		private final int totCount;
		
		private final List<Entry<String, Integer>> candidates;
		
		private final Map<String, Integer> candidatesMap;
		
	}
	
	private static void test(String name) { 
		@SuppressWarnings("deprecation")
		String path = "/home/antonio/workspace/abstracts/" + name.charAt(0) + "/" + URLEncoder.encode(name.replaceAll(" ", "_"));
		String text = FileUtils.readText(path);
		
		AliasFinder finder = new AliasFinder();
		finder.setName(name);
		finder.setText(text);
		
		AliasInfo info = finder.compute();
		String alias = info.getTopCandidate();
		double score = info.getScore(alias);
		Pair<Integer, Integer> scoreAsFrac = info.getScoreAsFraction(alias);
		System.out.format("name: \"%s\", alias: \"%s\", score: %.2f (%s/%s)%n", name, info.getTopCandidate(), score, scoreAsFrac.first(), scoreAsFrac.second());		
 	}
	
	/* Sort <String, Integer> entries by ascending order. */
	private static Comparator<Entry<String, Integer>> sortEntryByAscValue = new Comparator<Entry<String, Integer>>() {
		
		@Override
		public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
			int c = o2.getValue().compareTo(o1.getValue()); 
			// if two keys have the same count, take the one with the smallest one. 
			return c == 0 ? o2.getKey().length() - o1.getKey().length() : c;
		}
	}; 
	
	private String name;
	
	private String text;	 

}
