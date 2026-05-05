package chris.simulation;


import chris.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

class TreinSimulationTest {
    private static final int HOUR = 10;
    private static final int START_MINUUT = 30;
    private final int nummerStappen = 3;

    private final TreinStappenPlanner mockTreinStappenPlanner = mock(TreinStappenPlanner.class);
    private final GeheelStatusObserver mockGeheelStatusObserver = mock(GeheelStatusObserver.class);

    private final Trein trein1 = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP, Station.ALMERE));
    private final Trein trein2 = new Trein(TreinSoort.SPRINTER, List.of(Station.HILVERSUM, Station.WEESP));

    private final TreinSimulation victim = new TreinSimulation(
            List.of(trein1, trein2),
            mockTreinStappenPlanner,
            tijd(START_MINUUT),
            tijd(START_MINUUT + nummerStappen),
            mockGeheelStatusObserver);

    private static LocalTime tijd(int minute) {
        return LocalTime.of(HOUR, minute);
    }

    // volgorde moet consequent blijven zodat de tests kloppen, daarom LinkedHashMap
    final Map<Trein, List<TreinPositie>> expectedStatuses = new LinkedHashMap<>();

    TreinSimulationTest() {
        expectedStatuses.put(
                trein1,
                List.of(
                        new OpStation(Station.AMSTERDAM),
                        new OpStation(Station.WEESP),
                        new Onderweg(new Verbinding(Station.WEESP, Station.ALMERE, 20, 100), 10, Station.ALMERE),
                        new OpStation(Station.ALMERE)
                ));
        expectedStatuses.put(
                trein2,
                List.of(
                        new OpStation(Station.HILVERSUM),
                        new Onderweg(new Verbinding(Station.HILVERSUM, Station.WEESP, 20, 100), 5, Station.WEESP),
                        new Onderweg(new Verbinding(Station.HILVERSUM, Station.WEESP, 20, 100), 15, Station.WEESP),
                        new OpStation(Station.WEESP)
                ));
    }

    @Test
    void simulatieStoptVroegAlsAlleTreinenEindbestemmingBereiken() {
        when(mockTreinStappenPlanner.beginAsyncVolgendePostieBerekenen(
                new TreinStatus(trein1, new OpStation(Station.AMSTERDAM), 0)))
                .thenReturn(CompletableFuture.completedFuture(
                        new TreinStatus(trein1, new EindBestemmingBereikt(Station.ALMERE), 0)));
        when(mockTreinStappenPlanner.beginAsyncVolgendePostieBerekenen(
                new TreinStatus(trein2, new OpStation(Station.HILVERSUM), 0)))
                .thenReturn(CompletableFuture.completedFuture(
                        new TreinStatus(trein2, new EindBestemmingBereikt(Station.WEESP), 0)));

        victim.run();

        // observer called once only — simulation stopped after the first step
        verify(mockGeheelStatusObserver, times(1)).newStatus(any());
    }

    @Test
    void simulatieIsVoorAlleTreinenVanStartToBeginInEenMinnutStappen() {
        IntStream.range(0, nummerStappen - 1).forEach(this::treinPlannerInstellingVoorStap);

        victim.run();

        IntStream.range(1, nummerStappen).forEach(this::geheelStatusVerwachtingenVoorStap);
    }

    private void treinPlannerInstellingVoorStap(int stapNummer) {
        expectedStatuses.forEach((trein, treinPositions) ->
        {
            TreinPositie huidigePositie = treinPositions.get(stapNummer);
            TreinPositie nextPositie = treinPositions.get(stapNummer + 1);
            when(
                    mockTreinStappenPlanner.beginAsyncVolgendePostieBerekenen(new TreinStatus(trein, huidigePositie, 0))
            )
                    .thenReturn(CompletableFuture.completedFuture(new TreinStatus(trein, nextPositie, 0)));
        });
    }

    private void geheelStatusVerwachtingenVoorStap(int stapNummer) {
        var verwachtTreinStatuses = expectedStatuses
                .entrySet()
                .stream()
                .map(treinListEntry ->
                        new TreinStatus(treinListEntry.getKey(), treinListEntry.getValue().get(stapNummer), 0)
                )
                .toList();

        verify(mockGeheelStatusObserver).newStatus(
                new GeheelStatus(
                        tijd(START_MINUUT + stapNummer),
                        verwachtTreinStatuses
                )
        );
    }
}