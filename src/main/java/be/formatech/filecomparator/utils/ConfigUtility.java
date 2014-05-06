package be.formatech.filecomparator.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * This class is used to load and save configurations. <br>
 * 
 * @version $Id: ConfigUtility.java 161 2010-07-22 10:54:00Z g80195 $
 * 
 * @author Sebastien Vandamme
 */
public final class ConfigUtility {

	private static final Logger LOGGER = Logger.getLogger(ConfigUtility.class);

	/** The comma String ",". */
	private static final String COMMA = ",";

	/** The opening bracket String "[". */
	private static final String OPENING_BRACKET = "[";

	/** The closing bracket String "]". */
	private static final String CLOSING_BRACKET = "]";

	/** The empty Character ' '. */
	private static final char WHITESPACE = ' ';

	/**
	 * The default location of the configuration file : C:/AceComparator/config/
	 */
	private static final String CONFIG_FILE_DEFAULT_LOCATION = "C:/AceComparator/config/";

	/** The default name of the configuration file : AceComparator.config */
	private static final String CONFIG_FILE_NAME = "AceComparator.config";

	/**
	 * The key to identify the first file location in the configuration file :
	 * -firstFile
	 */
	public static final String FIRST_FILE_CONFIG_KEY = "-firstFile";

	/**
	 * The key to identify the second file location in the configuration file :
	 * -secondFile
	 */
	public static final String SECOND_FILE_CONFIG_KEY = "-secondFile";

	/** The key to identify the separator in the configuration file : -separator */
	public static final String SEPARATOR_CONFIG_KEY = "-separator";

	/** The key to identify if order matter in the configuration file : -ordered */
	public static final String ORDERED_CONFIG_KEY = "-ordered";

	/** The key to identify the dates fields in the configuration file : -dates */
	public static final String DATES_CONFIG_KEY = "-dates";

	/**
	 * The key to identify the number fields in the configuration file :
	 * -numbers
	 */
	public static final String NUMBERS_CONFIG_KEY = "-numbers";

	/**
	 * ConfigUtility should not normally be instantiated.
	 */
	private ConfigUtility() {

	}

	/**
	 * Loads the configuration file located at the default location, if present.
	 * The configuration is loaded in a map.
	 * 
	 * @return a map containing the keys with the values found in the
	 *         configuration file
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static Map<String, String> loadConfig() throws IOException {
		return loadConfig(getConfigFileLocation());
	}

	/**
	 * Loads the configuration file located at the location passed in parameter,
	 * if present. The configuration is loaded in a Map.
	 * 
	 * @param fileLocation
	 *            the location of the configuration file to load
	 * @return a map containing the keys with the values found in the
	 *         configuration file
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws IllegalArgumentException
	 *             if fileLocation is null or empty
	 */
	public static Map<String, String> loadConfig(String fileLocation) throws IOException {
		LOGGER.info("Loading configuration");

		if (StringUtils.isEmpty(fileLocation)) {
			throw new IllegalArgumentException("fileLocation must not be null, nor empty");
		}

		final Map<String, String> config = new HashMap<String, String>();

		FileReader reader = null;
		BufferedReader bufferedReader = null;

		try {
			reader = new FileReader(fileLocation);
			bufferedReader = new BufferedReader(reader);
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				if (StringUtils.startsWithIgnoreCase(line, FIRST_FILE_CONFIG_KEY)) {
					config.put(FIRST_FILE_CONFIG_KEY, StringUtils.substring(line, StringUtils
							.indexOf(line, FIRST_FILE_CONFIG_KEY)
							+ StringUtils.length(FIRST_FILE_CONFIG_KEY) + 1));
				} else if (StringUtils.startsWithIgnoreCase(line, SECOND_FILE_CONFIG_KEY)) {
					config.put(SECOND_FILE_CONFIG_KEY, StringUtils.substring(line, StringUtils
							.indexOf(line, SECOND_FILE_CONFIG_KEY)
							+ StringUtils.length(SECOND_FILE_CONFIG_KEY) + 1));
				} else if (StringUtils.startsWithIgnoreCase(line, SEPARATOR_CONFIG_KEY)) {
					config.put(SEPARATOR_CONFIG_KEY, StringUtils.substring(line, StringUtils
							.indexOf(line, SEPARATOR_CONFIG_KEY)
							+ StringUtils.length(SEPARATOR_CONFIG_KEY) + 1));
				} else if (StringUtils.startsWithIgnoreCase(line, ORDERED_CONFIG_KEY)) {
					config.put(ORDERED_CONFIG_KEY, StringUtils.substring(line, StringUtils.indexOf(
							line, ORDERED_CONFIG_KEY)
							+ StringUtils.length(ORDERED_CONFIG_KEY) + 1));
				} else if (StringUtils.startsWithIgnoreCase(line, DATES_CONFIG_KEY)) {
					config.put(DATES_CONFIG_KEY, StringUtils.substring(line, StringUtils.indexOf(
							line, DATES_CONFIG_KEY)
							+ StringUtils.length(DATES_CONFIG_KEY) + 1));
				} else if (StringUtils.startsWithIgnoreCase(line, NUMBERS_CONFIG_KEY)) {
					config.put(NUMBERS_CONFIG_KEY, StringUtils.substring(line, StringUtils.indexOf(
							line, NUMBERS_CONFIG_KEY)
							+ StringUtils.length(NUMBERS_CONFIG_KEY) + 1));
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}

			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}

		return config;
	}

