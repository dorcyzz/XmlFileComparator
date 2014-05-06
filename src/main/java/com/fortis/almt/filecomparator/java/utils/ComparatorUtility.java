package com.fortis.almt.filecomparator.java.utils;

import com.fortis.almt.filecomparator.java.comparator.element.Line;
import org.apache.commons.collections15.bidimap.TreeBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Utilities used for the comparison of files.
 * 
 * @version $Id: ComparatorUtility.java 163 2010-07-23 08:25:56Z g80195 $
 * 
 * @author Sebastien Vandamme
 */
public final class ComparatorUtility {
	private static final Logger LOGGER = Logger.getLogger(ComparatorUtility.class);

	/** The flag indicating if the parsing of files has been cancelled. */
	private static boolean cancel;

	/**
	 * ComparatorUtility should not normally be instantiated.
	 */
	private ComparatorUtility() {

	}

	/**
	 * Deletes all the white spaces of a String, included the ASCII 160.
	 * 
	 * @param str
	 *            the String from which delete the white spaces
	 * 
	 * @return a String without white spaces
	 */
	public static String deleteAllWhitespace(String str) {
		return StringUtils.deleteWhitespace(StringUtils.remove(str, ((char) 160)));
	}

	/**
	 * Tokenizes a String, separator string specified.
	 * 
	 * @param elementToTokenize
	 *            the String to tokenize
	 * @param separator
	 *            the field separator
	 * 
	 * @return a List of parsed Strings
	 */
	public static List<String> tokenize(String elementToTokenize, String separator) {
		List<String> tokens = new ArrayList<String>();

		if (elementToTokenize != null && separator != null) {
			final String[] st = StringUtils.splitByWholeSeparatorPreserveAllTokens(
					elementToTokenize, separator);

			tokens = Arrays.asList(st);
		}

		return tokens;
	}

	/**
	 * Parses a file and put each line of the file as a Line in a map.
	 * 
	 * @param filename
	 *            the path of the file to parse
	 * @param separator
	 *            the field separator
	 * @param dates
	 *            the Set identifying the date fields of the file
	 * @param numbers
	 *            the Set identifying the number fields of the file
	 * 
	 * @return the Lines of the file contained in a Map
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Map<Integer, Line> parseFile(final String filename, final String separator,
			final Set<Integer> dates, final Set<Integer> numbers) throws IOException,
			ParseException {
		LOGGER.info("Parsing file " + filename);
		final Map<Integer, Line> result = new TreeBidiMap<Integer, Line>();
		setCancel(false);

		FileReader in = null;
		BufferedReader br = null;

		try {
			if (!StringUtils.isEmpty(filename) && !StringUtils.isBlank(filename)
					&& !StringUtils.isEmpty(separator)) {
				in = new FileReader(filename);
				br = new BufferedReader(in);
				String line;
				int lineNumber = 1;

				while ((line = br.readLine()) != null) {
					if (isCancelled()) {
						LOGGER.info("Parsing was cancelled");
						return new TreeBidiMap<Integer, Line>();
					}

					final Line value = new Line(line, separator, dates, numbers);

					while (result.containsValue(value)) {
						if (isCancelled()) {
							LOGGER.info("Parsing was cancelled");
							return new TreeBidiMap<Integer, Line>();
						}

						value.setOccurenceNumber(value.getOccurenceNumber() + 1);
					}

					result.put(lineNumber++, value);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}

			if (br != null) {
				br.close();
			}
		}

		return result;
	}

	/**
	 * Returns the first relevant line of a file as a List of parsed String.
	 * 
	 * @param filename
	 *            the name of the file from which parse the first relevant line
	 * @param separator
	 *            the field separator
	 * 
	 * @return the first relevant line as a List of parsed String
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static List<String> getFirstRealLineTokenized(String filename, String separator)
			throws IOException {
		FileReader in = null;
		BufferedReader br = null;

		try {
			if (!StringUtils.isEmpty(filename) && !StringUtils.isBlank(filename)
					&& !StringUtils.isEmpty(separator)) {
				in = new FileReader(filename);
				br = new BufferedReader(in);
				String line;

				while ((line = br.readLine()) != null) {
					if (line.contains(separator)) {
						return ComparatorUtility.tokenize(line, separator);
					}
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}

			if (br != null) {
				br.close();
			}
		}

		return new ArrayList<String>();
	}

	/**
	 * Checks if both Strings are parseable as Dates.
	 * 
	 * @param token1
	 *            first String
	 * @param token2
	 *            second String
	 * 
	 * @return true if both Strings are parseable as Date, false otherwise
	 */
	public static boolean bothTokenDate(String token1, String token2) {
		return DateUtility.isDate(token1) && DateUtility.isDate(token2);
	}

	/**
	 * Checks if both Strings are parseable as Numbers.
	 * 
	 * @param token1
	 *            first String
	 * @param token2
	 *            second String
	 * 
	 * @return true if both Strings are parseable as Number, false otherwise
	 */
	public static boolean bothTokenNumber(String token1, String token2) {
		return NumberUtils.isNumber(ComparatorUtility.deleteAllWhitespace(token1))
				&& NumberUtils.isNumber(ComparatorUtility.deleteAllWhitespace(token2));
	}

	/**
	 * Checks if two Strings are equals as double.
	 * 
	 * @param token1
	 *            first String
	 * @param token2
	 *            second String
	 * 
	 * @return true if both Strings are parseable as Number and are equals,
	 *         false otherwise
	 */
	public static boolean numbersAreEqual(String token1, String token2) {
		if (token1 == null || token2 == null) {
			return false;
		}

		try {
			final double number1 = Double
					.parseDouble(ComparatorUtility.deleteAllWhitespace(token1));
			final double number2 = Double
					.parseDouble(ComparatorUtility.deleteAllWhitespace(token2));

			if (number1 != number2) {
				return false;
			}
		} catch (NumberFormatException e) {
			LOGGER.debug(token1 + " or/and " + token2 + " is not a parseable number");
			return false;
		}

		return true;
	}

	/**
	 * Checks if two Strings are equals as Date.
	 * 
	 * @param token1
	 *            first String
	 * @param token2
	 *            second String
	 * 
	 * @return true if both Strings are parseable as Date and are equals, false
	 *         otherwise
	 */
	public static boolean datesAreEqual(Object token1, Object token2) {
		if (token1 == null || token2 == null) {
			return false;
		}

		try {
			final Date date1 = DateUtils.parseDate(token1.toString(), DateUtility
                    .getDateFormatsAsPatternArray());
			final Date date2 = DateUtils.parseDate(token2.toString(), DateUtility
					.getDateFormatsAsPatternArray());

			if (!date1.equals(date2)) {
				return false;
			}
		} catch (ParseException e) {
			LOGGER.debug(token1 + " or/and " + token2 + " is not a parseable date");
			return false;
		}

		return true;
	}

	/**
	 * Checks if the parsing has been cancelled.
	 * 
	 * @return true if the parsing has been cancelled, false otherwise
	 * 
	 * @see ComparatorUtility.parseFile
	 */
	public static synchronized boolean isCancelled() {
		return cancel;
	}

	/**
	 * Cancels the parsing.
	 * 
	 * @see ComparatorUtility.parseFile
	 */
	public static synchronized void setCancel(boolean cancel) {
		ComparatorUtility.cancel = cancel;
	}
}