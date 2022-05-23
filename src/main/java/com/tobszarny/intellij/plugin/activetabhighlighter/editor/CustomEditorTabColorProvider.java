/*
 * Copyright (c) 2021 Tomasz Obszarny
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tobszarny.intellij.plugin.activetabhighlighter.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.HighlighterSettingsConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.HighlighterSettingsProjectConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Custom implementation of EditorTabColorProvider
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class CustomEditorTabColorProvider implements EditorTabColorProvider {

    private static final Logger LOGGER = Logger.getInstance(CustomEditorTabColorProvider.class);

    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        final FileEditorManagerEx fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(project);
        FileColorManager fileColorManager = FileColorManager.getInstance(project);
        HighlighterSettingsConfig highlighterSettingsProjectConfig = HighlighterSettingsConfig.getSettings(project);

        if (highlighterSettingsProjectConfig.isBackgroundColorUsed()) {
            EditorWindow activeWindow = fileEditorManagerEx.getCurrentWindow();
            if (activeWindow != null) {
                final EditorWithProviderComposite selectedEditor = activeWindow.getSelectedEditor();

                if (selectedEditor != null && virtualFile.equals(selectedEditor.getFile())) {
                    return highlighterSettingsProjectConfig.getBackgroundColor();
                }
            }
        }

        return fileColorManager.getFileColor(virtualFile);
    }
}
