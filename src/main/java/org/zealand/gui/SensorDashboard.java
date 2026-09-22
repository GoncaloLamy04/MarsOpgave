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
 * Simpelt Swing dashboard der viser sensorernes seneste værdi og status.
 * Bruger faste testdata indtil serveren kan levere live data.
 */
public class SensorDashboard extends JFrame {

    private static final String[] COLUMNS = {"Sensor", "Værdi", "Status"};
    private static final int STATUS_COLUMN = 2;
    private static final String ALARM = "ALARM";
    private static final String OK = "OK";
    private static final Color ALARM_COLOR = new Color(255, 102, 102);

    private final ThresholdChecker checker;

    /**
     * Opretter og opsætter dashboard-vinduet med testdata.
     *
     * @param checker threshold checker til validering af målinger mod grænseværdier
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

    // Tabelmodel hvor brugeren ikke kan redigere cellerne
    private DefaultTableModel createReadOnlyModel() {
        return new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Faste målinger, én af hver sensortype
    private List<Measurement> testData() {
        return List.of(
                new Measurement(SensorType.TEMP, 20),
                new Measurement(SensorType.O2, 18),
                new Measurement(SensorType.PRESSURE, 1000),
                new Measurement(SensorType.CO2, 2500)
        );
    }

    // Tilføjer en række pr. måling med status beregnet af checkeren
    private void addMeasurements(DefaultTableModel model, List<Measurement> measurements) {
        for (Measurement m : measurements) {
            String status = checker.isOutOfRange(m) ? ALARM : OK;
            model.addRow(new Object[]{m.type().name(), m.value(), status});
        }
    }

    // Farver rækker med alarm røde, men beholder markeringsfarven for valgte rækker
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
     * Starter dashboardet i et eget vindue.
     *
     * @param args ikke brugt
     */
    public static void main(String[] args) {
        ThresholdChecker checker = new DefaultThresholdChecker();
        // Swing komponenter skal oprettes på Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new SensorDashboard(checker).setVisible(true));
    }
}
