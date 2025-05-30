package mr.demonid.view.listeners;

import java.util.EventObject;

public class LoginEvent extends EventObject {

    public LoginEvent(Object source)
    {
        super(source);
    }

    @Override
    public String toString() {
        return "LoginEvent{}";
    }
}
