package chris.simulation;

import org.jspecify.annotations.NonNull;

public interface GeheelStatusObserver {
    void newStatus(@NonNull GeheelStatus geheelStatus);
}
