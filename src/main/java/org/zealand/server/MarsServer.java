package org.zealand.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Entry point for the Mars HQ server.
 *
 * <p>The server listens on port 5000, accepts sensor connections and
 * dispatches each connection to a {@link org.zealand.server.SensorHandler}
 * running in a fixed thread pool. A single shared {@code ThresholdChecker},
 * {@code MarsLogger} and {@code MeasurementParser} are created and injected
 * into each handler.</p>
 */
public class MarsServer {

    /**
     * Starts the Mars HQ server.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {
        final int port = 5000;
        System.out.println("[INFO] Starting MarsServer on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[INFO] Server listening on port " + port + ". Accepting connections...");

            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
            int nextSensorId = 1;

            // Create shared dependencies injected into each handler
            org.zealand.parsing.DefaultThresholdChecker checker = new org.zealand.parsing.DefaultThresholdChecker();
            org.zealand.contract.MarsLogger logger = new org.zealand.logging.FileMarsLogger("mars.log");
            org.zealand.parsing.SimpleParser parser = new org.zealand.parsing.SimpleParser();

            try {
                while (true) {
                    Socket client = serverSocket.accept();
                    System.out.println("[INFO] Accepted connection from " + client.getRemoteSocketAddress());

                    SensorHandler handler = new SensorHandler(client, nextSensorId++, parser, checker, logger);
                    executor.submit(handler);
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Accept failed: " + e.getMessage());
            } finally {
                executor.shutdownNow();
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Could not start server on port " + port + ": " + e.getMessage());
        }
    }
}
