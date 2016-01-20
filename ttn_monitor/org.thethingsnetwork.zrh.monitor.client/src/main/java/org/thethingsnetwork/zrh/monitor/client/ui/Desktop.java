package org.thethingsnetwork.zrh.monitor.client.ui;

import java.util.List;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.thethingsnetwork.zrh.monitor.client.ClientSession;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

/**
 * <h3>{@link Desktop}</h3>
 *
 * @author mzi
 */
public class Desktop extends AbstractDesktop {

	private final DesktopStyle m_desktopStyle;

	public Desktop(DesktopStyle desktopStyle) {
		super(false);
		if (desktopStyle == null) {
			throw new IllegalArgumentException("desktopStyle cannot be null");
		}
		m_desktopStyle = desktopStyle;
		callInitializer();
	}

	@Override
	protected DesktopStyle getConfiguredDesktopStyle() {
		return m_desktopStyle;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("ApplicationTitle");
	}

	@Override
	protected List<Class<? extends IOutline>> getConfiguredOutlines() {
		return CollectionUtility.<Class<? extends IOutline>> arrayList(TheThingsNetworkOutline.class);
	}

	@Override
	protected void execGuiAttached() {
		super.execGuiAttached();
		selectFirstVisibleOutline();
	}

	protected void selectFirstVisibleOutline() {
		for (IOutline outline : getAvailableOutlines()) {
			if (outline.isEnabled() && outline.isVisible()) {
				setOutline(outline);
				break;
			}
		}
	}

	@Order(1000)
	public class FileMenu extends AbstractMenu {

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("File");
		}

		@Order(1000.0)
		public class ResetModelMenu extends AbstractMenu {

			@Override
			protected String getConfiguredText() {
				return TEXTS.get("ResetModel");
			}

			@Override
			protected void execAction() {
				BEANS.get(TheThingsNetworkMqttClient.class).getModel().reset();
			}
		}

		@Order(2000.0)
		public class ExitMenu extends AbstractMenu {

			@Override
			protected String getConfiguredText() {
				return TEXTS.get("Exit");
			}

			@Override
			protected void execAction() {
				ClientSessionProvider.currentSession(ClientSession.class).stop();
			}
		}
	}

	@Order(10.0)
	public class RefreshOutlineKeyStroke extends AbstractKeyStroke {

		@Override
		protected String getConfiguredKeyStroke() {
			return IKeyStroke.F5;
		}

		@Override
		protected void execAction() {
			if (getOutline() != null) {
				IPage<?> page = getOutline().getActivePage();
				if (page != null) {
					page.reloadPage();
				}
			}
		}
	}

	@Order(1000.0)
	public class WorkOutlineViewButton extends AbstractOutlineViewButton {

		public WorkOutlineViewButton() {
			this(TheThingsNetworkOutline.class);
		}

		protected WorkOutlineViewButton(Class<? extends TheThingsNetworkOutline> outlineClass) {
			super(Desktop.this, outlineClass);
		}

		@Override
		protected String getConfiguredKeyStroke() {
			return IKeyStroke.F2;
		}
	}
}
