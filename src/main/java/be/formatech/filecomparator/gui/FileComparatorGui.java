package be.formatech.filecomparator.gui;

import be.formatech.filecomparator.comparator.Comparator;
import be.formatech.filecomparator.comparator.ComparatorFactory;
import be.formatech.filecomparator.utils.ComparatorUtility;
import be.formatech.filecomparator.utils.CompressUtility;
import be.formatech.filecomparator.utils.ConfigUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @author Sebastien Vandamme
 * @version $Id: FileComparatorGui.java 163 2010-07-23 08:25:56Z g80195 $
 */
public class FileComparatorGui extends JFrame implements PropertyChangeListener {

    private static final String LOG_FILE_LOCATION = "C:/AceComparator/logs/AceComparator.log";

    private static final int TEN_MEGABYTES = 10 * 1024 * 1024;

    private static final Logger LOGGER = Logger.getLogger(FileComparatorGui.class);

    public static final String APPLICATION_NAME_AND_VERSION = "Ace Comparator 1.0";

    private static final long serialVersionUID = 8027975454079710712L;

    private final JTextField firstFileToCompare;
    private final JTextField secondFileToCompare;
    private final JTextField separator;

    private final JLabel firstFileToCompareLabel;
    private final JLabel secondFileToCompareLabel;

    private final JButton fileChooserFirstFile;
    private final JButton fileChooserSecondFile;
    private final JButton formattedColumn;
    private final JButton compare;
    private final JButton reset;
    private final JButton clearResults;
    private final JButton cancel;

    private JTextArea file1TextArea;
    private JTextArea file2TextArea;

    private final JProgressBar progressBar;

    private final JCheckBox orderedColumn;
    private final JCheckBox multiFieldLines;

    private CompareTask task;

    private Comparator comparator;

    private final Set<Integer> dates = new TreeSet<Integer>();
    private final Set<Integer> numbers = new TreeSet<Integer>();

    public FileComparatorGui() {
        super(APPLICATION_NAME_AND_VERSION);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1024, 768);
        this.setLayout(new BorderLayout());

        this.firstFileToCompare = new JTextField();
        this.secondFileToCompare = new JTextField();
        this.separator = new JTextField();
        this.separator.setEnabled(false);

        this.firstFileToCompareLabel = new JLabel("First file to compare: ");
        this.firstFileToCompareLabel.setLabelFor(this.firstFileToCompare);
        this.secondFileToCompareLabel = new JLabel("Second file to compare: ");
        this.secondFileToCompareLabel.setLabelFor(this.secondFileToCompare);

