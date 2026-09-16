package org.zealand.contract;

public interface MeasurementParser {
    // Parses a line like "TEMP:27.4". Throws IllegalArgumentException on bad input.
    Measurement parse(String line);
}
