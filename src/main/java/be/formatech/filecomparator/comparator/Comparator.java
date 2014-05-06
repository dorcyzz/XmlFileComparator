package be.formatech.filecomparator.comparator;

import java.io.Serializable;

/**
 * An object that compare two files.<br>
 * The comparison can be cancelled by the cancel method.
 *
 * @author Sebastien Vandamme
 * @version $Id: Comparator.java 163 2010-07-23 08:25:56Z g80195 $
 * @see FileComparator, OrderedFileComparator
 */
public interface Comparator extends Serializable {

    /**
     * Compares two files.
     *
     * @param filename1 the path of the first file to compare
     * @param filename2 the path of the second file to compare
     * @return a String containing all the difference and error messages
     */
    public String compare(String filename1, String filename2);

    /**
     * Checks if the comparison has been cancelled.
     *
     * @return true if the comparison has been cancelled, false otherwise
     */
    public boolean isCancelled();

    /**
     * Cancels the comparison.
     */
    public void cancel();
}