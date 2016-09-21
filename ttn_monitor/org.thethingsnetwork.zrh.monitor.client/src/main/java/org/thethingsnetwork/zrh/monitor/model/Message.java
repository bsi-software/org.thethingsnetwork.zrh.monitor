package org.thethingsnetwork.zrh.monitor.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {
	private static final Logger LOG = LoggerFactory.getLogger(Message.class);
	
	public static final String EUI = "eui";
	public static final String NODE_EUI_1 = "node_eui";
	public static final String NODE_EUI_2 = "nodeEui";
	public static final String GATEWAY_EUI_1 = "gateway_eui";
	public static final String GATEWAY_EUI_2 = "gatewayEui";
	public static final String TIME = "time";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String FORMAT_TIMESTAMP = "yyyy-MM-ddHH:mm:ss";
	public static final String DATA = "data";
	
	public static final int TYPE_GATEWAY = 1;
	public static final int TYPE_NODE = 2;
	public static final int SOURCE_GENERIC = 1;
	public static final int SOURCE_NOISE = 2;
	
	private boolean m_parsedOk = true;
	private int m_type = TYPE_GATEWAY;
	private int m_source = SOURCE_GENERIC;
	private boolean m_noiseMessage = false;
	private Date m_timestamp = null;
	private String m_gatewayEui = null;
	private String m_nodeEui = null;
	private String m_eui = null;
	private String m_message = null;
	private String m_data = null;
	private Double m_latitude = null;
	private Double m_longitude = null;
	

	public Message(String messageData) {
		m_message = messageData;
		
		JSONObject json = new JSONObject(messageData);
		String time = json.getString(TIME);
		
		// get timestamp
		if(StringUtility.hasText(time)) {
			// 2016-01-15T13:07:56.008
			// yyyy-MM-ddTHH:mm:ss.SSS
			DateFormat format = new SimpleDateFormat(FORMAT_TIMESTAMP);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			try {
				String d = time.substring(0, 10);
				String t = time.substring(11,19);
				m_timestamp = format.parse(d + t);
			} 
			catch (ParseException e) {
				m_parsedOk = false;
			}		
		}
		
		LOG.debug("{ " + StringUtility.join(",", json.keySet()) + " }");
		
		// gateway message
		if(json.has(EUI)) {
			m_gatewayEui = json.getString(EUI);
			m_eui = m_gatewayEui;
		}
		// node message
		else if(json.has(NODE_EUI_1) || json.has(NODE_EUI_2)) {
			m_type = TYPE_NODE;
			m_data = json.getString(DATA);
			
			if(json.has(NODE_EUI_1)) {
				m_nodeEui = json.getString(NODE_EUI_1);
				m_gatewayEui = json.getString(GATEWAY_EUI_1);				
			}
			else {
				m_nodeEui = json.getString(NODE_EUI_2);
				m_gatewayEui = json.getString(GATEWAY_EUI_2);				
			}
			
			m_eui = m_nodeEui;
		}
		else {
			// should not reach this place
			System.err.println("WARNING: mqtt message does not meet expectation!");
		}
		
		// location data
		if(json.has(LATITUDE)) {
			m_latitude = json.getDouble(LATITUDE);
		}
		
		if(json.has(LONGITUDE)) {
			m_longitude = json.getDouble(LONGITUDE);
		}
	}
	
	public boolean parsedOk() {
		return m_parsedOk;
	}
	
	public Date getTimestamp() {
		return m_timestamp;
	}
	
	public String getGatewayEui() {
		return m_gatewayEui;
	}
	
	public String getNodeEui() {
		return m_nodeEui;
	}
	
	public String getEui() {
		return m_eui;
	}
	
	public boolean isNodeMessage() {
		return m_type == TYPE_NODE;
	}
	
	public Double getLatitude() {
		return m_latitude;
	}
	
	public Double getLongitude() {
		return m_longitude;
	}
	
	public String getData() {
		return m_data;
	}

	/**
	 * @return base 64 decoded data. Null if data is null.
	 */
	public String getPlainData() {
		if(m_data == null) {
			return null;
		}
		
		byte [] data = Base64.getDecoder().decode(m_data);
		StringBuffer plain = new StringBuffer();

		for(int i = 0; i < data.length; i++) {
			plain.append((char)data[i]);
		}

		return plain.toString();
	}	
	
	public String getCompleteMessage() {
		return m_message;
	}
	
	public void setSource(int source) {
		m_source = source;
	}
	
	public int getSource() {
		return m_source;
	}
	
	public String toString() {
		if(isNodeMessage()) {
			return "    " + m_timestamp + " data " + getNodeEui() + " message " + getData(); 
		}
		else {
			return "    " + m_timestamp + " status"; 			
		}
	}
	
	public boolean isNoiseMessage() {
		return m_noiseMessage;
	}

	public void setNoiseMessage(boolean noiseMessage) {
		m_noiseMessage = noiseMessage;
	}
	
	/**
	 * @return max noise value for this message. in case this is not a noise message 0 is returned.
	 */
	public int getMaxNoise() {
		if(!isNoiseMessage()) {
			return 0;
		}
		
		String data = getPlainData();
		String text = data.substring(0, 4);
		int value = Integer.parseInt(text, 16);
		
		return value;
	}
	
	/**
	 * @return accumulated noise value for this message. in case this is not a noise message 0 is returned.
	 */
	public int getAccNoise() {
		if(!isNoiseMessage()) {
			return 0;
		}
		
		String data = getPlainData();
		String text = data.substring(4, 8);
		int value = Integer.parseInt(text, 16);
		
		return value;
	}
	
	/**
	 * @return sample count for this noise message. in case this is not a noise message 0 is returned.
	 */
	public int getCntNoise() {
		if(!isNoiseMessage()) {
			return 0;
		}
		
		String data = getPlainData();
		String text = data.substring(8, 12);
		int value = Integer.parseInt(text, 16);
		
		return value;
	}
}
