package org.zealand.gui;

import org.zealand.contract.Measurement;
import org.zealand.contract.SensorType;
import org.zealand.contract.ThresholdChecker;
import org.zealand.parsing.DefaultThresholdChecker;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Simple Swing dashboard displaying the latest sensor values and alarm statuses.
 * Uses fixed test data until live data from the server is connected.
 */
public class SensorDashboard extends JFrame {

    private static final String[] COLUMNS = {"Sensor", "Værdi", "Status"};
    private static final int STATUS_COLUMN = 2;
    private static final String ALARM = "ALARM";
    private static final String OK = "OK";
    private static final Color ALARM_COLOR = new Color(255, 102, 102);

    private final ThresholdChecker checker;

    /**
     * Creates and initializes the dashboard window with test data.
     *
     * @param checker threshold checker for validating measurements against limits
     */
    public SensorDashboard(ThresholdChecker checker) {
        this.checker = checker;
        setTitle("Marsbase Sensor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        DefaultTableModel model = createReadOnlyModel();
        addMeasurements(model, testData());

        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, createAlarmRenderer());
        add(new JScrollPane(table));
    }

    // Table model where user cannot edit cells
    private DefaultTableModel createReadOnlyModel() {
        return new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Fixed measurements, one for each sensor type
    private List<Measurement> testData() {
        return List.of(
                new Measurement(SensorType.TEMP, 20),
                new Measurement(SensorType.O2, 18),
                new Measurement(SensorType.PRESSURE, 1000),
                new Measurement(SensorType.CO2, 2500)
        );
    }

    // Adds a row per measurement with status evaluated by the checker
    private void addMeasurements(DefaultTableModel model, List<Measurement> measurements) {
        for (Measurement m : measurements) {
            String status = checker.isOutOfRange(m) ? ALARM : OK;
            model.addRow(new Object[]{m.type().name(), m.value(), status});
        }
    }

    // Highlights alarm rows in red, preserving selection colors for focused rows
    private DefaultTableCellRenderer createAlarmRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    boolean alarm = ALARM.equals(table.getValueAt(row, STATUS_COLUMN));
                    c.setBackground(alarm ? ALARM_COLOR : table.getBackground());
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        };
    }

    /**
     * Starts the dashboard in its own window.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {
        ThresholdChecker checker = new DefaultThresholdChecker();
        // Swing components must be initialized on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new SensorDashboard(checker).setVisible(true));
    }
}
