# Agent Instructions: Marsbase Monitoring

This is a group assignment in Programmering 2. The goal is not only working code, but also using an AI agent systematically: Understand → Plan → Implement → Test → Review → Improve → Explain.

The agent implements small, clearly scoped parts only. Never build the whole system in one go.

## The assignment

A multithreaded Java server (Mars HQ) receives measurements from several sensor clients, checks them against thresholds, sends alarms and logs everything to `mars.log`.

## Build and run

```bash
mvn compile
mvn test
mvn exec:java -Dexec.mainClass=org.zealand.server.MarsServer
mvn exec:java -Dexec.mainClass=org.zealand.client.SensorClient -Dexec.args="TEMP"
```

(Update if the structure changes.)

## Architecture

Packages:

1. `org.zealand.contract`: `SensorType`, `Measurement`, `MeasurementParser`, `ThresholdChecker`, `MarsLogger`. Shared by everyone.
2. `org.zealand.server`: `MarsServer` (accept loop, `ExecutorService` with 5 threads, wiring in `main`) and `SensorHandler implements Runnable` (one sensor connection).
3. `org.zealand.client`: `SensorClient` (sends a measurement every 5 seconds using `Random`).
4. `org.zealand.logging`: `FileMarsLogger` (`BufferedWriter` with `FileWriter("mars.log", true)`).
5. `org.zealand.parsing`: `SimpleParser`, `DefaultThresholdChecker`.

`SensorHandler` only depends on the interfaces in `contract`, injected through the constructor.

The logger is shared by all threads, so writing must be `synchronized`.

## Protocol

Client sends one line per measurement: `TYPE:value`, e.g. `TEMP:27.4` or `CO2:2350`.

Server replies with one line:

1. `OK`
2. `ALARM: [SENSOR] value out of range! (value = xxx)`
3. `ERROR|message` if the line cannot be parsed

## Thresholds

| Sensor | Alarm when |
|---|---|
| TEMP | < -15 or > 35 °C |
| O2 | < 19 or > 23 % |
| PRESSURE | < 800 or > 1100 hPa |
| CO2 | > 2000 ppm |

The limit values themselves are not alarms.

## Logging

Every message is logged with timestamp and sensor type:

```
[2025-07-18 14:32:01] O2: 22.5
[2025-07-18 14:32:06] CO2: 2100 -> ALARM!
```

## Error handling and resources

1. Use try-with-resources for all sockets and streams.
2. Catch `IOException` and `NumberFormatException`. A bad line must never crash the server.
3. When a sensor disconnects, print `[ERROR] Sensor X mistede forbindelsen.`
4. The server keeps running when one client fails.

## Code style

1. All identifiers and comments in English. User facing messages follow the assignment text.
2. Simple, readable code over clever solutions. The code must be explainable at the exam.
3. Split code into small methods. KISS, no unnecessary complexity.

## Tests

Follow `docs/unit-test-guide.md` (AAA, naming `method_scenario_expectedResult`, one thing per test).

Manual tests as a minimum:

| Test | Expected result |
|---|---|
| One sensor sends normal values | `OK`, logged |
| Three sensors at the same time | All handled and logged |
| `TEMP:40` | ALARM in console, to client and in log |
| `CO2:abc` | Error message, server keeps running |
| Client is stopped (Ctrl+C) | `[ERROR] Sensor X mistede forbindelsen.` |
| Server not started | Understandable error in client |

Quick test without a client: `nc localhost 5000`.

## Git rules

1. Work only on the branch for the current issue (e.g. `3/parsing_thresholds`).
2. Only edit files listed in the issue. Ask before touching others.
3. Never change anything in `contract` without a separate issue.
4. Only `MarsServer.main` wires classes together. Do not edit it outside issue #2.
5. Before starting and before a PR: merge `main` into the current branch.
6. On merge conflicts: stop and show both sides. Never accept theirs/ours blindly.
7. If the branch, files or build look wrong: stop and report. Do not repair something you do not understand.
8. Never commit `.idea/`, `target/` or `mars.log`.

## Working with the agent

Every task given to the agent follows this structure:

```
Opgave: What should be changed?
Kontekst: Which classes are relevant?
Krav: What must the solution do?
Må ikke ændres: What must the agent stay away from?
Test: How do we know it works?
```

Order of work:

1. Server accepts one connection and prints lines
2. Several sensors via the thread pool
3. Parsing of measurements
4. Threshold checks
5. Alarm sent to client and console
6. Logging to file
7. Errors and disconnects tested

After every change: show which files and lines were changed, and explain the change briefly.

## Review mode

When asked for a review, the agent may point out problems in: handling of several clients, `ExecutorService`, sockets and streams, disconnecting clients, logging, thread safety and unnecessary complexity.

In review mode the agent must not change any code. Suggestions only.
