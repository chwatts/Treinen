package chris.simulation;

import chris.domain.*;
import chris.domain.treinpositie.*;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * De Planner berekent de volgende positie van elke trein tegelijk, het gebruikt "Virtual Threads",
 * meer om het realistischer te maken, dan dat het nodig hier is.
 */
class TreinStappenPlanner {
    private final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
    private final @NonNull Netwerk netwerk;
    private final Logger logger = Logger.getLogger(TreinStappenPlanner.class.getName());
    TreinStappenPlanner(@NonNull Netwerk netwerk) {
        this.netwerk = netwerk;
    }

    @NonNull Future<TreinStatus> beginAsyncVolgendePostieBerekenen(@NonNull TreinStatus status) {
        return exec.submit(() -> berekenVolgendePositie(status));
    }

    @NonNull TreinStatus berekenVolgendePositie(@NonNull TreinStatus status) {
        final Optional<TreinPositie> misschienNieuwePositie;
        switch (status.treinPositie()) {
            case Onderweg onderweg ->
                misschienNieuwePositie = berekenNieuwePositieEnGeefVerbindingTerugAlsHetAankomt(status, onderweg);

            case OpStation opStation -> {
                var volgend = status.trein().volgendStation(opStation.station());
                misschienNieuwePositie = volgend
                        .map(station -> probeerOpVerbindingTeKomen(status.trein(), opStation.station(), station))
                        .orElse(Optional.of(new EindBestemmingBereikt(opStation.station())));
            }

            case EindBestemmingBereikt _ -> {
                logger.fine(status.trein() + " nu op eind bestemming.");
                return status;
            }

            case OpStationMaarStoptNiet opStationMaarStoptNiet -> {
                logger.fine(status.trein() + " rijdt door " + opStationMaarStoptNiet.station());
                misschienNieuwePositie = probeerOpVerbindingTeKomen(
                        status.trein(),
                        opStationMaarStoptNiet.station(),
                        opStationMaarStoptNiet.bestemming()
                );
            }
        }

        return misschienNieuwePositie
                .map(nieuwPositie -> new TreinStatus(status.trein(), nieuwPositie, status.minutenVertraging()))
                .orElseGet(() -> {
                    logger.fine(status.trein() + " is vertraagd!");
                    return new TreinStatus(status.trein(), status.treinPositie(), status.minutenVertraging() + 1);
                });

    }

    private @NonNull Optional<TreinPositie> berekenNieuwePositieEnGeefVerbindingTerugAlsHetAankomt(@NonNull TreinStatus status, @NonNull Onderweg onderweg) {
        final Optional<TreinPositie> misschienNieuwePositie;
        var nieuwePositie = onderweg.vooruitVoorEenMinuut(status.trein());
        if (nieuwePositie instanceof OpStation || nieuwePositie instanceof OpStationMaarStoptNiet) {
            logger.fine(status.trein() + " nu bij een station, verbinding vrij");
            netwerk.geefVerbindingTerug(onderweg.verbinding());
        }
        misschienNieuwePositie = Optional.of(nieuwePositie);
        return misschienNieuwePositie;
    }

    private @NonNull Optional<TreinPositie> probeerOpVerbindingTeKomen(@NonNull Trein trein, @NonNull Station station, @NonNull Station bestemming) {
        final var verbindingen = netwerk.bestRoute(station, bestemming, trein.soort().snelheidKmPerMin);
        if (verbindingen.isEmpty()) throw new IllegalStateException("Geen route van " + station + " naar " + bestemming);
        var eerstVerbinding = verbindingen.getFirst();
        if (netwerk.pakVerbinding(eerstVerbinding)) {
            Onderweg onderweg = new Onderweg(eerstVerbinding, 0, bestemming);
            return Optional.of(onderweg.vooruitVoorEenMinuut(trein));
        }
        return Optional.empty();
    }

}
