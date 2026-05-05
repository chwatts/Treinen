package chris.domain;

/**
 * Type trein, snelheid is in km per minuut om de simulatie gemakkelijk te houden, integer rekening is prima.
 */
public enum TreinSoort {
    SPRINTER(2), INTERCITY(3);

    public final int snelheidKmPerMin;

    TreinSoort(int snelheidKmPerMin) {
        this.snelheidKmPerMin = snelheidKmPerMin;
    }
}
