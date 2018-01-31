package pianoroll;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import main.Chord;
import main.ChordSequence;
import main.Note;
import main.TimeSignature;

public class MeasurePane extends VBox {	
	private final int ROWS = 88;  //88 pitches like a standard piano roll
	private TimeSignature ts; //Time signature pertaining to this measure
	private int col;  //No. of "columns" in this measure. Each "column" pertains to a time signature of a pitch that may be modified?
//	private NoteButton focusedNoteButton;  //The current pitch button that is focused.
//	private MeasurePane prev, next; //Previous and next measures. Similar to doubly-linked LinkedList.
	private int measureNum;
	private GridPane gp;
	private Label lbl;
	private final int HEIGHT = 600;
	private int width;
	private Pane pane;
	private Rectangle[] rectArr;
	private Rectangle[] columnRectArr;
	private int mouseDragStartedIdx;
//	private Rectangle focusedRectangle;

	private PianoRollGUI pianoRollGUI;
	private double widthPerCell, heightPerCell;
	
	//Consider having a prev and next variable for measures (like linkedlist), for auto-focusing on next measure when needed
	private MeasurePane prev, next;

	public MeasurePane(PianoRollGUI pianoRollGUI) {
		this(new TimeSignature(4,4), pianoRollGUI, null, null);
	}
	
	public MeasurePane(TimeSignature ts, PianoRollGUI pianoRollGUI, MeasurePane prev, MeasurePane next) {
		this(new TimeSignature(4,4), 0, pianoRollGUI, prev, next);
	}
	
	public MeasurePane(int measureNum, PianoRollGUI pianoRollGUI, MeasurePane prev, MeasurePane next) {
		this(new TimeSignature(4,4), measureNum, pianoRollGUI, prev, next);
	}
	
	public MeasurePane(TimeSignature ts, int measureNum, PianoRollGUI pianoRollGUI, MeasurePane prev, MeasurePane next) {
		this(ts, 1, measureNum, pianoRollGUI, prev, next);
	}
	
