package org.thethingsnetwork.zrh.monitor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;

public class Node {
	private String m_eui = "<default-node-eui>";
	private Queue<Message> m_messages = null; 
	
	public Node(String messageData, int messageQueueSize) {
		JSONObject json = new JSONObject(messageData);
		String eui = json.getString("nodeEui");
		
		if(StringUtility.hasText(eui)) {
			m_eui = eui;
		}
		
		m_messages = new ArrayBlockingQueue<Message>(messageQueueSize);
	}
	
	public int messages() {
		return m_messages.size();
	}
	
	public List<Message> getMessages() {
		List<Message> list = new ArrayList<Message>();
		list.addAll(m_messages);
		return list;
	}
	
	public void addMessage(Message m) {
		m_messages.add(m);
	}

	public String getEui() {
		return m_eui;
	}
}
