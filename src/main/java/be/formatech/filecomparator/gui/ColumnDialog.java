package be.formatech.filecomparator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Sebastien Vandamme
 * @version $Id: ColumnDialog.java 161 2010-07-22 10:54:00Z g80195 $
 */
public class ColumnDialog extends JDialog {
    private static final long serialVersionUID = -913115599642074602L;

    private final Set<Integer> dates;

    private final Set<Integer> datesCopy;

    private final Set<Integer> numbers;

    private final Set<Integer> numbersCopy;

    public ColumnDialog(JFrame parent, Set<Integer> dates, Set<Integer> numbers,
                        List<String> columns) {
        super(parent, "Column type", true);

        this.dates = dates;
        this.numbers = numbers;

        this.datesCopy = new TreeSet<Integer>(dates);
        this.numbersCopy = new TreeSet<Integer>(numbers);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());

        final JScrollPane jScrollPane = new JScrollPane(this.createCheckBoxPanel(columns));
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.add(jScrollPane, BorderLayout.CENTER);
        this.add(this.createButtonPanel(), BorderLayout.SOUTH);
        this.setSize(600, 600);
        this.setVisible(true);

    }

    private JPanel createCheckBoxPanel(List<String> columns) {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(columns.size(), 3));

        for (int i = 0; i < columns.size(); i++) {
            final JLabel columnNumber = new JLabel("Column " + (i + 1) + " (" + columns.get(i)
                    + ")");
            final JCheckBox boxDate = new JCheckBox(new AbstractAction("is date") {
                private static final long serialVersionUID = -3150512300662062109L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColumnDialog.this.getFormattedColumnNumber(e, ColumnDialog.this.datesCopy,
                            ColumnDialog.this.numbersCopy);
                }
            });
            final JCheckBox boxNumber = new JCheckBox(new AbstractAction("is number") {
                private static final long serialVersionUID = -2096311430391217012L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColumnDialog.this.getFormattedColumnNumber(e, ColumnDialog.this.numbersCopy,
                            ColumnDialog.this.datesCopy);
                }

            });

            if (this.datesCopy.contains(i + 1)) {
                boxDate.setSelected(true);
            }

            if (this.numbersCopy.contains(i + 1)) {
                boxNumber.setSelected(true);
            }

            boxDate.setName(String.valueOf(i));
            boxNumber.setName(String.valueOf(i));

            panel.add(columnNumber);
            panel.add(boxDate);
            panel.add(boxNumber);
        }

        return panel;
    }

    public final JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints controlConstraints = new GridBagConstraints();

        final JButton okButton = new JButton(new AbstractAction("OK") {
            private static final long serialVersionUID = -2194014090469614446L;

            @Override
            public void actionPerformed(ActionEvent e) {
                ColumnDialog.this.dates.clear();
                ColumnDialog.this.dates.addAll(ColumnDialog.this.datesCopy);
                ColumnDialog.this.numbers.clear();
                ColumnDialog.this.numbers.addAll(ColumnDialog.this.numbersCopy);
                ColumnDialog.this.dispose();
            }
        });

        final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            private static final long serialVersionUID = -2602746364991413169L;

            @Override
            public void actionPerformed(ActionEvent e) {
                ColumnDialog.this.dispose();
            }
        });

        controlConstraints.anchor = GridBagConstraints.WEST;
        controlConstraints.gridwidth = GridBagConstraints.RELATIVE;
        controlConstraints.fill = GridBagConstraints.NONE;
        controlConstraints.weightx = 0.0;

        buttonPanel.add(okButton, controlConstraints);
        buttonPanel.add(cancelButton, controlConstraints);

        return buttonPanel;
    }

    private void getFormattedColumnNumber(ActionEvent e, Set<Integer> dest, Set<Integer> other) {
        final JCheckBox checkBox = (JCheckBox) e.getSource();
        final Integer columnNumber = Integer.parseInt(checkBox.getName()) + 1;

        if (checkBox.isSelected()) {

            if (other.contains(columnNumber)) {
                JOptionPane.showMessageDialog(null,
                        "Error:\nA column cannot be a dates and a number", "Error",
                        JOptionPane.ERROR_MESSAGE);
                checkBox.setSelected(false);
            } else {
                dest.add(columnNumber);
            }
        } else {
            dest.remove(columnNumber);
        }
    }
}
