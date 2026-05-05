package chris.domain;

import org.jspecify.annotations.NonNull;

public record Verbinding(@NonNull Station van, @NonNull Station naar, int afstandKm, int maxSnelheidKmPerMin) {
    public Verbinding {
        if (afstandKm < 1) throw new IllegalArgumentException("Kan niet onder 1km");
        if (maxSnelheidKmPerMin < 1 || maxSnelheidKmPerMin > 200) throw new IllegalArgumentException("Ongeldig max snelheid");
    }

    public int werkelijkSnelheid(int snelheidTreinKmPerMin) {
        return Math.min(snelheidTreinKmPerMin, maxSnelheidKmPerMin());
    }
}
