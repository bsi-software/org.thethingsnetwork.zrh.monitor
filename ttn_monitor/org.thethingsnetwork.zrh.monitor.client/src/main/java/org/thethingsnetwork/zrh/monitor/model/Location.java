package org.thethingsnetwork.zrh.monitor.model;

public class Location {
	private double m_latitude = 0.0;
    private double m_longitude = 0.0;
    
    public Location(double latitude, double longitude) {
    	m_latitude = latitude;
    	m_longitude = longitude;
    }
    
    public double getLatitude() {
    	return m_latitude;
    }
    
    public double getLongitude() {
    	return m_longitude;
    }
    
    public String toString() {
    	return "lat: " + getLatitude() + " lng: " + getLongitude();
    }
}
