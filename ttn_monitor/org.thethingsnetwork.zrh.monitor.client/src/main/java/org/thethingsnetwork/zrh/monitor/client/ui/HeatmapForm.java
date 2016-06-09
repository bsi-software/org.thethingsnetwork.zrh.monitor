package org.thethingsnetwork.zrh.monitor.client.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.widgets.heatmap.client.ui.form.fields.heatmapfield.AbstractHeatmapField;
import org.eclipse.scout.widgets.heatmap.client.ui.form.fields.heatmapfield.HeatPoint;
import org.eclipse.scout.widgets.heatmap.client.ui.form.fields.heatmapfield.HeatmapViewParameter;
import org.eclipse.scout.widgets.heatmap.client.ui.form.fields.heatmapfield.IHeatmapField;
import org.eclipse.scout.widgets.heatmap.client.ui.form.fields.heatmapfield.MapPoint;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MapLatitudeProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MapLongitudeProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MapZoomProperty;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.CloseButton;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.LiveMapField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.CenterXField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.CenterYField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.TimeField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.ZoomLevelField;
import org.thethingsnetwork.zrh.monitor.model.Gateway;
import org.thethingsnetwork.zrh.monitor.model.Location;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

/**
 * <h3>{@link HeatmapForm}</h3>
 */
@Order(100000.0)
public class HeatmapForm extends AbstractForm {

	public static final Float INTENSITY_FLOOR = 1000.0f;
	public static final Float INTENSITY_MESSAGE = 1000.0f;
	
	private P_ViewParameterListener m_viewParameterListener = new P_ViewParameterListener();

	public HeatmapForm() {
		super();
	}

	@Override
	protected boolean getConfiguredAskIfNeedSave() {
		return false;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("Heatmap");
	}

	public MainBox getMainBox() {
		return getFieldByClass(MainBox.class);
	}

	public ViewParamterBox getMySequenceBox() {
		return getFieldByClass(ViewParamterBox.class);
	}

	public CloseButton getCloseButton() {
		return getFieldByClass(CloseButton.class);
	}

	public TimeField getTimeField() {
		return getFieldByClass(TimeField.class);
	}

	public TopBox getTopBox() {
		return getFieldByClass(TopBox.class);
	}

	public LiveMapField getLiveMapField() {
		return getFieldByClass(LiveMapField.class);
	}

	public CenterXField getCenterXField() {
		return getFieldByClass(CenterXField.class);
	}

	public CenterYField getCenterYField() {
		return getFieldByClass(CenterYField.class);
	}
	
	public ZoomLevelField getZoomLevelField() {
		return getFieldByClass(ZoomLevelField.class);
	}

	protected void handleMapClick(MapPoint point) {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		Double latitude = point.getY().doubleValue();
		Double longitude = point.getX().doubleValue();
		
		Gateway g = model.getGateway(new Location(latitude, longitude));
		boolean isNode = false;
		
		if(g != null) {
			int result = MessageBoxes.createYesNo()
    		.withHeader(TEXTS.get("Gateway") + " " + g.getEui())
    		.withBody(TEXTS.get("AddToMyGateways") + ": " + g.getLocation())
    		.show();
			
			if(result == IMessageBox.YES_OPTION) {
				model.addToFavorites(g.getEui(), isNode);
			}
		}
	}
	
	/**
	 * @return returns the initial view parameters for the heatmap field in this form.
	 */
	protected HeatmapViewParameter getInitialViewParametes() {
		BigDecimal latitude = BigDecimal.valueOf(CONFIG.getPropertyValue(MapLatitudeProperty.class));
		BigDecimal longitude = BigDecimal.valueOf(CONFIG.getPropertyValue(MapLongitudeProperty.class));
		MapPoint center = new MapPoint(latitude, longitude);
		int zoomFactor = CONFIG.getPropertyValue(MapZoomProperty.class);

		return new HeatmapViewParameter(center, zoomFactor);		
	}
	
	/**
	 * Compiles list of heat points to be added to the heat map field.
	 */
	protected List<HeatPoint> collectHeatPoints() {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		List<HeatPoint> heatPoints = new ArrayList<>();

		// fill ttn gateway model info into heat points
		for(String eui: model.getGatewayEuis()) {
			Gateway g = model.getGateway(eui);
			Location l = g.getLocation();
			
			if(l == null) {
				continue;
			}
			
			HeatPoint hp = new HeatPoint(
					BigDecimal.valueOf(g.getLocation().getLatitude()),
					BigDecimal.valueOf(g.getLocation().getLongitude()),
					INTENSITY_FLOOR + g.messages() * INTENSITY_MESSAGE 
					);

			heatPoints.add(hp);
		}
		
		return heatPoints;
	}
	
	/**
	 * Refresh the live map field and set the time field to the current time.
	 */
	public void refreshMap() {
		getTimeField().setValue(new Date());
		getLiveMapField().refresh();
	}
	
	/**
	 * Reacts on updates of the time field
	 */
	protected void timeValueChanged(Date time) {
		// NOP
	}
	
