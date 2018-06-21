package com.tobszarny.intellij.plugin.activetabhighlighter;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.Gray;
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
				editorGroupsTabsPainter.setActiveColor(new Color(config.getDarcula_activeColor()));
			} else {
				editorGroupsTabsPainter.setMask(new Color(config.getClassic_mask()), config.getClassic_opacity());
				editorGroupsTabsPainter.setActiveColor(new Color(config.getClassic_activeColor()));
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

		/* workaround for https://youtrack.jetbrains.com/issue/IDEA-194380 */
		if (!hasUnderlineSelection()) {
			IdeEventQueue.getInstance().addDispatcher(createFocusDispatcher(component), component);
		}

		ReflectionUtil.setField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDefaultPainter", proxy(tabsPainter));
		ReflectionUtil.setField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDarkPainter", proxy(darculaTabsPainter));
	}

	private IdeEventQueue.EventDispatcher createFocusDispatcher(JBEditorTabs component) {
		return e -> {
			if (e instanceof FocusEvent) {
				Component from = ((FocusEvent) e).getOppositeComponent();
				Component to = ((FocusEvent) e).getComponent();
				if (isChild(from, component) || isChild(to, component)) {
					component.repaint();
				}
			}
			return false;
		};
	}

	private boolean isChild(@Nullable Component c, JBEditorTabs component) {
		if (c == null)
			return false;
		if (c == component)
			return true;
		return isChild(c.getParent(), component);
	}

	public boolean hasUnderlineSelection() {
		return UIUtil.isUnderDarcula() && Registry.is("ide.new.editor.tabs.selection");
	}

	private JBEditorTabsPainter proxy(TabsPainter tabsPainter) {
		Field fillPathField = null;
		Field pathField = null;
		Field labelPathField = null;
		try {
			final Class<?> clazz = Class.forName("com.intellij.ui.tabs.impl.JBTabsImpl$ShapeInfo");
			fillPathField = clazz.getField("fillPath");
			pathField = clazz.getField("path");
			labelPathField = clazz.getField("labelPath");

		} catch (Exception e) {
			LOG.error(e);
		}

		Field finalFillPathField = fillPathField;
		Field finalPathField = pathField;
		Field finalLabelPathField = labelPathField;
		return (TabsPainter) Enhancer.create(TabsPainter.class, new MethodInterceptor() {
			boolean broken = false;

			@Override
			public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
				final Object result = method.invoke(tabsPainter, objects);

				try {
					if (!broken) {
						if ("paintSelectionAndBorder".equals(method.getName())) {
							TabsPainterPatcherComponent.this.paintSelectionAndBorder(objects, tabsPainter, finalFillPathField, finalPathField,
									finalLabelPathField);
						}
					}
				} catch (Exception e) {
					LOG.error(e);
					broken = true;
				}

				return result;
			}
		});
	}

	/** kinda like the original */
	private void paintSelectionAndBorder(Object[] objects, TabsPainter tabsPainter, Field fillPathField, Field pathField, Field labelPathField)
			throws IllegalAccessException {

		// Retrieve arguments
		final Graphics2D g2d = (Graphics2D) objects[0];
		final Rectangle rect = (Rectangle) objects[1];
		final Object selectedShape = objects[2];
		final Insets insets = (Insets) objects[3];
		final Color tabColor = (Color) objects[4];

		JBEditorTabs myTabs = tabsPainter.getTabs();
		final JBTabsPosition position = myTabs.getTabsPosition();
		final boolean horizontalTabs = myTabs.isHorizontalTabs();
		int _x = rect.x;
		int _y = rect.y;
		int _height = rect.height;
		boolean hasFocus = JBEditorTabsPainter.hasFocus(myTabs);
		ShapeTransform shapeTransform = null;
		if (fillPathField != null) {
			shapeTransform = (ShapeTransform) fillPathField.get(selectedShape);
		}

		if (myTabs.hasUnderlineSelection() /* && myTabs.getTabCount() > 1 */) { // darcula
			if (shapeTransform != null && hasFocus) {
				fillSelectionAndBorder(g2d, tabsPainter, hasFocus, shapeTransform);
			}

			Color underline = new Color(config.getUnderlineColor());
			Color underlineColor_inactive = new Color(config.getUnderlineColor_inactive());
			int underlineOpacity_inactive = config.getUnderlineOpacity_inactive();
			Color inactiveUnderline = ColorUtil.withAlpha(underlineColor_inactive, underlineOpacity_inactive / 100.0);

			g2d.setColor(hasFocus ? underline : inactiveUnderline);
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
		} else { // classic skin
			if (pathField == null || labelPathField == null) {
				return;
			}
			final ShapeTransform path = (ShapeTransform) pathField.get(selectedShape);
			final ShapeTransform labelPath = (ShapeTransform) labelPathField.get(selectedShape);
			Insets i = path.transformInsets(insets);

			if (shapeTransform != null && hasFocus) {
				fillSelectionAndBorder(g2d, tabsPainter, hasFocus, shapeTransform);
			}

			g2d.setColor(Gray._0.withAlpha(15));
			g2d.draw(labelPath.transformLine(i.left, labelPath.getMaxY(), path.getMaxX(), labelPath.getMaxY()));
		}

	}

	private void fillSelectionAndBorder(Graphics2D g2d, TabsPainter tabsPainter, boolean hasFocus, ShapeTransform fillPath) throws IllegalAccessException {
		g2d.setColor(hasFocus ? tabsPainter.getActiveColor() : tabsPainter.getInactiveMaskColor());
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
		protected Color activeColor;

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

		public Color getActiveColor() {
			return activeColor;
		}

		public void setActiveColor(Color activeColor) {
			this.activeColor = activeColor;
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

		// currently not used - delete?
		private Integer darcula_mask = DEFAULT_DARCULA_MASK.getRGB();
		private int darcula_opacity = 50;

		private Integer underlineColor = new Color(0x439EB8).getRGB();
		private Integer underlineColor_inactive = Color.BLACK.getRGB();
		private int underlineOpacity_inactive = 0;

		private int darcula_activeColor = new Color(173, 46, 156).getRGB();
		private int classic_activeColor = new Color(173, 46, 156).getRGB();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

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

		public Integer getUnderlineColor() {
			return underlineColor;
		}

		public void setUnderlineColor(Integer underlineColor) {
			this.underlineColor = underlineColor;
		}

		public Integer getUnderlineColor_inactive() {
			return underlineColor_inactive;
		}

		public void setUnderlineColor_inactive(Integer underlineColor_inactive) {
			this.underlineColor_inactive = underlineColor_inactive;
		}

		public int getUnderlineOpacity_inactive() {
			return underlineOpacity_inactive;
		}

		public void setUnderlineOpacity_inactive(int underlineOpacity_inactive) {
			this.underlineOpacity_inactive = underlineOpacity_inactive;
		}

		@Transient
		public void setClassicOpacity(String text) {
			try {
				this.classic_opacity = parse(text);
			} catch (Exception e) {
				classic_opacity = DEFAULT_OPACITY;
			}
		}

		@Transient
		public void setDarculaOpacity(String text) {
			try {
				this.darcula_opacity = parse(text);
			} catch (Exception e) {
				darcula_opacity = DEFAULT_DARCULA_OPACITY;
			}
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

		public int getDarcula_activeColor() {
			return darcula_activeColor;
		}

		public void setDarcula_activeColor(int darcula_activeColor) {
			this.darcula_activeColor = darcula_activeColor;
		}

		public int getClassic_activeColor() {
			return classic_activeColor;
		}

		public void setClassic_activeColor(int classic_activeColor) {
			this.classic_activeColor = classic_activeColor;
		}
	}
}
