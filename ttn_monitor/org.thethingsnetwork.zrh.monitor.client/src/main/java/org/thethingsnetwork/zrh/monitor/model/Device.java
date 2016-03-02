package org.thethingsnetwork.zrh.monitor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Device {

	private String m_eui = "<default-eui>";
	private String m_name = null;
	private Location m_location = null;
	private int m_messageQueueSize = 1;

	private ArrayBlockingQueue<Message> m_messages = null;

	public Device(String eui, int messageQueueSize) {
		m_eui = eui;
		m_messageQueueSize = messageQueueSize;
		reset();
	}
	
	public Device(Message message, int messageQueueSize) {
		if(message != null) {
			m_eui = message.getEui();
		}
		
		m_messages = new ArrayBlockingQueue<Message>(messageQueueSize);
	}

	public String getEui() {
		return m_eui;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public boolean hasLocation() {
		return m_location != null;
	}

	public void setLocation(Location location) {
		m_location = location;
	}

	public Location getLocation() {
		return m_location;
	}

	public void addMessage(Message m) {
		synchronized(m_messages) {
			if(m_messages.remainingCapacity() == 0) {
				m_messages.remove();
			}
			
			m_messages.add(m);
		}
	}

	public int messages() {
		return m_messages.size();
	}

	public List<Message> getMessages() {
		List<Message> list = new ArrayList<Message>();
		list.addAll(m_messages);
		return list;
	}
	
	/**
	 * drops all messages in the message queue.
	 * @return 
	 */
	public void reset() {
		m_messages = new ArrayBlockingQueue<Message>(m_messageQueueSize);
	}

	public String toString() {
		StringBuffer s = new StringBuffer(getEui());
		s.append(" ");

		if(hasLocation()) {
			s.append(getLocation()).append(" ");
		}

		s.append("(").append(messages()).append(")");
		return s.toString();
	}

}
