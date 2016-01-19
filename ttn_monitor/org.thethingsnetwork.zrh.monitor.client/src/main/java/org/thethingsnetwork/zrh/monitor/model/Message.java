package org.thethingsnetwork.zrh.monitor.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;

public class Message {
	public static final String FORMAT_TIMESTAMP = "yyyy-MM-ddHH:mm:ss.SSS";
	
	private Date m_timestamp = null;
	private String m_gatewayEui = null;
	private String m_nodeEui = null;
	private String m_message = null;
	private String m_data = null;
	private boolean m_nodeMessage = false;
	private boolean m_parsedOk = true;

	public Message(String messageData) {
		m_message = messageData;
		
		JSONObject json = new JSONObject(messageData);
		String time = json.getString("time");
		
		// get timestamp
		if(StringUtility.hasText(time)) {
			// 2016-01-15T13:07:56.008
			// yyyy-MM-ddTHH:mm:ss.SSS
			DateFormat format = new SimpleDateFormat(FORMAT_TIMESTAMP);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			try {
				String d = time.substring(0, 10);
				String t = time.substring(11, 22);
				m_timestamp = format.parse(d + t);
			} 
			catch (ParseException e) {
				m_parsedOk = false;
			}		
		}
		
		// gateway message
		if(json.has("eui")) {
			m_gatewayEui = json.getString("eui");
		}
		// node message
		else {
			m_nodeMessage = true;
			m_nodeEui = json.getString("nodeEui");
			m_gatewayEui = json.getString("gatewayEui");
			m_data = json.getString("data");
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
	
	public boolean isNodeMessage() {
		return m_nodeMessage;
	}
	
	public String getNodeEui() {
		return m_nodeEui;
	}
	
	public String getData() {
		return m_data;
	}
	
	public String getCompleteMessage() {
		return m_message;
	}
	
	public String toString() {
		if(isNodeMessage()) {
			return "    " + m_timestamp + " data " + getNodeEui() + " message " + getData(); 
		}
		else {
			return "    " + m_timestamp + " status"; 			
		}
	}
}
