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
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.AutoconfigureListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.controller.SettingsConfigService;
import com.tobszarny.intellij.plugin.activetabhighlighter.editor.*;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main application ActiveTabHighlighter service
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class ActiveTabHighlighterStartupActivity implements ProjectActivity, DumbAware, AppLifecycleListener {

    private static final Logger LOGGER = Logger.getInstance(ActiveTabHighlighterStartupActivity.class);

    private Project myProject;

    private MessageBusConnection connection;

    private void init(Project project) {
        LOGGER.debug("***** Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        connection = bus.connect();
        TabFileEditorListenerImpl tabHighlighterFileEditorListener = new TabFileEditorListenerImpl(project);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, tabHighlighterFileEditorListener);
        connection.subscribe(SettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC, tabHighlighterFileEditorListener);
        connection.subscribe(AutoconfigureListener.AUTOCONFIGURE_TOPIC, tabHighlighterFileEditorListener);
        connection.subscribe(TabFileEditorListener.FILE_EDITOR_EVENT_TOPIC, tabHighlighterFileEditorListener);

        TabsListenerImpl tabsListener = new TabsListenerImpl(project);
        connection.subscribe(TabsListener.TABS_TOPIC, tabsListener);

        connection.subscribe(AppLifecycleListener.TOPIC, this);

        connection.subscribe(LafManagerListener.TOPIC, (LafManagerListener) source -> {
            LOGGER.debug("***** Theme changed");
            TabUpdateService.getTabUpdateService(project).update(SettingsConfigService.getSettingsConfigService(project));
        });


//        // Every time Intellij regains focus after loosing it :(
//        connection.subscribe(ApplicationActivationListener.TOPIC, new ApplicationActivationListener() {
//            @Override
//            public void applicationActivated(@NotNull IdeFrame ideFrame) {
//                LOGGER.debug("***** applicationActivated");
//            }
//        });

//        connection.subscribe(AutoconfigureListener.AUTOCONFIGURE_TOPIC, (AutoconfigureListener) () ->
//                LOGGER.debug("***** fireAutoconfigure()"));

//        bus.syncPublisher(AutoconfigureListener.AUTOCONFIGURE_TOPIC).fireAutoconfigure();
//
//        MessageBusConnection connectionProject = project.getMessageBus().connect();
//        AppLifecycleListener
//        connectionProject.subscribe(DumbService.DUMB_MODE, tabHighlighterFileEditorListener);
    }

    @Override
    public void appClosing() {

    }

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        LOGGER.debug("***** execute(" + project.getName() + ")");
        this.myProject = project;
        init(project);
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            // don't create the UI when unit testing
        } else {

        }
        return null;
    }
}
