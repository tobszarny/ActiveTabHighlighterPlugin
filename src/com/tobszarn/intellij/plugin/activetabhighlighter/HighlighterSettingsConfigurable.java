package com.tobszarn.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HighlighterSettingsConfigurable implements SearchableConfigurable {

    public static final String PREFERENCE_HIGHLIGHTER_SETTINGS_CONFIGURABLE = "preference.HighlighterSettingsConfigurable";
    public static final String ACTIVE_TAB_HIGHLIGHTER_PLUGIN_DISPLAY_NAME = "Active Tab Highlighter Plugin";

    private SettingsGUI settingsGUI;
    private final Project project;
    private final HighlighterSettingsConfig config;

    public HighlighterSettingsConfigurable(@NotNull Project project) {
        this.project = project;
        this.config = HighlighterSettingsConfig.getInstance(project);
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
        settingsGUI = new SettingsGUI();
        settingsGUI.createUI(project);
        return settingsGUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return settingsGUI.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        settingsGUI.apply();
    }

    @Override
    public void reset() {
        settingsGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        settingsGUI = null;
    }

}
