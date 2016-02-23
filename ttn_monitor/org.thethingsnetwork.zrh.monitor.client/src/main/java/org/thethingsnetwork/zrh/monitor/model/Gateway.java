package org.thethingsnetwork.zrh.monitor.model;

public class Gateway extends Device {

	public Gateway(String eui, int messageQueueSize) {
		super(eui, messageQueueSize);
	}

	public Gateway(Message message, int messageQueueSize) {
		super(message, messageQueueSize);

		if(message != null) {
			Double latitude = message.getLatitude();
			Double longitude = message.getLongitude();

			if(latitude != null && longitude != null) {
				setLocation(new Location(latitude, longitude));
			}
		}
	}
}
