package chris.domain;

import java.util.List;

/**
 * Maakt treinen voor de simulatie
 */
public class TreinFabriek {

    private TreinFabriek() {}
    
    public static List<Trein> create() {
        return List.of(
                new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.UTRECHT)),
                new Trein(TreinSoort.SPRINTER, List.of(Station.AMSTERDAM, Station.WEESP, Station.HILVERSUM, Station.UTRECHT)),
                new Trein(TreinSoort.SPRINTER, List.of(Station.ALMERE, Station.WEESP, Station.HILVERSUM, Station.UTRECHT)),
                new Trein(TreinSoort.SPRINTER, List.of(Station.AMSTERDAM, Station.WEESP, Station.HILVERSUM, Station.AMERSFOORT)),
                new Trein(TreinSoort.INTERCITY,  List.of(Station.ALMERE, Station.WEESP, Station.HILVERSUM, Station.AMERSFOORT)),
                new Trein(TreinSoort.INTERCITY, List.of(Station.ALMERE, Station.AMSTERDAM)),
                new Trein(TreinSoort.INTERCITY,  List.of(Station.ALMERE, Station.AMERSFOORT)),
                new Trein(TreinSoort.INTERCITY,  List.of(Station.WEESP, Station.UTRECHT))
                );
    }
}
