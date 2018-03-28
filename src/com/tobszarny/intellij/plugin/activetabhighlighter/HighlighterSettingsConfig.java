package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(name = "ActiveTabHighlighterConfiguration",
        storages = {
                @Storage(value = "active-tab-highlighter.xml")
        }, additionalExportFile = "active-tab-highlighter")
public class HighlighterSettingsConfig implements PersistentStateComponent<HighlighterSettingsConfig.State>, Disposable, ApplicationComponent {

    public static final String GROUP = "Highlighter";
    public static final String EXTERNAL_ID = "HIGHLIGHTER_TAB";
    private static final Logger LOGGER = Logger.getInstance(HighlighterSettingsConfig.class);
    public HighlightedTabTextAttributesDescription attributesDescription;
    State state;
    private Color backgroundColor;

    public HighlighterSettingsConfig() {
        setDefaults();
    }

    @Nullable
    public static HighlighterSettingsConfig getInstance() {
        HighlighterSettingsConfig sfec = ServiceManager.getService(HighlighterSettingsConfig.class);
        return sfec;
    }

    public void setDefaults() {
        LOGGER.info("*****setDefaults() ");
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
    @Override
    public HighlighterSettingsConfig.State getState() {
        return state;
    }

    @Override
    public void loadState(HighlighterSettingsConfig.State state) {
        LOGGER.info("*****LOADING " + state);
        XmlSerializerUtil.copyBean(state, this.state);
        backgroundColor = new Color(state.red, state.green, state.blue);
        updateAttributesBackgroundColor(backgroundColor);
    }

    private void rebuildHighlightColorIfNecessary() {
        LOGGER.info("*****REBUILDING COLOUR  " + state + " vs " + backgroundColor);
        LOGGER.info("*****REBUILDING COLOUR  " + attributesDescription.getBackgroundColor());
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
        attributesDescription.setBackgroundColor(backgroundColor);
    }

    public HighlightedTabTextAttributesDescription getAttributesDescription() {
        return attributesDescription;
    }

    @Override
    public void dispose() {
        LOGGER.info("ActiveTabHighlighterConfiguration: disposed");
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "com.tobszarny.intellij.plugin.activetabhighlighter.HighlighterSettingsConfig";
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
