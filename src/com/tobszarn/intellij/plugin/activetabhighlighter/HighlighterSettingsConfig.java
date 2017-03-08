package com.tobszarn.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(name = "activeTabHighlightPlugin",
        storages = {
                @Storage(value = "activeTabHighlightPlugin.xml",
                        roamingType = RoamingType.PER_OS)
        })
public class HighlighterSettingsConfig implements PersistentStateComponent<HighlighterSettingsConfig> {

    public Integer red = 173;
    public Integer green = 46;
    public Integer blue = 156;

    @Nullable
    @Override
    public HighlighterSettingsConfig getState() {
        return this;
    }

    @Override
    public void loadState(HighlighterSettingsConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Nullable
    public static HighlighterSettingsConfig getInstance(Project project) {
        HighlighterSettingsConfig sfec = ServiceManager.getService(project, HighlighterSettingsConfig.class);
        return sfec;
    }

    public Color buildHighlightColor() {
        return new Color(red, green, blue);
    }

    public void storeHighlightColor(Color highlightColor) {
        this.red = highlightColor.getRed();
        this.green = highlightColor.getGreen();
        this.blue = highlightColor.getBlue();

    }

    public Integer getRed() {
        return red;
    }

    public void setRed(Integer red) {
        this.red = red;
    }

    public Integer getGreen() {
        return green;
    }

    public void setGreen(Integer green) {
        this.green = green;
    }

    public Integer getBlue() {
        return blue;
    }

    public void setBlue(Integer blue) {
        this.blue = blue;
    }
}
