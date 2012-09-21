package be.krispypen.RaceAnalyzer.model;

/**
 * A Trackpoint is a point with speed, time and elevation information
 */
public class Trackpoint extends Point {

	private Track track;
	private double ele;
	private long time;
	private double speed;
	private Round round;
	private Trackpoint previouspoint;

	public Trackpoint(Track track, double lat, double lon, double ele, long time, double speed, Trackpoint previouspoint) {
		super(lat, lon);
		this.track = track;
		this.ele = ele;
		this.time = time;
		this.speed = speed;
		this.previouspoint = previouspoint;
	}

	public Track getTrack() {
		return this.track;
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

	public void setRound(Round round) {
		this.round = round;
	}

	public Round getRound() {
		return this.round;
	}

	public Trackpoint getPrevious() {
		return this.previouspoint;
	}

}
