package org.zealand.server;

import org.zealand.contract.MarsLogger;
import org.zealand.contract.Measurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakeLogger implements MarsLogger {
    private final List<Measurement> measurements = new ArrayList<>();
    private final List<Boolean> alarms = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    @Override
    public synchronized void log(Measurement measurement, boolean alarm) {
        measurements.add(measurement);
        alarms.add(alarm);
    }

    @Override
    public synchronized void error(String message) {
        errors.add(message);
    }

    public synchronized List<Measurement> getMeasurements() {
        return Collections.unmodifiableList(new ArrayList<>(measurements));
    }

    public synchronized List<Boolean> getAlarms() {
        return Collections.unmodifiableList(new ArrayList<>(alarms));
    }

    public synchronized List<String> getErrors() {
        return Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public synchronized void clear() {
        measurements.clear();
        alarms.clear();
        errors.clear();
    }
}
