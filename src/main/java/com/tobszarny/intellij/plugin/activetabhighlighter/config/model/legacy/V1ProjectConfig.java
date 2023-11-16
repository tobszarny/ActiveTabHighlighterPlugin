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
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(name = "ActiveTabHighlighterConfiguration",
        storages = {
                @Storage("active-tab-highlighter.xml")
        })
public class V1ProjectConfig implements PersistentStateComponent<V1ProjectConfig.PersistentState> {

    public static final String GROUP = "Highlighter";
    public static final String EXTERNAL_ID = "HIGHLIGHTER_TAB";
    private static final Logger LOGGER = Logger.getInstance(V1ProjectConfig.class);
    public HighlightedTabTextAttributesDescription attributesDescription;
    PersistentState persistentState;
    private Color backgroundColor;

    public V1ProjectConfig() {
        setDefaults();
    }

    @Nullable
    public static V1ProjectConfig getSettings(Project project) {
        return project.getService(V1ProjectConfig.class);
    }

    public void setDefaults() {
//        LOGGER.info("*****setDefaults() ");
        persistentState = new PersistentState();
        persistentState.background.enabled = true;
        persistentState.background.red = 173;
        persistentState.background.green = 46;
        persistentState.background.blue = 156;
        backgroundColor = persistentState.getBackgroundColor();
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
    public void loadState(PersistentState persistentState) {
//        LOGGER.info("*****LOADING " + persistentState);
        XmlSerializerUtil.copyBean(persistentState, this.persistentState);
        backgroundColor = persistentState.getBackgroundColor();
        updateAttributes(persistentState);
    }

    private void rebuildHighlightColorIfNecessary() {
        if (backgroundColor != null) {
//            LOGGER.info("*****REBUILDING COLOUR  " + attributesDescription.getBackgroundColor());
            if (persistentState.isBackgroundColorDifferentThan(backgroundColor)) {
                LOGGER.info("Rebuilding highlight color");
                LOGGER.debug("Color changed from  " + backgroundColor + " to " + persistentState);
                backgroundColor = persistentState.getBackgroundColor();
                updateAttributes(persistentState);
            }
        }
    }

    public boolean isBackgroundColorUsed() {
        return persistentState.isBackgroundColorUsed();

    }

    public Color getBackgroundColor() {
//        LOGGER.info("*****getBackgroundColor  " + backgroundColor);
        rebuildHighlightColorIfNecessary();
        return backgroundColor;
    }

    public void storeBackgroundColorInformation(boolean enabled, Color color) {
//        LOGGER.info("*****SAVE " + enabled + " " + color);
        this.persistentState.storeBackgroundColorInformation(enabled, color);

        updateAttributesBackgroundColor(enabled, color);
    }

    private void updateAttributes(PersistentState state) {
//        LOGGER.info("*****updateAttributes(" + state + ")");
        attributesDescription.setBackgroundColor(state.getBackgroundColor());
        attributesDescription.setBackgroundChecked(state.isBackgroundColorUsed());
    }

    private void updateAttributesBackgroundColor(boolean enabled, Color color) {
//        LOGGER.info("*****UPDATE BG COLOR " + enabled + "" + color);
        attributesDescription.setBackgroundColor(color);
        attributesDescription.setBackgroundChecked(enabled);
    }

    public HighlightedTabTextAttributesDescription getAttributesDescription() {
        return attributesDescription;
    }

    static class PersistentState {

        public PersistentColor background;
        public PersistentColor foreground;

        public PersistentState() {
            background = new PersistentColor();
            foreground = new PersistentColor();
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

    static class PersistentColor {
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

