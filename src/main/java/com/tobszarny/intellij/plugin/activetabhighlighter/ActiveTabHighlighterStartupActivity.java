package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.HighlighterSettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.editor.TabHighlighterFileEditorListener;
import org.jetbrains.annotations.NotNull;

/**
 * Main application ActiveTabsHighlighter service
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class ActiveTabHighlighterStartupActivity implements StartupActivity, DumbAware {

    private static final Logger logger = Logger.getInstance(ActiveTabHighlighterStartupActivity.class);

    private MessageBusConnection connection;

    public void init(Project project) {
        logger.debug("Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        connection = bus.connect();
        TabHighlighterFileEditorListener tabHighlighterFileEditorListener = new TabHighlighterFileEditorListener(project);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, tabHighlighterFileEditorListener);
        connection.subscribe(HighlighterSettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC, tabHighlighterFileEditorListener);
    }

    @Override
    public void runActivity(@NotNull Project project) {
        init(project);
        if(ApplicationManager.getApplication().isUnitTestMode()) {
            // don't create the UI when unit testing
            return;
        }
    }
}
