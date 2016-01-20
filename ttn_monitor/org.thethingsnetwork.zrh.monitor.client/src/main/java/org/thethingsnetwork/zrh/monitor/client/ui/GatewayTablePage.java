package org.thethingsnetwork.zrh.monitor.client.ui;

import java.math.BigDecimal;
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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
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
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatmapViewParameter;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.MapPoint;
import org.thethingsnetwork.zrh.monitor.client.ui.GatewayTablePage.Table.EuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.GatewayTablePage.Table.LatitudeColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.GatewayTablePage.Table.LongitudeColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.GatewayTablePage.Table.MessagesColumn;
import org.thethingsnetwork.zrh.monitor.model.Gateway;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

// TODO check how to reduce copy paste (new super class? GatewayTablePage and NodeTablePage are very similar...) 
public class GatewayTablePage extends AbstractPageWithTable<GatewayTablePage.Table> {

	private String m_title;
	private List<String> m_favorites;
	private boolean m_favoritesOnly;

	public GatewayTablePage(String title) {
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

	@Override
	protected void execLoadData(SearchFilter filter) {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		List<ITableRow> rows = new ArrayList<>();
		Table t = getTable();
		EuiColumn euiCol =  t.getEuiColumn();
		LatitudeColumn latCol = t.getLatitudeColumn();
		LongitudeColumn lngCol = t.getLongitudeColumn();
		MessagesColumn msgCol = t.getMessagesColumn();

		// fill ttn gateway model info into scout table page
		for(String eui: model.getGatewayEuis()) {

			// skip entries that don't match a favorite
			if(m_favoritesOnly && !m_favorites.contains(eui)) {
				continue;
			}

			Gateway g = model.getGateway(eui);
			TableRow r = new TableRow(t.getColumnSet());
			r.getCellForUpdate(euiCol).setValue(eui);
			r.getCellForUpdate(latCol).setValue(g.getLocation().getLatitude());
			r.getCellForUpdate(lngCol).setValue(g.getLocation().getLongitude());
			r.getCellForUpdate(msgCol).setValue(g.messages());
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
		childPage.setGatewayEui(getTable().getEuiColumn().getValue(row));
		return childPage;
	}	

	public void setFavorites(List<String> gateways) {
		m_favorites = gateways;
		m_favoritesOnly = true;
	}

	public class Table extends AbstractTable {

		
		@Order(1000.0)
		public class ShowOnMapMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("ShowOnMap");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected void execAction() {
				BigDecimal latitude = getTable().getLatitudeColumn().getSelectedValue();
				BigDecimal longitude = getTable().getLongitudeColumn().getSelectedValue();
				HeatmapViewParameter parameter = new HeatmapViewParameter(new MapPoint(latitude, longitude), 17);
				
				HeatmapForm form = new HeatmapForm();
				form.getCloseButton().setVisible(true);
				form.getLiveMapField().setViewParameter(parameter);
				form.start();
			}
		}

		public MessagesColumn getMessagesColumn() {
			return getColumnSet().getColumnByClass(MessagesColumn.class);
		}

		public LongitudeColumn getLongitudeColumn() {
			return getColumnSet().getColumnByClass(LongitudeColumn.class);
		}

		public LatitudeColumn getLatitudeColumn() {
			return getColumnSet().getColumnByClass(LatitudeColumn.class);
		}

		public EuiColumn getEuiColumn() {
			return getColumnSet().getColumnByClass(EuiColumn.class);
		}

		@Order(1000.0)
		public class EuiColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Eui");
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

		@Order(2000.0)
		public class LatitudeColumn extends AbstractBigDecimalColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Latitude");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(3000.0)
		public class LongitudeColumn extends AbstractBigDecimalColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Longitude");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
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
}
