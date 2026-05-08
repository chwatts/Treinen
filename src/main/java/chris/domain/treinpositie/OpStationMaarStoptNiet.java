package chris.domain.treinpositie;

import chris.domain.Station;
import org.jspecify.annotations.NonNull;

/**
 * De trein is klaar met een verbinding, en staat op een station, maar het station staat niet in de
 * route vsn trein, dus hij stopt niet om passagiers in of uit te laten.
 * @param station Station waar de trein nu staat.
 * @param bestemming Station waar de train wil stoppen.
 */
public record OpStationMaarStoptNiet(@NonNull Station station, @NonNull Station bestemming) implements TreinPositie {
}
