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
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangeListener;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.SettingsChangedEvent;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.PersistentConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsGlobalConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsProjectConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.ui.TabColorAndFontDescriptionPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsConfigurable implements SearchableConfigurable {

    public static final String PREFERENCE_HIGHLIGHTER_SETTINGS_CONFIGURABLE = "preference.SettingsConfigurable";
    public static final String ACTIVE_TAB_HIGHLIGHTER_PLUGIN_DISPLAY_NAME = "Active Tab Highlighter Plugin";
    private static final Logger LOGGER = Logger.getInstance(SettingsConfigurable.class);
    private final SettingsGlobalConfig globalConfig;
    private final SettingsProjectConfig projectConfig;
    private final EditorColorsScheme editorColorsScheme;
    private final MessageBus bus;
    private final Project myProject;

    private TabColorAndFontDescriptionPanel tabColorAndFontDescriptionPanel;

    public SettingsConfigurable(Project project) {
        LOGGER.debug("***** SettingsConfigurable() ");
        myProject = project;
        globalConfig = SettingsGlobalConfig.getSettings();
        projectConfig = SettingsProjectConfig.getSettings(project);
        editorColorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
        bus = ApplicationManager.getApplication().getMessageBus();
    }

    //TODO: Make this class replace function of  inferring properties currently hosted by SettingsConfigService

    @NotNull
    @Override
    public String getId() {
        return PREFERENCE_HIGHLIGHTER_SETTINGS_CONFIGURABLE;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return ACTIVE_TAB_HIGHLIGHTER_PLUGIN_DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return PREFERENCE_HIGHLIGHTER_SETTINGS_CONFIGURABLE;
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        LOGGER.debug("***** createComponent() ");
        tabColorAndFontDescriptionPanel = new TabColorAndFontDescriptionPanel();
        primeComponent();
        return tabColorAndFontDescriptionPanel.getPanel();
    }

    private void primeComponent() {
        // TODO: this may be obsolete when todo below are done, as reset will prime the component
        tabColorAndFontDescriptionPanel.primeGlobalPanel(globalConfig.getState());
        tabColorAndFontDescriptionPanel.primeProjectPanel(projectConfig.getState());
    }

    @Override
    public boolean isModified() {
        LOGGER.debug("***** isModified() ");
//        TabTextAttributesDescription attributesDescription = projectConfig.getAttributesDescription();

//        colorAndFontDescriptionPanel.is
        return tabColorAndFontDescriptionPanel.anyModified(globalConfig, projectConfig);
    }

    @Override
    public void apply() {
        LOGGER.debug("***** apply() ");
//        settingsGUI.apply();

        bus.syncPublisher(SettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC).beforeSettingsChanged(new SettingsChangedEvent(this));

        globalConfig.storeConfig(tabColorAndFontDescriptionPanel.generateGlobalConfig());
        projectConfig.storeConfig(tabColorAndFontDescriptionPanel.generateProjectConfig());

        // TODO: calculate attributesDescription here

        PersistentConfig attributesDescription = effectiveConfig(
                globalConfig.getState(), projectConfig.getState());

//        colorAndFontDescriptionPanel.apply(colorAndFontDescriptionPanel.generateGlobalConfig()., editorColorsScheme);

        bus.syncPublisher(SettingsChangeListener.CHANGE_HIGHLIGHTER_SETTINGS_TOPIC).settingsChanged(new SettingsChangedEvent(this));
    }

    private PersistentConfig effectiveConfig(PersistentConfig globalConfig, PersistentConfig projectConfig) {
        PersistentConfig resultingConfig = null;
        if (projectConfig.enabled) {
            resultingConfig = projectConfig;
        } else {
            resultingConfig = globalConfig;
        }

//        TextAttributes attributes = new TextAttributes();
//        attributes.setBackgroundColor(resultingConfig.background.toColor());
//        TextAttributesKey textAttributesKey = TextAttributesKey.createTextAttributesKey(Constants.EXTERNAL_ID);
//        TabTextAttributesDescription attributesDescription =
//                new TabTextAttributesDescription(Constants.GROUP, Constants.GROUP, attributes,
//                        textAttributesKey, EditorColorsManager.getInstance().getGlobalScheme());

        return resultingConfig;
    }

    @Override
    public void reset() {
        //FIXME: this breaks the coloring, called immediately after opening the dialog
//        colorAndFontDescriptionPanel.reset(projectConfig.getAttributesDescription());``
    }

//    @Override
//    public void disposeUIResources() {
//        LOGGER.debug("***** disposeUIResources() ");
//        colorAndFontDescriptionPanel = null;
//    }


}
