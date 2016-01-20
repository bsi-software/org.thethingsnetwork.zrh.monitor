package org.thethingsnetwork.zrh.monitor.mqtt;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.session.IGlobalSessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttGatewaysTopicProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttNodesTopicProperty;

public class SessionListener implements IGlobalSessionListener {

	TheThingsNetworkMqttClient m_mqtt = null;

	@Override
	public void sessionChanged(SessionEvent event) {
		switch (event.getType()) {
		case SessionEvent.TYPE_STARTED:
			m_mqtt = BEANS.get(TheThingsNetworkMqttClient.class);
			m_mqtt.setRunContext(ClientRunContexts.copyCurrent());
			m_mqtt.connect();
			m_mqtt.subscribe(CONFIG.getPropertyValue(MqttGatewaysTopicProperty.class));
			m_mqtt.subscribe(CONFIG.getPropertyValue(MqttNodesTopicProperty.class));
			
			break;
		case SessionEvent.TYPE_STOPPED:
			if(m_mqtt != null) {
				m_mqtt.disconnect();
			}
			
			break;
		}
	}

}
