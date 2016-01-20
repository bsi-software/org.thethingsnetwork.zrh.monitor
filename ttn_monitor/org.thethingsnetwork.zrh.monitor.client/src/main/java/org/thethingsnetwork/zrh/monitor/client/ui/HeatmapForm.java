package org.thethingsnetwork.zrh.monitor.client.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.AbstractHeatmapField;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatPoint;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatmapViewParameter;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.IHeatmapField;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.IHeatmapListener;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.MapPoint;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MapLatitudeProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MapLongitudeProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MapZoomProperty;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.CloseButton;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.LiveMapField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.CenterXField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.CenterYField;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm.MainBox.TopBox.ViewParamterBox.ZoomLevelField;
import org.thethingsnetwork.zrh.monitor.model.Gateway;
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


	@Override
	protected void execInitForm() {
		updateViewParamFields();
		getLiveMapField().addPropertyChangeListener(IHeatmapField.PROP_VIEW_PARAMETER, m_viewParameterListener);
		getLiveMapField().addHeatmapListener(new IHeatmapListener() {

			@Override
			public void mapClicked(MapPoint point) {				
				// TODO add business logic when user clicks on map
				// BigDecimal latitude = point.getX();
				// BigDecimal longitude = point.getY();
			}

			@Override
			public void heatPointsAdded(Collection<HeatPoint> points) {
			}

		});
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

				private void resetHeatPoints() {
					TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
					List<HeatPoint> heatPoints = new ArrayList<>();

					// fill ttn gateway model info into heat points
					for(String eui: model.getGatewayEuis()) {
						Gateway g = model.getGateway(eui);
						HeatPoint hp = new HeatPoint(
								BigDecimal.valueOf(g.getLocation().getLatitude()),
								BigDecimal.valueOf(g.getLocation().getLongitude()),
								INTENSITY_FLOOR + g.messages() * INTENSITY_MESSAGE 
								);

						heatPoints.add(hp);
					}

					setHeatPoints(heatPoints);
				}

				@Override
				public HeatmapViewParameter getConfiguredViewParameter() {
					BigDecimal latitude = BigDecimal.valueOf(CONFIG.getPropertyValue(MapLatitudeProperty.class));
					BigDecimal longitude = BigDecimal.valueOf(CONFIG.getPropertyValue(MapLongitudeProperty.class));
					MapPoint center = new MapPoint(latitude, longitude);
					int zoomFactor = CONFIG.getPropertyValue(MapZoomProperty.class);

					return new HeatmapViewParameter(center, zoomFactor);
				}

				public void reset() {
					setViewParameter(getConfiguredViewParameter());
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
//			@Override
//			protected String getConfiguredLabel() {
//				return TEXTS.get("Close");
//			}

//			@Override
//			protected void execClickAction() {
//			}
		}
		
		
	}

	public class PageFormHandler extends AbstractFormHandler {
	}
}
