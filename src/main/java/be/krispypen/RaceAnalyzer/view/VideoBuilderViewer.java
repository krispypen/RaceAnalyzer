package be.krispypen.RaceAnalyzer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import be.krispypen.RaceAnalyzer.App;
import be.krispypen.RaceAnalyzer.helper.VideoBuilder;

public class VideoBuilderViewer extends JPanel {

	private TrackView trackViewer;
	private VideoBuilder videoBuilder;
	private JTextField timecorrectionfield;
	private JLabel startdatelabel;
	private JProgressBar buildprogressbar;
	private JButton selectinputbutton, buildbutton;
	private VideoViewer videoViewer;

	public VideoBuilderViewer(final TrackView trackViewer, final VideoBuilder videoBuilder) {
		super(new BorderLayout());
		this.trackViewer = trackViewer;
		this.videoBuilder = videoBuilder;

		{ // config panel
			JPanel configpanel = new JPanel();
			configpanel.add(new JLabel("Input video"));
			{ // start date label
				startdatelabel = new JLabel("");
				configpanel.add(startdatelabel);
			}
			{ // select input button
				selectinputbutton = new JButton();
				selectinputbutton.setAction(new AbstractAction("Select") {

					public void actionPerformed(ActionEvent e) {
						JFileChooser jfc = new JFileChooser();
						jfc.showOpenDialog(VideoBuilderViewer.this);
						VideoBuilderViewer.this.videoBuilder.setInputVideo(jfc.getSelectedFile());
						startdatelabel.setText(App.TIME_FORMAT.format(VideoBuilderViewer.this.videoBuilder.getInputVideoStartdate()));
						buildbutton.setEnabled(true);
					}
				});
				configpanel.add(selectinputbutton);
			}
			{ // time correction field
				configpanel.add(new JLabel("time correction"));
				timecorrectionfield = new JTextField("-61000");
				configpanel.add(timecorrectionfield);
			}
			this.add(configpanel, BorderLayout.PAGE_START);
		}

		{ // video viewer
			videoViewer = new VideoViewer();
			this.add(videoViewer, BorderLayout.CENTER);
		}

		{ // build panel
			JPanel buildpanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			{ // button
				buildbutton = new JButton();
				buildbutton.setEnabled(false);
				buildbutton.setPreferredSize(new Dimension(200, 50));
				c.weightx = 2;
				c.gridy = 0;
				buildpanel.add(buildbutton, c);
			}

			{ // progress bar
				buildprogressbar = new JProgressBar(0, 100);
				buildprogressbar.setString("");
				buildprogressbar.setStringPainted(true);
				c.ipady = 15;
				c.weightx = 1;
				c.gridy = 1;
				buildpanel.add(buildprogressbar, c);
			}

			this.add(buildpanel, BorderLayout.PAGE_END);

		}

		final AbstractAction startbuildaction = new AbstractAction("Start building") {

			public void actionPerformed(ActionEvent e) {
				if (videoBuilder.getInputVideo() != null) {
					final AbstractAction startbuildaction = this;
					final AbstractAction stopbuildaction = new AbstractAction("Stop building") {

						public void actionPerformed(ActionEvent e) {
							videoBuilder.stopBuilding();
							buildbutton.setAction(startbuildaction);
						}
					};

					buildbutton.setAction(stopbuildaction);
					// int starttime = inputvideofile.get
					new Thread() {
						@Override
						public void run() {
							videoBuilder.build(trackViewer, videoViewer, videoBuilder, timecorrectionfield, buildprogressbar);
							buildbutton.setAction(startbuildaction);
						}
					}.start();
					new Thread() {
						public void run() {
							while (videoBuilder.isBuilding()) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								videoViewer.repaint();
							}
						};
					}.start();
				}
			}
		};
		buildbutton.setAction(startbuildaction);
	}

	public VideoViewer getVideoViewer() {
		return this.videoViewer;
	}

}