	public MeasurePane(TimeSignature ts, int multiple, int measureNum, PianoRollGUI pianoRollGUI,
			MeasurePane prev, MeasurePane next) {
		this.pianoRollGUI = pianoRollGUI;
		this.prev = prev;
		this.next = next;
		this.mouseDragStartedIdx = -1;
		//Consider using w and e keys to traverse to prev or next measure
//		super(); //Invoke parentclass GridPane's constructor first. (This is done implicitly, so comment out)

		this.ts = ts;
	    this.col = this.ts.getNumer() * multiple;
	    this.measureNum = measureNum;
	    this.rectArr = new Rectangle[ROWS * this.col];
	    this.columnRectArr = new Rectangle[this.col];
	    
	    this.setOnKeyPressed(e -> {
	    	//shift focused button depending on arrow keys
	    	if (e.getCode() == KeyCode.UP) {
//	    		if (this.focusedNoteButton.getI() > 0) {
	    			
	    			
	    			
//	    		}
	    	} else if (e.getCode() == KeyCode.DOWN) {
	    		
	    	} else if (e.getCode() == KeyCode.LEFT) {
//	    		if (this.focusedNoteButton.getJ() > 0) {
	    			
	    			
	    			
//	    		}
	    	} else if (e.getCode() == KeyCode.RIGHT) {
	    		
	    	} else if (e.getCode() == KeyCode.SPACE) {
//	    		this.focusedNoteButton.fire(); //same effect as mouseclick
	    	}
//	    	System.out.println("current focused button is at: " + this.focusedNoteButton.getI() + " " + this.focusedNoteButton.getJ());
//	    	this.focusedNoteButton.requestFocus();	    	
	    });  //end setOnKeyPressed
	    
	    this.setAlignment(Pos.CENTER);
	    this.getChildren().clear();
	    this.lbl = new Label("Measure " + this.measureNum);
	    lbl.setPadding(new Insets(5,10,5,10));
	    lbl.setStyle("-fx-background-color : darkgoldenrod ; -fx-text-fill: white; -fx-font-weight: bold");
	    HBox hb = new HBox();
	    hb.setAlignment(Pos.CENTER);
	    hb.setStyle("-fx-background-color: darkgoldenrod");
	    hb.setPadding(new Insets(2));
	    hb.getChildren().add(lbl);
	    
	    this.getChildren().add(hb);
	    hb.setOnMousePressed(e -> {
	    	this.pianoRollGUI.focus(this, false);  //no autoscroll when selecting label by mouse, because it's a bit irritating
	    });
	    
	    this.pane = new Pane();
	    
	    
	    this.heightPerCell = 20;
	    this.widthPerCell = 20;
	    double totalWidth = this.col * widthPerCell;
	    
	    for (int i = 0; i <= ROWS; ++i) {
	    	pane.getChildren().add(new Line(0, i*heightPerCell, totalWidth, i*heightPerCell));
	    }
	    
	    for (int j = 0; j <= this.col; ++j) {
	    	pane.getChildren().add(new Line(j*widthPerCell, 0, j*widthPerCell, ROWS * heightPerCell));
	    }
	    
	    this.getChildren().add(pane);
	    this.pane.setStyle("-fx-background-color: lavender ");
	    
	    this.pane.setOnMouseDragged(e -> {
	    	if (this.mouseDragStartedIdx < 0 || this.mouseDragStartedIdx >= this.rectArr.length) {
	    		mouseDragStartedIdx = -1;
	    		System.out.println("mouseDragStarted set to -1");
	    		return;
	    	}
	    	System.out.println("Mouse moved");
	    	int rect1DCoordinateIdx = get1DCoord(this.heightPerCell, this.widthPerCell, e.getX(), e.getY());
	    	
        	//Look up the index where the mouse drag was originally initiated,
        	//and check whether the current index is in the same row as the original index.
        	//If so, look at all the horizontal cells between the current index and the orig. drag index,
        	//and fill in all of them up to the current index.
        	if (this.mouseDragStartedIdx >= 0 && this.mouseDragStartedIdx < this.rectArr.length &&
        			getRow(mouseDragStartedIdx) == getRow(rect1DCoordinateIdx)) {
        		if (mouseDragStartedIdx < rect1DCoordinateIdx) {
        			for (int i = mouseDragStartedIdx; i <= rect1DCoordinateIdx; ++i) createPitch(i);
        		} else if (mouseDragStartedIdx > rect1DCoordinateIdx){
        			for (int i = mouseDragStartedIdx; i >= rect1DCoordinateIdx; --i) createPitch(i);
        		}
        	} else {
        		mouseDragStartedIdx = -1;
        		System.out.println("mouseDragStarted set to -1");
        	}
	    });
	    
	    this.pane.setOnMouseReleased(e -> {
	    	this.mouseDragStartedIdx = -1;
	    	System.out.println("mouseDragStartedIdx set to -1.");
	    });
	    this.pane.setOnMousePressed(e -> {
	    	//put a border around this measure
	    	this.pianoRollGUI.focus(this, false, false);  //focus this measure, but don't autoscroll and don't set active column to 0.
	    	int rect1DCoordinate = get1DCoord(this.heightPerCell, this.widthPerCell, e.getX(), e.getY());
	    	int leftCornerCell = (int) (e.getX() / widthPerCell);
        	int upperCornerCell = (int) (e.getY() / heightPerCell);
        	
	    	if (e.getButton() == MouseButton.SECONDARY) {
	    		System.out.println("Right click!");
	    		this.mouseDragStartedIdx = -1;
				System.out.println("mouseDragStarted set to -1");
//	    		int leftCornerCell = (int) (e.getX() / widthPerCell);
	    		if (leftCornerCell < this.col) {
	    			if (this.columnRectArr[leftCornerCell] == null) {
	    				System.out.println("Deleting any other column rectangles...");
//	    				this.clearColRect();  //delete any other column rectangles in this measure first
	    				this.createColRect(leftCornerCell, widthPerCell, ROWS, heightPerCell, this.pane);
			  
	    			} else {   //There is already a column rectangle in this cell. So right clicking it deletes it.
	    				Rectangle rect = this.columnRectArr[leftCornerCell];
	    				this.pane.getChildren().remove(rect);
	    				this.columnRectArr[leftCornerCell] = null;
	    			}
	    		}
	    		return;
	    	}
	    	
	    	//If this rectangle is contained it means this pitch is currently activated. Deactivate it.
	    	if (this.rectArr[rect1DCoordinate] != null) {
	    		System.out.println("Deactivating cell...");
	    		this.deleteRect(rect1DCoordinate, this.rectArr, this.pane);
//	    		this.clearColRect();  //delete any other column rectangles in this measure first
				this.createColRect(leftCornerCell, widthPerCell, ROWS, heightPerCell, this.pane);
//	    		Rectangle rect = this.rectArr[rect1DCoordinate];
//	    		this.pane.getChildren().remove(rect);
//	    		rect = null;
//		    	this.rectArr[rect1DCoordinate] = null;
				this.mouseDragStartedIdx = -1;
				System.out.println("mouseDragStarted set to -1");
		    	
	    	} else {
	    		//Coordinate of Rectangle with left upper corner at (leftCornerCell, upperCornerCell)
//	    		int leftCornerCell = (int) (e.getX() / widthPerCell);
//	        	int upperCornerCell = (int) (e.getY() / heightPerCell);
	        	System.out.printf("(x,y) = (%s, %s)\n", leftCornerCell, upperCornerCell);
	        	if (leftCornerCell < this.col) {
	        		this.mouseDragStartedIdx = get1DCoord(heightPerCell, widthPerCell, e.getX(), e.getY());
	        		System.out.println("mouseDragStartedIdx: " + mouseDragStartedIdx);
	        		this.createRect(leftCornerCell, widthPerCell, upperCornerCell, heightPerCell, this.pane, this.rectArr, rect1DCoordinate);
//	        		this.clearColRect();  //delete any other column rectangles in this measure first
//					this.createColRect(leftCornerCell, widthPerCell, ROWS, heightPerCell, this.columnRectArr, this.pane);
			    	
			    	int pitch = computePitch(rect1DCoordinate);
			    	this.play(pitch);
	        	}
				
	    	}  //end if/else
	    });
	    
	}	//end public MeasurePane

