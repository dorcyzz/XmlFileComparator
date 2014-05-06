package be.formatech.filecomparator.comparator;

import be.formatech.filecomparator.comparator.element.Line;
import be.formatech.filecomparator.utils.ComparatorUtility;
import org.apache.commons.collections15.bidimap.TreeBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Compares two files line by line, based on a separator that separates the
 * different fields of each line.<br>
 * It doesn't take care of the order of the lines i.e. if line 5 of the first
 * file is the same as line 154 of the second file, it considers that it found
 * line 5 in the second file, no matter where in the file it found it.<br>
 * For each field, it checks if it is a number or a date and, if it is the case,
 * it compares the field as a number or as a date.<br>
 * If not, it compares the field as a string.<br>
 * 
 * @version $Id: FileComparator.java 165 2010-07-23 13:49:51Z g80195 $
 * 
 * @author Sebastien Vandamme
 * 
 * @see Comparator, OrderedFileComparator
 */
public class FileComparator implements Comparator {
	private static final long serialVersionUID = 6160181523387826802L;

	private static final Logger LOGGER = Logger.getLogger(FileComparator.class);

	/** The Lines of the first file contained in a Map. */
	private Map<Integer, Line> firstFileLinesAsMap = null;

	/** The Lines of the second file contained in a Map. */
	private Map<Integer, Line> secondFileLinesAsMap = null;

	/** The field separator. */
	private final String separator;

	/** The Set identifying the date fields of the Lines. */
	private final Set<Integer> dates;

	/** The Set identifying the number fields of the Lines. */
	private final Set<Integer> numbers;

	/** The flag indicating if the comparison has been cancelled. */
	private boolean cancelled;

	/**
	 * Initializes a newly created FileComparator object.
	 * 
	 * @param separator
	 *            the field separator
	 * @param d
	 *            the Set identifying the date fields of the Lines
	 * @param n
	 *            the Set identifying the number fields of the Lines
	 */
	public FileComparator(String separator, Set<Integer> d, Set<Integer> n) {
		super();
		this.separator = separator;
		this.dates = d;
		this.numbers = n;
	}

	/**
	 * Compares two files line by line, based on a separator that separates the
	 * different fields of each line.<br>
	 * It doesn't take care of the order of the lines i.e. if line 5 of the
	 * first file is the same as line 154 of the second file, it considers that
	 * it found line 5 in the second file, no matter where in the file it found
	 * it.<br>
	 * For each field, it checks if it is a number or a date and, if it is the
	 * case, it compares the field as a number or as a date.<br>
	 * If not, it compares the field as a string.<br>
	 * 
	 * @param filename1
	 *            the path of the first file to compare
	 * @param filename2
	 *            the path of the second file to compare
	 * 
	 * @return a String containing all the difference and error messages
	 */
	public String compare(final String filename1, final String filename2) {
		LOGGER.info("Comparing...");
		this.cancelled = false;
		final StringBuilder result = new StringBuilder();

		if (!StringUtils.isEmpty(filename1) && !StringUtils.isEmpty(filename2)
				&& new File(filename1).exists() && new File(filename2).exists()) {
			// TODO review
			LOGGER.info("Parsing the files to compare");
			final FileParser parser1 = new FileParser(filename1, result);
			final Thread thread1 = new Thread(parser1);
			final FileParser parser2 = new FileParser(filename2, result);
			final Thread thread2 = new Thread(parser2);

			thread1.setPriority(Thread.MIN_PRIORITY);
			thread2.setPriority(Thread.MIN_PRIORITY);
			thread1.start();
			thread2.start();

			while ((thread1.isAlive() || thread2.isAlive())) {
				if (this.isCancelled()) {
					ComparatorUtility.setCancel(true);
					LOGGER.info("Comparison cancelled by the user while parsing the files");
					return "Comparison cancelled by the user";
				}
			}

			this.setFile1LinesAsMap(parser1.getFileLinesAsMap());
			this.setFile2LinesAsMap(parser2.getFileLinesAsMap());

			// if (ordered) {
			// LOGGER.info("Order matter");
			// result.append(this.checkFilesOrdered());
			// } else {
			// LOGGER.info("Order doesn't matter");
			result.append(this.checkFiles());
			// }

			final int numberOfDifferences = this.getNumberOfDifferences(result);

			if (numberOfDifferences == 0) {
				result.append("Files exactly match\n");
			} else {
				result.append("\n");
				result.append(numberOfDifferences);
				result.append(" difference(s) found\n");
			}
		} else {
			result.append("Two valid files must be specified");
		}

		LOGGER.info("End of comparison");

		return result.toString();
	}

