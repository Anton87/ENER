package it.unitn.uvq.antonio.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class IntRange implements Comparable<IntRange>, Externalizable {
	
	public IntRange() { }
	
	public IntRange(int start, int end) {
		if (start > end) throw new IllegalArgumentException("start > end");
		this.start = start;
		this.end = end;
	}
	
	public int start() { return start; }
	
	public int end() { return end; }
	
	public boolean contains(final IntRange o) { 
		if (o == null) throw new IllegalArgumentException("o: null");
		return start <= o.start && end >= o.end;
	}
	
	public boolean overlap(final IntRange o) { 
		if (o == null) throw new NullPointerException("o: null");
		return loverlap(o) || 
			   roverlap(o) || 
			   contains(o) ||
			   o.contains(this);
			   
	}
	
	public boolean roverlap(final IntRange o) { 
		if (o == null) throw new NullPointerException("o: null");
		return start >= o.start && o.end >= end;
	}
	
	public boolean loverlap(final IntRange o) {
		if (o == null) throw new NullPointerException("o: null");
		return start <= o.start && end <= o.end;
	}
	
	private int start;
	
	private int end;

	@Override
	public int compareTo(IntRange o) {
		if (o == null) throw new NullPointerException("o: null");
		return diff(this, o);
	}
	
	int diff(final IntRange r1, final IntRange r2) {
		if (r1 == null) throw new NullPointerException("r1: null");
		if (r2 == null) throw new NullPointerException("r2: null");
		return r1.end - r1.start - (r2.end - r2.start);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntRange other = (IntRange) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "{" + start + ", " + end + "}";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(start);
		out.writeInt(end);		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		start = in.readInt();
		end = in.readInt();
		
		
	}
	
	/*
	public static void main(String[] args) { 
		IntRange r1 = new IntRange(2, 3);
		IntRange r2 = new IntRange(1, 5);
		
		System.out.println(r2 + " contains " + r1 + "? " + r2.contains(r1));
		System.out.println(r1 + " contains " + r2 + "? " + r1.contains(r2));
		
		System.out.println(r2 + " overlap " + r1 + "? " + r2.overlap(r1));
		System.out.println(r1 + " overlap " + r2 + "? " + r1.overlap(r2));
	}
	*/
	
}
