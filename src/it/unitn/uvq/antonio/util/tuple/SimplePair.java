package it.unitn.uvq.antonio.util.tuple;

import java.util.Arrays;
import java.util.List;

/**
 * A simple Pair impl class.
 * 
 * @author Antonio Uva 145683
 *
 * @param <A> The type of the first element
 * @param <B> The type of the second element
 */
public class SimplePair<A, B> implements Pair<A, B> {
	
	/**
	 * Constructs a new SimplePair object.
	 * 
	 * @param first The first element of the pair
	 * @param second The second element of the pair
	 */
	public SimplePair(final A first, final B second) {
		this.first = first;
		this.second = second;
	}
	
	public static <A, B> SimplePair<A, B> newInstance(final A first, final B second) {
		return new SimplePair<A, B>(first, second);
	}

	@Override
	public Object get(int i) {
		return elems().get(i);
	}

	@Override
	public List<Object> elems() {
		return Arrays.asList(first, second);
	}

	@Override
	public A first() { return first; }
	
	@Override
	public B second() { return second; }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		SimplePair other = (SimplePair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimplePair(" + first + ", " + second + ")";
	}

	private final A first;
	
	private final B second;

}
