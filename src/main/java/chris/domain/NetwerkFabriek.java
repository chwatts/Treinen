package chris.domain;

import java.util.List;

public final class NetwerkFabriek {
    private NetwerkFabriek() {

    }

    public static Netwerk create() {
        return new Netwerk(List.of(
                new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, 3),
                new Verbinding(Station.AMSTERDAM, Station.WEESP, 30, 3),
                new Verbinding(Station.WEESP, Station.HILVERSUM, 30, 2),
                new Verbinding(Station.HILVERSUM, Station.AMERSFOORT, 30, 2),
                new Verbinding(Station.HILVERSUM, Station.UTRECHT, 30, 2),
                new Verbinding(Station.ALMERE, Station.AMSTERDAM, 30, 2),
                new Verbinding(Station.ALMERE, Station.WEESP, 30, 3)
                ));
    }

}
