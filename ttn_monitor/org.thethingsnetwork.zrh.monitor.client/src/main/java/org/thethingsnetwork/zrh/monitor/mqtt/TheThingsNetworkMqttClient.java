package org.thethingsnetwork.zrh.monitor.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttBrokerProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttClientIdProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttGatewaysTopicProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttNodesTopicProperty;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;

@ApplicationScoped
public class TheThingsNetworkMqttClient implements MqttCallback {
	private static final Logger LOG = LoggerFactory.getLogger(TheThingsNetworkMqttClient.class);

	private String m_broker = null;
	private String m_clientId = null;
	private MqttClient m_client = null;
	private MemoryPersistence m_persistence = null;
	private MqttConnectOptions m_connectOptions = null;
	private TheThingsNetworkModel m_model = null;

	public TheThingsNetworkMqttClient() {
		m_broker = CONFIG.getPropertyValue(MqttBrokerProperty.class);
		m_clientId = CONFIG.getPropertyValue(MqttClientIdProperty.class);
		m_persistence = new MemoryPersistence();

		try {
			m_client = new MqttClient(m_broker, m_clientId, m_persistence);
			m_client.setCallback(this);
			
			LOG.info(TEXTS.get("MqttClientOk"));
		} 
		catch(MqttException e) {
			LOG.warn(TEXTS.get("MqttCreationFailed") + ", e:" + e.getLocalizedMessage());
		}
		
		m_model = new TheThingsNetworkModel();
	}

	public void connect() {
		try {
			m_connectOptions = new MqttConnectOptions();
			m_client.connect(m_connectOptions);
			LOG.info(TEXTS.get("MqttConnectOk") + " " + m_broker);			
		} 
		catch (MqttException e) {
			LOG.warn(TEXTS.get("MqttConnectFailed") + " " + m_broker + ", e:" + e.getLocalizedMessage());
		}
	}
	
	public void reconnect() {
		connect();
		subscribe(CONFIG.getPropertyValue(MqttGatewaysTopicProperty.class));
		subscribe(CONFIG.getPropertyValue(MqttNodesTopicProperty.class));
	}
	
	public void subscribe(String topicFilter) {
		if(StringUtility.isNullOrEmpty(topicFilter)) {
			LOG.warn(TEXTS.get("MqttSubscribeNull"));
			return;
		}
		
		try {
			m_client.subscribe(topicFilter);
			LOG.info(TEXTS.get("MqttSubscribed") + " " + topicFilter);
		} 
		catch (MqttException e) {
			LOG.warn(TEXTS.get("MqttSubscribeFailed") + " '" + topicFilter + "', e:" + e.getLocalizedMessage());
		}
	}

	public void disconnect() {
		try {
			m_client.disconnect();
		} 
		catch (MqttException e) {
			LOG.warn(TEXTS.get("MqttDisconnectFailed") + ", e:" + e.getLocalizedMessage());
		}
	}

	@Override
	public void connectionLost(Throwable t) {
		// TODO add mqtt connection lost logic
		LOG.warn(TEXTS.get("MqttConnectionLost") + ", e:" + t.getLocalizedMessage());
		t.printStackTrace();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// nop, this client is not publishing anything
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
//		LOG.info(TEXTS.get("MqttMessage") + " topic:" + topic + " message:" + message);
		
		String messageData = new String(message.getPayload());
		m_model.addMessage(messageData);
	}
	
	public TheThingsNetworkModel getModel() {
		return m_model;
	}
}
