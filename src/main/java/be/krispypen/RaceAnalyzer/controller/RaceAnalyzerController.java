package be.krispypen.RaceAnalyzer.controller;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import be.krispypen.RaceAnalyzer.model.Track;
import be.krispypen.RaceAnalyzer.view.RaceAnalyzerView;

public class RaceAnalyzerController {

	private final RaceAnalyzerView view;

	public RaceAnalyzerController(final RaceAnalyzerView view) {
		this.view = view;

		view.addTrackSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) { // only after change
					JList trackList = (JList) e.getSource();
					List<Track> selectedtracks = new LinkedList<Track>();
					Object[] os = trackList.getSelectedValues();
					for (Object track : os) {
						selectedtracks.add((Track) track);
					}
					view.getTrackView().setTracks(selectedtracks);
				}
			}
		});
	}
}
