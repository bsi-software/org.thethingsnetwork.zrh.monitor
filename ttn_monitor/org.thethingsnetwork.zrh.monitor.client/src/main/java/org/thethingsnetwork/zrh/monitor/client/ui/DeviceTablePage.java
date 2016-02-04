package org.thethingsnetwork.zrh.monitor.client.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatmapViewParameter;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.MapPoint;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.RestUrlGatewaysProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.RestUrlNodesProperty;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.EuiColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.LatitudeColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.LongitudeColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.MessagesColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.NameColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.NoiseColumn;
import org.thethingsnetwork.zrh.monitor.client.ui.DeviceTablePage.Table.RemoveMenu;
import org.thethingsnetwork.zrh.monitor.client.ui.MessageTablePage.Table.OpenMessageRestMenu;
import org.thethingsnetwork.zrh.monitor.model.Device;
import org.thethingsnetwork.zrh.monitor.model.Location;
import org.thethingsnetwork.zrh.monitor.model.Node;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

public class DeviceTablePage extends AbstractPageWithTable<DeviceTablePage.Table> {

	private String m_title;
	private boolean m_isNodePage;
	private boolean m_favoritesOnly;

	public DeviceTablePage(String title, boolean isNodePage) {
		m_title = title;
		m_isNodePage = isNodePage;
		m_favoritesOnly = false;

		getTable().setEuiHeaderText(isNodePage ? TEXTS.get("NodeEui") : TEXTS.get("GatewayEui"));
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
			return CollectionUtility.hashSet(TreeMenuType.SingleSelection);
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
	protected void execPageActivated() {
		execLoadData(null);
	}

	@Override
	protected void execLoadData(SearchFilter filter) {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		Table t = getTable();
		EuiColumn euiCol =  t.getEuiColumn();
		NameColumn nameCol =  t.getNameColumn();
		NoiseColumn noiseCol = t.getNoiseColumn();
		LatitudeColumn latCol = t.getLatitudeColumn();
		LongitudeColumn lngCol = t.getLongitudeColumn();
		MessagesColumn msgCol = t.getMessagesColumn();
		List<ITableRow> rows = new ArrayList<>();
		Set<String> euis = null;

		if(m_isNodePage) {
			euis = model.getNodeEuis();
		}
		else {
			euis = model.getGatewayEuis();
		}

		// fill ttn gateway model info into scout table page
		for(String eui: euis) {

			// skip entries that don't match a favorite
			if(m_favoritesOnly && !model.hasFavorite(eui, m_isNodePage)) {
				continue;
			}

			Device d = null;
			if(m_isNodePage) {
				d = model.getNode(eui);
			}
			else {
				d = model.getGateway(eui);
			}

			TableRow r = new TableRow(t.getColumnSet());
			r.getCellForUpdate(euiCol).setValue(eui);
			r.getCellForUpdate(nameCol).setValue(d.getName());
			r.getCellForUpdate(msgCol).setValue(d.messages());

			if(d instanceof Node) {
				r.getCellForUpdate(noiseCol).setValue(((Node)d).isNoiseNode());
			}
			if(d.hasLocation()) {				
				r.getCellForUpdate(latCol).setValue(d.getLocation().getLatitude());
				r.getCellForUpdate(lngCol).setValue(d.getLocation().getLongitude());
			}

			rows.add(r);
		}

		// add favorites that don't have messages (yet)
		if(m_favoritesOnly) {
			for(String favorite: model.getFavorites(m_isNodePage)) {
				if(!euis.contains(favorite)) {
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
		MessageTablePage messagePage = new MessageTablePage(null);
		// TODO fix getMenu deprecation
		messagePage.getTable().getMenu(OpenMessageRestMenu.class).setVisible(false);

		if(row != null) {
			if(m_isNodePage) {
				messagePage.setNodeEui(getTable().getEuiColumn().getValue(row));
			}
			else {
				messagePage.setGatewayEui(getTable().getEuiColumn().getValue(row));
			}
		}

		return messagePage;
	}	

	public void setFavoritesPage(boolean isFavoritePage) {
		m_favoritesOnly = isFavoritePage;
		getTable().getMenuByClass(RemoveMenu.class).setVisible(m_favoritesOnly);
	}

	public boolean isFavoritesPage() {
		return m_favoritesOnly;
	}

	public class Table extends AbstractTable {

		public void setEuiHeaderText(String title) {
			IHeaderCell hc = getEuiColumn().getHeaderCell();

			if(hc instanceof HeaderCell) {
				((HeaderCell) hc).setText(title);
			}
		}


		@Order(1000.0)
		public class EditInfoMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("EditInfo");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected void execAction() {
				TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();				
				ITableRow row = getTable().getSelectedRow();
				DeviceForm form = new DeviceForm();

				String eui = (String) row.getKeyValues().get(0);
				Device device = null;

				if(m_isNodePage) {
					device = model.getNode(eui);
					form.getNoiseField().setValue(((Node)device).isNoiseNode());
				}
				else {
					device = model.getGateway(eui);
					form.getNoiseField().setVisible(false);
				}

				form.getNameField().setValue(device.getName());

				if(device.hasLocation()) {
					Location location = device.getLocation();
					form.getLatitudeField().setValue(new BigDecimal(location.getLatitude()));
					form.getLongitudeField().setValue(new BigDecimal(location.getLongitude()));					
				}

				form.start();
				form.waitFor();

				if(form.isFormStored()) {
					String name = form.getNameField().getValue();
					boolean noise = form.getNoiseField().getValue();
					
					// TODO first check if lat & long available, then get value
					if(!form.getLatitudeField().isEmpty() && !form.getLongitudeField().isEmpty()) {
						double latitude = form.getLatitudeField().getValue().doubleValue();
						double longitude = form.getLongitudeField().getValue().doubleValue();
						Location location = new Location(latitude, longitude);
						device.setLocation(location);
					}
					
					device.setName(name);
					((Node)device).setNoiseNode(noise);

					reloadPage();
				}
			}
		}


		@Order(2000.0)
		public class RemoveMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("Remove");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
			}

			@Override
			protected void execAction() {
				TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();

				for(ITableRow row: getTable().getSelectedRows()) {
					String eui = (String) row.getKeyValues().get(0);
					model.removeFromFavorites(eui, m_isNodePage);
				}

				reloadPage();
			}
		}

		@Order(2000.0)
		public class OpenRestMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("OpenRest");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected void execAction() {
				String eui = (String) getSelectedRow().getCell(getEuiColumn()).getValue();
				String restUrl = CONFIG.getPropertyValue(RestUrlGatewaysProperty.class) + eui;

				if(m_isNodePage) {
					restUrl = CONFIG.getPropertyValue(RestUrlNodesProperty.class) + eui;
				}

				getDesktop().openUri(restUrl, OpenUriAction.NEW_WINDOW);
			}
		}

		@Order(4000.0)
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
			protected void execSelectionChanged(boolean selection) {
				setEnabled(false);

				if(getSelectedRowCount() == 1) {
					BigDecimal latitude = getLatitudeColumn().getSelectedValue();
					BigDecimal longitude = getLongitudeColumn().getSelectedValue();

					if(latitude != null && longitude != null) {
						setEnabled(true);
					}					
				}
			}			

			@Override
			protected void execAction() {
				BigDecimal latitude = getLatitudeColumn().getSelectedValue();
				BigDecimal longitude = getLongitudeColumn().getSelectedValue();

				if(latitude != null && longitude != null) {
					HeatmapViewParameter parameter = new HeatmapViewParameter(new MapPoint(latitude, longitude), 17);

					HeatmapForm form = new HeatmapForm();
					form.getCloseButton().setVisible(true);
					form.getLiveMapField().setViewParameter(parameter);
					form.start();
				}
			}
		}

		public NoiseColumn getNoiseColumn() {
			return getColumnSet().getColumnByClass(NoiseColumn.class);
		}

		public NameColumn getNameColumn() {
			return getColumnSet().getColumnByClass(NameColumn.class);
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
				if(m_isNodePage) {
					return TEXTS.get("NodeEui");
				}
				else {
					return TEXTS.get("GatewayEui");
				}
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

		@Order(1500.0)
		public class NameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Name");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return false;
			}
		}


		@Order(1750.0)
		public class NoiseColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Noise");
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
