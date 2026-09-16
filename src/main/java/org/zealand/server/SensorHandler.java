package org.zealand.server;

import org.zealand.contract.Measurement;
import org.zealand.contract.MeasurementParser;
import org.zealand.contract.ThresholdChecker;
import org.zealand.contract.MarsLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SensorHandler implements Runnable {
    private final Socket socket;
    private final int sensorId;
    private final MeasurementParser parser;
    private final ThresholdChecker checker;
    private final MarsLogger logger;

    public SensorHandler(Socket socket, int sensorId, MeasurementParser parser, ThresholdChecker checker, MarsLogger logger) {
        this.socket = socket;
        this.sensorId = sensorId;
        this.parser = parser;
        this.checker = checker;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                handleLine(line, out);
            }

            // readLine returned null => client closed connection; report and log
            System.err.println("[ERROR] Sensor " + sensorId + " mistede forbindelsen.");
            try {
                logger.error("Sensor " + sensorId + " disconnected (EOF)");
            } catch (Exception logEx) {
                System.err.println("[LOG ERROR] " + logEx.getMessage());
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Sensor " + sensorId + " mistede forbindelsen.");
            try {
                logger.error("Sensor " + sensorId + " disconnected: " + e.getMessage());
            } catch (Exception logEx) {
                System.err.println("[LOG ERROR] " + logEx.getMessage());
            }
        }
    }

    // Extracted for testing: process a single line and reply via the given PrintWriter.
    public void handleLine(String line, PrintWriter out) {
        try {
            Measurement m = parser.parse(line);
            boolean alarm = checker.isOutOfRange(m);

            // Log the measurement
            try {
                logger.log(m, alarm);
            } catch (Exception logEx) {
                // Logging must not crash the handler
                System.err.println("[LOG ERROR] " + logEx.getMessage());
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
                System.err.println("[LOG ERROR] " + logEx.getMessage());
            }
        }
    }
}
