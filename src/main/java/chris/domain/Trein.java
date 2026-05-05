package chris.domain;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 *
 * @param soort
 * @param route De stations die de trein in volgorde aandoet, van beginstation naar eindstation.
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

    public @NonNull Optional<Station> volgendeStation(@NonNull Station station) {
        if (station == route.getLast()) return Optional.empty();
        return Optional.of(route.getLast());
    }
}
