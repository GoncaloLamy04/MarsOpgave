package org.zealand.parsing;

import org.junit.jupiter.api.Test;
import org.zealand.contract.Measurement;
import org.zealand.contract.MeasurementParser;
import org.zealand.contract.SensorType;

import static org.junit.jupiter.api.Assertions.*;

class SimpleParserTest {

    private final MeasurementParser parser = new SimpleParser();

    // Valid input

    @Test
    void parse_validTemp_returnsTempMeasurement() {
        // Arrange
        String line = "TEMP:27.4";

        // Act
        Measurement result = parser.parse(line);

        // Assert
        assertEquals(new Measurement(SensorType.TEMP, 27.4), result);
    }

    @Test
    void parse_validCo2WholeNumber_returnsCo2Measurement() {
        // Arrange
        String line = "CO2:2350";

        // Act
        Measurement result = parser.parse(line);

        // Assert
        assertEquals(new Measurement(SensorType.CO2, 2350.0), result);
    }

    @Test
    void parse_negativeValue_returnsMeasurement() {
        // Arrange
        String line = "TEMP:-20.5";

        // Act
        Measurement result = parser.parse(line);

        // Assert
        assertEquals(new Measurement(SensorType.TEMP, -20.5), result);
    }

    @Test
    void parse_surroundingSpaces_returnsMeasurement() {
        // Arrange
        String line = "  O2:21.0  ";

        // Act
        Measurement result = parser.parse(line);

        // Assert
        assertEquals(new Measurement(SensorType.O2, 21.0), result);
    }

    // Invalid input

    @Test
    void parse_nullLine_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    void parse_emptyLine_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
    }

    @Test
    void parse_missingColon_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("TEMP27.4"));
    }

    @Test
    void parse_unknownType_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("HUMIDITY:50"));
    }

    @Test
    void parse_textInsteadOfNumber_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("CO2:abc"));
    }

    @Test
    void parse_missingValue_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("TEMP:"));
    }

    @Test
    void parse_tooManyParts_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("TEMP:20:30"));
    }
}