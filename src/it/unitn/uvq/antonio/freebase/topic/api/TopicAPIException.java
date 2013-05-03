package it.unitn.uvq.antonio.freebase.topic.api;

public class TopicAPIException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TopicAPIException(String msg) {
		super(msg);
	}
	
	public TopicAPIException(String msg, Exception e) {
		super(msg, e);
	}
	
}
