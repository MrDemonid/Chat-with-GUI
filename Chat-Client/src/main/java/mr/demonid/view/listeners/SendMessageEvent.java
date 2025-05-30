package mr.demonid.view.listeners;

import java.util.EventObject;

public class SendMessageEvent extends EventObject {

    private String message;

    public SendMessageEvent(Object source, String message)
    {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SendMessageEvent{" +
                "message='" + message + '\'' +
                '}';
    }
}
