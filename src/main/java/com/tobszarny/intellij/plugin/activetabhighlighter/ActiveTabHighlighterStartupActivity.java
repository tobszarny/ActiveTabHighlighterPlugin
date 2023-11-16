/*
 *
 *  Copyright (c) 2023 Tomasz Obszarny
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.AutoconfigureListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.editor.TabFileEditorListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.editor.TabsListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.editor.TabsListenerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Main application ActiveTabHighlighter service
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class ActiveTabHighlighterStartupActivity implements StartupActivity, DumbAware, AppLifecycleListener {

    private static final Logger LOGGER = Logger.getInstance(ActiveTabHighlighterStartupActivity.class);

    private Project myProject;

    private MessageBusConnection connection;

    @Override
    public void runActivity(@NotNull Project project) {
        this.myProject = project;
        init(project);
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            // don't create the UI when unit testing
        } else {

        }
    }

    @Override
    public void appStarted() {
        LOGGER.warn("DADA");

//        ApplicationManager.getApplication().invokeLater(() -> {
//            LOGGER.warn("appStarted()->invokeLater()");
//            FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
//            FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
//            if (selectedEditor != null) {
//                VirtualFile file = selectedEditor.getFile();
//                selectedEditor.
//                handleSelectionChange(null, file);
//            } else {
//                LOGGER.warn("No editor");
//            }
//        },  myProject.getDisposed());
    }

    private void init(Project project) {
        LOGGER.warn("Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        connection = bus.connect();
        TabFileEditorListener tabHighlighterFileEditorListener = new TabFileEditorListener(project);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, tabHighlighterFileEditorListener);
        connection.subscribe(SettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC, tabHighlighterFileEditorListener);
        connection.subscribe(AutoconfigureListener.AUTOCONFIGURE_TOPIC, tabHighlighterFileEditorListener);

        TabsListenerImpl tabsListener = new TabsListenerImpl(project);
        connection.subscribe(TabsListener.TABS_TOPIC, tabsListener);

        connection.subscribe(AppLifecycleListener.TOPIC, this);
//        connection.subscribe(ApplicationActivationListener.TOPIC, new ApplicationActivationListener() {
//            @Override
//            public void applicationActivated(@NotNull IdeFrame ideFrame) {
//               LOGGER.warn("Activatexcdasdfasdfasdf");
//            }
//        });

//        bus.syncPublisher(AutoconfigureListener.AUTOCONFIGURE_TOPIC).fireAutoconfigure();
//
//        MessageBusConnection connectionProject = project.getMessageBus().connect();
//        AppLifecycleListener
//        connectionProject.subscribe(DumbService.DUMB_MODE, tabHighlighterFileEditorListener);
    }

}
