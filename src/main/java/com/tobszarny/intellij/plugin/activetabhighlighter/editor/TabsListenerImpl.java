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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class TabsListenerImpl implements TabsListener{

    private static final Logger LOGGER = Logger.getInstance(TabsListenerImpl.class);
    private final Project project;

    public TabsListenerImpl(Project project) {
        this.project = project;


    }

    @Override
    public void onTab(TabEvent event) {
        LOGGER.debug("***** onTab");
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        ApplicationManager.getApplication().runWriteAction(() -> {
            FileEditor selectedEditor = fileEditorManager.getSelectedEditor((VirtualFile)event.getSource());
        });

        new Exception().printStackTrace();
    }
}
