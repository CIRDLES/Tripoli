package org.cirdles.tripoli.gui.utilities.events;

import javafx.event.Event;
import javafx.event.EventType;
import org.cirdles.tripoli.gui.settings.SettingsRequestType;

public class PlotTabSelectedEvent extends Event {
    public static final EventType<PlotTabSelectedEvent> PLOT_TAB_SELECTED =
            new EventType<>(Event.ANY, "PLOT_TAB_SELECTED");

    private final SettingsRequestType requestType;

    public PlotTabSelectedEvent(SettingsRequestType requestType) {
        super(PLOT_TAB_SELECTED);
        this.requestType = requestType;
    }

    public static PlotTabSelectedEvent create(SettingsRequestType requestType) {
        return new PlotTabSelectedEvent(requestType);
    }

    public SettingsRequestType getRequestType() {
        return requestType;
    }
}
