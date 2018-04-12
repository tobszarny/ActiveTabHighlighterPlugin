package com.tobszarny.intellij.plugin.activetabhighlighter.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.HighlighterSettingsConfig;
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
        HighlighterSettingsConfig highlighterSettingsConfig = HighlighterSettingsConfig.getInstance();

        if (highlighterSettingsConfig.isBackgroundColorUsed()) {
            EditorWindow activeWindow = fileEditorManagerEx.getCurrentWindow();
            if (activeWindow != null) {
                final EditorWithProviderComposite selectedEditor = activeWindow.getSelectedEditor();

                if (selectedEditor != null && virtualFile.equals(selectedEditor.getFile())) {
                    return highlighterSettingsConfig.getBackgroundColor();
                }
            }
        }

        return fileColorManager.getFileColor(virtualFile);
    }
}
