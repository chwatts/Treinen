package chris.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetwerkTest {

    @Test
    void enkelvoudigeDirecteVerbinding() {
        var verbinding = new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, 3);
        var netwerk = new Netwerk(List.of(verbinding));

        assertEquals(List.of(verbinding),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 3));
    }

    @Test
    void viaTussenstopSnellerDanDirecteVerbinding() {
        // Direct: 90km, connection max 1, train max 10 -> effective 1 -> 90 min
        // Via WEESP: 2x10km, connection max 2, train max 10 -> effective 2 -> 10 min
        var direct = new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 90, 1);
        var naarWeesp = new Verbinding(Station.AMSTERDAM, Station.WEESP, 10, 2);
        var weespNaarUtrecht = new Verbinding(Station.WEESP, Station.UTRECHT, 10, 2);
        var netwerk = new Netwerk(List.of(direct, naarWeesp, weespNaarUtrecht));

        assertEquals(List.of(naarWeesp, weespNaarUtrecht),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 10));
    }

    @Test
    void directeVerbindingSnellerDanOmweg() {
        // Direct: 10km, connection max 3, train max 10 -> effective 3 -> ~3 min
        // Via WEESP: 2x50km, connection max 1, train max 10 -> effective 1 -> 100 min
        var direct = new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 10, 3);
        var naarWeesp = new Verbinding(Station.AMSTERDAM, Station.WEESP, 50, 1);
        var weespNaarUtrecht = new Verbinding(Station.WEESP, Station.UTRECHT, 50, 1);
        var netwerk = new Netwerk(List.of(direct, naarWeesp, weespNaarUtrecht));

        assertEquals(List.of(direct),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 10));
    }

    @Test
    void langereRouteViaMeerdereStationsSnellerDoorHogereMaxSnelheid() {
        // Direct: 60km, connection max 1, train max 10 -> effective 1 -> 60 min
        // Via 4 hops: 4x10km, connection max 10, train max 10 -> effective 10 -> 4 min
        var direct = new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 60, 1);
        var naarWeesp = new Verbinding(Station.AMSTERDAM, Station.WEESP, 10, 10);
        var weespNaarHilversum = new Verbinding(Station.WEESP, Station.HILVERSUM, 10, 10);
        var hilversumNaarAmersfoort = new Verbinding(Station.HILVERSUM, Station.AMERSFOORT, 10, 10);
        var amersfoortNaarUtrecht = new Verbinding(Station.AMERSFOORT, Station.UTRECHT, 10, 10);
        var netwerk = new Netwerk(List.of(direct, naarWeesp, weespNaarHilversum, hilversumNaarAmersfoort, amersfoortNaarUtrecht));

        assertEquals(List.of(naarWeesp, weespNaarHilversum, hilversumNaarAmersfoort, amersfoortNaarUtrecht),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 10));
    }

    @Test
    void langzameTreinWordtBeperktDoorEigenSnelheid() {
        // Train max 1 is the bottleneck on the direct route (connection max 10)
        // Direct: 60km, connection max 10, train max 1 -> effective 1 -> 60 min
        // Via WEESP: 2x10km, connection max 1, train max 1 -> effective 1 -> 20 min
        var direct = new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 60, 10);
        var naarWeesp = new Verbinding(Station.AMSTERDAM, Station.WEESP, 10, 1);
        var weespNaarUtrecht = new Verbinding(Station.WEESP, Station.UTRECHT, 10, 1);
        var netwerk = new Netwerk(List.of(direct, naarWeesp, weespNaarUtrecht));

        assertEquals(List.of(naarWeesp, weespNaarUtrecht),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 1));
    }

    @Test
    void snelleTreinKiestAndereRouteDanLangzameTrein() {
        // Same network as above, but fast train takes the direct high-speed route
        // Direct: 60km, connection max 10, train max 10 -> effective 10 -> 6 min
        // Via WEESP: 2x10km, connection max 1, train max 10 -> effective 1 -> 20 min
        var direct = new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 60, 10);
        var naarWeesp = new Verbinding(Station.AMSTERDAM, Station.WEESP, 10, 1);
        var weespNaarUtrecht = new Verbinding(Station.WEESP, Station.UTRECHT, 10, 1);
        var netwerk = new Netwerk(List.of(direct, naarWeesp, weespNaarUtrecht));

        assertEquals(List.of(direct),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 10));
    }

    @Test
    void omgekeerdRichtingNietBereikbaarInEenrichtingsnetwerk() {
        var netwerk = new Netwerk(List.of(
                new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, 3)
        ));

        assertEquals(List.of(),
                netwerk.bestRoute(Station.UTRECHT, Station.AMSTERDAM, 3));
    }

    @Test
    void doelstationNietVerbondenMetBronstation() {
        var netwerk = new Netwerk(List.of(
                new Verbinding(Station.AMSTERDAM, Station.WEESP, 15, 2),
                new Verbinding(Station.WEESP, Station.HILVERSUM, 15, 2)
        ));

        assertEquals(List.of(),
                netwerk.bestRoute(Station.AMSTERDAM, Station.UTRECHT, 5));
    }

    @Test
    void zelfdeStartEnEindstationGeeftLegeRoute() {
        var netwerk = new Netwerk(List.of(
                new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, 3)
        ));

        assertEquals(List.of(),
                netwerk.bestRoute(Station.AMSTERDAM, Station.AMSTERDAM, 3));
    }
}
