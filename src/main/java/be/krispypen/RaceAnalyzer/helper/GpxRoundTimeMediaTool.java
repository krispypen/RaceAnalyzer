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
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("H:mm");

	public static final int TRACK_SIZE = 300;

	public GpxRoundTimeMediaTool(List<Track> tracks, List<Round> rounds, long videostarttime) {
		this.videostarttime = videostarttime;
		this.tracks = tracks;
		this.rounds = rounds;
		trackPolygon = new Polygon();
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
		if (rounds != null && !rounds.isEmpty()) {
			roundsPolygon = new Polygon();
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
		if (rounds != null) {
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
			renderBackground(g);
			renderTrack(g, trackPoint);
			renderLapInfo(g, currentRound, trackPoint);
			if (trackPointSecAgo != null) {
				renderGForce(g, trackPointSecAgo, trackPoint);
			}
		}

		super.onVideoPicture(event);
	}

	private void renderBackground(Graphics2D g) {
		g.setColor(new Color(0f, 0f, 0f, .3f));
		g.fillRect(TRACK_X_POSITION - 10, TRACK_Y_POSITION - 10, 920, 330);
	}

	private void renderLapInfo(Graphics2D g, Round currentRound, Trackpoint trackPoint) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Serif", Font.PLAIN, 30));
		{ // speed
			int speedround = (int) (trackPoint.getSpeed());
			g.drawString("SPEED", 650, 780);
			String speed = String.valueOf(speedround);
			g.drawString(speed, 920 - g.getFontMetrics().stringWidth(speed), 780);
		}
		{ // lap
			g.drawString("LAP", 650, 820);
			String lap, time;
			if (currentRound != null) {
				lap = currentRound.getNumber() + "/" + currentRound.getTrack().getRounds().size();
				Date timestamp = new Date();
				timestamp.setTime(trackPoint.getTime() - currentRound.getStarttime().getTime());
				time = DURATION_FORMAT.format(timestamp);
			} else {
				lap = "-";
				time = "--:--:-";
			}
			g.drawString(lap, 920 - g.getFontMetrics().stringWidth(lap), 820);
			g.drawString(time, 920 - g.getFontMetrics().stringWidth(time), 860);
		}
		{ // fastest lap
			g.drawString("FASTEST", 650, 910);
			Round fastestRound = trackPoint.getTrack().getFastestRound();
			String lap = String.valueOf(fastestRound.getNumber());
			g.drawString(lap, 920 - g.getFontMetrics().stringWidth(lap), 910);
			Date timestamp = new Date();
			timestamp.setTime(fastestRound.getDuration());
			String time = DURATION_FORMAT.format(timestamp);
			g.drawString(time, 920 - g.getFontMetrics().stringWidth(time), 960);
		}
		{ // diff lap
			g.drawString("DIFF", 650, 1010);
			String diff = "-"; // TODO
			g.drawString(diff, 920 - g.getFontMetrics().stringWidth(diff), 1010);
		}
		{ // hour
			g.drawString("TIME", 650, 1060);
			Date date = new Date();
			date.setTime(trackPoint.getTime());
			String diff = TIME_FORMAT.format(date);
			g.drawString(diff, 920 - g.getFontMetrics().stringWidth(diff), 1060);
		}
	}

	private void renderTrack(Graphics2D g, Trackpoint trackPoint) {
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
		int size = 230;
		g.drawOval(370, 800, size, size);
		g.drawOval(370 + size / 4, 800 + size / 4, size / 2, size / 2);
		g.drawLine(370 + size / 2, 800, 370 + size / 2, 800 + size);
		g.drawLine(370, 800 + size / 2, 370 + size, 800 + size / 2);
		Font font = new Font("Serif", Font.PLAIN, 20);
		g.setFont(font);
		double gforce = (trackPoint.getSpeed() - trackPointSecAgo.getSpeed()) * 1000 / 3600 / 9.8;
		String gforcestr = (((double) (int) (gforce * 100)) / 100) + "G";
		g.drawString(gforcestr, 590 - g.getFontMetrics().stringWidth(gforcestr), 1050);
		int x = 370 + size / 2;
		int y = (int) (800 + size / 2 + (gforce * size / 2));
		g.setColor(Color.RED);
		g.fillOval((int) x - 5, (int) y - 5, 10, 10);
	}

}