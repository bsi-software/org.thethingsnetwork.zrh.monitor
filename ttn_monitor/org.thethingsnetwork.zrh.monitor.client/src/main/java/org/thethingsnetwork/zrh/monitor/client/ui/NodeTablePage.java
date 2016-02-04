package org.thethingsnetwork.zrh.monitor.client.ui;

import org.eclipse.scout.rt.shared.TEXTS;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.AddToMyDeviceMenu;

public class NodeTablePage extends DeviceTablePage {

	public static final boolean IS_NODE_PAGE = true;
	
	public NodeTablePage(String title) {
		super(title, IS_NODE_PAGE);
		
		getTable().getMenu(AddToMyDeviceMenu.class).setText(TEXTS.get("AddToMyNodesPage"));
	} 
}
