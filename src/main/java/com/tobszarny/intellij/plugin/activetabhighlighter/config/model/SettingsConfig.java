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

package com.tobszarny.intellij.plugin.activetabhighlighter.config.model;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;

public class SettingsConfig {
    private final Project myProject;
    private final SettingsGlobalConfig globalConfig;
    private final SettingsProjectConfig projectConfig;

    @Nullable
    public static SettingsConfig getSettings(Project project) {
        return project.getService(SettingsConfig.class);
    }

    public SettingsConfig(Project project) {
        this.myProject = project;
        this.globalConfig = SettingsGlobalConfig.getSettings();
        this.projectConfig = Optional.ofNullable(project)
                .map(SettingsProjectConfig::getSettings)
                .orElse(null);
    }

    public boolean isEnabled() {
        return Optional.ofNullable(this.globalConfig)
                .map(SettingsGlobalConfig::getState)
                .map(PersistentConfig::isEnabled)
                .orElse(Optional.ofNullable(this.projectConfig)
                        .map(SettingsProjectConfig::getState)
                        .map(PersistentConfig::isEnabled)
                        .orElse(false));
    }

    public Color getBackgroundColor() {
        final Color color = Optional.ofNullable(this.projectConfig)
                .map(SettingsProjectConfig::getState)
                .filter(PersistentConfig::isEnabled)
                .filter(PersistentConfig::isBackgroundEnabled)
                .map(PersistentConfig::getInferredBackgroundColor)
                .orElse(Optional.ofNullable(this.globalConfig)
                        .map(SettingsGlobalConfig::getState)
                        .filter(PersistentConfig::isEnabled)
                        .map(PersistentConfig::getInferredBackgroundColor)
                        .orElse(null));
        return color;
    }
}
