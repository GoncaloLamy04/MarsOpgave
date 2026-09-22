package org.zealand.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MarsServer {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        final int port = 5000;
        System.out.println("[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "] [INFO] Starting MarsServer on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "] [INFO] Server listening on port " + port + ". Accepting connections...");

            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
            int nextSensorId = 1;
            // create a single shared threshold checker and a simple console logger (temporary)
            org.zealand.parsing.DefaultThresholdChecker checker = new org.zealand.parsing.DefaultThresholdChecker();
            org.zealand.contract.MarsLogger logger = new org.zealand.contract.MarsLogger() {
                @Override
                public void log(org.zealand.contract.Measurement measurement, boolean alarm) {
                    String msg = String.format("[%s] [LOG] %s: %s%s",
                            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                            measurement.type(),
                            measurement.value(),
                            alarm ? " -> ALARM!" : "");
                    System.out.println(msg);
                }

                @Override
                public void error(String message) {
                    System.err.println("[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "] [LOG ERROR] " + message);
                }
            };

            try {
                while (true) {
                    Socket client = serverSocket.accept();
                    System.out.println("[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "] [INFO] Accepted connection from " + client.getRemoteSocketAddress());

                    // inject a parser into each handler (parsing is stateless)
                    org.zealand.parsing.SimpleParser parser = new org.zealand.parsing.SimpleParser();

                    SensorHandler handler = new SensorHandler(client, nextSensorId++, parser, checker, logger);
                    executor.submit(handler);
                }
            } catch (IOException e) {
                System.err.println("[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "] [ERROR] Accept failed: " + e.getMessage());
            } finally {
                executor.shutdownNow();
            }

        } catch (IOException e) {
            System.err.println("[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "] [ERROR] Could not start server on port " + port + ": " + e.getMessage());
        }
    }
}
