package chris.simulation;

import chris.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreinStappenPlannerTest {

    // INTERCITY speed 3 km/min == verbinding max speed -> effective speed 3
    static final Verbinding AMSTERDAM_WEESP = new Verbinding(Station.AMSTERDAM, Station.WEESP, 30, 3);
    static final Verbinding WEESP_UTRECHT = new Verbinding(Station.WEESP, Station.UTRECHT, 30, 3);

    Netwerk netwerk = new Netwerk(List.of(AMSTERDAM_WEESP, WEESP_UTRECHT));
    TreinStappenPlanner planner = new TreinStappenPlanner(netwerk);
    Trein intercity = new Trein(TreinSoort.INTERCITY, List.of(Station.AMSTERDAM, Station.WEESP));

    @Test
    void treinOnderwegGaatVooruit() {
        var status = new TreinStatus(intercity, new Onderweg(AMSTERDAM_WEESP, 0, Station.WEESP), 0);

        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(new TreinStatus(intercity, new Onderweg(AMSTERDAM_WEESP, 3, Station.WEESP), 0), nieuwStatus);
    }

    @Test
    void treinOpStationStaptOpVrijeVerbinding() {
        var status = new TreinStatus(intercity, new OpStation(Station.AMSTERDAM), 0);

        var nieuwStatus = planner.berekenVolgendePositie(status);

        // starts at 0km, immediately moves 3km forward
        assertEquals(new TreinStatus(intercity, new Onderweg(AMSTERDAM_WEESP, 3, Station.WEESP), 0), nieuwStatus);
    }

    @Test
    void treinOpStationWordtVertraagdAlsVerbindingBezet() {
        netwerk.pakVerbinding(AMSTERDAM_WEESP); // simulates another train already on this connection

        var status = new TreinStatus(intercity, new OpStation(Station.AMSTERDAM), 0);
        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(new TreinStatus(intercity, new OpStation(Station.AMSTERDAM), 1), nieuwStatus);
    }

    @Test
    void meerdereBlokkadeStappenAccumulerenVertraging() {
        netwerk.pakVerbinding(AMSTERDAM_WEESP);

        var status = new TreinStatus(intercity, new OpStation(Station.AMSTERDAM), 0);
        var status1 = planner.berekenVolgendePositie(status);
        var status2 = planner.berekenVolgendePositie(status1);
        var status3 = planner.berekenVolgendePositie(status2);

        assertEquals(3, status3.minutenVertraging());
        assertEquals(new OpStation(Station.AMSTERDAM), status3.treinPositie());
    }

    @Test
    void treinBereiktStationGeeftVerbindingVrij() {
        netwerk.pakVerbinding(AMSTERDAM_WEESP); // train is on this connection

        // 27km travelled, speed 3 -> 27+3=30 >= 30km, so arrives this step
        var status = new TreinStatus(intercity, new Onderweg(AMSTERDAM_WEESP, 27, Station.WEESP), 0);
        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(new TreinStatus(intercity, new OpStation(Station.WEESP), 0), nieuwStatus);
        assertTrue(netwerk.pakVerbinding(AMSTERDAM_WEESP)); // connection is now free again
    }

    @Test
    void treinAangekomenOpEindstationGaatNaarEindBestemmingBereikt() {
        // WEESP is the last station in route [AMSTERDAM, WEESP], so no next station
        var status = new TreinStatus(intercity, new OpStation(Station.WEESP), 0);

        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(new TreinStatus(intercity, new EindBestemmingBereikt(Station.WEESP), 0), nieuwStatus);
    }

    @Test
    void treinOpEindbestemmingBlijftStaan() {
        var status = new TreinStatus(intercity, new EindBestemmingBereikt(Station.WEESP), 0);

        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(status, nieuwStatus);
    }

    @Test
    void treinOpStationMaarStoptNietStaptOpVolgendeVerbinding() {
        var status = new TreinStatus(intercity, new OpStationMaarStoptNiet(Station.WEESP, Station.UTRECHT), 0);

        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(new TreinStatus(intercity, new Onderweg(WEESP_UTRECHT, 3, Station.UTRECHT), 0), nieuwStatus);
    }

    @Test
    void treinOpStationMaarStoptNietWordtVertraagdAlsVerbindingBezet() {
        netwerk.pakVerbinding(WEESP_UTRECHT);

        var status = new TreinStatus(intercity, new OpStationMaarStoptNiet(Station.WEESP, Station.UTRECHT), 0);
        var nieuwStatus = planner.berekenVolgendePositie(status);

        assertEquals(new TreinStatus(intercity, new OpStationMaarStoptNiet(Station.WEESP, Station.UTRECHT), 1), nieuwStatus);
    }

    @Test
    void vertraagdeTreinStaptOpNaVrijgaveVanVerbinding() {
        netwerk.pakVerbinding(AMSTERDAM_WEESP); // another train is on this connection

        var status = new TreinStatus(intercity, new OpStation(Station.AMSTERDAM), 0);
        var vertraagdStatus = planner.berekenVolgendePositie(status);
        assertEquals(1, vertraagdStatus.minutenVertraging());

        netwerk.geefVerbindingTerug(AMSTERDAM_WEESP); // other train arrives at its station

        var nieuwStatus = planner.berekenVolgendePositie(vertraagdStatus);
        assertEquals(new TreinStatus(intercity, new Onderweg(AMSTERDAM_WEESP, 3, Station.WEESP), 1), nieuwStatus);
    }
}
