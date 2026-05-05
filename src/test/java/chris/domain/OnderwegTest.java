package chris.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OnderwegTest {

    static final Verbinding VERBINDING_WEESP = new Verbinding(Station.AMSTERDAM, Station.WEESP, 30, 3);
    static final Verbinding VERBINDING_HILVERSUM = new Verbinding(Station.WEESP, Station.HILVERSUM, 30, 3);
    static final Trein INTERCITY = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.HILVERSUM));
    static final Trein SPRINTER = new Trein(TreinSoort.SPRINTER, List.of(Station.AMSTERDAM, Station.WEESP));

    @Test
    void vooruitVoorEenMinuutBlijftOnderwegMidRoute() {
        var onderweg = new Onderweg(VERBINDING_WEESP, 0, Station.WEESP);

        assertEquals(new Onderweg(VERBINDING_WEESP, 3, Station.WEESP), onderweg.vooruitVoorEenMinuut(INTERCITY));
    }

    @Test
    void vooruitVoorEenMinuutArriveertOpStationMaarStoptNietExacteGrens() {
        // 27 + 3 = 30 == verbinding.afstandKm -> arriveert
        var onderweg = new Onderweg(VERBINDING_WEESP, 27, Station.HILVERSUM);

        assertEquals(new OpStationMaarStoptNiet(Station.WEESP, Station.HILVERSUM), onderweg.vooruitVoorEenMinuut(INTERCITY));
    }

    @Test
    void vooruitVoorEenMinuutArriveertOokAlsTreinVerbindingOveschrijdtEnStopNiet() {
        // 29 + 3 = 32 > 30 -> arriveert toch
        var onderweg = new Onderweg(VERBINDING_WEESP, 29, Station.HILVERSUM);

        assertEquals(new OpStationMaarStoptNiet(Station.WEESP, Station.HILVERSUM), onderweg.vooruitVoorEenMinuut(INTERCITY));
    }

    @Test
    void vooruitVoorEenMinuutArriveertOpStationMaExacteGrens() {
        // 27 + 3 = 30 == verbinding.afstandKm -> arriveert
        var onderweg = new Onderweg(VERBINDING_HILVERSUM, 27, Station.HILVERSUM);

        assertEquals(new OpStation(Station.HILVERSUM), onderweg.vooruitVoorEenMinuut(INTERCITY));
    }

    @Test
    void vooruitVoorEenMinuutArriveertOokAlsTreinVerbindingOveschrijdt() {
        // 29 + 3 = 32 > 30 -> arriveert toch
        var onderweg = new Onderweg(VERBINDING_HILVERSUM, 29, Station.HILVERSUM);

        assertEquals(new OpStation(Station.HILVERSUM), onderweg.vooruitVoorEenMinuut(INTERCITY));
    }

    @Test
    void sprinterWordtBeperktDoorEigenSnelheid() {
        // SPRINTER snelheid 2, verbinding max 3 -> effectief 2
        var onderweg = new Onderweg(VERBINDING_WEESP, 0, Station.WEESP);

        assertEquals(new Onderweg(VERBINDING_WEESP, 2, Station.WEESP), onderweg.vooruitVoorEenMinuut(SPRINTER));
    }

    @Test
    void verbindingMaxSnelheidBeperktSnelleTrein() {
        // INTERCITY snelheid 3, verbinding max 2 -> effectief 2
        var langzameVerbinding = new Verbinding(Station.AMSTERDAM, Station.WEESP, 30, 2);
        var onderweg = new Onderweg(langzameVerbinding, 0, Station.WEESP);

        assertEquals(new Onderweg(langzameVerbinding, 2, Station.WEESP), onderweg.vooruitVoorEenMinuut(INTERCITY));
    }

    @Test
    void constructorGooitAlsAfstandKmNegatief() {
        assertThrows(IllegalArgumentException.class,
                () -> new Onderweg(VERBINDING_WEESP, -1, Station.WEESP));
    }

    @Test
    void constructorGooitAlsAfstandKmGroterDanVerbindingAfstand() {
        assertThrows(IllegalArgumentException.class,
                () -> new Onderweg(VERBINDING_WEESP, 31, Station.WEESP));
    }
}
