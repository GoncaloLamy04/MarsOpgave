package org.zealand.logging;

import org.zealand.contract.MarsLogger;
import org.zealand.contract.Measurement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes measurement logs to a file as append-only entries.
 */
public class FileMarsLogger implements MarsLogger {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String filePath;

    /**
     * Creates a logger that appends to the given file path.
     *
     * @param filePath path to the log file
     */
    public FileMarsLogger(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Logs a measurement entry with a timestamp to the file.
     *
     * @param measurement the measurement to log
     * @param alarm       true if the measurement triggered an alarm, false otherwise
     */
    @Override
    public synchronized void log(Measurement measurement, boolean alarm) {
        String line = String.format("[%s] %s: %s%s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                measurement.type(),
                measurement.value(),
                alarm ? " -> ALARM!" : "");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to log file: " + filePath, e);
        }
    }

    /**
     * Logs an error message with a timestamp to the file.
     *
     * @param message the error message to log
     */
    @Override
    public synchronized void error(String message) {
        String line = String.format("[%s] ERROR: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                message);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to log file: " + filePath, e);
        }
    }
}
