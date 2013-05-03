package it.unitn.uvq.antonio.util.tuple;

/**
 * A simple Quadruple type.
 * 
 * @author Antonio Uva 145683
 *
 * @param <A> The type of the first tuple's element
 * @param <B> The type of the second tuple's element
 * @param <C> The type of the third tuple's element
 * @param <D> The type of the fourth tuple's element
 */
public interface Quadruple<A, B, C, D> extends Tuple {
	
	/**
	 * Returns the first element of the tuple.
	 * 
	 * @return The first element of the tuple having type A
	 */
	public A first();
	
	/**
	 * Returns the second element of the tuple
	 * 
	 * @return The second element of the tuple having type B
	 */
	public B second();
	
	/**
	 * Returns the third element of the tuple.
	 * 
	 * @return The third element of the tuple having type C
	 */
	public C third();
	
	
	/**
	 * Returns the fourth element of the tuple.
	 * 
	 * @return The fourth element of the tuple having type D
	 */
	public D fourth();	

}
