package org.thethingsnetwork.zrh.monitor.client.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatPoint;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.HeatmapViewParameter;
import org.eclipsescout.demo.widgets.client.custom.ui.form.fields.heatmapfield.MapPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thethingsnetwork.zrh.monitor.model.Message;
import org.thethingsnetwork.zrh.monitor.model.Node;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;
import org.thethingsnetwork.zrh.monitor.mqtt.TheThingsNetworkMqttClient;

public class NoisemapForm extends HeatmapForm {
	private static final Logger LOG = LoggerFactory.getLogger(HeatmapForm.class);

	// zurich
	// public static final double MAP_CENTER_LAT = 47.39;
	// public static final double MAP_CENTER_LONG = 8.51;
	// public static final int MAP_ZOOM = 17;
	
	// daettwil
	public static final double MAP_CENTER_LAT = 47.45018;
	public static final double MAP_CENTER_LONG = 8.29282;
	public static final int MAP_ZOOM = 17;

	protected HeatmapViewParameter getInitialViewParametes() {
		BigDecimal latitude = BigDecimal.valueOf(MAP_CENTER_LAT);
		BigDecimal longitude = BigDecimal.valueOf(MAP_CENTER_LONG);
		MapPoint center = new MapPoint(latitude, longitude);

		return new HeatmapViewParameter(center, MAP_ZOOM);		
	}

	@Override
	protected List<HeatPoint> collectHeatPoints() {
		TheThingsNetworkModel model = BEANS.get(TheThingsNetworkMqttClient.class).getModel();

		// extract latest message per node
		Map<String, Message> noiseMessages = new HashMap<>();
		for(Message m : model.getNoiseMessages()) {
			String eui = m.getNodeEui();

			if(!noiseMessages.containsKey(eui)) {
				noiseMessages.put(eui, m);
			}
			else {
				Message mOld = noiseMessages.get(eui);

				if(m.getTimestamp().compareTo(mOld.getTimestamp()) > 0) {
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

			HeatPoint hp = new HeatPoint(
					latitude,
					longitude,
					(float) (1.0 + m.getMaxNoise() * 5.0)
					);

			heatPoints.add(hp);
			
			LOG.info("added heat point: " + hp + " max noise=" + m.getMaxNoise() + " len(list)=" + heatPoints.size());

		}

		return heatPoints;
	}

	@Override
	protected void execInitForm() {
		setViewParameter();

		getLiveMapField().refresh();

		super.execInitForm();
	}

	private void setViewParameter() {
		BigDecimal centerLat = new BigDecimal(MAP_CENTER_LAT);
		BigDecimal centerLong = new BigDecimal(MAP_CENTER_LONG);
		HeatmapViewParameter viewParameter = new HeatmapViewParameter(new MapPoint(centerLat, centerLong), MAP_ZOOM);
		getLiveMapField().setViewParameter(viewParameter);
	}
}
