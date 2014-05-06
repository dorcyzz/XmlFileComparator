package com.fortis.almt.filecomparator.java.comparator;

import java.util.Set;

/**
 * A factory for creating Comparator.
 * 
 * @version $Id$
 * 
 * @author Sebastien Vandamme
 * 
 * @see Comparator, FileComparator, OrderedFileComparator
 */
public final class ComparatorFactory {
	private static final ComparatorFactory INSTANCE = new ComparatorFactory();

	/**
	 * ComparatorFactory should not normally be instantiated outside of the
	 * class.
	 */
	private ComparatorFactory() {
		super();
	}

	/**
	 * Returns an instance of the factory.
	 * 
	 * @return an instance of the factory
	 */
	public static ComparatorFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns a Comparator based on the parameters.
	 * 
	 * @param separator
	 *            the field separator
	 * @param d
	 *            the Set identifying the date fields
	 * @param n
	 *            the Set identifying the number fields
	 * @param ordered
	 *            the flag indicating if the Comparator must take care of the
	 *            ordering
	 * 
	 * @return a Comparator
	 */
	public Comparator getComparator(String separator, Set<Integer> d, Set<Integer> n,
			final boolean ordered) {
		if (ordered) {
			return new OrderedFileComparator(separator, d, n);
		}

		return new FileComparator(separator, d, n);
	}
}