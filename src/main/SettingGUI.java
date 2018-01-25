package main;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
//import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
//import javafx.scene.control.ScrollPane;
//import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
//import javafx.scene.layout.Background;
//import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.ColumnConstraints;
//import javafx.scene.layout.CornerRadii;
//import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.beans.property.StringProperty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.midi.*;

import convertmidi.MidiFile;
//import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;


/**
 * @author Yury Park
 * A Settings GUI in JavaFX for setting weights, preferences, etc.
 */
public class SettingGUI extends Application {
	
	private MidiChannel[] mChannels;
	private int currentTF = 0;
	private TextField[] tfChordArr;
	private TextArea ta;
	private int option;
	private File file;
	private PrintWriter pw;
	private Stage primaryStage;
	private Label currSettingsFileLbl;
	private int octave; //how high up is the pitch? 1st octave, 2nd octave...
	private int beat;   //type of note -- 8th note? quarter note? etc.
	
	private Button backBtn, clearBtn, emissionBtn, transBtn, melodyBtn;
	
	private Button startMelodySeqBtn, endMelodySeqBtn, harmonizeBtn;  //for Melody creation
	private RadioButton tonalRB, defaultRB, impressionismRB; //for harmonize preset settings
	private HBox hbox_harmonize;  //for storing the above buttons
	private String settingFile; //the .dat file to read harmonize settings from
	private double emissionMult, transMult, ksMult; //multipliers for em, tr, ks.
	private Note[] noteseq;
	private ArrayList<Note> noteAL;
	private TextField tfMelody;
	private Label melodyLbl;
	
