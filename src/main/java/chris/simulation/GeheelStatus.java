package chris.simulation;

import chris.domain.EindBestemmingBereikt;
import chris.domain.TreinStatus;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public record GeheelStatus(@NonNull LocalTime tijd, @NonNull List<TreinStatus> treinStatuses) {
    public GeheelStatus {
        if (treinStatuses.isEmpty()) throw new IllegalArgumentException("Moet tenminste een trein zijn");
    }

    public GeheelStatus nieuwPosities(List<TreinStatus> nieuwPosities) {
        return new GeheelStatus(tijd.plus(Duration.ofMinutes(1)), nieuwPosities);
    }

    public boolean alleTreinenOpEindbestemming() {
        return treinStatuses.stream().allMatch(s -> s.treinPositie() instanceof EindBestemmingBereikt);
    }
}
