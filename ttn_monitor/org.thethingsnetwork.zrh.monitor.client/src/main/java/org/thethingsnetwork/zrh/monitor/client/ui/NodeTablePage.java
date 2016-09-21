package org.thethingsnetwork.zrh.monitor.client.ui;

public class NodeTablePage extends DeviceTablePage {

	public static final boolean IS_NODE_PAGE = true;
	
	public NodeTablePage(String title) {
		super(title, IS_NODE_PAGE);
		
//		getTable().getMenuByClass(AddToMyDeviceMenu.class).setText(TEXTS.get("AddToMyNodesPage"));
	} 
}
