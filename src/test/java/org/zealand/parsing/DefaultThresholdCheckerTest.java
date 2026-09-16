package org.zealand.parsing;

import org.junit.jupiter.api.Test;
import org.zealand.contract.Measurement;
import org.zealand.contract.SensorType;
import org.zealand.contract.ThresholdChecker;

import static org.junit.jupiter.api.Assertions.*;

class DefaultThresholdCheckerTest {

    private final ThresholdChecker checker = new DefaultThresholdChecker();

    // TEMP: alarm below -15 or above 35

    @Test
    void isOutOfRange_tempAbove35_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.TEMP, 35.1);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_tempExactly35_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.TEMP, 35.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    @Test
    void isOutOfRange_tempBelowMinus15_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.TEMP, -15.1);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_tempExactlyMinus15_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.TEMP, -15.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    @Test
    void isOutOfRange_tempNormal_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.TEMP, 20.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    // O2: alarm below 19 or above 23

    @Test
    void isOutOfRange_o2Below19_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.O2, 18.9);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_o2Above23_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.O2, 23.1);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_o2Exactly19_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.O2, 19.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    @Test
    void isOutOfRange_o2Normal_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.O2, 21.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    // PRESSURE: alarm below 800 or above 1100

    @Test
    void isOutOfRange_pressureBelow800_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.PRESSURE, 799.9);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_pressureAbove1100_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.PRESSURE, 1100.1);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_pressureExactly1100_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.PRESSURE, 1100.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    @Test
    void isOutOfRange_pressureNormal_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.PRESSURE, 1000.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    // CO2: alarm above 2000

    @Test
    void isOutOfRange_co2Above2000_returnsTrue() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.CO2, 2000.1);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOutOfRange_co2Exactly2000_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.CO2, 2000.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }

    @Test
    void isOutOfRange_co2Normal_returnsFalse() {
        // Arrange
        Measurement measurement = new Measurement(SensorType.CO2, 800.0);

        // Act
        boolean result = checker.isOutOfRange(measurement);

        // Assert
        assertFalse(result);
    }
}