package pianoroll;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChangeCellsPane extends BorderPane {

	private class TextFieldListener implements ChangeListener<String> {
		private final TextField textField;
		
		TextFieldListener(TextField textField) {
			this.textField = textField ;
		}
		
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			//do validation on textField
			checkAndFix(textField, oldValue, newValue);
		}
	}
	
	private final int PREF_WIDTH = 500;
	private final int PREF_HEIGHT = 100;
	private final int MAX_NUMBERFIELD_WIDTH = 50;

	private TextField numberField;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	
	public ChangeCellsPane(PianoRollGUI gui, Stage stage) {
		this.pianoRollGUI = gui;
		this.stage = stage;
		this.setPadding(new Insets(10,10,10,10));

		//HBox to hold Number field
		HBox numFieldHB = new HBox(10);
		numFieldHB.setAlignment(Pos.CENTER);
		
		numberField = new TextField();
		numberField.setText(pianoRollGUI.getTotalCols() + "");
		 
		numberField.textProperty().addListener(new TextFieldListener(numberField));

		numberField.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberField.setAlignment(Pos.CENTER);

		numFieldHB.getChildren().addAll(numberField);

		//OK, Cancel buttons in HBox
		HBox buttonsHB = new HBox(10);  //Will leave gap between the buttons
		buttonsHB.setAlignment(Pos.CENTER);
		Button ok = new Button("OK");
		ok.setOnAction(e -> {
			try {
				int numCells = Integer.parseInt(numberField.getText());
				if (numCells < 1 || numCells > ScorePane.MAX_CELLS) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Invalid range");
					alert.setContentText("Invalid range of values. Try again.");
					alert.showAndWait();
				} else {
					Alert alert = new Alert(AlertType.CONFIRMATION,
		    				"This will modify the number of columns. Unsaved changes may be lost. Continue?", 
		    				ButtonType.YES, ButtonType.NO);
					alert.showAndWait();
		    		if (alert.getResult() == ButtonType.YES) {
		    			this.pianoRollGUI.modifyColumns(numCells);
						this.stage.close();
		    		}
				}
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid number. Closing...");
				this.stage.close();
			}
		});
		Button cancel = new Button("Cancel");
		cancel.setOnAction(e -> {
			this.stage.close();
		});
		buttonsHB.getChildren().addAll(ok, cancel);
		
		//Instruction Label on top
		Label instr_lbl = new Label(String.format("Please input the number of desired cells per measure (1 - %s):", 
				ScorePane.MAX_CELLS));

		//Set this Pane's size and add nodes
		this.setPrefWidth(PREF_WIDTH);
		this.setPrefHeight(PREF_HEIGHT);
		this.setTop(instr_lbl);
		this.setCenter(numFieldHB);
		this.setBottom(buttonsHB);

		BorderPane.setMargin(instr_lbl, new Insets(10));
		BorderPane.setAlignment(instr_lbl, Pos.CENTER);
		BorderPane.setMargin(numFieldHB, new Insets(10));
		BorderPane.setAlignment(numFieldHB, Pos.CENTER);
		BorderPane.setMargin(buttonsHB, new Insets(10));
		BorderPane.setAlignment(buttonsHB,  Pos.CENTER);
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
				if (newValue_Int > ScorePane.MAX_CELLS || newValue_Int < 1) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}
}
