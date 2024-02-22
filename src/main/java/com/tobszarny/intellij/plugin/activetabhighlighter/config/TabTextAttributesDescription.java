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

import com.intellij.application.options.colors.TextAttributesDescription;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;

public class TabTextAttributesDescription extends TextAttributesDescription {

    public TabTextAttributesDescription(final String name,
                                        final String group,
                                        final TextAttributes attributes,
                                        final TextAttributesKey type,
                                        final EditorColorsScheme editorColorsScheme) {
        super(name, group, attributes, type, editorColorsScheme, null, null);
    }

    @Override
    public boolean isErrorStripeEnabled() {
        return TOGGLE;
    }


    @Override
    public TextAttributes getTextAttributes() {
        return super.getTextAttributes();
    }
}
