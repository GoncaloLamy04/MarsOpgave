package org.zealand.parsing;

import org.zealand.contract.Measurement;
import org.zealand.contract.MeasurementParser;
import org.zealand.contract.SensorType;

public class SimpleParser implements MeasurementParser {

    private static final String SEPARATOR = ":";
    private static final int EXPECTED_PARTS = 2;

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

        // -1 keeps empty parts, so "TEMP:" gives two parts instead of one
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

        // NaN would never trigger an alarm, so non-finite values are rejected
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be a finite number: " + text);
        }
        return value;
    }
}