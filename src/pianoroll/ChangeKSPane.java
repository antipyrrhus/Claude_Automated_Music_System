package pianoroll;

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
import main.KeySignature;

public class ChangeKSPane extends BorderPane {
	private final int PREF_WIDTH = 400;
	private final int PREF_HEIGHT = 100;
	
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	private ComboBox<String> keyCB, modeCB;

	/**
	 * Default constructor.
	 */
	public ChangeKSPane(PianoRollGUI gui, Stage stage) {
		this.stage = stage;
		this.pianoRollGUI = gui;
		this.setPadding(new Insets(10,10,10,10));
        
		HBox cbHB = new HBox(10);
		
		keyCB = new ComboBox<>();
		keyCB.getItems().addAll(KeySignature.intToTonicArr);
		
		modeCB = new ComboBox<>();
		modeCB.getItems().addAll("MAJOR", "HARMONIC MINOR", "NATURAL MINOR", "MELODIC MINOR");
		
		cbHB.getChildren().addAll(keyCB, modeCB);
		
        //OK, Cancel buttons in HBox
        HBox hb = new HBox(10);  //Will leave gap of 20 between the buttons
        hb.setAlignment(Pos.CENTER);
        Button ok = new Button("OK");
        ok.setOnAction(e -> {
        	String keyStr = keyCB.getSelectionModel().getSelectedItem();
        	String modeStr = modeCB.getSelectionModel().getSelectedItem();
        	if (keyStr != null && modeStr != null) {
        		pianoRollGUI.changeKeySignature(new KeySignature(keyStr, modeStr));
        		this.stage.close();
        	} else {
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("Error");
        		alert.setContentText("Please choose the key and mode values from the drop-down menus. Try again.");
        		alert.showAndWait();
        	}
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
        	 this.stage.close();
        });
        hb.getChildren().addAll(ok, cancel);
        
        //Instruction Label on top
        Label instr_lbl = new Label("Please input the desired Key Signature:");
        
        //Set this Pane's size and add nodes
        this.setPrefWidth(PREF_WIDTH);
        this.setPrefHeight(PREF_HEIGHT);
        this.setTop(instr_lbl);
        this.setCenter(cbHB);
        this.setBottom(hb);
        
        BorderPane.setMargin(instr_lbl, new Insets(10));
        BorderPane.setAlignment(instr_lbl, Pos.CENTER);
        BorderPane.setMargin(cbHB, new Insets(10));
        BorderPane.setAlignment(cbHB, Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(10));
        BorderPane.setAlignment(hb,  Pos.CENTER);
	}
}
