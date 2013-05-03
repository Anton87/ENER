package it.unitn.uvq.antonio.entity;

import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.TypeI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Provides a bunch of help-methods for the TopicsRepository API.
 * 
 * @author Antonio Uva 145683
 *
 */
class TopicsHelper {
	
	static void save(EntityI entity, String filepath) {
		if (entity == null) throw new NullPointerException("entity: null");
		if (filepath == null) throw new NullPointerException("filepath: null");
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(
				new FileWriter(filepath));
			out.write(entity.getId() + '\t');
			out.write(entity.getName() + '\t');
			out.write(entity.getAliases().toString() + '\t');
			out.write(getNotableTypesIDs(entity).toString() + '\t');
			out.write(entity.getNotableFor().getId());	
		} catch (IOException e) {
			System.err.println("I/O error.");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				System.err.println("I/O error.");
			}
		}
	}
		
	private static List<String> getNotableTypesIDs(EntityI entity) {
		List<String> typesIDs = new ArrayList<>();
		for (TypeI type : entity.getNotableTypes()) {
			typesIDs.add(type.getId()); 
		}
		return typesIDs;
	}
	
	/**
	 * Returns the wikipedia uri for a freebase topic. 
	 * An empty string is returned if the object does not have a
	 *  related wikipedia page.
	 *    
	 * @param jobject A JSON object holding the info about an freebase topic
	 * @return A string holding the wiki uri of the topic
	 */
	public static String getWikiUri(JSONObject jobject) { 
		if (jobject == null) throw new NullPointerException("jobject: null");
		
		return findWikiURI(jobject);
	}
	
	/* Retrieve the wikipedia uri from a json object holding all the info about a given freebasetopic. */ 
	private static String findWikiURI(JSONObject jobject) {
		assert jobject != null;
		
		String uri = null;
		JSONObject jo = jobject;
		JSONArray ja = null;
		jo = (JSONObject) jo.get("property");
		if (jo != null) { 
			jo = (JSONObject) jo.get("/common/topic/description");
			if (jo != null) {
				ja = (JSONArray) jo.get("values");
				if (ja != null && !ja.isEmpty()) {
					for (Object o : ja) {
						jo = ((JSONObject) o);						
						jo = (JSONObject) jo.get("citation");
						if (jo != null) { 
							Object p = jo.get("provider");
							if (p != null && ((String)p).equals("Wikipedia")) {
								uri = (String) jo.get("uri");					
							}
						}
					}					
				}
			}
		}
		return uri != null ? uri : "";
	}
		
	//private final static String URI_PATH = "property./common/topic/description.values[0].uri";

}
