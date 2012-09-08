package be.krispypen.RaceAnalyzer.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JProgressBar;
import javax.swing.JTextField;

import be.krispypen.RaceAnalyzer.view.TrackView;
import be.krispypen.RaceAnalyzer.view.VideoViewer;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaTool;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class VideoBuilder {

	private static final String outputFilename = "file:///tmp/filmoutput.mp4";
	public static final DateFormat ESTIMATE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private File inputVideo = null;
	private long inputVideoDuration = 0;
	private Date inputVideoStartdate = null;
	private boolean isbuilding = false;

	public void setInputVideo(File inputVideo) {
		this.inputVideo = inputVideo;
		final long videoendtime = inputVideo.lastModified();
		IMediaReader mediaReader = ToolFactory.makeReader(inputVideo.getAbsolutePath());
		mediaReader.open();
		this.inputVideoDuration = mediaReader.getContainer().getDuration();
		inputVideoStartdate = new Date();
		inputVideoStartdate.setTime(videoendtime - this.inputVideoDuration / 1000);
	}

	public File getInputVideo() {
		return this.inputVideo;
	}

	public long getInputVideoDuration() {
		return this.inputVideoDuration;
	}

	public Date getInputVideoStartdate() {
		return this.inputVideoStartdate;
	}

	public void build(final TrackView trackViewer, final VideoViewer videoviewer, final VideoBuilder videoBuilder, final JTextField timecorrectionfield, final JProgressBar buildprogressbar) {
		isbuilding = true;
		final Date buildstartdate = new Date();
		// create a media reader
		IMediaReader mediaReader = ToolFactory.makeReader(inputVideo.getAbsolutePath());

		// configure it to generate BufferImages
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

		IMediaWriter mediaWriter = ToolFactory.makeWriter(outputFilename, mediaReader);

		IMediaTool gpxMediaTool = new GpxRoundTimeMediaTool(trackViewer.getTracks(), trackViewer.getRounds(), videoBuilder.getInputVideoStartdate().getTime()
				+ Integer.valueOf(timecorrectionfield.getText()));

		IMediaTool progressListener = new MediaToolAdapter() {
			public void onVideoPicture(IVideoPictureEvent event) {
				int procent = (int) (event.getTimeStamp() * 100 / videoBuilder.getInputVideoDuration());
				buildprogressbar.setValue(procent);
				if (event.getTimeStamp() != 0) {
					Date estimatetime = new Date();
					estimatetime.setTime((new Date().getTime() - buildstartdate.getTime()) * videoBuilder.getInputVideoDuration() / event.getTimeStamp());
					buildprogressbar.setString(procent + " % (" + ESTIMATE_TIME_FORMAT.format(estimatetime) + ")");
				} else {
					buildprogressbar.setString(procent + " %");
				}
			};
		};
		IMediaTool videoViewerMediaTool = videoviewer.getListener();

		mediaReader.addListener(gpxMediaTool);
		gpxMediaTool.addListener(videoViewerMediaTool);
		videoViewerMediaTool.addListener(mediaWriter);
		mediaWriter.addListener(progressListener);

		while (isbuilding && mediaReader.readPacket() == null)
			;

		mediaWriter.close();
		isbuilding = false;
	}

	public void stopBuilding() {
		isbuilding = false;
	}

	public boolean isBuilding() {
		return isbuilding;
	}
}
