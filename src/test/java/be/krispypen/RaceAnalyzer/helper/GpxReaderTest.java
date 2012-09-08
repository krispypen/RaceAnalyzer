package be.krispypen.RaceAnalyzer.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import be.krispypen.RaceAnalyzer.model.Track;

/**
 * Unit test for {@link GpxReader}
 */
public class GpxReaderTest extends TestCase {
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public GpxReaderTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(GpxReaderTest.class);
	}

	public void testReadTracks() {
		try {
			File testgpxfile = new File("src/test/Resources/test.gpx");
			List<Track> tracks = GpxReader.readTrack(testgpxfile);
			assertEquals(3, tracks.size());
			assertEquals(202, tracks.get(0).getPoints().size());
			assertEquals(172, tracks.get(1).getPoints().size());
			assertEquals(2, tracks.get(2).getPoints().size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
