package chris.domain.treinpositie;

import chris.domain.Station;
import org.jspecify.annotations.NonNull;

public record EindBestemmingBereikt(@NonNull Station station) implements TreinPositie {
}
