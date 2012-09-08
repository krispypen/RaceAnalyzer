package be.krispypen.RaceAnalyzer.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Round {

	private List<Trackpoint> points = new LinkedList<Trackpoint>();

	/**
	 * Return all points for this Round
	 * 
	 * @return
	 */
	public List<Trackpoint> getPoints() {
		return this.points;
	}

	/**
	 * Add a point to this Round
	 * 
	 * @param point
	 */
	public void addPoint(Trackpoint point) {
		this.points.add(point);
	}

	/**
	 * This calculates the duration of the Round by taking the first and the
	 * last trackpoint's times
	 * 
	 * @return
	 */
	public Long getDuration() {
		if (points.isEmpty()) {
			return null;
		}
		Trackpoint tp1 = points.get(0);
		Trackpoint tp2 = points.get(points.size() - 1);
		return tp2.getTime() - tp1.getTime();
	}

	/**
	 * This calculates the starttime by taking the first trackpoint's time.
	 * 
	 * @return the time of the first trackpoint
	 */
	public Date getStarttime() {
		if (points.isEmpty()) {
			return null;
		}
		Date result = new Date();
		result.setTime(points.get(0).getTime());
		return result;
	}

	/**
	 * This calculates the endtime by taking the last trackpoint's time.
	 * 
	 * @return the time of the last trackpoint
	 */
	public Date getEndtime() {
		if (points.isEmpty()) {
			return null;
		}
		Date result = new Date();
		result.setTime(points.get(points.size() - 1).getTime());
		return result;
	}
}
