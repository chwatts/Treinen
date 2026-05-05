package chris.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TreinTest {

    @Test
    void routeMetMinderDanTweeStationsGooitException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM)));
    }

    @Test
    void volgendeStationOpEersteStationGeeftLaatsteStation() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP));

        assertEquals(Optional.of(Station.WEESP), trein.volgendeStation(Station.AMSTERDAM));
    }

    @Test
    void volgendeStationOpLaatsteStationGeeftLeeg() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP));

        assertEquals(Optional.empty(), trein.volgendeStation(Station.WEESP));
    }

    @Test
    void volgendeStationOpTussenstopGeeftEindstation() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP, Station.UTRECHT));

        assertEquals(Optional.of(Station.UTRECHT), trein.volgendeStation(Station.WEESP));
    }
}
