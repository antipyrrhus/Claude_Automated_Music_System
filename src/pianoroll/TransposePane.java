package pianoroll;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TransposePane extends BorderPane {
	
//	private class TextFieldListener implements ChangeListener<String> {
//		private final TextField textField;
//		private TextField otherTF;
//		private boolean otherTFIsMin;
//		
//		TextFieldListener(TextField textField, TextField otherTF, boolean otherTFIsMin) {
//			this.textField = textField ;
//			this.otherTF = otherTF;
//			this.otherTFIsMin = otherTFIsMin;
//		}
//		
//		@Override
//		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//			// do validation on textField
//			int max = 0, min = 0;
//			if (this.otherTFIsMin) {
//				min = Integer.parseInt(this.otherTF.getText());
//				max = pianoRollGUI.getCellsPerMeasure() - 1;
//			} else {
//				min = 0;
//				max = Integer.parseInt(this.otherTF.getText());
//			}
//			checkAndFix(textField, oldValue, newValue, max, min);
//		}
//	}
	
	private final int PREF_WIDTH = 500;
	private final int PREF_HEIGHT = 100;
	private final int MAX_NUMBERFIELD_WIDTH = 50;

	private TextField numberFieldFrom, numberFieldTo, transTF;
	private RadioButton rbUp, rbDown;
	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	
	public TransposePane(PianoRollGUI gui, Stage stage) {
		this.pianoRollGUI = gui;
		this.stage = stage;
		this.setPadding(new Insets(10,10,10,10));

		//HBox to hold Number fields (only accepts numbers in correct range) and labels
		HBox numFieldHB = new HBox(10);
		numFieldHB.setAlignment(Pos.CENTER);
		Label fromLbl = new Label("Cells: From");
		Label toLbl = new Label("To");
		
		numberFieldFrom = new TextField();
		numberFieldFrom.setText("0");
		
		numberFieldTo = new TextField();
		numberFieldTo.setText(pianoRollGUI.getTotalCols() - 1 + "");
		
//		numberFieldFrom.textProperty().addListener(new TextFieldListener(numberFieldFrom, numberFieldTo, false));
		numberFieldFrom.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberFieldFrom.setAlignment(Pos.CENTER);
		numberFieldFrom.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				checkAndFix(numberFieldFrom, oldValue, newValue, pianoRollGUI.getTotalCols() - 1, 0);
			}
		});

//		numberFieldTo.textProperty().addListener(new TextFieldListener(numberFieldTo, numberFieldFrom, true));
		numberFieldTo.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		numberFieldTo.setAlignment(Pos.CENTER);
		numberFieldTo.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				checkAndFix(numberFieldTo, oldValue, newValue, pianoRollGUI.getTotalCols() - 1, 0);
			}
		});
		
		numFieldHB.getChildren().addAll(fromLbl, numberFieldFrom, toLbl, numberFieldTo);

		//OK, Cancel buttons in HBox
		HBox buttonsHB = new HBox(10);  //Will leave gap between the buttons
		buttonsHB.setAlignment(Pos.CENTER);
		Button ok = new Button("OK");
		ok.setOnAction(e -> {
			try {
				int from = Integer.parseInt(numberFieldFrom.getText());
				int to = Integer.parseInt(numberFieldTo.getText());
				int transposeSteps = Integer.parseInt(transTF.getText());
				boolean up = rbUp.isSelected() ? true : false;
				if (from > to || to < 0 || from > pianoRollGUI.getTotalCols() - 1) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Invalid range");
					alert.setContentText("Invalid range of values for cells. Try again.");
					alert.showAndWait();
				} else {
					this.stage.close();
					this.pianoRollGUI.transpose(from, to, transposeSteps, up);
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
		
		HBox transposeHB = new HBox(10);
		transposeHB.setAlignment(Pos.CENTER);
		Label trans_lbl = new Label("No. of semitones to transpose:");
		this.transTF = new TextField();
		transTF.setText("0");
		transTF.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				checkAndFix(transTF, oldValue, newValue, pianoRollGUI.getTotalNumPitches() - 1, 0);
			}
			
		});
		transTF.setMaxWidth(MAX_NUMBERFIELD_WIDTH);
		transTF.setAlignment(Pos.BOTTOM_CENTER);
		transposeHB.getChildren().addAll(trans_lbl, transTF);
		
		//HBox to hold radiobuttons
		HBox radioHB = new HBox(10);
		radioHB.setAlignment(Pos.TOP_CENTER);
		rbUp = new RadioButton("Transpose Up");
		rbDown = new RadioButton("Transpose Down");
		ToggleGroup tg = new ToggleGroup();
		rbUp.setToggleGroup(tg);
		rbDown.setToggleGroup(tg);
		rbUp.setSelected(true);
		radioHB.getChildren().addAll(rbUp, rbDown);
		
		//Instruction Label on top
		Label instr_lbl = new Label("Please input the range of cells to transpose,\nand the no. of semitones to transpose:");

		
		//VBox to put the middle HBoxes together
		VBox vb = new VBox(10);
		vb.getChildren().addAll(numFieldHB, transposeHB, radioHB);
		VBox.setMargin(numFieldHB, new Insets(0,0,20,0));
		//Set this Pane's size and add nodes
		this.setPrefWidth(PREF_WIDTH);
		this.setPrefHeight(PREF_HEIGHT);
		this.setTop(instr_lbl);
		this.setCenter(vb);
		this.setBottom(buttonsHB);

		BorderPane.setMargin(instr_lbl, new Insets(10));
		BorderPane.setAlignment(instr_lbl, Pos.CENTER);
		BorderPane.setMargin(vb, new Insets(10));
		BorderPane.setAlignment(vb, Pos.CENTER);
		BorderPane.setMargin(buttonsHB, new Insets(10));
		BorderPane.setAlignment(buttonsHB,  Pos.CENTER);
	}
	
	/**
	 * Checks the number text field for illegal values and fixes them in real time
	 * @param tf
	 * @param oldValue
	 * @param newValue
	 */
	public void checkAndFix(TextField tf, String oldValue, String newValue, int max, int min) {
		/* If the textfield is empty or becomes empty, then do nothing. Otherwise... */
		if(!newValue.equals("")) {
			//...try to parse the the new value into an integer....
			try {
				int newValue_Int = Integer.parseInt(newValue);
				if (newValue_Int > max || newValue_Int < min) tf.setText(oldValue);
			}
			//...and if parsing fails, then just reset the text box to the former value
			catch(NumberFormatException nfe) {
				tf.setText(oldValue);
			}
			//End try/catch
		}
	}

}
