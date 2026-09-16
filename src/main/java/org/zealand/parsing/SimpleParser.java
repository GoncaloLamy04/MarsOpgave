package org.zealand.parsing;

import org.zealand.contract.Measurement;
import org.zealand.contract.MeasurementParser;
import org.zealand.contract.SensorType;

public class SimpleParser implements MeasurementParser {
    @Override
    public Measurement parse(String line) {
        if (line == null) throw new IllegalArgumentException("Empty line");
        String s = line.trim();
        String[] parts = s.split(":" , 2);
        if (parts.length != 2) throw new IllegalArgumentException("Invalid format, expected TYPE:value");

        String typeStr = parts[0].trim();
        String valStr = parts[1].trim();

        SensorType type;
        try {
            type = SensorType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown sensor type: " + typeStr);
        }

        double value;
        try {
            value = Double.parseDouble(valStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + valStr);
        }

        return new Measurement(type, value);
    }
}
