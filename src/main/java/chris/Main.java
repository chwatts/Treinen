package chris;

import chris.domain.NetwerkFabriek;
import chris.domain.TreinFabriek;
import chris.simulation.GeheelStatus;
import chris.simulation.GeheelStatusObserver;
import chris.simulation.TreinSimulatie;
import org.jspecify.annotations.NonNull;

class Observer implements GeheelStatusObserver {
    @Override
    public void newStatus(@NonNull GeheelStatus geheelStatus) {
        // Zou leuk als ik iets met dit zou doen..
    }
}

public class Main {
    static void main() {

        var simulatie = new TreinSimulatie(TreinFabriek.create(), NetwerkFabriek.create(), new Observer());

        simulatie.run();

    }
}
