package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsGUI {

    private static final Logger LOGGER = Logger.getInstance(SettingsGUI.class);
    public Integer red = 173;
    public Integer green = 46;
    public Integer blue = 156;
    private JPanel rootPanel;
    private JSpinner redSpinner;
    private JSpinner greenSpinner;
    private JSpinner blueSpinner;
    private JButton button1;
    private JPanel colorPane;
    private JPanel rootPanel2;
    private HighlighterSettingsConfig config;

    //    private Color editedColor;
    private SpinnerModel redModel;
    private SpinnerModel greenModel;
    private SpinnerModel blueModel;

    SettingsGUI() {

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SettingsGUI");
        frame.setContentPane(new SettingsGUI().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

    }

    public void createUI(Project project) {
        this.config = HighlighterSettingsConfig.getInstance();
        redModel = new SpinnerNumberModel(config.state.red.intValue(), 0, 255, 1);
        greenModel = new SpinnerNumberModel(config.state.green.intValue(), 0, 255, 1);
        blueModel = new SpinnerNumberModel(config.state.blue.intValue(), 0, 255, 1);
        redSpinner.setModel(redModel);
        greenSpinner.setModel(greenModel);
        blueSpinner.setModel(blueModel);

        rootPanel.setBorder(BorderFactory.createTitledBorder("Highlight color"));

        updateColorPane(config.state.red, config.state.green, config.state.blue);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color selectedColor = JColorChooser.showDialog(rootPanel, "Choose color", config.getBackgroundColor());
                if (selectedColor != null) {
                    red = selectedColor.getRed();
                    green = selectedColor.getGreen();
                    blue = selectedColor.getBlue();
                    updateSpinners(red, green, blue);
                    updateColorPane(red, green, blue);
                }
            }
        });
        redSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                red = (Integer) redSpinner.getValue();
                updateColorPane(red, green, blue);
            }
        });
        greenModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                green = (Integer) greenSpinner.getValue();
                updateColorPane(red, green, blue);
            }
        });
        blueSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                blue = (Integer) blueSpinner.getValue();
                updateColorPane(red, green, blue);
            }
        });
    }

    public void updateSpinners(Integer red, Integer green, Integer blue) {
        redModel.setValue(red);
        greenModel.setValue(green);
        blueModel.setValue(blue);
    }

    public void updateConfiguration(Integer red, Integer green, Integer blue) {
        config.state.red = red;
        config.state.green = green;
        config.state.blue = blue;
    }

    public void updateColorPane(Integer red, Integer green, Integer blue) {
        colorPane.setBackground(new Color(red, green, blue));
    }

    public JPanel getRootPanel() {
        return rootPanel2;
    }

    public void apply() {
        updateConfiguration(this.red, this.green, this.blue);
    }

    public boolean isModified() {
        boolean modified = false;
        modified |= config.state.red != this.red;
        modified |= config.state.green != this.green;
        modified |= config.state.blue != this.blue;
        LOGGER.debug("IsModified: {}", modified);
        return modified;
    }

    public void reset() {
        this.red = config.state.red;
        this.green = config.state.green;
        this.blue = config.state.blue;
        updateSpinners(red, green, blue);
        updateColorPane(red, green, blue);
    }
}
