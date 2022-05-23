/*
 * Copyright (c) 2022 Tomasz Obszarny
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tobszarny.intellij.plugin.activetabhighlighter.config.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Optional;

@ToString
@Builder(builderClassName = "PersistentColorBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class PersistentColor {
    public String color;

    public Color toColor() {
        return StringUtils.isEmpty(color) ? null : Color.decode(color.replace("#", "0x"));
    }

    public static class PersistentColorBuilder {
        public PersistentColorBuilder fromColor(Color from) {
            color = toHexString(from);
            return this;
        }
    }

    protected static String toHexString(Color color) {
        return Optional.ofNullable(color)
                .map(c -> String.format("#%06X", color.getRGB() & 0xFFFFFF))
                .orElse("");
    }
}
