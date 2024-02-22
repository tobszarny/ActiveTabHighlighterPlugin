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

package com.tobszarny.intellij.plugin.activetabhighlighter.config.controller;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangedEvent;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.Constants;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.PersistentConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsGlobalConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsProjectConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.legacy.V1ProjectConfig;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;

public class SettingsConfigService {

    private static final Logger LOGGER = Logger.getInstance(SettingsConfigService.class);
    private final Project myProject;
    private final SettingsGlobalConfig globalConfig;
    private final SettingsProjectConfig projectConfig;
    private final V1ProjectConfig legacyConfig;
    private final MessageBus bus;

    public SettingsConfigService(Project project) {
        this.bus = ApplicationManager.getApplication().getMessageBus();
        this.myProject = project;
        this.globalConfig = SettingsGlobalConfig.getSettings();
        this.projectConfig = Optional.ofNullable(project)
                .map(SettingsProjectConfig::getSettings)
                .orElse(null);
        this.legacyConfig = Optional.ofNullable(project)
                .map(V1ProjectConfig::getSettings)
                .orElse(null);

        init();
    }

    @Nullable
    public static SettingsConfigService getSettingsConfigService(Project project) {
        return project.getService(SettingsConfigService.class);
    }

    private void init() {
        if (isMigrationApplicable()) {
            migrateLegacyProjectSettings();
        }
    }

    private void migrateLegacyProjectSettings() {
        V1ProjectConfig.PersistentState legacyConfigState = legacyConfig.getState();
        PersistentConfig migratedState = PersistentConfig.builder()
                .backgroundEnabled(true)
                .backgroundFromColor(legacyConfigState.background.getColor())
                .backgroundDarkFromColor(legacyConfigState.background.getColor())
                .acrossThemes(true)
                .enabled(true)
                .migration(Constants.PRESENT_MIGRATION_VERSION)
                .build();

        projectConfig.storeConfig(migratedState);

        bus.syncPublisher(SettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC).settingsChanged(new SettingsChangedEvent(this));
        LOGGER.info("***** Migrated legacy project settings");
    }

    private boolean isMigrationApplicable() {
        return legacyConfig.getState().isStatePresent() && !Constants.PRESENT_MIGRATION_VERSION.equals(projectConfig.getState().migration);
    }

    public boolean isEnabled() {
        return Optional.ofNullable(this.globalConfig)
                .map(SettingsGlobalConfig::getState)
                .map(PersistentConfig::isEnabled)
                .orElse(false);
    }

    public Optional<Color> getBackgroundColorOptional() {
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
        return Optional.ofNullable(color);
    }
}
