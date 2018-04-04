package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * Main application ActiveTabsHighlighter component
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class ActiveTabHighlighterComponent implements ApplicationComponent {

    private static final Logger logger = Logger.getInstance(ActiveTabHighlighterComponent.class);

    private MessageBusConnection connection;

    public ActiveTabHighlighterComponent() {
    }

    @Override
    public void initComponent() {
        logger.debug("Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        connection = bus.connect();
        TabHighlighterFileEditorListener tabHighlighterFileEditorListener = new TabHighlighterFileEditorListener();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, tabHighlighterFileEditorListener);
        connection.subscribe(HighlighterSettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC, tabHighlighterFileEditorListener);
    }

    @Override
    public void disposeComponent() {
        logger.debug("Disposing component");
        connection.disconnect();
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "com.tobszarny.intellij.plugin.activetabhighlighter.ActiveTabHighlighterComponent";
    }
}