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

package com.tobszarny.intellij.plugin.activetabhighlighter.config.ui.bak;

import com.intellij.application.options.colors.ColorAndFontDescription;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorSchemeAttributeDescriptor;
import com.intellij.openapi.editor.colors.EditorSchemeAttributeDescriptorWithPath;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.colors.AbstractKeyDescriptor;
import com.intellij.openapi.options.colors.ColorAndFontDescriptorsProvider;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.EventDispatcher;
import com.intellij.util.FontUtil;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.PersistentConfig;
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
public class AATabColorAndFontDescriptionPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(AATabColorAndFontDescriptionPanel.class);

    private final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);

    private JPanel mainPanel;


    //region Global Panel
    private JPanel globalPanel;
    private JBCheckBox backgroundCheckBox;
    private JBCheckBox enableJBCheckBox;
    private JCheckBox sameColorAllThemesCheckBox;
    private ColorPanel backgroundChooser;
    private ColorPanel backgroundDarkChooser;
    private JLabel darkLabel;
    private JLabel lightLabel;
    private JLabel globalLabel;
    //endregion

    //region Project Panel
    private JPanel projectPanel;
    private JLabel projectPrivateLabel;
    private JBCheckBox projectOverrideJBCheckBox;
    private JCheckBox sameColorAllThemesProjectCheckBox;
    private ColorPanel backgroundProjectChooser;
    private ColorPanel backgroundDarkProjectChooser;
    private JBCheckBox backgroundProjectCheckBox;
    private JLabel lightProjectLabel;
    private JLabel darkProjectLabel;
    //endregion

    private final Map<String, EffectType> myEffectsMap;
    private boolean myUiEventsEnabled = true;

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

    public AATabColorAndFontDescriptionPanel() {
        super(new BorderLayout());

        JBEmptyBorder titleBorder = JBUI.Borders.empty(0, 0, 4, 0);
        globalLabel.setBorder(titleBorder);
        projectPrivateLabel.setBorder(titleBorder);

//        Border thisBorder = BorderFactory.createTitledBorder("Global");
//        globalPanel.setBorder(thisBorder);
//        Border this2Border = BorderFactory.createTitledBorder("Project private");
//        projectPanel.setBorder(this2Border);

        add(mainPanel, BorderLayout.CENTER);

        setBorder(JBUI.Borders.empty(4, 0, 4, 4));
        //noinspection unchecked
    }

    public void primeVisibilityAndBehavior() {
        updateEnabled();
        initGlobalPanelComponentsBehavior();
        initProjectPanelComponentsBehavior();
    }

    private void initProjectPanelComponentsBehavior() {
        projectOverrideJBCheckBox.addActionListener(e -> {
            updateProjectSection();
            copyProjectSectionDefaultValues();
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        sameColorAllThemesProjectCheckBox.addActionListener(e -> {
            updateProjectSection();
            if (!sameColorAllThemesProjectCheckBox.isSelected()) {
                backgroundDarkProjectChooser.setSelectedColor(backgroundDarkChooser.getSelectedColor());
            }
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        backgroundProjectCheckBox.addActionListener(e -> {
            updateProjectSection();
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        backgroundProjectChooser.addActionListener(e -> {
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        backgroundDarkProjectChooser.addActionListener(e -> {
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });
    }

    private void initGlobalPanelComponentsBehavior() {
        enableJBCheckBox.addActionListener((e -> {
            updateEnabled();
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        }));

        sameColorAllThemesCheckBox.addActionListener(e -> {
            updateGlobalSection();
            if (!sameColorAllThemesCheckBox.isSelected()) {
                backgroundDarkChooser.setSelectedColor(backgroundChooser.getSelectedColor());
            }
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        backgroundCheckBox.addActionListener(e -> {
            updateGlobalSection();
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        backgroundChooser.addActionListener(e -> {
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

        backgroundDarkChooser.addActionListener(e -> {
            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });
    }

    private void updateEnabled() {
        updateGlobalSection();
        updateProjectSection();
    }

    private void updateGlobalSection() {
        boolean enabled = enableJBCheckBox.isSelected();
        boolean separateColorsForThemes = !sameColorAllThemesCheckBox.isSelected();
        sameColorAllThemesCheckBox.setVisible(enabled);
        backgroundCheckBox.setVisible(enabled);
        backgroundChooser.setVisible(enabled);
        backgroundChooser.setEnabled(backgroundCheckBox.isSelected());
        lightLabel.setVisible(enabled && separateColorsForThemes);
        darkLabel.setVisible(enabled && separateColorsForThemes);
        backgroundDarkChooser.setVisible(enabled && separateColorsForThemes);
        backgroundDarkChooser.setEnabled(backgroundCheckBox.isSelected());
        projectOverrideJBCheckBox.setVisible(enabled);
        projectPrivateLabel.setVisible(enabled);
    }

    private void updateProjectSection() {
        updateProjectSectionVisibility();
        backgroundProjectChooser.setEnabled(backgroundProjectCheckBox.isSelected());
        backgroundDarkProjectChooser.setEnabled(backgroundProjectCheckBox.isSelected());
    }

    private void copyProjectSectionDefaultValues() {
        sameColorAllThemesProjectCheckBox.setSelected(sameColorAllThemesCheckBox.isSelected());
        sameColorAllThemesProjectCheckBox.setSelected(sameColorAllThemesCheckBox.isSelected());
        backgroundProjectCheckBox.setSelected(backgroundCheckBox.isSelected());
        backgroundProjectChooser.setSelectedColor(backgroundChooser.getSelectedColor());
        backgroundDarkProjectChooser.setSelectedColor(backgroundDarkChooser.getSelectedColor());
    }

    private void updateProjectSectionVisibility() {
        boolean override = enableJBCheckBox.isSelected() && projectOverrideJBCheckBox.isSelected();
        boolean separateColorsForThemes = !sameColorAllThemesProjectCheckBox.isSelected();
        sameColorAllThemesProjectCheckBox.setVisible(override);
        backgroundProjectCheckBox.setVisible(override);
        backgroundProjectChooser.setVisible(override);
        backgroundDarkProjectChooser.setVisible(override && separateColorsForThemes);
        darkProjectLabel.setVisible(override && separateColorsForThemes);
        lightProjectLabel.setVisible(override && separateColorsForThemes);
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

    @NotNull
    public JComponent getPanel() {
        return this;
    }

    public void primeGlobalPanel(PersistentConfig persistentConfig) {
        projectOverrideJBCheckBox.setSelected(persistentConfig.enabled);
        sameColorAllThemesProjectCheckBox.setSelected(persistentConfig.acrossThemes);
        backgroundProjectCheckBox.setSelected(persistentConfig.backgroundEnabled);
        backgroundProjectChooser.setSelectedColor(persistentConfig.getBackgroundColor());
        backgroundDarkProjectChooser.setSelectedColor(persistentConfig.getBackgroundDarkColor());
    }

    public void primeProjectPanel(PersistentConfig persistentConfig) {
        enableJBCheckBox.setSelected(persistentConfig.enabled);
        sameColorAllThemesCheckBox.setSelected(persistentConfig.acrossThemes);
        backgroundChooser.setSelectedColor(persistentConfig.getBackgroundColor());
        backgroundDarkChooser.setSelectedColor(persistentConfig.getBackgroundDarkColor());
    }

    public void resetDefault() {
        LOGGER.debug("resetDefault() called");
        try {
            myUiEventsEnabled = false;
            updateColorChooser(backgroundCheckBox, backgroundChooser, false, false, null);
        } finally {
            myUiEventsEnabled = true;
        }
    }

    public void reset(@NotNull EditorSchemeAttributeDescriptor attrDescription) {
        LOGGER.debug("reset(attrDescription) called");
        try {
            myUiEventsEnabled = false;
            if (!(attrDescription instanceof ColorAndFontDescription)) return;
            ColorAndFontDescription description = (ColorAndFontDescription) attrDescription;

//            if (description.isFontEnabled()) {
//                myLabelFont.setEnabled(description.isEditable());
//                myCbBold.setEnabled(description.isEditable());
//                myCbItalic.setEnabled(description.isEditable());
//                int fontType = description.getFontType();
//                myCbBold.setSelected(BitUtil.isSet(fontType, Font.BOLD));
//                myCbItalic.setSelected(BitUtil.isSet(fontType, Font.ITALIC));
//            } else {
//            }

//            updateColorChooser(myCbForeground, myForegroundChooser, description.isForegroundEnabled(),
//                    description.isForegroundChecked(), description.getForegroundColor());

            updateColorChooser(backgroundCheckBox, backgroundChooser, description.isBackgroundEnabled(),
                    description.isBackgroundChecked(), description.getBackgroundColor());

//            updateColorChooser(myCbErrorStripe, myErrorStripeColorChooser, description.isErrorStripeEnabled(),
//                    description.isErrorStripeChecked(), description.getErrorStripeColor());

//            EffectType effectType = description.getEffectType();
//            updateColorChooser(myCbEffects, myEffectsColorChooser, description.isEffectsColorEnabled(),
//                    description.isEffectsColorChecked(), description.getEffectColor());

//            String name = ContainerUtil.reverseMap(myEffectsMap).get(effectType);
//            myEffectsCombo.getModel().setSelectedItem(name);
//            myEffectsCombo
//                    .setEnabled((description.isEffectsColorEnabled() && description.isEffectsColorChecked()) && description.isEditable());
            setInheritanceInfo(description);
//            myLabelFont.setEnabled(myCbBold.isEnabled() || myCbItalic.isEnabled());
        } finally {
            myUiEventsEnabled = true;
        }
    }


    private void setInheritanceInfo(ColorAndFontDescription description) {
        Pair<ColorAndFontDescriptorsProvider, ? extends AbstractKeyDescriptor> baseDescriptor = description.getFallbackKeyDescriptor();
        if (baseDescriptor != null && baseDescriptor.second.getDisplayName() != null) {
            String attrName = baseDescriptor.second.getDisplayName();
            String attrLabel = attrName.replaceAll(EditorSchemeAttributeDescriptorWithPath.NAME_SEPARATOR, FontUtil.rightArrow(UIUtil.getLabelFont()));
            ColorAndFontDescriptorsProvider settingsPage = baseDescriptor.first;
            String style = "<div style=\"text-align:right\" vertical-align=\"top\">";
            String tooltipText;
            String labelText;
            if (settingsPage != null) {
                String pageName = settingsPage.getDisplayName();
                tooltipText = "Editor | Color Scheme | " + pageName + "<br>" + attrLabel;
                labelText = style + "<a href=\"" + pageName + "\">" + attrLabel + "</a><br>(" + pageName + ")";
            } else {
                tooltipText = attrLabel;
                labelText = style + attrLabel + "<br>&nbsp;";
            }

            setEditEnabled(!description.isInherited() && description.isEditable(), description);
        } else {
            setEditEnabled(description.isEditable(), description);
        }
    }

    private void setEditEnabled(boolean isEditEnabled, ColorAndFontDescription description) {
        backgroundCheckBox.setEnabled(isEditEnabled && description.isBackgroundEnabled());
//        myCbForeground.setEnabled(isEditEnabled && description.isForegroundEnabled());
//        myCbBold.setEnabled(isEditEnabled && description.isFontEnabled());
//        myCbItalic.setEnabled(isEditEnabled && description.isFontEnabled());
//        myCbEffects.setEnabled(isEditEnabled && description.isEffectsColorEnabled());
//        myCbErrorStripe.setEnabled(isEditEnabled && description.isErrorStripeEnabled());
//        myErrorStripeColorChooser.setEditable(isEditEnabled);
//        myEffectsColorChooser.setEditable(isEditEnabled);
//        myForegroundChooser.setEditable(isEditEnabled);
        backgroundChooser.setEditable(isEditEnabled);
    }

    public void apply(@NotNull EditorSchemeAttributeDescriptor attrDescription, EditorColorsScheme scheme) {

        //Propagate event
        LOGGER.debug("apply(attrDescription, scheme) called");
        if (!(attrDescription instanceof ColorAndFontDescription)) return;
        ColorAndFontDescription description = (ColorAndFontDescription) attrDescription;

        if (description.isInherited()) {
            TextAttributes baseAttributes = description.getBaseAttributes();
            if (baseAttributes != null) {
                description.setFontType(baseAttributes.getFontType());
//                description.setForegroundChecked(baseAttributes.getForegroundColor() != null);
//                description.setForegroundColor(baseAttributes.getForegroundColor());
                description.setBackgroundChecked(baseAttributes.getBackgroundColor() != null);
                description.setBackgroundColor(baseAttributes.getBackgroundColor());
//                description.setErrorStripeChecked(baseAttributes.getErrorStripeColor() != null);
//                description.setErrorStripeColor(baseAttributes.getErrorStripeColor());
//                description.setEffectColor(baseAttributes.getEffectColor());
//                description.setEffectType(baseAttributes.getEffectType());
//                description.setEffectsColorChecked(baseAttributes.getEffectColor() != null);
            } else {
                description.setInherited(false);
            }
            reset(description);
        } else {
            setInheritanceInfo(description);
            int fontType = Font.PLAIN;
            description.setFontType(fontType);
//            description.setForegroundChecked(myCbForeground.isSelected());
//            description.setForegroundColor(myForegroundChooser.getSelectedColor());
            description.setBackgroundChecked(backgroundCheckBox.isSelected());
            description.setBackgroundColor(backgroundChooser.getSelectedColor());
//            description.setErrorStripeChecked(myCbErrorStripe.isSelected());
//            description.setErrorStripeColor(myErrorStripeColorChooser.getSelectedColor());
//            description.setEffectsColorChecked(myCbEffects.isSelected());
//            description.setEffectColor(myEffectsColorChooser.getSelectedColor());

//            if (myEffectsCombo.isEnabled()) {
//                String effectType = (String) myEffectsCombo.getModel().getSelectedItem();
//                description.setEffectType(myEffectsMap.get(effectType));
//            }
        }
        description.apply(scheme);
    }

    public void addListener(@NotNull Listener listener) {
        myDispatcher.addListener(listener);
    }

    public boolean isBackgroundColorEnabled() {
        return backgroundCheckBox.isSelected();
    }

    public boolean isBackgroundColorProjectEnabled() {
        return backgroundProjectCheckBox.isSelected();
    }

    public Color getSelectedBackgroundColor() {
        return backgroundCheckBox.isSelected() ? backgroundChooser.getSelectedColor() : null;
    }

    public Color getSelectedBackgroundProjectColor() {
        return backgroundProjectCheckBox.isSelected() ? backgroundProjectChooser.getSelectedColor() : null;
    }

    public Color getSelectedBackgroundDarkProjectColor() {
        return backgroundProjectCheckBox.isSelected() ? backgroundDarkProjectChooser.getSelectedColor() : null;
    }

    public PersistentConfig generateGlobalConfig() {
        return PersistentConfig.builder()
                .enabled(enableJBCheckBox.isSelected())
                .acrossThemes(sameColorAllThemesCheckBox.isSelected())
                .backgroundEnabled(backgroundCheckBox.isSelected())
                .backgroundFromColor(backgroundChooser.getSelectedColor())
                .backgroundDarkFromColor(backgroundDarkChooser.getSelectedColor())
                .build();
    }

    public PersistentConfig generateProjectConfig() {
        return PersistentConfig.builder()
                .enabled(projectOverrideJBCheckBox.isSelected())
                .acrossThemes(sameColorAllThemesProjectCheckBox.isSelected())
                .backgroundEnabled(backgroundProjectCheckBox.isSelected())
                .backgroundFromColor(backgroundProjectChooser.getSelectedColor())
                .backgroundDarkFromColor(backgroundDarkProjectChooser.getSelectedColor())
                .build();
    }


    interface Listener extends EventListener {
        void onSettingsChanged(@NotNull ActionEvent e);

        void onHyperLinkClicked(@NotNull HyperlinkEvent e);
    }


}
