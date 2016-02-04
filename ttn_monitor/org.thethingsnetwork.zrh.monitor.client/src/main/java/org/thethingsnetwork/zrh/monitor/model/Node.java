package org.thethingsnetwork.zrh.monitor.model;

public class Node extends Device {
	
	private boolean m_noiseNode;
	
	public Node(Message message, int messageQueueSize) {
		super(message, messageQueueSize);
	}

	public boolean isNoiseNode() {
		return m_noiseNode;
	}
	
	public void setNoiseNode(boolean isNoiseNode) {
		m_noiseNode = isNoiseNode;
	}

	@Override
	public void addMessage(Message m) {
		m.setNoiseMessage(isNoiseNode());
		super.addMessage(m);
	}
}
