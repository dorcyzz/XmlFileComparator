package com.fortis.almt.filecomparator.java.comparator;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.fortis.almt.filecomparator.java.comparator.element.Line;
import com.fortis.almt.filecomparator.java.utils.ComparatorUtility;

/**
 * Compares two files line by line, based on a separator that separates the
 * different fields of each line.<br>
 * It take care of the order of the lines i.e. if line 5 of the first file is
 * the same as line 154 of the second file, it considers that it doesn't found
 * line 5 in the second file.<br>
 * For each field, it checks if it is a number or a date and, if it is the case,
 * it compares the field as a number or as a date.<br>
 * If not, it compares the field as a string.<br>
 * 
 * @version $Id$
 * 
 * @author Sebastien Vandamme
 * 
 * @see Comparator, FileComparator
 */
public class OrderedFileComparator extends FileComparator {
	private static final long serialVersionUID = -971211935842924131L;

	private static final Logger LOGGER = Logger.getLogger(OrderedFileComparator.class);

	/**
	 * Initializes a newly created OrderedFileComparator object.
	 * 
	 * @param separator
	 *            the field separator
	 * @param d
	 *            the Set identifying the date fields of the Lines
	 * @param n
	 *            the Set identifying the number fields of the Lines
	 */
	public OrderedFileComparator(String separator, Set<Integer> d, Set<Integer> n) {
		super(separator, d, n);
	}

	/**
	 * Checks the Lines one by one.<br>
	 * If the Lines at the same place in the two files doesn't exactly match, it
	 * compares the Lines field by field.
	 * 
	 * @return a String containing all the difference and error messages
	 */
	@Override
	String checkFiles() {
		final StringBuilder result = new StringBuilder();

		LOGGER.info("Comparing files with order activated");

		for (Entry<Integer, Line> line1 : this.getFirstFileLinesAsMap().entrySet()) {
			if (this.isCancelled()) {
				LOGGER.info("Comparison cancelled by the user");
				break;
			}

			final String value1 = line1.getValue().getFormattedLine();
			final int numberOfDifferences = this.getNumberOfDifferences(result);

			if (numberOfDifferences > 1000) {
				LOGGER.info("There is more than 1000 differences");
				result.append("There is more than 1000 differences. Only the first ");
				result.append(numberOfDifferences);
				result.append(" are displayed. \n");
				break;
			}

			final Line line = this.getSecondFileLinesAsMap().get(line1.getKey());

			if (line == null) {
				result.append("Line ");
				result.append(line1.getKey());
				result.append(" : Not present in file 2\n");

				continue;
			}

			final String value2 = line.getFormattedLine();

			if (!value1.equals(value2)) {
				final List<String> tokens1 = ComparatorUtility
						.tokenize(value1, this.getSeparator());
				final List<String> tokens2 = ComparatorUtility
						.tokenize(value2, this.getSeparator());

				result.append(this.compareLineFieldByField(line1.getKey(), tokens1, tokens2));
			}
		}

		return result.toString();
	}
}