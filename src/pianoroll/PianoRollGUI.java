package pianoroll;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Synthesizer;

import convertmidi.MidiFile;
import convertmidi.ReadMidi;
import javafx.application.Application;
import javafx.application.Platform;
//import javafx.concurrent.Task;
//import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
//import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
//import main.ChordSequence;
import main.Chord_NoLimit;
import main.Harmonizer;
import main.Note;
import main.TimeSignature;
import main.KeySignature;

public class PianoRollGUI extends Application {
	public static final int TOTAL_NUM_PITCHES = 88;
	public static final int MIN_PITCH = 20, MAX_PITCH = MIN_PITCH + TOTAL_NUM_PITCHES;
	public static final int RESERVED_DRUM_INSTRUMENT_MIDI_INDEX = 9;
	
	private Stage primaryStage;
	private HBox measuresHB;
	private InfoPane infopane;
	private boolean infopaneIsVisible;
	private int multiple;
	private TimeSignature ts;
	private KeySignature ks;
//	private int totalMeasures;
	private ScrollPane sp;
	private BorderPane bp;
	private ArrayList<ScorePane> measurePaneAL;
	private ScorePane focusedScorePane;
	private int[] midiInstrumentArr;
	private int tempo;				//duration of EACH CELL in a measure.
	private KeyPressHandler keyPressHandler;
//	private volatile boolean cancelTask;  //cancels existing concurrent thread (stops real-time playback)
	private boolean chordBuilderMode;
	private CheckBox melodyChordModeCB;
	private Scene scene;
	private static MidiChannel[] mChannels;
	private int focusedMidiChannel;
	private Synthesizer midiSynth;
	private Instrument[] instrumentArr;
	private MidiFile midifile;
	private boolean isAutoLoop;
	private CheckBox autoLoopChkBox;
	private Menu menuFile, menuEdit, menuPlayback, menuView;
	private MenuBar menuBar;
	private static final int DEFAULT_TOTAL_COLS = ScorePane.DEFAULT_CELLS;
	private static final int STAGE_DEFAULT_WIDTH = 1800, STAGE_DEFAULT_HEIGHT = 900;
	
//	private ArrayList<Integer> prevAL, currAL;
	private ArrayList<WrapperNote> prevNotesAL, currNotesAL;
	private int currColIdx;
	
	private File currSaveFile;
	
	
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
	
    class MyRunnable implements Runnable
    {
    	private volatile boolean cancelTask;
    	private int startIdx;
    	public MyRunnable(int startIdx) {
    		this.startIdx = startIdx;
    	}
    	
    	public void run() {
    		boolean playedThroughOnce = false;
    		/* Keep this loop going as long as at least one of the following conditions is met: 
    		 * 1) we have NOT played through to the end at least once; OR
    		 * 2) the auto loop option is ON */
    		while (!playedThroughOnce || PianoRollGUI.this.isAutoLoop()) {
    			if (cancelTask) break;
    			//prior to starting playback, make sure to auto-scroll to the right starting column
    			Platform.runLater(
    					() -> {
    						PianoRollGUI.this.focusedScorePane.setActiveColumn(startIdx);
    						autoScrollPane(sp, startIdx);
    						updateInfoPane();
    					}
    			);
//	    		currAL = startIdx == 0 ? null : focusedScorePane.getAllPitchesInColumn(startIdx - 1, true);
	    		for (int i = startIdx; i < focusedScorePane.getNumCells(); ++i) {
	    			if (cancelTask) break;
	//    			prevAL = currAL;
	    			prevNotesAL = currNotesAL;
	//    			currAL = focusedScorePane.getAllPitchesInColumn(i, true);
	    			currNotesAL = focusedScorePane.getAllNotesInColumn(i, true);
	//    			playBack(currAL, i, prevAL);
	    			playBack(currNotesAL, i, prevNotesAL);
	    			autoScrollPane(sp, i);
	    			//Use runlater() because we can't control JavaFX application updates within this thread otherwise.
	    			Platform.runLater(
	    					() -> {
	    						PianoRollGUI.this.advanceCol(true);
	    						updateInfoPane();
	    					}
	    			);
	    		}
	    		playedThroughOnce = true;
//	    		disableDuringPlayback(false);
	    		for (WrapperNote note : currNotesAL) {
					if (note == null) continue;
	
					mChannels[note.getChannel()].noteOff(note.getPitch());
				} //end for
	    		
	    		//Note off all previous notes
	    		for (WrapperNote note : prevNotesAL) {
	    			if (note == null) continue;
	    			
					mChannels[note.getChannel()].noteOff(note.getPitch());
	    		}
    		} //end while
    		
    		
    		
    		disableDuringPlayback(false);
    		cancelTask = false;
	   }

	   public void cancel()
	   {
	      cancelTask = true;  
	   }

//	   public boolean isCancelled() {
//	      return cancelled;
//	   }
	}
    
    private MyRunnable playBackThread;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
//		playBackThread = new MyRunnable();
		if (mChannels == null) {
			try{
				/* Create a new Sythesizer and open it. Most of 
				 * the methods you will want to use to expand on this 
				 * example can be found in the Java documentation here: 
				 * https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Synthesizer.html
				 */
				midiSynth = MidiSystem.getSynthesizer(); 
				midiSynth.open();

				//get and load default instrument and channel lists
		        instrumentArr = midiSynth.getAvailableInstruments();
		        
		        System.out.println(instrumentArr.length);
//		        midiSynth.loadInstrument(instrumentArr[0]);//load an instrument
				mChannels = midiSynth.getChannels();
				System.out.println("How many channels does mChannels have? " + mChannels.length);
				
				focusedMidiChannel = 0;
				

				//change instrument (optional if you want to just use the piano)
//		        mChannels[0].programChange(0);
			} catch (MidiUnavailableException mue) {
				mue.printStackTrace();
			}
		}
		
		/* By default, create 50 measures each of time signature 4/4 */
//		this.midifile = new MidiFile();
		
		/* Information Pane */
		//Auto-set all 16 instruments in midiInstrumentArr array to the default instrument (Piano, all with value 0)
		this.midiInstrumentArr = new int[mChannels.length];
		this.infopane = new InfoPane(this);
		this.octave = 0;
		this.tempo = MidiFile.MINIM;
		this.chordBuilderMode = false; //melodic sequence mode
		this.midifile = new MidiFile();
		
//		this.totalMeasures = 30;
		this.ts = new TimeSignature(6,8);
		this.ks = new KeySignature(0,0); //initialize to C major by default
		this.changeTimeSignature(new TimeSignature(4,4));
		this.multiple = 4;  //cells per the note duration described above. For example, for 6/8 time signature, multiple 2 means we break up each 8th note into two 16th notes.
		this.changeMeasureMult(4);  //testing method to change the multiple value
		this.currSaveFile = null;
		//Put all measures into a hbox
		this.measuresHB = new HBox(10);
		measuresHB.setAlignment(Pos.CENTER);
		//Place the above measures hbox into a scrollpane
	    this.sp = new ScrollPane(measuresHB);
		sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		this.drawMeasures(1, DEFAULT_TOTAL_COLS);
		this.setTempo(MidiFile.SEMIQUAVER);
//		this.changeInstrument(midi_instrument);
		
		//File menu
        menuBar = new MenuBar();
        // --- Menu File
        menuFile = new Menu("File");
        MenuItem newFile = new MenuItem("New File...");
        //These accelerators can replace much of the KeyPressedHandler stuff
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newFile.setOnAction(e -> {
        	System.out.println("New File");
        	Alert alert = new Alert(AlertType.CONFIRMATION, 
    				"By starting a new project, you will lose any unsaved changes in your current work. Continue?",
    				ButtonType.YES, ButtonType.NO);
    		alert.showAndWait();
    		if (alert.getResult() == ButtonType.NO) return;
    		clearInfo();
    		drawMeasures(1, DEFAULT_TOTAL_COLS);
    		updateInfoPane();
        });
        
        MenuItem load = new MenuItem("Load...");
        load.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        load.setOnAction(e -> {
        	System.out.println("Load");
        	File loadedFile = loadDialog();
        	if (loadedFile == null) return;
        	this.currSaveFile = loadedFile;
        	populate(loadedFile);
        });
        
        
        MenuItem save = new MenuItem("Save Current");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                System.out.println("Save");
                if (currSaveFile == null) {
                	currSaveFile = PianoRollGUI.this.saveAsDialog();
                }
                
