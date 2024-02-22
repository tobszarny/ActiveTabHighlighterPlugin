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

package com.tobszarny.intellij.plugin.activetabhighlighter.editor;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.AutoconfigureListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangedEvent;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.controller.SettingsConfigService;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * File Editor AsyncFileListenerImpl implementation for tab highlight
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class TabFileEditorListenerImpl implements TabFileEditorListener, FileEditorManagerListener, SettingsChangeListener, AutoconfigureListener, AppLifecycleListener {

    private static final Logger LOGGER = Logger.getInstance(TabFileEditorListenerImpl.class);
    private final Project myProject;
    private final SettingsConfigService settingsConfig;
    private final TabUpdateService tabUpdateService;
    private MessageBusConnection connection;
    private MessageBus messageBus;

    public TabFileEditorListenerImpl(Project project) {
        this.myProject = project;

        this.settingsConfig = SettingsConfigService.getSettingsConfigService(project);
        this.tabUpdateService = TabUpdateService.getTabUpdateService(project);

        initialize();
    }

    private void initialize() {
        LOGGER.debug("***** initialize()");
        messageBus = ApplicationManager.getApplication().getMessageBus();
        tabUpdateService.update(settingsConfig);
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        LOGGER.debug(String.format("fileOpened(): fileOpen %s", virtualFile.getUrl()));
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        LOGGER.debug("***** fileClosed()");
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent fileEditorManagerEvent) {
        LOGGER.debug(String.format("***** selectionChanged(): %s->%s", fileEditorManagerEvent.getOldFile(), fileEditorManagerEvent.getNewFile()));
        if (fileEditorManagerEvent.getManager().getProject().equals(myProject)) {
            handleSelectionChange(fileEditorManagerEvent.getOldFile(), fileEditorManagerEvent.getNewFile());
        }
    }

    private void handleSelectionChange(VirtualFile oldFile, VirtualFile newFile) {
        final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(myProject);
        Arrays.stream(manager.getWindows())
                .parallel()
                .filter(editorWindow -> (oldFile != null && editorWindow.isFileOpen(oldFile)) || (newFile != null && editorWindow.isFileOpen(newFile)))
                .forEach(editorWindow -> {
                    messageBus.syncPublisher(FILE_EDITOR_EVENT_TOPIC).highlight(newFile);
                    messageBus.syncPublisher(FILE_EDITOR_EVENT_TOPIC).unhighlight(oldFile);
                });
    }

    @Override
    public void unhighlight(VirtualFile file) {
        final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(myProject);
        final FileColorManager fileColorManager = FileColorManager.getInstance(myProject);

        Arrays.stream(manager.getWindows())
                .parallel()
                .filter(editorWindow -> (file != null && editorWindow.isFileOpen(file)))
                .forEach(editorWindow ->
                        tabUpdateService.unhighlightSafe(fileColorManager, file, editorWindow, settingsConfig));
    }

    @Override
    public void highlight(VirtualFile file) {
        final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(myProject);
        final FileColorManager fileColorManager = FileColorManager.getInstance(myProject);
        Arrays.stream(manager.getWindows())
                .parallel()
                .filter(editorWindow -> (file != null && editorWindow.isFileOpen(file)))
                .forEach(editorWindow ->
                        tabUpdateService.highlightSafe(fileColorManager, file, editorWindow, settingsConfig));

    }

    @Override
    public void beforeSettingsChanged(SettingsChangedEvent context) {
        LOGGER.debug("***** beforeSettingsChanged()");
    }

    @Override
    public void settingsChanged(SettingsChangedEvent context) {
        LOGGER.debug("***** settingsChanged()");
        tabUpdateService.update(settingsConfig);
    }

    @Override
    public void fireAutoconfigure() {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//
//        }
//        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
//            LOGGER.debug("***** initialize()->invokeLater()");
//            FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
//            FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
//            if (selectedEditor != null) {
//                VirtualFile file = selectedEditor.getFile();
//                handleSelectionChange(null, file);
//            }
//        });
    }
}
