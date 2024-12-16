package org.cirdles.tripoli.gui.utilities.events;
import javafx.event.EventType;
import javafx.event.Event;
import org.cirdles.tripoli.sessions.Session;

public class SaveAsEvent extends Event {
    public static final EventType<SaveAsEvent> SAVE_AS_EVENT_EVENT_TYPE =
                                new EventType<>(Event.ANY, "SAVE_AS_EVENT");

    private final Session session;

    public SaveAsEvent (Session session) {
        super(SAVE_AS_EVENT_EVENT_TYPE);
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }
}
