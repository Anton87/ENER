package it.unitn.uvq.antonio.util.tuple;

import java.util.List;

/**
 * A simple tuple type.
 * 
 * @author Antonio Uva 145683
 *
 */
public interface Tuple {
	
	/**
	 * Returns the element at position i.
	 * 
	 * @param i An integer holding the position of the retrieved element
	 * @return The element at the position i
	 */
	public Object get(int i);

	/**
	 * Returns all the elements of the tuple.
	 * 
	 * @return A list holding all the tuple's elements
	 */
	public List<Object> elems();

}
