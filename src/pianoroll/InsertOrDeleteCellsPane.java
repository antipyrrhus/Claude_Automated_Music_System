package pianoroll;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InsertOrDeleteCellsPane extends BorderPane {

	private final int PREF_WIDTH = 500;
	private final int PREF_HEIGHT = 100;
	private final int MAX_NUMBERFIELD_WIDTH = 50;
//	private final int MAX_CELLS_TO_ADD = 999;

	private TextField numberFieldFrom, numberFieldTo, nColsField, colField;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	private RadioButton insertRB, deleteRB, beforeCurrColRB, afterCurrColRB;
	private HBox nFieldHB, radioHB1, nColsToAddHB, radioHB2, buttonsHB;
	private VBox vbox;

	public InsertOrDeleteCellsPane(PianoRollGUI gui, Stage stage) {
		this.pianoRollGUI = gui;
		this.stage = stage;
		this.setPadding(new Insets(10,10,10,10));

		//HBox for RadioButtons re: Insert or Delete columns
		radioHB1 = new HBox(20);
		radioHB1.setAlignment(Pos.CENTER);
		this.insertRB = new RadioButton("Insert Columns");
		this.deleteRB = new RadioButton("Delete Columns");
		this.insertRB.setSelected(true);
		ToggleGroup tg1 = new ToggleGroup();
		insertRB.setToggleGroup(tg1);
		deleteRB.setToggleGroup(tg1);
		insertRB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				vbox.getChildren().clear();
				vbox.getChildren().addAll(radioHB1, nColsToAddHB, radioHB2, buttonsHB);
			}
		});
		deleteRB.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				vbox.getChildren().clear();
				vbox.getChildren().addAll(radioHB1, nFieldHB, buttonsHB);
			}
			
			
		});
		radioHB1.getChildren().addAll(insertRB, deleteRB);
		
		//Number field "FROM" (only accepts numbers in correct range)
		numberFieldFrom = new TextField();
		numberFieldFrom.setText("" + pianoRollGUI.getActiveColumn());
		numberFieldFrom.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Checking numberField...");
				checkAndFix(numberFieldFrom, oldValue, newValue);
			}
		});
		numberFieldFrom.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberFieldFrom.setAlignment(Pos.CENTER);
		numberFieldFrom.requestFocus();
		numberFieldFrom.positionCaret(0);
		numberFieldFrom.selectNextWord();

		//Number Field "TO"
		numberFieldTo = new TextField();
		numberFieldTo.setText(numberFieldFrom.getText());
		numberFieldTo.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Checking numberField...");
				checkAndFix(numberFieldTo, oldValue, newValue);
			}
		});
		numberFieldTo.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberFieldTo.setAlignment(Pos.CENTER);
		
		//Labels corresponding to the above numberfields
		Label lblFrom = new Label("From");
		Label lblTo = new Label("To");
		
		//HBox for holding the above numberfields
		nFieldHB = new HBox(20);
		nFieldHB.setAlignment(Pos.CENTER);
		nFieldHB.getChildren().addAll(lblFrom, numberFieldFrom, lblTo, numberFieldTo);
		
		//HBox for entering the no. of columns to insert
		nColsToAddHB = new HBox(20);
		nColsToAddHB.setAlignment(Pos.CENTER);
		Label nColsLbl = new Label("No. of columns to add:");
		nColsField = new TextField();
		nColsField.setText("0");
		nColsField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(!newValue.equals("")) {
					//...try to parse the the new value into an integer....
					try {
						int newValue_Int = Integer.parseInt(newValue);
						if (newValue_Int + pianoRollGUI.getTotalCols() > ScorePane.MAX_CELLS || 
							newValue_Int < 0) {
							nColsField.setText(oldValue);
						}
					}
					//...and if parsing fails, then just reset the text box to the former value
					catch(NumberFormatException nfe) {
						nColsField.setText(oldValue);
					}
					//End try/catch
				}
			}
		});
		nColsToAddHB.getChildren().addAll(nColsLbl, nColsField);
		
		//HBox for RadioButtons and textfield re: Add columns either "Before" or "After" selected column
		radioHB2 = new HBox(20);
		radioHB2.setAlignment(Pos.CENTER);
		this.beforeCurrColRB = new RadioButton("Before");
		this.afterCurrColRB = new RadioButton("After");
		this.beforeCurrColRB.setSelected(true);
		ToggleGroup tg2 = new ToggleGroup();
		beforeCurrColRB.setToggleGroup(tg2);
		afterCurrColRB.setToggleGroup(tg2);
		Label colLbl = new Label("Column Index");
		colField = new TextField();
		colField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Checking numberField...");
				checkAndFix(colField, oldValue, newValue);
			}
		});
		colField.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		colField.setAlignment(Pos.CENTER);
		colField.setText("" + pianoRollGUI.getActiveColumn());
		radioHB2.getChildren().addAll(beforeCurrColRB, afterCurrColRB, colLbl, colField);
		
		//OK, Cancel buttons in HBox
		buttonsHB = new HBox(10);  //Will leave gap of 10 between the buttons
		buttonsHB.setAlignment(Pos.CENTER);
		Button ok = new Button("OK");
		ok.setOnAction(e -> {
			try {
				//Check to see if insert or delete is selected
				//If insert, check the no. of cols to add, and whether it's before or after a certain col index
				if (insertRB.isSelected()) {
					pianoRollGUI.insertCells(
							Integer.parseInt(nColsField.getText()), 
							Integer.parseInt(colField.getText()),
							beforeCurrColRB.isSelected());
				}
				//If delete, check the from and to textboxes and make sure they're legal
				else {
					pianoRollGUI.deleteCells(
							Integer.parseInt(numberFieldFrom.getText()),
							Integer.parseInt(numberFieldTo.getText())
							);
				}
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid number. Closing...");
			} finally {
				this.stage.close();
			}
			
		});
		Button cancel = new Button("Cancel");
		cancel.setOnAction(e -> {
			this.stage.close();
		});
		buttonsHB.getChildren().addAll(ok, cancel);
		
		//VBox for holding the numberfields and the radiobutton hboxes
		vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(radioHB1, nColsToAddHB, radioHB2, buttonsHB);
		

		//Set this Pane's size and add nodes
		this.setPrefWidth(PREF_WIDTH);
		this.setPrefHeight(PREF_HEIGHT);
//		this.setTop(instr_lbl);
		this.setCenter(vbox);
//		this.setBottom(vbox);

		BorderPane.setMargin(vbox, new Insets(10));
		BorderPane.setAlignment(vbox,  Pos.CENTER);

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
				if (newValue_Int >= pianoRollGUI.getTotalCols() || newValue_Int < 0) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}
}