	//Notate pitch given the array index.
	private void createPitch(int rect1DCoordinateIdx) {
		//convert the 1d index to 2-d rowindex and colindex, then create the rectangle.
		this.createRect(getCol(rect1DCoordinateIdx), widthPerCell, getRow(rect1DCoordinateIdx), heightPerCell, pane, rectArr, rect1DCoordinateIdx);
	}
	
	private int getRow(int rect1DCoordinateIdx) {
		return rect1DCoordinateIdx / this.col;
	}
	
	private int getCol(int rect1DCoordinateIdx) {
		return rect1DCoordinateIdx % this.col;
	}
	
	public void setPrev(MeasurePane prev) {
		this.prev = prev;
	}
	
	public void setNext(MeasurePane next) {
		this.next = next;
	}
	
	public MeasurePane getPrev() {
		return this.prev;
	}
	
	public MeasurePane getNext() {
		return this.next;
	}
	
	public void deleteAllNotes() {
		for (int i = 0; i < this.rectArr.length; i++) {
			this.deleteRect(i, rectArr, this.pane);
		}
	}
	
	private void deleteRect(int index, Rectangle[] rectArr, Pane pane) {
		Rectangle rect = this.rectArr[index];
		if (rect != null) {
			pane.getChildren().remove(rect);
			rect = null;
	    	rectArr[index] = null;
		}
	}
	
