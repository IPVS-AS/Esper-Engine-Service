package org.ipvs_as.event.types;

public class SoundStrength extends BaseEvent {

    private int soundStrength;

    public int getSoundStrength() {
	return soundStrength;
    }

    public void setSoundStrength(int soundStrength) {
	this.soundStrength = soundStrength;
    }

    @Override
    public String toString() {
	return "SoundStrength [soundStrength=" + soundStrength + ", sensorID="
		+ getSensorID() + ", timestamp=" + getTimestamp() + "]";
    }

}
