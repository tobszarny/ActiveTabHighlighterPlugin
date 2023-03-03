/*
 * Copyright (c) 2022 Tomasz Obszarny
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tobszarny.intellij.plugin.activetabhighlighter.config.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.TabTextAttributesDescription;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(name = "ActiveTabHighlighterConfiguration",
        storages = {
                @Storage(Constants.ACTIVE_TAB_HIGHLIGHTER_CONFIG_PROJECT_XML)
        })
public class SettingsProjectConfig implements PersistentStateComponent<PersistentConfig> {

    private static final Logger LOGGER = Logger.getInstance(SettingsProjectConfig.class);
    public TabTextAttributesDescription attributesDescription;
    PersistentConfig persistentConfig;

    public SettingsProjectConfig() {
        setDefaults();
    }

    @Nullable
    public static SettingsProjectConfig getSettings(Project project) {
        return project.getService(SettingsProjectConfig.class);
    }

    public void setDefaults() {
//        LOGGER.info("*****setDefaults() ");
        persistentConfig = new PersistentConfig();
        persistentConfig.enabled = true;
        persistentConfig.background.color = "#AD2E9C";
        TextAttributes attributes = new TextAttributes();
        attributes.setBackgroundColor(persistentConfig.background.toColor());
        TextAttributesKey textAttributesKey = TextAttributesKey.createTextAttributesKey(Constants.EXTERNAL_ID);
        attributesDescription = new TabTextAttributesDescription(Constants.GROUP, Constants.GROUP, attributes, textAttributesKey, EditorColorsManager.getInstance().getGlobalScheme());
    }

    @Nullable
    @Override
    public PersistentConfig getState() {
        return persistentConfig;
    }

    @Override
    public void loadState(PersistentConfig persistentState) {
//        LOGGER.info("*****LOADING " + persistentState);
        XmlSerializerUtil.copyBean(persistentState, this.persistentConfig);
        updateAttributes(persistentState);
    }

    private void rebuildHighlightColorIfNecessary() {
        updateAttributes(persistentConfig);
    }

    private void updateAttributes(PersistentConfig state) {
//        LOGGER.info("*****updateAttributes(" + state + ")");
        attributesDescription.setBackgroundChecked(state.isBackgroundEnabled());
        attributesDescription.setBackgroundColor(state.getInferredBackgroundColor());
        attributesDescription.setBackgroundChecked(state.isBackgroundEnabled());
    }

    public TabTextAttributesDescription getAttributesDescription() {
        return attributesDescription;
    }

    public void storeConfig(PersistentConfig config) {
        this.persistentConfig.storeConfig(config);

        updateAttributes(config);
    }

    public boolean isBackgroundColorUsed() {
        return persistentConfig.backgroundEnabled;

    }

    public Color getBackgroundColor() {
        //TODO: check theme and return light or dark
        return this.persistentConfig.getInferredBackgroundColor();
    }
}
