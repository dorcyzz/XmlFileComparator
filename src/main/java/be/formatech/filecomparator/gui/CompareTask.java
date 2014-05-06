package be.formatech.filecomparator.gui;

import be.formatech.filecomparator.comparator.Comparator;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A task used to perform lengthy GUI-interacting comparison in a dedicated
 * thread.
 *
 * @author Sebastien Vandamme
 * @version $Id: CompareTask.java 162 2010-07-22 11:59:59Z g80195 $
 */
public class CompareTask extends SwingWorker<Void, Void> {
    private static final Logger LOGGER = Logger.getLogger(CompareTask.class);

    /**
     * The location of the first file to compare.
     */
    private final String firstFileLocation;

    /**
     * The location of the second file to compare.
     */
    private final String secondFileLocation;

    /**
     * The comparator that will compare the files.
     */
    private final Comparator comparator;

    /**
     * The List containing the results of the comparison.
     */
    private List<String> result;

    /**
     * Initializes a newly created CompareTask object.
     *
     * @param firstFileLocation  the location of the first file to compare
     * @param secondFileLocation the location of the second file to compare
     * @param comparator         the comparator that will compare the files
     */
    public CompareTask(String firstFileLocation, String secondFileLocation, Comparator comparator) {
        super();

        this.firstFileLocation = firstFileLocation;
        this.secondFileLocation = secondFileLocation;
        this.comparator = comparator;
    }

    /**
     * Compares the files and put the result in a List.<br>
     * This method is executed only once, and in a background thread.
     */
    @Override
    public Void doInBackground() {
        LOGGER.info("Launching task in background");

        this.setProgress(0);
        this.result = new ArrayList<String>();

        try {
            this.result.add(this.comparator
                    .compare(this.firstFileLocation, this.secondFileLocation));

            if (!this.isCancelled()) {
                this.result.add(this.comparator.compare(this.secondFileLocation,
                        this.firstFileLocation));
            }
        } catch (Exception e) {
            LOGGER.error("While running task in background", e);
            JOptionPane.showMessageDialog(null, "Error:\n " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (!this.isCancelled()) {
                this.setProgress(100);
            }
        }

        LOGGER.info("End of task");

        return null;
    }

    /**
     * Returns the List containing the results of the comparison.
     *
     * @return the List containing the results of the comparison
     */
    public List<String> getResult() {
        return this.result;
    }
}