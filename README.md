# Marsbase Monitoring

Multithreaded Java server (Mars HQ) der modtager målinger fra flere sensorklienter, tjekker dem mod grænseværdier, sender alarmer og logger alt til `mars.log`.

Gruppe 4 "Hello World": Nicki, Goncalo, Mattias

## Krav

Java 17 eller nyere og Maven.

## Kør programmet

### Server (Goncalo)
I IntelliJ: åbn `MarsServer.java` og tryk på den grønne pil ved `main`-metoden, eller højreklik på filen → **Run 'MarsServer.main()'**.

Eller fra terminalen:
```bash
mvn compile
mvn exec:java -Dexec.mainClass=org.zealand.server.MarsServer
```

### Sensorklient (Mattias)
I IntelliJ: åbn SensorClient.java og tryk på den grønne pil ved main, eller højreklik → Run. Vælg sensortype ved opstart eller giv den som argument (TEMP, O2, PRESSURE, CO2).

### GUI dashboard (bonus)
Åbn `SensorDashboard.java` i `org.zealand.gui` og tryk på den grønne pil ved `main`, eller højreklik → Run.

## Kør tests

```bash
mvn test
```

## Arkitektur

| Pakke | Ansvar |
|---|---|
| `org.zealand.contract` | Fælles interfaces og typer, som alle klasser koder op imod |
| `org.zealand.server` | `MarsServer` tager imod forbindelser, `SensorHandler` håndterer én sensor i en tråd fra poolen |
| `org.zealand.parsing` | `SimpleParser` laver en linje om til en måling, `DefaultThresholdChecker` tjekker grænser |
| `org.zealand.logging` | `FileMarsLogger` skriver til `mars.log` |
| `org.zealand.client` | `SensorClient` sender en måling hvert 5. sekund |

Vi aftalte interfaces i `contract` først, så vi kunne arbejde parallelt på hver vores branch uden at vente på hinanden.

## Protokol

Klient sender én linje pr. måling: `TYPE:value`, fx `TEMP:27.4`

Server svarer med én linje:

| Svar | Hvornår |
|---|---|
| `OK` | Målingen er inden for grænserne |
| `ALARM: TEMP value out of range! (value = 40.2)` | Målingen er uden for grænserne |
| `ERROR\|besked` | Linjen kunne ikke læses |

## Grænseværdier

| Sensor | Alarm når |
|---|---|
| TEMP | under −15 eller over 35 °C |
| O2 | under 19 eller over 23 % |
| PRESSURE | under 800 eller over 1100 hPa |
| CO2 | over 2000 ppm |

Grænseværdierne selv giver ikke alarm.

## Fejlhåndtering

### Ugyldige linjer (Nicki)
`SimpleParser` kaster `IllegalArgumentException` ved tomme linjer, forkert format, ukendt sensortype, tekst i stedet for tal samt `NaN` og `Infinity`.

### Server og afbrudte klienter (Goncalo)
Serveren lytter på port 5000 og accepterer sensorforbindelser i et evigt loop, som bliver dispatchet til SensorHandler i en fast threadpool på 5 tråde. Afbrudte klienter fanges og logges uden at påvirke de øvrige forbindelser.

### Klient og logger (Mattias)
TODO

## Manuel test af serveren

Uden færdig klient testede vi serveren med en lille PowerShell TCP klient, der sendte:

| Linje | Svar |
|---|---|
| `TEMP:20` | `OK` |
| `TEMP:40` | `ALARM: TEMP value out of range! (value = 40.0)` |
| `O2:18` | `ALARM: O2 value out of range! (value = 18.0)` |
| `CO2:2500` | `ALARM: CO2 value out of range! (value = 2500.0)` |
| `CO2:abc` | `ERROR|Invalid numeric value: abc` |
| `HUMIDITY:50` | `ERROR|Unknown sensor type: HUMIDITY` |
| `TEMP:NaN` | `ERROR|Value must be a finite number: NaN` |

Serveren fortsatte efter ugyldige linjer, og ved afbrydelse skrev den `[ERROR] Sensor 1 mistede forbindelsen.`
Tre klienter forbundet samtidig blev håndteret parallelt af trådpoolen.

## AI agent

