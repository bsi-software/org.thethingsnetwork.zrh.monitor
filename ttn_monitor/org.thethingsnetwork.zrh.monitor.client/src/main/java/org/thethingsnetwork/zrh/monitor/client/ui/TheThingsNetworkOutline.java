package org.thethingsnetwork.zrh.monitor.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.GatewayFavoritesProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.NodeFavoritesProperty;
import org.thethingsnetwork.zrh.monitor.shared.Icons;

/**
 * <h3>{@link TheThingsNetworkOutline}</h3>
 *
 * @author mzi
 */
@Order(1000)
public class TheThingsNetworkOutline extends AbstractOutline {

	@Override
	protected void execCreateChildPages(List<IPage<?>> pageList) {
		super.execCreateChildPages(pageList);

		pageList.add(new HeatmapPage());

		GatewayTablePage gatewayPage = new GatewayTablePage(TEXTS.get("MyGateways"));
		gatewayPage.setFavorites(getStringList(CONFIG.getPropertyValue(GatewayFavoritesProperty.class)));
		gatewayPage.setExpanded(true);
		pageList.add(gatewayPage);

		NodeTablePage nodePage = new NodeTablePage(TEXTS.get("MyNodes"));
		nodePage.setFavorites(getStringList(CONFIG.getPropertyValue(NodeFavoritesProperty.class)));
		nodePage.setExpanded(true);
		pageList.add(nodePage);

		pageList.add(new GatewayTablePage(TEXTS.get("AllGateways")));
		pageList.add(new NodeTablePage(TEXTS.get("AllNodes")));
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("TTN");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.World;
	}

	private List<String> getStringList(String csv) {
		if(StringUtility.hasText(csv) && !ConfigProperties.FAVORITE_GATEWAYS_DEFAULT.equals(csv)) {
			return Arrays.asList(csv.split(","));
		}
		else {
			return new ArrayList<String>();
		}
	}
}
