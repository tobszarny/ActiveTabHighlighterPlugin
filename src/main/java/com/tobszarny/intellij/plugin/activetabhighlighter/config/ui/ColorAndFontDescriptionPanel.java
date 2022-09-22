package com.tobszarny.intellij.plugin.activetabhighlighter.config.ui;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Tomasz Obszarny
 */
public class ColorAndFontDescriptionPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(ColorAndFontDescriptionPanel.class);

    private final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);


    //region Global Panel
    private JPanel mainPanel;
    private JBCheckBox backgroundCheckBox;
    private JCheckBox sameColorAllThemesCheckBox;
    private ColorPanel backgroundChooser;
    private ColorPanel backgroundDarkChooser;
    private JLabel darkLabel;
    private JLabel lightLabel;
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

    public ColorAndFontDescriptionPanel() {
        super(new BorderLayout());

//        JBEmptyBorder titleBorder = JBUI.Borders.empty(0, 0, 4, 0);
//        globalLabel.setBorder(titleBorder);


//        add(globalLabel, BorderLayout.CENTER);

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
        backgroundChooser.setVisible(this.isVisible() && backgroundCheckBox.isSelected());
        backgroundDarkChooser.setVisible(this.isVisible() && backgroundCheckBox.isSelected() && !sameColorAllThemesCheckBox.isSelected());
        lightLabel.setVisible(this.isVisible() && backgroundCheckBox.isSelected() && !sameColorAllThemesCheckBox.isSelected());
        darkLabel.setVisible(this.isVisible() && backgroundCheckBox.isSelected() && !sameColorAllThemesCheckBox.isSelected());
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        backgroundChooser.setVisible(aFlag);
        backgroundDarkChooser.setVisible(aFlag);
        lightLabel.setVisible(aFlag);
        darkLabel.setVisible(aFlag);
        sameColorAllThemesCheckBox.setVisible(aFlag);
        backgroundCheckBox.setVisible(aFlag);
    }

    interface Listener extends EventListener {
        void onSettingsChanged(@NotNull ActionEvent e);

        void onHyperLinkClicked(@NotNull HyperlinkEvent e);
    }


}