### Fælles arbejdsgang
Vi brugte Google Antigravity og GitHub Copilot. Regler til agenten ligger i `AGENTS.md`, og hver opgave fik struktur: opgave, kontekst, krav, hvad der ikke må ændres, og hvordan vi tester. Efter hver ændring læste vi diffen, kørte testene selv og tjekkede med `git status`, at kun de tilladte filer var ændret.

### Parser og grænseværdier (Nicki)

**Opgave til agenten:** Implementér `isOutOfRange` i `DefaultThresholdChecker`. Klassen fandtes allerede, og agenten måtte kun rette den ene metode.

**Hvorfor afgrænset sådan:** Testene var skrevet først (TDD), og agenten måtte ikke ændre dem eller `contract`. Så kunne den ikke få testene grønne ved at "snyde", og den kunne ikke ødelægge de andres arbejde.

**Accepteret:** Switch expression over `SensorType` uden `default`. Tilføjes en ny sensortype, kompilerer koden ikke før den er håndteret.

**Ændret:** `SimpleParser` fra agenten bestod alle tests, men ved review fandt vi at `TEMP:NaN` slap igennem. NaN giver aldrig alarm, så en ugyldig måling ville blive godkendt stille. Vi skrev en test for det og rettede koden. Ved review med Claude blev begge klasser også refaktoreret med navngivne konstanter og små hjælpemetoder.

**Test:** 16 tests til checkeren og 13 til parseren, skrevet før koden. 7 checker tests var røde før implementeringen og alle grønne efter. Kørt i IntelliJ, ikke kun ud fra agentens egen rapport.

### Server (Goncalo)

**Opgave til agenten:** Vi gav agenten en opgave om at overskrive vores test logger med vores rigtig FileMarsLogger, så systemet kunne virke efter vi alle merget vores issues.

**Hvorfor afgrænset sådan:** Instruktionerne var afgrænset til at den ikke skulle ændre noget logik, den skulle følge copilot.instruction.md filen og får erstattet testlogger med FileMarsLogger.

**Accepteret:** Et forslag vi accepterede var uni-test med en fakelogger og en testlogger i MarsServer, så vi kunne teste programmet.

**Ændret eller afvist:** Et forslag som vi ændrede var at dele SensorHandler klassen i to metoder, fordi copilot havde lavet et langt metode, som sagtens kunne deles op både for Clean Code og testning af programmet.

**Test:** 16 test til checkeren, 11 test til parseren og 3 test til sensorhandleren som blev kørt før hele koden blev implementeret. Tests blev tjekket med mvn test i terminalen.


### Sensorklient og logger (Mattias)

**Opgave til agenten:** Den skulle implementere `SensorClient` og `FileMarsLogger` og ingen af klasserne fandtes i forvejen.
**Hvorfor afgrænset sådan:** Test blev skrevet før agenten blev promptet, så den kunne ikke snyde sig til grønne tests. Den måtte heller ikke ændre `contract`.
**Accepteret:** Accepteret måden den opstillede klienten på, med en `while`-løkke der sender målinger hvert 5. sekund. Loggeren deles via dependency injection, samme instans injiceret i alle handlers.
**Ændret eller afvist:** Ændret at man skulle vælge sensor før SensorClient startede, da agenten kørte det uden et par gange før den forstod prompten.
**Test:** 4 tests blev kørt, 2 af dem var røde før implementeringen og alle grønne efter. Kørt i IntelliJ, ikke kun ud fra agentens egen rapport.

### Review af hele systemet

Da server-delen var på plads, bad vi agenten reviewe `MarsServer`, `SensorHandler` og de tilhørende klasser uden at ændre koden, som opgaven beskriver under "Brug AI som reviewer".

**Accepteret:**
- Genbrug én `SimpleParser` instans i stedet for en ny pr. klient (parseren er tilstandsløs, ligesom checker og logger allerede er delt)
- Saml den duplikerede "mistede forbindelsen"-besked i `SensorHandler` i en hjælpemetode
- Eksplicit UTF-8 charset på streams, i stedet for platformens standard

**Fravalgt/noteret:**
- Graceful shutdown med `awaitTermination`: uden for opgavens krav, men noteret som noget vi kunne have tilføjet