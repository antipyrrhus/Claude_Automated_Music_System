package pianoroll;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
//import javafx.stage.Stage;

public class InfoPane extends BorderPane {

	
	private final int PREF_WIDTH = 400;
	private final int PREF_HEIGHT = 100;
//	private final int MAX_NUMBERFIELD_WIDTH = 50;

//	private TextField numberField;
//	private Stage stage;
	private Label nameProject_lbl;
	private Label nameProject_lbl_val;
	private Label totalCol_lbl;
	private Label totalCol_lbl_val;
	private Label currCol_lbl;
	private Label currCol_lbl_val;
	private Label tempo_lbl;
	private Label tempo_lbl_val;
	private Label instrument_lbl;
//	private Label instrument_lbl_val;
	private ListView<String> instrument_lbl_val_lv;
	private Label chordBuilder_lbl, chordBuilder_lbl_val, octave_lbl, octave_lbl_val, ks_lbl, ks_lbl_val, 
					cols_per_measure_lbl, cols_per_measure_lbl_val, offset_lbl, offset_lbl_val;
	private Button playFromBeginningBtn, playFromCurrentBtn, stopBtn;
	
	private PianoRollGUI pianoRollGUI;
	
	public InfoPane(PianoRollGUI gui) {
		this.pianoRollGUI = gui;
		this.setPadding(new Insets(10,10,10,10));
		
		//Instruction Label on top
		Label instr_lbl = new Label("Project Information");
		
		//Gridpane for holding various info.
		
		GridPane gPane = new GridPane();
		gPane.setAlignment(Pos.CENTER);
		gPane.setPadding(new Insets(10));
		gPane.setHgap(70);
		gPane.setVgap(10);
		
		nameProject_lbl = new Label("Project name");
		nameProject_lbl_val = new Label("");
		totalCol_lbl = new Label("Columns");
		totalCol_lbl_val = new Label("blahblahblah");
		currCol_lbl = new Label("Current column no.");
		currCol_lbl_val = new Label();
		tempo_lbl = new Label("Duration per col");
		tempo_lbl_val = new Label();
		cols_per_measure_lbl = new Label("Columns per measure");
		cols_per_measure_lbl_val = new Label();
		offset_lbl = new Label("Offset to 1st Measure");
		offset_lbl_val = new Label();
		
		instrument_lbl = new Label("Playback instrument per channel (current ch: " + pianoRollGUI.getFocusedMidiChannel() + ")");
//		instrument_lbl_val = new Label();
		instrument_lbl_val_lv = new ListView<String>();
		instrument_lbl_val_lv.setPrefHeight(280);
		ObservableList<String> items =FXCollections.observableArrayList ();
		instrument_lbl_val_lv.setItems(items);
		this.updateInstr();
		instrument_lbl_val_lv.getSelectionModel().select(0);
		instrument_lbl_val_lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		    		int selectedItemIdx = instrument_lbl_val_lv.getSelectionModel().getSelectedIndex();
		    		pianoRollGUI.setFocusedMidiChannel(selectedItemIdx);
		    		pianoRollGUI.setMidiChannelForSelectedNotes(selectedItemIdx);
		    		update(selectedItemIdx);
		    	}    
		});
		
		
		chordBuilder_lbl = new Label("Chord builder mode");
		chordBuilder_lbl_val = new Label();
		octave_lbl = new Label("Current octave");
		octave_lbl_val = new Label("");
		ks_lbl = new Label("Key signature");
		ks_lbl_val = new Label();
		
		gPane.add(instrument_lbl, 0,0,2,1);
		gPane.add(instrument_lbl_val_lv, 0, 1, 2, 1);
		gPane.addColumn(0, nameProject_lbl, ks_lbl, totalCol_lbl, currCol_lbl, octave_lbl, 
				chordBuilder_lbl, tempo_lbl,cols_per_measure_lbl,offset_lbl);
		gPane.addColumn(1, nameProject_lbl_val, ks_lbl_val, totalCol_lbl_val, currCol_lbl_val, octave_lbl_val, 
				chordBuilder_lbl_val, tempo_lbl_val, cols_per_measure_lbl_val,offset_lbl_val);
		ScrollPane sp = new ScrollPane(gPane);

		playFromBeginningBtn = new Button("Play from Beginning");
		playFromCurrentBtn = new Button("Play from Current");
		stopBtn = new Button("Stop");
		stopBtn.setDisable(true);

		playFromBeginningBtn.setOnAction(e -> {
			pianoRollGUI.playBack(true);
		});
		playFromCurrentBtn.setOnAction(e -> {
			pianoRollGUI.playBack(false);
		});
		stopBtn.setOnAction(e -> {
			pianoRollGUI.stopPlayback();
		});
		
		VBox vb = new VBox();
		vb.setAlignment(Pos.CENTER);
		vb.getChildren().addAll(playFromBeginningBtn, playFromCurrentBtn, stopBtn);
		
		//Set this Pane's size and add nodes
		this.setPrefWidth(PREF_WIDTH);
		this.setPrefHeight(PREF_HEIGHT);
		this.setTop(instr_lbl);
		this.setCenter(sp);
		this.setBottom(vb);

		BorderPane.setMargin(instr_lbl, new Insets(10));
		BorderPane.setAlignment(instr_lbl, Pos.CENTER);
		BorderPane.setMargin(sp, new Insets(10));
		BorderPane.setAlignment(sp, Pos.CENTER);
		BorderPane.setMargin(vb, new Insets(10));
		BorderPane.setAlignment(vb,  Pos.CENTER);
	}

	public void update(int selectedIndex) {
		File f = this.pianoRollGUI.getCurrSaveFile();
		if (f != null) {
			nameProject_lbl_val.setText(f.getName());
		} else {
			nameProject_lbl_val.setText("{UNTITLED}");
		}
		totalCol_lbl_val.setText(pianoRollGUI.getTotalCols() + "");
		currCol_lbl_val.setText(pianoRollGUI.getActiveColumn() + "");
		chordBuilder_lbl_val.setText(pianoRollGUI.isChordBuilderMode() ? "ON" : "OFF");
		tempo_lbl_val.setText(pianoRollGUI.getTempo() + "");
		this.cols_per_measure_lbl_val.setText(pianoRollGUI.getNumColsPerSubMeasure() + "");
		this.offset_lbl_val.setText(pianoRollGUI.getOffset()+"");
		this.updateInstr();
		instrument_lbl.setText("Playback instrument per channel (current ch: " + pianoRollGUI.getFocusedMidiChannel() + ")");
		instrument_lbl_val_lv.getSelectionModel().select(selectedIndex);
		octave_lbl_val.setText(pianoRollGUI.getOctave()+"");
		ks_lbl_val.setText(pianoRollGUI.getKeySignature().toString());
	}
	
	
	private void updateInstr() {
		instrument_lbl_val_lv.getItems().clear();
		//Go thru each channel and update instrument
		for (int i = 0; i < pianoRollGUI.getNumMidiInstrumentChannels(); ++i) {
			
			String curr_instr = pianoRollGUI.getInstrument(i);
			int curr_instr_num = pianoRollGUI.getInstrumentInt(i);
			instrument_lbl_val_lv.getItems().add("Channel " + i + ": " + curr_instr.substring(
					"Instrument: ".length(), curr_instr.indexOf("bank #")) + 
					" " + "(" + curr_instr_num + ")");
		}
	}
	public void disableBtns(boolean isPlaying) {
		stopBtn.setDisable(!isPlaying);
		this.playFromBeginningBtn.setDisable(isPlaying);
		this.playFromCurrentBtn.setDisable(isPlaying);
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
