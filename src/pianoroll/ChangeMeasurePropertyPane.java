package pianoroll;

import convertmidi.MidiFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
//import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
//import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChangeMeasurePropertyPane extends BorderPane {
	private final int MAX_NUMBERFIELD_WIDTH = 50;
	private final int PREF_WIDTH = 400;
	private final int PREF_HEIGHT = 100;
	
	private TextField numColsPerMeasureField, offsetField;
	private int thresholdNumCols, thresholdOffset;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	

	/**
	 * Default constructor.
	 */
	public ChangeMeasurePropertyPane(PianoRollGUI gui, Stage stage) {
		this(gui, stage, MidiFile.MINIM); //call overloaded constructor
	}
	
	/**
	 * Constructor using tempo variable.
	 * @param tempo
	 */
	public ChangeMeasurePropertyPane(PianoRollGUI gui, Stage stage, int tempo) {
		this.stage = stage;
		this.pianoRollGUI = gui;
		this.setPadding(new Insets(10,10,10,10));
		
		this.thresholdNumCols = 1;
		this.thresholdOffset = 0;
		
		
		//Number field (only accepts positive numbers)
		Label numColsPerMeasureLbl = new Label("Number of columns per measure:");
		numColsPerMeasureField = new TextField();
		numColsPerMeasureField.setText(pianoRollGUI.getNumColsPerSubMeasure()+"");
		numColsPerMeasureField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Checking numberField...");
				checkAndFix(numColsPerMeasureField, oldValue, newValue, thresholdNumCols);
			}
		});
		numColsPerMeasureField.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numColsPerMeasureField.setAlignment(Pos.CENTER);
		
		//Number field re: measure offset (only accepts NONNEGATIVE numbers)
		Label offsetLbl = new Label("Measure Offset:");
		offsetField = new TextField();
		offsetField.setText(pianoRollGUI.getOffset()+"");
		offsetField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//				System.out.println("Checking numberField...");
				checkAndFix(offsetField, oldValue, newValue, thresholdOffset);
			}
		});
		offsetField.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		offsetField.setAlignment(Pos.CENTER);
		
		//GP for holding both of the above numberfields
		GridPane gPane = new GridPane();
		gPane.setAlignment(Pos.CENTER);
		gPane.setPadding(new Insets(10));
		gPane.setHgap(30);
		gPane.setVgap(20);
		gPane.addColumn(0, numColsPerMeasureLbl, offsetLbl);
		gPane.addColumn(1, numColsPerMeasureField, offsetField);
		
        //OK, Cancel buttons in HBox
        HBox hb = new HBox(10);  //Will leave gap of 20 between the buttons
        hb.setAlignment(Pos.CENTER);
        Button ok = new Button("OK");
        ok.setOnAction(e -> {
        	try {
        		int numColsPerMeasure = Integer.parseInt(numColsPerMeasureField.getText());
        		int offset = Integer.parseInt(offsetField.getText());
        		pianoRollGUI.updateMeasureProperty(numColsPerMeasure, offset);
        		this.stage.close();
//        		if (currTempo >= MidiFile.MIN_DURATION && currTempo <= MidiFile.MAX_DURATION) {
//        			this.pianoRollGUI.setTempo(currTempo);
//        			this.stage.close();
//        		} else {
//        			Alert alert = new Alert(AlertType.ERROR);
//					alert.setTitle("Invalid range");
//					alert.setContentText(String.format("Invalid range of tempo values (must be between %s and %s). Try again.", 
//							MidiFile.MIN_DURATION, MidiFile.MAX_DURATION));
//					alert.showAndWait();
//        		}
        	} catch(NumberFormatException nfe) {
        		System.out.println("Invalid number. Closing...");
        		this.stage.close();
        	}
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
        	 this.stage.close();
        });
        hb.getChildren().addAll(ok, cancel);
        
        //Instruction Label on top
        Label instr_lbl = new Label("Please input the desired measure properties.");
        
        //Set this Pane's size and add nodes
        this.setPrefWidth(PREF_WIDTH);
        this.setPrefHeight(PREF_HEIGHT);
        this.setTop(instr_lbl);
        this.setCenter(gPane);
        this.setBottom(hb);
        
        BorderPane.setMargin(instr_lbl, new Insets(10));
        BorderPane.setAlignment(instr_lbl, Pos.CENTER);
        BorderPane.setMargin(gPane, new Insets(10));
        BorderPane.setAlignment(gPane, Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(10));
        BorderPane.setAlignment(hb,  Pos.CENTER);
        
	}
	
	/**
	 * Checks the number text field for illegal values and fixes them in real time
	 * @param tf
	 * @param oldValue
	 * @param newValue
	 */
	public void checkAndFix(TextField tf, String oldValue, String newValue, int threshold) {
		/* If the textfield is empty or becomes empty, then do nothing. Otherwise... */
		if(!newValue.equals("")) {
			//...try to parse the the new value into an integer....
			try {
				int newValue_Int = Integer.parseInt(newValue);
				if (newValue_Int < threshold) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}
}
