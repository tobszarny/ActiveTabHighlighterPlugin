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

package com.tobszarny.intellij.plugin.activetabhighlighter.config.model.legacy;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.Constants;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(name = "ActiveTabHighlighterConfiguration",
        storages = {
                @Storage(Constants.ACTIVE_TAB_HIGHLIGHTER_CONFIG_PROJECT_XML)
        })
public class V1ProjectConfig implements PersistentStateComponent<V1ProjectConfig.PersistentState> {

    public static final String GROUP = "Highlighter";
    public static final String EXTERNAL_ID = "HIGHLIGHTER_TAB";
    private static final Logger LOGGER = Logger.getInstance(V1ProjectConfig.class);
    public HighlightedTabTextAttributesDescription attributesDescription;
    V1ProjectConfig.PersistentState persistentState;
    private Color backgroundColor;

    public V1ProjectConfig() {
        setDefaults();
    }

    @Nullable
    public static V1ProjectConfig getSettings(Project project) {
        return project.getService(V1ProjectConfig.class);
    }

    public void setDefaults() {
        LOGGER.debug("***** setDefaults() ");
        persistentState = new V1ProjectConfig.PersistentState();
        TextAttributes attributes = new TextAttributes();
        attributes.setBackgroundColor(backgroundColor);
        TextAttributesKey textAttributesKey = TextAttributesKey.createTextAttributesKey(EXTERNAL_ID);
        attributesDescription = new HighlightedTabTextAttributesDescription(GROUP, GROUP, attributes, textAttributesKey, EditorColorsManager.getInstance().getGlobalScheme());
    }

    @Nullable
    @Override
    public PersistentState getState() {
        return persistentState;
    }

    @Override
    public void loadState(V1ProjectConfig.PersistentState persistentState) {
        LOGGER.debug("***** loadState() " + persistentState);
        XmlSerializerUtil.copyBean(persistentState, this.persistentState);
        backgroundColor = persistentState.getBackgroundColor();
        updateAttributes(persistentState);
    }

    private void rebuildHighlightColorIfNecessary() {
        if (backgroundColor != null) {
            if (persistentState.isBackgroundColorDifferentThan(backgroundColor)) {
                LOGGER.debug("***** Rebuilding highlight color");
                LOGGER.debug("Color changed from  " + backgroundColor + " to " + persistentState);
                backgroundColor = persistentState.getBackgroundColor();
                updateAttributes(persistentState);
            }
        }
    }

    public Color getBackgroundColor() {
//        LOGGER.debug("***** getBackgroundColor  " + backgroundColor);
        rebuildHighlightColorIfNecessary();
        return backgroundColor;
    }

    private void updateAttributes(PersistentState state) {
//        LOGGER.debug("***** updateAttributes(" + state + ")");
        attributesDescription.setBackgroundColor(state.getBackgroundColor());
        attributesDescription.setBackgroundChecked(state.isBackgroundColorUsed());
    }

    public HighlightedTabTextAttributesDescription getAttributesDescription() {
        return attributesDescription;
    }

    public static class PersistentState {

        public PersistentColor background;
        public PersistentColor foreground;

        public PersistentState() {
            background = new PersistentColor();
            foreground = new PersistentColor();
        }

        public boolean isStatePresent() {
            return background.getColor() != null || foreground.getColor() != null;
        }

        public Color getBackgroundColor() {
            return background.getColor();
        }

        public Color getForegroundColor() {
            return foreground.getColor();
        }

        public void storeBackgroundColorInformation(boolean enabled, Color color) {
            background.enabled = enabled;
            if (enabled) {
                if (color == null) {
                    throw new NullPointerException("Color cannot be null when enabled");
                } else {
                    background.red = color.getRed();
                    background.green = color.getGreen();
                    background.blue = color.getBlue();
                }
            }
        }

        public boolean isBackgroundColorDifferentThan(Color color) {
            return !background.red.equals(color.getRed()) || !background.green.equals(color.getGreen()) || !background.blue.equals(color.getBlue());
        }

        @Override
        public String toString() {
            return "PersistentState{" +
                    "background=" + background +
                    ", foreground=" + foreground +
                    '}';
        }

        public boolean isBackgroundColorUsed() {
            return background.enabled;
        }
    }

    public static class PersistentColor {
        public boolean enabled = false;
        public Integer red;
        public Integer green;
        public Integer blue;

        public Color getColor() {
            if (!enabled) {
                return null;
            } else {
                return new Color(red, green, blue);
            }
        }

        @Override
        public String toString() {
            return "PersistentColor{" +
                    (enabled ? "enabled" : "disabled") +
                    ", red=" + red +
                    ", green=" + green +
                    ", blue=" + blue +
                    '}';
        }
    }
}

