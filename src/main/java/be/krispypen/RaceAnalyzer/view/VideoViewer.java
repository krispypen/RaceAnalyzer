package be.krispypen.RaceAnalyzer.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class VideoViewer extends JPanel {

	public VideoViewer() {
	}

	private BufferedImage image;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		if (image != null) {
			double w = image.getWidth();
			double h = image.getHeight();
			double factor = Math.max(w / getWidth(), h / getHeight());
			int x = (getWidth() - (int) (w / factor)) / 2;
			int y = (getHeight() - (int) (h / factor)) / 2;
			g2.drawImage(image, x, y, x + (int) (w / factor), y + (int) (h / factor), 0, 0, image.getWidth(), image.getHeight(), null);
		}
	}

	public MediaToolAdapter getListener() {
		return new MediaToolAdapter() {
			@Override
			public void onVideoPicture(IVideoPictureEvent event) {
				VideoViewer.this.image = event.getImage();
				super.onVideoPicture(event);
			}
		};
	}
}
