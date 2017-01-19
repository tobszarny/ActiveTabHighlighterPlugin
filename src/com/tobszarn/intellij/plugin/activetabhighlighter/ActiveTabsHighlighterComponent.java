package com.tobszarn.intellij.plugin.activetabhighlighter;

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
public class ActiveTabsHighlighterComponent implements ApplicationComponent {

    private static final Logger logger = Logger.getInstance(ActiveTabsHighlighterComponent.class);

    private MessageBusConnection connection;

    public ActiveTabsHighlighterComponent() {
    }

    @Override
    public void initComponent() {
        logger.debug("Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        connection = bus.connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new TabHighlighterFileEditorListener());
    }

    @Override
    public void disposeComponent() {
        logger.debug("Disposing component");
        connection.disconnect();
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "com.tobszarn.intellij.plugin.activetabhighlighter.ActiveTabsHighlighterComponent";
    }
}