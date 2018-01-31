package pianoroll;
import java.util.ArrayList;
import java.util.HashMap;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import convertmidi.MidiFile;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.ChordSequence;
import main.Harmonizer;
import main.TimeSignature;

public class PianoRollGUI extends Application {
	public static final int MIN_PITCH = 20, MAX_PITCH = MIN_PITCH + 88;
	
	private Stage primaryStage;
	private HBox measuresHB;
	private int multiple;
	private TimeSignature ts;
	private int totalMeasures;
	private ScrollPane sp;
	private BorderPane bp;
	private ArrayList<MeasurePane> measurePaneAL;
	private MeasurePane focusedMeasure;
	private int midi_instrument;
	private int tempo;				//duration of EACH CELL in a measure.
	private KeyPressHandler keyPressHandler;
	private boolean cancelTask;  //cancels existing concurrent thread (stops real-time playback)
	private boolean chordBuilderMode;
	private Scene scene;
	private static MidiChannel[] mChannels;
	private MidiFile midifile;
	private Menu menuFile, menuEdit, menuPlayback;
	private MenuBar menuBar;
	
	
	
	private int octave; //how high up is the pitch? 1st octave, 2nd octave...
	private final HashMap<String, Integer> PITCH_MAP = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
	{
		put("Z",21);
		put("S",22);
		put("X",23);
		put("C",24);
		put("F",25);
		put("V",26);
		put("G",27);
		put("B",28);
		put("N",29);
		put("J",30);
		put("M",31);
		put("K",32);
		put("COMMA",33);
		put("L",34);
		put("PERIOD",35);
		put("SLASH",36);
	}};
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		if (mChannels == null) {
			try{
				/* Create a new Sythesizer and open it. Most of 
				 * the methods you will want to use to expand on this 
				 * example can be found in the Java documentation here: 
				 * https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Synthesizer.html
				 */
				Synthesizer midiSynth = MidiSystem.getSynthesizer(); 
				midiSynth.open();

				//get and load default instrument and channel lists
//		        Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
//		        midiSynth.loadInstrument(instr[150]);//load an instrument
				mChannels = midiSynth.getChannels();

				//change instrument (optional if you want to just use the piano)
//		        mChannels[0].programChange(0);
			} catch (MidiUnavailableException mue) {
				mue.printStackTrace();
			}
		}
		
		/* By default, create 50 measures each of time signature 4/4 */
		//TODO have functions to change these values
		this.midifile = new MidiFile();
		this.octave = 0;
		this.midi_instrument = 60;
		this.tempo = MidiFile.MINIM;
		this.chordBuilderMode = false; //melodic sequence mode
		this.changeTempo(MidiFile.SEMIQUAVER);
		this.changeInstrument(midi_instrument);
		this.totalMeasures = 30;
		this.ts = new TimeSignature(6,8);
		this.changeTimeSignature(new TimeSignature(4,4));
		this.multiple = 4;  //cells per the note duration described above. For example, for 6/8 time signature, multiple 2 means we break up each 8th note into two 16th notes.
		this.changeMeasureMult(4);  //testing method to change the multiple value
		//Put all measures into a hbox
		this.measuresHB = new HBox(10);
		measuresHB.setAlignment(Pos.CENTER);
		
		drawMeasures(totalMeasures,ts,multiple,measuresHB);
				
		//Place the above measures hbox into a scrollpane
	    this.sp = new ScrollPane(measuresHB);
		sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		this.focus(this.measurePaneAL.get(0));
		
		//TODO File menu
        menuBar = new MenuBar();
        // --- Menu File
        menuFile = new Menu("File");
        MenuItem newFile = new MenuItem("New File...");
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN));
        newFile.setOnAction(e -> {
        	System.out.println("New File");
        });
        MenuItem load = new MenuItem("Load...");
        MenuItem save = new MenuItem("Save Current");
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                System.out.println("Save");
            }
        });
        MenuItem saveAs = new MenuItem("Save As...");
        menuFile.getItems().addAll(newFile, load, save, saveAs);        
        
        // --- Menu Edit
        menuEdit = new Menu("Edit");
        MenuItem nextCol = new MenuItem("Next Column");
        nextCol.setId(nextCol.getText());
        nextCol.setAccelerator(new KeyCodeCombination(KeyCode.R));
        nextCol.setOnAction(e -> {
        	this.advanceCol(true);
        });
        MenuItem prevCol = new MenuItem("Prev Column");
        prevCol.setId(prevCol.getText());
        prevCol.setAccelerator(new KeyCodeCombination(KeyCode.Q));
        prevCol.setOnAction(e -> {
        	this.advanceCol(false);
        });
        MenuItem nextMeasure = new MenuItem("Next Measure");
        nextMeasure.setId(nextMeasure.getText());
        nextMeasure.setAccelerator(new KeyCodeCombination(KeyCode.E));
        nextMeasure.setOnAction(e -> {
        	goToNextOrPrevMeasure(true);
        });
        MenuItem prevMeasure = new MenuItem("Prev Measure");
        prevMeasure.setId(prevMeasure.getText());
        prevMeasure.setAccelerator(new KeyCodeCombination(KeyCode.W));
        prevMeasure.setOnAction(e -> {
        	goToNextOrPrevMeasure(false);
        });
        MenuItem backSpace = new MenuItem("Backspace");
        backSpace.setId(backSpace.getText());
        backSpace.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));
        backSpace.setOnAction(e -> {
        	if (focusedMeasure != null) focusedMeasure.backSpace();
        });
        MenuItem deleteNotesInColumn = new MenuItem("Delete");
        deleteNotesInColumn.setId(deleteNotesInColumn.getText());
        deleteNotesInColumn.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        deleteNotesInColumn.setOnAction(e -> {
        	if (focusedMeasure != null) focusedMeasure.delete();
        });
        MenuItem deleteAllNotesInMeasure = new MenuItem("Delete All Notes in Current Measure");
        deleteAllNotesInMeasure.setId(deleteAllNotesInMeasure.getText());
        deleteAllNotesInMeasure.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.ALT_DOWN));
        deleteAllNotesInMeasure.setOnAction(e -> {
        	this.confirmDelAllNotesInMeasure();
        });
        MenuItem insertMeasure = new MenuItem("Insert/Add Measures...");
        MenuItem delCurrMeasure = new MenuItem("Delete Current Measure");
        MenuItem delMeasures = new MenuItem("Delete Measures...");
        MenuItem changeTempo = new MenuItem("Change Tempo...");
        MenuItem increaseTempo = new MenuItem("Increase Tempo");
        increaseTempo.setAccelerator(new KeyCodeCombination(KeyCode.ADD));
        increaseTempo.setOnAction(e -> {
        	this.changeTempo(Math.max(1, tempo - 1));
        });
        MenuItem decreaseTempo = new MenuItem("Decrease Tempo");
        decreaseTempo.setAccelerator(new KeyCodeCombination(KeyCode.SUBTRACT));
        decreaseTempo.setOnAction(e -> {
			this.changeTempo(Math.min(48, tempo + 1));
        });
        MenuItem changeCells = new MenuItem("Change Cells per Measure...");
        menuEdit.getItems().addAll(nextCol, prevCol, nextMeasure, prevMeasure, 
        		backSpace, deleteNotesInColumn, deleteAllNotesInMeasure, 
        		insertMeasure, delCurrMeasure, delMeasures, 
        		changeTempo, increaseTempo, decreaseTempo, changeCells);
        
        // --- Menu Playback
        menuPlayback = new Menu("Playback");
        MenuItem playFromBeginning = new MenuItem("Play from Beginning");
        playFromBeginning.setId("PlayFromBeginning");
        playFromBeginning.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN));
        playFromBeginning.setOnAction(e -> {
        	this.focus(this.measurePaneAL.get(0));
        	PianoRollGUI.this.playBack(focusedMeasure);
        });
        MenuItem playFromCurrent = new MenuItem("Play from Current");
        playFromCurrent.setId("PlayFromCurrent");
        playFromCurrent.setAccelerator(new KeyCodeCombination(KeyCode.P));
        playFromCurrent.setOnAction(e -> {
        	PianoRollGUI.this.playBack(focusedMeasure);
        });
        MenuItem stop = new MenuItem("Stop");
        stop.setId("Stop");
        stop.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
        stop.setOnAction(e -> {
        	cancelTask = true;
        });
        stop.setDisable(true);
        menuPlayback.getItems().addAll(playFromBeginning, playFromCurrent, stop);
        
        menuBar.getMenus().addAll(menuFile, menuEdit, menuPlayback);
		 
		
		
		this.bp = new BorderPane();
		bp.setTop(menuBar);
		bp.setCenter(sp);
