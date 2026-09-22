# Klient og logger, trin for trin

Test og commit efter hver bid. Ikke lav det hele på én gang.

Før du sender en prompt: læs den igennem og sig kort, med egne ord, hvad du forventer den gør. Så sender du den. Det er ikke for at bremse dig, det er så du selv kan forklare koden bagefter, uden det tager ekstra tid.

## Del 0: hent branchen

Branchen `client_logger` er allerede lavet og pushet. Hent den ned:

```
git fetch
git switch client_logger
git pull
```

Tjek at du står det rigtige sted:

```
git status
```

Der skal stå `On branch client_logger`.

## Commit-beskeder

Skriv hvad du faktisk lavede, ikke "opdatering" eller "fix". Dansk er fint, samme som resten af gruppen bruger:

```
git commit -m "Tilføj normal logning til FileMarsLogger"
```

ikke

```
git commit -m "lol virker nu"
```

Kort og præcist er nok, du behøver ikke en roman.

## Del 1: FileMarsLogger, normal logning

Opret `src/test/java/org/zealand/logging/FileMarsLoggerTest.java`:

```java
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
}
```

Kør testen, den fejler, det er meningen (klassen findes ikke endnu).

Det er helt fint at bede Copilot om at skrive selve testen for dig også, hvis du synes det er nemmere. Bare vær sikker på du forstår hvad den tester, det er den del der tæller.

Prompt til Copilot:

```
Følg reglerne i copilot-instructions.md.

Opgave:
Implementér log i FileMarsLogger for det normale tilfælde (alarm = false). Skriv IKKE alarm-delen endnu.

Kontekst:
MarsLogger og Measurement ligger i org.zealand.contract. Testen findes allerede: log_normalMeasurement_writesLineWithoutAlarm.

Krav:
Skriv til mars.log med BufferedWriter og try-with-resources.
Format: [yyyy-MM-dd HH:mm:ss] TYPE: value

Må ikke ændres:
Ingen filer i contract eller andre pakker.

Test:
Testen skal blive grøn.
```

Kør testen igen, grøn nu. `git add src`, commit, push.

Forslag til besked: `Tilføj normal logning til FileMarsLogger`

## Del 2: alarm-delen

Tilføj i samme fil:

```java
@Test
void log_alarmMeasurement_writesLineWithAlarm() throws IOException {
    File file = new File(tempDir, "mars.log");
    FileMarsLogger logger = new FileMarsLogger(file.getPath());

    logger.log(new Measurement(SensorType.CO2, 2500.0), true);

    String content = Files.readString(file.toPath());
    assertTrue(content.contains("ALARM"));
}
```

Prompt:

```
Følg reglerne i copilot-instructions.md.

Opgave:
Udvid log i FileMarsLogger, så alarm-tilfældet også håndteres. Klassen findes allerede.

Krav:
Når alarm er true, tilføjes " -> ALARM!" til linjen.

Må ikke ændres:
Ingen filer i contract. Ingen ændring af den eksisterende test.

Test:
Begge tests skal være grønne.
```

Kør begge tests, commit, push.

Forslag til besked: `Tilføj alarm-håndtering til FileMarsLogger`

## Del 3: error()

Tilføj:

```java
@Test
void error_message_writesErrorLine() throws IOException {
    File file = new File(tempDir, "mars.log");
    FileMarsLogger logger = new FileMarsLogger(file.getPath());

    logger.error("Sensor 1 disconnected");

    String content = Files.readString(file.toPath());
    assertTrue(content.contains("ERROR"));
    assertTrue(content.contains("Sensor 1 disconnected"));
}
```

Prompt:

```
Følg reglerne i copilot-instructions.md.

Opgave:
Implementér error i FileMarsLogger. Klassen findes allerede.

Krav:
Skriv til mars.log: [yyyy-MM-dd HH:mm:ss] ERROR: message

Må ikke ændres:
Ingen filer i contract eller andre pakker.

Test:
Testen skal blive grøn.
```

Kør alle tre tests, commit, push. Loggeren er færdig.

Forslag til besked: `Tilføj error-logning til FileMarsLogger`

## Del 3b: flere linjer (append)

