package be.krispypen.RaceAnalyzer.model;

import java.util.LinkedList;
import java.util.List;

/**
 * A Track is a collection of trackpoints.
 */
public class Track {

	private String name;
	private List<Trackpoint> points = new LinkedList<Trackpoint>();

	/**
	 * Create a track for a given name
	 * 
	 * @param name
	 */
	public Track(String name) {
		this.name = name;
	}

	/**
	 * Add a point to this Track
	 * 
	 * @param point
	 */
	public void addPoint(Trackpoint point) {
		this.points.add(point);
	}

	public List<Trackpoint> getPoints() {
		return this.points;
	}

	@Override
	public String toString() {
		return this.name + " [" + this.points.size() + "]";
	}

}
