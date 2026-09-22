package org.zealand.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Simple sensor client used to send a single measurement to the Mars server.
 */
public class SensorClient {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java org.zealand.client.SensorClient <SENSOR_TYPE>");
            return;
        }

        String sensorType = args[0];

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(sensorType + ":20");
            System.out.println(in.readLine());
        }
    }
}
