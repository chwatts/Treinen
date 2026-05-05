package chris.domain;

import org.jspecify.annotations.NonNull;

public record OpStationMaarStoptNiet(@NonNull Station station, @NonNull Station bestemming) implements TreinPositie {
}
