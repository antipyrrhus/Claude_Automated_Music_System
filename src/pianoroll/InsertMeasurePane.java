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

public class InsertMeasurePane extends BorderPane {

	private final int PREF_WIDTH = 500;
	private final int PREF_HEIGHT = 100;
	private final int MAX_NUMBERFIELD_WIDTH = 50;
	private final int MAX_MEASURES_TO_ADD = 50;

	private TextField numberField;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	private RadioButton[] rbArr;

	public InsertMeasurePane(PianoRollGUI gui, Stage stage) {
		this.pianoRollGUI = gui;
		this.stage = stage;
		this.setPadding(new Insets(10,10,10,10));

		//Number field (only accepts numbers in correct range, as set by MidiFile's MIN_DURATION and MAX_DURATION)
		numberField = new TextField();
		numberField.setText("1");
		numberField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("Checking numberField...");
				checkAndFix(numberField, oldValue, newValue);
			}
		});

		numberField.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberField.setAlignment(Pos.CENTER);

		numberField.requestFocus();
		numberField.positionCaret(0);
		numberField.selectNextWord();
		

		//OK, Cancel buttons in HBox
		HBox buttonsHB = new HBox(10);  //Will leave gap of 20 between the buttons
		buttonsHB.setAlignment(Pos.CENTER);
		Button ok = new Button("OK");
		ok.setOnAction(e -> {
			try {
				this.pianoRollGUI.insertMeasures(Integer.parseInt(numberField.getText()), rbArr[0].isSelected() ? true:false);
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

		//Another HBox for RadioButtons
		HBox radioHB = new HBox(20);
		radioHB.setAlignment(Pos.CENTER);
		this.rbArr = new RadioButton[]{new RadioButton("Before"), new RadioButton("After")};
		rbArr[1].setSelected(true);
		ToggleGroup tg = new ToggleGroup();
		for (int i = 0; i < rbArr.length; ++i) {
			rbArr[i].setToggleGroup(tg);
		}
		
		
		int currMeasureIndex = this.pianoRollGUI.getFocusedMeasureNum();
		Label rbLbl = new Label();
		//this means focusedMeasure == null, ie no measure is currently focused (probably means all measures have been deleted)
		if (currMeasureIndex == -1) {
			//do nothing
		} else {
			rbLbl = new Label(String.format("Current Measure (#%s)", currMeasureIndex));
		}
		
		radioHB.getChildren().addAll(rbArr[0], rbArr[1], rbLbl);

		//VBox to store the above HBoxes
		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(radioHB, buttonsHB);

		//Instruction Label on top
		Label instr_lbl = new Label(String.format("Please input the no. of measures to add (from 1 to %s):",
				MAX_MEASURES_TO_ADD));

		//Set this Pane's size and add nodes
		this.setPrefWidth(PREF_WIDTH);
		this.setPrefHeight(PREF_HEIGHT);
		this.setTop(instr_lbl);
		this.setCenter(numberField);
		this.setBottom(vbox);

		BorderPane.setMargin(instr_lbl, new Insets(10));
		BorderPane.setAlignment(instr_lbl, Pos.CENTER);
		BorderPane.setMargin(numberField, new Insets(10));
		BorderPane.setAlignment(numberField, Pos.CENTER);
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
				if (newValue_Int > this.MAX_MEASURES_TO_ADD || newValue_Int < 0) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}
}
