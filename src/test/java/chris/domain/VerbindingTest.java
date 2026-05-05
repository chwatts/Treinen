package chris.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerbindingTest {

    @Test
    void afstandKmNulGooitException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 0, 3));
    }

    @Test
    void maxSnelheidNulGooitException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, 0));
    }

    @Test
    void maxSnelheidNegatiefGooitException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, -1));
    }

    @Test
    void maxSnelheidBoven200GooitException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Verbinding(Station.AMSTERDAM, Station.UTRECHT, 30, 201));
    }
}
