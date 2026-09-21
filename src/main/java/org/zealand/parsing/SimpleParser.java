package org.zealand.parsing;

import org.zealand.contract.Measurement;
import org.zealand.contract.MeasurementParser;
import org.zealand.contract.SensorType;

/**
 * Omdanner sensorlinjer i formatet TYPE:værdi til en Measurement.
 * Ugyldigt input giver IllegalArgumentException.
 */
public class SimpleParser implements MeasurementParser {

    private static final String SEPARATOR = ":";
    private static final int EXPECTED_PARTS = 2;

    /**
     * Omdanner en linje i formatet TYPE:værdi til en Measurement.
     *
     * @param line linjen der skal parses, fx "TEMP:27.4"
     * @return den parsede måling
     * @throws IllegalArgumentException hvis linjen er ugyldig
     */
    @Override
    public Measurement parse(String line) {
        String[] parts = splitLine(line);
        SensorType type = parseType(parts[0]);
        double value = parseValue(parts[1]);
        return new Measurement(type, value);
    }

    private String[] splitLine(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Input line cannot be null or empty");
        }

        // -1 beholder tomme dele, så "TEMP:" giver to dele i stedet for én
        String[] parts = line.trim().split(SEPARATOR, -1);
        if (parts.length != EXPECTED_PARTS || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("Invalid format. Expected TYPE:value");
        }
        return parts;
    }

    private SensorType parseType(String text) {
        try {
            return SensorType.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown sensor type: " + text, e);
        }
    }

    private double parseValue(String text) {
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + text, e);
        }

        // NaN udløser aldrig en alarm, så ikke-endelige værdier afvises
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be a finite number: " + text);
        }
        return value;
    }
}
