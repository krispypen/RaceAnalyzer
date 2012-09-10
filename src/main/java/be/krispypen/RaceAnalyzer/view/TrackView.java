package be.krispypen.RaceAnalyzer.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;

import be.krispypen.RaceAnalyzer.model.Finishline;
import be.krispypen.RaceAnalyzer.model.Point;
import be.krispypen.RaceAnalyzer.model.Round;
import be.krispypen.RaceAnalyzer.model.Track;
import be.krispypen.RaceAnalyzer.model.Trackpoint;

public class TrackView extends JPanel {

	private List<Track> tracks;
	private JLabel roundslabel;
	private Finishline finishline;
	private List<Round> rounds;
	private Polygon tracksShape;
	private Shape finishlineShape;
	private Polygon roundsShape;
	private double minLat = Integer.MAX_VALUE;
	private double maxLat = 0;
	private double minLon = Integer.MAX_VALUE;
	private double maxLon = 0;
	private static final int TRACK_Y_POSITION = 0;
	private static final int TRACK_X_POSITION = 0;
	private static final int TRACK_SIZE = 300;
	private static final SimpleDateFormat TIME_FORMAT = buildTimeFormat();
	private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("mm:ss.S");

	private static SimpleDateFormat buildTimeFormat() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		return format;
	}

	public TrackView(List<Track> tracks, JLabel roundslabel) {
		this.setTracks(tracks);
		this.roundslabel = roundslabel;
	}

	public List<Track> getTracks() {
		return this.tracks;
	}

	public List<Round> getRounds() {
		return this.rounds;
	}

	public void setTracks(List<Track> tracks) {
		minLat = Integer.MAX_VALUE;
		maxLat = 0;
		minLon = Integer.MAX_VALUE;
		maxLon = 0;
		this.tracks = tracks;
		tracksShape = new Polygon();
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
		int i = 0;
		for (Track track : tracks) {
			for (Trackpoint tp : track.getPoints()) {
				double y = TRACK_SIZE - ((tp.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
				double x = ((tp.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
				y = y + TRACK_Y_POSITION;
				x = x + TRACK_X_POSITION;
				if (i % 5 == 0) {
					tracksShape.addPoint((int) x, (int) y);
				}
				i++;
			}
		}
		this.setSize((int) tracksShape.getBounds().getWidth(), (int) tracksShape.getBounds().getHeight());
		this.recalculateRounds();
		this.repaint();
	}

	protected void setFinish(Finishline finishline) {
		this.finishline = finishline;
		double y1 = TRACK_SIZE - ((finishline.getPoint1().getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
		double x1 = ((finishline.getPoint1().getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
		y1 = y1 + TRACK_Y_POSITION;
		x1 = x1 + TRACK_X_POSITION;
		double y2 = TRACK_SIZE - ((finishline.getPoint2().getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
		double x2 = ((finishline.getPoint2().getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
		y2 = y2 + TRACK_Y_POSITION;
		x2 = x2 + TRACK_X_POSITION;
		this.finishlineShape = new Line2D.Double(x1, y1, x2, y2);
		this.recalculateRounds();
		this.repaint();
	}

	public void recalculateRounds() {
		if (this.finishline != null) {
			List<Round> rounds = new LinkedList<Round>();
			for (Track track : this.tracks) {
				track.clearRounds();
				Round round = null;
				Trackpoint previousPoint = null;
				for (Trackpoint point : track.getPoints()) {
					// TODO: use crossing point as trackpoint for both ending
					// and starting round
					if (round != null) {
						round.addPoint(point);
					}
					// check when going over finish and (current round has more
					// then 10 points or does not exist) -> start new round
					if (previousPoint != null
							&& Line2D.linesIntersect(previousPoint.getLat(), previousPoint.getLon(), point.getLat(), point.getLon(), this.finishline.getPoint1().getLat(), this.finishline.getPoint1()
									.getLon(), this.finishline.getPoint2().getLat(), this.finishline.getPoint2().getLon())) {
						if (round == null || round.getPoints().size() > 10) {
							if (round != null) {
								rounds.add(round);
								track.addRound(round);
							}
							round = new Round(rounds.size() + 1, track);
						}
					}
					previousPoint = point;
					// add point to current round
				}
			}
			this.rounds = rounds;
			int i = 1;
			String roundsstr = "<html>";
			for (Round r : rounds) {
				Date duration = new Date();
				duration.setTime(r.getDuration());
				roundsstr += i++ + " time: " + TIME_FORMAT.format(r.getStarttime()) + " duration: " + DURATION_FORMAT.format(duration) + "<br />";
			}
			roundsstr += "</html>";
			this.roundslabel.setText(roundsstr);
		}
		// regenerate roundsShape
		if (this.rounds != null) {
			int i = 0;
			this.roundsShape = new Polygon();
			for (Round round : this.rounds) {
				for (Trackpoint tp : round.getPoints()) {
					double y = TRACK_SIZE - ((tp.getLat() - minLat) / (maxLat - minLat)) * TRACK_SIZE;
					double x = ((tp.getLon() - minLon) / (maxLon - minLon)) * TRACK_SIZE;
					y = y + TRACK_Y_POSITION;
					x = x + TRACK_X_POSITION;
					if (i % 5 == 0) {
						this.roundsShape.addPoint((int) x, (int) y);
					}
					i++;
				}
			}
		} else {
			this.roundsShape = null;
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(3));
		if (this.roundsShape != null) {
			g2.draw(this.roundsShape);
		} else {
			g2.draw(this.tracksShape);
		}
		if (this.finishline != null) {
			g2.setColor(Color.BLACK);
			g2.draw(this.finishlineShape);
		}
	}

	public void configureFinish() {
		this.addMouseListener(new MouseListener() {

			private Point startPoint = null;

			public void mousePressed(MouseEvent e) {
				double lon = minLon + (maxLon - minLon) / TRACK_SIZE * (e.getX() - TRACK_X_POSITION);
				double lat = minLat + (maxLat - minLat) / TRACK_SIZE * ((TRACK_SIZE - e.getY()) - TRACK_Y_POSITION);
				this.startPoint = new Point(lat, lon);
			}

			public void mouseReleased(MouseEvent e) {
				double lon = minLon + (maxLon - minLon) / TRACK_SIZE * (e.getX() - TRACK_X_POSITION);
				double lat = minLat + (maxLat - minLat) / TRACK_SIZE * ((TRACK_SIZE - e.getY()) - TRACK_Y_POSITION);
				TrackView.this.setFinish(new Finishline(startPoint, new Point(lat, lon)));
				TrackView.this.removeMouseListener(this);
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});
	}

}
