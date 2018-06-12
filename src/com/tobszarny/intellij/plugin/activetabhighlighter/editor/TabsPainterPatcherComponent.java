package com.tobszarny.intellij.plugin.activetabhighlighter.editor;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.impl.DefaultEditorTabsPainter;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.ui.tabs.impl.JBEditorTabsPainter;
import com.intellij.ui.tabs.impl.ShapeTransform;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.annotations.Transient;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@SuppressWarnings("UseJBColor")
public final class TabsPainterPatcherComponent implements ApplicationComponent {
	private static final Logger LOG = Logger.getInstance(TabsPainterPatcherComponent.class);

	private static Config config = new Config();

	private WeakHashMap<TabsPainter, String> map = new WeakHashMap<>();

	public TabsPainterPatcherComponent() {
	}

	public static TabsPainterPatcherComponent getInstance() {
		return ServiceManager.getService(TabsPainterPatcherComponent.class);
	}

	public static void onColorsChanged(@NotNull Config config) {
		TabsPainterPatcherComponent.config = config;
		TabsPainterPatcherComponent instance = getInstance();
		for (TabsPainter editorGroupsTabsPainter : instance.map.keySet()) {
			setColors(editorGroupsTabsPainter);
		}
	}

	@SuppressWarnings("UseJBColor")
	private static void setColors(TabsPainter editorGroupsTabsPainter) {
		if (editorGroupsTabsPainter != null) {
			if (editorGroupsTabsPainter instanceof DarculaTabsPainter) {
				editorGroupsTabsPainter.setMask(new Color(config.getDarcula_mask()), config.getDarcula_opacity());
			} else {
				editorGroupsTabsPainter.setMask(new Color(config.getClassic_mask()), config.getClassic_opacity());
			}
			JBEditorTabs painterTabs = editorGroupsTabsPainter.getTabs();
			if (!painterTabs.isDisposed()) {
				painterTabs.repaint();
			}
		}
	}

