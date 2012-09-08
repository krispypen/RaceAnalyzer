package be.krispypen.RaceAnalyzer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import be.krispypen.RaceAnalyzer.helper.GpxReader;
import be.krispypen.RaceAnalyzer.helper.VideoBuilder;
import be.krispypen.RaceAnalyzer.model.Track;

public class RaceAnalyzerView extends JFrame {

	private TrackView trackView;
	private JLabel roundsList;
	private DefaultListModel trackListModel;
	private JList trackList;

	public RaceAnalyzerView() {
		this.setTitle("Race Analyzer");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Choose your gpx files");
		jfc.setMultiSelectionEnabled(true);
		jfc.showOpenDialog(this);
		File[] files = jfc.getSelectedFiles();
		try {
			final List<Track> tracks = GpxReader.readTracks(files);
			roundsList = new JLabel();
			trackView = new TrackView(tracks, roundsList);

			trackListModel = new DefaultListModel();
			for (Track track : tracks) {
				trackListModel.addElement(track);
			}
			trackList = new JList(trackListModel);
			trackList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			trackList.setSelectedIndex(0);
			trackList.setVisibleRowCount(5);

			JPanel leftpanel = new JPanel(new BorderLayout(0, 2));
			leftpanel.add(new JScrollPane(trackList), BorderLayout.CENTER);

			JPanel leftbottompanel = new JPanel(new GridLayout(5, 0));
			leftbottompanel.add(new JButton(new AbstractAction("Set finishline") {

				public void actionPerformed(ActionEvent e) {
					trackView.configureFinish();
				}
			}));
			leftpanel.add(leftbottompanel, BorderLayout.SOUTH);

			// TODO: replace with JTable and TableModel
			JPanel roundspanel = new JPanel(new BorderLayout());
			roundspanel.add(new JLabel("Rounds:"), BorderLayout.PAGE_START);
			roundspanel.add(roundsList, BorderLayout.CENTER);
			JScrollPane roundsscrollpane = new JScrollPane(roundspanel);

			VideoBuilder videoBuilder = new VideoBuilder();
			VideoBuilderViewer videobuilder = new VideoBuilderViewer(trackView, videoBuilder);

			JSplitPane middleSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, trackView, roundsscrollpane);
			middleSplitPane.setResizeWeight(1);
			middleSplitPane.setDividerLocation(350);

			JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, middleSplitPane, videobuilder);
			rightSplitPane.setResizeWeight(1);
			rightSplitPane.setDividerLocation(350);

			JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftpanel, rightSplitPane);
			leftSplitPane.setDividerLocation(250);

			Dimension minimumSize = new Dimension(100, 50);
			leftpanel.setMinimumSize(minimumSize);
			trackView.setMinimumSize(minimumSize);
			this.add(leftSplitPane);
			this.pack();
			this.setSize(1100, 600);
			this.setVisible(true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public TrackView getTrackView() {
		return this.trackView;
	}

	/**
	 * 
	 * TODO: replace with TableModel
	 * 
	 * @return
	 */
	public JLabel getRoundsList() {
		return this.roundsList;
	}

	public DefaultListModel getTrackList() {
		return this.trackListModel;
	}

	public void addTrackSelectionListener(ListSelectionListener listener) {
		trackList.addListSelectionListener(listener);
	}
}
