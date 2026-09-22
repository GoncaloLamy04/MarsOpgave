package org.zealand.contract;

/**
 * Parser for converting raw text lines into {@link Measurement} instances.
 */
public interface MeasurementParser {

    /**
     * Parses a raw line (e.g. "TEMP:27.4") into a {@link Measurement}.
     *
     * @param line the raw line to parse
     * @return the parsed measurement
     * @throws IllegalArgumentException if the line format or value is invalid
     */
    Measurement parse(String line);
}
