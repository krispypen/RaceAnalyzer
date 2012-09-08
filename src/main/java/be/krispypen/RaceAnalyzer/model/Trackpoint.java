package be.krispypen.RaceAnalyzer.model;

/**
 * A Trackpoint is a point with speed, time and elevation information
 */
public class Trackpoint extends Point {

	private double ele;
	private long time;
	private double speed;

	public Trackpoint(double lat, double lon, double ele, long time, double speed) {
		super(lat, lon);
		this.ele = ele;
		this.time = time;
		this.speed = speed;
	}

	public double getEle() {
		return ele;
	}

	public void setEle(double ele) {
		this.ele = ele;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
