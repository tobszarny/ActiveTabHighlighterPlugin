package com.tobszarn.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Custom implementation of EditorTabColorProvider
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class CustomEditorTabColorProvider implements EditorTabColorProvider {

    private static final Logger logger = Logger.getInstance(CustomEditorTabColorProvider.class);

    public static final Color HIGHLIGHTED_TAB_BG_COLOUR = new Color(156, 173, 46);

    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        final FileEditorManagerEx fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(project);
        FileColorManager fileColorManager = FileColorManager.getInstance(project);

        EditorWindow activeWindow = fileEditorManagerEx.getCurrentWindow();
        if (activeWindow != null) {
            final EditorWithProviderComposite selectedEditor = activeWindow.getSelectedEditor();

            if (selectedEditor != null && selectedEditor.getFile() != null && selectedEditor.getFile().equals(virtualFile)) {
                return HIGHLIGHTED_TAB_BG_COLOUR;
            }
        }

        return fileColorManager.getFileColor(virtualFile);
    }
}