	private void createRect(int colIndex, double widthPerCell, int rowIndex, double heightPerCell, Pane pane, Rectangle[] rectArr, int rectArrIndex) {
		if (rectArr[rectArrIndex] == null) {
			Rectangle rect = new Rectangle(colIndex*widthPerCell, rowIndex*heightPerCell, widthPerCell, heightPerCell);
	    	rect.setFill(Color.GREEN);
	    	pane.getChildren().add(rect);
	    	rectArr[rectArrIndex] = rect;
	    	this.createColRect(colIndex, widthPerCell, this.ROWS, heightPerCell, pane);
		}
	}
	
	private void clearColRect() {
		for (int i = 0; i < this.columnRectArr.length; ++i) {
			this.pane.getChildren().remove(this.columnRectArr[i]);
			columnRectArr[i] = null;
		}
	}
	
	private void createColRect(int colIndex, double widthPerCell, int totalRows, double heightPerCell, Pane pane) {
		this.clearColRect();
		Rectangle rect = new Rectangle(colIndex*widthPerCell, 0, widthPerCell, totalRows * heightPerCell);
		rect.setStroke(Color.BROWN);
		rect.setStrokeWidth(3);
		rect.setFill(Color.TRANSPARENT);
		pane.getChildren().add(rect);
		columnRectArr[colIndex] = rect;
		System.out.println("Column Rect created at column " + colIndex);
	}
	
	public void clear() {
		this.clearColRect();
		this.setStyle("");
	}
	public void setFocus() {
		this.setStyle("-fx-border-color: darkgray; -fx-border-width:5");
	}
	
	
	
	private int get1DCoord(double heightPerCell, double widthPerCell, double x, double y) {
    	//Get the upper left corner (x,y) of the cell pertaining to the area clicked by the mouse
    	//Example: Say each cell has size 10x10, and there are total of 88 rows and 8 columns of cells.
    	int leftCornerCell = (int) (x / widthPerCell);
    	int upperCornerCell = (int) (y / heightPerCell);
//    	System.out.printf("Cell clicked (x,y): (%s, %s)\n", leftCornerCell, upperCornerCell);
    	int rect1DCoordinate = upperCornerCell * this.col + leftCornerCell;
    	return rect1DCoordinate;
	}
	
	public void deletePitch(int pitch) {
		//Delete pitch on this measure, inside the column rectangle if one exists,
		//Otherwise do nothing.
		int activeColumn = getActiveColumn();
		if (activeColumn >= 0) {
			System.out.println("activeColumn");
			int row = this.computeRow(pitch);
			System.out.println("Row: " + row);
			int index = convertTo1DCoord(row, activeColumn);
			System.out.println("Index: " + index);
			this.deleteRect(index, this.rectArr, this.pane);
		}
	}
	
	private int convertTo1DCoord(int rowIndex, int colIndex) {
		return rowIndex * this.col + colIndex;
	}
	
	private int getActiveColumn() {
		for (int i = 0; i < this.columnRectArr.length; ++i) {
			if (columnRectArr[i] != null) {
				return i;
			}
		}
		return -1;
	}
	
	//Delete all notes in current active column and then move active column backward,
	//moving over to previous measure if needed
	public void backSpace() {
		this.delete();
		if (advanceActiveColumn(false) == false) {
			if (this.prev != null) {
				this.clear();
				this.pianoRollGUI.goToNextOrPrevMeasure(false);
				this.prev.setActiveColumn(this.prev.col - 1);
			}
		}
	}
	
	public void delete() {
		int activeCol = getActiveColumn();
		if (activeCol == -1) return;
		for (int i = activeCol; i < this.rectArr.length; i += this.col) {
			this.deleteRect(i, rectArr, this.pane);
		}
	}
	
	public void setActiveColumn(int colIndex) {
		this.createColRect(colIndex, this.widthPerCell, this.ROWS, this.heightPerCell, this.pane);
	}
	