//		BorderPane.setAlignment(node, Pos.TOP_CENTER);		//static method to set alignment for node
		
		this.scene = new Scene(bp);
	    primaryStage.setScene(scene);
		primaryStage.setTitle("Piano Roll");
		primaryStage.setWidth(600);
		primaryStage.setHeight(200);
		
//		sp.setOnMouseClicked(e -> {
//			System.out.println(e.getX() + " " + e.getY());
//			System.out.println(e.getSceneX() + " " + e.getSceneY());
//		});
		this.keyPressHandler = new KeyPressHandler();
		scene.setOnKeyPressed(keyPressHandler);
		
		this.cancelTask = false;
		
		this.primaryStage = primaryStage;
		this.primaryStage.show();
		
	} //end public void start
	

	//Automatically scroll according to focused measure
	private void autoScrollPane(ScrollPane sp) {
		if (this.measurePaneAL == null || this.measurePaneAL.size() <= 1) sp.setHvalue(0);
		else if (this.focusedMeasure != null) {
			double x = this.focusedMeasure.getMeasureNum() / (double)(this.measurePaneAL.size()-1);
			sp.setHvalue(x);
		}
	}
	
	public void focus(MeasurePane measurePane, boolean autoscroll, boolean setActiveColToZero) {
		if (measurePane != null) measurePane.setFocus();
		if (this.focusedMeasure != null) {
			if (focusedMeasure.equals(measurePane)) {}
			else {
				focusedMeasure.clear();
				focusedMeasure = null;
			}
		}
		focusedMeasure = measurePane;
		if (autoscroll) autoScrollPane(this.sp);
		if (focusedMeasure != null) focusedMeasure.setActiveColumn(setActiveColToZero ? 0 : focusedMeasure.getCol() - 1);
	}
	
	public void focus(MeasurePane measurePane, boolean autoscroll) {
		focus(measurePane, autoscroll, true);
	}
	
	public void focus(MeasurePane measurePane) {
		focus(measurePane, true);
	}
	
	//Playback from given measure to the end unless stopped (with ESC key)
	private void playBack(MeasurePane measurePane) {
		if (measurePane == null) {
			cancelTask = false;
			this.disableDuringPlayback(false);
			return;
		}
		
		this.focus(measurePane);
		ChordSequence cs = measurePane.getChordSeq();
		//Implement playback in separate thread so can stop anytime
		//Disable the pianoRoll while the separate thread is running
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				midifile.play(cs, midi_instrument, tempo);
				Platform.runLater(new Runnable() {
					public void run() {
						PianoRollGUI.this.disableDuringPlayback(false);
					}
				});
				return null;
				
			}
		};//end Task
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				if (cancelTask == false) {
					playBack(measurePane.getNext());
				} else {  //cancelTask = true.
					//we're done. re-initialize to false.
					cancelTask = false;
				}
			}
		});
		
		
		PianoRollGUI.this.disableDuringPlayback(true);
		new Thread(task).start();
	}

	private void disableMenuItemsDuringPlayback(boolean playBackInProgress) {
		for (Menu menu : menuBar.getMenus()) {
			for (MenuItem menuitem : menu.getItems()) {
				menuitem.setDisable(playBackInProgress);
				if (menuitem.getText().equalsIgnoreCase("Stop")) {
					menuitem.setDisable(!playBackInProgress);
				}
			}
		}
	}
	
	private void disableDuringPlayback(boolean disable) {
		this.sp.setDisable(disable);
		disableMenuItemsDuringPlayback(disable);
	}

	
	private void changeTempo(int tempo) {
		this.tempo = tempo;
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
			
			System.out.println(PITCH_MAP.get("Z") + Harmonizer.SCALE * octave);
			
			if (k.getCode() == KeyCode.SHIFT) {
				octave += 1;
				if (PITCH_MAP.get("SLASH") + Harmonizer.SCALE * octave > PianoRollGUI.MAX_PITCH) {
					octave -= 1;
				}
			} else if (k.getCode() == KeyCode.CONTROL) {
				octave -= 1;
				if (PITCH_MAP.get("Z") + Harmonizer.SCALE * octave < PianoRollGUI.MIN_PITCH) {
					octave += 1;
				}
//			} else if (k.getCode() == KeyCode.BACK_SPACE) {
//				if (focusedMeasure != null) focusedMeasure.backSpace();
//			} else if (k.getCode() == KeyCode.DELETE && focusedMeasure != null) {
//				//Delete all notes in measure after confirming
//				confirmDelAllNotesInMeasure();
//			} else if (k.getCode() == KeyCode.W) {
//				//go to prev measure
//				goToNextOrPrevMeasure(false);
//			} else if (k.getCode() == KeyCode.E) {
//				//go to next measure
//				goToNextOrPrevMeasure(true);
//			} else if (k.getCode() == KeyCode.R) {
//				//Rest note (skip to the next active column)
//				insertRest();
//			} else if (k.getCode() == KeyCode.P) {
//				PianoRollGUI.this.playBack(focusedMeasure);
//				
			} else if (k.getCode() == KeyCode.DIGIT1) {
				PianoRollGUI.this.chordBuilderMode = false;
			} else if (k.getCode() == KeyCode.DIGIT2) {
				PianoRollGUI.this.chordBuilderMode = true;
			}
			//Have a separate GUI for tempo changes later. For now just testing
//			else if (k.getCode() == KeyCode.ADD) {
//				PianoRollGUI.this.changeTempo(Math.max(1, tempo - 1));
//				System.out.println("Tempo changed to " + tempo);
//			} else if (k.getCode() == KeyCode.SUBTRACT) {
//				PianoRollGUI.this.changeTempo(Math.min(48, tempo + 1));
//				System.out.println("Tempo changed to " + tempo);
//			}
			
			else if (PITCH_MAP.get(k.getCode().toString()) != null) {
				Integer pitch = PITCH_MAP.get(k.getCode().toString()) + Harmonizer.SCALE * octave;
				if (pitch != null && PianoRollGUI.this.focusedMeasure != null) {
					if (k.isAltDown()) {
						//Don't play, delete note from the measure's notation if exists
						if (focusedMeasure != null) {
							System.out.println("Deleting pitch...");
							focusedMeasure.deletePitch(pitch);
						}
					} else if (chordBuilderMode) {  //just notate/play pitch, don't move the column bar forward
						focusedMeasure.play(pitch);
					} else {  //move column bar forward then notate/play pitch
						focusedMeasure.play(pitch);
						if (focusedMeasure.advanceActiveColumn(true) == false) {
							if (nextMeasure(focusedMeasure) != null) {
								PianoRollGUI.this.focus(nextMeasure(focusedMeasure));
							}
						}	
					}
				}
				
			} //end if(PITCH_MAP...)
		} //end public void handle
	} //end private class KeyPressHandler
	
	private void advanceCol(boolean forward) {
		if (focusedMeasure == null) return;
		if (focusedMeasure.advanceActiveColumn(forward) == false) {
//			if (nextMeasure(focusedMeasure) != null) {
//				PianoRollGUI.this.focus(nextMeasure(focusedMeasure));
//			}
			this.goToNextOrPrevMeasure(forward, forward);
		}
	}
	
	//TODO implement save / load later
