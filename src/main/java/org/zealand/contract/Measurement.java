package org.zealand.contract;

/**
 * Represents a single sensor measurement consisting of a sensor type and a numerical value.
 *
 * @param type  the sensor type
 * @param value the measured value
 */
public record Measurement(SensorType type, double value) { }
