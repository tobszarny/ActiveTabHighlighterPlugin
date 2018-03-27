package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
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
                @Storage(value = "active-tab-highlighter.xml")
        })
public class HighlighterSettingsConfig implements PersistentStateComponent<HighlighterSettingsConfig.State> {

    public static final String GROUP = "Highlighter";
    public static final String EXTERNAL_ID = "HIGHLIGHTER_TAB";
    private static final Logger LOGGER = Logger.getInstance(HighlighterSettingsConfig.class);
    public HighlightedTabTextAttributesDescription attributesDescription;
    State state;
    private Color backgroundColor;

    public HighlighterSettingsConfig() {
        state = new State();
        state.red = 173;
        state.green = 46;
        state.blue = 156;
        backgroundColor = new Color(state.red, state.green, state.blue);
        TextAttributes attributes = new TextAttributes();
        attributes.setBackgroundColor(backgroundColor);
        TextAttributesKey textAttributesKey = TextAttributesKey.createTextAttributesKey(EXTERNAL_ID);
        attributesDescription = new HighlightedTabTextAttributesDescription(GROUP, GROUP, attributes, textAttributesKey, EditorColorsManager.getInstance().getGlobalScheme());
    }

    @Nullable
    public static HighlighterSettingsConfig getInstance(Project project) {
        HighlighterSettingsConfig sfec = ServiceManager.getService(project, HighlighterSettingsConfig.class);
        return sfec;
    }

    @Nullable
    @Override
    public HighlighterSettingsConfig.State getState() {
        return state;
    }

    @Override
    public void loadState(HighlighterSettingsConfig.State state) {
        LOGGER.info("*****LOADING " + state);
        XmlSerializerUtil.copyBean(state, this.state);
        rebuildHighlightColorIfNecessary();
    }

    private void rebuildHighlightColorIfNecessary() {
        LOGGER.info("*****REBUILDING COLOUR  " + state + " vs " + backgroundColor);
        LOGGER.info("*****REBUILDING COLOUR  " + attributesDescription.getTextAttributes().getBackgroundColor());
        if (backgroundColor != null) {
            if (!state.red.equals(backgroundColor.getRed()) || !state.green.equals(backgroundColor.getGreen()) || !state.blue.equals(backgroundColor.getBlue())) {
                LOGGER.info("*****REBUILDING " + state);
                backgroundColor = new Color(state.red, state.green, state.blue);
                updateAttributesBackgroundColor(backgroundColor);
            }
        }
    }

    public Color getBackgroundColor() {
        rebuildHighlightColorIfNecessary();
        return backgroundColor;
    }

    public void storeBackgroundColor(Color backgroundColor) {
        LOGGER.info("*****SAVE " + backgroundColor);
        this.backgroundColor = backgroundColor;
        this.state.red = backgroundColor.getRed();
        this.state.green = backgroundColor.getGreen();
        this.state.blue = backgroundColor.getBlue();

        updateAttributesBackgroundColor(backgroundColor);
    }

    private void updateAttributesBackgroundColor(Color backgroundColor) {
        LOGGER.info("*****UPDATE BG COLOR " + backgroundColor);
        attributesDescription.getTextAttributes().setBackgroundColor(backgroundColor);
    }

    public HighlightedTabTextAttributesDescription getAttributesDescription() {
        return attributesDescription;
    }

    static class State {

        public Integer red;
        public Integer green;
        public Integer blue;

        @Override
        public String toString() {
            return "State{" +
                    "red=" + red +
                    ", green=" + green +
                    ", blue=" + blue +
                    '}';
        }
    }
}