	@Override
	protected void execInitForm() {
		updateViewParamFields();
		getLiveMapField().addPropertyChangeListener(IHeatmapField.PROP_VIEW_PARAMETER, m_viewParameterListener);
		getTimeField().setEnabled(false);
	}

	private void updateViewParamFields() {
		HeatmapViewParameter viewParameter = getLiveMapField().getViewParameter();
		getCenterXField().setValue(viewParameter.getCenter().getX());
		getCenterYField().setValue(viewParameter.getCenter().getY());
		getZoomLevelField().setValue(viewParameter.getZoomFactor());
	}

	private final class P_ViewParameterListener implements PropertyChangeListener {

		private boolean m_enabled = true;

		public void setEnabled(boolean enabled) {
			m_enabled = enabled;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (m_enabled) {
				updateViewParamFields();
			}
		}
	}

	@Order(1000.0)
	public class MainBox extends AbstractGroupBox {

		@Order(1000.0)
		public class TopBox extends AbstractGroupBox {

			
			@Order(1000.0)
			public class LiveMapField extends AbstractHeatmapField {

				@Override
				protected boolean getConfiguredLabelVisible() {
					return false;
				}

				@Override
				protected int getConfiguredGridW() {
					return 2;
				}
				
				@Override
				protected int getConfiguredGridH() {
					return 5;
				}

				@Override
				protected void execInitField() {
					resetHeatPoints();
				}
				
				@Override
				public void handleClick(MapPoint point) {
					super.handleClick(point);
					handleMapClick(point);					
				}
				
				private void resetHeatPoints() {
					List<HeatPoint> heatPoints = collectHeatPoints();
					setHeatPoints(heatPoints);
				}

				@Override
				public HeatmapViewParameter getConfiguredViewParameter() {
					return getInitialViewParametes();
				}

				public void reset() {
					setViewParameter(getConfiguredViewParameter());
					getTimeField().setValue(new Date());
				}

				public void refresh() {
					resetHeatPoints();			
				}

			}


			@Order(2000.0)
			public class ViewParamterBox extends AbstractSequenceBox {

				@Override
				protected int getConfiguredGridW() {
					return 2;
				}

				@Override
				protected boolean getConfiguredLabelVisible() {
					return false;
				}

				@Override
				protected boolean getConfiguredAutoCheckFromTo() {
					return false;
				}

				@Order(1000.0)
				public class CenterXField extends AbstractBigDecimalField {

					@Override
					protected int getConfiguredMinFractionDigits() {
						return 5;
					}

					@Override
					protected int getConfiguredMaxFractionDigits() {
						return 5;
					}

					@Override
					protected int getConfiguredFractionDigits() {
						return 5;
					}

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("CenterX");
					}

					@Override
					protected void execChangedValue() {
						HeatmapViewParameter oldViewParameter = getLiveMapField().getViewParameter();
						try {
							m_viewParameterListener.setEnabled(false);
							MapPoint newCenter = new MapPoint(getValue(), oldViewParameter.getCenter().getY());
							getLiveMapField().setViewParameter(
									new HeatmapViewParameter(newCenter, oldViewParameter.getZoomFactor()));
						}
						finally {
							m_viewParameterListener.setEnabled(true);
						}
					}
				}

				@Order(2000.0)
				public class CenterYField extends AbstractBigDecimalField {

					@Override
					protected int getConfiguredMinFractionDigits() {
						return 5;
					}

					@Override
					protected int getConfiguredMaxFractionDigits() {
						return 5;
					}

					@Override
					protected int getConfiguredFractionDigits() {
						return 5;
					}

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("CenterY");
					}

					@Override
					protected void execChangedValue() {
						HeatmapViewParameter oldViewParameter = getLiveMapField().getViewParameter();
						try {
							m_viewParameterListener.setEnabled(false);
							MapPoint newCenter = new MapPoint(oldViewParameter.getCenter().getX(), getValue());
							getLiveMapField().setViewParameter(
									new HeatmapViewParameter(newCenter, oldViewParameter.getZoomFactor()));
						}
						finally {
							m_viewParameterListener.setEnabled(true);
						}
					}

				}

				@Order(3000.0)
				public class ZoomLevelField extends AbstractIntegerField {

					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("ZoomLevel");
					}

					@Override
					protected void execChangedValue() {
						HeatmapViewParameter oldViewParameter = getLiveMapField().getViewParameter();
						try {
							m_viewParameterListener.setEnabled(false);
							getLiveMapField()
							.setViewParameter(new HeatmapViewParameter(oldViewParameter.getCenter(), getValue()));
						}
						finally {
							m_viewParameterListener.setEnabled(true);
						}
					}
				}

				@Order(4000.0)
				public class TimeField extends AbstractDateTimeField {
					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("Time");
					}
					
					@Override
					protected void execChangedValue() {
						timeValueChanged(getValue());
					}
				}
				
				
			}
		}

		@Order(40)
		public class ResetMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("Reset");
			}

			@Override
			protected void execAction() {
				getLiveMapField().reset();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return "F6";
			}			
		}

		@Order(2000.0)
		public class CloseButton extends AbstractCloseButton {
		}
	}

	public class PageFormHandler extends AbstractFormHandler {
	}
}