            	if (currSaveFile == null) {
            		return;
            	}
            	saveToFile(currSaveFile, true);
            }
        });
        
        MenuItem saveAs = new MenuItem("Save As...");
        saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        saveAs.setOnAction(e -> {
        	System.out.println("SAVE AS");
        	currSaveFile = PianoRollGUI.this.saveAsDialog();
        	if (currSaveFile == null) {
        		return;
        	}
        	saveToFile(currSaveFile, true);
        });
        
        MenuItem importMidi = new MenuItem("Import Midi...");
        importMidi.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
        importMidi.setOnAction(e -> {
        	System.out.println("IMPORT MIDI");
        	
        	Alert alert = new Alert(AlertType.CONFIRMATION, 
    				"By importing a MIDI file, you will lose any unsaved changes in your current work. Continue?", 
    				ButtonType.YES, ButtonType.NO);
    		alert.showAndWait();
    		if (alert.getResult() == ButtonType.NO) return;
        	
    		//if we get this far, user clicked YES for the above alert.
        	FileChooser fileChooser = new FileChooser();
        	fileChooser.setTitle("Choose MIDI file to import");
        	fileChooser.setInitialDirectory(new File("."));
        	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MIDI files (*.mid)", "*.mid");
        	fileChooser.getExtensionFilters().add(extFilter);
        	try {
        		File fileChoice = fileChooser.showOpenDialog(primaryStage);
        		if (fileChoice != null && fileChoice.isFile() && fileChoice.getName().endsWith(".mid")) {
        			this.currSaveFile = null;
        			System.out.println("Reading MIDI file...");
        			ReadMidi rmidi = new ReadMidi(fileChoice); //read from midi file
        			ArrayList<Chord_NoLimit> cAL = rmidi.getcAL();
        			
        			//NOTE: cellDuration here is simply the greatest common divisor (gcd) value as computed in the ReadMidi class
        			//      and should NOT be confused with the tempo variable in this class.
        			int cellDuration = rmidi.getCellDuration();
      
        			long finalTick = rmidi.getLastTick();
        			System.out.println("cellDuration, finalTick: " + cellDuration + " " + finalTick);
        			
            		//delete all current measures, then create a SINGLE measure with the specified no. of cells
        			//and set the tempo per cell
        			int totalNoCells = (int)finalTick / cellDuration;
        			this.deleteMeasures(0, this.measurePaneAL.size() - 1);
        			this.insertMeasures(1, true);
        			this.drawMeasures(totalNoCells);
        			this.setTempo(cellDuration);
        			
        			//Now populate the cells in that measure with the notes from cAL
        			//Recall that each Chord_NoLimit object in cAL now contains the tick information
        			//to help denote which column each chord lies in the pianoroll.
        			long currTick = 0;
        			int currIdx = 0;
        			for (Chord_NoLimit cn : cAL) {
        				long nextTick = cn.getTick();
        				
        				//Advance the column highlight rectangle to the correct spot
        				while (currTick < nextTick) {
        					currTick += cellDuration;
        					currIdx++;
        				}
        				
        				//"play" all notes in the current chord without advance the column highlighter rectangle
        				//and without doing real-time playback
        				for (Note n : cn.getNotesAL()) {
        					//(done) in MeasurePane, make up wrapper class RectangleNote that supports different durations
        					//over 2 or more cells. Modify PianoRollGUI to support dragging out the note via mouse or otherwise.
        					//Then, draw each note with the given duration here.
        					//(done, kind of)consider just fixing the pianoroll to a SINGLE Measure. Otherwise it's too annoying
        					//to keep track of notes that are held for a long duration, spanning 2 or more measures.
        					//It's also kind of annoying to import / export to MIDI.
        					//Instead, consider divvying up a single measure with bolded lines to indicate measures perhaps? 
        					//(done) use MeasurePane's createNote() method to populate the measure with the notes of varying durations.
        					//(done) then fix playback method so that it takes into account varying durations of notes
        					//and it can be stopped at any time (fix the multithreading part)
//        					this.focusedScorePane.createNote(n, currIdx, cellDuration, false);
        					WrapperNote wn = new WrapperNote(n, currIdx);
        					wn.setDuration(wn.getDuration() / cellDuration);
        					this.focusedScorePane.createNote(wn, false);
//        					this.focusedScorePane.createNote(n, currIdx, cellDuration, false);
        					
        				}
        			} //end for (Chord_NoLimit cn : cAL)
            		this.currSaveFile = null;
            		updateInfoPane();
            	} //end if (fileChoice != null && fileChoice.isFile() && fileChoice.getName().endsWith(".mid"))
        	} catch (NullPointerException npe) {
        		System.out.println("No valid file chosen from the dialog! Returning...");
        		return;
        	} catch (IOException ioe) {
        		System.out.println("File exception raised! Returning...");
        		return;
        	} catch (InvalidMidiDataException imde) {
        		System.out.println("MIDI data exception raised! Returning...");
        		return;
        	}
        });
        
        MenuItem exportMidi = new MenuItem("Export to Midi...");
        exportMidi.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
        exportMidi.setOnAction(e -> {
        	//Show Export to MIDI dialog
        	FileChooser fc = new FileChooser();
    		fc.setTitle("Export to Midi");
    		fc.setInitialDirectory(new File("."));
    		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MIDI files (*.mid)", "*.mid");
            fc.getExtensionFilters().add(extFilter);
            File exportToMidiFile = fc.showSaveDialog(this.primaryStage);
        	if (exportToMidiFile == null) return;
        	File tmpSaveFile = new File("~tmpsave.TMP");
        	saveToFile(tmpSaveFile, false);  //The 2nd parameter here refers to updateInfoPane = false
        	
        	//Try to save to midi file
    		if (midifile.saveToMidi(tmpSaveFile, exportToMidiFile.getAbsolutePath()) == true) {
    			Alert conf = new Alert(AlertType.INFORMATION);
            	conf.setTitle("Export success");
            	conf.setContentText("Successfully exported to " + exportToMidiFile.getName());
            	conf.showAndWait();
    		} else {
    			Alert error = new Alert(AlertType.ERROR);
    			error.setTitle("Error exporting to MIDI");
    			error.setContentText("Something went wrong while exporting to MIDI. Please check the stack trace.");
    			error.showAndWait();
    		} //end if
        });
        
        menuFile.getItems().addAll(newFile, load, save, saveAs, importMidi, exportMidi);        
        
        // --- Menu Edit
        menuEdit = new Menu("Edit");
        
        MenuItem selectAll = new MenuItem("Select All Notes");
        selectAll.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        selectAll.setOnAction(e -> {
        	this.focusedScorePane.selectAllNotes();
        });
        
        MenuItem copySelected = new MenuItem("Copy Selected Notes");
        copySelected.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        copySelected.setOnAction(e -> {
        	this.focusedScorePane.storeSelectedNotesCopy(true);
        });
        
        MenuItem cutSelected = new MenuItem("Cut Selected Notes");
        cutSelected.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        cutSelected.setOnAction(e -> {
        	this.focusedScorePane.storeSelectedNotesCopy(false);
        });
        
        MenuItem pasteSelected = new MenuItem("Paste Notes from Clipboard");
        pasteSelected.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        pasteSelected.setOnAction(e -> {
        	this.focusedScorePane.pasteSelected();
        });
        
        MenuItem insertSelected = new MenuItem("Insert Notes from Clipboard (and Shift Columns)");
        insertSelected.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        insertSelected.setOnAction(e -> {
        	this.focusedScorePane.insertNotes();
        });
        
        MenuItem findNextSelectedPattern = new MenuItem("Find Next Instance of Selected Notes");
        findNextSelectedPattern.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        findNextSelectedPattern.setOnAction(e -> {
        	this.focusedScorePane.findNextSelectedPattern();
        });
        
        MenuItem muteSelectedNotes = new MenuItem("Mute Selected Notes");
        muteSelectedNotes.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN));
        muteSelectedNotes.setOnAction(e -> {
        	focusedScorePane.muteSelectedNotes(true);
        });
        
        MenuItem unmuteSelectedNotes = new MenuItem("Unmute Selected Notes");
        unmuteSelectedNotes.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN));
        unmuteSelectedNotes.setOnAction(e -> {
        	focusedScorePane.muteSelectedNotes(false);
        });
        
        //Update: disable this for now. Color note scheme is kind of a mess and we don't want to designate a special color for melody right now
//        this.melodyChordModeCB = new CheckBox();
//        setMelodyChordModeCB(this.chordBuilderMode);
//        MenuItem toggleMelodyChordMode = new MenuItem("Toggle Chord Mode (currently disabled)", melodyChordModeCB);
//        toggleMelodyChordMode.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SLASH));
//        toggleMelodyChordMode.setOnAction(e -> {
//        	this.chordBuilderMode = !this.chordBuilderMode;
//        	this.setMelodyChordModeCB(this.chordBuilderMode);
////        	lockMelodyNotes(chordBuilderMode);
//        	updateInfoPane();
//        });
        MenuItem raiseOctave = new MenuItem("Raise Octave");
        raiseOctave.setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN));
        raiseOctave.setOnAction(e -> {
        	octave += 1;
			if (PITCH_MAP.get("SLASH") + Harmonizer.SCALE * octave > PianoRollGUI.MAX_PITCH) {
				octave -= 1;
			}
			updateInfoPane();
        });
        
        MenuItem lowerOctave = new MenuItem("Lower Octave");
        lowerOctave.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN));
        lowerOctave.setOnAction(e -> {
        	octave -= 1;
			if (PITCH_MAP.get("Z") + Harmonizer.SCALE * octave < PianoRollGUI.MIN_PITCH) {
				octave += 1;
			}
			updateInfoPane();
        });
        
        MenuItem nextCol = new MenuItem("Next Column");
//        nextCol.setId(nextCol.getText());
        nextCol.setAccelerator(new KeyCodeCombination(KeyCode.R));
        nextCol.setOnAction(e -> {
        	this.advanceCol(true);
        });
        MenuItem prevCol = new MenuItem("Prev Column");
//        prevCol.setId(prevCol.getText());
        prevCol.setAccelerator(new KeyCodeCombination(KeyCode.Q));
        prevCol.setOnAction(e -> {
        	this.advanceCol(false);
        });
//        MenuItem nextMeasure = new MenuItem("Next Measure");
//        nextMeasure.setId(nextMeasure.getText());
//        nextMeasure.setAccelerator(new KeyCodeCombination(KeyCode.E));
//        nextMeasure.setOnAction(e -> {
//        	goToNextOrPrevMeasure(true);
//        });
//        MenuItem prevMeasure = new MenuItem("Prev Measure");
//        prevMeasure.setId(prevMeasure.getText());
//        prevMeasure.setAccelerator(new KeyCodeCombination(KeyCode.W));
//        prevMeasure.setOnAction(e -> {
//        	goToNextOrPrevMeasure(false);
//        });
        MenuItem backSpace = new MenuItem("Backspace");
        backSpace.setId(backSpace.getText());
        backSpace.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));
        backSpace.setOnAction(e -> {
        	if (focusedScorePane != null) {
        		focusedScorePane.backSpace();
        		updateInfoPane();
        	}
        });
        MenuItem deleteNotesInColumn = new MenuItem("Delete Current Column");
        deleteNotesInColumn.setId(deleteNotesInColumn.getText());
        deleteNotesInColumn.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN));
        deleteNotesInColumn.setOnAction(e -> {
        	if (focusedScorePane != null) focusedScorePane.delete();
        });
        
        MenuItem deleteSelectedNotes = new MenuItem("Delete Selected Notes");
        deleteSelectedNotes.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        deleteSelectedNotes.setOnAction(e -> {
        	if (focusedScorePane != null) focusedScorePane.deleteSelectedNotes();
        });
        
        MenuItem deleteAllNotesInMeasure = new MenuItem("Delete All Notes");
//        deleteAllNotesInMeasure.setId(deleteAllNotesInMeasure.getText());
        deleteAllNotesInMeasure.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN));
        deleteAllNotesInMeasure.setOnAction(e -> {
        	this.confirmDelAllNotesInMeasure();
        });
