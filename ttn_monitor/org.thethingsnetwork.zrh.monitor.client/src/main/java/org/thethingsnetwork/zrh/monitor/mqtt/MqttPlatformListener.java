/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.thethingsnetwork.zrh.monitor.mqtt;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttGatewaysTopicProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttNodesTopicProperty;

/**
 * Connect to the MQTT broker and register for gateway and nodes topcis once the Scout platform is up and running.
 */
@Order(10)
public class MqttPlatformListener implements IPlatformListener {

	@Override
	public void stateChanged(PlatformEvent event) {
		if (event.getState() == State.BeanManagerValid) {
			TheThingsNetworkMqttClient mqtt = BEANS.get(TheThingsNetworkMqttClient.class);

			mqtt.connect();
			mqtt.subscribe(CONFIG.getPropertyValue(MqttGatewaysTopicProperty.class));
			mqtt.subscribe(CONFIG.getPropertyValue(MqttNodesTopicProperty.class));
		}
	}
}
