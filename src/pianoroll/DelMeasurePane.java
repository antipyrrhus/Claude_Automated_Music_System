package pianoroll;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
//import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DelMeasurePane extends BorderPane{

	private class TextFieldListener implements ChangeListener<String> {
		private final TextField textField;
		
		TextFieldListener(TextField textField) {
			this.textField = textField ;
		}
		
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			// do validation on textField
			checkAndFix(textField, oldValue, newValue);
		}
	}
	
	private final int PREF_WIDTH = 500;
	private final int PREF_HEIGHT = 100;
	private final int MAX_NUMBERFIELD_WIDTH = 50;

	private TextField numberFieldFrom, numberFieldTo;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	

	public DelMeasurePane(PianoRollGUI gui, Stage stage) {
		this.pianoRollGUI = gui;
		this.stage = stage;
		this.setPadding(new Insets(10,10,10,10));

		//HBox to hold Number fields (only accepts numbers in correct range) and labels
		HBox numFieldHB = new HBox(10);
		numFieldHB.setAlignment(Pos.CENTER);
		Label fromLbl = new Label("From");
		Label toLbl = new Label("To");
		
		numberFieldFrom = new TextField();
		numberFieldFrom.setText(pianoRollGUI.getFocusedMeasureNum() + "");
		 
		numberFieldFrom.textProperty().addListener(new TextFieldListener(numberFieldFrom));

		numberFieldFrom.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberFieldFrom.setAlignment(Pos.CENTER);

		numberFieldTo = new TextField();
		numberFieldTo.setText(pianoRollGUI.getFocusedMeasureNum() + "");
		 
		numberFieldTo.textProperty().addListener(new TextFieldListener(numberFieldTo));

		numberFieldTo.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberFieldTo.setAlignment(Pos.CENTER);
		numFieldHB.getChildren().addAll(fromLbl, numberFieldFrom, toLbl, numberFieldTo);
//		numberField.requestFocus();
//		numberField.positionCaret(0);
//		numberField.selectNextWord();
		

		//OK, Cancel buttons in HBox
		HBox buttonsHB = new HBox(10);  //Will leave gap between the buttons
		buttonsHB.setAlignment(Pos.CENTER);
		Button ok = new Button("OK");
		ok.setOnAction(e -> {
			try {
				int from = Integer.parseInt(numberFieldFrom.getText());
				int to = Integer.parseInt(numberFieldTo.getText());
				if (from > to || from < 0 || to < 0) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Invalid range");
					alert.setContentText("Invalid range of values. Try again.");
					alert.showAndWait();
				} else {
					this.pianoRollGUI.deleteMeasures(from, to);
					this.stage.close();
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
		Label instr_lbl = new Label(String.format("Please input the range of measures #'s to delete (0 - %s)",
				this.pianoRollGUI.getTotalNumMeasures() - 1));

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
				if (newValue_Int >= this.pianoRollGUI.getTotalNumMeasures() || newValue_Int < 0) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}
}
