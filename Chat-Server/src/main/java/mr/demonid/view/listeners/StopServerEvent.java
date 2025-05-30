package mr.demonid.view.listeners;

import java.util.EventObject;

public class StopServerEvent extends EventObject {

    public StopServerEvent(Object source)
    {
        super(source);
    }

    @Override
    public String toString() {
        return "StopServerEvent{}";
    }
}
