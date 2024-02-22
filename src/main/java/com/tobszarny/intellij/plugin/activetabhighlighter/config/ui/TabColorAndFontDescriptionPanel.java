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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorSchemeAttributeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.PersistentConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsGlobalConfig;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.SettingsProjectConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author cdr
 */
public class TabColorAndFontDescriptionPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(TabColorAndFontDescriptionPanel.class);

    private final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);
    private final Map<String, EffectType> myEffectsMap;
    private final boolean myUiEventsEnabled = true;
    private JPanel mainPanel;
    //region Global Panel
    private JPanel globalPanel;
    private JBCheckBox globalConfigEnableJBCheckBox;

    //endregion
    private JLabel globalLabel;
    private ColorAndFontDescriptionPanel globalColorAndFontDescriptionPanel;
    //region Project Panel
    private JPanel projectPanel;
    private JLabel projectPrivateLabel;
    //endregion
    private JBCheckBox projectOverrideJBCheckBox;
    private ColorAndFontDescriptionPanel projectColorAndFontDescriptionPanel;

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

    public TabColorAndFontDescriptionPanel() {
        super(new BorderLayout());

        JBEmptyBorder titleBorder = JBUI.Borders.empty(0, 0, 4, 0);
        globalLabel.setBorder(titleBorder);
        projectPrivateLabel.setBorder(titleBorder);

        add(mainPanel, BorderLayout.CENTER);

        setBorder(JBUI.Borders.empty(4, 0, 4, 4));

        initPanelComponentsBehavior();
        //noinspection unchecked
    }

    private static void updateColorChooser(JCheckBox checkBox,
                                           ColorPanel colorPanel,
                                           boolean isEnabled,
                                           boolean isChecked,
                                           @Nullable Color color) {
        checkBox.setEnabled(isEnabled);
        checkBox.setSelected(isChecked);
        if (color != null) {
            colorPanel.setSelectedColor(color);
        } else {
            colorPanel.setSelectedColor(JBColor.WHITE);
        }
        colorPanel.setEnabled(isChecked);
    }

    private void initPanelComponentsBehavior() {
        globalConfigEnableJBCheckBox.addActionListener(e -> {
            globalColorAndFontDescriptionPanel.setVisible(globalConfigEnableJBCheckBox.isSelected());
            projectColorAndFontDescriptionPanel.setVisible(globalConfigEnableJBCheckBox.isSelected() && projectOverrideJBCheckBox.isSelected());
            globalLabel.setVisible(globalConfigEnableJBCheckBox.isSelected());
            projectPrivateLabel.setVisible(globalConfigEnableJBCheckBox.isSelected());
            projectOverrideJBCheckBox.setVisible(globalConfigEnableJBCheckBox.isSelected());

            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });


        projectOverrideJBCheckBox.addActionListener(e -> {
            projectColorAndFontDescriptionPanel.setVisible(globalConfigEnableJBCheckBox.isSelected() && projectOverrideJBCheckBox.isSelected());
            projectPrivateLabel.setVisible(globalConfigEnableJBCheckBox.isSelected() && projectOverrideJBCheckBox.isSelected());

            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

    }

    @NotNull
    public JComponent getPanel() {
        return this;
    }

    public void reset(@NotNull EditorSchemeAttributeDescriptor attrDescription) {
        LOGGER.debug("reset(attrDescription) called");
//        try {
//            myUiEventsEnabled = false;
//            if (!(attrDescription instanceof ColorAndFontDescription)) return;
//            ColorAndFontDescription description = (ColorAndFontDescription) attrDescription;
//
//            updateColorChooser(backgroundCheckBox, backgroundChooser, description.isBackgroundEnabled(),
//                    description.isBackgroundChecked(), description.getBackgroundColor());
//
//            setInheritanceInfo(description);
//        } finally {
//            myUiEventsEnabled = true;
//        }
    }


    public void apply(@NotNull EditorSchemeAttributeDescriptor attrDescription, EditorColorsScheme scheme) {

        //Propagate event
        LOGGER.debug("apply(attrDescription, scheme) called");

    }

    public void addListener(@NotNull Listener listener) {
        myDispatcher.addListener(listener);
    }


    public PersistentConfig generateGlobalConfig() {
        return PersistentConfig.builder()
                .enabled(globalConfigEnableJBCheckBox.isSelected())
                .acrossThemes(globalColorAndFontDescriptionPanel.isSameColorAllThemesCheckBoxSelected())
                .backgroundEnabled(globalColorAndFontDescriptionPanel.isBackgroundCheckBoxSelected())
                .backgroundFromColor(globalColorAndFontDescriptionPanel.getBackgroundChooserColor())
                .backgroundDarkFromColor(globalColorAndFontDescriptionPanel.getBackgroundDarkChooserColor())
                .build();
    }

    public PersistentConfig generateProjectConfig() {
        return PersistentConfig.builder()
                .enabled(projectOverrideJBCheckBox.isSelected())
                .acrossThemes(projectColorAndFontDescriptionPanel.isSameColorAllThemesCheckBoxSelected())
                .backgroundEnabled(projectColorAndFontDescriptionPanel.isBackgroundCheckBoxSelected())
                .backgroundFromColor(projectColorAndFontDescriptionPanel.getBackgroundChooserColor())
                .backgroundDarkFromColor(projectColorAndFontDescriptionPanel.getBackgroundDarkChooserColor())
                .build();
    }

    public boolean anyModified(SettingsGlobalConfig globalConfig, SettingsProjectConfig projectConfig) {
        boolean modified = globalConfigEnableJBCheckBox.isSelected() != globalConfig.getState().isEnabled();
        modified = modified || (projectOverrideJBCheckBox.isSelected() != projectConfig.getState().isEnabled());
        modified = modified || globalColorAndFontDescriptionPanel.isModified(globalConfig);
        modified = modified || projectColorAndFontDescriptionPanel.isModified(projectConfig);

        return modified;
    }

    public void primeGlobalPanel(PersistentConfig persistentConfig) {
        globalConfigEnableJBCheckBox.setSelected(persistentConfig.isEnabled());
        globalColorAndFontDescriptionPanel.primePanel(persistentConfig);
    }

    public void primeProjectPanel(PersistentConfig persistentConfig) {
        projectOverrideJBCheckBox.setSelected(persistentConfig.isEnabled());
        projectColorAndFontDescriptionPanel.primePanel(persistentConfig);
    }


    interface Listener extends EventListener {
        void onSettingsChanged(@NotNull ActionEvent e);

        void onHyperLinkClicked(@NotNull HyperlinkEvent e);
    }


}