        this.fileChooserFirstFile = new JButton(new AbstractAction("...") {
            private static final long serialVersionUID = -4518444826804845039L;

            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser("C:");
                final int returnVal = chooser.showOpenDialog(FileComparatorGui.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    FileComparatorGui.this.firstFileToCompare.setText(chooser.getSelectedFile()
                            .getPath());
                }
            }
        });

        this.fileChooserSecondFile = new JButton(new AbstractAction("...") {
            private static final long serialVersionUID = 2434641560841951498L;

            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser("C:");
                final int returnVal = chooser.showOpenDialog(FileComparatorGui.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    FileComparatorGui.this.secondFileToCompare.setText(chooser.getSelectedFile()
                            .getPath());
                }
            }
        });

        this.formattedColumn = new JButton(new AbstractAction("Formatted Columns") {
            private static final long serialVersionUID = -1429073264905414195L;

            public void actionPerformed(ActionEvent e) {
                if (FileComparatorGui.this.checkParameters(
                        FileComparatorGui.this.firstFileToCompare.getText(),
                        FileComparatorGui.this.secondFileToCompare.getText(),
                        FileComparatorGui.this.separator.getText())) {
                    try {
                        final List<String> columns = ComparatorUtility.getFirstRealLineTokenized(
                                FileComparatorGui.this.firstFileToCompare.getText(),
                                FileComparatorGui.this.separator.getText());
                        new ColumnDialog(FileComparatorGui.this, FileComparatorGui.this.dates,
                                FileComparatorGui.this.numbers, columns);
                    } catch (IOException ioe) {
                        LOGGER.error("Problem while trying to read the file", ioe);
                        JOptionPane.showMessageDialog(null,
                                "Error:\nProblem while trying to read the file", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });

        this.orderedColumn = new JCheckBox("Order matter");
        this.multiFieldLines = new JCheckBox(new AbstractAction(
                "Line contains fields (enter separator)") {
            private static final long serialVersionUID = 548103558246481414L;

            @Override
            public void actionPerformed(ActionEvent e) {
                FileComparatorGui.this.separator.setEnabled(FileComparatorGui.this.multiFieldLines
                        .isSelected());
            }
        });

        this.reset = new JButton(new AbstractAction("Reset All") {
            private static final long serialVersionUID = 3470111722498829078L;

            public void actionPerformed(ActionEvent e) {
                FileComparatorGui.this.firstFileToCompare.setText(StringUtils.EMPTY);
                FileComparatorGui.this.secondFileToCompare.setText(StringUtils.EMPTY);
                FileComparatorGui.this.separator.setText(StringUtils.EMPTY);
                FileComparatorGui.this.separator.setEnabled(false);
                FileComparatorGui.this.file1TextArea.setText(StringUtils.EMPTY);
                FileComparatorGui.this.file2TextArea.setText(StringUtils.EMPTY);
                FileComparatorGui.this.orderedColumn.setSelected(false);
                FileComparatorGui.this.multiFieldLines.setSelected(false);
                FileComparatorGui.this.dates.clear();
                FileComparatorGui.this.numbers.clear();
            }
        });

        this.clearResults = new JButton(new AbstractAction("Clear Results") {
            private static final long serialVersionUID = -2018501188857289999L;

            public void actionPerformed(ActionEvent e) {
                FileComparatorGui.this.file1TextArea.setText(StringUtils.EMPTY);
                FileComparatorGui.this.file2TextArea.setText(StringUtils.EMPTY);
            }
        });

        this.cancel = new JButton(new AbstractAction("Cancel") {
            private static final long serialVersionUID = 8488561353063538565L;

            public void actionPerformed(ActionEvent e) {
                if (FileComparatorGui.this.task != null
                        && !FileComparatorGui.this.task.isCancelled()) {
                    FileComparatorGui.this.comparator.cancel();
                    FileComparatorGui.this.task.cancel(true);
                    FileComparatorGui.this.enableAllControls(true);

                    if (FileComparatorGui.this.task != null
                            && FileComparatorGui.this.task.getResult() != null
                            && FileComparatorGui.this.task.getResult().size() > 0) {
                        final List<String> result = FileComparatorGui.this.task.getResult();

                        FileComparatorGui.this.file1TextArea.setText(result.get(0));
                        FileComparatorGui.this.file2TextArea.setText(result.get(0));
                    }

                    FileComparatorGui.this.progressBar.setString("Comparison cancelled");

                }
            }
        });
        this.cancel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progressBar.setString("Comparing . . .");

        this.compare = new JButton(new AbstractAction("Compare") {
            private static final long serialVersionUID = -8328345339976174365L;

            public void actionPerformed(ActionEvent e) {
                final String filename1 = FileComparatorGui.this.firstFileToCompare.getText();
                final String filename2 = FileComparatorGui.this.secondFileToCompare.getText();
                final String separatorString = FileComparatorGui.this.separator.getText();

                if (FileComparatorGui.this.checkParameters(filename1, filename2, separatorString)) {
                    FileComparatorGui.this.enableAllControls(false);

                    FileComparatorGui.this.comparator = ComparatorFactory.getInstance()
                            .getComparator(separatorString, FileComparatorGui.this.dates,
                                    FileComparatorGui.this.numbers,
                                    FileComparatorGui.this.orderedColumn.isSelected());

                    FileComparatorGui.this.task = new CompareTask(filename1, filename2,
                            FileComparatorGui.this.comparator);
                    FileComparatorGui.this.task.addPropertyChangeListener(FileComparatorGui.this);
                    FileComparatorGui.this.task.execute();
                }
            }

        });

        this.add(this.createControlPanel(), BorderLayout.NORTH);
        this.add(this.createResultPane(), BorderLayout.CENTER);
        this.add(this.progressBar, BorderLayout.SOUTH);

        this.setJMenuBar(this.createMenu());

        this.addToolTips();

        this.setVisible(true);

        this.checkLogFileSize();
    }

    /**
     * Checks if the current log file size is more than 10 Mb.<br>
     * If it is the case, it archive it in a zip file named with the date and
     * time of the creation of the archive.
     */
    private void checkLogFileSize() {
        final File logFile = new File(LOG_FILE_LOCATION);

        if (logFile.exists() && logFile.isFile() && logFile.length() > TEN_MEGABYTES) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_hhmm");

            CompressUtility.compress(StringUtils.remove(LOG_FILE_LOCATION, ".log")
                    + dateFormat.format(new Date()) + ".zip", LOG_FILE_LOCATION);
            FileUtils.deleteQuietly(logFile);
        }
    }

    private boolean checkParameters(String filename1, String filename2, String separatorString) {
        if (StringUtils.isEmpty(filename1)) {
            JOptionPane.showMessageDialog(null, "Error:\nFirst file cannot be empty", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!new File(filename1).exists()) {
            JOptionPane.showMessageDialog(null, "Error:\nFile " + filename1
                    + " doesn't seem to exist", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (StringUtils.isEmpty(filename2)) {
            JOptionPane.showMessageDialog(null, "Error:\nSecond file cannot be empty", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!new File(filename2).exists()) {
            JOptionPane.showMessageDialog(null, "Error:\nFile " + filename2
                    + " doesn't seem to exist", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (this.multiFieldLines.isSelected() && StringUtils.isEmpty(separatorString)) {
            JOptionPane.showMessageDialog(null, "Error:\nSeparator cannot be empty", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private JSplitPane createResultPane() {
        final Font font = new Font("Arial", Font.PLAIN, 14);

        this.file1TextArea = new JTextArea();
        this.file1TextArea.setFont(font);
        this.file1TextArea.setLineWrap(true);
        this.file1TextArea.setWrapStyleWord(true);
        this.file1TextArea.setEditable(false);

        final JScrollPane file1AreaScrollPane = new JScrollPane(this.file1TextArea);
        file1AreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        file1AreaScrollPane
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        file1AreaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createCompoundBorder(BorderFactory.createTitledBorder("First File"), BorderFactory
                        .createEmptyBorder(5, 5, 5, 5)), file1AreaScrollPane.getBorder()));

        this.file2TextArea = new JTextArea();
        this.file2TextArea.setFont(font);
        this.file2TextArea.setLineWrap(true);
        this.file2TextArea.setWrapStyleWord(true);
        this.file2TextArea.setEditable(false);

        final JScrollPane file2AreaScrollPane = new JScrollPane(this.file2TextArea);
        file2AreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        file2AreaScrollPane
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        file2AreaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createCompoundBorder(BorderFactory.createTitledBorder("Second File"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)), file2AreaScrollPane
                .getBorder()));

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                file1AreaScrollPane, file2AreaScrollPane);

        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    private JPanel createParameterPanel() {
        final JPanel parameterPanel = new JPanel();
        final GridBagLayout parameterGridBag = new GridBagLayout();
        final GridBagConstraints parameterConstraints = new GridBagConstraints();
        parameterPanel.setLayout(parameterGridBag);

        parameterConstraints.anchor = GridBagConstraints.WEST;

        parameterConstraints.gridwidth = GridBagConstraints.REMAINDER;
        parameterConstraints.fill = GridBagConstraints.NONE;
        parameterConstraints.weightx = 1.0;
        parameterPanel.add(this.firstFileToCompareLabel, parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.RELATIVE;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;
        parameterPanel.add(this.firstFileToCompare, parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.REMAINDER;
        parameterConstraints.fill = GridBagConstraints.NONE;
        parameterConstraints.weightx = 0.0;
        parameterPanel.add(this.fileChooserFirstFile, parameterConstraints);
        parameterPanel.add(this.secondFileToCompareLabel, parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.RELATIVE;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;
        parameterConstraints.weightx = 1.0;
        parameterPanel.add(this.secondFileToCompare, parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.REMAINDER;
        parameterConstraints.fill = GridBagConstraints.NONE;
        parameterConstraints.weightx = 0.0;
        parameterPanel.add(this.fileChooserSecondFile, parameterConstraints);

        parameterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Parameters"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return parameterPanel;
    }

    private JPanel createOptionPanel() {
        final JPanel optionPanel = new JPanel();
        optionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Options"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final GridBagLayout parameterGridBag = new GridBagLayout();
        final GridBagConstraints parameterConstraints = new GridBagConstraints();
        optionPanel.setLayout(parameterGridBag);

        parameterConstraints.anchor = GridBagConstraints.WEST;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;

        parameterConstraints.gridx = 0;
        parameterConstraints.gridy = 0;
        optionPanel.add(this.formattedColumn, parameterConstraints);

        parameterConstraints.gridx = 0;
        parameterConstraints.gridy = 1;
        optionPanel.add(this.orderedColumn, parameterConstraints);

        parameterConstraints.gridx = 0;
        parameterConstraints.gridy = 2;
        optionPanel.add(this.multiFieldLines, parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.RELATIVE;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;
        parameterConstraints.weightx = 0.5;
        parameterConstraints.gridx = 1;
        parameterConstraints.gridy = 2;
        optionPanel.add(this.separator, parameterConstraints);

        return optionPanel;
    }

    private JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel();
        final GridBagLayout parameterGridBag = new GridBagLayout();
        final GridBagConstraints parameterConstraints = new GridBagConstraints();
        buttonPanel.setLayout(parameterGridBag);

        parameterConstraints.anchor = GridBagConstraints.WEST;

        parameterConstraints.gridwidth = GridBagConstraints.REMAINDER;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;
        parameterConstraints.weightx = 1.0;
        parameterConstraints.insets = new Insets(3, 5, 2, 5);

        buttonPanel.add(this.compare, parameterConstraints);
        buttonPanel.add(this.reset, parameterConstraints);
        buttonPanel.add(this.clearResults, parameterConstraints);
        parameterConstraints.insets = new Insets(2, 5, 3, 5);
        buttonPanel.add(this.cancel, parameterConstraints);

        return buttonPanel;
    }

    private JPanel createControlPanel() {
        final JPanel controlPanel = new JPanel();
        final GridBagLayout parameterGridBag = new GridBagLayout();
        final GridBagConstraints parameterConstraints = new GridBagConstraints();
        controlPanel.setLayout(parameterGridBag);

        parameterConstraints.anchor = GridBagConstraints.WEST;

        parameterConstraints.gridwidth = GridBagConstraints.REMAINDER;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;
        parameterConstraints.weightx = 1.0;
        controlPanel.add(this.createParameterPanel(), parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.RELATIVE;
        parameterConstraints.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(this.createOptionPanel(), parameterConstraints);

        parameterConstraints.gridwidth = GridBagConstraints.REMAINDER;
        parameterConstraints.fill = GridBagConstraints.NONE;
        parameterConstraints.weightx = 0.0;
        controlPanel.add(this.createButtonPanel(), parameterConstraints);

        return controlPanel;
    }

    private JMenuBar createMenu() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu menu = new JMenu("Configuration");
        menuBar.add(menu);

        JMenuItem savePreferenceItem = new JMenuItem(new AbstractAction("Save Preferences") {
            private static final long serialVersionUID = 3012208092294315451L;

            @Override
            public void actionPerformed(ActionEvent e) {
                final Map<String, String> config = new HashMap<String, String>();

                config.put(ConfigUtility.FIRST_FILE_CONFIG_KEY,
                        FileComparatorGui.this.firstFileToCompare.getText());
                config.put(ConfigUtility.SECOND_FILE_CONFIG_KEY,
                        FileComparatorGui.this.secondFileToCompare.getText());
                config.put(ConfigUtility.SEPARATOR_CONFIG_KEY, FileComparatorGui.this.separator
                        .getText());
                config.put(ConfigUtility.ORDERED_CONFIG_KEY, Boolean
                        .toString(FileComparatorGui.this.orderedColumn.isSelected()));
                config.put(ConfigUtility.DATES_CONFIG_KEY, ConfigUtility
                        .getSetAsString(FileComparatorGui.this.dates));
                config.put(ConfigUtility.NUMBERS_CONFIG_KEY, ConfigUtility
                        .getSetAsString(FileComparatorGui.this.numbers));

                try {
                    ConfigUtility.saveConfig(config, APPLICATION_NAME_AND_VERSION);
                } catch (IOException ioe) {
                    LOGGER.error("Problem while trying to save the configuration", ioe);
                    JOptionPane.showMessageDialog(null,
                            "Error:\nProblem while trying to save the configuration", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem loadPreferenceItem = new JMenuItem(new AbstractAction("Load Preferences") {
            private static final long serialVersionUID = -137839491604569165L;

            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, String> config;

                try {
                    config = ConfigUtility.loadConfig();
                } catch (IOException ioe) {
                    LOGGER.error("Problem while trying to load the configuration", ioe);
                    JOptionPane.showMessageDialog(null,
                            "Error:\nProblem while trying to load the configuration", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                FileComparatorGui.this.firstFileToCompare.setText(config
                        .get(ConfigUtility.FIRST_FILE_CONFIG_KEY));
                FileComparatorGui.this.secondFileToCompare.setText(config
                        .get(ConfigUtility.SECOND_FILE_CONFIG_KEY));

                final String separatorConfig = config.get(ConfigUtility.SEPARATOR_CONFIG_KEY);
                FileComparatorGui.this.separator.setText(separatorConfig);
                FileComparatorGui.this.multiFieldLines.setSelected(!StringUtils
                        .isEmpty(separatorConfig));
                FileComparatorGui.this.separator.setEnabled(!StringUtils.isEmpty(separatorConfig));

                FileComparatorGui.this.orderedColumn.setSelected(Boolean.parseBoolean(config
                        .get(ConfigUtility.ORDERED_CONFIG_KEY)));
                FileComparatorGui.this.dates.clear();
                FileComparatorGui.this.dates.addAll(ConfigUtility.getStringAsSet(config
                        .get(ConfigUtility.DATES_CONFIG_KEY)));
                FileComparatorGui.this.numbers.clear();
                FileComparatorGui.this.numbers.addAll(ConfigUtility.getStringAsSet(config
                        .get(ConfigUtility.NUMBERS_CONFIG_KEY)));
            }
        });

        savePreferenceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_MASK));
        loadPreferenceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                InputEvent.CTRL_MASK));

        menu.add(savePreferenceItem);
        menu.add(loadPreferenceItem);

        return menuBar;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName()) && ((Integer) evt.getNewValue()) == 100) {
            LOGGER.info("Property changed");
            this.enableAllControls(true);

            if (this.task != null && this.task.getResult() != null
                    && this.task.getResult().size() > 1) {
                final List<String> result = this.task.getResult();

                this.file1TextArea.setText(result.get(0));
                this.file2TextArea.setText(result.get(1));
            }
        }
    }

    private void enableAllControls(boolean enable) {
        this.firstFileToCompare.setEnabled(enable);
        this.secondFileToCompare.setEnabled(enable);
        this.fileChooserFirstFile.setEnabled(enable);
        this.fileChooserSecondFile.setEnabled(enable);
        this.formattedColumn.setEnabled(enable);
        this.orderedColumn.setEnabled(enable);
        this.multiFieldLines.setEnabled(enable);
        this.separator.setEnabled(enable);
        this.compare.setEnabled(enable);
        this.reset.setEnabled(enable);
        this.clearResults.setEnabled(enable);
        this.progressBar.setIndeterminate(!enable);

        if (enable) {
            this.progressBar.setValue(100);
            this.progressBar.setString("Comparison finished");
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.progressBar.setStringPainted(true);
        }
    }

    private void addToolTips() {
        this.firstFileToCompare.setToolTipText("Enter first file to compare");
        this.secondFileToCompare.setToolTipText("Enter first file to compare");
        this.fileChooserFirstFile.setToolTipText("Open filechooser to select first file");
        this.fileChooserSecondFile.setToolTipText("Open filechooser to select second file");
        this.formattedColumn.setToolTipText("Mark columns as Date or Number");
        this.orderedColumn.setToolTipText("Must the lines in the two files be in the same order");
        this.multiFieldLines
                .setToolTipText("Does the file contain fields delimited by a separator");
        this.separator.setToolTipText("Enter the separator if needed");
        this.compare.setToolTipText("Run the comparison");
        this.reset.setToolTipText("reset all options to default");
        this.clearResults.setToolTipText("Clear the results");
        this.cancel.setToolTipText("Cancel the running comparison");
        this.file1TextArea
                .setToolTipText("Display the result of the comparison between the first file and the second file");
        this.file2TextArea
                .setToolTipText("Display the result of the comparison between the second file and the first file");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FileComparatorGui();
            }
        });
    }
}