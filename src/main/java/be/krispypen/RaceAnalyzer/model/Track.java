package be.krispypen.RaceAnalyzer.model;

import java.util.LinkedList;
import java.util.List;

/**
 * A Track is a collection of trackpoints.
 */
public class Track {

	private String name;
	private List<Trackpoint> points = new LinkedList<Trackpoint>();
	private List<Round> rounds = new LinkedList<Round>();

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

	public void addRound(Round round) {
		this.rounds.add(round);
	}

	public List<Round> getRounds() {
		return this.rounds;
	}

	public Round getFastestRound() {
		Round result = null;
		for (Round round : rounds) {
			if (result == null || result.getDuration() > round.getDuration()) {
				result = round;
			}
		}
		return result;
	}

	public void clearRounds() {
		this.rounds.clear();
	}

}
