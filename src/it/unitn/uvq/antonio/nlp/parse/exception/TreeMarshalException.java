package it.unitn.uvq.antonio.nlp.parse.exception;

public class TreeMarshalException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TreeMarshalException(final String msg) {
		super(msg);
	}
	
	public TreeMarshalException(final String msg, final Exception e) {
		super(msg, e);
	}

}
