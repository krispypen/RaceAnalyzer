package be.krispypen.RaceAnalyzer.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import be.krispypen.RaceAnalyzer.model.Track;
import be.krispypen.RaceAnalyzer.model.Trackpoint;

/**
 * Parse a gpx file to a List of Tracks
 */
public class GpxReader extends DefaultHandler {

	private static final DateFormat TIME_FORMAT = buildTimeFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final DateFormat TIME_FORMAT_MILLISECONDS = buildTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private static SimpleDateFormat buildTimeFormat(String str) {
		SimpleDateFormat format = new SimpleDateFormat(str);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format;
	}

	private List<Track> tracks = new LinkedList<Track>();
	private Map<String, Track> tracksByName = new LinkedHashMap<String, Track>();
	private StringBuffer buf = new StringBuffer();
	private double lat;
	private double lon;
	private double ele;
	private double speed;
	private int msec;
	private long time;
	private String name = "noname";

	public static List<Track> readTrack(InputStream in) throws IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			SAXParser parser = factory.newSAXParser();
			GpxReader reader = new GpxReader();
			parser.parse(in, reader);
			return reader.getTracks();
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}
	}

	public static List<Track> readTrack(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			return readTrack(in);
		} finally {
			in.close();
		}
	}

	public static List<Track> readTracks(File[] files) throws IOException {
		List<Track> result = new LinkedList<Track>();
		for (File file : files) {
			InputStream in = new FileInputStream(file);
			try {
				result.addAll(readTrack(in));
			} finally {
				in.close();
			}
		}
		return result;

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		buf.setLength(0);
		if (qName.equals("trkpt")) {
			lat = Double.parseDouble(attributes.getValue("lat"));
			lon = Double.parseDouble(attributes.getValue("lon"));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("trkpt")) {
			Track track = tracksByName.get(name);
			if (track == null) {
				track = new Track(name);
				tracks.add(track);
				tracksByName.put(name, track);
			}
			track.addPoint(new Trackpoint(track, lat, lon, ele, time + msec, speed));
		} else if (qName.equals("ele")) {
			ele = Double.parseDouble(buf.toString());
		} else if (qName.equals("time")) {
			try {
				time = TIME_FORMAT_MILLISECONDS.parse(buf.toString()).getTime();
			} catch (ParseException e) {
				try {
					time = TIME_FORMAT.parse(buf.toString()).getTime();
				} catch (ParseException e2) {
					// do nothing
					// throw new SAXException("Invalid time " + buf.toString());
				}
			}
		} else if (qName.equals("mtk:msec")) {
			time = time - (time % 1000);
			msec = Integer.parseInt(buf.toString());
		} else if (qName.equals("mtk:speed")) {
			speed = Double.parseDouble(buf.toString());
		} else if (qName.equals("name")) {
			name = buf.toString();
		}
	}

	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		buf.append(chars, start, length);
	}

	public List<Track> getTracks() {
		return this.tracks;
	}

}
