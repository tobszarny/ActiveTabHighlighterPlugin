package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * File Editor Listener implementation for tab highlight
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class TabHighlighterFileEditorListener implements FileEditorManagerListener {

    private static final Logger logger = Logger.getInstance(TabHighlighterFileEditorListener.class);

    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent fileEditorManagerEvent) {
        final Project project = fileEditorManagerEvent.getManager().getProject();
        final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
        final FileColorManager fileColorManager = FileColorManager.getInstance(project);
        final HighlighterSettingsConfig highlighterSettingsConfig = HighlighterSettingsConfig.getInstance(project);

        final VirtualFile oldFile = fileEditorManagerEvent.getOldFile();
        final VirtualFile newFile = fileEditorManagerEvent.getNewFile();

        for (EditorWindow editorWindow : manager.getWindows()) {
            setUnfocusedTabWithColorManagerDefaultColor(fileColorManager, oldFile, editorWindow);

            setFocusedTabHighlighterColor(highlighterSettingsConfig, newFile, editorWindow);
        }
    }

    private void setFocusedTabHighlighterColor(@NotNull HighlighterSettingsConfig highlighterSettingsConfig, VirtualFile file, EditorWindow editorWindow) {
        if (null != file) {
            setTabColor(highlighterSettingsConfig.getBackgroundColor(), file, editorWindow);
        }
    }

    private void setUnfocusedTabWithColorManagerDefaultColor(@NotNull FileColorManager fileColorManager, VirtualFile file, EditorWindow editorWindow) {
        if (null != file) {
            setTabColor(fileColorManager.getFileColor(file), file, editorWindow);
        }
    }

    private void setTabColor(Color color, @NotNull VirtualFile file, @NotNull EditorWindow editorWindow) {
        final EditorWithProviderComposite fileComposite = editorWindow.findFileComposite(file);

        final int index = getEditorIndex(editorWindow, fileComposite);
        if (index >= 0) {
            if (editorWindow.getTabbedPane() != null) { //Distraction free mode // Presentation mode
                editorWindow.getTabbedPane().getTabs().getTabAt(index).setTabColor(color);
            }
        }
    }

    private int getEditorIndex(@NotNull EditorWindow editorWindow, EditorWithProviderComposite fileComposite) {
        int index = -1;
        for (EditorWithProviderComposite editorWithProviderComposite : editorWindow.getEditors()) {
            index++;
            if (editorWithProviderComposite.equals(fileComposite)) {
                break;
            }
        }

        return index;
    }
}
