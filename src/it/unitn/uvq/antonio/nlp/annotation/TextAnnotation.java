package it.unitn.uvq.antonio.nlp.annotation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import it.unitn.uvq.antonio.util.IntRange;

public class TextAnnotation implements TextAnnotationI, Externalizable {
	
	public TextAnnotation() { }
	
	public TextAnnotation(String text, IntRange span) {
		if (text == null) throw new NullPointerException("text: null");
		if (span == null) throw new NullPointerException("span: null");		
		init(text, span);
	}
	
	public TextAnnotation(String text, int start, int end) {
		if (text == null) throw new NullPointerException("text: null");			
		IntRange span = new IntRange(start, end);
		init(text, span);
	}
	
	private void init(String text, IntRange span) {
		assert text != null;
		assert span != null;
		
		this.text = text;
		this.span = span;
	}
	
	@Override
	public String text() { return text; }
	
	@Override
	public IntRange span() { return span; }
	
	@Override
	public int start() { return span.start(); }
	
	@Override
	public int end() { return span.end(); }
	
	@Override
	public String toString() {
		return "TextAnnotation(text=\"" + text + "\", start=" + start() + ", end=" + end() + ")";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(text);
		out.writeObject(span);		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		text = (String) in.readObject();
		span = (IntRange) in.readObject();		
	}

	private String text;
	
	private IntRange span;

}
