package org.thethingsnetwork.zrh.monitor.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.RestUrlGatewaysProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.RestUrlNodesProperty;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.AccNoiseLevelColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.CompleteTextColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.DataColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.GatewayEuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.MaxNoiseLevelColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.NodeEuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.PlainDataColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.TimestampColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.TypeColumn;
import org.thethingsnetwork.zrh.monitor.model.Message;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

public class MessageTablePage extends AbstractPageWithTable<MessageTablePage.Table> {

	public static final String REST_GATEWAYS = "http://thethingsnetwork.org/api/v0/gateways/";
	public static final String REST_NODES = "http://thethingsnetwork.org/api/v0/nodes/";

	private String m_title;
	private String m_gatewayEui;
	private String m_nodeEui;

	public MessageTablePage(String title) {
		if(title != null) {
			m_title = title;
		}
		else {
			m_title = TEXTS.get("Messages");	
		}
	}

	@Override
	protected String getConfiguredTitle() {
		return m_title;
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

	protected IDesktop getDesktop() {
		return ClientRunContexts.copyCurrent().getDesktop();
	}

	@Override
	protected void execLoadData(SearchFilter filter) throws ProcessingException {
		String gatewayEui = getGatewayEui();
		String nodeEui = getNodeEui();

		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		Table table = getTable();
		TimestampColumn timeCol =  table.getTimestampColumn();
		TypeColumn typeCol = table.getTypeColumn();
		GatewayEuiColumn gwyEuiCol = table.getGatewayEuiColumn();		
		NodeEuiColumn ndeEuiCol = table.getNodeEuiColumn();
		PlainDataColumn plainCol = table.getPlainDataColumn();
		DataColumn dataCol = table.getDataColumn();
		CompleteTextColumn textCol = table.getCompleteTextColumn();
		MaxNoiseLevelColumn maxNoiseCol = table.getMaxNoiseLevelColumn();
		AccNoiseLevelColumn accNoiseCol = table.getAccNoiseLevelColumn();

		List<Message> messages = new ArrayList<>();
		List<ITableRow> rows = new ArrayList<>();

		if(StringUtility.hasText(gatewayEui)) {
			messages = model.getGateway(gatewayEui).getMessages();
			table.getGatewayEuiColumn().setDisplayable(false);
		}
		else if(StringUtility.hasText(nodeEui)) {
			messages = model.getNode(nodeEui).getMessages();
			table.getTypeColumn().setDisplayable(false);
			table.getNodeEuiColumn().setDisplayable(false);
		}
		else {
			messages = model.getMessages();
		}

		// fill messages into table rows
		for(Message m : messages) {
			TableRow r = new TableRow(table.getColumnSet());
			String text = m.getCompleteMessage();
			String data = m.getData();
			String plainData = m.getPlainData();

			r.getCellForUpdate(timeCol).setValue(m.getTimestamp());
			r.getCellForUpdate(typeCol).setValue(m.isNodeMessage() ? TEXTS.get("TtnMessagePacket") : TEXTS.get("TtnMessageStatus"));
			r.getCellForUpdate(ndeEuiCol).setValue(m.getNodeEui());
			r.getCellForUpdate(gwyEuiCol).setValue(m.getGatewayEui());
			r.getCellForUpdate(plainCol).setValue(plainData);
			r.getCellForUpdate(dataCol).setValue(data);
			r.getCellForUpdate(textCol).setValue(text);
			
			if(m.isNoiseMessage()) {
				Integer maxNoise = m.getMaxNoise();
				Integer accNoise = m.getAccNoise();
				r.getCellForUpdate(maxNoiseCol).setValue(maxNoise);
				r.getCellForUpdate(accNoiseCol).setValue(accNoise);				
			}

			rows.add(r);
		}

		table.discardAllRows();
		table.addRows(rows);

		return;

	}

	/*
	private String toPlainText(String encodedText) {
		if(encodedText == null) {
			return null;
		}
		
		byte [] data = Base64.getDecoder().decode(encodedText);
		StringBuffer plain = new StringBuffer();

		for(int i = 0; i < data.length; i++) {
			plain.append((char)data[i]);
		}

		return plain.toString();
	}
	*/

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

		public PlainDataColumn getPlainDataColumn() {
			return getColumnSet().getColumnByClass(PlainDataColumn.class);
		}

		public MaxNoiseLevelColumn getMaxNoiseLevelColumn() {
			return getColumnSet().getColumnByClass(MaxNoiseLevelColumn.class);
		}

		public AccNoiseLevelColumn getAccNoiseLevelColumn() {
			return getColumnSet().getColumnByClass(AccNoiseLevelColumn.class);
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

		@Order(2000.0)
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

		@Order(3000.0)
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

		@Order(4000.0)
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

		@Order(5000.0)
		public class MaxNoiseLevelColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("MaxNoise");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return false;
			}
		}

		@Order(6000.0)
		public class AccNoiseLevelColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("AccNoise");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return false;
			}
		}
		
		@Order(7000.0)
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

		@Order(8000.0)
		public class PlainDataColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("PlainData");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(9000.0)
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
				Table table = getTable();
				String messageText = table.getCompleteTextColumn().getSelectedValue();
				messageText = messageText.replaceAll("\\{", "{\n");
				messageText = messageText.replaceAll("\\,", ",\n");
				messageText = messageText.replaceAll("\\}", "\n}");

				MessageForm form = new MessageForm();
				form.getGatewayEuiField().setValue(table.getGatewayEuiColumn().getSelectedValue());
				form.getTimestampField().setValue(table.getTimestampColumn().getSelectedValue());
				form.getCompleteTextField().setValue(messageText);

				if(table.getTypeColumn().getSelectedValue().equals("Status")) {
					form.getMaxNoiseField().setVisible(false);
					form.getAccNoiseField().setVisible(false);
					form.getDataField().setVisible(false);
				}
				else {
					form.getMaxNoiseField().setValue(table.getMaxNoiseLevelColumn().getSelectedValue());
					form.getAccNoiseField().setValue(table.getAccNoiseLevelColumn().getSelectedValue());					
					form.getDataField().setValue(table.getDataColumn().getSelectedValue());
					form.getPlainDataField().setValue(table.getPlainDataColumn().getSelectedValue());
				}

				form.start();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return "F6";
			}
		}

		@Order(2000.0)
		public class OpenMessageRestMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("OpenRest");
			}

			@Override
			protected void execInitAction() {
				setVisible(getParentPage() == null);
			}

			@Override
			protected void execAction() {
				String gatewayEui = (String) getSelectedRow().getCell(getGatewayEuiColumn()).getValue();
				String nodeEui = (String) getSelectedRow().getCell(getNodeEuiColumn()).getValue();
				String restUrl = CONFIG.getPropertyValue(RestUrlGatewaysProperty.class) + gatewayEui;

				if(nodeEui != null) {
					restUrl = CONFIG.getPropertyValue(RestUrlNodesProperty.class) + nodeEui;
				}

				getDesktop().openUri(restUrl, OpenUriAction.NEW_WINDOW);
			}
		}
	}
}
