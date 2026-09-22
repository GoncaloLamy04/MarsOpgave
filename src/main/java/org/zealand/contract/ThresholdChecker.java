package org.zealand.contract;

/**
 * Validates measurements against safety threshold boundaries.
 */
public interface ThresholdChecker {

    /**
     * Checks if a measurement is outside safe operating limits.
     *
     * @param measurement the measurement to validate
     * @return true if the measurement is out of range, false otherwise
     */
    boolean isOutOfRange(Measurement measurement);
}
