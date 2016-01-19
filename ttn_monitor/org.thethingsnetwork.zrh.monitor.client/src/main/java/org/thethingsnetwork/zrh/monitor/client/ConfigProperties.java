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
package org.thethingsnetwork.zrh.monitor.client;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;

/**
 * Holds application configuration properties defined in the config.properties file 
 * @author mzi
 */
public final class ConfigProperties {

	public static final String MAP_LATITUDE_KEY = "map.latitude";
	public static final Double MAP_LATITUDE_DEFAULT = 43.0;

	public static final String MAP_LONGITUDE_KEY = "map.longitude";
	public static final Double MAP_LONGITUDE_DEFAULT = 15.0;

	public static final String MAP_ZOOM_KEY = "map.zoom";
	public static final Integer MAP_ZOOM_DEFAULT = 2;

	public static final String FAVORITE_GATEWAYS_KEY = "gateway.favorites";
	public static final String FAVORITE_GATEWAYS_DEFAULT = "<NONE>";
	
	public static final String FAVORITE_NODES_KEY = "node.favorites";
	public static final String FAVORITE_NODES_DEFAULT = FAVORITE_GATEWAYS_DEFAULT;
	
	public static final String MQTT_BROKER_KEY = "ttn.mqtt.broker";
	public static final String MQTT_BROKER_DEFAULT = "tcp://croft.thethings.girovito.nl:1883";
	
	public static final String MQTT_CLIENT_ID_KEY = "ttn.mqtt.clientid";
	public static final String MQTT_CLIENT_ID_DEFAULT = "org.thethingsnetwork.zrh.monitor";

	public static final String MQTT_GATEWAYS_KEY = "ttn.mqtt.gateways";
	public static final String MQTT_GATEWAYS_DEFAULT = "gateways/+/status";

	public static final String MQTT_NODES_KEY = "ttn.mqtt.nodes";
	public static final String MQTT_NODES_DEFAULT = "nodes/+/packets";
	
	public static final String DB_AUTO_CREATE_KEY = "ttn.database.schema.autocreate";
	public static final Boolean DB_AUTO_CREATE_DEFAULT = true;

	public static final String SUPER_USER_KEY = "ttn.superuser";
	public static final String SUPER_USER_DEFAULT = "system";

	private ConfigProperties() {
	}


	public static class MapLatitudeProperty extends AbstractDoubleConfigProperty {

		@Override
		protected Double getDefaultValue() {
			return MAP_LATITUDE_DEFAULT;
		}

		@Override
		public String getKey() {
			return MAP_LATITUDE_KEY;
		}	  
	}

	public static class MapLongitudeProperty extends AbstractDoubleConfigProperty {

		@Override
		protected Double getDefaultValue() {
			return MAP_LONGITUDE_DEFAULT;
		}

		@Override
		public String getKey() {
			return MAP_LONGITUDE_KEY;
		}	  
	}

	public static class MapZoomProperty extends AbstractPositiveIntegerConfigProperty {

		@Override
		protected Integer getDefaultValue() {
			return MAP_ZOOM_DEFAULT;
		}

		@Override
		public String getKey() {
			return MAP_ZOOM_KEY;
		}	  
	}
	
	public static class SuperUserSubjectProperty extends AbstractSubjectConfigProperty {

		@Override
		protected Subject getDefaultValue() {
			return convertToSubject(SUPER_USER_DEFAULT);
		}

		@Override
		public String getKey() {
			return SUPER_USER_KEY;
		}
	}
	
	public static class DatabaseAutoCreateProperty extends AbstractBooleanConfigProperty {

		@Override
		protected Boolean getDefaultValue() {
			return DB_AUTO_CREATE_DEFAULT;
		}

		@Override
		public String getKey() {
			return DB_AUTO_CREATE_KEY;
		}
	}

	public static class MqttBrokerProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return MQTT_BROKER_DEFAULT;
		}

		@Override
		public String getKey() {
			return MQTT_BROKER_KEY;
		}	  
	}

	public static class MqttClientIdProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return MQTT_CLIENT_ID_DEFAULT;
		}

		@Override
		public String getKey() {
			return MQTT_CLIENT_ID_KEY;
		}	  
	}
	
	public static class MqttGatewaysTopicProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return MQTT_GATEWAYS_DEFAULT;
		}

		@Override
		public String getKey() {
			return MQTT_GATEWAYS_KEY;
		}	  
	}

	public static class MqttNodesTopicProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return MQTT_NODES_DEFAULT;
		}

		@Override
		public String getKey() {
			return MQTT_NODES_KEY;
		}	  
	}

	public static class GatewayFavoritesProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return FAVORITE_GATEWAYS_DEFAULT;
		}

		@Override
		public String getKey() {
			return FAVORITE_GATEWAYS_KEY;
		}	  
	}

	public static class NodeFavoritesProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return FAVORITE_NODES_DEFAULT;
		}

		@Override
		public String getKey() {
			return FAVORITE_NODES_KEY;
		}	  
	}
}