	public boolean advanceActiveColumn(boolean forward) {
		int activeColumn = getActiveColumn() + (forward ? 1 : -1);
		if (activeColumn >= this.col || activeColumn < 0) {
			return false;
		} else {
			this.clearColRect();
			this.createColRect(activeColumn, this.widthPerCell, this.ROWS, this.heightPerCell, this.pane);
			return true;
		}
	}
	
	public int getMeasureNum() {
		return measureNum;
	}

	public void play(int pitch) {
		//notate the pitch on this measure, inside the column rectangle if one already exists,
		//and inside the first column (creating the column rectangle along the way) otherwise.
		int activeColumn = getActiveColumn();
		if (activeColumn == -1) {
			System.out.println("ActiveColumn == -1");
			activeColumn = 0;
			this.createColRect(activeColumn, this.widthPerCell, ROWS, this.heightPerCell,this.pane);
		}
		this.notate(pitch, activeColumn);
		
		//play pitch sound
		//Move this function over to the pianorollGUI so we don't need multiple mchannels for every measure
		this.pianoRollGUI.playSound(pitch);
	}
	
	private void notate(int pitch, int colIdx) {
		int pitchRow = computeRow(pitch);
//		System.out.println(pitchRow);
		int rectArrIndex = pitchRow * this.col + colIdx;
		System.out.println("rectArrIndex: " + rectArrIndex);
		this.createRect(colIdx, this.widthPerCell, pitchRow, this.heightPerCell, this.pane, this.rectArr, rectArrIndex);
	}
	
	private int computeRow(int pitch) {
		//pitch = ROWS - row + MIN_PITCH
		//row = ROWS - pitch + MIN_PITCH
		return ROWS - pitch + PianoRollGUI.MIN_PITCH;
	}
	
	private int computePitch(int coord1D) {
		int row = coord1D / this.col;	//compute the row
		//Since the rows numbers increase downwards, but we want the pitch numbers 
		//to decrease downwards, we return this value, with an offset to raise overall pitch range by a bit
		return ROWS - row + PianoRollGUI.MIN_PITCH;			
	}
	
	/**
	 * Looks at this measure and converts all the chords into a ChordSequence object.
	 * NOTE: If any chord in a given column is "invalid" (see below comments), then that chord is treated as null
	 * @return ChordSequence object
	 */
	public ChordSequence getChordSeq() {
		ChordSequence chordSeq = new ChordSequence(this.col);

		for (int i = 0; i < chordSeq.length(); ++i) {
			//get all notes in the current column and create a Chord.
			//If the Chord is illegal or empty, e.g. it consists of more than 4 notes
			//(TODO we'll later change this to allow up to 5 notes), then set it to null
			ArrayList<Integer> pitchAL = getAllNotesInColumn(i);
			System.out.println(pitchAL);
			if (pitchAL == null || pitchAL.size() == 0 || pitchAL.size() > 4) {
				chordSeq.setChord(i, null);
			} else {
				Note[] noteArr = convertToNotes(pitchAL);
				Chord c = new Chord(noteArr);
				chordSeq.setChord(i, c);
			}
		} //end for i
	 	
		return chordSeq;
	}
	
	private Note[] convertToNotes(ArrayList<Integer> pitchAL) {
		Note[] ret = new Note[pitchAL.size()];
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = new Note(pitchAL.get(i), this.pianoRollGUI.getTempo());
		}
		return ret;
	}
	
	//Return all notes in given column in ascending order
	private ArrayList<Integer> getAllNotesInColumn(int colIndex) {
		ArrayList<Integer> ret = new ArrayList<>();
		for (int i = colIndex; i < this.rectArr.length; i += this.col) {
			if (this.rectArr[i] != null) {
				int pitch = this.computePitch(i);
				ret.add(pitch);
			}
		}
		Collections.reverse(ret);  //This sorts the pitches in ascending order
		return ret;
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



	@Override
	public String toString() {
		return "MeasurePane";
	}
	
}
