# Trein Simulatie

Een Java-simulatie van treinplanning op een klein spoorwegnetwerk. De simulatie loopt minuut voor minuut en beweegt meerdere treinen tegelijkertijd langs hun routes, terwijl exclusieve toegang tot gedeelde spoorverbindingen wordt afgedwongen.

## Netwerk

```
 AMSTERDAM ──────────── ALMERE
  │                     │
  └────────────────── WEESP ──── HILVERSUM ──── AMERSFOORT
  │                                  │
  └────────────────────────────── UTRECHT
```

Zeven gerichte verbindingen, elk met een afstand (km) en maximumsnelheid (km/min). Zes treinen rijden tegelijkertijd — een mix van Sprinters en Intercity's.

## Ontwerp

### Domeinmodel

De positie van een trein wordt gemodelleerd als een sealed interface met vier toestanden:

| Toestand | Betekenis |
|---|---|
| `OpStation` | Gestopt op een gepland halte |
| `OpStationMaarStoptNiet` | Rijdt door een netwerkknooppunt dat niet op de dienstregeling staat |
| `Onderweg` | Rijdt over een verbinding |
| `EindBestemmingBereikt` | Aangekomen op de eindbestemming |

Deze toestanden gaan elke simulatietick over via pattern-matching switch-expressies. Omdat de interface sealed is, dwingt de compiler volledige afhandeling af — geen default-case nodig.

Kerndomaintypes (`Trein`, `Verbinding`, `TreinStatus`, `Onderweg`, enz.) zijn onveranderlijke Java-records met compacte constructorvalidatie.

### Simplificaties

Alle afstanden en snelheden zijn integers (km en km/min) en de afstanden kunnen in zijn geheel gedeeld worden 
met de snelheden zonder fracties. 

Een trein mag op een verbinding, andere treinen moeten wachten.

De treinen gaan alleen een kant op, ze komen niet terug.  De simulatie beeindigt als alle treinen 
zijn op hun EindBestemming.

### Routering

Wanneer een trein een station verlaat, roept hij `Netwerk.bestRoute()` aan, dat **het algoritme van Dijkstra** (https://nl.wikipedia.org/wiki/Kortstepad-algoritme) uitvoert om het snelste pad naar het volgende waypoint te vinden, rekening houdend met zowel de eigen maximumsnelheid van de trein als de snelheidslimiet van elke verbinding. Alleen de eerste verbinding uit het resultaat wordt gebruikt — het algoritme wordt opnieuw uitgevoerd bij elke tussenstop.

### Gelijktijdigheid

Elke verbinding heeft een `Semaphore(1)`. Wanneer een trein een verbinding wil betreden, roept hij `tryAcquire()` aan — een niet-blokkerende poging. Als de verbinding bezet is, registreert de trein een minuut vertraging en probeert het de volgende tick opnieuw. De verbinding wordt vrijgegeven zodra de trein aan het andere eind aankomt.

Elke simulatietick dient alle treinstapberekeningen in als virtual thread-taken, verzamelt de futures en wacht ze vervolgens af — zodat treinen hun volgende positie parallel berekenen voordat de simulatieklok verder gaat.

### Observer

`TreinSimulatie` accepteert een `GeheelStatusObserver`-callback die na elke tick wordt aangeroepen. Dit houdt de simulatielus ontkoppeld van uitvoer of andere presentatielogica.

## Projectstructuur

```
src/
├── main/java/chris/
│   ├── Main.java
│   ├── domain/          # Onveranderlijk model: treinen, verbindingen, posities, netwerk
│   └── simulation/      # Simulatielus, stappenplanner, observer-interface
└── test/java/chris/
    ├── domain/          # Unittests voor routering, positielogica en validatie
    └── simulation/      # Tests voor stappenplanner en simulatielevenscyclus
```

## Bouwen en uitvoeren

Vereist Java 21+ en Maven.

```bash
mvn compile exec:java -Dexec.mainClass=chris.Main
```

Tests uitvoeren:

```bash
mvn test
```

## Gebruikte Java-functies

- **Sealed interfaces** met uitputtende pattern-matching switch
- **Records** voor onveranderlijke domeinobjecten
- **Virtual threads** (`Executors.newVirtualThreadPerTaskExecutor()`) voor parallelle stapberekeningen
- **JSpecify** `@NonNull` / `@Nullable`-annotaties door de hele codebase
- **Algoritme van Dijkstra** op een graaf van `Station`-knopen en `Verbinding`-kanten
- **Semaphore**-gebaseerde wederzijdse uitsluiting voor gedeelde spoortrajecten
