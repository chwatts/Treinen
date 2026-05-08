package chris.domain.treinpositie;

import chris.domain.Station;
import org.jspecify.annotations.NonNull;

public record OpStation(@NonNull Station station) implements TreinPositie {
}
