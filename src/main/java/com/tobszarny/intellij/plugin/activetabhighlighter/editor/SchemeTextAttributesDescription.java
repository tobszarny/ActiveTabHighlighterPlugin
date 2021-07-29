package com.tobszarny.intellij.plugin.activetabhighlighter.editor;

import com.intellij.application.options.colors.TextAttributesDescription;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.impl.AbstractColorsScheme;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.colors.AbstractKeyDescriptor;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorAndFontDescriptorsProvider;
import com.intellij.openapi.options.colors.ColorSettingsPages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SchemeTextAttributesDescription extends TextAttributesDescription {
    @NotNull
    private final TextAttributes myInitialAttributes;
    @NotNull
    private final TextAttributesKey key;

    private TextAttributes myFallbackAttributes;
    private Pair<ColorAndFontDescriptorsProvider, AttributesDescriptor> myBaseAttributeDescriptor;
    private final boolean myIsInheritedInitial;

    SchemeTextAttributesDescription(String name,
                                    String group,
                                    @NotNull TextAttributesKey key,
                                    @NotNull MyColorScheme scheme,
                                    Icon icon,
                                    String toolTip) {
        super(name, group,
                getInitialAttributes(scheme, key).clone(),
                key, scheme, icon, toolTip);
        this.key = key;
        myInitialAttributes = getInitialAttributes(scheme, key);
        TextAttributesKey fallbackKey = key.getFallbackAttributeKey();
        if (fallbackKey != null) {
            myFallbackAttributes = scheme.getAttributes(fallbackKey);
            myBaseAttributeDescriptor = ColorSettingsPages.getInstance().getAttributeDescriptor(fallbackKey);
            if (myBaseAttributeDescriptor == null) {
                myBaseAttributeDescriptor =
                        new Pair<>(null, new AttributesDescriptor(fallbackKey.getExternalName(), fallbackKey));
            }
        }
        myIsInheritedInitial = scheme.isInherited(key);
        setInherited(myIsInheritedInitial);
        if (myIsInheritedInitial && myFallbackAttributes != null) {
            getTextAttributes().copyFrom(myFallbackAttributes);
        }
        initCheckedStatus();
    }

    @NotNull
    private static TextAttributes getInitialAttributes(@NotNull EditorColorsScheme scheme, @NotNull TextAttributesKey key) {
        TextAttributes attributes = scheme.getAttributes(key);
        return attributes != null ? attributes : new TextAttributes();
    }

    @Override
    public void apply(EditorColorsScheme scheme) {
        if (scheme == null) scheme = getScheme();
        boolean skip = scheme instanceof EditorColorsSchemeImpl && isInherited() && myIsInheritedInitial;
        if (!skip) {
            // IDEA-162844 set only if previously was not inherited (and, so, we must mark it as inherited)
            scheme.setAttributes(key, isInherited() ? AbstractColorsScheme.INHERITED_ATTRS_MARKER : getTextAttributes());
        }
    }

    @Override
    public boolean isModified() {
        if (isInherited()) {
            return !myIsInheritedInitial;
        }
        return !Comparing.equal(myInitialAttributes, getTextAttributes()) || myIsInheritedInitial;
    }

    @Override
    public boolean isErrorStripeEnabled() {
        return true;
    }

    @Nullable
    @Override
    public TextAttributes getBaseAttributes() {
        return myFallbackAttributes;
    }

    @Nullable
    @Override
    public Pair<ColorAndFontDescriptorsProvider, ? extends AbstractKeyDescriptor> getFallbackKeyDescriptor() {
        return myBaseAttributeDescriptor;
    }
}