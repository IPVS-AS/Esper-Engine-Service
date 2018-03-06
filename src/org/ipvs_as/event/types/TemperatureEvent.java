package org.ipvs_as.event.types;

public class TemperatureEvent extends BaseEvent {

    public TemperatureEvent() {
        super();
    }

    private double temperature;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "TemperatureEvent [temperature=" + temperature + ", sensorID=" + getSensorID() + ", timestamp="
                + getTimestamp() + "]";
    }

}
