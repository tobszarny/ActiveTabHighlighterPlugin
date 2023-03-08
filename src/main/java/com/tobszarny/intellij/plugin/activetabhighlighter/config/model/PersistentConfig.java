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

package com.tobszarny.intellij.plugin.activetabhighlighter.config.model;

import com.intellij.util.ui.UIUtil;
import com.tobszarny.intellij.plugin.activetabhighlighter.config.ColorUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.awt.*;
import java.util.Optional;

@Builder(builderClassName = "PersistentConfigBuilder")
@AllArgsConstructor
public
class PersistentConfig {

    public boolean enabled;
    public boolean acrossThemes;

    public boolean backgroundEnabled;
    public String background;
    public String backgroundDark;

    public boolean foregroundEnabled;
    public String foreground;

    public PersistentConfig() {
//        background = new PersistentColor();
//        backgroundDark = new PersistentColor();
//        foreground = new PersistentColor();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAcrossThemes() {
        return acrossThemes;
    }

    public Color getBackgroundDarkColor() {
        return ColorUtils.decodeColor(backgroundDark);
    }

    public Color getBackgroundColor() {
        return ColorUtils.decodeColor(background);
    }

    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    public Color getInferredBackgroundColor() {
        if (acrossThemes)
            return ColorUtils.decodeColor(background);

        return UIUtil.isUnderDarcula() ? Optional.ofNullable(backgroundDark).map(ColorUtils::decodeColor).orElse(null) :
                Optional.ofNullable(background).map(ColorUtils::decodeColor).orElse(null);
    }

    public Color getForegroundColor() {
        return ColorUtils.decodeColor(foreground);
    }

/*    public void storeBackgroundColorInformation(boolean enabled, Color color, Color colorDark) {
        backgroundEnabled = enabled;
        if (enabled) {
            if (color == null) {
                throw new NullPointerException("Color cannot be null when enabled");
            } else {
                background = PersistentColor.builder().fromColor(color).build();
            }

            if (colorDark == null) {
                throw new NullPointerException("Color cannot be null when enabled");
            } else {
                backgroundDark = PersistentColor.builder().fromColor(colorDark).build();
            }
        }
    }

    public boolean isBackgroundColorDifferentThan(Color color) {
        return !background.toColor().equals(color);
    }*/

    @Override
    public String toString() {
        return "PersistentState{" +
                "background=" + background +
                ", foreground=" + foreground +
                '}';
    }

    public void storeConfig(PersistentConfig config) {
        this.enabled = config.enabled;
        this.acrossThemes = config.acrossThemes;
        this.backgroundEnabled = config.backgroundEnabled;
        this.background = config.background;
        this.foreground = config.foreground;

    }

    public static class PersistentConfigBuilder {
        public PersistentConfigBuilder backgroundFromColor(Color from) {
            background = ColorUtils.encodeColor(from);
            return this;
        }

        public PersistentConfigBuilder backgroundDarkFromColor(Color from) {
            backgroundDark = ColorUtils.encodeColor(from);
            return this;
        }
    }

}
