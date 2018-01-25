package pianoroll;
import java.util.ArrayList;
import java.util.PriorityQueue;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import main.TimeSignature;

public class MeasurePane_DELETE extends VBox {	
	private final int ROWS = 88;  //88 pitches like a standard piano roll
	private TimeSignature ts; //Time signature pertaining to this measure
	private int col;  //No. of "columns" in this measure. Each "column" pertains to a time signature of a pitch that may be modified?
	private NoteButton_DELETE[][] noteButtonArr;  //2D array of pitch buttons inside this measure
	private NoteButton_DELETE focusedNoteButton;  //The current pitch button that is focused.
//	private MeasurePane prev, next; //Previous and next measures. Similar to doubly-linked LinkedList.
	private int measureNum;
	private GridPane gp;
	private Label lbl;
	private ArrayList<PriorityQueue<NoteButton_DELETE>> activatedBtnsAL;
	private static MidiChannel[] mChannels;

	public MeasurePane_DELETE() {
		this(new TimeSignature(4,4));
	}
	
	public MeasurePane_DELETE(TimeSignature ts) {
		this(new TimeSignature(4,4), 0);
	}
	
	public MeasurePane_DELETE(int measureNum) {
		this(new TimeSignature(4,4), measureNum);
	}
	
	public MeasurePane_DELETE(TimeSignature ts, int measureNum) {
		this(ts, 1, measureNum);
	}
	
	public MeasurePane_DELETE(TimeSignature ts, int multiple, int measureNum) {
		
//		super(); //Invoke parentclass GridPane's constructor first. (This is done implicitly, so comment out)
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
//		        mChannels[1].programChange(0);
			} catch (MidiUnavailableException mue) {
				mue.printStackTrace();
			}
		}
		this.ts = ts;
	    this.col = this.ts.getNumer() * multiple;
	    this.measureNum = measureNum;
