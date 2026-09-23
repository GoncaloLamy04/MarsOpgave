package org.zealand.server;

import org.zealand.contract.Measurement;
import org.zealand.contract.MeasurementParser;
import org.zealand.contract.ThresholdChecker;
import org.zealand.contract.MarsLogger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Handles a single sensor connection.
 *
 * <p>Each handler reads lines from the socket, parses measurements, checks
 * thresholds, logs the measurement and sends a response to the client.</p>
 */
public class SensorHandler implements Runnable {
    private final Socket socket;
    private final int sensorId;
    private final MeasurementParser parser;
    private final ThresholdChecker checker;
    private final MarsLogger logger;

    /**
     * Creates a handler for a sensor connection.
     *
     * @param socket   socket connected to the sensor client
     * @param sensorId numerical id assigned to this sensor (for logging)
     * @param parser   parser used to convert incoming lines to {@link Measurement}
     * @param checker  threshold checker used to determine alarms
     * @param logger   logger to record measurements and errors (shared across threads)
     */
    public SensorHandler(Socket socket, int sensorId, MeasurementParser parser, ThresholdChecker checker, MarsLogger logger) {
        this.socket = socket;
        this.sensorId = sensorId;
        this.parser = parser;
        this.checker = checker;
        this.logger = logger;
    }

    /**
     * Continuously reads lines from the sensor socket until the client closes the
     * connection or an I/O error occurs. Each received line is delegated to
     * {@link #handleLine(String, java.io.PrintWriter)} for processing.
     */
    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                handleLine(line, out);
            }

            // readLine returned null => client closed connection; report and log
            reportDisconnect("disconnected (EOF)");

        } catch (IOException e) {
            reportDisconnect("disconnected: " + e.getMessage());
        }
    }

    /**
     * Helper to report a sensor disconnect: prints a standard error message and
     * forwards a descriptive message to the shared logger. Centralizing the
     * logic prevents duplicated code paths for EOF and I/O error handling.
     *
     * @param detail detail string appended to the logger message (e.g. "disconnected (EOF)")
     */
    private void reportDisconnect(String detail) {
        System.err.println("[ERROR] Sensor " + sensorId + " mistede forbindelsen.");
        try {
            logger.error("Sensor " + sensorId + " " + detail);
        } catch (Exception logEx) {
            reportLoggingFailure(logEx);
        }
    }

    /**
     * Processes a single incoming line from the sensor and replies to the client.
     *
     * <p>The method parses the given line into a {@link Measurement}, checks if
     * the measurement is out of range and logs the result. If the measurement
     * is out of range an alarm message is sent to the client, otherwise "OK"
     * is sent. If parsing fails an error message is returned to the client.
     * </p>
     *
     * @param line the raw line received from the sensor client (e.g. "TEMP:27.4")
     * @param out  writer to send a single-line response back to the client
     */
    public void handleLine(String line, PrintWriter out) {
        try {
            Measurement m = parser.parse(line);
            boolean alarm = checker.isOutOfRange(m);

            // Log the measurement
            try {
                logger.log(m, alarm);
            } catch (Exception logEx) {
                reportLoggingFailure(logEx);
            }

            if (alarm) {
                String msg = String.format("ALARM: %s value out of range! (value = %s)", m.type(), m.value());
                out.println(msg);
                System.out.println("[ALARM] Sensor " + sensorId + " -> " + msg);
            } else {
                out.println("OK");
                System.out.println("[Sensor " + sensorId + "] " + m.type() + ": " + m.value());
            }

        } catch (IllegalArgumentException ex) {
            String err = "ERROR|" + ex.getMessage();
            out.println(err);
            System.err.println("[ERROR] Sensor " + sensorId + " sent bad line: " + ex.getMessage());
            try {
                logger.error("Sensor " + sensorId + " bad line: " + ex.getMessage());
            } catch (Exception logEx) {
                reportLoggingFailure(logEx);
            }
        }
    }

    /**
     * Skriver logger-fejl til server-errors.log, så de ikke kun forsvinder i konsollen.
     * Klient-protokollen påvirkes ikke, kaldet fanges internt.
     *
     * @param logEx den exception loggeren kastede
     */
    private synchronized void reportLoggingFailure(Exception logEx) {
        System.err.println("[LOG ERROR] " + logEx.getMessage());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("server-errors.log", true))) {
            writer.write("[" + LocalDateTime.now() + "] Logging failed: " + logEx.getMessage());
            writer.newLine();
        } catch (IOException writeEx) {
            System.err.println("[CRITICAL] Could not write to server-errors.log either: " + writeEx.getMessage());
        }
    }
}