	/**
	 * Returns the number of difference contained in a StringBuilder by counting
	 * the number of \n in it.
	 * 
	 * @param result
	 *            the StringBuilder in which we want to know the number of lines
	 * 
	 * @return the number of difference contained in the StringBuilder
	 */
	int getNumberOfDifferences(StringBuilder result) {
		return StringUtils.countMatches(result.toString(), "\n");
	}

	/**
	 * Compares two Lines field by field.<br>
	 * For each field, it checks if it is a number or a date and, if it is the
	 * case, it compares the field as a number or as a date.<br>
	 * If not, it compares the field as a string.<br>
	 * 
	 * @param key
	 *            the number of the Line
	 * @param line1
	 *            the Line of the first file
	 * @param line2
	 *            the Line of the second file
	 * 
	 * @return a String containing all the difference and error messages
	 */
	String compareLineFieldByField(Integer key, List<String> line1, List<String> line2) {
		final StringBuilder result = new StringBuilder();

		LOGGER.info("Comparing line " + key + " field by field");

		if (line2.size() != line1.size()) {
			result.append("Line ");
			result.append(key);
			result.append(" has not the same number of fields in both files\n");

			return result.toString();
		}

		for (int tokenIndex = 0; tokenIndex < line1.size(); ++tokenIndex) {
			final String token1 = line1.get(tokenIndex);
			final String token2 = line2.get(tokenIndex);

			if (!token1.equals(token2)) {
				if (ComparatorUtility.bothTokenNumber(token1, token2)) {
					if (!ComparatorUtility.numbersAreEqual(token1, token2)) {
						this.generateDifferenceMessage(key, tokenIndex + 1, result, token1, token2);
					}
				} else if (ComparatorUtility.bothTokenDate(token1, token2)) {
					if (!ComparatorUtility.datesAreEqual(token1, token2)) {
						this.generateDifferenceMessage(key, tokenIndex + 1, result, token1, token2);
					}
				} else {
					this.generateDifferenceMessage(key, tokenIndex + 1, result, token1, token2);
				}
			}
		}

		return result.toString();
	}

	/**
	 * Generates a difference message saying that one field of the first file is
	 * different that the same field of the second file.<br>
	 * Example : <I>Line 452 : aToken - anotherToken.</I>
	 * 
	 * @param key
	 *            the number of the line
	 * @param result
	 *            the StringBuilder where to append the message
	 * @param token1
	 *            the token of the first file
	 * @param token2
	 *            the token of the second file
	 */
	private void generateDifferenceMessage(Integer key, int tokenIndex, final StringBuilder result,
			final String token1, final String token2) {
		result.append("Line ");
		result.append(key);
		result.append(", field ");
		result.append(tokenIndex);
		result.append(" : ");
		result.append(token1);
		result.append(" � ");
		result.append(token2);
		result.append("\n");
	}

	/**
	 * Checks the Lines one by one.<br>
	 * If it doesn't find a match for a complete Line, it searches the first
	 * Line of the second file that has a Levenshtein distance less than 10% and
	 * compares the Line of the first file with it, field by field.
	 * 
	 * @return a String containing all the difference and error messages
	 */
	String checkFiles() {
		final StringBuilder result = new StringBuilder();

		LOGGER.info("Comparing files with order deactivated");

		int key = -1;

		for (Integer keyFile1 : this.getFirstFileLinesAsMap().keySet()) {
			if (this.isCancelled()) {
				LOGGER.info("Comparison cancelled by the user");
				break;
			}

			final Line line1 = this.getFirstFileLinesAsMap().get(keyFile1);
			final String value1 = line1.getFormattedLine();
			final int numberOfDifferences = this.getNumberOfDifferences(result);

			if (numberOfDifferences > 1000) {
				LOGGER.info("There is more than 1000 differences");
				result.append("There is more than 1000 differences. Only the first ");
				result.append(numberOfDifferences);
				result.append(" are displayed. \n");
				break;
			}

			if (this.getSecondFileLinesAsMap().containsValue(line1)) {
				for (Entry<Integer, Line> line2 : this.getSecondFileLinesAsMap().entrySet()) {
					if (line2.getValue().equals(line1)) {
						key = line2.getKey();
						break;
					}
				}
			} else {
				for (Entry<Integer, Line> line2 : this.getSecondFileLinesAsMap().entrySet()) {
					if (this.isCancelled()) {
						LOGGER.info("Comparison cancelled by the user");
						break;
					}

					final String value2 = line2.getValue().getFormattedLine();

					if (value1.equals(value2)) {
						key = line2.getKey();
						break;
					} else if (StringUtils.getLevenshteinDistance(value1, value2) <= (value1
							.length() / 10)) {
						final List<String> tokens1 = ComparatorUtility.tokenize(value1, this
								.getSeparator());
						final List<String> tokens2 = ComparatorUtility.tokenize(value2, this
								.getSeparator());
						key = line2.getKey();
						result.append(this.compareLineFieldByField(key, tokens1, tokens2));
						break;
					}
				}
			}

			if (key == -1) {
				for (Entry<Integer, Line> line2 : this.getSecondFileLinesAsMap().entrySet()) {
					final List<String> tokens1 = ComparatorUtility.tokenize(value1, this
							.getSeparator());
					final List<String> tokens2 = ComparatorUtility.tokenize(line2.getValue()
							.getFormattedLine(), this.getSeparator());
					key = line2.getKey();
					result.append(this.compareLineFieldByField(key, tokens1, tokens2));
					break;
				}
			}

			this.getSecondFileLinesAsMap().remove(key);
			key = -1;
		}

		return result.toString();
	}

