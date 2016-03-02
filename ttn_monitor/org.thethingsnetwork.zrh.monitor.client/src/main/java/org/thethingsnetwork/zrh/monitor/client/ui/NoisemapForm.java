package org.thethingsnetwork.zrh.monitor.client.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatPoint;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatmapViewParameter;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.MapPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thethingsnetwork.zrh.monitor.model.Location;
import org.thethingsnetwork.zrh.monitor.model.Message;
import org.thethingsnetwork.zrh.monitor.model.Node;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

public class NoisemapForm extends HeatmapForm {
	private static final Logger LOG = LoggerFactory.getLogger(HeatmapForm.class);

	public static final String DEVICE_ID_TEMPLATE = "5A4801__";
	
	// zurich foerrlibuckstrasse
	public static final double MAP_CENTER_LAT = 47.39156;
	public static final double MAP_CENTER_LONG = 8.51105;
	public static final int MAP_ZOOM = 17;

	protected HeatmapViewParameter getInitialViewParametes() {
		BigDecimal latitude = BigDecimal.valueOf(MAP_CENTER_LAT);
		BigDecimal longitude = BigDecimal.valueOf(MAP_CENTER_LONG);
		MapPoint center = new MapPoint(latitude, longitude);

		return new HeatmapViewParameter(center, MAP_ZOOM);		
	}

	@Override
	protected void handleMapClick(MapPoint point) {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();
		double latitude = point.getX().doubleValue();
		double longitude = point.getY().doubleValue();
		
		DeviceForm form = new DeviceForm();
		form.getEuiField().setValue(DEVICE_ID_TEMPLATE);
		form.getNoiseField().setValue(true);
		form.getLatitudeField().setValue(new BigDecimal(latitude));
		form.getLongitudeField().setValue(new BigDecimal(longitude));
		form.getEuiField().setEnabled(true);
		form.getNoiseField().setEnabled(false);
		form.getLatitudeField().setEnabled(false);
		form.getLongitudeField().setEnabled(false);
		
		form.start();
		form.waitFor();
		
		if(form.isFormStored()) {
			String eui = form.getEuiField().getValue();
			Node node = model.getNode(eui);
			
			node.setName(form.getNameField().getValue());
			node.setNoiseNode(true);
			node.setLocation(new Location(latitude, longitude));
		}
	}
	
	@Override
	public void refreshMap() {
		Date timeNow = new Date();
		Date timeField = getTimeField().getValue();
		
		// if time is missing: set to current time
		if(timeField == null) {
			getTimeField().setValue(timeNow);
		}
		// if time is almost now (max 60 secs ago), set to current time
		else if(Math.abs(timeNow.getTime() - timeField.getTime()) <= 60000) {
			getTimeField().setValue(timeNow);
		}
		
		getLiveMapField().refresh();
	}
	
	@Override
	protected List<HeatPoint> collectHeatPoints() {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();

		// extract latest message per node
		Map<String, Message> noiseMessages = new HashMap<>();
		Date time = getTimeField().getValue();
		long targetTime = System.currentTimeMillis();
		
		if(time != null) {
			targetTime = time.getTime();
		}
		
		for(Message m : model.getNoiseMessages()) {
			String eui = m.getNodeEui();

			if(!noiseMessages.containsKey(eui)) {
				noiseMessages.put(eui, m);
			}
			else {
				Message mOld = noiseMessages.get(eui);
				long diffOld = Math.abs(mOld.getTimestamp().getTime() - targetTime);
				long diffNew = Math.abs(m.getTimestamp().getTime() - targetTime);

				if(diffNew < diffOld) {
					noiseMessages.put(eui, m);
				}
			}
		}

		// convert latest messages to heat points
		List<HeatPoint> heatPoints = new ArrayList<>();
		for(Message m: noiseMessages.values()) {
			String eui = m.getNodeEui();
			Node node = model.getNode(eui);
			
			if(!node.hasLocation()) {
				continue;
			}
			
			BigDecimal latitude = BigDecimal.valueOf(node.getLocation().getLatitude());
			BigDecimal longitude = BigDecimal.valueOf(node.getLocation().getLongitude());
			float noise = m.getMaxNoise();
			HeatPoint hp = new HeatPoint(latitude, longitude, noise2heat(noise));
			heatPoints.add(hp);
			
			LOG.info("added heat point: " + hp + " max noise=" + m.getMaxNoise() + " time=" + m.getTimestamp() + " len(list)=" + heatPoints.size());

		}
		
		// uncomment to add samples to help calibrate noise 2 heat value conversion
		// heatPoints = addSampleNoiseHeatPoints(heatPoints);

		return heatPoints;
	}

	/**
	 * Fine tune noise value to heat value for display on map.
	 * @param noise
	 * @return heat value to represent noise level
	 */
	private float noise2heat(float noise) {
		if(noise < 2.0) {
			return (float) (2.0);
		}
		if(noise <= 10.0) {
			return (float) (6.0 * noise / 10.0);
		}
		if(noise <= 15.0) {
			return (float) (10.0 * noise / 15.0);
		}
		
		return (float) (12.0 * noise / 18.0);
	}

	/**
	 * Add virtual noise samples for calibration of display heat values.
	 * Only for development, not needed for production.
	 * @param noise
	 * @return heat value to represent noise level
	 */
	private List<HeatPoint> addSampleNoiseHeatPoints(List<HeatPoint> heatPoints) {
		double lat = MAP_CENTER_LAT;
		double lng = MAP_CENTER_LONG;
		float [] noise = new float[] {0.0f, 2.0f, 4.0f, 6.0f, 8.0f, 10.0f, 12.0f, 14.0f, 16.0f, 18.0f, 20.0f};
		
		for(int i = 0; i < noise.length; i++) {
			HeatPoint hp = new HeatPoint(new BigDecimal(lat), new BigDecimal(lng), noise2heat(noise[i]));
			heatPoints.add(hp);
			
			lng += 0.0005;
		}
		
		return heatPoints;
	}

	@Override
	protected void execInitForm() {
		setViewParameter();

		getLiveMapField().refresh();

		super.execInitForm();
		getTimeField().setEnabled(true);
		getTimeField().setValue(new Date());
	}
	
	@Override
	protected void timeValueChanged(Date time) {
		getLiveMapField().refresh();
	}

	@Override
	protected void execFormActivated() {
		getLiveMapField().refresh();
	}
	
	private void setViewParameter() {
		BigDecimal centerLat = new BigDecimal(MAP_CENTER_LAT);
		BigDecimal centerLong = new BigDecimal(MAP_CENTER_LONG);
		HeatmapViewParameter viewParameter = new HeatmapViewParameter(new MapPoint(centerLat, centerLong), MAP_ZOOM);
		getLiveMapField().setViewParameter(viewParameter);
	}
}
