package org.cirdles.tripoli.gui.utilities.events;

import javafx.event.Event;
import javafx.event.EventType;

public class SaveCurrentSessionEvent extends Event {
    public static EventType<SaveCurrentSessionEvent> SAVE_CURRENT_SESSION_EVENT =
            new EventType<>(Event.ANY, "SAVE_CURRENT_SESSION_EVENT");

    public SaveCurrentSessionEvent() {
        super(SAVE_CURRENT_SESSION_EVENT);
    }
}
