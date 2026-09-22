package org.zealand.contract;

/**
 * Logger interface for recording sensor measurements and system errors.
 */
public interface MarsLogger {

    /**
     * Logs a measurement and whether it triggered an alarm.
     *
     * @param measurement the measurement to log
     * @param alarm       true if the measurement triggered an alarm, false otherwise
     */
    void log(Measurement measurement, boolean alarm);

    /**
     * Logs an error message.
     *
     * @param message the error message to log
     */
    void error(String message);
}
