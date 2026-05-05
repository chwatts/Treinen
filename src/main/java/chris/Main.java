package chris;

import chris.domain.NetwerkFabriek;
import chris.domain.TreinFabriek;
import chris.simulation.GeheelStatus;
import chris.simulation.GeheelStatusObserver;
import chris.simulation.TreinSimulation;
import org.jspecify.annotations.NonNull;

class Observer implements GeheelStatusObserver {

    @Override
    public void newStatus(@NonNull GeheelStatus geheelStatus) {
        //IO.println(geheelStatus);
    }
}

public class Main {
    static void main() {

        var simulatie = new TreinSimulation(TreinFabriek.create(), NetwerkFabriek.create(), new Observer());

        simulatie.run();

    }
}
