package org.zealand.contract;

public interface ThresholdChecker {
    boolean isOutOfRange(Measurement measurement);
}
