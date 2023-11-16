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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.ui.tabs.TabInfo;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.AutoconfigureListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangedEvent;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsConfig;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

/**
 * File Editor Listener implementation for tab highlight
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class TabFileEditorListener implements FileEditorManagerListener, SettingsChangeListener, AutoconfigureListener, AppLifecycleListener {

    private static final Logger LOGGER = Logger.getInstance(TabFileEditorListener.class);
    private final Project myProject;
    private final SettingsConfig settingsConfig;

    public TabFileEditorListener(Project project) {
        this.myProject = project;

        this.settingsConfig = SettingsConfig.getSettings(project);

        initialize();
    }

    private void initialize() {
        LOGGER.warn("initialize()");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        LOGGER.warn(String.format("fileOpened(): fileOpen %s", virtualFile.getUrl()));
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        LOGGER.warn("fileClosed()");
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent fileEditorManagerEvent) {
        LOGGER.warn("selectionChanged()");
        if (fileEditorManagerEvent.getManager().getProject().equals(myProject)) {
            handleSelectionChange(fileEditorManagerEvent.getOldFile(), fileEditorManagerEvent.getNewFile());
        }
    }

    @Override
    public void fileOpenedSync(@NotNull FileEditorManager source, @NotNull VirtualFile file, @NotNull List<FileEditorWithProvider> editorsWithProviders) {
        LOGGER.warn("fileOpenedSync()");
    }

    //    @Override
//    public  void exitDumbMode() {
//        ApplicationManager.getApplication().invokeLater(() -> {
//            LOGGER.warn("exitDumbMode()->invokeLater()");
//            FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
//            FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
//            if (selectedEditor != null) {
//                VirtualFile file = selectedEditor.getFile();
//                handleSelectionChange(null, file);
//            } else {
//                LOGGER.warn("No editor");
//            }
//        },  myProject.getDisposed());
//    }

    private void handleSelectionChange(VirtualFile oldFile, VirtualFile newFile) {
        final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(myProject);
        final FileColorManager fileColorManager = FileColorManager.getInstance(myProject);

        for (EditorWindow editorWindow : manager.getWindows()) {
            unhighlightSafe(fileColorManager, oldFile, editorWindow);

            highlightSafe(newFile, editorWindow);
        }
    }

    private void highlightSafe(VirtualFile file, EditorWindow editorWindow) {
        if (file != null && editorWindow.getComposite(file) != null) {
            highlight(file, editorWindow);
        }
    }

    private void unhighlightSafe(FileColorManager fileColorManager, VirtualFile oldFile, EditorWindow editorWindow) {
        if (oldFile != null && editorWindow.getComposite(oldFile) != null) {
            unhighlight(fileColorManager, oldFile, editorWindow);
        }
    }


    private void highlight(VirtualFile file, EditorWindow editorWindow) {
        setTabColor(settingsConfig.getBackgroundColor(), file, editorWindow);
    }

    private void unhighlight(@NotNull FileColorManager fileColorManager, VirtualFile file, EditorWindow editorWindow) {
        setTabColor(fileColorManager.getFileColor(file), file, editorWindow);
    }

    private void setTabColor(Color color, @NotNull VirtualFile file, @NotNull EditorWindow editorWindow) {
        final EditorComposite fileComposite = editorWindow.getComposite(file);

        final int index = getFileWithinEditorIndex(editorWindow, fileComposite);
        if (index >= 0) {
            if (editorWindow.getTabbedPane() != null) { //Distraction free mode // Presentation mode
                editorWindow.getTabbedPane().getTabs().getTabAt(index).setTabColor(color);
            }
        }
    }

    private int getFileWithinEditorIndex(@NotNull EditorWindow editorWindow, EditorComposite fileComposite) {
        Validate.notNull(editorWindow, "EditorWindow cannot be null");
        return editorWindow.getAllComposites().indexOf(fileComposite);
    }

    @Override
    public void beforeSettingsChanged(SettingsChangedEvent context) {
        LOGGER.warn("beforeSettingsChanged()");
    }

    @Override
    public void settingsChanged(SettingsChangedEvent context) {
        LOGGER.warn("settingsChanged()");
        ProjectManager.getInstance().getOpenProjects();
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);

            for (EditorWindow editorWindow : manager.getWindows()) {
                TabInfo selected = editorWindow.getTabbedPane().getTabs().getSelectedInfo();
                selected.setTabColor(settingsConfig.getBackgroundColor());
            }

        }

    }

    @Override
    public void fireAutoconfigure() {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//
//        }
//        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
//            LOGGER.warn("initialize()->invokeLater()");
//            FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
//            FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
//            if (selectedEditor != null) {
//                VirtualFile file = selectedEditor.getFile();
//                handleSelectionChange(null, file);
//            }
//        });
    }
}
