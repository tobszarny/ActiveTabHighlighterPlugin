package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.application.options.colors.TextAttributesDescription;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;

public class HighlightedTabTextAttributesDescription extends TextAttributesDescription {

    public HighlightedTabTextAttributesDescription(final String name,
                                                   final String group,
                                                   final TextAttributes attributes,
                                                   final TextAttributesKey type,
                                                   final EditorColorsScheme editorColorsScheme) {
        super(name, group, attributes, type, editorColorsScheme, null, null);
    }

    @Override
    public boolean isErrorStripeEnabled() {
        return true;
    }


    @Override
    public TextAttributes getTextAttributes() {
        return super.getTextAttributes();
    }
}
