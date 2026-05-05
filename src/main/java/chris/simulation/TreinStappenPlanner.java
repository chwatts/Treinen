package chris.simulation;

import chris.domain.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.*;
import java.util.logging.Logger;

class TreinStappenPlanner {
    private final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
    private final @NonNull Netwerk netwerk;
    private final Logger logger = Logger.getLogger(TreinStappenPlanner.class.getName());
    TreinStappenPlanner(@NonNull Netwerk netwerk) {
        this.netwerk = netwerk;
    }

    @NonNull Future<TreinStatus> beginAsyncVolgendePostieBerekenen(@NonNull  final TreinStatus status) {
        return exec.submit(() -> berekenVolgendePositie(status));
    }

    @NonNull TreinStatus berekenVolgendePositie(@NonNull TreinStatus status) {
        final TreinPositie misschienNieuwePositie;
        switch (status.treinPositie()) {
            case Onderweg onderweg -> {
                var nieuwePositie = onderweg.vooruitVoorEenMinuut(status.trein());
                if (nieuwePositie instanceof OpStation || nieuwePositie instanceof OpStationMaarStoptNiet) {
                    logger.info(status.trein() + " nu bij een station, verbinding vrij");
                    netwerk.geefVerbindingTerug(onderweg.verbinding());
                }
                misschienNieuwePositie = nieuwePositie;
            }
            case OpStation opStation -> {
                var volgend = status.trein().volgendeStation(opStation.station());
                if (volgend.isEmpty()) {
                    misschienNieuwePositie = new EindBestemmingBereikt(opStation.station());
                } else {
                    misschienNieuwePositie = probeerOpVerbindingTeKomen(status.trein(), opStation.station(), volgend.get());
                }
            }

            case EindBestemmingBereikt _ -> {
                logger.info(status.trein() + " nu op eind bestemming.");
                return status;
            }

            case OpStationMaarStoptNiet opStationMaarStoptNiet -> {
                logger.info(status.trein() + " gaat door " + opStationMaarStoptNiet.station());
                misschienNieuwePositie = probeerOpVerbindingTeKomen(
                        status.trein(),
                        opStationMaarStoptNiet.station(),
                        opStationMaarStoptNiet.bestemming()
                );
            }
        }
        boolean isVertraagd = misschienNieuwePositie == null;
        if (isVertraagd) {
            logger.info(status.trein() + " is vertraagd!");
            return new TreinStatus(status.trein(), status.treinPositie(), status.minutenVertraging() + 1);
        } else {
            return new TreinStatus(status.trein(), misschienNieuwePositie, status.minutenVertraging());
        }
    }

    private @Nullable TreinPositie probeerOpVerbindingTeKomen(@NonNull Trein trein, @NonNull Station station, @NonNull Station bestemming) {
        final var verbindingen = netwerk.bestRoute(station, bestemming, trein.soort().snelheidKmPerMin);
        if (verbindingen.isEmpty()) throw new IllegalStateException("Geen route van " + station + " naar " + bestemming);
        var eerstVerbinding = verbindingen.getFirst();
        if (netwerk.pakVerbinding(eerstVerbinding)) {
            Onderweg onderweg = new Onderweg(eerstVerbinding, 0, bestemming);
            return onderweg.vooruitVoorEenMinuut(trein);
        }
        return null;
    }

}
