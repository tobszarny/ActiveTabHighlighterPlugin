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

package com.tobszarny.intellij.plugin.activetabhighlighter.config.ui;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.JBUI;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.PersistentConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * @author Tomasz Obszarny
 */
public class ColorAndFontDescriptionPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(ColorAndFontDescriptionPanel.class);

    private final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);


    //region Global Panel
    private JPanel mainPanel;
    private JBCheckBox enable;
    private JBCheckBox backgroundCheckBox;
    private JCheckBox sameColorAllThemesCheckBox;
    private ColorPanel backgroundChooser;
    private ColorPanel backgroundDarkChooser;
    private JLabel darkLabel;
    private JLabel lightLabel;
    //endregion

    private final Map<String, EffectType> myEffectsMap;
    private final boolean myUiEventsEnabled = true;

    {
        Map<String, EffectType> map = new LinkedHashMap();
        map.put(ApplicationBundle.message("combobox.effect.underscored"), EffectType.LINE_UNDERSCORE);
        map.put(ApplicationBundle.message("combobox.effect.boldunderscored"), EffectType.BOLD_LINE_UNDERSCORE);
        map.put(ApplicationBundle.message("combobox.effect.underwaved"), EffectType.WAVE_UNDERSCORE);
        map.put(ApplicationBundle.message("combobox.effect.bordered"), EffectType.BOXED);
        map.put(ApplicationBundle.message("combobox.effect.strikeout"), EffectType.STRIKEOUT);
        map.put(ApplicationBundle.message("combobox.effect.bold.dottedline"), EffectType.BOLD_DOTTED_LINE);
        myEffectsMap = Collections.unmodifiableMap(map);
    }

    public ColorAndFontDescriptionPanel() {
        super(new BorderLayout());

        setBorder(JBUI.Borders.empty(4, 0, 4, 4));
        //noinspection unchecked
        initProjectPanelComponentsBehavior();
    }

    private void initProjectPanelComponentsBehavior() {
        sameColorAllThemesCheckBox.addActionListener(e -> {
            updateUi();
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });


        backgroundCheckBox.addActionListener(e -> {
            updateUi();
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        updateUi();
    }

    private void updateUi() {
        LOGGER.info("Updating UI");
        backgroundChooser.setVisible(this.isVisible() && backgroundCheckBox.isSelected());
        backgroundDarkChooser.setVisible(this.isVisible() && backgroundCheckBox.isSelected() && !sameColorAllThemesCheckBox.isSelected());
        lightLabel.setVisible(this.isVisible() && backgroundCheckBox.isSelected() && !sameColorAllThemesCheckBox.isSelected());
        darkLabel.setVisible(this.isVisible() && backgroundCheckBox.isSelected() && !sameColorAllThemesCheckBox.isSelected());
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        enable.setVisible(aFlag);
        backgroundChooser.setVisible(aFlag);
        backgroundDarkChooser.setVisible(aFlag);
        lightLabel.setVisible(aFlag);
        darkLabel.setVisible(aFlag);
        sameColorAllThemesCheckBox.setVisible(aFlag);
        backgroundCheckBox.setVisible(aFlag);
    }

    public boolean isModified(PersistentStateComponent<PersistentConfig> globalConfig) {
        PersistentConfig state = globalConfig.getState();

        if (state == null) {
            return false;
        }

        boolean modified = !Objects.equals(state.isEnabled(), enable.isSelected());

        if (enable.isSelected()) {
            modified = modified || checkChanges(state);
        }

        return modified;
    }

    private boolean checkChanges(PersistentConfig state) {
        boolean modified = !Objects.equals(state.isAcrossThemes(), sameColorAllThemesCheckBox.isSelected());
        modified = modified || !Objects.equals(state.isBackgroundEnabled(), backgroundCheckBox.isSelected());
        if (backgroundCheckBox.isSelected()) {
            modified = modified || checkBackgrounds(state);
        }
        return modified;
    }

    private boolean checkBackgrounds(PersistentConfig state) {
        boolean modified = !Objects.equals(state.getBackgroundColor(), backgroundChooser.getSelectedColor());
        if (!sameColorAllThemesCheckBox.isSelected()) {
            modified = modified || !Objects.equals(state.getBackgroundDarkColor(), backgroundDarkChooser.getSelectedColor());
        }
        return modified;
    }

    public void primePanel(PersistentConfig persistentConfig) {
        if (persistentConfig == null) {
            return;
        }

        enable.setSelected(persistentConfig.isEnabled());
        backgroundCheckBox.setSelected(persistentConfig.isBackgroundEnabled());
        sameColorAllThemesCheckBox.setSelected(persistentConfig.isAcrossThemes());
        backgroundChooser.setSelectedColor(persistentConfig.getBackgroundColor());
        backgroundDarkChooser.setSelectedColor(persistentConfig.getBackgroundDarkColor());

        updateUi();
    }

    interface Listener extends EventListener {

        void onSettingsChanged(@NotNull ActionEvent e);

        void onHyperLinkClicked(@NotNull HyperlinkEvent e);

    }

    public boolean isSameColorAllThemesCheckBoxSelected() {
        return this.sameColorAllThemesCheckBox.isSelected();
    }

    public boolean isBackgroundCheckBoxSelected() {
        return this.backgroundCheckBox.isSelected();
    }

    public Color getBackgroundChooserColor() {
        return this.backgroundChooser.getSelectedColor();
    }

    public Color getBackgroundDarkChooserColor() {
        return this.backgroundDarkChooser.getSelectedColor();
    }


}
