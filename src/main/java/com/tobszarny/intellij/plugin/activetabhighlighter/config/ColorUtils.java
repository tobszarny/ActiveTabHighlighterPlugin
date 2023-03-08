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

package com.tobszarny.intellij.plugin.activetabhighlighter.config;

import org.apache.commons.lang3.StringUtils;

import java.awt.Color;
import java.util.Optional;

public final class ColorUtils {
    private ColorUtils() {
    }

    public static Color decodeColor(String color) {
        return StringUtils.isEmpty(color) ? null :
                Color.decode(color.replace("#", "0x"));
    }

    public static String encodeColor(Color color) {
        return Optional.ofNullable(color)
                .map(c -> String.format("#%06X", color.getRGB() & 0xFFFFFF))
                .orElse("");
    }
}
