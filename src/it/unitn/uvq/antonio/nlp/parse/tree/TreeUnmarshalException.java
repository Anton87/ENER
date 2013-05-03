package it.unitn.uvq.antonio.nlp.parse.tree;

public class TreeUnmarshalException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TreeUnmarshalException(String msg) {
		super(msg);
	}

	public TreeUnmarshalException(String msg, Exception e) {
		super(msg, e);
	}
	
}
