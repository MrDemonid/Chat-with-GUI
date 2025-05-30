package mr.demonid.view.listeners;

import java.util.EventObject;

public class DisconnectEvent extends EventObject {

    public DisconnectEvent(Object source) {
        super(source);
    }

    @Override
    public String toString() {
        return "DisconnectEvent{}";
    }
}
