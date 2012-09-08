package be.krispypen.RaceAnalyzer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import be.krispypen.RaceAnalyzer.controller.RaceAnalyzerController;
import be.krispypen.RaceAnalyzer.view.RaceAnalyzerView;

public class App extends JPanel {

	public static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public App() {
	}

	public static void main(String[] args) {
		RaceAnalyzerView view = new RaceAnalyzerView();
		RaceAnalyzerController controller = new RaceAnalyzerController(view);
	}

}
