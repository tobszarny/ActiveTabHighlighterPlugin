package com.tobszarny.intellij.plugin.activetabhighlighter.config.ui;

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
import com.tobszarny.intellij.plugin.activetabhighlighter.config.model.PersistentColor;
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
public class TabColorAndFontDescriptionPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(TabColorAndFontDescriptionPanel.class);

    private final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);

    private JPanel mainPanel;


    //region Global Panel
    private JPanel globalPanel;
    private JBCheckBox enableJBCheckBox;
    private JLabel globalLabel;
    private ColorAndFontDescriptionPanel globalColorAndFontDescriptionPanel;

    //endregion

    //region Project Panel
    private JPanel projectPanel;
    private JLabel projectPrivateLabel;
    private JBCheckBox projectOverrideJBCheckBox;
    private ColorAndFontDescriptionPanel projectColorAndFontDescriptionPanel;
    //endregion

    private Map<String, EffectType> myEffectsMap;
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

    public TabColorAndFontDescriptionPanel() {
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

        initPanelComponentsBehavior();
        //noinspection unchecked
    }

    private void initPanelComponentsBehavior() {
        enableJBCheckBox.addActionListener(e -> {
            globalColorAndFontDescriptionPanel.setVisible(enableJBCheckBox.isSelected());
            projectColorAndFontDescriptionPanel.setVisible(enableJBCheckBox.isSelected());
            globalLabel.setVisible(enableJBCheckBox.isSelected());
            projectPrivateLabel.setVisible(enableJBCheckBox.isSelected());
            projectOverrideJBCheckBox.setVisible(enableJBCheckBox.isSelected());

            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });


        projectOverrideJBCheckBox.addActionListener(e -> {
            projectColorAndFontDescriptionPanel.setVisible(projectOverrideJBCheckBox.isSelected());
            projectPrivateLabel.setVisible(projectOverrideJBCheckBox.isSelected());

            if (myUiEventsEnabled) {
                myDispatcher.getMulticaster().onSettingsChanged(e);
            }
        });

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
                .enabled(enableJBCheckBox.isSelected())
//                .acrossThemes(sameColorAllThemesCheckBox.isSelected())
//                .backgroundEnabled(backgroundCheckBox.isSelected())
//                .background(PersistentColor.builder()
//                        .fromColor(backgroundChooser.getSelectedColor())
//                        .build())
//                .backgroundDark(PersistentColor.builder()
//                        .fromColor(backgroundDarkChooser.getSelectedColor())
//                        .build())
                .build();
    }

    public PersistentConfig generateProjectConfig() {
        return PersistentConfig.builder()
                .enabled(projectOverrideJBCheckBox.isSelected())
//                .acrossThemes(sameColorAllThemesProjectCheckBox.isSelected())
//                .backgroundEnabled(backgroundProjectCheckBox.isSelected())
//                .background(PersistentColor.builder()
//                        .fromColor(backgroundProjectChooser.getSelectedColor())
//                        .build())
//                .backgroundDark(PersistentColor.builder()
//                        .fromColor(backgroundDarkProjectChooser.getSelectedColor())
//                        .build())
                .build();
    }


    interface Listener extends EventListener {
        void onSettingsChanged(@NotNull ActionEvent e);

        void onHyperLinkClicked(@NotNull HyperlinkEvent e);
    }


}
