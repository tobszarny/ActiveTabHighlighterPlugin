package com.tobszarn.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import org.jetbrains.annotations.NotNull;

import static com.tobszarn.intellij.plugin.activetabhighlighter.CustomEditorTabColorProvider.HIGHLIGHTED_TAB_BG_COLOUR;

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
        FileColorManager fileColorManager = FileColorManager.getInstance(project);

        final VirtualFile oldFile = fileEditorManagerEvent.getOldFile();
        final VirtualFile newFile = fileEditorManagerEvent.getNewFile();

        for (EditorWindow editorWindow : manager.getWindows()) {
            if (null != oldFile) {
                final int index = editorWindow.findEditorIndex(editorWindow.findFileComposite(oldFile));
                if (index >= 0) {
                    editorWindow.getTabbedPane().setBackgroundColorAt(index, fileColorManager.getFileColor(oldFile));
                }
            }

            if (null != newFile) {
                final int index = editorWindow.findEditorIndex(editorWindow.findFileComposite(newFile));
                if (index >= 0) {
                    editorWindow.getTabbedPane().setBackgroundColorAt(index, HIGHLIGHTED_TAB_BG_COLOUR);
                }
            }
        }
    }
}
