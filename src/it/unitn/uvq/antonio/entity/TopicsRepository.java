package it.unitn.uvq.antonio.entity;

import it.unitn.uvq.antonio.dbpedia.DbPedia;
import it.unitn.uvq.antonio.freebase.db.EntityI;
import it.unitn.uvq.antonio.freebase.db.FreebaseDB;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPI;
import it.unitn.uvq.antonio.freebase.topic.api.TopicAPIException;

import java.util.List;

import org.json.simple.JSONObject;

/**
 * A class exposing a set of friendly-methods to interact with the Freebase Topic API.
 * A topic in Freebase can represents both a fictional or a real-word entity.
 *  
 * @author Antonio Uva 145683
 *
 */
public class TopicsRepository {
	
	/**
	 * Return a list of entity mids (with at most max elements) having 
	 *  the specified notable type.
	 * The Notable type id the main type of an entity.
	 * 
	 * @param typeId A string holding the selected entity id
	 * @param max An integer holding the max number of entity ids to return
	 * @return An immutable list of entity ids having the specified notableFor type id
	 * @throws NullPointerException if typeId is null
	 * @throws max if max is less than 0
	 */
	public static List<String> getMidsByNotableTypeId(String typeId, int num) {
		return freebase.getEntityIdsByNotableForTypeId(typeId, 0, num);
	}
	
	public static List<String> getMidsByNotableTypeId(String typeId, int offset, int num) { 
		if (typeId == null) throw new NullPointerException("typeId: null");
		if (offset < 0) throw new IllegalArgumentException("offset < 0:" + offset);
		if (num < 0) throw new IllegalArgumentException("num < 0: " + num);
		
		return freebase.getEntityIdsByNotableForTypeId(typeId, offset, num);
	}
	
	/**
	 * Get the wikipedia abstract for the specified topic.
	 * In case an entity does not have a wikiUri, an empty string is returned.
	 * 
	 * @param mid A string holding a topic mid
	 * @return The wikipedia abstract for the topic with the specified mid
	 * @throws TopicAPIException if the Topic API service failes (e.g. No Internet connection)
	 * @throws NullPointerException if mid is null 
	 */
	public static String getWikiAbstract(String mid) throws TopicAPIException {
		if (mid == null) throw new NullPointerException("mid: null");
		
		JSONObject jsonData = TopicAPI.get(mid);
		String wikiUri = TopicsHelper.getWikiUri(jsonData);
		return wikiUri.isEmpty() ? wikiUri : DbPedia.getAbstract(wikiUri); 
	}
	
	/**
	 * Returns the title of the page with this mid.
	 * 
	 * @param mid A string holding the wiki mid
	 * @return The wiki title for this mid 
	 * @throws TopicAPIException An error occurred
	 * @throws NullPointerException if (mid == null)
	 */
	public static String getWikiTitle(String mid) throws TopicAPIException { 
		if (mid == null) throw new NullPointerException("mid: null");
		
		JSONObject jsonData = TopicAPI.get(mid);
		
		String wikiUri = jsonData == null ? null : TopicsHelper.getWikiUri(jsonData);
		String title  = wikiUri == null ? null :
			wikiUri.isEmpty() ? null
				: wikiUri.substring(wikiUri.lastIndexOf('/') + 1);
		return title;
	}
	
	public static void saveEntity(EntityI entity, String filepath) {
		if (entity == null) throw new NullPointerException("entity: null");
		if (filepath == null) throw new NullPointerException("filepath: null");
		
		TopicsHelper.save(entity, filepath);
	}
	
	/**
	 * Return an entity by its mid (machine-identifier)
	 * @param mid A string holding the entity mid to retrieve
	 * @return The Entity with the specified mid
	 * @throws NullPointerException if the mid is null
	 */
	public static EntityI getEntityByMid(final String mid) { 
		if (mid == null) throw new NullPointerException("mid: null");
		
		return freebase.getEntityById(mid);
	}
	
	private static FreebaseDB freebase = new FreebaseDB();

}
