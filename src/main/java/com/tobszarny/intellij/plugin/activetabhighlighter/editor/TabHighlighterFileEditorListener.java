package com.tobszarny.intellij.plugin.activetabhighlighter.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.ui.tabs.TabInfo;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.HighlighterSettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.HighlighterSettingsConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * File Editor Listener implementation for tab highlight
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class TabHighlighterFileEditorListener implements FileEditorManagerListener, HighlighterSettingsChangeListener {

    private static final Logger LOGGER = Logger.getInstance(TabHighlighterFileEditorListener.class);
    private final HighlighterSettingsConfig highlighterSettingsConfig;
    private final Project myProject;

    public TabHighlighterFileEditorListener(Project project) {
        this.myProject = project;
        highlighterSettingsConfig = HighlighterSettingsConfig.getSettings(project);
    }

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


        final VirtualFile oldFile = fileEditorManagerEvent.getOldFile();
        final VirtualFile newFile = fileEditorManagerEvent.getNewFile();

        for (EditorWindow editorWindow : manager.getWindows()) {
            if (oldFile != null && editorWindow.findFileComposite(oldFile) != null) {
                setUnfocusedTabWithColorManagerDefaultColor(fileColorManager, oldFile, editorWindow);
            }

            if (newFile != null && editorWindow.findFileComposite(newFile) != null) {
                setFocusedTabHighlighterColor(newFile, editorWindow);
            }
        }
    }


    private void setFocusedTabHighlighterColor(VirtualFile file, EditorWindow editorWindow) {
        setTabColor(highlighterSettingsConfig.getBackgroundColor(), file, editorWindow);
    }

    private void setUnfocusedTabWithColorManagerDefaultColor(@NotNull FileColorManager fileColorManager, VirtualFile file, EditorWindow editorWindow) {
        setTabColor(fileColorManager.getFileColor(file), file, editorWindow);
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

    @Override
    public void settingsChanged(SettingsChangedEvent context) {
        if (ProjectManager.getInstance().getOpenProjects() != null) {
            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);

                if (manager.getWindows() != null) {
                    for (EditorWindow editorWindow : manager.getWindows()) {
                        TabInfo selected = editorWindow.getTabbedPane().getTabs().getSelectedInfo();
                        selected.setTabColor(highlighterSettingsConfig.getBackgroundColor());
                    }
                }

            }
        }

    }
}
