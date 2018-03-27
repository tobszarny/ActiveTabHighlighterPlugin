package com.tobszarny.intellij.plugin.activetabhighlighter;

//import com.intellij.application.options.colors.ColorAndFontDescriptionPanel;

import com.intellij.application.options.colors.TextAttributesDescription;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class HighlighterSettingsConfigurable implements SearchableConfigurable {

    public static final String PREFERENCE_HIGHLIGHTER_SETTINGS_CONFIGURABLE = "preference.HighlighterSettingsConfigurable";
    public static final String ACTIVE_TAB_HIGHLIGHTER_PLUGIN_DISPLAY_NAME = "Active Tab Highlighter Plugin";
    private final Project project;
    private final HighlighterSettingsConfig config;
    private final EditorColorsScheme editorColorsScheme;
    private SettingsGUI settingsGUI;
    private ColorAndFontDescriptionPanel colorAndFontDescriptionPanel;

    public HighlighterSettingsConfigurable(@NotNull Project project) {
        this.project = project;
        this.config = HighlighterSettingsConfig.getInstance(project);
        this.editorColorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
    }

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
        colorAndFontDescriptionPanel = new ColorAndFontDescriptionPanel();

//        TextAttributes(@Nullable Color foregroundColor, @Nullable Color backgroundColor, @Nullable Color effectColor, EffectType effectType, @JdkConstants.FontStyle int fontType);
        TextAttributes attributes = new TextAttributes(null, config.getBackgroundColor(), null, null, Font.PLAIN);

        TextAttributesDescription textAttributesDescription = new HighlightedTabTextAttributesDescription( /*String name*/ "name",
                /*String group*/null,
                attributes,
                /*TextAttributesKey type*/null,
                editorColorsScheme); // /*, /*Icon*/ null, /*String toolTip*/ null);
        colorAndFontDescriptionPanel.apply(textAttributesDescription, editorColorsScheme);
//        settingsGUI = new SettingsGUI();
//        settingsGUI.createUI(project);
//        return settingsGUI.getRootPanel();
        return colorAndFontDescriptionPanel.getPanel();
    }

    @Override
    public boolean isModified() {
//        colorAndFontDescriptionPanel.is
//        return settingsGUI.isModified();
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
//        settingsGUI.apply();
        HighlightedTabTextAttributesDescription attributesDescription = config.getAttributesDescription();
        colorAndFontDescriptionPanel.apply(attributesDescription, editorColorsScheme);
        config.storeBackgroundColor(colorAndFontDescriptionPanel.getSelectedBackgroundColor());
    }

    @Override
    public void reset() {
//        settingsGUI.reset();
        colorAndFontDescriptionPanel.reset(config.getAttributesDescription());
    }

    @Override
    public void disposeUIResources() {
        settingsGUI = null;
        colorAndFontDescriptionPanel = null;
    }

}
