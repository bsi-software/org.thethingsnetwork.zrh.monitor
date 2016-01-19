package org.thethingsnetwork.zrh.monitor.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.thethingsnetwork.zrh.monitor.client.ui.NodeTablePage.Table.EuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.NodeTablePage.Table.MessagesColumn;
import org.thethingsnetwork.zrh.monitor.model.Node;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

public class NodeTablePage extends AbstractPageWithTable<NodeTablePage.Table> {
	
	private String m_title;
	private List<String> m_favorites;
	private boolean m_favoritesOnly;

	public NodeTablePage(String title) {
		m_title = title;
		m_favorites = null;
		m_favoritesOnly = false;
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

	public class Table extends AbstractTable {

		public MessagesColumn getMessagesColumn() {
			return getColumnSet().getColumnByClass(MessagesColumn.class);
		}

		public EuiColumn getEuiColumn() {
			return getColumnSet().getColumnByClass(EuiColumn.class);
		}

		@Order(1000.0)
		public class EuiColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("NodeEui");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}

			@Override
			protected boolean getConfiguredPrimaryKey() {
				return true;
			}
		}


		@Order(4000.0)
		public class MessagesColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Messages");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}
	}

	@Override
	protected void execLoadData(SearchFilter filter) {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		List<ITableRow> rows = new ArrayList<>();
		Table t = getTable();
		EuiColumn euiCol =  t.getEuiColumn();
		MessagesColumn msgCol = t.getMessagesColumn();
		
		// fill ttn node model info into scout table page
		for(String eui: model.getNodeEuis()) {

			// skip entries that don't match a favorite
			if(m_favoritesOnly && !m_favorites.contains(eui)) {
				continue;
			}

			Node n = model.getNode(eui);
			TableRow r = new TableRow(t.getColumnSet());
			r.getCellForUpdate(euiCol).setValue(eui);
			r.getCellForUpdate(msgCol).setValue(n.messages());
			rows.add(r);
		}

		// add favorites that don't have messages (yet)
		if(m_favoritesOnly) {
			Set<String> eui_with_messages = model.getGatewayEuis();
			for(String favorite: m_favorites) {
				if(!eui_with_messages.contains(favorite)) {
					TableRow r = new TableRow(t.getColumnSet());
					r.getCellForUpdate(euiCol).setValue(favorite);
					r.getCellForUpdate(msgCol).setValue(0);
					rows.add(r);
				}
			}
		}
		
		getTable().discardAllRows();
		getTable().addRows(rows);
	}
	
	@Override
	protected IPage<MessageTablePage.Table> execCreateChildPage(ITableRow row) throws ProcessingException {
	  MessageTablePage childPage = new MessageTablePage();
	  childPage.setNodeEui(getTable().getEuiColumn().getValue(row));
	  return childPage;
	}

	public void setFavorites(List<String> gateways) {
		m_favorites = gateways;
		m_favoritesOnly = true;
	}	
}
