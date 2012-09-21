package be.krispypen.RaceAnalyzer.helper;

import junit.framework.TestCase;
import be.krispypen.RaceAnalyzer.model.Point;

public class GForceUtilTest extends TestCase {

	public void testTT() {
		Point point1 = new Point(5, 5);
		Point point2 = new Point(6, -2);
		Point point3 = new Point(2, -4);
		Point middle = GForceUtil.getMiddle(point1, point2, point3);
		assertEquals((double) 2, middle.getLat());
		assertEquals((double) 1, middle.getLon());
	}

	public void testDD() {
		Point point1 = new Point(1, 1);
		Point point2 = new Point(1.1, 3);
		Point point3 = new Point(3, 6);
		Point middle = GForceUtil.getMiddle(point1, point2, point3);
		System.out.println("middle:" + middle.getLat() + " - " + middle.getLon());
		double radius = GForceUtil.distFrom(middle.getLat(), middle.getLon(), point3.getLat(), point3.getLon());
		System.out.println("radius: " + radius);
		double asinFrom = (GForceUtil.distFrom(point2.getLat(), point2.getLon(), point3.getLat(), point3.getLon()) / 2) / radius;
		System.out.println("asinFrom: " + asinFrom);
		double fullcircle = 2 * Math.PI;
		double circlepart = Math.asin(asinFrom) * 2 / fullcircle;
		System.out.println("circlepart: " + circlepart);
	}
}
