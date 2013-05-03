package it.unitn.uvq.antonio.util.tuple;

import java.util.Arrays;
import java.util.List;

/**
 * A simple Triple impl class.
 * 
 * @author Antonio Uva 145683
 *
 * @param <A> The type of the first element
 * @param <B> The type of the second element
 * @param <C> The type of the third element
 */
public class SimpleTriple<A, B, C> implements Triple<A, B, C>{
	
	/**
	 * Constructs a new SimpleTriple object.
	 * 
	 * @param first The first element of the pair
	 * @param second The second element of the pair
	 * @param third The third element of the pair
	 */
	public SimpleTriple(final A first, final B second, final C third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public static <A, B, C> SimpleTriple<A, B, C> newInstance(final A first, final B second, final C third) {
		return new SimpleTriple<A, B, C>(first, second, third);
	}

	@Override
	public A first() { return first; }

	@Override
	public B second() { return second; }

	@Override
	public C third() { return third; }

	@Override
	public Object get(int i) { 
		return elems().get(i);
	}
	
	@Override
	public List<Object> elems() { 
		return Arrays.asList(first, second, third); 
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
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
		SimpleTriple other = (SimpleTriple) obj;
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
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleTriple(" + first + ", " + second + ", " + third + ")";
	}
	
	private final A first;
	
	private final B second;
	
	private final C third;
	
}