//        MenuItem insertMeasure = new MenuItem("Insert/Add Measures...");
//        insertMeasure.setAccelerator(new KeyCodeCombination(KeyCode.INSERT, KeyCombination.CONTROL_DOWN));
//        insertMeasure.setOnAction(e -> {
//        	Stage impStage = new Stage();
//        	InsertMeasurePane imp = new InsertMeasurePane(this, impStage);
//        	Scene impScene = new Scene(imp);
//        	impStage.setScene(impScene);
//        	impStage.initModality(Modality.APPLICATION_MODAL);
//        	impStage.setTitle("Insert Measures");
//        	impStage.show();
//        });
//        MenuItem delCurrMeasure = new MenuItem("Delete Current Measure");
//        delCurrMeasure.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN));
//        delCurrMeasure.setOnAction(e -> {
//        	if (focusedScorePane == null) return;
//    		//Alert is a new built-in class for JavaFX
//    		Alert alert = new Alert(AlertType.CONFIRMATION, String.format(
//    				"Delete current measure (#%s)?", focusedScorePane.getMeasureNum()), ButtonType.YES, ButtonType.NO);
//    		alert.showAndWait();
//    		if (alert.getResult() == ButtonType.YES) {
//    			int index = focusedScorePane.getMeasureNum();
//    			this.deleteMeasures(index, index);
//    		} //end if ButtonType.YES
//        });
//        MenuItem delMeasures = new MenuItem("Delete Measures...");
//        delMeasures.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
//        delMeasures.setOnAction(e -> {
//        	if (this.measurePaneAL.isEmpty()) return;
//        	Stage dmStage = new Stage();
//        	DelMeasurePane dp = new DelMeasurePane(this, dmStage);
//        	Scene dmScene = new Scene(dp);
//        	dmStage.setScene(dmScene);
//        	dmStage.initModality(Modality.APPLICATION_MODAL);
//        	dmStage.setTitle("Delete Measures");
//        	dmStage.show();
//        });

        MenuItem changeKS = new MenuItem("Change Key Signature...");
        changeKS.setAccelerator(new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN));
        changeKS.setOnAction(e -> {
        	Stage ksStage = new Stage();
        	ChangeKSPane ksp = new ChangeKSPane(this, ksStage);
        	Scene ksScene = new Scene(ksp);
        	ksStage.setScene(ksScene);
        	ksStage.initModality(Modality.APPLICATION_MODAL);
        	ksStage.setTitle("Change Key Signature");
        	ksStage.show();
        });
        
        MenuItem changeTempo = new MenuItem("Change Tempo...");
        changeTempo.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
        changeTempo.setOnAction(e -> {
        	Stage tpStage = new Stage();
        	TempoPane tp = new TempoPane(this, tpStage, this.tempo);
        	Scene tpScene = new Scene(tp);
        	tpStage.setScene(tpScene);
        	tpStage.initModality(Modality.APPLICATION_MODAL);
        	tpStage.setTitle("Change Tempo");
        	tpStage.show();
        });
        MenuItem increaseTempo = new MenuItem("Increase Tempo");
        increaseTempo.setAccelerator(new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN));
        increaseTempo.setOnAction(e -> {
        	this.setTempo(Math.max(MidiFile.MIN_DURATION, tempo - 1));
        });
        MenuItem decreaseTempo = new MenuItem("Decrease Tempo");
        decreaseTempo.setAccelerator(new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN));
        decreaseTempo.setOnAction(e -> {
			this.setTempo(Math.min(MidiFile.MAX_DURATION, tempo + 1));
        });
        
        MenuItem insertOrDeleteCells = new MenuItem("Insert or Delete Columns...");
        insertOrDeleteCells.setAccelerator(new KeyCodeCombination(KeyCode.CLOSE_BRACKET));
        insertOrDeleteCells.setOnAction(e -> {
        	Stage idcStage = new Stage();
        	InsertOrDeleteCellsPane idcp = new InsertOrDeleteCellsPane(this, idcStage);
        	Scene idcScene = new Scene(idcp);
        	idcStage.setScene(idcScene);
        	idcStage.initModality(Modality.APPLICATION_MODAL);
        	idcStage.setTitle("Insert / Delete Column Cells");
        	idcStage.show();
        });
        
        MenuItem changeMeasureProperty = new MenuItem("Change Measure Properties...");
        changeMeasureProperty.setAccelerator(new KeyCodeCombination(KeyCode.OPEN_BRACKET));
        changeMeasureProperty.setOnAction(e -> {
        	Stage cmpStage = new Stage();
        	ChangeMeasurePropertyPane cmpPane = new ChangeMeasurePropertyPane(this, cmpStage);
        	Scene cmpScene = new Scene(cmpPane);
        	cmpStage.setScene(cmpScene);
        	cmpStage.initModality(Modality.APPLICATION_MODAL);
        	cmpStage.setTitle("Change Measure Property");
        	cmpStage.show();
        	
        });
        
        MenuItem transpose = new MenuItem("Transpose Notes...");
        transpose.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        transpose.setOnAction(e -> {
        	if (this.getTotalNumMeasures() == 0) {
    			Alert alert = new Alert(AlertType.ERROR);
    			alert.setTitle("Empty");
    			alert.setContentText("The sequencer is empty. Please create one or more measures and try again.");
    			alert.showAndWait();
    			return;
    		}
        	Stage transStage = new Stage();
        	TransposePane transP = new TransposePane(this, transStage);
        	Scene transScene = new Scene(transP);
        	transStage.setScene(transScene);
        	transStage.initModality(Modality.APPLICATION_MODAL);
        	transStage.setTitle("Transpose Notes");
        	transStage.show(); 
        });

        menuEdit.getItems().addAll(selectAll, copySelected, cutSelected, pasteSelected, insertSelected,
        		findNextSelectedPattern, muteSelectedNotes, unmuteSelectedNotes,
//        		toggleMelodyChordMode, 
        		raiseOctave, lowerOctave, nextCol, prevCol, 
        		backSpace, deleteNotesInColumn, deleteSelectedNotes, deleteAllNotesInMeasure, 
        		changeKS, changeTempo, increaseTempo, decreaseTempo, 
        		changeMeasureProperty, insertOrDeleteCells, transpose);
        
        // --- Menu Playback
        menuPlayback = new Menu("Playback");
        
        MenuItem playCurrent = new MenuItem("Play Current Column");
        playCurrent.setAccelerator(new KeyCodeCombination(KeyCode.W));
        playCurrent.setOnAction(e -> {
        	this.playBackCurrCol(false);
        });
        
        MenuItem playAndStep = new MenuItem("Play and Step");
        playAndStep.setAccelerator(new KeyCodeCombination(KeyCode.E));
        playAndStep.setOnAction(e -> {
        	this.playBackCurrCol(true);
        });
        
        MenuItem playFromBeginning = new MenuItem("Play from Beginning");
        playFromBeginning.setId("PlayFromBeginning");
        playFromBeginning.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
        playFromBeginning.setOnAction(e -> {
        	this.focus(this.measurePaneAL.get(0));
        	PianoRollGUI.this.playBack(focusedScorePane);
        });
        
        MenuItem playFromCurrent = new MenuItem("Play from Current");
        playFromCurrent.setId("PlayFromCurrent");
        playFromCurrent.setAccelerator(new KeyCodeCombination(KeyCode.P));
        playFromCurrent.setOnAction(e -> {
        	PianoRollGUI.this.playBack(focusedScorePane, focusedScorePane.getActiveColumn());
        });
        
        MenuItem stop = new MenuItem("Stop");
        stop.setId("Stop");
        stop.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
        stop.setOnAction(e -> {
        	stopPlayback();
        });
        stop.setDisable(true);
        
        this.autoLoopChkBox = new CheckBox();
        this.setAutoLoop(false); //initialize auto-loop to false
        MenuItem autoLoop = new MenuItem("Auto-Loop", autoLoopChkBox);
        autoLoop.setOnAction(e -> {
        	this.setAutoLoop(!this.isAutoLoop()); //toggle between auto-loop and not
        });
        
        MenuItem changeInstr = new MenuItem("Change Instrument...");
        changeInstr.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        changeInstr.setOnAction(e -> {
        	Stage instrStage = new Stage();
        	ChangeInstrumentPane instP = new ChangeInstrumentPane(this, instrStage);
        	instrStage.setScene(new Scene(instP));
        	instrStage.initModality(Modality.APPLICATION_MODAL);
        	instrStage.setTitle("Change Instrument");
        	instrStage.show(); 
        });

        menuPlayback.getItems().addAll(playCurrent, playAndStep, playFromBeginning, playFromCurrent, stop, autoLoop, changeInstr);
        
        menuView = new Menu("View");
        MenuItem showInfo = new MenuItem("Show/Hide Information Panel");
        showInfo.setAccelerator(new KeyCodeCombination(KeyCode.I));
        showInfo.setOnAction(e -> {
        	if (infopaneIsVisible) {
        		bp.setLeft(null);
        	} else {
        		bp.setLeft(infopane);
        	}
        	infopaneIsVisible = !infopaneIsVisible;
        });
        
        MenuItem zoomIn = new MenuItem("Zoom In");
        zoomIn.setAccelerator(new KeyCodeCombination(KeyCode.ADD));
        zoomIn.setOnAction(e -> {
        	this.focusedScorePane.zoomIn();
        });
        
        MenuItem zoomOut = new MenuItem("Zoom Out");
        zoomOut.setAccelerator(new KeyCodeCombination(KeyCode.SUBTRACT));
        zoomOut.setOnAction(e -> {
        	this.focusedScorePane.zoomOut();
        });
        
        MenuItem showCustomPane = new MenuItem("Show User Custom Dialog");
        showCustomPane.setAccelerator(new KeyCodeCombination(KeyCode.F10));
        showCustomPane.setOnAction(e -> {
        	Stage customStage = new Stage();
        	CustomFunctionsPane customPane = new CustomFunctionsPane(this, customStage);
        	customStage.setScene(new Scene(customPane));
        	customStage.initModality(Modality.APPLICATION_MODAL);
        	customStage.setTitle("User Defined Methods");
        	customStage.show(); 
        });
        
        menuView.getItems().addAll(zoomIn, zoomOut, showInfo, showCustomPane);
        
        menuBar.getMenus().addAll(menuFile, menuEdit, menuPlayback, menuView);
		
		this.bp = new BorderPane();
		bp.setTop(this.menuBar);
		bp.setCenter(this.sp);
		bp.setLeft(this.infopane);
		this.infopaneIsVisible = true;
//		BorderPane.setAlignment(node, Pos.TOP_CENTER);		//static method to set alignment for node
		
		this.scene = new Scene(bp);
	    primaryStage.setScene(scene);
		primaryStage.setTitle("Piano Roll");
		primaryStage.setWidth(STAGE_DEFAULT_WIDTH);
		primaryStage.setHeight(STAGE_DEFAULT_HEIGHT);
		
		sp.setOnMouseClicked(e -> {
			System.out.println(e.getX() + " " + e.getY());
			System.out.println(e.getSceneX() + " " + e.getSceneY());
			updateInfoPane();
		});
		this.keyPressHandler = new KeyPressHandler();
		scene.setOnKeyPressed(keyPressHandler);
		
//		this.cancelTask = false;
		this.clearInfo();
		this.primaryStage = primaryStage;
		this.primaryStage.show();
		updateInfoPane();
		
	} //end public void start
	
//	private void lockMelodyNotes(boolean lock) {
//		focusedScorePane.lockMelodyNotes(lock);
//	}
	
	public void setFocusedMeasure(ScorePane m) {
		this.focusedScorePane = m;
	}
	
	private void clearInfo() {
		this.currSaveFile = null;
		this.chordBuilderMode = false;
		this.setTempo(MidiFile.SEMIQUAVER);
		this.changeInstrument(0);
		this.changeKeySignature(0,0);
		this.octave = 0;
	}
	
	public int getActiveColumn() {
		return this.focusedScorePane.getActiveColumn();
	}
	
	public boolean isChordBuilderMode() {
		return this.chordBuilderMode;
	}
	
	public ScorePane getScorePane() {
		return this.focusedScorePane;
	}
	
	/**
	 * Loads data from a file to populate the sequencer.
	 * NOTE: this works via invoking a helper method. A bit awkward, but needed because
	 * we have another method called modifyColumns() that modifies the number of columns without affecting
	 * the existing notes on the sequencer, and this method works by saving current music canvas to a temp file
	 * and then loading that file (except the no. of columns is different from that saved in the temp file),
	 * we have a helper populate() method that takes in the savefile and, additionally, a separate numcol variable
	 * containing the new number of columns to set, which again is different from that saved in the savefile.
	 * @param file
	 */
	private void populate(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			//Ignore first 2 lines. We just want the num. of cols info.
			br.readLine();
			br.readLine();
			int numCol = Integer.parseInt(br.readLine());
			br.close();
			populate(file, numCol); //call helper method.
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void populate(File file, int numCol) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			br.readLine(); 	 //durationPerCell. No longer used.
			int tempo = Integer.parseInt(br.readLine());
			br.readLine();   //ignore the next line that contains the num of columns, because we have that info already
			String[] ksArr = br.readLine().split(" ");  //For key signature
//			midi_instrument = Integer.parseInt(br.readLine());
			String[] midiInstr = br.readLine().split(" ");
			for (int i = 0; i < this.midiInstrumentArr.length; ++i) this.midiInstrumentArr[i] = Integer.parseInt(midiInstr[i]);
			this.chordBuilderMode = Integer.parseInt(br.readLine()) == 1 ? true : false;
			this.setMelodyChordModeCB(chordBuilderMode);

			String[] nextLine = br.readLine().split(" ");
			int colsPerMeasure = Integer.parseInt(nextLine[0]);
			int offSet = Integer.parseInt(nextLine[1]);
			nextLine = br.readLine().split(" ");
			double widthPerCell = Double.parseDouble(nextLine[0]);
			double heightPerCell = Double.parseDouble(nextLine[1]);
			
			this.setTempo(tempo);
			
			String line;
			this.drawMeasures(1, numCol);
			
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(" ");
				WrapperNote wn = new WrapperNote(Integer.parseInt(arr[0]));
				wn.setDuration(Integer.parseInt(arr[1]));
				wn.setIdx(Integer.parseInt(arr[2]));
				wn.setColorInt(Integer.parseInt(arr[3]));
				if (arr.length >= 5) {
					wn.setOrigColorInt(Integer.parseInt(arr[4]));
				}
				if (arr.length == 7) {
					wn.setVolume(Integer.parseInt(arr[5]));
					wn.setChannel(Integer.parseInt(arr[6]));
				}
				
				if (wn.getColIdx() < focusedScorePane.getNumCells())
//					focusedScorePane.createNote(wn, wn.getColIdx(), durationPerCell, false);
					focusedScorePane.createNote(wn, false);
			}
			this.changeKeySignature(new KeySignature(Integer.parseInt(ksArr[0]), Integer.parseInt(ksArr[1])));
			br.close();
