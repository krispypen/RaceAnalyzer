package be.krispypen.RaceAnalyzer.model;

/**
 * This is a definition of a finish line, a line between 2 fixed points
 */
public class Finishline {

	private Point point1;
	private Point point2;

	/**
	 * @param point1
	 * @param point2
	 */
	public Finishline(Point point1, Point point2) {
		this.point1 = point1;
		this.point2 = point2;
	}

	public Point getPoint1() {
		return this.point1;
	}

	public Point getPoint2() {
		return this.point2;
	}
}