	//weight array indices and corresponding values to assign to a chord or transition between chords
	private final int HIGHEST_PRIORITY = 0, HIGH_PRIORITY = 1, 
			MED_PRIORITY = 2, LOW_PRIORITY = 3, DISALLOW = 4;
	private final double HighestP_WEIGHT = 4, HighP_WEIGHT = 3,
			MP_WEIGHT = 2, LowP_WEIGHT = 1, LowestP_WEIGHT = Double.NEGATIVE_INFINITY; 
	/*
	 * The following keyboard keys are mapped to pitches that will be played.
	 * 48 = C, 49 = C#, and so on.
	 */
	private final HashMap<String, Integer> PITCH_MAP = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
	{
		put("Z",48);
		put("S",49);
		put("X",50);
		put("D",51);
		put("C",52);
		put("V",53);
		put("G",54);
		put("B",55);
		put("H",56);
		put("N",57);
		put("J",58);
		put("M",59);
		put("COMMA",60);
		put("L",61);
		put("PERIOD",62);
		put("SEMICOLON",63);
		put("SLASH",64);
	}};
	
	
	/**
	 * Start method.
	 */
	@Override
	public void start(Stage primaryStage) {
		this.noteAL = new ArrayList<Note>();
		this.primaryStage = primaryStage;
		this.octave = 0;
		this.beat = MidiFile.CROTCHET; //default beat
		try{
			/* Create a new Sythesizer and open it. Most of 
			 * the methods you will want to use to expand on this 
			 * example can be found in the Java documentation here: 
			 * https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Synthesizer.html
			 */
			Synthesizer midiSynth = MidiSystem.getSynthesizer(); 
			midiSynth.open();

			//get and load default instrument and channel lists
//	        Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
//	        midiSynth.loadInstrument(instr[150]);//load an instrument
			mChannels = midiSynth.getChannels();

			//change instrument (optional if you want to just use the piano)
//	        mChannels[0].programChange(0);
//	        mChannels[1].programChange(0);
		} catch (MidiUnavailableException mue) {
			mue.printStackTrace();
		}
		
		//Set up the stage
		
		/* Set the label's text accordingly. */
		Label lbl = new Label();
		lbl.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
		lbl.setText("Choose the settings you want to create/modify.");
		
		/* Buttons. Place them into a HBox. */
		clearBtn = new Button("Clear");
		backBtn = new Button("Back");
		emissionBtn = new Button("Emission");
		transBtn = new Button("Transition");
		melodyBtn = new Button("Melody Creator");
		
		HBox hbox = new HBox(10);  //spacing = 10
		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(emissionBtn, transBtn, melodyBtn);
		vbox.getChildren().add(hbox);
		
		
		/* Gridpane for showing pitches. Will be setup when emission or transition button is pressed */
		GridPane gPane = new GridPane();
		gPane.setAlignment(Pos.TOP_CENTER);
		gPane.setPadding(new Insets(10));

		/* Textarea with scrollbar for showing the user edited settings */
		ta = new TextArea();
		ta.setWrapText(true);
//		ta.setMaxHeight(300);
//		ta.setPrefHeight(150);
//		
		
//		ScrollPane sp = new ScrollPane(ta);
//		sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
//		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
//		sp.setMaxHeight(150);
//		sp.setFitToWidth(true);
		
		
		
		/* Radiobuttons for Emission / Transition */
		RadioButton[] rbArr = {new RadioButton("Highest"), new RadioButton("High"),
				new RadioButton("Normal"), new RadioButton("Low"), new RadioButton("Disallow")};
		rbArr[2].setSelected(true);
		ToggleGroup tg = new ToggleGroup();
		for (int i =0; i < rbArr.length; ++i) {
			rbArr[i].setToggleGroup(tg);
		}
		
		/* Buttons to save settings */
		Button storeBtn = new Button("Store");
		Button saveToFileBtn = new Button("Save to Current");
		Button saveToNewFileBtn = new Button("Save as...");
		Button loadFileBtn = new Button("Load a File...");
		
		
		
		storeBtn.setOnAction(e -> {
			String ret = parseChord(tfChordArr, rbArr, this.option);
			if (!ret.isEmpty()) {
				if (checkAndAppendText(ta, ret + "\n") == false) return;
//				ta.appendText(ret + "\n");
				Chord[] chords = getChords(tfChordArr);
				if (option == 0) {
					Chord c1 = chords[0];
					for (int i = 1; i < c1.getNumNotes(); ++i) {
						c1.invertAny();
						checkAndAppendText(ta, computeEmissionStr(rbArr, c1) + "\n");
//						ta.appendText(computeEmissionStr(rbArr, c1) + "\n");
					}
					
				} else if (option == 1) {
					Chord c1 = chords[0];
					Chord c2 = chords[1];
					for (int i = 1; i < c1.getNumNotes(); ++i) {
						c1.invertAny();
						c2.invertAny();
						checkAndAppendText(ta, computeTransitionStr(rbArr, c1, c2) + "\n");
//						ta.appendText(computeTransitionStr(rbArr, c1, c2) + "\n");
					}
				}
				this.currentTF = 0;
				this.tfChordArr[currentTF].requestFocus();
			}
		});
		
		saveToFileBtn.setOnAction(new SavetoExistingFileHandler());
		
		loadFileBtn.setOnAction(new LoadFileHandler());
		
		saveToNewFileBtn.setOnAction(e -> {
			new SaveNewFileHandler().handle(e);
		});
		
		/* HBox to hold the buttons and buttons */
		HBox rbHB = new HBox(10);
		rbHB.setAlignment(Pos.CENTER);
		rbHB.getChildren().addAll(rbArr);
		
		HBox saveHB = new HBox(10);
		saveHB.setAlignment(Pos.CENTER);
		saveHB.getChildren().addAll(storeBtn, saveToFileBtn, saveToNewFileBtn, loadFileBtn);
		
		/* Label to accompany the radiobuttons */
		Label priorityLbl = new Label("Choose Priority:");
		priorityLbl.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
		
		/* Label to let user know of the current settings file */
		currSettingsFileLbl = new Label("");
		
		/* Create borderpane to hold everything. 
		 * Not all components are added at the beginning, as some components
		 * should only show when certain buttons are clicked. */
		BorderPane bPane = new BorderPane();
		bPane.setPadding(new Insets(15));
		bPane.setTop(lbl);		//label on top
		bPane.setCenter(gPane);		
		bPane.setBottom(vbox);			//hbox containing the buttons at bottom
		
		BorderPane.setAlignment(lbl, Pos.TOP_CENTER);		//set alignment for label
//		BorderPane.setAlignment(hbox,  Pos.BOTTOM_CENTER);			//set alignment for hbox containing buttons (Update: not necessary due to hbox.setAlignment(Pos.CENTER);)

		tfChordArr = new TextField[]{new TextField(),new TextField(),new TextField(),new TextField(),
									new TextField(),new TextField(),new TextField(),new TextField()};
		
		for (int i = 0; i < tfChordArr.length; ++i) {
			tfChordArr[i].setId(i + "");
			tfChordArr[i].setEditable(false);
			tfChordArr[i].setAlignment(Pos.CENTER);
			tfChordArr[i].setOnKeyPressed(new KeyPressHandler());

			tfChordArr[i].setOnMouseClicked(e -> {
				currentTF = Integer.parseInt(((TextField)(e.getSource())).getId());
			});
		}
		
		//TextField for melody creation
		tfMelody = new TextField();
		tfMelody.setEditable(false);
		tfMelody.setAlignment(Pos.CENTER);
		tfMelody.setOnKeyPressed(new KeyPressHandler());
		
		/* Create scene, add to stage and show */
		Scene scene = new Scene(bPane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Settings");
		primaryStage.setWidth(600);
		primaryStage.setHeight(200);
		primaryStage.show();

		/* Set event listeners for the clear button, this time anonymously just because we can. */
		clearBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ae) {
				for (int i = 0; i < tfChordArr.length; ++i) {
					tfChordArr[i].clear();
				}
			}
		});
		
		/* Buttons for melody sequence creation only */
		startMelodySeqBtn = new Button("Start new melody sequence");
		endMelodySeqBtn = new Button("End sequence");
		endMelodySeqBtn.setDisable(true);
		harmonizeBtn = new Button("Harmonize");
		
		startMelodySeqBtn.setOnAction(e -> {
			noteAL.clear();
			noteseq = null;
			startMelodySeqBtn.setDisable(true);
			endMelodySeqBtn.setDisable(false);
			ta.clear();
			ta.appendText("Note[] seq = new Note[]{\n");
		});
		
		endMelodySeqBtn.setOnAction(e -> {
			endMelodySeqBtn.setDisable(true);
			SettingGUI.this.startMelodySeqBtn.setDisable(false);
			ta.appendText("};");
			SettingGUI.this.noteseq = noteAL.toArray(new Note[noteAL.size()]);
			System.out.println(Arrays.toString(noteseq));
		});
		
		harmonizeBtn.setOnAction(e -> {
			String code = ta.getText();
			
			int index = 0;
			ArrayList<Note> noteSeqAL = new ArrayList<>();
			while (index != -1) {
				index = code.indexOf("new Note(", index);
				
				if (index == -1) break;
				index += "new Note(".length();
				int index2 = code.indexOf("),", index);
				
				String[] tuple = code.substring(index, index2).split(",");
				System.out.println("Array: " + Arrays.toString(tuple));
				int pitch = Integer.parseInt(tuple[0].trim());
				int duration = Integer.parseInt(tuple[1].trim());
				System.out.println(pitch + " " + duration);
				noteSeqAL.add(new Note(pitch, duration));
			}
			noteseq = new Note[noteSeqAL.size()];
			noteSeqAL.toArray(noteseq);
			if (noteseq == null || noteseq.length == 0) {
				melodyLbl.setText("Error: cannot find melody sequence to harmonize");
			} else {
				melodyLbl.setText("Harmonizing...");
				
				Harmonizer harmonizer = new Harmonizer(settingFile, true, 
						-1, -1,	-1,	
						emissionMult, transMult, ksMult);

				/* Run harmonizer in separate thread for thread safety */
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.submit(new RunHarmonizer(harmonizer, noteseq, noteseq.length * 400,
						30, new MidiFile()));
				executor.shutdown();
				try {
					Thread.sleep(10000);
				} catch(InterruptedException ie) {
					ie.printStackTrace();
				}

				
//				toggleAllMelodyMenu(true);
//				ChordSequence cs = harmonizer.harmonize(noteseq, noteseq.length * 400, 70);
//				MidiFile mf = new MidiFile();
//				mf.progChange(0);
//				mf.saveToMidi(cs, null);
			}
		});
		
		/* Radiobuttons for preset harmonization settings */
		this.tonalRB = new RadioButton("Classic/Tonal");
		this.defaultRB = new RadioButton("Default");
		this.impressionismRB = new RadioButton("Modern/Atonal");
		ToggleGroup tg_preset = new ToggleGroup();
		tonalRB.setToggleGroup(tg_preset); 
		defaultRB.setToggleGroup(tg_preset); 
		impressionismRB.setToggleGroup(tg_preset);
		
		hbox_harmonize = new HBox(10);
		hbox_harmonize.setAlignment(Pos.CENTER);
		hbox_harmonize.getChildren().addAll(tonalRB, defaultRB, impressionismRB, harmonizeBtn);
		
		/**
		 * Actiontrigger when the radiobutton is toggled
		 * (works even when the arrow key is used to select)
		 */
		tonalRB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			/**
			 * Method: changed
			 *
			 * Overrides built-in changed() method.
			 */
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					SettingGUI.this.settingFile = "tonal.dat";
					SettingGUI.this.emissionMult = 1.0;
					SettingGUI.this.transMult = 3.0;
					SettingGUI.this.ksMult = 5.0;
					System.out.println("tonal RB triggered");
				}
			}
		});
		
		defaultRB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			/**
			 * Method: changed
			 *
			 * Overrides built-in changed() method.
			 */
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					SettingGUI.this.settingFile = "default.dat";
					SettingGUI.this.emissionMult = 1.0;
					SettingGUI.this.transMult = 3.0;
					SettingGUI.this.ksMult = 5.0;
					System.out.println("default RB triggered");
				}
			}
		});
		
		impressionismRB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			/**
			 * Method: changed
			 *
			 * Overrides built-in changed() method.
			 */
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					SettingGUI.this.settingFile = "impressionism.dat";
					SettingGUI.this.emissionMult = 1.0;
					SettingGUI.this.transMult = 5.0;
					SettingGUI.this.ksMult = 3.0;
					System.out.println("atonal RB triggered");
				}
			}
		});
		
		/* Set event listener for the emission button. This time using a lambda shortcut
		 * just because we can. */
		emissionBtn.setOnAction(e -> {
			hbox.getChildren().clear();
			hbox.getChildren().addAll(clearBtn, transBtn, melodyBtn, backBtn);
			this.option = 0;
			setupPane(gPane, primaryStage, tfChordArr, this.option);
			bPane.setCenter(gPane);
			bPane.setTop(ta);
			primaryStage.setWidth(750);
			primaryStage.setHeight(700);
			
			vbox.getChildren().clear();
			vbox.getChildren().addAll(priorityLbl, rbHB, saveHB, hbox, currSettingsFileLbl);
			
		});
		
		transBtn.setOnAction(e -> {
			hbox.getChildren().clear();
			hbox.getChildren().addAll(clearBtn, emissionBtn, melodyBtn, backBtn);
			this.option = 1;
			setupPane(gPane, primaryStage, tfChordArr, this.option);
			bPane.setTop(ta);
			bPane.setCenter(gPane);
			primaryStage.setWidth(750);
			primaryStage.setHeight(700);

			vbox.getChildren().clear();
			vbox.getChildren().addAll(priorityLbl, rbHB, saveHB, hbox, currSettingsFileLbl);
		});
		
		melodyBtn.setOnAction( e -> {
			hbox.getChildren().clear();
			hbox.getChildren().addAll(clearBtn, emissionBtn, transBtn, backBtn);
			this.option = 2;
			bPane.setTop(ta);
			ta.clear();
			gPane.getChildren().clear();
			primaryStage.setTitle("Melody Creator");
			primaryStage.setWidth(750);
			primaryStage.setHeight(700);
			melodyLbl = new Label("");
			melodyLbl.setStyle("-fx-text-fill: red; -fx-font-style: italic; -fx-font-weight: bold;");
			bPane.setCenter(tfMelody);
			
			vbox.getChildren().clear();
			vbox.getChildren().addAll(melodyLbl, startMelodySeqBtn, endMelodySeqBtn, hbox_harmonize, hbox);
			defaultRB.setSelected(true);
		});
		
		backBtn.setOnAction(e -> {
			start(this.primaryStage);
		});
	}
	//End start
	
