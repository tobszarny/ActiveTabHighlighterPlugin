package com.tobszarny.intellij.plugin.activetabhighlighter.config;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface HighlighterSettingsChangeListener extends EventListener {
    Topic<HighlighterSettingsChangeListener> CHANGE_HIGHLIGHTER_SETTINGS_TOPIC = Topic.create("Highlighter Topic", HighlighterSettingsChangeListener.class);

    default void beforeSettingsChanged(SettingsChangedEvent context) {
    }

    void settingsChanged(SettingsChangedEvent context);
}