//	private boolean save(String path) {
//		
//	}
//	
//	private boolean load(String path) {
//		
//	}
	
	//TODO (DONE) modify the column rectangle and how it moves on keytyped -- notate in place THEN move forward, and
	//     also have the rectangle default to the first cell whenever a new measure is focused, as well as
	//     when first initialized, have it appear in the first cell of the first measure
	//TODO (DONE) implement chord editor - have the red column rectangle stay in place while you play more keys
	//TODO (DONE) implement mouse-drag for the same note holding for multiple durations
	//TODO (DONE) minor visual changes
	
	//TODO implement GUI for displaying/changing playback instrument, changing tempo, no of cells per measure, playback/stop button
	//TODO File menu for various tasks, set keyboard shortcuts as accelerator and remove redundant code from KeyPressHandler
	
	/* TODO implement real-time playback support for individual notes held over duration (Update: the playback algorithm 
	 * as currently implemented make this difficult unless the entire chord is held for the same duration. 
	 * Will need to implement arpeggios and the like as a separate task. */
	
	
	//TODO implement warning message when a chord is invalid (i.e. more than 4 (later 5) notes per column, a chord has notes of diff duration, etc)
	
	//TODO improve scrollpane auto-scrolling?
	
	
	/**
	 * Confirmation dialog before deleting all notes in a measure
	 */
	private void confirmDelAllNotesInMeasure() {
		if (focusedMeasure == null) return;
		//Alert is a new built-in class for JavaFX
		Alert alert = new Alert(AlertType.CONFIRMATION, "Delete all notes in the selected measure?", ButtonType.YES, ButtonType.NO);
		alert.showAndWait();
		if (alert.getResult() == ButtonType.YES) {
			this.focusedMeasure.deleteAllNotes();
		}		 
	}
	public void playSound(int pitch) {
		mChannels[0].noteOn(pitch, 120);//play note number with specified volume 
		try { 
			Thread.sleep(100); // wait time in milliseconds to control duration
		} catch (InterruptedException ie) {

		}
		mChannels[0].noteOff(pitch);//turn off the note
	}
	
	public void goToNextOrPrevMeasure(boolean nextMeasure, boolean setColIndexToZero) {
		if (nextMeasure) {
			MeasurePane next = nextMeasure(focusedMeasure);
			if (next != null) focus(next, true, setColIndexToZero);
//			autoScrollPane(sp);
		} else {
			MeasurePane prev = prevMeasure(this.focusedMeasure);
			if (prev != null) focus(prev, true, setColIndexToZero);
//			autoScrollPane(sp);
		} 
	}
	
	public void goToNextOrPrevMeasure(boolean nextMeasure) {
		goToNextOrPrevMeasure(nextMeasure, true);
	}
	
	private void changeInstrument(int instrument) {
		mChannels[0].programChange(instrument);
	}
	private MeasurePane prevMeasure(MeasurePane currMeasure) {
		return (currMeasure == null ? null : currMeasure.getPrev());
	}
	private MeasurePane nextMeasure(MeasurePane currMeasure) {
		return (currMeasure == null ? null : currMeasure.getNext());
	}
	
	private void drawMeasures(int totalMeasures, TimeSignature ts, int multiple, HBox measuresHB) {
		this.measurePaneAL = new ArrayList<MeasurePane>();
		
		//Constructor using the prev and next variables.
		for (int i = 0; i < totalMeasures; ++i) {
			MeasurePane curr = new MeasurePane(ts, multiple, i, this, null, null); //Initialize MeasurePane, a GridPane subclass
			this.measurePaneAL.add(curr);
			if (i > 0) {
				curr.setPrev(measurePaneAL.get(i-1));
				measurePaneAL.get(i-1).setNext(curr);
			}
		}
		measuresHB.getChildren().clear();
		measuresHB.getChildren().addAll(measurePaneAL);
	}
	
	public int getTempo() {
		return this.tempo;
	}
	
	private void changeTimeSignature(TimeSignature ts) {
		this.ts = ts;
		
	}
	
	private void changeMeasureMult(int multiple) {
		this.multiple = multiple;
		
	}
	
	
	public static void main(String[] args) throws Exception {
		Application.launch(args);
	}
}
