package chris;

import chris.domain.*;
import chris.simulation.GeheelStatus;
import chris.simulation.GeheelStatusObserver;
import chris.simulation.TreinSimulatie;
import org.jspecify.annotations.NonNull;

class Observer implements GeheelStatusObserver {
    private static final String RESET = "\033[0m";
    private static final String GROEN = "\033[32m";
    private static final String GEEL  = "\033[33m";
    private static final String BLAUW = "\033[36m";
    private static final String VET   = "\033[1m";

    @Override
    public void newStatus(@NonNull GeheelStatus geheelStatus) {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(VET + "┌──────────────────────────────────────────────────────────────────┐");
        System.out.printf( "│  TREIN SIMULATIE%49s│%n", "Tijd: " + geheelStatus.tijd() + "  ");
        System.out.println("└──────────────────────────────────────────────────────────────────┘" + RESET);
        System.out.println();

        for (TreinStatus status : geheelStatus.treinStatuses()) {
            String kleur;
            if (status.treinPositie() instanceof EindBestemmingBereikt) {
                kleur = BLAUW;
            } else {
                kleur =  status.minutenVertraging() > 0 ? GEEL : GROEN;
            }

            String positie = switch (status.treinPositie()) {
                case Onderweg o -> {
                    int filled = (int) Math.round((double) o.afstandKm() / o.verbinding().afstandKm() * 16);
                    String balk = "[" + "█".repeat(filled) + "░".repeat(16 - filled) + "]";
                    yield String.format("Onderweg   %s → %s  %s  %d/%d km",
                            o.verbinding().van(), o.verbinding().naar(), balk,
                            o.afstandKm(), o.verbinding().afstandKm());
                }
                case OpStation o              -> "Op station          " + o.station();
                case OpStationMaarStoptNiet o -> "Rijdt door          " + o.station();
                case EindBestemmingBereikt e  -> "Aangekomen          " + e.station() + "  ✓";
            };

            String vertraging = status.minutenVertraging() > 0
                    ? "  ⚠  " + status.minutenVertraging() + " min vertraging"
                    : "";

            System.out.println(kleur + String.format("  %-35s  %s%s", status.trein(), positie, vertraging) + RESET);
        }

        System.out.println();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    static void main() {
        var simulatie = new TreinSimulatie(TreinFabriek.create(), NetwerkFabriek.create(), new Observer());
        simulatie.run();
    }
}
