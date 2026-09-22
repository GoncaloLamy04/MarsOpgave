package org.zealand.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.zealand.contract.Measurement;
import org.zealand.contract.SensorType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileMarsLoggerTest {
    @TempDir
    File tempDir;

    @Test
    void log_normalMeasurement_writesLineWithoutAlarm() throws IOException {
        File file = new File(tempDir, "mars.log");
        FileMarsLogger logger = new FileMarsLogger(file.getPath());

        logger.log(new Measurement(SensorType.TEMP, 20.0), false);

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("TEMP: 20.0"));
        assertFalse(content.contains("ALARM"));
    }

    @Test
    void log_alarmMeasurement_writesLineWithAlarm() throws IOException {
        File file = new File(tempDir, "mars.log");
        FileMarsLogger logger = new FileMarsLogger(file.getPath());

        logger.log(new Measurement(SensorType.CO2, 2500.0), true);

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("ALARM"));
    }

    @Test
    void error_message_writesErrorLine() throws IOException {
        File file = new File(tempDir, "mars.log");
        FileMarsLogger logger = new FileMarsLogger(file.getPath());

        logger.error("Sensor 1 disconnected");

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("ERROR"));
        assertTrue(content.contains("Sensor 1 disconnected"));
    }

    @Test
    void log_calledTwice_appendsSecondLineWithoutOverwriting() throws IOException {
        File file = new File(tempDir, "mars.log");
        FileMarsLogger logger = new FileMarsLogger(file.getPath());

        logger.log(new Measurement(SensorType.TEMP, 20.0), false);
        logger.log(new Measurement(SensorType.O2, 21.0), false);

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("TEMP: 20.0"));
        assertTrue(content.contains("O2: 21.0"));
    }
}
