package org.zealand.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;

/**
 * Sends random measurements to the Mars server every five seconds and prints the reply.
 */
public class SensorClient {
    public static void main(String[] args) throws InterruptedException {
        String sensorType = args.length > 0 ? args[0] : "TEMP";
        if (args.length == 0) {
            System.out.println("No sensor type provided; defaulting to TEMP");
        }

        Random random = new Random();

        while (true) {
            double value;
            switch (sensorType) {
                case "TEMP" -> value = -20 + random.nextDouble() * 70;
                case "O2" -> value = 10 + random.nextDouble() * 20;
                case "PRESSURE" -> value = 700 + random.nextDouble() * 500;
                case "CO2" -> value = random.nextDouble() * 5000;
                default -> value = random.nextDouble() * 100;
            }

            String payload = sensorType + ":" + value;
            System.out.println("Sending: " + payload);

            try (Socket socket = new Socket("localhost", 5000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(payload);
                System.out.println("Reply: " + in.readLine());
            } catch (ConnectException e) {
                System.out.println("Could not connect to Mars server on localhost:5000");
                return;
            } catch (IOException e) {
                System.out.println("I/O error: " + e.getMessage());
                return;
            }

            Thread.sleep(5000);
        }
    }
}
