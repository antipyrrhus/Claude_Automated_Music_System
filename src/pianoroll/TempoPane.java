package pianoroll;

import convertmidi.MidiFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TempoPane extends BorderPane {
	private final int MAX_NUMBERFIELD_WIDTH = 50;
	private final int PREF_WIDTH = 400;
	private final int PREF_HEIGHT = 100;
	
//	private int tempo;
	private TextField numberField;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	

	/**
	 * Default constructor.
	 */
	public TempoPane(PianoRollGUI gui, Stage stage) {
		this(gui, stage, MidiFile.MINIM); //call overloaded constructor
	}
	
	/**
	 * Constructor using tempo variable.
	 * @param tempo
	 */
	public TempoPane(PianoRollGUI gui, Stage stage, int tempo) {
		this.stage = stage;
		this.pianoRollGUI = gui;
		this.setPadding(new Insets(10,10,10,10));
		
		//Number field (only accepts numbers in correct range, as set by MidiFile's MIN_DURATION and MAX_DURATION)
		numberField = new TextField();
		numberField.setText(String.valueOf(tempo));
		numberField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Checking numberField...");
				checkAndFix(numberField, oldValue, newValue);
			}
		});
        
        numberField.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
        numberField.setAlignment(Pos.CENTER);
        
        //OK, Cancel buttons in HBox
        HBox hb = new HBox(10);  //Will leave gap of 20 between the buttons
        hb.setAlignment(Pos.CENTER);
        Button ok = new Button("OK");
        ok.setOnAction(e -> {
        	try {
        		int currTempo = Integer.parseInt(numberField.getText());
        		if (currTempo >= MidiFile.MIN_DURATION && currTempo <= MidiFile.MAX_DURATION) {
        			this.pianoRollGUI.setTempo(currTempo);
        			this.stage.close();
        		} else {
        			Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Invalid range");
					alert.setContentText(String.format("Invalid range of tempo values (must be between %s and %s). Try again.", 
							MidiFile.MIN_DURATION, MidiFile.MAX_DURATION));
					alert.showAndWait();
        		}
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
        Label instr_lbl = new Label(String.format("Please input the desired tempo (from %s to %s):",
        		MidiFile.MIN_DURATION, MidiFile.MAX_DURATION));
        
        //Set this Pane's size and add nodes
        this.setPrefWidth(PREF_WIDTH);
        this.setPrefHeight(PREF_HEIGHT);
        this.setTop(instr_lbl);
        this.setCenter(numberField);
        this.setBottom(hb);
        
        BorderPane.setMargin(instr_lbl, new Insets(10));
        BorderPane.setAlignment(instr_lbl, Pos.CENTER);
        BorderPane.setMargin(numberField, new Insets(10));
        BorderPane.setAlignment(numberField, Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(10));
        BorderPane.setAlignment(hb,  Pos.CENTER);
        
	}
	
	/**
	 * Checks the number text field for illegal values and fixes them in real time
	 * @param tf
	 * @param oldValue
	 * @param newValue
	 */
	public void checkAndFix(TextField tf, String oldValue, String newValue) {
		/* If the textfield is empty or becomes empty, then do nothing. Otherwise... */
		if(!newValue.equals("")) {
			//...try to parse the the new value into an integer....
			try {
				int newValue_Int = Integer.parseInt(newValue);
				if (newValue_Int > MidiFile.MAX_DURATION || newValue_Int < 1) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}
}
