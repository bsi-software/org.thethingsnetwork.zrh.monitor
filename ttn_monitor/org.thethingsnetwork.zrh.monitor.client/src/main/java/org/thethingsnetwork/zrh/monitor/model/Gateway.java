package org.thethingsnetwork.zrh.monitor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;

public class Gateway {
	
	private String m_eui = "<default-gateway-eui>";
	private Location m_location = null;
	private Queue<Message> m_messages = null; 
	
	public Gateway(String messageData, int messageQueueSize) {
		JSONObject json = new JSONObject(messageData);
		String eui = json.getString("eui");
		Double latitude = json.getDouble("latitude");
		Double longitude = json.getDouble("longitude");
		
		if(StringUtility.hasText(eui)) {
			m_eui = eui;
		}
		
		if(latitude != null && longitude != null) {
			m_location = new Location(latitude, longitude);
		}
		
		m_messages = new ArrayBlockingQueue<Message>(messageQueueSize);
	}
	
	public void addMessage(Message m) {
		m_messages.add(m);
	}
	
	public int messages() {
		return m_messages.size();
	}
	
	public List<Message> getMessages() {
		List<Message> list = new ArrayList<Message>();
		list.addAll(m_messages);
		return list;
	}
	
	public String getEui() {
		return m_eui;
	}
	
	public Location getLocation() {
		return m_location;
	}
	
	public String toString() {
		return getEui() + " " + getLocation() + " (" + messages() + ")";
	}
}
