package it.unitn.uvq.antonio.nlp.annotation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import it.unitn.uvq.antonio.nlp.ner.NamedEntityType;
import it.unitn.uvq.antonio.util.IntRange;

public class NeAnnotation implements NeAnnotationI, Externalizable {
	
	public NeAnnotation() { }
	
	public NeAnnotation(String text, NamedEntityType type, IntRange span) {
		if (text == null) throw new NullPointerException("text: null");
		if (type == null) throw new NullPointerException("type: null");
		if (span == null) throw new NullPointerException("span: null");
		init(text, type, span);
	}
	
	public NeAnnotation(String text, NamedEntityType type, int start, int end) {
		this(text, type, new IntRange(start, end));
	}
	
	private void init(String text, NamedEntityType type, IntRange span) { 
		assert text != null;
		assert type != null;
		assert span != null;
		
		this.text = text;
		this.type = type;
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
	public NamedEntityType type() { return type; }
	
	
	@Override
	public String toString() {
		return "NeAnnotation(text=\"" + text + "\", type=" + type + ", start=" + span.start() + ", end=" + span.end() + ")";
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(text);
		out.writeObject(type);
		out.writeObject(span);		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		text = (String) in.readObject();
		type = (NamedEntityType) in.readObject();
		span = (IntRange) in.readObject();
	}
	
	private String text;
	
	private IntRange span;
	
	private NamedEntityType type;

}
