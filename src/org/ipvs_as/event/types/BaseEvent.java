package org.ipvs_as.event.types;

public class BaseEvent {

    private String sensorID;

    private String timestamp;

    public String getSensorID() {
	return sensorID;
    }

    public void setSensorID(String sensorID) {
	this.sensorID = sensorID;
    }

    public String getTimestamp() {
	return timestamp;
    }

    public void setTimestamp(String timestamp) {
	this.timestamp = timestamp;
    }
}
