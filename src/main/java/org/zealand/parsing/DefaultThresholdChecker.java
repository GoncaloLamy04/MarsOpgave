package org.zealand.parsing;

import org.zealand.contract.Measurement;
import org.zealand.contract.ThresholdChecker;

/**
 * Tjekker målinger mod koloniens sikkerhedsgrænser.
 * Selve grænseværdierne er tilladt, kun værdier udenfor giver alarm.
 */
public class DefaultThresholdChecker implements ThresholdChecker {

    private static final double TEMP_MIN = -15;
    private static final double TEMP_MAX = 35;
    private static final double O2_MIN = 19;
    private static final double O2_MAX = 23;
    private static final double PRESSURE_MIN = 800;
    private static final double PRESSURE_MAX = 1100;
    private static final double CO2_MAX = 2000;

    /**
     * Tjekker om en måling er uden for de tilladte grænser.
     *
     * @param measurement målingen der skal tjekkes
     * @return true hvis værdien er uden for grænsen for dens sensortype
     */
    @Override
    public boolean isOutOfRange(Measurement measurement) {
        double value = measurement.value();

        return switch (measurement.type()) {
            case TEMP -> isOutside(value, TEMP_MIN, TEMP_MAX);
            case O2 -> isOutside(value, O2_MIN, O2_MAX);
            case PRESSURE -> isOutside(value, PRESSURE_MIN, PRESSURE_MAX);
            case CO2 -> value > CO2_MAX;
        };
    }

    // Grænseværdierne selv er tilladt, kun værdier udenfor giver alarm
    private boolean isOutside(double value, double min, double max) {
        return value < min || value > max;
    }
}
