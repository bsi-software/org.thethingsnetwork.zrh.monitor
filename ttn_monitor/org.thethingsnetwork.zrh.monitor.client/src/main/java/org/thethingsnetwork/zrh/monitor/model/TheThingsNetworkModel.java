package org.thethingsnetwork.zrh.monitor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.GatewayFavoritesProperty;
import org.thethingsnetwork.zrh.monitor.client.ConfigProperties.NodeFavoritesProperty;

// TODO initially populate gateways using http://ttnstatus.org/gateways
public class TheThingsNetworkModel {
	public static int MESSAGE_QUEUE_SIZE = 100;

	private Map<String, Gateway> m_gateway = null;
	private Map<String, Node> m_node = null;

	private List<String> m_gatewayFavorite = null;
	private List<String> m_nodeFavorite = null;

	public TheThingsNetworkModel() {
		m_gatewayFavorite = getStringList(CONFIG.getPropertyValue(GatewayFavoritesProperty.class));
		m_nodeFavorite = getStringList(CONFIG.getPropertyValue(NodeFavoritesProperty.class));
		reset();
	}

	public void addMessage(String message) {
		Message m = new Message(message);

		if(m.parsedOk()) {
			if(m.isNodeMessage()) {
				updateNodes(m);
			}

			updateGateways(m);
		}
	}

	private void updateGateways(Message message) {
		String eui = message.getGatewayEui();
		Gateway g = m_gateway.get(eui);

		// add new gateway if this is a gateway message
		if(g == null && !message.isNodeMessage()) {
			g = new Gateway(message, MESSAGE_QUEUE_SIZE);
			m_gateway.put(eui, g);
		}

		if(g != null) {
			g.addMessage(message);
		}
	}

	private void updateNodes(Message message) {
		String eui = message.getNodeEui();
		Node n = m_node.get(eui);

		// add new node
		if(n == null) {
			n = new Node(message, MESSAGE_QUEUE_SIZE);
			m_node.put(eui, n);
		}

		if(n != null) {
			n.addMessage(message);
		}
	}

	/**
	 * @return a copy of the set of the currently known gateway eui. copy is needed to avoid concurrent update issues 
	 */
	public Set<String> getGatewayEuis() {
		return m_gateway.keySet();
	}

	public Gateway getGateway(String eui) {
		Gateway gateway = m_gateway.get(eui);
		return gateway != null ? gateway : new Gateway(null, 1);
	}

	public Gateway getGateway(Location location) {
		Gateway nearestGateway = null;
		double minDistance = Location.MAX_DISTANCE;

		for(String eui: getGatewayEuis()) {
			Gateway gateway = getGateway(eui);
			double distance = location.distance(gateway.getLocation());

			if(distance < minDistance) {
				nearestGateway = getGateway(eui);
				minDistance = distance;
			}
		}

		return nearestGateway;
	}

	public Set<String> getNodeEuis() {
		return m_node.keySet();
	}

	public Node getNode(String eui) {
		Node node = m_node.get(eui);
		return node != null ? node : new Node(null, 1);
	}

	public List<Message> getMessages() {
		List<Message> messages = new ArrayList<>();

		for(String gatewayEui: getGatewayEuis()) {
			messages.addAll(getGateway(gatewayEui).getMessages());
		}

		return messages;
	}
	
	public void addToFavorites(String eui, boolean isNode) {
		if(isNode) {
			m_nodeFavorite.add(eui);
		}
		else {
			m_gatewayFavorite.add(eui);
		}
	}

	public void removeFromFavorites(String eui, boolean isNode) {
		if(isNode) {
			m_nodeFavorite.remove(eui);
		}
		else {
			m_gatewayFavorite.remove(eui);
		}
	}

	public boolean hasFavorite(String eui, boolean isNode) {
		if(isNode) {
			return m_nodeFavorite.contains(eui);
		}
		else {
			return m_gatewayFavorite.contains(eui);
		}
	}

	public List<String> getFavorites(boolean nodes) {
		if(nodes) {
			return m_nodeFavorite;
		}
		else {
			return m_gatewayFavorite;
		}
	}

	public void reset() {
		m_gateway = new ConcurrentHashMap<String, Gateway>();
		m_node = new ConcurrentHashMap<String, Node>();
	}


	private List<String> getStringList(String csv) {
		if(StringUtility.hasText(csv) && !ConfigProperties.FAVORITE_GATEWAYS_DEFAULT.equals(csv)) {
			return new LinkedList<String>(Arrays.asList(csv.split(",")));
		}
		else {
			return new ArrayList<String>();
		}
	}

}
