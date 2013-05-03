package it.unitn.uvq.antonio.util.tuple;

import java.util.Arrays;
import java.util.List;

public class SimpleQuintuple<A, B, C, D, E> implements Quintuple<A, B, C, D, E> {
	
	public SimpleQuintuple(A first, B second, C third, D fourth, E fifth) { 
		if (first == null) throw new NullPointerException("first: null");
		if (second == null) throw new NullPointerException("second: null");
		if (third == null) throw new NullPointerException("third: null");
		if (fourth == null) throw new NullPointerException("fourth: null");
		if (fifth == null) throw new NullPointerException("fifth: null");
		
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
		this.fifth = fifth;
	}
	
	public Object get(int i) { 
		if (i < 0) throw new IllegalArgumentException("i < 0: " + i);
		if (i > 5) throw new IllegalArgumentException("i > 5: " + i);
		
		return elems().get(i);
	}
	
	@Override
	public A first() { return first; }
	
	@Override
	public B second() { return second; }
	
	@Override
	public C third() { return third; }
	
	@Override
	public D fourth() { return fourth; }
	
	@Override
	public E fifth() { return fifth; }
	
	public List<Object> elems() {
		return Arrays.asList(first, second, third, fourth, fifth);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fifth == null) ? 0 : fifth.hashCode());
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
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
		@SuppressWarnings("rawtypes")
		SimpleQuintuple other = (SimpleQuintuple) obj;
		if (fifth == null) {
			if (other.fifth != null)
				return false;
		} else if (!fifth.equals(other.fifth))
			return false;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (fourth == null) {
			if (other.fourth != null)
				return false;
		} else if (!fourth.equals(other.fourth))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleQuintuple(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ")";
	}
	
	private A first;
	
	private B second;
	
	private C third;
	
	private D fourth;
	
	private E fifth;

}