//	private void toggleAllMelodyMenu(boolean enable) {
//		ta.setDisable(!enable);
//		startMelodySeqBtn.setDisable(!enable);
//		endMelodySeqBtn.setDisable(true);
//		clearBtn.setDisable(!enable);
//		emissionBtn.setDisable(!enable);
//		transBtn.setDisable(!enable);
//		backBtn.setDisable(!enable);
//		tfMelody.setDisable(!enable);
//		tonalRB.setDisable(!enable);
//		defaultRB.setDisable(!enable);
//		impressionismRB.setDisable(!enable);
//	}
	
	/**
	 * Appends text to textarea as long as it's not a duplicate.
	 * @param ta textarea
	 * @param s String
	 * @return true IFF append to textarea successful (i.e. no duplicate)
	 */
	private boolean checkAndAppendText(TextArea ta, String s) {
		if (s.startsWith("Emission")) {
			int index = s.indexOf("]") + 1;
			if (!ta.getText().contains(s.substring(0, index))) {
				ta.appendText(s);
				return true;
			} else {
				currSettingsFileLbl.setText(s.substring(0, index) + " is a duplicate.");
				return false;
			}
		} else if (s.startsWith("Transition")) {
			int index = s.indexOf("]", s.indexOf("]") + 1) + 4;
			if (!ta.getText().contains(s.substring(0, index))) {
				ta.appendText(s);
				return true;
			} else {
				currSettingsFileLbl.setText(s.substring(0, index) + " is a duplicate.");
				return false;
			}
		} else {
			ta.appendText(s);
			return true;
		}
	}
	
	/**
	 * getChords
	 * @param tfChordArr TextField array containing chords
	 * @return the Chords as contained in the TextField. One or both Chords may be null
	 *         in the event one or more TextFields are empty.
	 */
	private Chord[] getChords(TextField[] tfChordArr) {
		int[] pitchArr = new int[tfChordArr.length];
		Arrays.fill(pitchArr, -1); //initialize to invalid pitch values to start
		
		for (int i = 0; i < pitchArr.length; ++i) {
			String s = tfChordArr[i].getText();
			if (s.isEmpty()) continue;
			pitchArr[i] = Integer.parseInt(s.substring(2));
		}
		System.out.println(Arrays.toString(pitchArr));
		
		ArrayList<Note> noteAL1 = new ArrayList<>();
		ArrayList<Note> noteAL2 = new ArrayList<>();
		
		//Check the first chord is valid (sorted, at least 3 notes)
		int prevPitch = -1;
		int numNotes = 0;
		for (int i = 0; i < pitchArr.length / 2; ++i) {
			if (pitchArr[i] == -1) continue;
			if (pitchArr[i] < prevPitch) return null;
			noteAL1.add(new Note(pitchArr[i]));
			prevPitch = pitchArr[i];
			numNotes++;
		}
		if (prevPitch == -1 || numNotes < 3) return null;  //this means the chord contains fewer than 3 notes
		
		Chord c1 = new Chord(noteAL1.toArray(new Note[noteAL1.size()]));
		
		Chord[] ret = new Chord[] {c1, null};
		
		
		//Also check the 2nd chord is valid
		
		prevPitch = -1;
		numNotes = 0;
		for (int i = pitchArr.length / 2; i < pitchArr.length; ++i) {
			if (pitchArr[i] == -1) continue;
			if (pitchArr[i] < prevPitch) return ret;
			noteAL2.add(new Note(pitchArr[i]));
			prevPitch = pitchArr[i];
			numNotes++;
		}
		if (prevPitch == -1 || numNotes < 3) return ret;

		Chord c2 = new Chord(noteAL2.toArray(new Note[noteAL2.size()]));
		ret[1] = c2;
		return ret;
	}
	
	/**
	 * Returns the Chord emission or transition in the form of a String
	 * @param tfChordArr Chord textfield array
	 * @param rbArr Radiobutton array
	 * @param option Emission = 0, Transition = 1
	 * @return emission or transition setting String, as the case may be.
	 */
	private String parseChord(TextField[] tfChordArr, RadioButton[] rbArr, int option) {
		Chord[] chords = getChords(tfChordArr); //custom method
		if (chords == null) return "";
		StringBuilder retSB = new StringBuilder();
		if (option == 0) {
			Chord c1 = chords[0];
			if (c1 == null) return "";

			//get the base interval of this chord, and then see radiobutton for
			//what priority the user wants to assign
			retSB.append(computeEmissionStr(rbArr, c1));
			return retSB.toString();
		} else if (option == 1) {
			Chord c1 = chords[0];
			if (c1 == null) return "";
			Chord c2 = chords[1];
			if (c2 == null) return "";
			
			
			
			retSB.append(computeTransitionStr(rbArr, c1, c2));
		}
		return retSB.toString();
	}
	
	/**
	 * Generates String to print out to textarea for a given transition chord pair.
	 * @param rbArr RadioButton array
	 * @param c1 Chord 1
	 * @param c2 Chord 2
	 * @return String
	 */
	private String computeTransitionStr(RadioButton[] rbArr, Chord c1, Chord c2) {
		StringBuilder retSB = new StringBuilder();
//		Interval i1 = c1.getBaseInterval();
//		Interval i2 = c2.getBaseInterval();
		Interval i1 = c1.getIntv();
		Interval i2 = c2.getIntv();
		int baseNoteIntvDiff = c2.getNotes()[0].getPitch() - c1.getNotes()[0].getPitch();
		if (baseNoteIntvDiff >= Harmonizer.SCALE) baseNoteIntvDiff %= Harmonizer.SCALE;
		while (baseNoteIntvDiff < 0) baseNoteIntvDiff += Harmonizer.SCALE;
		
		if (rbArr[HIGHEST_PRIORITY].isSelected()) {
			retSB.append("Transition " + i1 + " " + i2 + " " + baseNoteIntvDiff + " " + HighestP_WEIGHT);
		} else if (rbArr[HIGH_PRIORITY].isSelected()) {
			retSB.append("Transition " + i1 + " " + i2 + " " + baseNoteIntvDiff + " " + HighP_WEIGHT);
		}
		else if (rbArr[MED_PRIORITY].isSelected()) {
			retSB.append("Transition " + i1 + " " + i2 + " " + baseNoteIntvDiff + " " + MP_WEIGHT);
		} else if (rbArr[LOW_PRIORITY].isSelected()) {
			retSB.append("Transition " + i1 + " " + i2 + " " + baseNoteIntvDiff + " " + LowP_WEIGHT);
		} else if (rbArr[DISALLOW].isSelected()){
			retSB.append("Transition " + i1 + " " + i2 + " " + baseNoteIntvDiff + " " + LowestP_WEIGHT);
		}
		
		if (!retSB.toString().isEmpty()) {
			retSB.append("     //e.g. " + Arrays.toString(c1.getNotes()) + " -> "+ 
						Arrays.toString(c2.getNotes()));
		}
		return retSB.toString();
	}
	
	/**
	 * Generates String to to print out to textArea for a given emission chord.
	 * @param rbArr RadioButton array
	 * @param c1 Chord
	 * @return String
	 */
	private String computeEmissionStr(RadioButton[] rbArr, Chord c1) {
		StringBuilder retSB = new StringBuilder();
//		Interval i1 = c1.getBaseInterval();
		Interval i1 = c1.getIntv();
		if (rbArr[HIGHEST_PRIORITY].isSelected()) {
			retSB.append("Emission " + i1 + " " + HighestP_WEIGHT);
		} else if (rbArr[HIGH_PRIORITY].isSelected()) {
			retSB.append("Emission " + i1 + " " + HighP_WEIGHT);
		} else if (rbArr[MED_PRIORITY].isSelected()) {
			retSB.append("Emission " + i1 + " " + MP_WEIGHT);
		} else if (rbArr[LOW_PRIORITY].isSelected()) {
			retSB.append("Emission " + i1 + " " + LowP_WEIGHT);
		} else {  //lowest
			retSB.append("Emission " + i1 + " " + LowestP_WEIGHT);
		}
		if (!retSB.toString().isEmpty()) {
			retSB.append("     //e.g. " + Arrays.toString(c1.getNotes()));
		}
		return retSB.toString();
	}
	/**
	 * Sets up GridPane for playing pitches
	 * @param gridPane
	 * @param primaryStage
	 * @param tfArr
	 * @param option
	 */
	private void setupPane(GridPane gridPane, Stage primaryStage, 
							TextField[] tfArr, int option) {
		gridPane.getChildren().clear();
	    gridPane.setHgap(10);
	    gridPane.setVgap(10);
	    primaryStage.setTitle(option == 0 ? "Emission" : "Transition");
	    gridPane.add(new Label("Chord"), 0, 0);
	    for (int i = 0; i < tfArr.length / 2; ++i) {
	    	gridPane.add(tfArr[i], i, 1);
	    }
	    
	    if (option == 1) {
	    	gridPane.add(new Label("Transition Chord"), 0,2);
	    	for (int i = tfArr.length / 2; i < tfArr.length; ++i)
	    		gridPane.add(tfArr[i], i - tfArr.length / 2, 3);
	    }
	    
	    // Set properties for UI
	    gridPane.setAlignment(Pos.CENTER);
	}
	
	/**
	 * For saving to a new file
	 * @author Yury Park
	 *
	 */
	private class SaveNewFileHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			Stage saveNewFileStage = new Stage();
			
			
			VBox tmpVB = new VBox(10);
			tmpVB.setPadding(new Insets(15));
			tmpVB.setAlignment(Pos.CENTER);
			Label tmpLbl = new Label("Enter the name of the file to save settings to (alphanumeric only, no extension):");
			Label errorLbl = new Label("Invalid filename");
			errorLbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold");
			errorLbl.setVisible(false);
			TextField saveToNewFileTF = new TextField("settings");
			Button btnOK = new Button("OK");
			Button btnCancel = new Button("Cancel");
			btnOK.setOnAction(e -> {
				String s = saveToNewFileTF.getText().trim();
				if (!s.isEmpty() && s.matches(("[A-Za-z0-9]+"))) {
					file = new File(s + ".dat");
					currSettingsFileLbl.setText("Current settings file: " + file.getName());
					new SavetoExistingFileHandler().handle(e);
					saveNewFileStage.close();
				} else {
					errorLbl.setVisible(true);
				}
			});
			btnCancel.setOnAction(e -> {
				saveNewFileStage.close();
			});
			
			HBox tmpHB = new HBox(10);
			tmpHB.setAlignment(Pos.CENTER);
			tmpHB.getChildren().addAll(btnOK, btnCancel);
			
			tmpVB.getChildren().addAll(tmpLbl, saveToNewFileTF, tmpHB, errorLbl);
			
			Scene tmpscn = new Scene(tmpVB);
			saveNewFileStage.setScene(tmpscn);
			saveNewFileStage.initModality(Modality.WINDOW_MODAL);
			saveNewFileStage.initOwner(primaryStage);
			saveNewFileStage.show();
		}
	}
	
	/**
	 * For saving settings to file.
	 * @author Yury Park
	 *
	 */
	private class SavetoExistingFileHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if (file == null) {
				new SaveNewFileHandler().handle(event);
				return;
			}
			
			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(
						file.getAbsolutePath())));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			pw.print(ta.getText());
