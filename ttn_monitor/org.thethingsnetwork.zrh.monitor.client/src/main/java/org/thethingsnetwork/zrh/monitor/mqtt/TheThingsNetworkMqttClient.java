package org.thethingsnetwork.zrh.monitor.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttBrokerProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttClientIdProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttGatewaysTopicProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.MqttNodesTopicProperty;
import org.thethingsnetwork.zrh.monitor.client.ui.HeatmapForm;
import org.thethingsnetwork.zrh.monitor.client.ui.NoisemapForm;
import org.thethingsnetwork.zrh.monitor.model.Message;
import org.thethingsnetwork.zrh.monitor.model.TheThingsNetworkModel;

@ApplicationScoped
public class TheThingsNetworkMqttClient implements MqttCallback {
	private static final Logger LOG = LoggerFactory.getLogger(TheThingsNetworkMqttClient.class);

	// link to scout run context
	private ClientRunContext m_clientRunContext;

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
		ModelJobs.schedule(new IRunnable() {

			@Override
			public void run() throws Exception {
				MessageBoxes.createOk()
				.withHeader(TEXTS.get("ConnectionLost"))
				.withBody(TEXTS.get("UseReconnect")).show();
			}

		}, ModelJobs.newInput(m_clientRunContext
				.withRunMonitor(BEANS.get(RunMonitor.class))));

		LOG.warn(TEXTS.get("MqttConnectionLost") + ", e:" + t.getLocalizedMessage());
		t.printStackTrace();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// nop, this client is not publishing anything
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		LOG.debug(TEXTS.get("MqttMessage") + " topic:" + topic + " message:" + message);

		String messageData = new String(message.getPayload());
		Message ttnMessage = m_model.addMessage(messageData);

		refreshGatewayMap();

		if(ttnMessage.isNoiseMessage()) {
			refreshNoiseMap();
		}

	}

	private void refreshGatewayMap() {
		ModelJobs.schedule(new IRunnable() {

			@Override
			public void run() throws Exception {
				IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
				IForm form = desktop.getPageDetailForm();

				if(form instanceof HeatmapForm && !(form instanceof NoisemapForm)) {
					((HeatmapForm)form).refreshMap();
				}
			}

		}, ModelJobs.newInput(m_clientRunContext
				.withRunMonitor(BEANS.get(RunMonitor.class))));
	}

	private void refreshNoiseMap() {
		ModelJobs.schedule(new IRunnable() {

			@Override
			public void run() throws Exception {
				IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
				IForm form = desktop.getPageDetailForm();

				if(form instanceof NoisemapForm) {
					((NoisemapForm)form).refreshMap();
				}
			}

		}, ModelJobs.newInput(m_clientRunContext
				.withRunMonitor(BEANS.get(RunMonitor.class))));

	}

	public void setRunContext(ClientRunContext context) {
		m_clientRunContext = context;
	}

	public TheThingsNetworkModel getModel() {
		return m_model;
	}
}
