package chris.domain;

import org.jspecify.annotations.NonNull;

public record EindBestemmingBereikt(@NonNull Station station) implements TreinPositie {
}
