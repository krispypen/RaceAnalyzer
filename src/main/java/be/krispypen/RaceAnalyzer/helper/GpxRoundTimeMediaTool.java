package be.krispypen.RaceAnalyzer.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import be.krispypen.RaceAnalyzer.model.Round;
import be.krispypen.RaceAnalyzer.model.Track;
import be.krispypen.RaceAnalyzer.model.Trackpoint;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class GpxRoundTimeMediaTool extends MediaToolAdapter {

	private List<Track> tracks;
	private List<Round> rounds;
	private long videostarttime;
	private Polygon trackPolygon;
	private Polygon roundsPolygon;
	private double minLat = Integer.MAX_VALUE;
	private double maxLat = 0;
	private double minLon = Integer.MAX_VALUE;
	private double maxLon = 0;

	static final int GFORCE_Y_POSITION = 930;
	static final int GFORCE_X_POSITION = 400;
	static final int ROUNDTIME_Y_POSITION = 830;
	static final int ROUNDTIME_X_POSITION = 400;
	static final int SPEED_Y_POSITION = 1030;
	static final int SPEED_X_POSITION = 400;
	static final int TRACK_Y_POSITION = 750;
	static final int TRACK_X_POSITION = 50;
	private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("mm:ss.S");

	public static final int TRACK_SIZE = 300;

	public GpxRoundTimeMediaTool(List<Track> tracks, List<Round> rounds, long videostarttime) {
		this.videostarttime = videostarttime;
		this.tracks = tracks;
		this.rounds = rounds;
		roundsPolygon = new Polygon();
		for (Track track : tracks) {
			for (Trackpoint tp : track.getPoints()) {
				if (tp.getLat() < minLat) {
					minLat = tp.getLat();
				}
				if (tp.getLat() > maxLat) {
					maxLat = tp.getLat();
				}
				if (tp.getLon() < minLon) {
					minLon = tp.getLon();
				}
				if (tp.getLon() > maxLon) {
					maxLon = tp.getLon();
				}
			}
		}
		if (rounds != null) {
			for (Round round : rounds) {
				for (Trackpoint tp : round.getPoints()) {
					double y = TRACK_SIZE - ((tp.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
					double x = ((tp.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
					y = y + TRACK_Y_POSITION;
					x = x + TRACK_X_POSITION;
					roundsPolygon.addPoint((int) x, (int) y);
				}
			}
		} else {
			for (Track track : tracks) {
				for (Trackpoint tp : track.getPoints()) {
					double y = TRACK_SIZE - ((tp.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
					double x = ((tp.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
					y = y + TRACK_Y_POSITION;
					x = x + TRACK_X_POSITION;
					trackPolygon.addPoint((int) x, (int) y);
				}
			}
		}
	}

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		double timestamp = ((double) event.getTimeStamp()) / 1000;
		Round currentRound = null;
		for (Round round : rounds) {
			if (round.getStarttime().getTime() - videostarttime < timestamp && round.getEndtime().getTime() - videostarttime > timestamp) {
				for (Trackpoint tp : round.getPoints()) {
					if (tp.getTime() - videostarttime > timestamp) {
						currentRound = round;
						break;
					}
				}
			}
		}
		Trackpoint trackPoint = null;
		Trackpoint trackPointSecAgo = null;
		for (Track track : tracks) {
			for (Trackpoint tp : track.getPoints()) {
				if (trackPointSecAgo == null && tp.getTime() - videostarttime + 1000 > timestamp) {
					trackPointSecAgo = tp;
				}
				if (tp.getTime() - videostarttime > timestamp) {
					trackPoint = tp;
					break;
				}
			}
		}
		if (trackPoint != null) {
			BufferedImage image = event.getImage();
			// get the graphics for the image
			Graphics2D g = image.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			renderTrack(g, trackPoint);
			if (trackPointSecAgo != null) {
				renderGForce(g, trackPointSecAgo, trackPoint);
			}
			renderSpeed(g, trackPoint.getSpeed());
			if (currentRound != null) {
				renderRoundTime(g, currentRound, trackPoint);
			}
		}

		super.onVideoPicture(event);
	}

	private void renderSpeed(Graphics2D g, double speed) {
		g.setColor(Color.WHITE);
		Font font = new Font("Serif", Font.PLAIN, 96);
		g.setFont(font);
		int speedround = (int) (speed);
		g.drawString(speedround + "km/h", SPEED_X_POSITION, SPEED_Y_POSITION);
	}

	private void renderTrack(Graphics2D g, Trackpoint trackPoint) {
		g.setColor(new Color(0f, 0f, 0f, .3f));
		g.fillRect(TRACK_X_POSITION - 10, TRACK_Y_POSITION - 10, TRACK_SIZE + 20, TRACK_SIZE + 20);
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(3));
		if (roundsPolygon != null) {
			g.drawPolygon(roundsPolygon);
		} else {
			g.drawPolygon(trackPolygon);
		}
		g.setColor(Color.RED);
		double y = TRACK_SIZE - ((trackPoint.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
		double x = ((trackPoint.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
		y = y + TRACK_Y_POSITION;
		x = x + TRACK_X_POSITION;
		g.fillOval((int) x - 5, (int) y - 5, 10, 10);
	}

	private void renderGForce(Graphics2D g, Trackpoint trackPointSecAgo, Trackpoint trackPoint) {
		g.setColor(Color.WHITE);
		Font font = new Font("Serif", Font.PLAIN, 60);
		g.setFont(font);
		double gforce = Math.abs(trackPointSecAgo.getSpeed() - trackPoint.getSpeed()) * 1000 / 3600 / 9.8;
		gforce = ((double) (int) (gforce * 100)) / 100;
		g.drawString(gforce + "G", GFORCE_X_POSITION, GFORCE_Y_POSITION);
	}

	private void renderRoundTime(Graphics2D g, Round round, Trackpoint trackPoint) {
		g.setColor(Color.WHITE);
		Font font = new Font("Serif", Font.PLAIN, 30);
		g.setFont(font);
		Date timestamp = new Date();
		timestamp.setTime(trackPoint.getTime() - round.getStarttime().getTime());
		g.drawString(DURATION_FORMAT.format(timestamp), ROUNDTIME_X_POSITION, ROUNDTIME_Y_POSITION);
	}

}