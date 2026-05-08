package chris.domain.treinpositie;

import chris.domain.*;
import org.jspecify.annotations.NonNull;

/**
 * Trein is onderweg op verbinding
 * @param verbinding Verbinding waarop de trein rijdt
 * @param afstandKm Hoe ver langs de verbinding is de trein
 * @param bestemming De volgende station waar de trein eigenlijk stopt (dit is misschien het eind station van de verbinding niet)
 */
public record Onderweg(@NonNull Verbinding verbinding, int afstandKm, @NonNull Station bestemming) implements TreinPositie {

    public Onderweg {
        if (afstandKm < 0) throw new IllegalArgumentException("Afstand mag niet negatief zijn");
        if (afstandKm > verbinding.afstandKm())
            throw new IllegalArgumentException("Afstand mag niet groter dan " + verbinding.afstandKm() + " km zijn");
    }

    /*
     * De trein is onderweg, dit berekent waar de trein is na een minuut.
     */
    public @NonNull TreinPositie vooruitVoorEenMinuut(@NonNull Trein trein) {
        int snelheidKmPerMin = verbinding.werkelijkSnelheid(trein.soort().snelheidKmPerMin);

        int afstandNaMinuut = snelheidKmPerMin + afstandKm;

        // Als de trein kan het volgende station bereiken, of nog verder gaan,
        // zal de trein altijd stoppen ook als het eigenlijk niet hoeft om het
        // eenvoudiger te maken.
        if (afstandNaMinuut >= verbinding.afstandKm()) {
            if (trein.route().contains(verbinding.naar())) {
                return new OpStation(verbinding.naar());
            } else {
                return new OpStationMaarStoptNiet(verbinding.naar(), bestemming);
            }
        } else {
            return new Onderweg(verbinding, afstandNaMinuut, bestemming);
        }
    }
}
