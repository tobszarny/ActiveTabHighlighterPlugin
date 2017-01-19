package com.tobszarn.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import static com.tobszarn.intellij.plugin.activetabhighlighter.CustomEditorTabColorProvider.HIGHLIGHTED_TAB_BG_COLOUR;

/**
 * Main application ActiveTabsHighlighter component
 * Created by Tomasz Obszarny on 19.01.2017.
 */
public class ActiveTabsHighlighterComponent implements ApplicationComponent {

    private static final Logger logger = Logger.getInstance(ActiveTabsHighlighterComponent.class);

    private MessageBusConnection connection;

    public ActiveTabsHighlighterComponent() {
    }

    @Override
    public void initComponent() {
        logger.debug("Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        connection = bus.connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
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
                EditorWindow activeWindow = manager.getCurrentWindow();
                final VirtualFile oldFile = fileEditorManagerEvent.getOldFile();
                final VirtualFile newFile = fileEditorManagerEvent.getNewFile();


                if (null != oldFile) {
                    final int index = activeWindow.findEditorIndex(activeWindow.findFileComposite(oldFile));
                    activeWindow.getTabbedPane().setBackgroundColorAt(index, fileColorManager.getFileColor(oldFile));
                }
                if (null != newFile) {
                    final int index = activeWindow.findEditorIndex(activeWindow.findFileComposite(newFile));
                    activeWindow.getTabbedPane().setBackgroundColorAt(index, HIGHLIGHTED_TAB_BG_COLOUR);
                }

                logger.info("File changed from" + (oldFile == null ? "none" : oldFile.getCanonicalPath()) + "\n\tto:" +
                        newFile.getCanonicalPath());
            }
        });
    }

    @Override
    public void disposeComponent() {
        logger.debug("Disposing component");
        connection.disconnect();
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "com.tabhighlighter.ActiveTabsHighlighterComponent";
    }
}
