# Marsbase Monitoring

Multithreaded Java server (Mars HQ) der modtager målinger fra flere sensorklienter, tjekker dem mod grænseværdier, sender alarmer og logger alt til `mars.log`.

Gruppe 4 "Hello World": Nicki, Goncalo, Mattias

## Krav

Java 26 og Maven.

## Kør programmet

TODO når server og klient er færdige

## Kør tests

mvn test

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

TODO: afbrudte klienter, ugyldige linjer, server ikke startet

## AI agent

### Arbejdsgang
Vi brugte Google Antigravity og GitHub Copilot. Regler til agenten ligger i `AGENTS.md`, og hver opgave fik struktur: opgave, kontekst, krav, hvad der ikke må ændres, og hvordan vi tester.

### En opgave vi gav agenten
Implementér `isOutOfRange` i `DefaultThresholdChecker`. Klassen fandtes allerede, og agenten måtte kun rette den ene metode.

### Hvorfor den var afgrænset sådan
Testene var skrevet først (TDD), og agenten måtte ikke ændre dem eller `contract`. Så kunne den ikke få testene grønne ved at "snyde", og den kunne ikke ødelægge de andres arbejde.

### Et forslag vi accepterede
Switch expression over `SensorType` uden `default`. Tilføjes en ny sensortype, kompilerer koden ikke før den er håndteret, så man kan ikke glemme en.

### Et forslag vi ændrede eller afviste
`SimpleParser` fra agenten bestod alle tests, men ved review fandt vi at `TEMP:NaN` slap igennem. NaN giver aldrig alarm, så en ugyldig måling ville blive godkendt stille. Vi skrev en test for det og rettede koden. Checkeren blev også refaktoreret bagefter med navngivne konstanter i stedet for magiske tal.

### Sådan testede vi AI genereret kode
Unit tests skrevet før koden, kørt selv i IntelliJ i stedet for kun at stole på agentens egen rapport. Vi læste diffen og tjekkede med `git status`, at kun de tilladte filer var ændret.

TODO: noter fra server og klient/logger
