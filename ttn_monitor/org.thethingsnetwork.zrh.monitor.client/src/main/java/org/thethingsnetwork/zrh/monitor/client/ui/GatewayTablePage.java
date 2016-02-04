package org.thethingsnetwork.zrh.monitor.client.ui;

import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.NoiseColumn;

public class GatewayTablePage extends DeviceTablePage {

	public static final boolean IS_NODE_PAGE = false;

	public GatewayTablePage(String title) {
		super(title, IS_NODE_PAGE);
		
		getTable().getColumnSet().getColumnByClass(NoiseColumn.class).setDisplayable(false);
	} 
}
