package org.thethingsnetwork.zrh.monitor.model;

public class Location {
	public static double MAX_DISTANCE = 10000.0;
	
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
    
    public double distance(Location location) {
    	if(location == null) {
    		return MAX_DISTANCE;
    	}
    	
    	double dlat = (getLatitude() - location.getLatitude());
    	double dlng = (getLongitude() - location.getLongitude());
    	
    	return Math.sqrt(dlat*dlat - dlng*dlng);
    }
    
    public String toString() {
    	return "N: " + getLatitude() + " E: " + getLongitude();
    }
}
