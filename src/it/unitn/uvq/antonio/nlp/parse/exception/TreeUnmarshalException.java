package it.unitn.uvq.antonio.nlp.parse.exception;

public class TreeUnmarshalException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TreeUnmarshalException(final String msg) {
		super(msg);
	}
	
	public TreeUnmarshalException(final String msg, final Exception e) {
		super(msg, e);
	}	
	
}
