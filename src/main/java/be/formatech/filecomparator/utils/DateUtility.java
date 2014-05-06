package be.formatech.filecomparator.utils;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utilities used to know if a String contains a Date.
 * 
 * @version $Id: DateUtility.java 162 2010-07-22 11:59:59Z g80195 $
 * 
 * @author Sebastien Vandamme
 */
public final class DateUtility {
	private static final Logger LOGGER = Logger.getLogger(DateUtility.class);

	/** The list of date formats accepted. */
	private static List<SimpleDateFormat> dateFormats;

	static {
		dateFormats = new ArrayList<SimpleDateFormat>();

		final Locale belgium = new Locale("fr", "BE");

		dateFormats.add(new SimpleDateFormat("dd/MM/yyyy", belgium));
		dateFormats.add(new SimpleDateFormat("yyyyMMdd", belgium));
		dateFormats.add(new SimpleDateFormat("MMM dd yyyy hh:mma", belgium));
		dateFormats.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", belgium));
	}

	/**
	 * DateUtility should not normally be instantiated.
	 */
	private DateUtility() {

	}

	/**
	 * Returns the list of date formats accepted.
	 * 
	 * @return the list of date formats accepted
	 */
	public static List<SimpleDateFormat> getDateFormats() {
		return dateFormats;
	}

	/**
	 * Returns the list of date formats accepted as a String array.
	 * 
	 * @return Returns the list of date formats accepted
	 */
	public static String[] getDateFormatsAsPatternArray() {
		final String[] patternArray = new String[dateFormats.size()];
		int i = 0;

		for (SimpleDateFormat dateFormat : dateFormats) {
			patternArray[i++] = dateFormat.toPattern();
		}

		return patternArray;
	}

	/**
	 * Checks if the String passed in parameter contains a date.
	 * 
	 * @param date
	 *            a string that could contain a date
	 * @return true if the parameter is a parseable date, false otherwise
	 */
	public static boolean isDate(String date) {
		try {
			DateUtils.parseDate(date, getDateFormatsAsPatternArray());
		} catch (Exception e) {
			LOGGER.debug(date + " is not a parseable date");
			return false;
		}

		return true;
	}
}