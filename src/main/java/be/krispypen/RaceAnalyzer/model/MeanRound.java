package be.krispypen.RaceAnalyzer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MeanRound {

	private final Map<Trackpoint, Point> pointsmap = new HashMap<Trackpoint, Point>();
	private final LinkedList<Point> points = new LinkedList<Point>();

	public void add(Round round) {
		if (pointsmap.isEmpty()) {
			for (Trackpoint tp : round.getPoints()) {
				Point point = new Point(tp.getLat(), tp.getLon());
				points.add(point);
				pointsmap.put(tp, point);
			}
		}
	}

	public Collection<Point> getPoints() {
		return points;
	}
}
