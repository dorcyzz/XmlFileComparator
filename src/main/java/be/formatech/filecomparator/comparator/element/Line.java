package be.formatech.filecomparator.comparator.element;

import be.formatech.filecomparator.utils.ComparatorUtility;
import be.formatech.filecomparator.utils.DateUtility;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.annotation.Inherited;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

/**
 * @version $Id: Line.java 164 2010-07-23 13:31:32Z g80195 $
 * 
 * @author Sebastien Vandamme
 */
public class Line implements Comparable<Line> {

	/** The content of the Line. */
	private final String line;

	/** The content of the Line formatted. */
	private final String formattedLine;

	/**
	 * The occurence of the Line i.e. if there is more than one occurence of
	 * same a line, this field identify the occurence represented.
	 */
	private int occurenceNumber;

	/** The Set identifying the date fields of the Line. */
	private final Set<Integer> dates;

	/** The Set identifying the number fields of the Line. */
	private final Set<Integer> numbers;

	/**
	 * Initializes a newly created Line object.
	 * 
	 * @param line
	 *            the content of the Line
	 * @param separator
	 *            the field separator
	 * @param dates
	 *            the Set identifying the date fields of the Line
	 * @param numbers
	 *            the Set identifying the number fields of the Line
	 * @throws ParseException
	 *             If the Line contains a date that cannot be parsed.
	 */
	public Line(String line, String separator, Set<Integer> dates, Set<Integer> numbers)
			throws ParseException {
		this(line, separator, 1, dates, numbers);
	}

	/**
	 * Initializes a newly created Line object.<br>
	 * More than a Line can contain exactly the same data.<br>
	 * In that case, each Line has an occurence number. The first created has
	 * the occurence number 1, the second the 2, etc... <br>
	 * By default, a Line as the occurence number 1.
	 * 
	 * @param line
	 *            the content of the Line
	 * @param separator
	 *            the field separator
	 * @param occurence
	 *            the occurence number
	 * @param dates
	 *            the Set identifying the date fields of the Line
	 * @param numbers
	 *            the Set identifying the number fields of the Line
	 * @throws ParseException
	 *             If the Line contains a date that cannot be parsed.
	 */
	public Line(String line, String separator, int occurence, Set<Integer> dates,
			Set<Integer> numbers) throws ParseException {
		super();

		this.line = line;
		this.occurenceNumber = occurence;
		this.dates = dates;
		this.numbers = numbers;
		this.formattedLine = this.formatLine(separator);
	}

	/**
	 * Formats the Line by parsing the dates and numbers fields.
	 * 
	 * @param separator
	 *            the field separator
	 * @return the formatted content of the Line
	 * @throws ParseException
	 *             If the Line contains a date that cannot be parsed.
	 */
	private String formatLine(String separator) throws ParseException {
		final List<String> tokens = ComparatorUtility.tokenize(this.line, separator);
		final StringBuilder builder = new StringBuilder();

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);

			if ((!this.dates.isEmpty() && this.dates.contains(i + 1))
					|| (this.dates.isEmpty() && DateUtility.isDate(token))) {
				token = DateUtils.parseDate(token, DateUtility.getDateFormatsAsPatternArray())
						.toString();
			} else if ((!this.numbers.isEmpty() && this.numbers.contains(i + 1))
					&& (this.numbers.isEmpty() && NumberUtils.isNumber(ComparatorUtility
                    .deleteAllWhitespace(token)))) {
				token = Double.valueOf(ComparatorUtility.deleteAllWhitespace(token)).toString();
			}

			builder.append(token);

			if (i < tokens.size() - 1) {
				builder.append(separator);
			}
		}

		return builder.toString();
	}

	/**
	 * Returns the content of the Line.
	 * 
	 * @return the content of the Line
	 */
	public String getLine() {
		return this.line;
	}

	/**
	 * Returns the formatted content of the Line.
	 * 
	 * @return the formatted content of the Line
	 */
	public String getFormattedLine() {
		return this.formattedLine;
	}

	/**
	 * Returns the occurence number.
	 * 
	 * @return the occurence number
	 */
	public int getOccurenceNumber() {
		return this.occurenceNumber;
	}

	/**
	 * Sets the occurence number.
	 * 
	 * @param o
	 *            the occurence number
	 */
	public void setOccurenceNumber(int o) {
		this.occurenceNumber = o;
	}

	/**
	 * {@link Inherited}
	 */
	@Override
	public int compareTo(Line l) {
		if (this == l) {
			return 0;
		}

		if (this.formattedLine.equals(l.getFormattedLine())
				&& (this.occurenceNumber == l.getOccurenceNumber())) {
			return 0;
		}

		int comparison = this.line.compareTo(l.getLine());
		if (comparison != 0) {
			return comparison;
		}

		comparison = this.formattedLine.compareTo(l.getFormattedLine());
		if (comparison != 0) {
			return comparison;
		}

		if (this.occurenceNumber > l.getOccurenceNumber()) {
			return 1;
		}
		if (this.occurenceNumber < l.getOccurenceNumber()) {
			return -1;
		}

		return 0;
	}

	/**
	 * {@link Inherited}
	 */
	@Override
	public boolean equals(Object l) {
		if (this == l) {
			return true;
		}

		if (!(l instanceof Line)) {
			return false;
		}

		final Line that = (Line) l;
		return ((this.formattedLine.equals(that.getFormattedLine())) && (this.occurenceNumber == that
				.getOccurenceNumber()));
	}

	/**
	 * {@link Inherited}
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + this.formattedLine.hashCode();

		return (37 * result + this.occurenceNumber);
	}

	/**
	 * {@link Inherited}
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		builder.append(this.line);
		builder.append(this.formattedLine);
		builder.append(this.occurenceNumber);
		builder.append(this.dates);
		builder.append(this.numbers);

		return builder.toString();
	}
}