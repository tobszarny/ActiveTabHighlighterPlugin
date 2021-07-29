package com.tobszarny.intellij.plugin.activetabhighlighter.config;

import java.util.EventObject;

public class SettingsChangedEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public SettingsChangedEvent(Object source) {
        super(source);
    }
}
