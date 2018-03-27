package com.tobszarny.intellij.plugin.activetabhighlighter;

import com.intellij.application.options.colors.ColorAndFontOptions;
import com.intellij.application.options.colors.ColorSettingsUtil;
import com.intellij.application.options.colors.RainbowColorsInSchemeState;
import com.intellij.application.options.colors.ScopeAttributesUtil;
import com.intellij.codeHighlighting.RainbowHighlighter;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorSchemeAttributeDescriptor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.impl.AbstractColorsScheme;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.editor.colors.impl.ReadOnlyColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.colors.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.FileStatusFactory;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.packageDependencies.DependencyValidationManagerImpl;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MyColorScheme extends EditorColorsSchemeImpl {

    private static final Logger LOG = Logger.getInstance(MyColorScheme.class);

    private EditorSchemeAttributeDescriptor[] myDescriptors;
    private String myName;
    private boolean myIsNew = false;
    private final RainbowColorsInSchemeState myRainbowState;
    private final static Predicate<ColorKey> FILE_STATUS_COLORS =
            input -> input != null && input.getExternalName().startsWith(FileStatusFactory.FILESTATUS_COLOR_KEY_PREFIX);


    private MyColorScheme(@NotNull EditorColorsScheme parentScheme) {
        super(parentScheme);

        if (parentScheme.isUseEditorFontPreferencesInConsole()) {
            setUseEditorFontPreferencesInConsole();
        } else {
            setConsoleFontPreferences(parentScheme.getConsoleFontPreferences());
        }

        if (parentScheme.isUseAppFontPreferencesInEditor()) {
            setUseAppFontPreferencesInEditor();
        } else {
            setFontPreferences(parentScheme.getFontPreferences());
        }

        setQuickDocFontSize(parentScheme.getQuickDocFontSize());
        myName = parentScheme.getName();

        RainbowHighlighter.transferRainbowState(this, parentScheme);
        myRainbowState = new RainbowColorsInSchemeState(this, parentScheme);

        initFonts();
    }

    @Nullable
    @Override
    public AbstractColorsScheme getOriginal() {
        return myParentScheme instanceof AbstractColorsScheme ? ((AbstractColorsScheme) myParentScheme).getOriginal() : null;
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public void setName(@NotNull String name) {
        myName = name;
    }

    public void setDescriptors(EditorSchemeAttributeDescriptor[] descriptors) {
        myDescriptors = descriptors;
    }

    public EditorSchemeAttributeDescriptor[] getDescriptors() {
        return myDescriptors;
    }

    @Override
    public boolean isReadOnly() {
        return myParentScheme instanceof ReadOnlyColorsScheme;
    }

    public boolean isModified() {
        if (isFontModified() || isConsoleFontModified()) return true;

        for (EditorSchemeAttributeDescriptor descriptor : myDescriptors) {
            if (descriptor.isModified()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canBeDeleted() {
        return (myParentScheme instanceof AbstractColorsScheme) && ((AbstractColorsScheme) myParentScheme).canBeDeleted();
    }

    private boolean isFontModified() {
        return !areDelegatingOrEqual(getFontPreferences(), myParentScheme.getFontPreferences());
    }

    private boolean isConsoleFontModified() {
        return !areDelegatingOrEqual(getConsoleFontPreferences(), myParentScheme.getConsoleFontPreferences());
    }

    private boolean apply() {
        if (!(myParentScheme instanceof ReadOnlyColorsScheme)) {
            return apply(myParentScheme);
        }
        return false;
    }

    private boolean apply(@NotNull EditorColorsScheme scheme) {
        boolean isModified = isFontModified() || isConsoleFontModified();

        if (isUseAppFontPreferencesInEditor()) {
            scheme.setUseAppFontPreferencesInEditor();
        } else {
            scheme.setFontPreferences(getFontPreferences());
        }

        if (isUseEditorFontPreferencesInConsole()) {
            scheme.setUseEditorFontPreferencesInConsole();
        } else {
            scheme.setConsoleFontPreferences(getConsoleFontPreferences());
        }

        for (EditorSchemeAttributeDescriptor descriptor : myDescriptors) {
            if (descriptor.isModified()) {
                isModified = true;
                descriptor.apply(scheme);
            }
        }

        if (isModified && scheme instanceof AbstractColorsScheme) {
            ((AbstractColorsScheme) scheme).setSaveNeeded(true);
        }
        return isModified;
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    public void setIsNew() {
        myIsNew = true;
    }

    public boolean isNew() {
        return myIsNew;
    }

    @NotNull
    @Override
    public String toString() {
        return "temporary scheme for " + myName;
    }

    public boolean isInherited(@NotNull TextAttributesKey key) {
        TextAttributesKey fallbackKey = key.getFallbackAttributeKey();
        if (fallbackKey != null) {
            if (myParentScheme instanceof AbstractColorsScheme) {
                TextAttributes ownAttrs = ((AbstractColorsScheme) myParentScheme).getDirectlyDefinedAttributes(key);
                if (ownAttrs != null) {
                    return ownAttrs == AbstractColorsScheme.INHERITED_ATTRS_MARKER;
                }
            }
            TextAttributes attributes = getAttributes(key);
            if (attributes != null) {
                TextAttributes fallbackAttributes = getAttributes(fallbackKey);
                return attributes == fallbackAttributes;
            }
        }
        return false;
    }

    public boolean isInherited(ColorKey key) {
        ColorKey fallbackKey = key.getFallbackColorKey();
        if (fallbackKey != null) {
            if (myParentScheme instanceof AbstractColorsScheme) {
                Color ownAttrs = ((AbstractColorsScheme) myParentScheme).getDirectlyDefinedColor(key);
                if (ownAttrs != null) {
                    return ownAttrs == AbstractColorsScheme.INHERITED_COLOR_MARKER;
                }
            }
            Color attributes = getColor(key);
            if (attributes != null) {
                Color fallback = getColor(fallbackKey);
                return attributes == fallback;
            }
        }
        return false;
    }

    public void resetToOriginal() {
        if (myParentScheme instanceof AbstractColorsScheme) {
            AbstractColorsScheme originalScheme = ((AbstractColorsScheme) myParentScheme).getOriginal();
            if (originalScheme != null) {
                copyPreservingFileStatusColors(originalScheme, (AbstractColorsScheme) myParentScheme);
                copyPreservingFileStatusColors(originalScheme, this);
                initScheme(this);
            }
        }
    }

    private static void copyPreservingFileStatusColors(@NotNull AbstractColorsScheme source,
                                                       @NotNull AbstractColorsScheme target) {
        Map<ColorKey, Color> fileStatusColors = target.getColorKeys().stream().filter(FILE_STATUS_COLORS).collect(
                Collectors.toMap(Function.identity(), target::getDirectlyDefinedColor));
        source.copyTo(target);
        for (ColorKey key : fileStatusColors.keySet()) {
            target.setColor(key, fileStatusColors.get(key));
        }
        target.setSaveNeeded(true);
    }

    private static void initScheme(@NotNull MyColorScheme scheme) {
        List<EditorSchemeAttributeDescriptor> descriptions = new ArrayList<>();
        initPluggedDescriptions(descriptions, scheme);
        initScopesDescriptors(descriptions, scheme);

        scheme.setDescriptors(descriptions.toArray(new EditorSchemeAttributeDescriptor[0]));
    }

    private static void initPluggedDescriptions(@NotNull List<EditorSchemeAttributeDescriptor> descriptions,
                                                @NotNull MyColorScheme scheme) {
        ColorSettingsPage[] pages = ColorSettingsPages.getInstance().getRegisteredPages();
        for (ColorSettingsPage page : pages) {
            initDescriptions(page, descriptions, scheme);
        }
        for (ColorAndFontDescriptorsProvider provider : Extensions.getExtensions(ColorAndFontDescriptorsProvider.EP_NAME)) {
            initDescriptions(provider, descriptions, scheme);
        }
    }

    private static void initScopesDescriptors(@NotNull List<EditorSchemeAttributeDescriptor> descriptions, @NotNull MyColorScheme scheme) {
        Set<Pair<NamedScope, NamedScopesHolder>> namedScopes = new THashSet<>(new TObjectHashingStrategy<Pair<NamedScope, NamedScopesHolder>>() {
            @Override
            public int computeHashCode(@NotNull final Pair<NamedScope, NamedScopesHolder> object) {
                return object.getFirst().getName().hashCode();
            }

            @Override
            public boolean equals(@NotNull final Pair<NamedScope, NamedScopesHolder> o1, @NotNull final Pair<NamedScope, NamedScopesHolder> o2) {
                return o1.getFirst().getName().equals(o2.getFirst().getName());
            }
        });
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            DependencyValidationManagerImpl validationManager = (DependencyValidationManagerImpl) DependencyValidationManager.getInstance(project);
            List<Pair<NamedScope, NamedScopesHolder>> cachedScopes = validationManager.getScopeBasedHighlightingCachedScopes();
            namedScopes.addAll(cachedScopes);
        }

        List<Pair<NamedScope, NamedScopesHolder>> list = new ArrayList<>(namedScopes);

        Collections.sort(list, (o1, o2) -> o1.getFirst().getName().compareToIgnoreCase(o2.getFirst().getName()));
        for (Pair<NamedScope, NamedScopesHolder> pair : list) {
            NamedScope namedScope = pair.getFirst();
            String name = namedScope.getName();
            TextAttributesKey textAttributesKey = ScopeAttributesUtil.getScopeTextAttributeKey(name);
            if (scheme.getAttributes(textAttributesKey) == null) {
                scheme.setAttributes(textAttributesKey, new TextAttributes());
            }
            NamedScopesHolder holder = pair.getSecond();

            PackageSet value = namedScope.getValue();
            String toolTip = holder.getDisplayName() + (value == null ? "" : ": " + value.getText());
            descriptions.add(new SchemeTextAttributesDescription(name, ColorAndFontOptions.SCOPES_GROUP, textAttributesKey, scheme, holder.getIcon(), toolTip));
        }
    }

    private static void initDescriptions(@NotNull ColorAndFontDescriptorsProvider provider,
                                         @NotNull List<EditorSchemeAttributeDescriptor> descriptions,
                                         @NotNull MyColorScheme scheme) {
        String className = provider.getClass().getName();
        String group = provider.getDisplayName();
        List<AttributesDescriptor> attributeDescriptors = ColorSettingsUtil.getAllAttributeDescriptors(provider);
        if (provider instanceof RainbowColorSettingsPage) {
            RainbowAttributeDescriptor d = new RainbowAttributeDescriptor(
                    ((RainbowColorSettingsPage) provider).getLanguage(), group,
                    ApplicationBundle.message("rainbow.option.panel.display.name"),
                    scheme, scheme.myRainbowState);
            descriptions.add(d);
        }
        for (AttributesDescriptor descriptor : attributeDescriptors) {
            if (descriptor == null) {
                LOG.warn("Null attribute descriptor in " + className);
                continue;
            }
            SchemeTextAttributesDescription d = new SchemeTextAttributesDescription(
                    descriptor.getDisplayName(), group, descriptor.getKey(), scheme, null, null);
            descriptions.add(d);
        }
        for (ColorDescriptor descriptor : provider.getColorDescriptors()) {
            if (descriptor == null) {
                LOG.warn("Null color descriptor in " + className);
                continue;
            }
            EditorSettingColorDescription d = new EditorSettingColorDescription(
                    descriptor.getDisplayName(), group, descriptor.getKey(), descriptor.getKind(), scheme);
            descriptions.add(d);
        }
    }
}
