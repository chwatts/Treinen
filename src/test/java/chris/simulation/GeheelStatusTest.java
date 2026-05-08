package chris.simulation;

import chris.domain.*;
import chris.domain.treinpositie.EindBestemmingBereikt;
import chris.domain.treinpositie.Onderweg;
import chris.domain.treinpositie.OpStation;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeheelStatusTest {

    static final LocalTime TIJD = LocalTime.of(10, 0);
    static final Trein TREIN_1 = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.UTRECHT));
    static final Trein TREIN_2 = new Trein(TreinSoort.SPRINTER, List.of(Station.WEESP, Station.HILVERSUM));

    @Test
    void constructorGooitAlsTrainStatusesLeeg() {
        assertThrows(IllegalArgumentException.class, () -> new GeheelStatus(TIJD, List.of()));
    }

    @Test
    void nieuwPositiesVerhoogtTijdMetEenMinuut() {
        var status = new GeheelStatus(TIJD, List.of(
                new TreinStatus(TREIN_1, new OpStation(Station.AMSTERDAM), 0)
        ));

        var nieuw = status.nieuwPosities(List.of(
                new TreinStatus(TREIN_1, new OpStation(Station.UTRECHT), 0)
        ));

        assertEquals(TIJD.plusMinutes(1), nieuw.tijd());
    }

    @Test
    void alleTreinenOpEindbestemmingAlsAlleOpEindBestemmingBereikt() {
        var status = new GeheelStatus(TIJD, List.of(
                new TreinStatus(TREIN_1, new EindBestemmingBereikt(Station.UTRECHT), 0),
                new TreinStatus(TREIN_2, new EindBestemmingBereikt(Station.HILVERSUM), 0)
        ));

        assertTrue(status.alleTreinenOpEindbestemming());
    }

    @Test
    void nietAlleOpEindbestemmingAlsEenTreinNogOnderweg() {
        var status = new GeheelStatus(TIJD, List.of(
                new TreinStatus(TREIN_1, new EindBestemmingBereikt(Station.UTRECHT), 0),
                new TreinStatus(TREIN_2, new Onderweg(new Verbinding(Station.WEESP, Station.HILVERSUM, 15, 2), 5, Station.HILVERSUM), 0)
        ));

        assertFalse(status.alleTreinenOpEindbestemming());
    }

    @Test
    void nietAlleOpEindbestemmingAlsEenTreinOpStation() {
        var status = new GeheelStatus(TIJD, List.of(
                new TreinStatus(TREIN_1, new EindBestemmingBereikt(Station.UTRECHT), 0),
                new TreinStatus(TREIN_2, new OpStation(Station.WEESP), 0)
        ));

        assertFalse(status.alleTreinenOpEindbestemming());
    }

    @Test
    void nietAlleOpEindbestemmingAlsGeenEnkeleTreinKlaarIs() {
        var status = new GeheelStatus(TIJD, List.of(
                new TreinStatus(TREIN_1, new OpStation(Station.AMSTERDAM), 0),
                new TreinStatus(TREIN_2, new OpStation(Station.WEESP), 0)
        ));

        assertFalse(status.alleTreinenOpEindbestemming());
    }
}