//			System.out.println("Saved to file " + file.getName());
			currSettingsFileLbl.setText("Saved to file " + file.getName());
			pw.close();
		}
	}
	
	/**
	 * For loading existing settings file.
	 * @author Yury Park
	 *
	 */
	private class LoadFileHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File("."));
			fileChooser.setTitle("Open Settings File");
			fileChooser.getExtensionFilters().addAll(
					new ExtensionFilter("Setting Files", "*.dat"),
//					new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
//					new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
					new ExtensionFilter("All Files", "*.*"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				file = new File(selectedFile.getAbsolutePath());
				System.out.println("settings file loaded");
				currSettingsFileLbl.setText("Current settings file: " + file.getName());
				
				try  {
					BufferedReader br = new BufferedReader(new FileReader(file));
					try {
						StringBuilder sb = new StringBuilder();
						String line = br.readLine();

						while (line != null) {
							sb.append(line);
							sb.append(System.lineSeparator());
							line = br.readLine();
						}
						String everything = sb.toString();
						ta.setText(everything);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					br.close();
				} catch (FileNotFoundException fnf) {
					fnf.printStackTrace();
				} catch (IOException ioe2) {
					ioe2.printStackTrace();
				}
			}
			//end if
		}
	}
	/**
	 * Use keyboard to play notes
	 * @author Yury Park
	 *
	 */
	private class KeyPressHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent k) {
			System.out.println(k.getCode().toString());
			
			if (k.getCode() == KeyCode.SHIFT) {
				octave += 1;
				if (PITCH_MAP.get("SLASH") + Harmonizer.SCALE * octave > 88) {
					octave -= 1;
				} else {
					if (option == 2) {
						melodyLbl.setText("Octave shifted up");
					}
				}
			} else if (k.getCode() == KeyCode.CONTROL) {
				octave -= 1;
				if (PITCH_MAP.get("Z") + Harmonizer.SCALE * octave < 0) {
					octave += 1;
				} else {
					if (option == 2) {
						melodyLbl.setText("Octave shifted down");
					}
				}
			} else if (k.getCode() == KeyCode.ADD) {
				beat -= MidiFile.SEMIQUAVER;
				if (beat < MidiFile.SEMIQUAVER) beat += MidiFile.SEMIQUAVER;
				else if (option == 2) {
					melodyLbl.setText("Tempo set to " + beatToString(beat));
				}
			} else if (k.getCode() == KeyCode.SUBTRACT) {
				beat += MidiFile.SEMIQUAVER;
				if (beat > MidiFile.SEMIBREVE) beat -= MidiFile.SEMIQUAVER; 
				else if (option == 2) melodyLbl.setText("Tempo set to " + beatToString(beat));
			} else if (k.getCode() == KeyCode.DELETE || k.getCode() == KeyCode.BACK_SPACE) {
				if (option == 2) {
					tfMelody.setText("");
					if (startMelodySeqBtn.isDisabled()) {
						if (ta.getText().lastIndexOf("new Note(") >= 0) {
							ta.deleteText(ta.getText().lastIndexOf("new Note("), ta.getText().lastIndexOf(",") + 2);
							melodyLbl.setText("Last note deleted.");
							SettingGUI.this.noteAL.remove(noteAL.size() - 1);
						}
					}
				} else {
					tfChordArr[currentTF].setText("");
				}
			} else if (k.getCode() == KeyCode.SPACE && option == 2) { //rest (for melody creator only)
				tfMelody.setText("Rest");
				if (startMelodySeqBtn.isDisabled()) {
					Note rest = new Note();
					rest.setDuration(beat);
					SettingGUI.this.noteAL.add(rest);
					ta.appendText(String.format("new Note(%s, %s),\n", rest.getPitch(), rest.getDuration()));
				}
			}
			if (PITCH_MAP.get(k.getCode().toString()) != null) {
				Integer pitch = PITCH_MAP.get(k.getCode().toString()) + Harmonizer.SCALE * octave;
	//			System.out.println(pitch);
				if (pitch != null) {
					mChannels[0].noteOn(pitch, 120);//play note number with specified volume 
					try { 
						Thread.sleep(100); // wait time in milliseconds to control duration
					} catch (InterruptedException ie) {
	
					}
					mChannels[0].noteOff(pitch);//turn of the note
					Note note = new Note(pitch);
					note.setDuration(beat);
					if (option == 2) {  //melody generator
						tfMelody.setText(note.toString());
						if (startMelodySeqBtn.isDisabled()) {  //write to textarea and save note
							SettingGUI.this.noteAL.add(note);
							ta.appendText(String.format("new Note(%s, %s),\n", note.getPitch(), note.getDuration()));
						}
					} else {  //emission or transition
						tfChordArr[currentTF++].setText(note.getName() + note.getPitch());
						if (SettingGUI.this.option == 0 && currentTF >= tfChordArr.length / 2 ||
							SettingGUI.this.option == 1 && currentTF >= tfChordArr.length) {
							currentTF = 0;
						}
						tfChordArr[currentTF].requestFocus();
					}
				}
			}
		}
	}
	
	private String beatToString(int beat) {
		String ret = String.valueOf(beat);
		String ret2 = "";
		switch(beat) {
		case MidiFile.SEMIQUAVER:
			ret2 = " (16th note)";
			break;
		case MidiFile.QUAVER:
			ret2 = " (8th note)";
			break;
		case MidiFile.CROTCHET:
			ret2 = " (Quarter note)";
			break;
		case MidiFile.MINIM:
			ret2 = " (Half note)";
			break;
		case MidiFile.SEMIBREVE:
			ret2 = " (Whole note)";
			break;
		case MidiFile.QUAVER + MidiFile.SEMIQUAVER:
			ret2 = " (Dotted 8th note)";
			break;
		case MidiFile.CROTCHET + MidiFile.QUAVER:
			ret2 = " (Dotted quarter note)";
			break;
		case MidiFile.MINIM + MidiFile.CROTCHET:
			ret2 = " (Dotted half note)";
			break;
		case MidiFile.SEMIBREVE + MidiFile.MINIM:
			ret2 = " (Dotted whole note)";
			break;
		default:
			ret2 = "";
		}
		return ret + ret2;
	}

	private class RunHarmonizer implements Runnable {
		Harmonizer harmonizer;
		Note[] noteseq;
		int max_iter;
		int c;
		MidiFile mf;
		public RunHarmonizer(Harmonizer harmonizer, Note[] noteseq, int max_iter, int c,
				MidiFile mf) {
			this.harmonizer = harmonizer;
			this.noteseq = noteseq;
			this.max_iter = max_iter;
			this.c = c;
			this.mf = mf;
		}

		@Override /** Override the run() method to tell the system
		 * what task to perform
		 */
		public void run() {
			ChordSequence cs = this.harmonizer.harmonize(noteseq, max_iter, c);
			mf.progChange(0);
			mf.saveToMidi(cs, null);
		}
	}
	
	
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	//End main
}