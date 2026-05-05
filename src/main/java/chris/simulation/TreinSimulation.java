package chris.simulation;

import chris.domain.*;
import org.jspecify.annotations.NonNull;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class TreinSimulation {
    private final Logger logger = Logger.getLogger(TreinSimulation.class.getName());

    private final LocalTime eindExclusiefSimulatieTijd;

    private final TreinStappenPlanner treinStappenPlanner;

    // This will change every minute tick
    private GeheelStatus geheelStatus;

    private final GeheelStatusObserver geheelStatusObserver;

    public TreinSimulation(@NonNull List<Trein> treinen, @NonNull Netwerk netwerk, @NonNull GeheelStatusObserver geheelStatusObserver) {
      this(treinen, new TreinStappenPlanner(netwerk), LocalTime.MIDNIGHT, LocalTime.MAX, geheelStatusObserver) ;
    }

    TreinSimulation(@NonNull List<Trein> treinen, @NonNull TreinStappenPlanner treinStappenPlanner, LocalTime startSimulatieTijd, LocalTime eindExclusiefSimulatieTijd, GeheelStatusObserver geheelStatusObserver) {
        if (treinen.isEmpty()) throw new IllegalArgumentException("Moet tenminste een trein zijn");
        this.treinStappenPlanner = treinStappenPlanner;
        this.geheelStatusObserver = geheelStatusObserver;

        var treinStatus = treinen
                .stream()
                .map(trein -> new TreinStatus(trein, new OpStation(trein.route().getFirst()), 0))
                .toList();
        geheelStatus = new GeheelStatus(startSimulatieTijd, treinStatus);
        this.eindExclusiefSimulatieTijd = eindExclusiefSimulatieTijd;
    }

    public void run() {
        logger.info(" Simulatie aan het starten");
        while (geheelStatus.tijd().isBefore(eindExclusiefSimulatieTijd) && !geheelStatus.alleTreinenOpEindbestemming()) {
            logger.info(() -> """
                    Tijd: %s
                    TreinStatuses:
                    %s
                    """.formatted(
                            geheelStatus.tijd(),
                    geheelStatus.treinStatuses().stream().map(Record::toString).collect(Collectors.joining("\n"))
            ));

            var newPosities = geheelStatus.treinStatuses()
                    .stream()
                    .map(treinStappenPlanner::beginAsyncVolgendePostieBerekenen)
                    .map(x -> {
                        try {
                            return x.get(100, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();


            geheelStatus = geheelStatus.nieuwPosities(newPosities);

            geheelStatusObserver.newStatus(geheelStatus);
        }
    }

}
