package pianoroll;


import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.TimeSignature;

public class PianoRollGUI_DELETE extends Application {
	
	private Stage primaryStage;
	private ArrayList<MeasurePane_DELETE> measurePaneAL;
	private HBox measuresHB;
	private int multiple;
	private TimeSignature ts;
	private int totalMeasures;
	private ScrollPane sp;

	@Override
	public void start(Stage primaryStage) throws Exception {
		/* By default, create 8 measures each of time signature 4/4, with each note button slot pertaining to an 8th note. */
		this.totalMeasures = 50;
		this.ts = new TimeSignature(4,4);
		this.multiple = 2;  //2 slots per quarter note, by default.
		//Put all measures into a hbox
		this.measuresHB = new HBox(10);
		measuresHB.setAlignment(Pos.CENTER);
		
		//Place the above measures hbox into a scrollpane
	    this.sp = new ScrollPane(measuresHB);
		sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		
		drawMeasures(this.measurePaneAL, totalMeasures, ts, multiple, measuresHB);
		
	    
	    Scene scene = new Scene(sp);
	    primaryStage.setScene(scene);
		primaryStage.setTitle("Piano Roll");
		primaryStage.setWidth(600);
		primaryStage.setHeight(200);
		
		this.primaryStage = primaryStage;
		this.primaryStage.show();
	}
	
	private void changeTimeSignature(TimeSignature ts) {
		this.ts = ts;
		drawMeasures(measurePaneAL, totalMeasures, ts, multiple, measuresHB);
	}
	
	private void changeMeasureMult(int multiple) {
		this.multiple = multiple;
		drawMeasures(measurePaneAL, totalMeasures, ts, multiple, measuresHB);
	}
	
	private void drawMeasures(ArrayList<MeasurePane_DELETE> measurePaneAL, int totalMeasures, TimeSignature ts, int multiple, HBox measuresHB) {
		measurePaneAL = new ArrayList<MeasurePane_DELETE>();
		for (int i = 0; i < totalMeasures; ++i) {
			MeasurePane_DELETE curr = new MeasurePane_DELETE(ts, multiple, i); //Initialize MeasurePane, a GridPane subclass
			measurePaneAL.add(curr);
		}
		measuresHB.getChildren().clear();
		measuresHB.getChildren().addAll(measurePaneAL);
	}
	
	private void addMeasure() {
		this.insertMeasure(this.measurePaneAL.size());
	}
	
	private void insertMeasure(int i) {
		if (!measurePaneAL.isEmpty()) {
			ts = measurePaneAL.get(0).getTs();
		}
		measurePaneAL.add(i, new MeasurePane_DELETE(this.ts, this.multiple, i));
		measuresHB.getChildren().clear();
		measuresHB.getChildren().addAll(measurePaneAL);
	}
	
	public static void main(String[] args) throws Exception {
		Application.launch(args);
	}
}
