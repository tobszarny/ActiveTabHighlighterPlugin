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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.EDT;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Custom implementation of EditorTabColorProvider
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class CustomEditorTabColorProvider implements EditorTabColorProvider, DumbAware {

    private static final Logger LOGGER = Logger.getInstance(CustomEditorTabColorProvider.class);

    private final MessageBus bus;


    public CustomEditorTabColorProvider() {
        bus = ApplicationManager.getApplication().getMessageBus();
    }

    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        FileColorManager fileColorManager = FileColorManager.getInstance(project);

        if (!EDT.isCurrentThreadEdt()) {
            return fileColorManager.getFileColor(virtualFile);
        }

        LOGGER.warn(String.format("EDT getEditorTabColor(%s, %s)", project.getName(), virtualFile.getName()));

//        final FileEditorManagerEx fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(project);
        SettingsConfig settingsConfig = SettingsConfig.getSettings(project); //FIXME: just project settings?

        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        String file = virtualFile.getCanonicalPath();

        FileEditor selectedEditor;
        selectedEditor = fileEditorManager.getSelectedEditor(virtualFile);

        if (selectedEditor != null && settingsConfig.isEnabled()) {
            if (virtualFile.equals(selectedEditor.getFile())) {
                return settingsConfig.getBackgroundColor();
            }
        }

//        bus.syncPublisher(TabsListener.TABS_TOPIC).onTab(new TabEvent(virtualFile));

        return fileColorManager.getFileColor(virtualFile);
    }
}
