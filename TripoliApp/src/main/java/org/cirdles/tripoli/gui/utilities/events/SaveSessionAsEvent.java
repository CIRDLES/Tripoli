package org.cirdles.tripoli.gui.utilities.events;
import javafx.event.EventType;
import javafx.event.Event;

public class SaveSessionAsEvent extends Event {
    public static final EventType<SaveSessionAsEvent> SAVE_SESSION_AS_EVENT_EVENT_TYPE =
                                new EventType<>(Event.ANY, "SAVE_SESSION_AS_EVENT");


    public SaveSessionAsEvent() {
        super(SAVE_SESSION_AS_EVENT_EVENT_TYPE);
    }

}
