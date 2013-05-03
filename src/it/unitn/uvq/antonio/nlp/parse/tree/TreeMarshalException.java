package it.unitn.uvq.antonio.nlp.parse.tree;

public class TreeMarshalException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TreeMarshalException(String msg) {
		super(msg);
	}
	
	public TreeMarshalException(String msg, Exception e) {
		super(msg, e);
	}

}
