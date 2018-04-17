package pianoroll;

import javax.sound.midi.Instrument;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChangeInstrumentPane extends BorderPane {
	private final int PREF_WIDTH = 400;
	private final int PREF_HEIGHT = 100;
	
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	private ComboBox<String> instrCB;

	/**
	 * Default constructor.
	 */
	public ChangeInstrumentPane(PianoRollGUI gui, Stage stage) {
		this.stage = stage;
		this.pianoRollGUI = gui;
		this.setPadding(new Insets(10,10,10,10));
        
		instrCB = new ComboBox<>();
		Instrument[] arr = pianoRollGUI.getInstrumentArr();
		for (int i = 0; i < arr.length; ++i) {
			instrCB.getItems().add(String.format("%-5s %s", i, arr[i].toString()));
		}
		
        //OK, Cancel buttons in HBox
        HBox hb = new HBox(10);  //Will leave gap of 20 between the buttons
        hb.setAlignment(Pos.CENTER);
        Button ok = new Button("OK");
        ok.setOnAction(e -> {
        	String keyStr = instrCB.getSelectionModel().getSelectedItem();
        	if (keyStr != null) {
        		pianoRollGUI.changeInstrument(Integer.parseInt(keyStr.substring(0, keyStr.indexOf(" "))));
        		this.stage.close();
        	} else {
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("Error");
        		alert.setContentText("Please choose the instrument from the drop-down menus. Try again.");
        		alert.showAndWait();
        	}
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
        	 this.stage.close();
        });
        hb.getChildren().addAll(ok, cancel);
        
        //Instruction Label on top
        Label instr_lbl = new Label("Please choose your desired instrument:");
        
        //Set this Pane's size and add nodes
        this.setPrefWidth(PREF_WIDTH);
        this.setPrefHeight(PREF_HEIGHT);
        this.setTop(instr_lbl);
        this.setCenter(instrCB);
        this.setBottom(hb);
        
        BorderPane.setMargin(instr_lbl, new Insets(10));
        BorderPane.setAlignment(instr_lbl, Pos.CENTER);
        BorderPane.setMargin(instrCB, new Insets(10));
        BorderPane.setAlignment(instrCB, Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(10));
        BorderPane.setAlignment(hb,  Pos.CENTER);
	}
}
