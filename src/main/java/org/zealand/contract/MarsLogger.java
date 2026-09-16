package org.zealand.contract;

public interface MarsLogger {
    void log(Measurement measurement, boolean alarm);
    void error(String message);
}
