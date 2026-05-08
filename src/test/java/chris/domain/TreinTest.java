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
    void volgendStationOpEersteStationGeeftLaatsteStation() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP));

        assertEquals(Optional.of(Station.WEESP), trein.volgendStation(Station.AMSTERDAM));
    }

    @Test
    void volgendStationOpLaatsteStationGeeftLeeg() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP));

        assertEquals(Optional.empty(), trein.volgendStation(Station.WEESP));
    }

    @Test
    void volgendStationOpTussenstopGeeftEindstation() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP, Station.UTRECHT));

        assertEquals(Optional.of(Station.UTRECHT), trein.volgendStation(Station.WEESP));
    }

    @Test
    void volgendStationOpEersteStopGeeftTussenStop() {
        var trein = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP, Station.UTRECHT));

        assertEquals(Optional.of(Station.WEESP), trein.volgendStation(Station.AMSTERDAM));
    }
}
