package org.zealand.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zealand.parsing.DefaultThresholdChecker;
import org.zealand.parsing.SimpleParser;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class SensorHandlerTest {
    private FakeLogger fakeLogger;
    private SensorHandler handler;

    @BeforeEach
    public void setUp() {
        fakeLogger = new FakeLogger();
        SimpleParser parser = new SimpleParser();
        DefaultThresholdChecker checker = new DefaultThresholdChecker();
        // create handler with null socket (we won't call run)
        handler = new SensorHandler(null, 42, parser, checker, fakeLogger);
    }

    @Test
    public void validLine_underThreshold_logsWithoutAlarm() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw, true);

        handler.handleLine("TEMP:20", out);

        assertEquals(1, fakeLogger.getMeasurements().size());
        assertEquals(1, fakeLogger.getAlarms().size());
        assertFalse(fakeLogger.getAlarms().get(0));
    }

    @Test
    public void validLine_overThreshold_logsWithAlarm() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw, true);

        handler.handleLine("TEMP:40", out);

        assertEquals(1, fakeLogger.getMeasurements().size());
        assertEquals(1, fakeLogger.getAlarms().size());
        assertTrue(fakeLogger.getAlarms().get(0));
    }

    @Test
    public void invalidLine_callsLoggerError_andNotThrow() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw, true);

        assertDoesNotThrow(() -> handler.handleLine("CO2:abc", out));

        assertEquals(0, fakeLogger.getMeasurements().size());
        assertEquals(1, fakeLogger.getErrors().size());
        String errMsg = fakeLogger.getErrors().get(0);
        assertTrue(errMsg.contains("bad line") || errMsg.contains("Invalid"));

        String outStr = sw.toString();
        assertTrue(outStr.startsWith("ERROR|"));
    }

    @Test
    void handleLine_loggerThrows_writesToServerErrorsLog() throws IOException {
        Files.deleteIfExists(Path.of("server-errors.log"));
        // Arrange: brug en FakeLogger hvis log() kaster en RuntimeException
        // Kald handleLine med en gyldig linje

        String content = Files.readString(Path.of("server-errors.log"));
        assertTrue(content.contains("Logging failed"));
    }
}
