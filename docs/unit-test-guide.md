# Unit Test Guide

## Naming
`method_scenario_expectedResult`

Example: `isOutOfRange_tempAbove35_returnsTrue`

## AAA: Arrange, Act, Assert
Every test follows this structure.

```java
@Test
void isOutOfRange_tempAbove35_returnsTrue() {
    // Arrange
    ThresholdChecker checker = new DefaultThresholdChecker();
    Measurement measurement = new Measurement(SensorType.TEMP, 35.1);

    // Act
    boolean result = checker.isOutOfRange(measurement);

    // Assert
    assertTrue(result);
}
```

## TDD: Test Driven Development
Write the test before the code.

1. Write a failing test (red)
2. Write the smallest code that makes it pass (green)
3. Refactor without breaking the test (refactor)

## Without fakes (pure logic)
Used when the method has no external dependencies. Parser and threshold checker belong here.

```java
@Test
void parse_invalidNumber_throwsIllegalArgument() {
    // Arrange
    MeasurementParser parser = new SimpleParser();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> parser.parse("CO2:abc"));
}
```

```java
@Test
void isOutOfRange_tempExactly35_returnsFalse() {
    // Arrange
    ThresholdChecker checker = new DefaultThresholdChecker();
    Measurement measurement = new Measurement(SensorType.TEMP, 35.0);

    // Act
    boolean result = checker.isOutOfRange(measurement);

    // Assert
    assertFalse(result);
}
```

## With fakes (external dependency)
Used when the code depends on network, file system or another team member's class. Replace the dependency with a small fake that implements the interface from `contract`.

```java
class FakeLogger implements MarsLogger {
    List<String> logged = new ArrayList<>();

    public void log(Measurement measurement, boolean alarm) {
        logged.add(measurement.type() + ":" + measurement.value() + ":" + alarm);
    }

    public void error(String message) {
        logged.add("ERROR:" + message);
    }
}
```

```java
@Test
void handleLine_co2Above2000_logsAlarm() {
    // Arrange
    FakeLogger logger = new FakeLogger();
    SensorHandler handler = new SensorHandler(null, new SimpleParser(),
            new DefaultThresholdChecker(), logger);

    // Act
    handler.handleLine("CO2:2350");

    // Assert
    assertEquals("CO2:2350.0:true", logger.logged.get(0));
}
```

Mockito can be used instead of a fake class if the group prefers it.

## Rules of thumb

1. One thing per test. If it fails, you should know exactly what went wrong.
2. Test the limits. Values exactly on the threshold, just above, just below.
3. Test error cases. Empty line, missing colon, unknown type, text instead of a number.
4. Fake the dependencies. Anything using sockets or files is replaced in unit tests.
5. No logic in tests. No if, no loops. Tests should be dumb and direct.
6. Test behaviour, not implementation. Test what the method does, not how.
