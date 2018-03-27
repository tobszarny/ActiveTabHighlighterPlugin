package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.application.options.colors.ColorAndFontDescription;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.impl.AbstractColorsScheme;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.colors.AbstractKeyDescriptor;
import com.intellij.openapi.options.colors.ColorAndFontDescriptorsProvider;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class EditorSettingColorDescription extends ColorAndFontDescription {
    private final ColorKey myColorKey;
    private final ColorDescriptor.Kind myKind;
    private final Color myInitialColor;
    private final boolean myIsInheritedInitial;
    private Color myColor;
    private TextAttributes myFallbackAttributes;
    private Pair<ColorAndFontDescriptorsProvider, ColorDescriptor> myBaseAttributeDescriptor;

    EditorSettingColorDescription(String name,
                                  String group,
                                  @NotNull ColorKey colorKey,
                                  @NotNull ColorDescriptor.Kind kind,
                                  @NotNull MyColorScheme scheme) {
        super(name, group, colorKey.getExternalName(), scheme, null, null);
        myColorKey = colorKey;
        myKind = kind;
        ColorKey fallbackKey = myColorKey.getFallbackColorKey();
        Color fallbackColor = null;
        if (fallbackKey != null) {
            fallbackColor = scheme.getColor(fallbackKey);
            myBaseAttributeDescriptor = ColorSettingsPages.getInstance().getColorDescriptor(fallbackKey);
            if (myBaseAttributeDescriptor == null) {
                myBaseAttributeDescriptor = Pair.create(null, new ColorDescriptor(fallbackKey.getExternalName(), fallbackKey, myKind));
            }
            myFallbackAttributes = new TextAttributes(myKind == ColorDescriptor.Kind.FOREGROUND ? fallbackColor : null,
                    myKind == ColorDescriptor.Kind.BACKGROUND ? fallbackColor : null,
                    null, null, Font.PLAIN);
        }
        myColor = scheme.getColor(myColorKey);
        myInitialColor = ObjectUtils.chooseNotNull(fallbackColor, myColor);

        myIsInheritedInitial = scheme.isInherited(myColorKey);
        setInherited(myIsInheritedInitial);
        if (myIsInheritedInitial) {
            //setInheritedAttributes(getTextAttributes());
        }
        initCheckedStatus();
    }

    @Override
    public int getFontType() {
        return Font.PLAIN;
    }

    @Override
    public void setFontType(int type) {
    }

    @Override
    public Color getExternalEffectColor() {
        return null;
    }

    @Override
    public void setExternalEffectColor(Color color) {
    }

    @NotNull
    @Override
    public EffectType getExternalEffectType() {
        return EffectType.LINE_UNDERSCORE;
    }

    @Override
    public void setExternalEffectType(EffectType type) {
    }

    @Override
    public Color getExternalForeground() {
        return myKind == ColorDescriptor.Kind.FOREGROUND ? myColor : null;
    }

    @Override
    public void setExternalForeground(Color col) {
        if (myKind != ColorDescriptor.Kind.FOREGROUND) return;
        if (myColor != null && myColor.equals(col)) return;
        myColor = col;
    }

    @Override
    public Color getExternalBackground() {
        return myKind == ColorDescriptor.Kind.BACKGROUND ? myColor : null;
    }

    @Override
    public void setExternalBackground(Color col) {
        if (myKind != ColorDescriptor.Kind.BACKGROUND) return;
        if (myColor != null && myColor.equals(col)) return;
        myColor = col;
    }

    @Override
    public Color getExternalErrorStripe() {
        return null;
    }

    @Override
    public void setExternalErrorStripe(Color col) {
    }

    @Override
    public boolean isFontEnabled() {
        return false;
    }

    @Override
    public boolean isForegroundEnabled() {
        return myKind == ColorDescriptor.Kind.FOREGROUND;
    }

    @Override
    public boolean isBackgroundEnabled() {
        return myKind == ColorDescriptor.Kind.BACKGROUND;
    }

    @Override
    public boolean isEffectsColorEnabled() {
        return false;
    }

    @Override
    public boolean isModified() {
        if (isInherited()) {
            return !myIsInheritedInitial;
        }
        return !Comparing.equal(myInitialColor, myColor) || myIsInheritedInitial;
    }

    @Nullable
    @Override
    public TextAttributes getBaseAttributes() {
        return myFallbackAttributes;
    }

    @Override
    public void apply(EditorColorsScheme scheme) {
        if (scheme == null) scheme = getScheme();
        boolean skip = scheme instanceof EditorColorsSchemeImpl && isInherited() && myIsInheritedInitial;
        if (!skip) {
            scheme.setColor(myColorKey, isInherited() ? AbstractColorsScheme.INHERITED_COLOR_MARKER : myColor);
        }
    }

    @Nullable
    @Override
    public Pair<ColorAndFontDescriptorsProvider, ? extends AbstractKeyDescriptor> getFallbackKeyDescriptor() {
        return myBaseAttributeDescriptor;
    }
}
