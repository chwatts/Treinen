package chris.domain;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 *
 * @param soort
 * @param route De stations die de trein in volgorde aandoet, van beginstation naar eindstation.
 *
 */
public record Trein(@NonNull TreinSoort soort, @NonNull List<Station> route) {
    public Trein {
        if (route.size() < 2) throw new IllegalArgumentException("Route moet uit 2 of meer stations bestaan");
    }

    @NonNull
    @Override
    public String toString() {
        return soort.name() + "[" + route.getFirst() + " -> " + route.getLast() + "]";
    }

    /**
     * Geef het volgende station na deze op de route van de trein
     * @param station Huidig station
     * @return Het volgende station, als de trein al op zijn eind bestemming is, dan is `empty()` het resultaat.
     * @throws IllegalArgumentException Als een station is gegeven dat niet in de route staat,
     */
    public @NonNull Optional<Station> volgendStation(@NonNull Station station) {
        int stapNummer = route.indexOf(station);
        if (stapNummer < 0) {
            throw new IllegalArgumentException(station + " staat niet in de route");
        }
        if (stapNummer == route.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(route.get(stapNummer + 1));
    }
}
