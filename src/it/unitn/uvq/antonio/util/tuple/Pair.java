package it.unitn.uvq.antonio.util.tuple;

/**
 * A simple Pair type.
 * 
 * @author Antonio Uva 145683
 *
 * @param <A> The type of the first element
 * @param <B> The type of the second element
 */
public interface Pair<A, B> extends Tuple {
	
	/**
	 * Returns the first element of the tuple
	 * 
	 * @return The first element of the tuple
	 */
	public A first();
	
	/**
	 * Returns the second element of the tuple.
	 * 
	 * @return The second element of the tuple
	 */
	public B second();

}