//			changeInstrument(midi_instrument);
			this.changeAllInstruments(this.midiInstrumentArr);
			focusedScorePane.setActiveColumn(0);
//			this.chordBuilderMode = false;
//			if (chordBuilderMode) this.lockMelodyNotes(true);
			
			focusedScorePane.setNumColsPerMeasure(colsPerMeasure);
			focusedScorePane.setMeasureOffset(offSet);
			focusedScorePane.setWidthPerCell(widthPerCell);
			focusedScorePane.setHeightPerCell(heightPerCell);
			focusedScorePane.reDrawMeasure();
			
			updateInfoPane();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void modifyColumns(int num) {
		//Save the current notes, then make a new measure with the specified num of columns,
		//then populate it. IGNORE notes whose col indices exceed the new no. of cols.
		File file = new File("~tmpsave.TMP");
		saveToFile(file, false);  //The 2nd parameter here refers to updateInfoPane = false
		populate(file, num);
	}
	
	public void refresh(ScorePane focusedScorePane) {
		this.measurePaneAL = new ArrayList<ScorePane>();
		
		this.measurePaneAL.add(focusedScorePane);
//		this.linkMeasures(0);
		measuresHB.getChildren().clear();
		measuresHB.getChildren().addAll(measurePaneAL);
		this.focus(this.measurePaneAL.get(0), true, false);  //don't reset active column
	}
	
	private void saveToFile(File currSaveFile, boolean updateInfoPane) {
		ArrayList<WrapperNote> noteAL = new ArrayList<>();
		//Deselect all notes first
		focusedScorePane.deSelectAll();
		
        //Read from the measure (we assume a single measure only) and save each note as a Note object, 
        //along with column index, and duration per cell info (let's just save it as 1), and tempo,
        //and no. of cells in this measure.
		//NOTE: duration per cell here indicates the GCD variable in ReadMidi.java class. This GCD variable
		//      helps determine the total no. of cells needed. But when saving an existing music canvas,
		//      we already know the total no. of cells we have. So no need for a GCD variable.
        for (int row = 0; row < ScorePane.ROWS; ++row) {
        	noteAL.addAll(focusedScorePane.getAllNotesInRow(row));
        } //end for row
        System.out.println(noteAL); //testing
        int durationPerCell = 1;
        int tempo = PianoRollGUI.this.getTempo();
        PrintWriter writer;
		try {
			writer = new PrintWriter(currSaveFile, "UTF-8");
			writer.println(durationPerCell);
			writer.println(tempo);
			writer.println(focusedScorePane.getNumCells());
			writer.println(this.ks.getTonic() + " " + this.ks.getMode());
			for (int i = 0; i < this.midiInstrumentArr.length; ++i) {
				writer.print(this.midiInstrumentArr[i] + " ");
			}
			writer.println();
			writer.println(this.chordBuilderMode ? 1 : 0);
			writer.println(this.focusedScorePane.getColsPerMeasure() + " " + this.focusedScorePane.getOffset());
			writer.println(focusedScorePane.getWidthPerCell() + " " + this.focusedScorePane.getHeightPerCell());
			for (WrapperNote n : noteAL) {
				writer.println(n.getPitch() + " " + n.getDuration() + " " + n.getColIdx() + " " + 
							n.getColorInt() + " " + n.getOrigColorInt() + " " + n.getVolume() + " " + n.getChannel());
			}
			
            writer.close();
            if (updateInfoPane) {
            	updateInfoPane();
            	Alert conf = new Alert(AlertType.INFORMATION);
            	conf.setTitle("Save success");
            	conf.setContentText("Successfully saved to " + currSaveFile.getName());
            	conf.showAndWait();
            }
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} //end try/catch
	}
	
	private File saveAsDialog() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save As");
		fc.setInitialDirectory(new File("."));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DATA files (*.dat)", "*.dat");
        fc.getExtensionFilters().add(extFilter);
        File file = fc.showSaveDialog(this.primaryStage);
        return file;
	}
	
	private File loadDialog() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Load Project");
		fc.setInitialDirectory(new File("."));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DATA files (*.dat)", "*.dat");
        fc.getExtensionFilters().add(extFilter);
        return fc.showOpenDialog(this.primaryStage);
	}
	
	
	//Automatically scroll according to focused measure
	private void autoScrollPane(ScrollPane sp) {
		if (this.measurePaneAL == null || this.measurePaneAL.size() <= 1) sp.setHvalue(0);
		else if (this.focusedScorePane != null) {
			double x = this.focusedScorePane.getMeasureNum() / (double)(this.measurePaneAL.size()-1);
			sp.setHvalue(x);
		}
	}
	
	/**
	 * Autoscrolls the horizontal scrollbar, assuming a SINGLE measure, according to the current active column.
	 * @param sp
	 * @param activeCol
	 */
	private void autoScrollPane(ScrollPane sp, int activeCol) {
		//Fix the centering issue (update: done, more or less)
		
		//First get the width of the scrollpane, and width of the measure pane.
		double spWidth = this.sp.getWidth();
		double mpWidth = this.focusedScorePane.getWidth();
		
		//If the measure pane fits entirely within scrollpane, then there is no need to set any scroll value
		if (spWidth >= mpWidth) return;
		
		/* Figure out the situations where the scroll should be all the way to the left.
		 * This will occur if the current active column index is such that it would be <= half of the scrollpane's width.
		 * */
		double leftThreshold = spWidth/2;
		double leftThreshold_InTermsOfMeasurePane = leftThreshold / mpWidth;
		
		double currX = (double)activeCol / this.focusedScorePane.getNumCells();
		if (currX <= leftThreshold_InTermsOfMeasurePane) {
			sp.setHvalue(0);
			return;
		}
		
		/* Figure out the situations where the scroll should be all the way to the right. */
		double rightThreshold_InTermsOfMeasurePane = 1 - leftThreshold_InTermsOfMeasurePane;
		if (currX >= rightThreshold_InTermsOfMeasurePane) {
			sp.setHvalue(1);
			return;
		}
		
		/* If we get all the way here, the currX is somewhere between the left and right threshold.
		 * The scrollpane should be set in a way that is equidistant for each column index that remain
		 * between the left and right threshold. */
		int leftIdx = (int)Math.ceil(leftThreshold_InTermsOfMeasurePane * this.focusedScorePane.getNumCells());
		int rightIdx = this.focusedScorePane.getNumCells() - leftIdx;
		double step = 1.0 / (rightIdx - leftIdx); 
//		double x =  (double)activeCol / this.focusedScorePane.getNumCells();
		sp.setHvalue(step * (activeCol - leftIdx));
	}
	
	public void focus(ScorePane measurePane, boolean autoscroll, boolean setActiveColToZero) {
		if (measurePane != null) measurePane.setFocus();
		if (this.focusedScorePane != null) {
			if (focusedScorePane.equals(measurePane)) {}
			else {
				focusedScorePane.clear();
				focusedScorePane = null;
			}
		}
		focusedScorePane = measurePane;
		if (autoscroll) autoScrollPane(this.sp);
		if (focusedScorePane != null) focusedScorePane.setActiveColumn(setActiveColToZero ? 0 : this.getActiveColumn());
	}
	
	public void focus(ScorePane measurePane, boolean autoscroll) {
		focus(measurePane, autoscroll, true);
	}
	
	public void focus(ScorePane measurePane) {
		focus(measurePane, true);
	}
	
	
	private void playBack(ArrayList<WrapperNote> currNotesInCol, int currColIdx,
			ArrayList<WrapperNote> prevNotesInCol) {
		if (prevNotesInCol == null) {
			//don't bother computing note on or note off
		} else {
			for (WrapperNote note : prevNotesInCol) {
				if (note == null) continue;
				if (!focusedScorePane.isHeld(note.getPitch(), currColIdx-1, currColIdx)) {
					mChannels[note.getChannel()].noteOff(note.getPitch());
				}
			} //end for
		} //end if/else
		
		for (WrapperNote note : currNotesInCol) {
			if (note == null) continue;
			if (!focusedScorePane.isHeld(note.getPitch(), currColIdx-1, currColIdx)) {
				mChannels[note.getChannel()].noteOn(note.getPitch(), note.getVolume());
			}
			
		}
		try {
			Thread.sleep(MidiFile.MULT * this.tempo); // wait time in milliseconds to control duration. The constant is arbitrary.
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Playback the chords, given the current notes (pitch values) in column and prev notes in column.
	 * This should help determine the note on and note off.
	 * @param currNotesInCol
	 * @param prevNotesInCol
	 */
//	private void playBack(ArrayList<Integer> currNotesInCol, int currColIdx, 
//						  ArrayList<Integer> prevNotesInCol) {
////		cancelTask = false;
//		if (prevNotesInCol == null) {
//			//don't bother computing note on or note off
//		} else {
//			for (Integer pitch : prevNotesInCol) {
//				if (pitch == null) continue;
//				if (!focusedScorePane.isHeld(pitch, currColIdx-1, currColIdx)) {
//					mChannels[0].noteOff(pitch);
//				}
//			} //end for
//		} //end if/else
//		
//		for (Integer pitch : currNotesInCol) {
//			if (pitch == null) continue;
//			if (!focusedScorePane.isHeld(pitch, currColIdx-1, currColIdx)) {
//				mChannels[0].noteOn(pitch, MidiFile.MAX_VOL);
//			}
//			
//		}
//		try {
//			Thread.sleep(MidiFile.MULT * this.tempo); // wait time in milliseconds to control duration. The constant is arbitrary.
//		} catch (InterruptedException ie) {
//			ie.printStackTrace();
//		}
//	}
	
	/**
	 * Play only the current column, then move active column forward or backward
	 * @param advanceColForward
	 */
	private void playBackCurrCol(boolean forward) {
		this.currColIdx = this.getActiveColumn();
//		ArrayList<Integer> currNotesInCol = this.focusedScorePane.getAllPitchesInColumn(currColIdx, true);
		ArrayList<WrapperNote> currNotesInCol = this.focusedScorePane.getAllNotesInColumn(currColIdx, true);
		this.playBack(currNotesInCol, currColIdx, null);

		for (WrapperNote note : currNotesInCol) {
			if (note == null) continue;
			mChannels[note.getChannel()].noteOff(note.getPitch());
		}
		if (forward) this.advanceCol(forward);
	}
	
	private void playBack(ScorePane measurePane, int colIdx) {
		if (measurePane == null) {
//			cancelTask = false;
			this.disableDuringPlayback(false);
			return;
		}
		this.focus(measurePane);
		
		this.currColIdx = colIdx;
		measurePane.setActiveColumn(colIdx);
//		this.prevAL = null;
		this.prevNotesAL = null;
		
//		this.currAL = measurePane.getAllPitchesInColumn(currColIdx, true);
		this.currNotesAL = measurePane.getAllNotesInColumn(currColIdx, true);
		
		PianoRollGUI.this.disableDuringPlayback(true);
		playBackThread = new MyRunnable(currColIdx);
		new Thread(playBackThread).start();
	}
	
	private void playBack(ScorePane measurePane) {
		playBack(measurePane, 0);
	}
	
	public void playBack(boolean fromBeginning) {
		if (fromBeginning) {
			playBack(focusedScorePane);
		} else {
			playBack(focusedScorePane, this.getActiveColumn());
		}
	}
	public void stopPlayback() {
		playBackThread.cancel();
	}
	
	//Playback from given measure to the end unless stopped (with ESC key)
//	private void playBack(MeasurePane measurePane) {
//		if (measurePane == null) {
//			cancelTask = false;
//			this.disableDuringPlayback(false);
//			return;
//		}
//		
//		this.focus(measurePane);
//		ChordSequence cs = measurePane.getChordSeq();
//		//Implement playback in separate thread so can stop anytime
//		//Disable the pianoRoll while the separate thread is running
//		Task<Void> task = new Task<Void>() {
//
//			@Override
//			protected Void call() throws Exception {
//				midifile.play(cs, midi_instrument, tempo);
//				Platform.runLater(new Runnable() {
//					public void run() {
//						PianoRollGUI.this.disableDuringPlayback(false);
//					}
//				});
//				return null;
//				
//			}
//		};//end Task
//		
//		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//
//			@Override
//			public void handle(WorkerStateEvent arg0) {
//				if (cancelTask == false) {
//					playBack(measurePane.getNext());
//				} else {  //cancelTask = true.
//					//we're done. re-initialize to false.
//					cancelTask = false;
//				}
//			}
//		});
//		
//		
//		PianoRollGUI.this.disableDuringPlayback(true);
//		new Thread(task).start();
//	}
	public File getCurrSaveFile() {
		return this.currSaveFile;
	}
	
	private void disableMenuItemsDuringPlayback(boolean playBackInProgress) {
		for (Menu menu : menuBar.getMenus()) {
			for (MenuItem menuitem : menu.getItems()) {
				if (menu.getText().equalsIgnoreCase("View") || menuitem.getText().equalsIgnoreCase("Auto-loop")) continue;
				menuitem.setDisable(playBackInProgress);
				if (menuitem.getText().equalsIgnoreCase("Stop")) {
					menuitem.setDisable(!playBackInProgress);
				}
			}
		}
		scene.setOnKeyPressed(playBackInProgress ? null : this.keyPressHandler);
	}
	
	private void disableDuringPlayback(boolean disable) {
//		this.sp.setDisable(disable);
		this.focusedScorePane.setDisable(disable);
//		this.sp.setHbarPolicy(disable ? ScrollBarPolicy.NEVER : ScrollBarPolicy.AS_NEEDED);
		disableMenuItemsDuringPlayback(disable);
		this.infopane.disableBtns(disable);
	}

	public void deleteMeasures(int fromIndex, int toIndex) {
		int indices_range = toIndex - fromIndex + 1;
		assert indices_range > 0;
		if (indices_range <= 0) {
			System.out.println("All measures are already deleted. Nothing to do. Returning...");
		}
		for (int i = 0; i < indices_range; ++i) {
			this.measurePaneAL.remove(fromIndex);
		}
		
		if (this.measurePaneAL.isEmpty()) {
			//Cannot focus on any measure as they have all been deleted.
			focusedScorePane = null;
		} else {
			//Try to put focus on the next measure. If this isn't possible, focus the prev measure.
			ScorePane measureToFocus = null;
			if (fromIndex - 1 >= 0) {
				measureToFocus = this.measurePaneAL.get(fromIndex - 1);
			} else if (fromIndex < this.measurePaneAL.size()) {
				measureToFocus = this.measurePaneAL.get(fromIndex);
			}
			if (measureToFocus == null) {
				throw new RuntimeException("Can't find measure to focus on after deleting one or more measure!");
			}
			this.focus(measureToFocus, true, true);
			
			//Re-order measure numbers
			for (int i = Math.max(0, fromIndex-1); i < this.measurePaneAL.size(); ++i) {
				linkMeasures(i);
			}
		} //end if measurePaneAL is empty / else
		refreshMeasures();
	}
	
	public int getOctave() {
		return this.octave;
	}
	
	//Update: Use setTempo() setter method instead
//	private void changeTempo(int tempo) {
//		this.tempo = tempo;
//	}
	
	/**
	 * Use keyboard to play notes
	 * @author Yury Park
	 *
	 */
	private class KeyPressHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent k) {
//			System.out.println(k.getCode().toString());			
//			System.out.println(PITCH_MAP.get("Z") + Harmonizer.SCALE * octave);
			if (k.getCode().isDigitKey()) {
				System.out.println(k.getCode().getName());
				if (k.getCode().getName().startsWith("Numpad")) return; //numpad digits will be disabled from changing note color
				try {
					focusedScorePane.colorSelectedNotes(Integer.parseInt(k.getCode().getName()));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (k.getCode() == KeyCode.MINUS) {
				focusedScorePane.colorSelectedNotes(10);
			} else if (k.getCode() == KeyCode.EQUALS) {
				focusedScorePane.colorSelectedNotes(11);
			}
//			if (k.getCode() == KeyCode.SHIFT) {
//				octave += 1;
//				if (PITCH_MAP.get("SLASH") + Harmonizer.SCALE * octave > PianoRollGUI.MAX_PITCH) {
//					octave -= 1;
//				}
//			} else if (k.getCode() == KeyCode.CONTROL) {
//				octave -= 1;
//				if (PITCH_MAP.get("Z") + Harmonizer.SCALE * octave < PianoRollGUI.MIN_PITCH) {
//					octave += 1;
//				}
//			} else if (k.getCode() == KeyCode.BACK_SPACE) {
//				if (focusedScorePane != null) focusedScorePane.backSpace();
//			} else if (k.getCode() == KeyCode.DELETE && focusedScorePane != null) {
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
//				PianoRollGUI.this.playBack(focusedScorePane);
//				
//			}
//			if (k.getCode() == KeyCode.DIGIT1) {
//				PianoRollGUI.this.chordBuilderMode = false;
//			} else if (k.getCode() == KeyCode.DIGIT2) {
//				PianoRollGUI.this.chordBuilderMode = true;
//			}
			//Have a separate GUI for tempo changes later. For now just testing
//			else if (k.getCode() == KeyCode.ADD) {
//				PianoRollGUI.this.changeTempo(Math.max(1, tempo - 1));
//				System.out.println("Tempo changed to " + tempo);
//			} else if (k.getCode() == KeyCode.SUBTRACT) {
//				PianoRollGUI.this.changeTempo(Math.min(48, tempo + 1));
//				System.out.println("Tempo changed to " + tempo);
//			}
			
			if (PITCH_MAP.get(k.getCode().toString()) != null) {
				Integer pitch = PITCH_MAP.get(k.getCode().toString()) + Harmonizer.SCALE * octave;
				if (pitch != null && PianoRollGUI.this.focusedScorePane != null) {
					if (k.isAltDown()) {
						//Don't play, delete note from the measure's notation if exists
						if (focusedScorePane != null) {
							System.out.println("Deleting pitch...");
							focusedScorePane.deletePitch(pitch);
						}
					} else if (k.isControlDown() || k.isShiftDown()) {
						return;
					}
					else if (chordBuilderMode) {  //just notate/play pitch, don't move the column bar forward
						focusedScorePane.play(pitch);
					} else {  //move column bar forward then notate/play pitch
						focusedScorePane.play(pitch);
						if (focusedScorePane.advanceActiveColumn(true) == false) {
							if (nextMeasure(focusedScorePane) != null) {
								PianoRollGUI.this.focus(nextMeasure(focusedScorePane));
							}
						}	
					}
				}
				
			} //end if(PITCH_MAP...)
			
			updateInfoPane();
		} //end public void handle
	} //end private class KeyPressHandler
	
	private void advanceCol(boolean forward) {
		if (focusedScorePane == null) return;
		if (focusedScorePane.advanceActiveColumn(forward) == false) {
//			if (nextMeasure(focusedScorePane) != null) {
//				PianoRollGUI.this.focus(nextMeasure(focusedScorePane));
//			}
			this.goToNextOrPrevMeasure(forward, forward);
		}
	}
	
	public int getFocusedMidiChannel() {
		return focusedMidiChannel;
	}
	
	public void setFocusedMidiChannel(int ch) {
		if (ch < 0 || ch >= mChannels.length) throw new RuntimeException("Channel no. is out of range.");
		focusedMidiChannel = ch;
	}
	
	//TODO (DONE) implement save / load later
	//TODO (DONE) modify the column rectangle and how it moves on keytyped -- notate in place THEN move forward, and
	//     also have the rectangle default to the first cell whenever a new measure is focused, as well as
	//     when first initialized, have it appear in the first cell of the first measure
	//TODO (DONE) implement chord editor - have the red column rectangle stay in place while you play more keys
	//TODO (DONE) implement mouse-drag for the same note holding for multiple durations
	//TODO (DONE) minor visual changes
	//TODO (DONE) complete Chord_NoLimit.java for building chords of varying sizes (e.g. not limited to tetrads) and durations
	//     (i.e. not every note in a chord has to have the same duration)
	//TODO (DONE) implement real-time playback support for individual notes held over duration
	//TODO (DONE) make it possible to stop the real-time playback at any time (instead of after each measure)
	//TODO (DONE) have option to "decrease" or "increase" no. of columns without affecting existing note rectangles
	//TODO (DONE) change instrument for playback
	//TODO (DONE) implement Information Pane for displaying/changing playback instrument, changing tempo, current column, no of cells per measure, playback/stop button etc
	//TODO (DONE) File menu for various tasks, set keyboard shortcuts as accelerator and remove redundant code from KeyPressHandler
	//TODO (DONE) have a way to separate the melody and make it non-editable
	//TODO (DONE) have the save/load methods also save information re: chordBuilderMode == true or not, and load accordingly
	//TODO (DONE) fix toggle chordbuilder mode by not highlighting non-melody notes that are held over longer durations	
	//TODO (DONE) Improve scrollpane auto-scrolling so that it centers on the current active column
	//TODO (DONE) transpose pane
	//TODO (DONE) more visual changes to the color of cells - at least 2 diff colors toggling between every adjacent row
	//TODO (DONE) Enable scrollbar during playback, just don't allow any editing
	//TODO (DONE) make the stage a bit bigger during initialization
	//TODO (DONE) export to MIDI option
	//TODO (DONE) ability to insert / delete specified no. of columns at particular position - InsertOrDeleteCellsPane (complete this)
	//TODO (DONE) ability to select with mouse (MeasurePane class)
	//TODO (DONE) get rid of menu choices re: measure insert / delete
	//TODO (DONE) fix bug when extending note duration more than once
	//TODO (DONE) implement multiple note duration edits when using selection rectangle
	
	//updated as of 4.4.2018:
	//TODO (DONE) zoom out / in feature, and menu item re same
	//TODO (DONE) while playback, have the "VIEW" menu options open
	//TODO (DONE) have separation bars instead of discrete measures
	//TODO (DONE) update InfoPane class re: number of cells per measure and offset
	//TODO (DONE) rename MeasurePane to ScorePane
	//TODO (DONE) have a menu for setting cells per (sub)measure, and measure offset
	//TODO (DONE) display no. of cells per measure, without explicitly displaying measures discretely (use thick bar etc. instead)
	//TODO (DONE) Function re: let the user specify beginning and ending bar, and return the information 
	//			  contained in the range of bars in the form of 2D array of colors
	//TODO (DONE) select note by mouse clicking left, delete note by mouse clicking with CTRL
	//TODO (DONE) fix bug re: deleteSelectedNotes() in ScorePane not deleting notes that aren't inside grey rectangle
	//TODO (DONE) Enable user to color selected notes according to whatever preference using 0-9, -, = keys?
	//TODO (DONE) edit WrapperNote class to also save note's color.
	//TODO (done for now, some bugs though) complete ReadMidi.java for reading in midi files and populating the pianoroll accordingly
	//TODO (done for now) implement ability to save from pianoroll to MIDI, and vice versa.
	
	//update as of 4.12
	//TODO (DONE) make it possible to select a section by drawing a rectangle with mouse?
	//TODO (DONE) the concept of time signature has been sort of replaced by just the no. of cells per measure at the moment.
	//     Consider whether to implement time signatures at all, whether it's relevant, etc.
	//TODO (DONE) get rid of grey shaded selection rectangle. Just adds confusion. Instead use selectionMouseRect in its stead.
	//TODO (DONE) fix bug re: notes color changing to default when zooming in/out
	//TODO (DONE) fix bug with toggle melody/chord mode, and bugs after zoom in / out
	//TODO (DONE) make up separate RectangleNote class, ColorIntMap and fix all dependencies
	//TODO (DONE) have origColorInt attribute for WrapperNote for save / load
	//TODO (DONE) do a search everywhere for "WrapperNote" and edit it to also account for its color
	//TODO (DONE) when doing stuff like transposing notes, extending, zooming in/out, or creating notes (createNote and createRect methods in ScorePane)
	//     take account of the rectangle's color whenever applicable
	//TODO (DONE) have save / load / also save info re: note colors and original colors
	//TODO (DONE) use mouse click instead of mouse press for most notation actions
	//TODO (DONE) don't allow notation or extending note by mouse if mouse left button pressed and dragged away;
	//            only allow notation if mouse press and mouse release occur on the same cell
	//TODO (DONE) move / copy selected notes by dragging with mouse, (Copy if ctrl down, otherwise move)
	//TODO (DONE) when moving or copying, reset notes to original spot if their new spots would overwrite another existing note
	//TODO (DONE) when moving or copying, preserve all notes' attributes, including colors
	//TODO (DONE) Menu items for cut / copy / paste selected notes (in addition to mouse dragging, which is already implemented)
	//TODO (DONE) when selecting notes with mouse, play it
	//TODO (DONE) Fix bug with moving notes by mouse creating weird duplicate notes after zooming in or out
	//TODO (DONE) when shift-left clicking a note, allow the note to be curtailed in length
	//TODO (DONE) when shift-left clicking a selected note, allow that note and all other selected notes to be curtailed in length
	//TODO (DONE) Ctrl-A to select all notes
	//TODO (DONE) make it possible to zoom out more
	//TODO (DONE) re-create some .dat files that we lost, maybe import from midi?
	//TODO (DONE) save / load also take care of info re cells per measure, measure offset, heightpercell, widthpercell...
	//TODO (DONE) insert cells / delete cells bug fix
	//TODO (DONE) Make RectangleNOtes be comparable, sortable
	//TODO (DONE) pattern search (exact match only)
	//TODO (DONE) menu option for just playing back a single column via pressing W or E, where E advances column by one
	//TODO (DONE) implement insertion of selected notes (automatically shifting columns to the right)
	//TODO (DONE) implement insertion of a PATTERN of selected notes, with offset and column range
	//TODO (DONE) ability to mute selected notes with color-code gray, and have them stay silent during playback
	//TODO (DONE) getAllNotesInColumn() should be renamed to getAllPitchesInColumn, and a separate method by the former name
	//     should be created that returns an arraylist of note objects
	
	
	//Update as of 4.19
	//TODO (DONE) Create CustomFunctions class (will become API for end user)
	//TODO (DONE) create CustomFunctions pane, a GUI for displaying all the user-created functions
	//TODO (DONE) create initial helper methods in CustomFunctions
	//TODO (DONE) edit CustomFunctions class to gather other information about the note such as mute? melody? etc.
	//TODO (DONE) Create a hashmap that binds indices or Strings to the user-editable functions themselves,
	//            so that they can be invoked directly from the GUI
	//TODO (DONE) allow menu access of CustomFunctions pane from PianoRollGUI
	//TODO (DONE) Create SuperCustomFunctions class (superclass for CustomFunctions), needed for dynamic reloading
	//            of CustomFunctions class without restarting the entire program
	//            (See CustomFunctionsPane's reload() method and comments therein for more info)
	//TODO (DONE) Allow for dynamic reloading of CustomFunctions, as mentioned above.
	
	
	//Update as of 4.26.2018
	//TODO (DONE) deal with colorintmap's two different color maps. Kind of confusing right now.
	//TODO (DONE) create focusedMidiChannel variable, for real-time playback instrument while notating with keyboard or mouse
	//TODO (DONE) make up a changeAllInstruments() method that sets ALL channels to a specified instrument
	//TODO (DONE) change instrument method should also be modified to change a specific mchannels[i] value
	
	//TODO (DONE) Infopane should be updated to reflect that we now have an array of 16 instruments, one per channel
	//TODO (DONE) edit save / load so that it now saves 16 instruments in all the midi channels, instead of 1 instrument
	//TODO (DONE) each note (Note and RectangleNote) class should have attribute var re: the midi channel it belongs to,
	//			  not the instrument
	//TODO (DONE) Make it possible to edit each note's volume / midi channel in custom functions class
	//TODO (DONE) MAKE IT POSSIBLE TO AUTO-LOOP ONCE PLAYBACK REACHES THE END
	//TODO (DONE) the concepts of "locked" melody and explicit mute function for notes are now gone, as it presents
	//            too much unnecessary complication re: coloring and what notes can be edited when and how.
	//            now, the only special color is BLACK, to indicate "selected" notes. But now any note is editable,
	//            and any color can be set to indicate "melody" or otherwise. As for muting, setMute() is gone. Instead,
	//            we can just use setVolume() method to set a note to 0 volume. We still have isMute() method
	//            to check to see if a note has 0 volume.	
	//TODO (DONE) clean up CustomFunctions, divide into 3 sections: 
	//            1) template for GUI display, 2) custom functions, 3) utility functions
	
	//TODO (DONE) midi_instrument variable should now be changed to array of 16 instruments (midiInstrumentArr)
	//TODO (DONE) in Infopane, clicking on the listview of channel updates the current focused midi channel	
	//TODO (DONE) consider allowing separate volume and /or instrumentation for individual notes (edit RectangleNote & either WrapperNote or Note classes)
	//TODO (DONE) when selecting a note, play the sound according to the instrument channel the note belongs to
	//TODO (DONE) when playing (notating) a note via mouse or keyboard, play sound according to the instrument channel
	//            which is currently focused (focusedMidiChannel variable in this class)
	//TODO (DONE) enable toggling auto-loop on or off during playback
	//TODO (DONE) implement bitset representation of note features in a new BitSetUtil class and test the methods
	
	//TODO (DONE) add checkbox to the melody/chord mode menuitem
	//TODO (DONE) improve the createNote() and createRect() method for efficiency in ScorePane
	//TODO (DONE) minor edit to resetAllCopyRelatedVars() method in ScorePane
	//TODO (DONE) After selecting and dragging a note, the notes' channel defaults to whatever is currently the focused channel,
	//            despite the fact the notes were originally assigned a different channel. Fix this.
	//TODO (DONE) change instrument pane should now allow for one or all channels to be assigned the selected instrument
	//TODO (DONE) make it possible to assign different channel to selected notes using menuoptions:
	//            by clicking on the channel listview in infopane while having notes selected.
	//TODO (DONE) upon selecting a channel in the infopane, keep that channel selected upon refreshing the infopane
	//TODO (DONE) experiment with instrument changes
	//TODO (DONE) set max. number of columns in a given score (for purposes of setting limits on the bitset 
	//            representation of notefeatures, and to ensure we set aside enough no. of bits for the representation of
	//            things like note length, not to exceed the max. number of columns)
	//TODO (DONE) edit note insert, and column insert methods in the menu, to ensure we can't go above max. number of columns in any case
	//            (we want to set arbitrary limit on how long a score can get, and ensure no method breaks this limit)
	//TODO (DONE) complete setVolumeGivenColIndexRange() method in custom functions class, maybe after
	//     implementing the bitset representation of note features
	//TODO (DONE) make up a short looping piece and experiment with volume changes
	//TODO (DONE) Test & make sure we can save/load properly with all new note information (instrument channel, colors, volume...)
	//TODO (DONE) Make it possible to construct NoteFeatures either from RectangleNote or BitSet, and to
	//            convert from features to bitset and vice versa, and also to be represented in binaryString format.
	//TODO (DONE) implement some mod functions into NoteFeatures constructor with BitSet as parameter, to protect
	//            as much as possible from creating invalid notes
	//            (e.g. notes that are out of column range, have color values that are out of ColorIntMap's valid range...)
	//TODO (DONE) make up custom (utility) function for notating a note given a bitset
	//TODO (DONE) readme file for customfunctions
	
	
	
	//Update as of 5.2.2018
	//TODO (DONE) With John's help, clean up CustomFunctions and move many of the core methods to SuperCustomFunctions
	//TODO (DONE) With John's help, clean up the process by which the end user can add custom functions.
	//TODO (DONE) when saving, deselect every note first
	//TODO (DONE) debug mute / unmute command, set aside new variable called origVolume for this purpose
	//TODO (DONE) clean up NoteFeatures such that stencilbits is the only attribute that can be explicitly
	//     converted and returned as a bitset object or binary string. If the end user wants to play around
	//     with other attributes in a bit format (not recommended I don't think, due to the unexpected behaviors
	//     this can cause, e.g. modifying the column index attribute so that it's out of range of the score pane).
	//     s/he can create custom methods to do so.
	//TODO (DONE) stencilBits for NoteFeatures, set/write
	//TODO (DONE) read/write function for each feature in NoteFeatures
	//TODO (DONE) getTempo/setTempo should be a custom (protected) method	
	//TODO (DONE) stop/start playback be a custom (protected) method as well?
	//TODO (DONE) get current active column should be a custom (protected) method
	//TODO (DONE) debug changeInstrument() method in PianoRollGUI, some instruments aren't loading correctly.
	//TODO (DONE) edit changeInstrument() so that both the bank and the patch # are taken into account
	//TODO (DONE) realize that drum kit instrument is fixed to channel index 9 (see https://en.wikipedia.org/wiki/General_MIDI)
	//TODO (DONE) update infopane re: above fixed drum channel
	//TODO (DONE) Tutorial videos
	//TODO (DONE) make up a function that composes a random dance track with multiple instruments
	
	//TODO (DONE) create a ColorEnum class for binding certain "special" colors for notes, such as GRAY for mute
	
	//TODO there is no UI option for the end user to set note volume (other than mute/unmute)
	
	//TODO color scheme between CustomFunctions and UI interface commands is messed up
	//     (e.g. unmute doesn't change colors back, default colors like mute=gray doesn't always work etc.
	//     Consider doing away with custom colors altogether?
	//TODO have a "lock entire pianoroll from editing" or similar option
	//TODO do we really need a WrapperNote class? Can't we just use RectangleNote for everything?
	
	//TODO consider re-doing color RGB to Int map, to account for all possible colors 255 x 255 x 255
	//TODO alert pane for when end users make a mistake, esp while using custom functions pane
	//TODO consider editing Note / RectangleNote / WrapperNote classes for less redundancy.
	//     (perhaps have WrapperNote be a subclass, and have RectangleNote contain a Note attribute?)
	//TODO consider using code re: command line build of custom functions, another thread
	//     https://stackoverflow.com/questions/8496494/running-command-line-in-java
	//     https://stackoverflow.com/questions/16137713/how-do-i-run-a-java-program-from-the-command-line-on-windows
	//TODO debug export to midi problem 
	//TODO ex. Show how to change color of all notes for which stencilbit index 3 == 1, turn it green
	//TODO functionality to save to defaultcustomfunctions and load from it in event of a catastrophe
	//TODO expeirment with converting individual packages to jars except for custom functions class
	
	//TODO button in the custom pane for building from source
	//TODO button for save/load, implement it in custom pane and link the function to supercustom
	
	
	
	
	
	
	
	
	
	
	
	//TODO experiment with doing some bit operations between notes or on a single note, and notate it on scorepane 
	 
	
	//TODO whenever they're 4 notes played together, color code them depending on what type of chord it is (minor, major, etc.)
	//TODO for above, also depending on key signature, based on rhythm, tonality
	//TODO then, use the above to change / modify / delete / insert some notes
	//     e.g. whenever there are 2 minor chords in a row, change the 2nd chord to a major?
	//TODO e.g. a pane in which user can type in some syntax, change notes' color accordingly
	//TODO color by a sequence of rules
	//For every value of some variable T, given a 2D array of colors, we can think of inserting and/or coloring
	//TODO T = time (columns), F = frequency, or color of notes
	
	//TODO once a user searches / filters out a subset of a given score, the result should be saved as a separate visual
	
	//TODO pattern search (implement similarity measures and/or transpositions, not just exact match)
	//TODO consider saving melody / chords separately
	//TODO have user press spacebar duing playback and draw bars that way via averaging
	//TODO Let users code preferences / scripts / repeats / notes, harmonics
	//TODO Think about ways to let users "compose via script"
	//TODO rule-based (?) script / syntax that can change visualization colors, querying one note / chord at a time and
	//     its features (duration, pitch, volume, etc.) and neighborhood thereof,
	//     ways to insert, delete, or change color
	//     voice leading / musical flow
	//TODO paint rows in yellow / tonic / pertaining to key signatures and notes in that scale? 
	//TODO consider using more colors to distinguish between notes / patterns in terms of their importance / structure / history
	//TODO color - dissonant / consonant combinations?
	//TODO color for major/minor chords
	
	//TODO implement warning message when a chord is invalid (i.e. more than 4 (later 5) notes per column, a chord has notes of diff duration, etc)
	
	
	//TODO think more on machine learning process: have another mode where you go thru each chord and have user assign / modify a score
	//from 1 (worst) thru 5 (best), for a given musical style profile.
	//Machine learning takes place by doing a plurality vote on all those scores for the same given musical context.
	//If there is a tie, pick one at random OR do an average, then round to nearest integer?
	/* TODO machine learning vector representation of a chord:
	 * Interval notation of current and previous chords
	 * - a chord should be inverted to its "simplified" form, where the lowest note of that chord is
	 *   NEAREST TO and >= the tonic note of the key signature. This treats certain inversions as equal for sake of simplicity.
	 * - chord interval notation is represented as one-hot vector. SO instead of say [2, 4, 6], we'll have say 12 columns
	 *   representing each semitone and each column will have 0 or 1 in it.
	 * - Perhaps another 12 columns for saving the distance between the lowest note of the chord and the tonic note of the key signature
	 * - Another 12 + 12 columns for saving the same info re: previous chord? 
	 * 
	 * So, total of 48 columns for representing prev. and curr. chord
	 * 
	 * Key signature (12 columns, one-hot encoding - possible for KS to change during a piece)
	 * Major / Minor (consider doing away with distinguishing between harmonic/melodic/natural minors for simplicity)
	 * - 2 columns, one-hot encoding (possible to change during a piece)
	 * User-defined emotion arcs (happy, sad - by default set to happy if major and sad if minor, but user can change?)
	 * - 2 columns
	 * Musical style (tonal, atonal, etc.)
	 * - X columns, where X = no. of musical styles considered
	 * All in all we're looking at 64 + X feature columns? */
	// idea:if inserting, then from that column onward, offset the position of all notes by how many columns are being inserted,
	// then save it with that many total columns, then load / repopulate.
	// if deleting, then get rid of any notes within that column range, and for the column afterwards, offset the position of all
	// notes leftward, then save it and load/repopulate
		
	/**
	 * Confirmation dialog before deleting all notes in a measure
	 */
	private void confirmDelAllNotesInMeasure() {
		if (focusedScorePane == null) return;
		//Alert is a new built-in class for JavaFX
		Alert alert = new Alert(AlertType.CONFIRMATION, "Delete all notes in the selected measure?", ButtonType.YES, ButtonType.NO);
		alert.showAndWait();
		if (alert.getResult() == ButtonType.YES) {
			this.focusedScorePane.deleteAllNotes();
		}		 
	}
	
	public String getInstrument(int channel) {
		return this.instrumentArr[this.midiInstrumentArr[channel]].toString();
	}
	
	public int getInstrumentInt(int channel) {
		return this.midiInstrumentArr[channel];
	}
	
	public Instrument[] getInstrumentArr() {
		return this.instrumentArr;
	}
	
	public int getNumMidiInstruments() {
		return this.instrumentArr.length;
	}
	
	public int getNumMidiInstrumentChannels() {
		return this.midiInstrumentArr.length;
	}
	
	/**
	 * Return the i-th midi instrument value (int value)
	 * @param i
	 * @return
	 */
	public int getMidiInstrumentInt(int i) {
		return this.midiInstrumentArr[i];
	}
	
	public void playSound(int pitch) {
		playSound(pitch, this.focusedMidiChannel);
	}
	
	public void playSound(int pitch, int channel) {
		mChannels[channel].noteOn(pitch, 120);//play note number with specified volume 
		try { 
			Thread.sleep(100); // wait time in milliseconds to control duration
		} catch (InterruptedException ie) {

		}
		mChannels[channel].noteOff(pitch);//turn off the note
	}
	
	public void goToNextOrPrevMeasure(boolean nextMeasure, boolean setColIndexToZero) {
		if (nextMeasure) {
			ScorePane next = nextMeasure(focusedScorePane);
			if (next != null) focus(next, true, setColIndexToZero);
//			autoScrollPane(sp);
		} else {
			ScorePane prev = prevMeasure(this.focusedScorePane);
			if (prev != null) focus(prev, true, setColIndexToZero);
//			autoScrollPane(sp);
		} 
	}
	
	public void goToNextOrPrevMeasure(boolean nextMeasure) {
		goToNextOrPrevMeasure(nextMeasure, true);
	}
	
	public void updateInfoPane() {
		this.infopane.update(this.focusedMidiChannel);
	}
	
	public int getNumColsPerSubMeasure() {
		return this.focusedScorePane.getColsPerMeasure();
	}
	
	//Set all (16) midi channels
	public void changeAllInstruments(int[] instruments) {
		for (int i = 0; i < mChannels.length; ++i) {
			this.changeInstrument(instruments[i], i);
		}
	}
	
	public void changeAllInstruments(int instrument) {
		for (int i = 0; i < mChannels.length; ++i) {
			this.changeInstrument(instrument, i);
		}
	}
	
	public void changeInstrument(int instrument) {
		this.changeInstrument(instrument, focusedMidiChannel);
	}
	
	public void changeInstrument(int instr, int midichannel) {
		System.out.println(this.instrumentArr[instr].toString());
		String instrStr = this.instrumentArr[instr].toString();
		if (instrStr.startsWith("Drumkit")) {
//			midichannel = 10;
			mChannels[midichannel].programChange(instr);
			this.midiInstrumentArr[midichannel] = instr;
			updateInfoPane();
			return;
		}
		int bankNo = Integer.parseInt(instrStr.substring(instrStr.indexOf("bank #") + "bank #".length(), instrStr.indexOf(" preset #")));
		int presetNo = Integer.parseInt(instrStr.substring(instrStr.indexOf("preset #") + "preset #".length()));
		mChannels[midichannel].programChange(bankNo, presetNo);
		this.midiInstrumentArr[midichannel] = instr;
		updateInfoPane();
		System.out.println(this.getFocusedMidiChannel());
	}
	
	private ScorePane prevMeasure(ScorePane currMeasure) {
		return (currMeasure == null ? null : currMeasure.getPrev());
	}
	private ScorePane nextMeasure(ScorePane currMeasure) {
		return (currMeasure == null ? null : currMeasure.getNext());
	}
	
	public void drawMeasures(int totalNumMeasures, int numCellsPerMeasure) {
		drawMeasures(totalNumMeasures, new TimeSignature(1,4), numCellsPerMeasure, this.measuresHB);
	}
	
	public void drawMeasures(int numCellsPerMeasure) {
		drawMeasures(this.getTotalNumMeasures(), numCellsPerMeasure);
	}
	
	private void drawMeasures(int totalMeasures, TimeSignature ts, int multiple, HBox measuresHB) {
		if (totalMeasures <= 0) {
			System.out.println("No measures to draw. Returning...");
			return;
		}
		this.measurePaneAL = new ArrayList<ScorePane>();
		
		//Constructor using the prev and next variables.
		for (int i = 0; i < totalMeasures; ++i) {
			ScorePane curr = new ScorePane(ts, multiple, i, this, null, null); //Initialize MeasurePane, a GridPane subclass
			this.measurePaneAL.add(curr);
//			if (i > 0) {
//				curr.setPrev(measurePaneAL.get(i-1));
//				measurePaneAL.get(i-1).setNext(curr);
//			}
//			linkMeasures(i);
		}
//		this.measuresHB.getChildren().clear();
//		this.measuresHB.getChildren().addAll(measurePaneAL);
//		this.focus(this.measurePaneAL.get(0));
	}
	
	
	
	/**
	 * Inserts a total of k measures either before or after the currently focused measure.
	 * @param k
	 * @param beforeFocusMeasure
	 */
	public void insertMeasures(int k, boolean beforeFocusMeasure) {
		if (this.focusedScorePane == null) {
			System.out.println("Focused measure is null! Inserting measure(s) and setting focus on the first measure created...");
			assert this.measurePaneAL.isEmpty();
			if (!this.measurePaneAL.isEmpty()) {
				throw new RuntimeException("Focused measure is null, but measurePaneAL isn't empty! Something is wrong...");
			}
			for (int i = 0; i < k; ++i) {
				this.measurePaneAL.add(new ScorePane(this.ts, this.multiple, i, this, null, null));
				linkMeasures(i);
			}
			//focus first measure
			this.focus(this.measurePaneAL.get(0));
		} else {
			int currIndex = this.focusedScorePane.getMeasureNum();
			
			//Insert k measures before or after the current focused measure, as the case may be
			for (int i = 0; i < k; ++i) {
				this.measurePaneAL.add(beforeFocusMeasure ? currIndex : currIndex+1,
						new ScorePane(this.ts, this.multiple, -1, this, null, null));
			}
			
			//We need to fix the measure Num variable for each measure we added, plus all measures after it
			for (int i = beforeFocusMeasure ? currIndex : currIndex + 1; i < this.measurePaneAL.size(); ++i) {
	//			MeasurePane curr = this.measurePaneAL.get(i);
	//			if (i > 0) {
	//				curr.setPrev(measurePaneAL.get(i-1));
	//				measurePaneAL.get(i-1).setNext(curr);
	//			}
	//			curr.setMeasureNum(i);
				linkMeasures(i);
			}
			
			//Testing (comment out when done)
			for (int i = 0; i < this.measurePaneAL.size(); ++i) {
				System.out.println(measurePaneAL.get(i).getMeasureNum());
			}
		}
		refreshMeasures();
	}
	
	private void refreshMeasures() {
		this.measuresHB.getChildren().clear();
		this.measuresHB.getChildren().addAll(measurePaneAL);
	}
	
	//Deprecated
	private void linkMeasures(int i) {
		ScorePane curr = this.measurePaneAL.get(i);
		if (i > 0) {
			curr.setPrev(measurePaneAL.get(i-1));
			curr.setNext(null);
			measurePaneAL.get(i-1).setNext(curr);
		} else {
			curr.setPrev(null);
			curr.setNext(null);
		}
//		curr.setMeasureNum(i);
	}
	
	/**
	 * Sets tempo to new value if valid.
	 * @param tempo
	 */
	public void setTempo(int tempo) {
		if (tempo >= MidiFile.MIN_DURATION && tempo <= MidiFile.MAX_DURATION) {
			this.tempo = tempo;
			System.out.printf("Tempo set to %s\n", tempo);
			updateInfoPane();
		} else {
			System.out.printf("%s is an invalid valid for Tempo. Must be between %s and %s.\n",
					tempo, MidiFile.MIN_DURATION, MidiFile.MAX_DURATION);
		}
	}


	public int getTempo() {
		return this.tempo;
	}
	
	public int getOffset() {
		return focusedScorePane.getOffset();
	}
	
	public void updateMeasureProperty(int numColsPerMeasure, int measureOffset) {
		this.focusedScorePane.setNumColsPerMeasure(numColsPerMeasure);
		this.focusedScorePane.setMeasureOffset(measureOffset);
		this.focusedScorePane.reDrawMeasure();
	}
	
	public int getTotalCols() {
		if (focusedScorePane == null) return 0;
		return focusedScorePane.getNumCells();
	}
	
	private void changeTimeSignature(TimeSignature ts) {
		this.ts = ts;
	}
	
	public void changeKeySignature(int tonic, int mode) {
		this.ks = new KeySignature(tonic, mode);
		changeKeySignature(ks);
	}
	
	public void changeKeySignature(KeySignature ks) {
		this.ks = ks;
		updateInfoPane();
	}
	
	
	public KeySignature getKeySignature() {
		return this.ks;
	}
	
	private void changeMeasureMult(int multiple) {
		this.multiple = multiple;
	}
	
	/**
	 * Transposes all the notes in the given measure indices (from, to) inclusive
	 * by the number of semitones indicates, either up or down.
	 * @param from start cell index
	 * @param to ending cell index
	 * @param semitones how many steps to transpose
	 * @param transposeUp transpose up or down
	 */
	public void transpose(int from, int to, int semitones, boolean transposeUp) {
		//Idea: use MeasurePane's getAllNotesInRow() method for all the rows,
		//except modify that method a bit by overloading it with start and ending column indices as parameters
		//and then get all notes in each row for those column range, then
		//take the array of wrappernotes and transpose them all, then use MeasurePane's deleteNote()
		//method to delete the old notes and use createNote() method below to create the transposed notes
		//CAUTION: If the melodies are locked, this will not work as intended. So make sure melodies are unlocked.
		//CAUTION 2: You might want to begin with row 0 and make the way down, or begin with the last row and make the way up,
		//depending on the direction of the transposition.

		
		//If we're currently in chord builder mode, the melody notes are locked and unable to be edited.
		//So temporarily unlock melody notes to allow for transposition. We'll lock them at the end of this method.
//		if (chordBuilderMode) {
//			this.lockMelodyNotes(false);
//		}
		if (transposeUp) {
			for (int i = 0; i < ScorePane.ROWS; ++i) {
				transposeNotes(i, from, to, semitones);
			} //end for i
		} else {
			semitones = -semitones;  //since we'll be transposing down
			for (int i = ScorePane.ROWS - 1; i >= 0; --i) {
				transposeNotes(i, from, to, semitones);
			}
		} //end if/else
		
		//Lock up the melody notes after transposition if needed
//		if (chordBuilderMode) {
//			this.lockMelodyNotes(true);
//		}
	} //end public void transpose
	
	/**
	 * Transpose all notes in a given row, between the start and ending column indices only, by the given no. of semitones.
	 * @param rowIdx
	 * @param colStartIdx
	 * @param colEndIdx
	 * @param semitones
	 */
	private void transposeNotes(int rowIdx, int colStartIdx, int colEndIdx, int semitones) {
		ArrayList<WrapperNote> wnAL = this.focusedScorePane.getAllNotesInRow(rowIdx, colStartIdx, colEndIdx);
		for (WrapperNote wn : wnAL) {
			//Delete the pitch in the current row index (as indicated by the pitch value) and current column index
			focusedScorePane.deletePitch(wn.getPitch(), wn.getColIdx());
			//Transpose pitch
			wn.setPitch(wn.getPitch() + semitones);
			//Ensure transposed pitch is within the legal pitch range, and if so, notate it
			if (wn.getPitch() >= MIN_PITCH && wn.getPitch() <= MAX_PITCH) {
//				focusedScorePane.createNote(wn, wn.getColIdx(), 1, false);
				focusedScorePane.createNote(wn, false);
			}
		} //end for WrapperNote
	}
	
	public void insertCells(int nColsToAdd, int startingAtCol, boolean addBeforeCol) {
		insertCells(nColsToAdd, startingAtCol, addBeforeCol, null);
	}
	
	public void insertCells(int nColsToAdd, int startingAtCol, boolean addBeforeCol, ArrayList<WrapperNote> al) {
		//First make sure that inserting these many columns would NOT result in exceeding the max. allowed columns.
		int newNumCol = this.getTotalCols() + nColsToAdd;
		if (newNumCol > ScorePane.MAX_CELLS) {
			this.focusedScorePane.throwErrorAlertDialog("Abort", 
					String.format("This would cause the number of cells to exceed the max. allowed (%s). Command canceled.",
					ScorePane.MAX_CELLS));
			return;
		}
		
		//Begin by saving the current canvas to a tmp file, except that every note that begins
		//on a column >= or > the startingAtCol variable (depennding on the boolean parameter)
		//is going to be offset by the no. of columns to be added
		//Afterward, populate it with the new column number

		
		File file = new File("~tmpsave.TMP");

		//Save current canvas to file, pursuant to the comments above
		ArrayList<WrapperNote> noteAL = new ArrayList<>();
		focusedScorePane.deSelectAll();
		//Read from the measure (we assume a single measure only) and save each note as a Note object, 
		//along with column index, and duration per cell info (let's just save it as 1), and tempo,
		//and no. of cells in this measure.
		//NOTE: duration per cell here indicates the GCD variable in ReadMidi.java class. This GCD variable
		//      helps determine the total no. of cells needed. But when saving an existing music canvas,
		//      we already know the total no. of cells we have. So no need for a GCD variable.
		for (int row = 0; row < ScorePane.ROWS; ++row) {
			noteAL.addAll(focusedScorePane.getAllNotesInRow(row));
		} //end for row
		System.out.println(noteAL); //testing

		int durationPerCell = 1;
		int tempo = PianoRollGUI.this.getTempo();
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.println(durationPerCell);
			writer.println(tempo);
			//					writer.println(focusedScorePane.getNumCells());
			writer.println(newNumCol);
			writer.println(this.ks.getTonic() + " " + this.ks.getMode());
//			writer.println(this.midi_instrument);
			for (int i = 0; i < this.midiInstrumentArr.length; ++i) {
				writer.print(this.midiInstrumentArr[i] + " ");
			}
			writer.println();
			writer.println(this.chordBuilderMode ? 1 : 0);
			writer.println(this.focusedScorePane.getColsPerMeasure() + " " + this.focusedScorePane.getOffset());
			writer.println(this.focusedScorePane.getWidthPerCell() + " " + this.focusedScorePane.getHeightPerCell());
			for (WrapperNote n : noteAL) {
				if (n.getColIdx() >= (addBeforeCol ? startingAtCol : startingAtCol + 1)) {
					writer.println(n.getPitch() + " " + n.getDuration() + " " + (n.getColIdx() + nColsToAdd) +
							" " + n.getColorInt() + " " + n.getOrigColorInt() + " " + n.getVolume() + " " + n.getChannel());
				} else {
					writer.println(n.getPitch() + " " + n.getDuration() + " " + n.getColIdx() + " " +
							n.getColorInt() + " " + n.getOrigColorInt() + " " + n.getVolume() + " " + n.getChannel());
				}
			}

			if (al != null) {
				for (WrapperNote n : al) {
					writer.println(n.getPitch() + " " + n.getDuration() + " " + n.getColIdx() + " " +
							n.getColorInt() + " " + n.getOrigColorInt() + " " + n.getVolume() + " " + n.getChannel());
				}
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} //end try/catch

		//Now load
		populate(file, newNumCol);

		//Finally set the active column to the spot where the change was made
		this.focusedScorePane.setActiveColumn(startingAtCol);
	}
	
	public void deleteCells(int from, int to) {
		if (from > to) {
			System.out.println("invalid range of columns. Aborting...");
			return;
		}
		int nColsToDelete = to - from + 1;
		
		int newNumCol = this.getTotalCols() - nColsToDelete;
		File file = new File("~tmpsave.TMP");
		
		//Save current canvas to file, then exclude notes that will be deleted along with columns
		ArrayList<WrapperNote> noteAL = new ArrayList<>();
		focusedScorePane.deSelectAll();
        for (int row = 0; row < ScorePane.ROWS; ++row) {
        	noteAL.addAll(focusedScorePane.getAllNotesInRow(row));
        } //end for row
        System.out.println(noteAL); //testing
        
        //Now exclude notes to be deleted along with the deleted columns.
        //If any part of the note's duration falls anywhere within the range of the deleted columns, get rid of them
        int index = 0;
        while (index < noteAL.size()) {
        	WrapperNote wn = noteAL.get(index);
        	int colIdxStart = wn.getColIdx();
        	int colIdxEnd = colIdxStart + wn.getDuration() - 1;
        	if (intersects(colIdxStart, colIdxEnd, from, to)) {
        		noteAL.remove(index);
        	} else {
        		index++;
        	}
        }
        
        int durationPerCell = 1;
        int tempo = PianoRollGUI.this.getTempo();
        PrintWriter writer;
		try {
			
			
			
			
			writer = new PrintWriter(file, "UTF-8");
			writer.println(durationPerCell);
			writer.println(tempo);
//			writer.println(focusedScorePane.getNumCells());
			writer.println(newNumCol);
			writer.println(this.ks.getTonic() + " " + this.ks.getMode());
//			writer.println(this.midi_instrument);
			for (int i = 0; i < this.midiInstrumentArr.length; ++i) {
				writer.print(this.midiInstrumentArr[i] + " ");
			}
			writer.println();
			writer.println(this.chordBuilderMode ? 1 : 0);
			writer.println(this.focusedScorePane.getColsPerMeasure() + " " + this.focusedScorePane.getOffset());
			writer.println(focusedScorePane.getWidthPerCell() + " " + this.focusedScorePane.getHeightPerCell());
			for (WrapperNote n : noteAL) {
				if (n.getColIdx() >= to) {
					writer.println(n.getPitch() + " " + n.getDuration() + " " + (n.getColIdx() - nColsToDelete) + " " + 
									n.getColorInt() + " " + n.getOrigColorInt() + " " + n.getVolume() + " " + n.getChannel());
				} else {
					writer.println(n.getPitch() + " " + n.getDuration() + " " + n.getColIdx() + " " 
								+ n.getColorInt() + " " + n.getOrigColorInt() + " " + n.getVolume() + " " + n.getChannel());
				}
			}
            writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} //end try/catch
		
		//Now load
		populate(file, newNumCol);
		
		//Finally set the active column to the spot where the change was made
		//(In the event the columns were deleted, set the active column to the earliest deleted column - 1, or 0, whichever is greater)
		this.focusedScorePane.setActiveColumn(Math.max(0, from-1));
		
	}
	
	private boolean intersects(int fromA, int toA, int fromB, int toB) {
		return !(toA < fromB || fromA > toB);
	}
	
	private void setMelodyChordModeCB(boolean b) {
		melodyChordModeCB.setSelected(b);
	}
	
	public int getFocusedMeasureNum() {
		if (this.focusedScorePane == null)
			return -1;
		return this.focusedScorePane.getMeasureNum();
	}
	
	public int getTotalNumMeasures() {
		return this.measurePaneAL.size();
	}
	public int getTotalNumPitches() {
		return TOTAL_NUM_PITCHES;
	}
	public boolean isAutoLoop() {
		return isAutoLoop;
	}

	public void setAutoLoop(boolean isAutoLoop) {
		this.isAutoLoop = isAutoLoop;
		autoLoopChkBox.setSelected(isAutoLoop);
		
	}
	
	public void setMidiChannelForSelectedNotes(int channel) {
		HashSet<RectangleNote> selectedRnHS = this.focusedScorePane.getAllSelectedNotes();
		for (RectangleNote rn : selectedRnHS) {
			rn.setChannel(channel);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ColorIntMap.getIntToRGBArr();  //initialize the color map just in case
		Application.launch(args);
	}
}
