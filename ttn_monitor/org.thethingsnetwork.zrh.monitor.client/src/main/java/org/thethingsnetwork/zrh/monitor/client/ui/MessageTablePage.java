package org.thethingsnetwork.zrh.monitor.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.CompleteTextColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.DataColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.GatewayEuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.NodeEuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.TimestampColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.TypeColumn;
import org.thethingsnetwork.zrh.monitor.model.Gateway;
import org.thethingsnetwork.zrh.monitor.model.Message;
import org.thethingsnetwork.zrh.monitor.model.Node;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

public class MessageTablePage extends AbstractPageWithTable<MessageTablePage.Table> {

	private String m_gatewayEui;
	private String m_nodeEui;


	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("Messages");
	}	
	
	@Order(1000.0)
	public class RefreshMenu extends AbstractMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("Refresh");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet(TreeMenuType.SingleSelection, TreeMenuType.MultiSelection);
		}

		@Override
		protected void execAction() {
			execLoadData(null);
		}

		@Override
		protected String getConfiguredKeyStroke() {
			return "F5";
		}
	}
	
	@Override
	protected void execLoadData(SearchFilter filter) throws ProcessingException {
		String eui = getGatewayEui();
		String nodeEui = getNodeEui();
		
		if(StringUtility.hasText(eui)) {
			TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
			Gateway g = model.getGateway(eui);
			
			// can be null if it is added in my nodes page and no message has yet been received
			if(g == null) {
				return;
			}
			
			List<ITableRow> rows = new ArrayList<>();
			Table t = getTable();
			t.getGatewayEuiColumn().setDisplayable(false);
			
			TimestampColumn timeCol =  t.getTimestampColumn();
			TypeColumn typeCol = t.getTypeColumn();
			NodeEuiColumn ndeEuiCol = t.getNodeEuiColumn();
			DataColumn dataCol = t.getDataColumn();
			CompleteTextColumn textCol = t.getCompleteTextColumn();
			String txtStatus = TEXTS.get("TtnMessageStatus");
			String txtPacket = TEXTS.get("TtnMessagePacket");
			
			// fill ttn gateway messages into scout table page
			for(Message m : g.getMessages()) {
				TableRow r = new TableRow(t.getColumnSet());
				
				r.getCellForUpdate(timeCol).setValue(m.getTimestamp());
				r.getCellForUpdate(typeCol).setValue(m.isNodeMessage() ? txtPacket : txtStatus);
				r.getCellForUpdate(ndeEuiCol).setValue(m.getNodeEui());
				r.getCellForUpdate(dataCol).setValue(m.getData());
				r.getCellForUpdate(textCol).setValue(m.getCompleteMessage());
				
				rows.add(r);
			}
			
			getTable().discardAllRows();
			getTable().addRows(rows);
			
			return;
		}
		
		if(StringUtility.hasText(nodeEui)) {
			TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
			Node n = model.getNode(nodeEui);
			
			// can be null if it is added in my nodes page and no message has yet been received
			if(n == null) {
				return;
			}
			
			List<ITableRow> rows = new ArrayList<>();
			Table t = getTable();
			t.getTypeColumn().setDisplayable(false);
			t.getNodeEuiColumn().setDisplayable(false);
			
			TimestampColumn timeCol =  t.getTimestampColumn();
			GatewayEuiColumn gwyEuiCol = t.getGatewayEuiColumn();
			DataColumn dataCol = t.getDataColumn();
			CompleteTextColumn textCol = t.getCompleteTextColumn();
			
			// fill ttn gateway messages into scout table page
			for(Message m : n.getMessages()) {
				TableRow r = new TableRow(t.getColumnSet());
				
				r.getCellForUpdate(timeCol).setValue(m.getTimestamp());
				r.getCellForUpdate(gwyEuiCol).setValue(m.getGatewayEui());
				r.getCellForUpdate(dataCol).setValue(m.getData());
				r.getCellForUpdate(textCol).setValue(m.getCompleteMessage());
				
				rows.add(r);
			}
			
			getTable().discardAllRows();
			getTable().addRows(rows);
			
			return;
		}		
	}
	
	public void setGatewayEui(String eui) {
		m_gatewayEui = eui;
	}

	public String getGatewayEui() {
		return m_gatewayEui;
	}
	
	public void setNodeEui(String eui) {
		m_nodeEui = eui;
	}

	public String getNodeEui() {
		return m_nodeEui;
	}
	
	public class Table extends AbstractTable {
		
		@Override
		protected void execRowAction(ITableRow row) {
			getMenuByClass(OpenMessageMenu.class).execAction();
		}
		
		public GatewayEuiColumn getGatewayEuiColumn() {
			return getColumnSet().getColumnByClass(GatewayEuiColumn.class);
		}

		public TypeColumn getTypeColumn() {
			return getColumnSet().getColumnByClass(TypeColumn.class);
		}

		public DataColumn getDataColumn() {
			return getColumnSet().getColumnByClass(DataColumn.class);
		}

		public CompleteTextColumn getCompleteTextColumn() {
			return getColumnSet().getColumnByClass(CompleteTextColumn.class);
		}

		public NodeEuiColumn getNodeEuiColumn() {
			return getColumnSet().getColumnByClass(NodeEuiColumn.class);
		}

		public TimestampColumn getTimestampColumn() {
			return getColumnSet().getColumnByClass(TimestampColumn.class);
		}

		@Order(1000.0)
		public class TimestampColumn extends AbstractDateTimeColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Timestamp");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
			
			@Override
			protected String getConfiguredFormat() {
				return "yyyy.MM.dd HH:mm:ss.S";
			}
		}


		@Order(1500.0)
		public class TypeColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Type");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}


		@Order(2000.0)
		public class NodeEuiColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("NodeEui");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(3000.0)
		public class GatewayEuiColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("GatewayEui");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(4000.0)
		public class DataColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Data");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}


		@Order(5000.0)
		public class CompleteTextColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("CompleteText");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
			
			@Override
			protected boolean getConfiguredVisible() {
				return false;
			}
		}
		@Order(1000.0)
		public class OpenMessageMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("OpenMessage");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected void execAction() {
				String messageText = getTable().getCompleteTextColumn().getSelectedValue();
				messageText = messageText.replaceAll("\\{", "{\n");
				messageText = messageText.replaceAll("\\,", ",\n");
				messageText = messageText.replaceAll("\\}", "\n}");
				MessageBoxes.createOk().withHeader(TEXTS.get("CompleteMessageText")).withBody(messageText).show();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return "F6";
			}
		}
	}
}
