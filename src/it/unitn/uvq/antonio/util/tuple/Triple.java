package it.unitn.uvq.antonio.util.tuple;

/**
 * A simple triple type.
 * 
 * @author Antonio Uva 145683
 *
 * @param <A> The type of the first element
 * @param <B> The type of the second element 
 * @param <C> The type of the third element 
 */
public interface Triple<A, B, C> extends Tuple {
	
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
	
	/**
	 * Returns the third element of the tuple.
	 * 
	 * @return The third element of the tuple
	 */
	public C third();
	
}
