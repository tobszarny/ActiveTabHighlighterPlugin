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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.ui.tabs.TabInfo;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.controller.SettingsConfigService;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TabUpdateService {


    private final Project project;
    private final Application application;

    public TabUpdateService(Project project) {
        this.project = project;
        this.application = ApplicationManager.getApplication();

    }

    @Nullable
    public static TabUpdateService getTabUpdateService(Project project) {
        return project.getService(TabUpdateService.class);
    }

    public static void setTabColor(Color color, @NotNull VirtualFile file, @NotNull EditorWindow editorWindow) {
        final EditorComposite fileComposite = editorWindow.getComposite(file);

        final int index = getFileWithinEditorIndex(editorWindow, fileComposite);
        if (index >= 0) {
            if (editorWindow.getTabbedPane() != null) { //Distraction free mode // Presentation mode
                editorWindow.getTabbedPane().getTabs().getTabAt(index).setTabColor(color);
            }
        }
    }

    public static int getFileWithinEditorIndex(@NotNull EditorWindow editorWindow, EditorComposite fileComposite) {
        Validate.notNull(editorWindow, "EditorWindow cannot be null");
        return editorWindow.getAllComposites().indexOf(fileComposite);
    }

    public void update(SettingsConfigService service) {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            final FileColorManager fileColorManager = FileColorManager.getInstance(project);
            final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);

            for (EditorWindow editorWindow : manager.getWindows()) {
                TabInfo selected = editorWindow.getTabbedPane().getTabs().getSelectedInfo();
                if (selected == null) {
                    continue;
                }

                Color color = Stream.<Supplier<Optional<Color>>>of(
                                service::getBackgroundColorOptional,
                                () -> getColor(fileColorManager, editorWindow.getSelectedFile()))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst().orElse(null);

                application.invokeLater(() -> selected.setTabColor(color));
            }

        }
    }

    public void highlightSafe(FileColorManager fileColorManager, VirtualFile file, EditorWindow editorWindow,
                              SettingsConfigService settingsConfig) {
        if (file != null && editorWindow.getComposite(file) != null) {
            highlight(fileColorManager, file, editorWindow, settingsConfig);
        }
    }

    public void unhighlightSafe(FileColorManager fileColorManager, VirtualFile oldFile, EditorWindow editorWindow,
                                SettingsConfigService settingsConfig) {
        if (oldFile != null && editorWindow.getComposite(oldFile) != null) {
            unhighlight(fileColorManager, oldFile, editorWindow, settingsConfig);
        }
    }

    public void highlight(FileColorManager fileColorManager, VirtualFile file, EditorWindow editorWindow,
                          SettingsConfigService settingsConfig) {
        TabUpdateService.setTabColor(settingsConfig.getBackgroundColorOptional().orElseGet(() ->
                        getColor(fileColorManager, file).orElse(null)),
                file, editorWindow);
    }

    public void unhighlight(@NotNull FileColorManager fileColorManager, VirtualFile file, EditorWindow editorWindow,
                            SettingsConfigService settingsConfig) {

        TabUpdateService.setTabColor(getColor(fileColorManager, file).orElse(null),
                file, editorWindow);
    }

    private Optional<Color> getColor(FileColorManager fileColorManager, VirtualFile file) {
        Future<Color> colorFuture = application.executeOnPooledThread(() -> fileColorManager.getFileColor(file));
        Optional<Color> colorOpt;
        try {
            colorOpt = Optional.ofNullable(colorFuture.get(1000, TimeUnit.MILLISECONDS));
        } catch (ExecutionException | RuntimeException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return colorOpt;
    }
}
