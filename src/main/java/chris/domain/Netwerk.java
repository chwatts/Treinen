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

    public List<Verbinding> bestRoute(@NonNull Station van, @NonNull Station naar, int maxSnelheidTreinKmPerMin) {
        var reverseSnelsteRouteNaarElkStation = berekenSnelsteRoutesNaarElkStation(van, naar, maxSnelheidTreinKmPerMin);

        return reverseSnelsteRouteNaarElkStation
                .map(verbindingMap -> vindWerkelijkeRoute(verbindingMap, van, naar))
                .orElse(Collections.emptyList());
    }

    private List<Verbinding> vindWerkelijkeRoute(Map<Station, Verbinding> reverseSnelsteRouteNaarElkStation, @NonNull Station van, @NonNull Station naar) {
        Station current = naar;
        List<Verbinding> reverseRoute = new ArrayList<>();
        while (current != van) {
            Verbinding verbinding = reverseSnelsteRouteNaarElkStation.get(current);
            reverseRoute.add(verbinding);
            current = verbinding.van();
        }

        return reverseRoute.reversed();
    }

    private @NonNull Optional<Map<Station, Verbinding>> berekenSnelsteRoutesNaarElkStation(@NonNull Station van, @NonNull Station naar, int maxSnelheidTreinKmPerMin) {
        // Algoritme van Dijkstra -> snelste route van A naar B
        // Dit wordt gewijzigd alleen een keer per station, als wij een station tegenkom, de eerste keer wij het tegenkom is
        // altijd de snelste.
        Map<Station, Integer> tijdNaarStations =
                Arrays.stream(Station.values()).collect(Collectors.toMap(
                        Function.identity(),
                        (station) -> {
                            if (station == van) return 0;
                            else return Integer.MAX_VALUE;
                        })
                );

        final Map<Station, Verbinding> reverseSnelsteRoute = new HashMap<>();
        // Dit verzekers dat wij de snelste routes altijd volgen, en dat wij op elk station de eerste bezoek
        // is altijd de snelste
        final PriorityQueue<Station> stationsMetSnelsteEerst = new PriorityQueue<>(
                Comparator.comparing(tijdNaarStations::get)
        );
        final Set<Station> stationsGeweest = new HashSet<>();
        stationsMetSnelsteEerst.add(van);

        while (!stationsMetSnelsteEerst.isEmpty()) {
            var huidigeStation = stationsMetSnelsteEerst.poll();
            if (!stationsGeweest.contains(huidigeStation)) {
                stationsGeweest.add(huidigeStation);

                Stream<Verbinding> verbindingStream = verbindingenVan(huidigeStation);
                verbindingStream.forEach(verbinding -> {
                    int tijdNaarStationVanVertrekStation = tijdNaarStations.get(huidigeStation) + minutenNaarBestemming(verbinding, maxSnelheidTreinKmPerMin);
                    if (tijdNaarStations.get(verbinding.naar()) > tijdNaarStationVanVertrekStation) {
                        tijdNaarStations.put(verbinding.naar(), tijdNaarStationVanVertrekStation);
                        reverseSnelsteRoute.put(verbinding.naar(), verbinding);
                        stationsMetSnelsteEerst.add(verbinding.naar());
                    }
                });
            }

        }

        if (tijdNaarStations.get(naar) == Integer.MAX_VALUE) {
            return Optional.empty();
        } else {
            return Optional.of(reverseSnelsteRoute);
        }
    }

    private static int minutenNaarBestemming(Verbinding verbinding, int maxSnelheidTreinKmPerMin) {
        int werkelijkMaxSnelheid = verbinding.werkelijkSnelheid(maxSnelheidTreinKmPerMin);
        return verbinding.afstandKm() / werkelijkMaxSnelheid;
    }

    private Stream<Verbinding> verbindingenVan(@NonNull Station van) {
        return verbindingen.stream().filter( verbinding -> verbinding.van() == van);
    }

}