	@Override
	public void initComponent() {
		final MessageBus bus = ApplicationManagerEx.getApplicationEx().getMessageBus();

		final MessageBusConnection connect = bus.connect();
		connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
			@Override
			public void selectionChanged(@NotNull final FileEditorManagerEvent event) {
				if (!config.isEnabled()) {
					return;
				}

				final FileEditor editor = event.getNewEditor();
				if (editor != null) {
					Component component = editor.getComponent();
					while (component != null) {
						if (component instanceof JBEditorTabs) {
							patchPainter((JBEditorTabs) component);
							return;
						}
						component = component.getParent();
					}
				}
			}
		});

	}

	private void patchPainter(final JBEditorTabs component) {
		if (alreadyPatched(component)) {
			return;
		}

		final TabsPainter tabsPainter = new TabsPainter(component);
		init(tabsPainter);

		final TabsPainter darculaTabsPainter = new DarculaTabsPainter(component);
		init(darculaTabsPainter);

		LOG.info("HACK: Overriding JBEditorTabsPainters");
		ReflectionUtil.setField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDefaultPainter", proxy(tabsPainter));
		ReflectionUtil.setField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDarkPainter", proxy(darculaTabsPainter));
	}

	private JBEditorTabsPainter proxy(TabsPainter tabsPainter) {
		Field fillPathField = null;
		try {
			final Class<?> clazz = Class.forName("com.intellij.ui.tabs.impl.JBTabsImpl$ShapeInfo");
			fillPathField = clazz.getField("fillPath");
		} catch (Exception e) {
			LOG.error(e);
		}

		Field finalFillPathField = fillPathField;
		return (TabsPainter) Enhancer.create(TabsPainter.class, new MethodInterceptor() {
			@Override
			public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
				final Object result = method.invoke(tabsPainter, objects);

				if ("paintSelectionAndBorder".equals(method.getName())) {
					TabsPainterPatcherComponent.this.paintSelectionAndBorder(objects, tabsPainter, finalFillPathField);
				}

				return result;
			}
		});
	}

	/** kinda like the original */
	private void paintSelectionAndBorder(Object[] objects, TabsPainter tabsPainter, Field fillPathField) throws IllegalAccessException {
		// Retrieve arguments
		final Graphics2D g2d = (Graphics2D) objects[0];
		final Rectangle rect = (Rectangle) objects[1];
		final Object selectedShape = objects[2];
		// final Insets insets = (Insets) objects[3];
		// final Color tabColor = (Color) objects[4];

		JBEditorTabs myTabs = tabsPainter.getTabs();
		final JBTabsPosition position = myTabs.getTabsPosition();

		if (myTabs.hasUnderlineSelection() /* && myTabs.getTabCount() > 1 */) {

			if (fillPathField != null && !JBEditorTabsPainter.hasFocus(myTabs)) {
				fillSelectionAndBorder(fillPathField, g2d, selectedShape, tabsPainter);
			}

			Color underline = new Color(config.getUnderline_color());
			Color underlineColor_inactive = new Color(config.getUnderline_color_inactive());
			int underlineOpacity_inactive = config.getUnderline_opacity_inactive();
			Color inactiveUnderline = ColorUtil.withAlpha(underlineColor_inactive, underlineOpacity_inactive / 100.0);

			g2d.setColor(JBEditorTabsPainter.hasFocus(myTabs) ? underline : inactiveUnderline);
			int thickness = 3;
			if (position == JBTabsPosition.bottom) {
				g2d.fillRect(rect.x, rect.y - 1, rect.width, thickness);
			} else if (position == JBTabsPosition.top) {
				g2d.fillRect(rect.x, rect.y + rect.height - thickness + 1, rect.width, thickness);
				g2d.setColor(UIUtil.CONTRAST_BORDER_COLOR);
				g2d.drawLine(Math.max(0, rect.x - 1), rect.y, rect.x + rect.width, rect.y);
			} else if (position == JBTabsPosition.left) {
				g2d.fillRect(rect.x + rect.width - thickness + 1, rect.y, thickness, rect.height);
			} else if (position == JBTabsPosition.right) {
				g2d.fillRect(rect.x, rect.y, thickness, rect.height);
			}
		}
	}

	private void fillSelectionAndBorder(Field fillPathField, Graphics2D g2d, Object selectedShape, TabsPainter tabsPainter) throws IllegalAccessException {
		final ShapeTransform fillPath = (ShapeTransform) fillPathField.get(selectedShape);

		g2d.setColor(tabsPainter.getInactiveMaskColor());
		g2d.fill(fillPath.getShape());
	}

	private boolean alreadyPatched(JBEditorTabs component) {
		if (UIUtil.isUnderDarcula()) {
			JBEditorTabsPainter painter = ReflectionUtil.getField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDarkPainter");
			if (painter instanceof TabsPainter) {
				return true;
			}
			if (!painter.getClass().getPackage().getName().startsWith("com.intellij")) { // some other plugin
				LOG.warn("JBEditorTabsPainter already patched by " + painter.getClass().getCanonicalName());
			}
		} else {
			JBEditorTabsPainter painter = ReflectionUtil.getField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDefaultPainter");
			if (painter instanceof TabsPainter) {
				return true;
			}
			if (!painter.getClass().getPackage().getName().startsWith("com.intellij")) { // some other plugin
				LOG.warn("JBEditorTabsPainter already patched by " + painter.getClass().getCanonicalName());
			}
		}
		return false;
	}

	public void init(TabsPainter tabsPainter) {
		setColors(tabsPainter);
		map.put(tabsPainter, null);
	}

	public static class TabsPainter extends DefaultEditorTabsPainter {

		private JBEditorTabs tabs;

		protected Color inactiveMaskColor;

		/** for proxy */
		public TabsPainter() {
			super(null);
		}

		/**
		 * @see DefaultEditorTabsPainter#getInactiveMaskColor()
		 */
		public TabsPainter(final JBEditorTabs tabs) {
			super(tabs);
			this.tabs = tabs;
			inactiveMaskColor = ColorUtil.withAlpha(Config.DEFAULT_MASK, (Config.DEFAULT_OPACITY / 100.0));
		}

		public JBEditorTabs getTabs() {
			return tabs;
		}

		@Override
		public Color getDefaultTabColor() {
			return super.getDefaultTabColor();
		}

		protected Color getInactiveMaskColor() {
			return inactiveMaskColor;
		}

		public void setMask(Color mask, int opacity) {
			this.inactiveMaskColor = ColorUtil.withAlpha(mask, (opacity / 100.0));
		}

	}

	/**
	 * @see com.intellij.ui.tabs.impl.DarculaEditorTabsPainter
	 */
	public static class DarculaTabsPainter extends TabsPainter {

		public DarculaTabsPainter(JBEditorTabs component) {
			super(component);
			inactiveMaskColor = ColorUtil.withAlpha(Config.DEFAULT_DARCULA_MASK, (Config.DEFAULT_DARCULA_OPACITY / 100.0));
		}

		/** same as @see com.intellij.ui.tabs.impl.DarculaEditorTabsPainter */
		@Override
		public Color getDefaultTabColor() {
			if (myDefaultTabColor != null) {
				return myDefaultTabColor;
			}
			return new Color(0x515658);
		}

	}

	static class Config {
		public static final Color DEFAULT_MASK = new Color(0x262626);
		public static final int DEFAULT_OPACITY = 20;

		public static final Color DEFAULT_DARCULA_MASK = new Color(0x262626);
		public static final int DEFAULT_DARCULA_OPACITY = 50; // too low

		/** disabling requires restart */
		private boolean enabled = true;

		private Integer classic_mask = DEFAULT_MASK.getRGB();
		private int classic_opacity = DEFAULT_OPACITY;

		private Integer darcula_mask = DEFAULT_DARCULA_MASK.getRGB();
		private int darcula_opacity = DEFAULT_DARCULA_OPACITY + 20;

		private Integer underlineColor = new Color(0x439EB8).getRGB();
		private Integer underlineColor_inactive = Color.BLACK.getRGB();
		private int underlineOpacity_inactive = 100;

		public Integer getClassic_mask() {
			return classic_mask;
		}

		public void setClassic_mask(Integer classic_mask) {
			this.classic_mask = classic_mask;
		}

		public int getClassic_opacity() {
			return classic_opacity;
		}

		public void setClassic_opacity(int classic_opacity) {
			this.classic_opacity = classic_opacity;
		}

		public Integer getDarcula_mask() {
			return darcula_mask;
		}

		public void setDarcula_mask(Integer darcula_mask) {
			this.darcula_mask = darcula_mask;
		}

		public int getDarcula_opacity() {
			return darcula_opacity;
		}

		public void setDarcula_opacity(int darcula_opacity) {
			this.darcula_opacity = darcula_opacity;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Integer getUnderline_color() {
			return underlineColor;
		}

		public void setUnderline_color(Integer underlineColor) {
			this.underlineColor = underlineColor;
		}

		@Transient
		public void setOpacity(String text) {
			try {
				this.classic_opacity = parse(text);
			} catch (Exception e) {
				classic_opacity = DEFAULT_OPACITY;
			}
		}

		@Transient
		public void setDarcula_opacity(String text) {
			try {
				this.darcula_opacity = parse(text);
			} catch (Exception e) {
				darcula_opacity = DEFAULT_DARCULA_OPACITY;
			}
		}

		public Integer getUnderline_color_inactive() {
			return underlineColor_inactive;
		}

		public void setUnderline_color_inactive(Integer underlineColor_inactive) {
			this.underlineColor_inactive = underlineColor_inactive;
		}

		public int getUnderline_opacity_inactive() {
			return underlineOpacity_inactive;
		}

		public void setUnderline_opacity_inactive(int underlineOpacity_inactive) {
			this.underlineOpacity_inactive = underlineOpacity_inactive;
		}

		private int parse(String text) {
			int opacity = Integer.parseInt(text);
			if (opacity > 100) {
				opacity = 100;
			} else if (opacity < 0) {
				opacity = 0;
			}
			return opacity;
		}
	}
}