	/**
	 * Checks if the comparison has been cancelled.
	 * 
	 * @return true if the comparison has been cancelled, false otherwise
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Cancels the comparison.
	 */
	public void cancel() {
		this.cancelled = true;
	}

	/**
	 * Sets the Lines of the first file contained in a Map.
	 * 
	 * @param file1LinesAsMap
	 */
	public void setFile1LinesAsMap(Map<Integer, Line> file1LinesAsMap) {
		this.firstFileLinesAsMap = file1LinesAsMap;
	}

	/**
	 * Returns the Lines of the first file contained in a Map.
	 * 
	 * @return the Lines of the first file contained in a Map
	 */
	public Map<Integer, Line> getFirstFileLinesAsMap() {
		return this.firstFileLinesAsMap;
	}

	/**
	 * Sets the Lines of the second file contained in a Map.
	 * 
	 * @param file2LinesAsMap
	 */
	public void setFile2LinesAsMap(Map<Integer, Line> file2LinesAsMap) {
		this.secondFileLinesAsMap = file2LinesAsMap;
	}

	/**
	 * Returns the Lines of the second file contained in a Map.
	 * 
	 * @return the Lines of the second file contained in a Map
	 */
	public Map<Integer, Line> getSecondFileLinesAsMap() {
		return this.secondFileLinesAsMap;
	}

	/**
	 * Returns the field separator.
	 * 
	 * @return the field separator
	 */
	public String getSeparator() {
		return this.separator;
	}

	/**
	 * Parses a file and put each line of the file as a Line in a Map.
	 * 
	 * @version $Id: FileComparator.java 165 2010-07-23 13:49:51Z g80195 $
	 * 
	 * @author G80195
	 * 
	 * @see Line
	 */
	private final class FileParser implements Runnable {
		private final String filename;
		private final StringBuilder result;
		private Map<Integer, Line> fileLinesAsMap;

		/**
		 * Initializes a newly created FileParser object.
		 * 
		 * @param filename
		 *            the name of the file to parse
		 * @param result
		 *            the StringBuilder where to put the possible error message
		 */
		public FileParser(String filename, StringBuilder result) {
			this.fileLinesAsMap = new TreeBidiMap<Integer, Line>();
			this.filename = filename;
			this.result = result;
		}

		/**
		 * Parses the file and put each line of the file as a Line in a map.
		 */
		@Override
		public void run() {
			LOGGER.info("Parsing file " + this.filename);

			try {
				this.fileLinesAsMap = ComparatorUtility.parseFile(this.filename,
						FileComparator.this.getSeparator(), FileComparator.this.dates,
						FileComparator.this.numbers);
			} catch (Exception e) {
				LOGGER.error("Problem while parsing file " + this.filename, e);
				this.result.append("Problem while parsing file ");
				this.result.append(this.filename);
			}
		}

		/**
		 * Returns the Lines of the file contained in a Map.
		 * 
		 * @return the Lines of the file contained in a Map
		 */
		public Map<Integer, Line> getFileLinesAsMap() {
			return this.fileLinesAsMap;
		}
	}
}