//	    this.prev = prev;
//	    this.next = next;
	    
	    this.gp = new GridPane();
		this.gp.setAlignment(Pos.CENTER);
		this.gp.setPadding(new Insets(0));
		this.gp.getChildren().clear();
	    this.gp.setHgap(0);
	    this.gp.setVgap(0);
	    this.focusedNoteButton = null;
	    
	    this.activatedBtnsAL = new ArrayList<PriorityQueue<NoteButton_DELETE>>();
	    for (int i = 0; i < this.col; ++i) {
	    	this.activatedBtnsAL.add(new PriorityQueue<NoteButton_DELETE>());
	    }
	    
	    noteButtonArr = new NoteButton_DELETE[ROWS][this.col];
	    
	    for (int i = 0; i < ROWS; ++i) {
	    	for (int j = 0; j < this.col; ++j) {
	    		NoteButton_DELETE btn = new NoteButton_DELETE(i, j);
	    		btn.setMaxSize(5, 5);
	    		
	    		this.gp.add(btn, j,ROWS-i);
	    		this.noteButtonArr[i][j] = btn;
	    		
	    		btn.setOnMouseEntered(e -> {
	    			if (!btn.isActivated()) {
	    				btn.setStyle("-fx-background-color: lightgreen");
	    			}
	    			btn.requestFocus();
	    			this.focusedNoteButton = btn;
	    		});
	    		btn.setOnMouseExited(e -> {
	    			if (!btn.isActivated())	btn.setStyle("");
	    			
	    		});
	    		btn.setOnMousePressed(e -> {
	    			btn.setStyle("-fx-background-color: darkgreen");
	    		});
	    		btn.setOnMouseReleased(e -> {
	    			if (btn.isHover()) {
	    				if (!btn.isActivated())	btn.setStyle("-fx-background-color: lightgreen");
	    			} else {
	    				if (!btn.isActivated())	btn.setStyle("");
	    			}
	    		});
	    		
	    		btn.setOnAction(e -> {
	    			if (btn.isActivated()) {
	    				btn.setActivated(false);
	    				btn.setStyle("");
	    				this.activatedBtnsAL.get(btn.getJ()).remove(btn);
	    			} else {
	    				btn.setStyle("-fx-background-color: mediumseagreen");
		    			btn.setActivated(true);
		    			this.activatedBtnsAL.get(btn.getJ()).add(btn);
		    			
		    			int pitch = btn.getPitch();
		    			mChannels[0].noteOn(pitch, 120);//play note number with specified volume 
						try { 
							Thread.sleep(100); // wait time in milliseconds to control duration
						} catch (InterruptedException ie) {
		
						}
						mChannels[0].noteOff(pitch);//turn of the note
	    			}
	    			
	    		});
	    	}	//end for j
	    }	//end for i
	    
	    this.focusedNoteButton = this.noteButtonArr[0][0];
	    
	    this.setOnKeyPressed(e -> {
	    	//shift focused button depending on arrow keys
	    	if (e.getCode() == KeyCode.UP) {
	    		if (this.focusedNoteButton.getI() > 0) {
	    			NoteButton_DELETE upperBtn = this.noteButtonArr[this.focusedNoteButton.getI() - 1][this.focusedNoteButton.getJ()];
	    			
	    			this.focusedNoteButton = upperBtn;
	    			upperBtn.requestFocus();
	    		}
	    	} else if (e.getCode() == KeyCode.DOWN) {
	    		if (this.focusedNoteButton.getI() < this.noteButtonArr.length - 1) {
	    			NoteButton_DELETE lowerBtn = this.noteButtonArr[this.focusedNoteButton.getI() + 1][this.focusedNoteButton.getJ()];
	    			
	    			this.focusedNoteButton = lowerBtn;
	    			lowerBtn.requestFocus();
	    		}
	    	} else if (e.getCode() == KeyCode.LEFT) {
	    		if (this.focusedNoteButton.getJ() > 0) {
	    			NoteButton_DELETE leftBtn = this.noteButtonArr[this.focusedNoteButton.getI()][this.focusedNoteButton.getJ()-1];
	    			
	    			this.focusedNoteButton = leftBtn;
	    			leftBtn.requestFocus();
	    		}
	    	} else if (e.getCode() == KeyCode.RIGHT) {
	    		if (this.focusedNoteButton.getJ() < this.noteButtonArr[0].length - 1) {
	    			NoteButton_DELETE rightBtn = this.noteButtonArr[this.focusedNoteButton.getI()][this.focusedNoteButton.getJ()+1];
	    			
	    			this.focusedNoteButton = rightBtn;
	    			rightBtn.requestFocus();
	    		}
	    	} else if (e.getCode() == KeyCode.SPACE) {
	    		this.focusedNoteButton.fire(); //same effect as mouseclick
	    	}
//	    	System.out.println("current focused button is at: " + this.focusedNoteButton.getI() + " " + this.focusedNoteButton.getJ());
//	    	this.focusedNoteButton.requestFocus();	    	
	    });  //end setOnKeyPressed
	    
	    this.setAlignment(Pos.CENTER);
	    this.getChildren().clear();
	    this.lbl = new Label("Measure " + this.measureNum);
	    lbl.setStyle("-fx-background-color : gold");
	    lbl.setOnMousePressed(e -> {
	    	this.requestFocus();
	    	this.setStyle("-fx-border-color: blue");
	    });
	    lbl.setOnMouseReleased(e -> {
	    	this.setStyle("");
	    });
	    this.getChildren().add(lbl);
	    this.getChildren().add(this.gp);
	}	//end public MeasurePane
	
	public ArrayList<PriorityQueue<NoteButton_DELETE>> getActivatedBtnsAL() {
		return activatedBtnsAL;
	}
	
	public TimeSignature getTs() {
		return ts;
	}

	public void setTs(TimeSignature ts) {
		this.ts = ts;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

//	public MeasurePane getPrev() {
//		return prev;
//	}
//
//	public void setPrev(MeasurePane prev) {
//		this.prev = prev;
//	}
//
//	public MeasurePane getNext() {
//		return next;
//	}
//
//	public void setNext(MeasurePane next) {
//		this.next = next;
//	}

	public int getROWS() {
		return ROWS;
	}

	public NoteButton_DELETE[][] getNoteButtonArr() {
		return noteButtonArr;
	}

	@Override
	public String toString() {
		return "MeasurePane";
	}
	
}
