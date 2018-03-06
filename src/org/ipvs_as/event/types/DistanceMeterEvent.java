package org.ipvs_as.event.types;

public class DistanceMeterEvent extends BaseEvent {

    private double distance;

    public double getDistance() {
	return distance;
    }

    public void setDistance(double distance) {
	this.distance = distance;
    }

    @Override
    public String toString() {
	return "DistanceMeterEvent [distance=" + distance + ", sensorID="
		+ getSensorID() + ", timestamp=" + getTimestamp() + "]";
    }

}
