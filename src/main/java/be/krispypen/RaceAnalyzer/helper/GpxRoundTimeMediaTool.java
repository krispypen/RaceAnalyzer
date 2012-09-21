package be.krispypen.RaceAnalyzer.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import be.krispypen.RaceAnalyzer.model.MeanRound;
import be.krispypen.RaceAnalyzer.model.Point;
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
	private MeanRound meanRound;
	private double minLat = Integer.MAX_VALUE;
	private double maxLat = 0;
	private double minLon = Integer.MAX_VALUE;
	private double maxLon = 0;

	private static final int DASHBOARD_Y_POSITION = 750;
	private static final int DASHBOARD_X_POSITION = 20;
	private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("mm:ss.SSS");
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("H:mm");
	private static final String BASIC_FONT = "Helvetica";

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
			this.meanRound = new MeanRound();
			for (Round round : rounds) {
				meanRound.add(round);
			}
			roundsPolygon = new Polygon();
			/*
			 * for (Round round : rounds) { for (Trackpoint tp : round.getPoints()) { double y = TRACK_SIZE - ((tp.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE; double x = ((tp.getLon() -
			 * minLon) / (maxLon - minLon)) * TRACK_SIZE; y = y + TRACK_Y_POSITION; x = x + TRACK_X_POSITION; roundsPolygon.addPoint((int) x, (int) y); } }
			 */
			for (Point point : meanRound.getPoints()) {
				double y = TRACK_SIZE - ((point.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
				double x = ((point.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
				y = y + DASHBOARD_Y_POSITION;
				x = x + DASHBOARD_X_POSITION;
				roundsPolygon.addPoint((int) x, (int) y);
			}
		} else {
			for (Track track : tracks) {
				for (Trackpoint tp : track.getPoints()) {
					double y = TRACK_SIZE - ((tp.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
					double x = ((tp.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
					y = y + DASHBOARD_Y_POSITION;
					x = x + DASHBOARD_X_POSITION;
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
		for (Track track : tracks) {
			for (Trackpoint tp : track.getPoints()) {
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
			renderGForce(g, trackPoint);
			if (currentRound != null) {
				long roundTime = trackPoint.getTime() - currentRound.getStarttime().getTime();
				if (roundTime < 5000) {
					renderRoundList(g, rounds, currentRound);
				}
			} else {
				Round lastRound = rounds.get(rounds.size() - 1);
				long i = trackPoint.getTime() - lastRound.getEndtime().getTime();
				if (i > 0 && i < 5000) {
					renderRoundList(g, rounds, null);
				}
			}
		}

		super.onVideoPicture(event);
	}

	private void renderBackground(Graphics2D g) {
		g.setColor(new Color(0f, 0f, 0f, .3f));
		g.fillRect(DASHBOARD_X_POSITION - 10, DASHBOARD_Y_POSITION - 10, 900, 330);
	}

	private void renderLapInfo(Graphics2D g, Round currentRound, Trackpoint trackPoint) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(BASIC_FONT, Font.PLAIN, 30));
		{ // speed
			int speedround = (int) (trackPoint.getSpeed());
			g.drawString("SPEED", DASHBOARD_X_POSITION + 600, DASHBOARD_Y_POSITION + 30);
			String speed = String.valueOf(speedround) + " km/h";
			g.drawString(speed, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(speed), DASHBOARD_Y_POSITION + 30);
		}
		{ // lap
			g.drawString("LAP", DASHBOARD_X_POSITION + 600, DASHBOARD_Y_POSITION + 70);
			String lap, time;
			if (currentRound != null) {
				lap = currentRound.getNumber() + "/" + currentRound.getTrack().getRounds().size();
				Date timestamp = new Date();
				timestamp.setTime(trackPoint.getTime() - currentRound.getStarttime().getTime());
				time = DURATION_FORMAT.format(timestamp);
				// skip millisecond precision
				time = time.substring(0, time.length() - 2);
			} else {
				lap = "-";
				time = "--:--:-";
			}
			g.drawString(lap, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(lap), DASHBOARD_Y_POSITION + 70);
			g.drawString(time, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(time), DASHBOARD_Y_POSITION + 110);
		}
		{ // fastest lap
			g.drawString("FASTEST", DASHBOARD_X_POSITION + 600, DASHBOARD_Y_POSITION + 160);
			Round fastestRound = trackPoint.getTrack().getFastestRound();
			String lap = String.valueOf(fastestRound.getNumber());
			g.drawString(lap, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(lap), DASHBOARD_Y_POSITION + 160);
			Date timestamp = new Date();
			timestamp.setTime(fastestRound.getDuration());
			String time = DURATION_FORMAT.format(timestamp);
			// skip millisecond precision
			time = time.substring(0, time.length() - 2);
			g.drawString(time, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(time), DASHBOARD_Y_POSITION + 210);
		}
		{ // diff lap
			g.drawString("DIFF", DASHBOARD_X_POSITION + 600, DASHBOARD_Y_POSITION + 260);
			String diff = "-"; // TODO
			g.drawString(diff, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(diff), DASHBOARD_Y_POSITION + 260);
		}
		{ // hour
			g.drawString("TIME", DASHBOARD_X_POSITION + 600, DASHBOARD_Y_POSITION + 310);
			Date date = new Date();
			date.setTime(trackPoint.getTime());
			String diff = TIME_FORMAT.format(date);
			g.drawString(diff, DASHBOARD_X_POSITION + 870 - g.getFontMetrics().stringWidth(diff), DASHBOARD_Y_POSITION + 310);
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
		int y = (int) (TRACK_SIZE - ((trackPoint.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE);
		int x = (int) (((trackPoint.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE);
		g.fillOval(DASHBOARD_X_POSITION + x - 5, DASHBOARD_Y_POSITION + y - 5, 10, 10);
	}

	private void renderGForce(Graphics2D g, Trackpoint trackPoint) {
		Point2D.Double gforce = GForceUtil.calculateGForce(trackPoint);
		g.setColor(Color.WHITE);
		int size = 230;
		g.drawOval(DASHBOARD_X_POSITION + 320, DASHBOARD_Y_POSITION + 50, size, size);
		g.drawOval(DASHBOARD_X_POSITION + 320 + size / 4, DASHBOARD_Y_POSITION + 50 + size / 4, size / 2, size / 2);
		g.drawLine(DASHBOARD_X_POSITION + 320 + size / 2, DASHBOARD_Y_POSITION + 50, DASHBOARD_X_POSITION + 320 + size / 2, DASHBOARD_Y_POSITION + 50 + size);
		g.drawLine(DASHBOARD_X_POSITION + 320, DASHBOARD_Y_POSITION + 50 + size / 2, DASHBOARD_X_POSITION + 320 + size, DASHBOARD_Y_POSITION + 50 + size / 2);
		Font font = new Font(BASIC_FONT, Font.PLAIN, 14);
		g.setFont(font);
		g.drawString("0.5", DASHBOARD_X_POSITION + 320 + size / 4 * 3 + 7, DASHBOARD_Y_POSITION + 50 + size / 2 - 7);
		g.drawString("1", DASHBOARD_X_POSITION + 320 + size + 7, DASHBOARD_Y_POSITION + 50 + size / 2 - 7);
		// Font font = new Font(BASIC_FONT, Font.PLAIN, 20);
		// g.setFont(font);
		// String gforcestr = (((double) (int) (Math.abs(gforce.getX()) * 100)) / 100) + "-" + (((double) (int) (Math.abs(gforce.getY()) * 100)) / 100) + "G";
		// g.drawString(gforcestr, 590 - g.getFontMetrics().stringWidth(gforcestr), 1050);

		int x = (int) (DASHBOARD_X_POSITION + 320 + size / 2 + (gforce.getX() * size / 2));
		int y = (int) (DASHBOARD_Y_POSITION + 50 + size / 2 + (gforce.getY() * size / 2));
		g.setColor(Color.RED);
		g.fillOval((int) x - 5, (int) y - 5, 10, 10);
	}

	private void renderRoundList(Graphics2D g, List<Round> rounds, Round currentRound) {
		{ // background
			g.setColor(new Color(0f, 0f, 0f, .3f));
			g.fillRect(1650, 20, 250, rounds.size() * 30 + 60);
		}
		g.setColor(Color.WHITE);
		g.setFont(new Font(BASIC_FONT, Font.BOLD, 24));
		String title = "Laps";
		g.drawString(title, 1650 + 250 / 2 - g.getFontMetrics().stringWidth(title) / 2, 50);
		g.setFont(new Font(BASIC_FONT, Font.PLAIN, 24));
		for (int i = 0; i < rounds.size(); i++) {
			Round round = rounds.get(i);
			if (round.equals(currentRound)) {
				g.setColor(Color.RED);
			} else {
				g.setColor(Color.WHITE);
			}
			g.drawString(String.valueOf(round.getNumber()), 1670, 90 + i * 30);
			Date timestamp = new Date();
			timestamp.setTime(round.getDuration());
			String time = DURATION_FORMAT.format(timestamp);
			// skip millisecond precision
			time = time.substring(0, time.length() - 2);
			g.drawString(time, 1880 - g.getFontMetrics().stringWidth(time), 90 + i * 30);
		}
	}

}