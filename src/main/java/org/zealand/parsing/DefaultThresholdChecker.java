package org.zealand.parsing;

import org.zealand.contract.Measurement;
import org.zealand.contract.ThresholdChecker;
import org.zealand.contract.SensorType;

public class DefaultThresholdChecker implements ThresholdChecker {
    @Override
    public boolean isOutOfRange(Measurement measurement) {
        SensorType type = measurement.type();
        double v = measurement.value();

        return switch (type) {
            case TEMP -> (v < -15.0) || (v > 35.0);
            case O2 -> (v < 19.0) || (v > 23.0);
            case PRESSURE -> (v < 800.0) || (v > 1100.0);
            case CO2 -> (v > 2000.0);
        };
    }
}
