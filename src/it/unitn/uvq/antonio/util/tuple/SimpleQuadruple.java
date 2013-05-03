package it.unitn.uvq.antonio.util.tuple;

import java.util.Arrays;
import java.util.List;

public class SimpleQuadruple<A, B, C, D> implements Quadruple<A, B, C, D> {
	
	/**
	 * Constructs a new SimpleQuadruple object.
	 * 
	 * @param first The first quadruple's element
	 * @param second The second quadruple's element
	 * @param third The third quadruple's element
	 * @param fourth The fourth quadruple's element
	 */
	public SimpleQuadruple(final A first, final B second, final C third, final D fourth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	@Override
	public Object get(int i) {
		return elems().get(i);
	}

	@Override
	public List<Object> elems() {
		return Arrays.asList(first, second, third, fourth);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		SimpleQuadruple other = (SimpleQuadruple) obj;
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
		return "SimpleQuadruple(" + first + ", " + second + ", " + third + ", " + fourth + ")";
	}
	
	private final A first;
	
	private final B second;
	
	private final C third;
	
	private final D fourth;

}
