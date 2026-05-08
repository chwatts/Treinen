package chris.domain;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Een netwerk bestaat uit meerdere verbindingen tussen stations.
 * Het netwerk garandeert dat een verbinding uitsluitend voor een trein is.
 * Het kan ook berekenen wat snelste route is tussen stations, het gebruikt het algoritme van Dijkstra voor dit.
 */
public class Netwerk {
    private final List<Verbinding> verbindingen;

    private final Map<Verbinding, Semaphore> verbindingSloten;

    public Netwerk(@NonNull List<Verbinding> verbindingen) {
        this.verbindingen = verbindingen;
        this.verbindingSloten = verbindingen.stream().collect(Collectors.toMap(Function.identity(), _ -> new Semaphore(1)));
    }

    public boolean pakVerbinding(Verbinding verbinding) {
        return verbindingSloten.get(verbinding).tryAcquire();
    }

    public void geefVerbindingTerug(Verbinding verbinding) {
        verbindingSloten.get(verbinding).release();
    }

    /**
     * Berekent de beste route, als er geen route is dan de List leeg.
     * @param begin Begin Station
     * @param eind Eind Station
     * @param maxSnelheidTreinKmPerMin Hoe snel de trein kan rijden
     * @return Een List met al de verbindingen nodig om van 'van' naar 'naar' te rijden
     */
    public @NonNull List<Verbinding> bestRoute(@NonNull Station begin, @NonNull Station eind, int maxSnelheidTreinKmPerMin) {
        var stationsMetLaatsteVerbindingVanSnelsteRoute = berekenSnelsteRoutesNaarElkStation(begin, eind, maxSnelheidTreinKmPerMin);

        return stationsMetLaatsteVerbindingVanSnelsteRoute
                .map(verbindingMap -> vindWerkelijkeRoute(verbindingMap, begin, eind))
                .orElse(Collections.emptyList());
    }

    private @NonNull List<Verbinding> vindWerkelijkeRoute(
            @NonNull Map<Station, Verbinding> stationsMetLaatsteVerbindingVanSnelsteRoute,
            @NonNull Station begin,
            @NonNull Station eind) {
        // Omdat wij alleen de laatste verbindingen hebben, moeten achteruit, van het laatste station naar het begin
        // wij volgen de Verbindingen achteruit
        Station current = eind;
        List<Verbinding> achteruitRoute = new ArrayList<>();
        while (current != begin) {
            Verbinding verbinding = stationsMetLaatsteVerbindingVanSnelsteRoute.get(current);
            achteruitRoute.add(verbinding);
            current = verbinding.van();
        }

        return achteruitRoute.reversed();
    }

    private @NonNull Optional<Map<Station, Verbinding>> berekenSnelsteRoutesNaarElkStation(
            @NonNull Station begin,
            @NonNull Station eind,
            int maxSnelheidTreinKmPerMin) {
        // Algoritme van Dijkstra -> snelste route van `begin` naar `eind`

        // De Map waar wij verzamelen de snelste minuten dat wij vinden naar elk station
        final Map<Station, Integer> minutenNaarStations =
                Arrays.stream(Station.values()).collect(Collectors.toMap(
                        Function.identity(),
                        (station) -> {
                            if (station == begin) return 0;
                            else return Integer.MAX_VALUE;
                        })
                );

        // Dit is een map van elk station en de laatste verbinding die ernaartoe rijdt, die deel is van een snelste route
        final Map<Station, Verbinding> snelsteRouteStationEnLaatsteVerbinding = new HashMap<>();

        // Wij gaan door elk station in het netwerk, maar in volgorde van snelste eerst
        // Dat betekent dat als wij het station nemen van deze queue, dan zijn er geen andere sneller manieren om hier te komen
        final PriorityQueue<StationEnTijd> stationsMetSnelsteEerst = new PriorityQueue<>(
                Comparator.comparing(StationEnTijd::minuten)
        );

        stationsMetSnelsteEerst.add(new StationEnTijd(begin, 0));

        while (!stationsMetSnelsteEerst.isEmpty()) {
            final var huidigeStationEnTijd = stationsMetSnelsteEerst.poll();
            if (huidigeStationEnTijd.station() == eind) {
                // Wij zijn er op de snelste tijd, wij kunnen alvast stoppen!
                break;
            }
            verbindingenVan(huidigeStationEnTijd.station())
                    .forEach(verbinding -> {
                        int minutenNaarStationVanVertrekStation = huidigeStationEnTijd.minuten() + minutenTotEindVerbinding(verbinding, maxSnelheidTreinKmPerMin);
                        if (minutenNaarStations.get(verbinding.naar()) > minutenNaarStationVanVertrekStation) {
                            minutenNaarStations.put(verbinding.naar(), minutenNaarStationVanVertrekStation);
                            snelsteRouteStationEnLaatsteVerbinding.put(verbinding.naar(), verbinding);
                            stationsMetSnelsteEerst.add(new StationEnTijd(verbinding.naar(), minutenNaarStationVanVertrekStation));
                        }
                    });
        }

        if (minutenNaarStations.get(eind) == Integer.MAX_VALUE) {
            return Optional.empty();
        } else {
            return Optional.of(snelsteRouteStationEnLaatsteVerbinding);
        }
    }

    private static int minutenTotEindVerbinding(Verbinding verbinding, int maxSnelheidTreinKmPerMin) {
        int werkelijkMaxSnelheid = verbinding.werkelijkSnelheid(maxSnelheidTreinKmPerMin);
        return verbinding.afstandKm() / werkelijkMaxSnelheid;
    }

    private Stream<Verbinding> verbindingenVan(@NonNull Station begin) {
        return verbindingen.stream().filter(verbinding -> verbinding.van() == begin);
    }

    private record StationEnTijd(Station station, int minuten) {
    }
}
