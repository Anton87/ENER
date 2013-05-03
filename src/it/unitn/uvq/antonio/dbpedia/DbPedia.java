package it.unitn.uvq.antonio.dbpedia;

import it.unitn.uvq.antonio.file.FileUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/*
 * Retrieve the wikipedia page abstracts parsed by the dbPedia service
 *  by their wiki url.
 * 
 * @author Antonio Uva id:145683
 * *
 */
public class DbPedia {
	
	/**
	 * Return the page abstract for the specified wiki uri.
	 *  
	 * @param wikiUri A string holding the wiki uri to search for abstract
	 * @return A string holding the page abstract for the wiki uri
	 */
	public static String getAbstract(String wikiUri) {
		if (wikiUri == null) throw new NullPointerException("wikiUri: null");
		
		String wikiPageFilepath = getWikiPageFilepath(wikiUri);
		return existsFile(wikiPageFilepath) ? readText(wikiPageFilepath) : "";		
	}
	
	private static String getWikiPageFilepath(String wikiUri) {
		assert wikiUri != null;
		
		String pagename = null;
		pagename = wikiUri.substring(WIKIPEDIA_SERVICE_URL.length());
		try {
			pagename = URLDecoder.decode(pagename, "UTF-8");
		} catch (UnsupportedEncodingException e) { 
			// wikipedia pages are in utf-8.
		}
		pagename = pagename.replace("/", "%2F");
		
		String filepath = "";
		filepath += ABSTRACTS_DIRPATH + File.separator;
		filepath += pagename.charAt(0) + File.separator;
		filepath += pagename;
		return filepath;		
	}
	
	private static String readText(String filepath) { 
		assert filepath != null; 
		
		return FileUtils.readText(filepath);
	}
	
	private static boolean existsFile(String filepath) {
		assert filepath != null;
		
		return new File(filepath).exists();
	}
	
	private final static String WIKIPEDIA_SERVICE_URL = "http://en.wikipedia.org/wiki/";
	
	private final static String ABSTRACTS_DIRPATH = "/home/antonio/workspace/abstracts";

}
