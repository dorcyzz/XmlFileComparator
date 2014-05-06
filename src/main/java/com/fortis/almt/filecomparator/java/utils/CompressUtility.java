package com.fortis.almt.filecomparator.java.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class is used to compress and decompress files. <br>
 * It should be replaced by the more generic org.apache.commons.compress package
 * when this one will be stable.
 * 
 * @version $Id: CompressUtility.java 161 2010-07-22 10:54:00Z g80195 $
 * 
 * @author Sebastien Vandamme
 */
public final class CompressUtility {
	private static final Logger LOGGER = Logger.getLogger(CompressUtility.class);

	/** The size of the buffer used to decompress files. */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * CompressUtility should not normally be instantiated.
	 */
	private CompressUtility() {

	}

	/**
	 * Compresses all the files represented by their path into the archive
	 * represented by the target path.
	 * 
	 * @param archiveFilePath
	 *            the path of the archive file to create, must not be null
	 * @param filesToCompress
	 *            the path of all the files to add to the archive file, must not
	 *            be null
	 * @throws IllegalArgumentException
	 *             if either String input null or is empty
	 */
	public static boolean compress(final String archiveFilePath, final String... filesToCompress) {
		if (StringUtils.isEmpty(archiveFilePath) || filesToCompress == null) {
			throw new IllegalArgumentException(
					"archiveFilePath and filesToCompress must not be null, nor empty");
		}

		final List<String> files = new ArrayList<String>();

		// Take only the entries that are not null and not empty
		for (String file : filesToCompress) {
			if (!StringUtils.isEmpty(file) && new File(file).exists()) {
				files.add(file);
			}
		}

		if (!files.isEmpty()) {
			FileOutputStream fos = null;
			ZipOutputStream zos = null;
			FileInputStream fis = null;

			try {
				fos = new FileOutputStream(archiveFilePath);
				zos = new ZipOutputStream(fos);

				for (final String source : files) {
					fis = new FileInputStream(source);
					final String filename = new File(source).getName();

					zos.putNextEntry(new ZipEntry(filename));

					IOUtils.copy(fis, zos);
				}

				return true;
			} catch (final IOException e) {
				LOGGER.error("Problem while compressing file(s) "
						+ Arrays.toString(filesToCompress), e);
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}

					if (zos != null) {
						zos.closeEntry();
						zos.close();
					}

					if (fos != null) {
						fos.close();
					}
				} catch (IOException e) {
					LOGGER.error("Problem while closing streams", e);
				}
			}

		}

		return false;
	}

	/**
	 * Decompresses all the files contained in the target path into the
	 * specified location.
	 * 
	 * @param archiveFilePath
	 *            the path where the archive file is located, must not be null
	 * @param destination
	 *            the path where the decompressed files must be located, must
	 *            not be null
	 * @throws IllegalArgumentException
	 *             if either String input null or is empty
	 */
	public static boolean decompress(final String archiveFilePath, final String destination) {
		if (StringUtils.isEmpty(archiveFilePath) || StringUtils.isEmpty(destination)) {
			throw new IllegalArgumentException(
					"archiveFilePath and destination must not be null, nor empty");
		}
		try {
			BufferedOutputStream bos = null;
			final FileInputStream fis = new FileInputStream(archiveFilePath);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final ZipInputStream zis = new ZipInputStream(bis);
			ZipEntry entry;

			try {
				while ((entry = zis.getNextEntry()) != null) {
					final FileOutputStream fos = new FileOutputStream(destination + entry.getName());

					bos = new BufferedOutputStream(fos, BUFFER_SIZE);
					final byte[] data = new byte[BUFFER_SIZE];
					int count;

					while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
						bos.write(data, 0, count);
					}

					bos.flush();
					bos.close();
				}
			} finally {
				zis.closeEntry();
				zis.close();
				bis.close();
				fis.close();
			}

			return true;
		} catch (final FileNotFoundException e) {
			LOGGER.error("File to decompress not found : " + archiveFilePath, e);
		} catch (final IOException e) {
			LOGGER.error("Problem while decompressing file " + archiveFilePath, e);
		}

		return false;
	}
}