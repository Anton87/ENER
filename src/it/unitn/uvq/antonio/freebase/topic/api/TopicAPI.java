package it.unitn.uvq.antonio.freebase.topic.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TopicAPI {
	
	public static JSONObject get(String topicId) throws TopicAPIException {
		if (topicId == null) throw new NullPointerException("topicId: null");
		
		return getInstance().readJSON(topicId);
	}
	
	private static TopicAPI getInstance() {
		if (instance == null) { 
			instance = new TopicAPI();
		}
		return instance;
	}
	
	private TopicAPI() {
		this.apiKey = readApiKey(API_KEY_FILE);
	}
	
	public JSONObject readJSON(String topicId) throws TopicAPIException {
		if (topicId == null) throw new NullPointerException("topicId: null");
		
		JSONObject jsonData = null;
		String url = SERVICE_URL + topicId + "?key=" + apiKey;
		try {
			String data = readData(url);
			jsonData = parse(data);
		} catch (ParseException e) { 
			throw new TopicAPIException("Parse error", e);
		}  catch (IOException e) {
			throw new TopicAPIException("I/O error", e);
		}
		return jsonData;
	}
	
	private JSONObject parse(String data) throws ParseException { 
		assert data != null;
		
		return (JSONObject) jparser.parse(data);		
	}
	
	public String readData(String url) throws IOException {
		assert url != null;
		
		URLConnection urlConn = urlopen(url);
		return read(urlConn);		
	}
	
	private URLConnection urlopen(String urlName) throws MalformedURLException, IOException {
		assert urlName != null;
		
		URL url = new URL(urlName);
		return url.openConnection();		
	}
	
	private String read(URLConnection urlConn) throws IOException {
		assert urlConn != null;
		
		StringBuilder builder = new StringBuilder();		
		BufferedReader in =
				new BufferedReader(
						new InputStreamReader(
							urlConn.getInputStream()));
		String line = null;
		while ((line = in.readLine()) != null) {
			builder.append(line);
		}
		in.close();
		return builder.toString();		
	}
	
	private String readApiKey(String filepath) {
		assert filepath != null;
		
		String apiKey = null;
		BufferedReader in = null;				
		try {
			in = new BufferedReader(
					new FileReader(API_KEY_FILE));
			apiKey = in.readLine();
		} catch (FileNotFoundException e) {
			logger.warning("File \"" + API_KEY_FILE + "\" not found.");
		} catch (IOException e) {
				 logger.warning("API KEY reading error.");
		} finally {
			try {
				in.close();
			} catch (IOException e) { 
				logger.warning("I/O close stream error.");
			}
		}
		return apiKey != null ? apiKey : "";		
	}
		
	private final static String SERVICE_URL = "https://www.googleapis.com/freebase/v1/topic";
	
	private final static String API_KEY_FILE = "googleapis/freebase_api_key";
	
	private static TopicAPI instance;
	
	private static JSONParser jparser = new JSONParser();
	
	private final String apiKey;
	
	private final Logger logger = Logger.getLogger(getClass().getName());

}