Én ting mangler, samme princip som Nickis parser-tests (test mere end bare happy path): tjekker vi at loggeren skriver til filen i append-mode, ikke overskriver den hver gang?

```java
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
```

Denne test burde allerede være grøn, hvis `log` blev implementeret rigtigt i del 1 (med `FileWriter(path, true)` for append). Er den rød, er det et rigtigt fund, ret koden i stedet for testen.

`git add src`, commit, push.

Forslag til besked: `Tilføj test for append-logning i FileMarsLogger`

## Del 4: SensorClient, forbindelse

Ingen unit tests her, det er en rigtig socket. Test manuelt i stedet.

Start `MarsServer` i IntelliJ (Run), lad den køre i baggrunden.

Prompt:

```
Følg reglerne i copilot-instructions.md.

Opgave:
Byg kun forbindelsesdelen af SensorClient. Ny klasse i org.zealand.client.

Krav:
main tager sensortypen som argument.
Forbind til localhost:5000 med Socket, brug try-with-resources.
Send én linje "TYPE:20" og print serverens svar. Luk forbindelsen igen. Ikke løkke endnu, ikke fejlhåndtering endnu.

Må ikke ændres:
Ingen andre filer.

Test:
Vis koden.
```

Test manuelt: kør `SensorClient` med argument `TEMP`, skal printe `OK` og lukke. Commit, push.

Forslag til besked: `Tilføj forbindelsesdel til SensorClient`

## Del 5: løkken

Prompt:

```
Følg reglerne i copilot-instructions.md.

Opgave:
Udvid SensorClient med løkken. Klassen findes allerede.

Krav:
Hvert 5. sekund: generér en tilfældig værdi med Random for den valgte sensortype, send den, print svaret. Kør uendeligt (while true).

Må ikke ændres:
Ingen andre filer.

Test:
Vis koden.
```

Test manuelt: lad den køre 15-20 sekunder. Commit, push.

Forslag til besked: `Tilføj løkke til SensorClient, sender måling hvert 5. sekund`

## Del 6: fejlhåndtering

Prompt:

```
Følg reglerne i copilot-instructions.md.

Opgave:
Tilføj fejlhåndtering til SensorClient, hvis serveren ikke kører ved opstart.

Krav:
Hvis forbindelsen fejler ved start, print en forståelig fejlbesked i stedet for et Java stacktrace.

Må ikke ændres:
Ingen andre filer.

Test:
Vis koden.
```

Test manuelt: LUK serveren, kør klienten, tjek fejlbeskeden giver mening. Commit, push.

Forslag til besked: `Tilføj fejlhåndtering til SensorClient ved manglende serverforbindelse`

## Til sidst

Lav PR med `Closes #1` i beskrivelsen. Brug fx denne skabelon:

```
Closes #1

FileMarsLogger: skriver til mars.log med timestamp, alarm-markering og error-beskeder.
SensorClient: sender en tilfældig måling hvert 5. sekund og printer serverens svar. Fejler pænt hvis serveren ikke kører.

Tests: [antal] tests til FileMarsLogger, alle grønne. Klienten testet manuelt mod den kørende server.

AI: Copilot implementerede begge klasser ud fra tests og trinvise prompts. [skriv om noget blev rettet eller accepteret undervejs, fx: agenten glemte først try-with-resources, det fik vi rettet / vi accepterede formatet den foreslog uden ændringer]
```

Ret `[antal]` og AI-linjen til det der faktisk skete hos dig. Send linket i Discord.

Husk også: gå ind på issue #1 og sæt flueben på de punkter du rent faktisk har opfyldt, før du sender PR'en.

## README

Skriv din egen del i README's AI-sektion, samme sted som Nicki og Goncalos afsnit. Ligger i `readme` branchen (`git fetch`, `git switch readme`, `git pull`), og skal indeholde:

1. En opgave du gav agenten (en af prompterne herover er fint)
2. Hvorfor den var afgrænset sådan
3. Et forslag du accepterede
4. Et forslag du ændrede eller afviste, hvis der var noget
5. Hvordan du testede det (loggerens tests, klientens manuelle test)

Commit og push på `readme`, ikke på `client_logger`.