	/**
	 * Saves the configuration passed in parameter in a Map into a file located
	 * at the default location.<br>
	 * If the file exists, it is overwritten.
	 * 
	 * @param config
	 *            the configuration to save
	 * @param applicationTitle
	 *            the title of the application, to put it in the generated file
	 * @return true if the configuration was saved successfully, false otherwise
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws IllegalArgumentException
	 *             if config is null or empty
	 */
	public static boolean saveConfig(Map<String, String> config, String applicationTitle)
			throws IOException {
		return saveConfig(config, applicationTitle, getConfigFileLocation());
	}

	/**
	 * Saves the configuration passed in parameter in a Map into a file located
	 * at the location passed in parameter. <br>
	 * If the file exists, it is overwritten.
	 * 
	 * @param config
	 *            the configuration to save
	 * @param applicationTitle
	 *            the title of the application, to put it in the generated file
	 * @param fileLocation
	 *            the location where to save the configuration
	 * @return true if the configuration was saved successfully, false otherwise
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws IllegalArgumentException
	 *             if config or fileLocation is null or empty
	 */
	public static boolean saveConfig(Map<String, String> config, String applicationTitle,
			String fileLocation) throws IOException {
		LOGGER.info("Saving configuration");

		if (config == null || config.isEmpty() || StringUtils.isEmpty(fileLocation)) {
			throw new IllegalArgumentException(
					"config and fileLocation must not be null, nor empty");
		}

		FileWriter writer = null;
		BufferedWriter bufferedWriter = null;

		if (createConfigDirectoryIfNeeded(fileLocation)) {
			try {

				writer = new FileWriter(fileLocation, false);
				bufferedWriter = new BufferedWriter(writer);

				bufferedWriter.write("# Tool : " + applicationTitle);
				bufferedWriter.newLine();
				bufferedWriter.write("# File : AceComparator.config");
				bufferedWriter.newLine();
				bufferedWriter.write("# Date : " + new Date());
				bufferedWriter.newLine();
				bufferedWriter.newLine();

				bufferedWriter.write(FIRST_FILE_CONFIG_KEY + WHITESPACE
						+ config.get(FIRST_FILE_CONFIG_KEY));
				bufferedWriter.newLine();
				bufferedWriter.write(SECOND_FILE_CONFIG_KEY + WHITESPACE
						+ config.get(SECOND_FILE_CONFIG_KEY));
				bufferedWriter.newLine();
				bufferedWriter.write(SEPARATOR_CONFIG_KEY + WHITESPACE
						+ config.get(SEPARATOR_CONFIG_KEY));
				bufferedWriter.newLine();
				bufferedWriter.write(ORDERED_CONFIG_KEY + WHITESPACE
						+ config.get(ORDERED_CONFIG_KEY));
				bufferedWriter.newLine();
				bufferedWriter.write(DATES_CONFIG_KEY + WHITESPACE + config.get(DATES_CONFIG_KEY));
				bufferedWriter.newLine();
				bufferedWriter.write(NUMBERS_CONFIG_KEY + WHITESPACE
						+ config.get(NUMBERS_CONFIG_KEY));
				bufferedWriter.newLine();

				bufferedWriter.flush();

				return true;
			} finally {
				if (writer != null) {
					writer.close();
				}

				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
			}
		}

		return false;
	}

	/**
	 * Creates the directory if it doesn't exists.
	 * 
	 * @param fileLocation
	 *            the location to create
	 * @return true if the directory already exists or was created successfully,
	 *         false otherwise
	 */
	public static boolean createConfigDirectoryIfNeeded(String fileLocation) {
		final File directory = new File(new File(fileLocation).getParent());

		if (!directory.exists()) {
			return directory.mkdirs();
		}

		return true;
	}

	/**
	 * Transforms a Set<Integer> into a String under the form [2,7,12].
	 * 
	 * @param set
	 *            a set containing integers
	 * @return a string representing the content of a set
	 */
	public static String getSetAsString(Set<Integer> set) {
		final StringBuilder builder = new StringBuilder();
		builder.append(OPENING_BRACKET);

		if (set != null && !set.isEmpty()) {
			for (Integer integer : set) {
				builder.append(integer);
				builder.append(COMMA);
			}

			builder.deleteCharAt(builder.lastIndexOf(COMMA));
		}

		builder.append(CLOSING_BRACKET);

		return builder.toString();
	}

	/**
	 * Transforms a String under the form [2,7,12] into a Set<Integer>.
	 * 
	 * @param string
	 *            a string representing the content of a set
	 * @return a set containing integers
	 */
	public static Set<Integer> getStringAsSet(String string) {
		final TreeSet<Integer> set = new TreeSet<Integer>();

		if (!StringUtils.isEmpty(string)) {
			final StringBuilder builder = new StringBuilder(string);
			builder.deleteCharAt(0); // remove opening bracket
			builder.deleteCharAt(builder.length() - 1); // remove closing
			// bracket

			final List<String> list = Arrays.asList(StringUtils.split(builder.toString(), COMMA));

			for (String element : list) {
				set.add(Integer.valueOf(element));
			}
		}

		return set;
	}

	/**
	 * Returns the default location of the configuration file.
	 * 
	 * @return the default location of the configuration files
	 */
	public static String getConfigFileLocation() {
		return CONFIG_FILE_DEFAULT_LOCATION + CONFIG_FILE_NAME;
	}
}