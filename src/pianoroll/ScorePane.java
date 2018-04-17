package pianoroll;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
//import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
//import javafx.scene.layout.FlowPane;
//import javafx.scene.layout.GridPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
//import main.Chord;
//import main.ChordSequence;
import main.Note;
import main.TimeSignature;

public class ScorePane extends VBox {	
	
	public static final int MAX_CELLS = 5000;
	public static final int ROWS = 88;  //88 pitches like a standard piano roll
	private TimeSignature ts; //Time signature pertaining to this measure
	private int col;  //No. of "columns" in this measure. Each "column" pertains to a time signature of a pitch that may be modified?
//	private NoteButton focusedNoteButton;  //The current pitch button that is focused.
//	private MeasurePane prev, next; //Previous and next measures. Similar to doubly-linked LinkedList.
	private int measureNum;
	private int measureOffset; //how many columns in the first measure?
	private int numCellsPerMeasure;
//	private GridPane gp;
	private Label lbl;
//	private final int HEIGHT = 600;
//	private int width;
	private Pane pane;
	private RectangleNote[] rectArr;
	private Rectangle[] columnRectArr;
	private int rectIndexWhereDragStarted;
//	private int mouseDragStartedIdx;
//	private Rectangle focusedRectangle;
	private PianoRollGUI pianoRollGUI;
	private double widthPerCell, heightPerCell;
	private static double DEFAULT_HEIGHT_PER_CELL = 20;
	private static double DEFAULT_WIDTH_PER_CELL = 20;
	private static final double MAX_HEIGHT_PER_CELL = 30;
	private static final double MAX_WIDTH_PER_CELL = 30;
	private static final double MIN_HEIGHT_PER_CELL = 6;
	private static final double MIN_WIDTH_PER_CELL = 6;
	private SelectedNotesRectangle selectionMouseRect;
	private double selectionRectX, selectionRectY;
	private int activeCol;
//	private Rectangle[] selectedColumnArr;
//	private SelectedNotesRectangle selectedNotesGreyRect;
	
	private HashSet<RectangleNote> selectedNotesRectHS;
	private RectangleNote selectedPrimaryNoteForDragging, leftmostNoteForCopyPasting;
	private boolean copyingNotes, draggingNotes;
	
	
	
	//Consider having a prev and next variable for measures (like linkedlist), for auto-focusing on next measure when needed
	private ScorePane prev, next;

	
	private class SelectedNotesRectangle extends Rectangle {
		int xStart, xEnd, yStart, yEnd;
		SelectedNotesRectangle(double x, double y, double width, double height) {
			super(x,y,width,height);
		}
		
		SelectedNotesRectangle(int xStart, int xEnd, int yStart, int yEnd) {
			super(xStart * widthPerCell, yStart * heightPerCell, 
					(xEnd - xStart + 1) * widthPerCell, 
					(yEnd - yStart + 1) * heightPerCell);
			this.xStart = xStart;
			this.xEnd = xEnd;
			this.yStart = yStart;
			this.yEnd = yEnd;
			
			this.setStyle("-fx-opacity: 0.3; -fx-fill: gray");
		}
	}
	
	

	
	public ScorePane(PianoRollGUI pianoRollGUI) {
		this(new TimeSignature(4,4), pianoRollGUI, null, null);
	}
	
	public ScorePane(TimeSignature ts, PianoRollGUI pianoRollGUI, ScorePane prev, ScorePane next) {
		this(new TimeSignature(4,4), 0, pianoRollGUI, prev, next);
	}
	
	public ScorePane(int measureNum, PianoRollGUI pianoRollGUI, ScorePane prev, ScorePane next) {
		this(new TimeSignature(4,4), measureNum, pianoRollGUI, prev, next);
	}
	
	public ScorePane(TimeSignature ts, int measureNum, PianoRollGUI pianoRollGUI, ScorePane prev, ScorePane next) {
		this(ts, 1, measureNum, pianoRollGUI, prev, next);
	}
	/**
	 * @param ts
	 * @param multiple
	 * @param measureNum
	 * @param pianoRollGUI
	 * @param prev
	 * @param next
	 */
	public ScorePane(TimeSignature ts, int multiple, int measureNum, PianoRollGUI pianoRollGUI,
			ScorePane prev, ScorePane next) {
	    this(ts.getNumer() * multiple, 16, 0, measureNum, pianoRollGUI, prev, next, null, null);
	}	//end public MeasurePane
	
	public ScorePane(int numCols, int measureNum, PianoRollGUI pianoRollGUI, double heightPerCell, double widthPerCell) {
		this(numCols, 0, measureNum, 16, pianoRollGUI, null, null, heightPerCell, widthPerCell);
	}
	
	/**
	 * 
	 * @param col # of cells in a measure
	 * @param ts
	 * @param measureNum
	 * @param pianoRollGUI
	 * @param prev
	 * @param next
	 */
	public ScorePane(int col, int cellsPerSubMeasure, int measureOffset, int measureNum, PianoRollGUI pianoRollGUI, 
			           ScorePane prev, ScorePane next, Double heightPerCell, Double widthPerCell) {
		this.pianoRollGUI = pianoRollGUI;
		this.prev = prev;
		this.next = next;
		this.measureOffset = measureOffset;
		this.numCellsPerMeasure = cellsPerSubMeasure;
		this.activeCol = -1;
		this.rectIndexWhereDragStarted = -1;
		//Consider using w and e keys to traverse to prev or next measure
//		super(); //Invoke parentclass GridPane's constructor first. (This is done implicitly, so comment out)

//		this.ts = ts;
	    this.col = col;
	    this.measureNum = measureNum;
	    this.rectArr = new RectangleNote[ROWS * this.col];
	    this.columnRectArr = new Rectangle[this.col];
	    
	    this.heightPerCell = (heightPerCell == null ? DEFAULT_HEIGHT_PER_CELL : heightPerCell);
	    this.widthPerCell = (widthPerCell == null ? DEFAULT_WIDTH_PER_CELL : widthPerCell);
//	    this.selectedNotesGreyRect = null;
	    
	    redrawMeasure(col, this.numCellsPerMeasure, measureOffset, ts, measureNum, pianoRollGUI, prev, next, activeCol, rectArr, columnRectArr, 
	    		this.heightPerCell, this.widthPerCell);
	}
	
	private void redrawMeasure(int col, int numCellsPerSubMeasure, int measureOffset, TimeSignature ts, int measureNum, PianoRollGUI pianoRollGUI, 
			ScorePane prev, ScorePane next, int activeCol, RectangleNote[] rectArr, Rectangle[] columnRectArr, 
			double heightPerCell, double widthPerCell) {

		this.pianoRollGUI = pianoRollGUI;
		this.prev = prev;
		this.next = next;
		this.activeCol = activeCol;
		this.measureOffset = measureOffset;
		this.numCellsPerMeasure = numCellsPerSubMeasure;
		//Consider using w and e keys to traverse to prev or next measure
//		super(); //Invoke parentclass GridPane's constructor first. (This is done implicitly, so comment out)

		this.ts = ts;
	    this.col = col;
	    this.measureNum = measureNum;
	    this.rectArr = rectArr;
	    this.columnRectArr = columnRectArr;
	    
	    this.heightPerCell = heightPerCell;
	    this.widthPerCell = widthPerCell;
//	    this.selectedNotesGreyRect = selectedNotesGreyRect;
	    
	    this.setAlignment(Pos.CENTER);
//	    if (this.pane != null) this.pane.getChildren().clear();
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
	    
	    double totalWidth = this.col * widthPerCell;
	    for (int i = 0; i < ROWS; i += 2) {
	    	this.createRowRect(i, this.widthPerCell, widthPerCell * this.col, this.heightPerCell, this.pane);
	    }
	    
	    for (int i = 0; i <= ROWS; ++i) {
	    	pane.getChildren().add(new Line(0, i*heightPerCell, totalWidth, i*heightPerCell));
	    }
	    
	    for (int j = 0; j <= this.col; ++j) {
	    	Line ln = new Line(j*widthPerCell, 0, j*widthPerCell, ROWS * heightPerCell);
	    	if (j % this.numCellsPerMeasure == this.measureOffset) {
	    		ln.setStrokeWidth(2.5);
	    		ln.setStroke(Color.BLACK);
	    	} else {
	    		ln.setStroke(Color.GRAY);
	    	}
	    	pane.getChildren().add(ln);
	    }
	    
	    this.getChildren().add(pane);
	    this.pane.setStyle("-fx-background-color: lavender ");
	    
	    this.pane.setOnDragDetected(e -> {
	    	if (e.isSecondaryButtonDown()) {
	    		System.out.println("Drag detected " + e.getX() + " " + e.getY());
	    		this.selectionMouseRect = getSelectionRect(e);
	    		
	    	} else if (e.isPrimaryButtonDown()) {
	    		//Drag selected notes if current mouse position is over a selected note
	    		int rectIndex = get1DCoord(this.heightPerCell, this.widthPerCell, e.getX(), e.getY());
	    		rectIndexWhereDragStarted = rectIndex;
	    		if (this.rectArr[rectIndex] != null && this.rectArr[rectIndex].isSelected) {
	    			System.out.println("DRAGGING STARTED");
	    			this.draggingNotes = true;
	    			HashSet<RectangleNote> hs = getAllSelectedNotes();
	    			RectangleNote primaryNoteForDragging = this.rectArr[rectIndex];
	    			if (e.isControlDown()) {
	    				//Make COPIES of these notes if control down, but don't add them to rectArr.
	    				//Do show them on the pane.
	    				this.selectedNotesRectHS = new HashSet<RectangleNote>();
	    				for (RectangleNote rn : hs) {
	    					RectangleNote rnCopy = rn.copy();
	    					if (rn == primaryNoteForDragging) this.selectedPrimaryNoteForDragging = rnCopy;
	    					this.selectedNotesRectHS.add(rnCopy);
	    					this.pane.getChildren().add(rnCopy);
	    				}
	    				this.copyingNotes = true;
	    			} else {
	    				this.selectedNotesRectHS = hs;
	    				this.selectedPrimaryNoteForDragging = primaryNoteForDragging;
	    				this.copyingNotes = false;
	    			}
	    		} //end if (this.rectArr[rectIndex] != null && this.rectArr[rectIndex].isSelected) 
	    	} //end else if (e.isPrimaryButtonDown())
	    }); //end setOnDragDetected
	    
	    this.pane.setOnMouseDragged(e -> {
	    	if (e.isSecondaryButtonDown()) {
	    		System.out.println("MouseDragged " + e.getX() + " " + e.getY());
	    		this.selectionMouseRect = getSelectionRect(e);
	    		selectionMouseRect.setWidth(Math.abs(e.getX() - selectionRectX));
	    		selectionMouseRect.setHeight(Math.abs(e.getY() - selectionRectY));
	    		if (e.getX() < selectionRectX) {
	    			selectionMouseRect.setX(e.getX());
	    		}
	    		if (e.getY() < selectionRectY) {
	    			selectionMouseRect.setY(e.getY());
	    		}
	    	} else if (e.isPrimaryButtonDown() && this.selectedNotesRectHS != null && this.draggingNotes) {
	    		//Drag selected notes. Begin with the primary selected note, then offset all other notes by the same
	    		double formerX = this.selectedPrimaryNoteForDragging.getX();
	    		double formerY = this.selectedPrimaryNoteForDragging.getY();
	    		this.selectedPrimaryNoteForDragging.setX(e.getX());
	    		this.selectedPrimaryNoteForDragging.setY(e.getY());
	    		double offsetX = formerX - this.selectedPrimaryNoteForDragging.getX();
	    		double offsetY = formerY - this.selectedPrimaryNoteForDragging.getY();
	    		for (RectangleNote rn : this.selectedNotesRectHS) {
	    			if (rn == this.selectedPrimaryNoteForDragging) continue;
	    			rn.setX(rn.getX() - offsetX);
	    			rn.setY(rn.getY() - offsetY);
	    		}

	    	} //end if if (e.isSecondaryButtonDown()) / else
	    });
	    	    
	    this.pane.setOnMouseReleased(e -> {
	    	
	    	if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
	    		System.out.println("Mouse Released " + e.getX() + " " + e.getY());
	    		
	    		if (selectionMouseRect != null && e.getButton() == MouseButton.SECONDARY) {
	    			this.pane.getChildren().remove(selectionMouseRect); //no longer need to visualize this rectangle on GUI
		    		//get all notes inside (or in any way intersecting) the selection rectangle
			    	int xStart = (int) (Math.min(e.getX(), selectionRectX) / widthPerCell);
		        	int yStart = (int) (Math.min(e.getY(), selectionRectY) / heightPerCell);
		        	int xEnd = (int) (Math.max(e.getX(), selectionRectX) / widthPerCell);
		        	int yEnd = (int) (Math.max(e.getY(), selectionRectY) / heightPerCell);
		        	System.out.printf("Rectangle spans the following corners: (%s, %s) to (%s, %s)", xStart, yStart, xEnd, yEnd);
		        	
		        	//Expand the selection rectangle to completely span all rows and columns that it intersects
		        	this.selectionMouseRect = new SelectedNotesRectangle(Math.max(0, xStart), Math.min(this.col-1, xEnd),
		        			Math.max(0, yStart), Math.min(ROWS-1, yEnd));
		        	//Select all (non-melody) notes contained in this selection rectangle
		        	this.selectNotesInMouseRect(xStart, xEnd, yStart, yEnd);
		        	this.pane.getChildren().remove(selectionMouseRect);
		        	this.selectedNotesRectHS = this.getAllSelectedNotes();
		        	selectionMouseRect = null;
//	    	    	this.selectedNotesRectHS = null;
//	    	    	this.selectedPrimaryNoteForDragging = null;
	    		} else if (this.selectedNotesRectHS != null && e.getButton() == MouseButton.PRIMARY && this.draggingNotes) {
	    			//Set notes down to the nearest (rounded row and col) spots, and update rectArr indices
	    			//If there is at least one note whose new location would be out of range, then cancel and
	    			//     set all selected notes to their original positions.
	    			int rect1DCoordinate = get1DCoord(this.heightPerCell, this.widthPerCell, e.getX(), e.getY());    	
	            	//Compare primary selected note's original location to this new location, and calculate X,Y offsets
	            	//Then see if every other selected note's new location would be within bounds
	            	System.out.println("Original colidx, rowidx: " + 
	            						this.selectedPrimaryNoteForDragging.colIdx + " " + 
	            						this.selectedPrimaryNoteForDragging.rowIdx);
	            	int newColIdxForPrimarySelectedNote = this.getCol(rect1DCoordinate);
	            	int newRowIdxForPrimarySelectedNote = this.getRow(rect1DCoordinate);
	            	
	            	int offsetCol = newColIdxForPrimarySelectedNote - this.selectedPrimaryNoteForDragging.colIdx;
	            	int offsetRow = newRowIdxForPrimarySelectedNote - this.selectedPrimaryNoteForDragging.rowIdx;
	            	
	            	System.out.println("OffsetCol, OffsetRow : " + offsetCol + " " + offsetRow);
	            	boolean allValid = true;
	            	
	            	//Check to see if these notes' new locations are taken by other notes
	            	//or if they're out of range
	            	for (RectangleNote rn : this.selectedNotesRectHS) {
	            		if (!isValid(rn.colIdx + offsetCol, rn.rowIdx + offsetRow) ||
	            			!isValid(rn.colIdx + offsetCol + rn.length - 1, rn.rowIdx + offsetRow)) {
		            		allValid = false;
		            		break;
	            		} else if (occupiedByOtherNotes(rn.colIdx + offsetCol, rn.colIdx + offsetCol + rn.length - 1,
	            										rn.rowIdx + offsetRow, rn)) {
	            			allValid = false;
	            			break;
	            		}
	            	} //end for
	            	if (allValid) {
	            		System.out.println("Moving or copying notes as the case may be...");
	            		if (this.copyingNotes) {
	            			copySelectedNotesViaMouse(this.selectedNotesRectHS, offsetCol, offsetRow);
	            		} else moveSelectedNotes(this.selectedNotesRectHS, offsetCol, offsetRow);
	            	} else { //just reset all notes to original. If copying notes, delete copied notes
	            		System.out.println("Can't move all notes since some of these notes' new locations would be out of range.");
	            		if (this.copyingNotes) {
	            			for (RectangleNote rn : this.selectedNotesRectHS) this.pane.getChildren().remove(rn);
	            		} else {
	            			moveSelectedNotes(this.selectedNotesRectHS, 0,0);
	            		}
	            	}
	            	this.resetAllCopyRelatedVars();
	    		} //end else if (this.selectedNotesRectHS != null && e.getButton() == MouseButton.PRIMARY)
	    	} //end if (e.getEventType() == MouseEvent.MOUSE_RELEASED) {
	    	
	    });
	    
	    this.pane.setOnMousePressed(e -> {
	    	this.rectIndexWhereDragStarted = this.get1DCoord(this.heightPerCell, this.widthPerCell, e.getX(), e.getY());
	    });
	    
	    this.pane.setOnMouseClicked(e -> {
	    	
	    	//put a border around this measure
	    	this.pianoRollGUI.focus(this, false, false);  //focus this measure, but don't autoscroll and don't set active column to 0.
	    	int rect1DCoordinate = get1DCoord(this.heightPerCell, this.widthPerCell, e.getX(), e.getY());
	    	int leftCornerCell = (int) (e.getX() / widthPerCell);
        	int upperCornerCell = (int) (e.getY() / heightPerCell);
        	
        	//The mouse release must occur in the same place as where mouse was first clicked, otherwise (if drag occurred),
        	//do nothing.
        	System.out.println(rect1DCoordinate + " " + this.rectIndexWhereDragStarted);
        	if (rect1DCoordinate != this.rectIndexWhereDragStarted) {
        		rectIndexWhereDragStarted = -1;
        		return;
        	}
        	
        	//Reset place where drag started
        	rectIndexWhereDragStarted = -1;
        	
        	//If right click, create column rect
	    	if (e.getButton() == MouseButton.SECONDARY) {
	    		System.out.println("Right click!");
	    		System.out.println("no column rectangle here.");
	    		this.createColRect(leftCornerCell, widthPerCell, ROWS, heightPerCell, this.pane);
	    		deSelectAll();
	    		return;
	    	}
	    	
	    	//If this rectangle is contained in the array it means this pitch is currently activated. Deactivate it if control is down,
	    	//otherwise toggle between select / deselect
	    	if (this.rectArr[rect1DCoordinate] != null) {
	    		if (e.isControlDown()) {
	    			this.deleteRect(rect1DCoordinate, this.rectArr, this.pane);
	    		} else if (e.isShiftDown()){  //If shift down, curtail length of note
	    			RectangleNote currRN = this.rectArr[rect1DCoordinate];
	    			int startRectArrIdx = currRN.index;
	    			int rectLength = currRN.length;
	    			if (rect1DCoordinate < startRectArrIdx + rectLength - 1) {
	    				int curtailLength = startRectArrIdx + rectLength - 1 - rect1DCoordinate;
	    				//Curtail length of note. If the note is currently selected, curtail length of every selected note
	    				//by that much, up to a minimum of length 1, whichever is greater.
	    				if (currRN.isSelected) {
	    					this.selectedNotesRectHS = this.getAllSelectedNotes();
	    					for (RectangleNote rn : this.selectedNotesRectHS) {
	    						this.curtailNoteLength(rn, curtailLength);
	    					}
	    				} else {
	    					this.curtailNoteLength(currRN, curtailLength);
	    				}
	    				this.resetAllCopyRelatedVars();
	    			}
	    		} else { //just select or deselect the note
//	    			this.createColRect(leftCornerCell, widthPerCell, ROWS, heightPerCell, this.pane);
	    			this.rectArr[rect1DCoordinate].setSelected(!this.rectArr[rect1DCoordinate].isSelected);
	    			if (this.rectArr[rect1DCoordinate].isSelected) {
	    				this.pianoRollGUI.playSound(this.computePitch(rect1DCoordinate));
	    			}
	    			removeSelectedNotesGreyRect();
	    		}
//	    		deSelectAll();	    				    	
	    	} else if (e.isShiftDown()){  //Extend duration of note, unless it's melody
	        	if (leftCornerCell < this.col) {
	        		int currIdx = get1DCoord(heightPerCell, widthPerCell, e.getX(), e.getY());
	        		//check the previous note that exists, if any, so we can link
	        		int prevIdx = -1;
	        		int extensionLength = -1;
	        		for (int i = currIdx-1; i >= currIdx - (currIdx % this.col); --i) {
	        			if (this.rectArr[i] != null) {
	        				if (this.rectArr[i].isMelody) break;  //if melody, just break out
	        				prevIdx = this.convertTo1DCoord(this.computeRow(this.computePitch(i)), this.rectArr[i].colIdx);
//	        				prevIdx = this.rectArr[i].startIdx;
	        				extensionLength = currIdx - i;
	        				break;
	        			}
	        		}
	        		if (prevIdx == -1) prevIdx = currIdx;
	        		System.out.printf("Extending note from %s to %s\n", prevIdx, currIdx);
	        		
	        		
	        		//If the note to be extended is "selected", then search for all other selected notes
	        		//and extend them by the same length (or, if extension is "blocked" by another note, then 
	        		//extend them as much as possible without overwriting another note
	        		if (prevIdx < currIdx && this.rectArr[prevIdx] != null && this.rectArr[prevIdx].isSelected) {
//	        			int extensionLength = currIdx - prevIdx;
	        			for (int i = 0; i < rectArr.length; ++i) {
	        				if (rectArr[i] == null) continue;
	        				if (rectArr[i].isSelected) {
	        					//Find the length to extend this note by, while not overwriting any other note
	        					int endOfPrevNote = i + rectArr[i].length - 1;
	        					int j = endOfPrevNote;
	        					//Make sure not to extend to the next row, and that the length stays under the extensionLength.
	        					while (j+1 < this.rectArr.length && j / this.col == (j+1) / this.col && j - endOfPrevNote < extensionLength) {
	        						if (rectArr[j+1] != null) break;
	        						else j++;
	        					} //end while
	        					this.createNote(i, j, false, ColorIntMap.colorHashMap.get(rectArr[i].color),
	        							ColorIntMap.colorHashMap.get(rectArr[i].origColor));
	        					i = j;
//	        					rectArr[i].setSelected(true);
	        				} //end if selected
	        			} //end for i
//	        			this.createNote(prevIdx, currIdx, false);
//	        			rectArr[prevIdx].setSelected(true);
	        			deSelectAll();
	        		} //end if
	        		else {
		        		//Just extend the current note if one exists
	        			System.out.println(rectArr[prevIdx]);
	        			if (this.rectArr[prevIdx] != null) {
			        		this.createNote(prevIdx, currIdx, true, ColorIntMap.colorHashMap.get(rectArr[prevIdx].color),
			        				ColorIntMap.colorHashMap.get(rectArr[prevIdx].origColor));
			        		deSelectAll();
	        			}
	        		}
	        	}
	    	} else {  //Just notate a single note
	    		deSelectAll();
	    		//Coordinate of Rectangle with left upper corner at (leftCornerCell, upperCornerCell)
	        	if (leftCornerCell < this.col) {
	        		this.createRect(leftCornerCell, widthPerCell, upperCornerCell, heightPerCell, this.pane, this.rectArr, rect1DCoordinate);
			    	int pitch = computePitch(rect1DCoordinate);
			    	this.play(pitch);
	        	}
				
	    	}  //end if/else
	    });
	    
	    //Draw all rectangle notes and column rects, selected rects etc.

//	    if (this.selectedNotesGreyRect != null) {
//	    	this.selectedNotesGreyRect = new SelectedNotesRectangle(selectedNotesGreyRect.xStart,
//	    																selectedNotesGreyRect.xEnd,
//	    																selectedNotesGreyRect.yStart,
//	    																selectedNotesGreyRect.yEnd);
//	    	this.pane.getChildren().add(this.selectedNotesGreyRect);
//	    }
	    
	    for (int i = 0; i < this.rectArr.length; ++i) {
	    	if (this.rectArr[i] != null) {
	    		//check to see how long the rectangle note lasts
	    		RectangleNote rn = this.rectArr[i];
	    		this.pane.getChildren().remove(rn);
	    		RectangleNote newRN = new RectangleNote(rn.colIdx * this.widthPerCell, rn.rowIdx * this.heightPerCell, 
	    				widthPerCell, heightPerCell, rn.colIdx, rn.rowIdx, rn.length,
	    				rn.isMelody, rn.isSelected, rn.color, rn.origColor, rn.index, rn.isMute);
	    		
	    		int j = i;
	    		while (i+1 < this.rectArr.length && i / this.col == (i+1) / this.col && this.rectArr[i] == this.rectArr[i+1]) ++i;
	    		for (int k = j; k <= i; ++k) this.rectArr[k] = newRN;
	    		this.pane.getChildren().add(newRN);
	    		
	    	}
	    } //end for i
	    
	    this.resetAllCopyRelatedVars();
	    this.pianoRollGUI.setFocusedMeasure(this);
	    this.setActiveColumn(this.activeCol < 0 ? 0 : this.activeCol);
	    this.pianoRollGUI.refresh(this);	    
	}
	
	private boolean isValid(int colIdx, int rowIdx) {
		return (colIdx >= 0 && colIdx < this.col && rowIdx >= 0 && rowIdx < ROWS);
	}
	
	private void resetAllCopyRelatedVars() {
		this.selectionMouseRect = null;
		this.copyingNotes = false;
    	this.selectedNotesRectHS = null;
    	this.selectedPrimaryNoteForDragging = null;
    	this.leftmostNoteForCopyPasting = null;
    	this.draggingNotes = false;
	}
	
	private void curtailNoteLength(RectangleNote rn, int curtailLength) {
		int origLength = rn.length;
		int newLength = Math.max(origLength - curtailLength, 1);
		rn.length = newLength;
		rn.setWidth(newLength * this.widthPerCell);
		for (int i = rn.index + newLength; i <= rn.index + origLength - 1; ++i) {
			this.rectArr[i] = null;
		}
	}
	
	private boolean occupiedByOtherNotes(int colIdxStart, int colIdxEnd, int rowIdx, RectangleNote rn) {
		for (int colIdx = colIdxStart; colIdx <= colIdxEnd; ++colIdx) {
			int rectArrIndex = this.convertTo1DCoord(rowIdx, colIdx); 
			if (this.rectArr[rectArrIndex] != null && this.rectArr[rectArrIndex] != rn) {
				return true;
			}
		} //end for
		return false;
	}
	
	private void moveSelectedNotes(HashSet<RectangleNote> rnHS, int offsetCol, int offsetRow) {
		for (RectangleNote rn : rnHS) {
			//Retain colors and selected mode
			this.deleteRect(rn.index, this.rectArr, this.pane);
			this.createRect(rn.colIdx + offsetCol, this.widthPerCell, rn.rowIdx + offsetRow, heightPerCell, 
							this.pane, this.rectArr, this.convertTo1DCoord(rn.rowIdx + offsetRow, rn.colIdx + offsetCol),
							rn.length, false,
							rn.isMelody, rn.isSelected, rn.color, rn.origColor);
		} //end for
		
	}
	
	public void selectAllNotes() {
		for (int i = 0; i < rectArr.length; ++i) {
			if (rectArr[i] != null) rectArr[i].setSelected(true);
		}
	}
	
	/**
	 * Saves all selected notes to HashSet
	 */
	public void copySelectedNotesViaMouse(HashSet<RectangleNote> rnHS, int offsetCol, int offsetRow) {
		this.copySelectedNotes(rnHS, offsetCol, offsetRow, true);
	}
	
	public void copySelectedNotesViaKeyboard(HashSet<RectangleNote> rnHS, int offsetCol, int offsetRow) {
		this.copySelectedNotes(rnHS, offsetCol, offsetRow, false);
	}
	
	private void copySelectedNotes(HashSet<RectangleNote> rnHS, int offsetCol, int offsetRow, boolean deleteOrigNotes) {
		for (RectangleNote rn : rnHS) {
			//Retain colors and selected mode
//			this.deleteRect(rn.index, this.rectArr, this.pane);
			if (deleteOrigNotes) this.pane.getChildren().remove(rn);
			this.createRect(rn.colIdx + offsetCol, this.widthPerCell, rn.rowIdx + offsetRow, heightPerCell, 
							this.pane, this.rectArr, this.convertTo1DCoord(rn.rowIdx + offsetRow, rn.colIdx + offsetCol),
							rn.length, false,
							rn.isMelody, rn.isSelected, rn.color, rn.origColor);
		} //end for
	}
	
	/**
	 * Copies all selected notes over to new column Index. OVERWRITES any other notes in the way.
	 */
	public void pasteSelected() {
//		int colIndex = this.activeCol;
		pasteSelected(this.activeCol);
	}
	private void pasteSelected(int startColIndex) {
		pasteSelected(startColIndex, this.selectedNotesRectHS);
	}
	
	private void pasteSelected(int startColIndex, HashSet<RectangleNote> hs) {
		this.selectedNotesRectHS = hs;
		//Check to see if the new location for pasting would be out of range or occupied by other notes, if so do nothing
		if (this.selectedNotesRectHS == null || this.selectedNotesRectHS.isEmpty() ||
				this.leftmostNoteForCopyPasting == null) return;
		int newColIdxForPrimarySelectedNote = startColIndex;
		int newRowIdxForPrimarySelectedNote = this.leftmostNoteForCopyPasting.rowIdx;

		int offsetCol = newColIdxForPrimarySelectedNote - this.leftmostNoteForCopyPasting.colIdx;
		int offsetRow = newRowIdxForPrimarySelectedNote - this.leftmostNoteForCopyPasting.rowIdx;

		System.out.println("OffsetCol, OffsetRow : " + offsetCol + " " + offsetRow);
		boolean allValid = true;

		//Check to see if these notes' new locations are taken by other notes
		//or if they're out of range
		for (RectangleNote rn : this.selectedNotesRectHS) {
			if (!isValid(rn.colIdx + offsetCol, rn.rowIdx + offsetRow) ||
					!isValid(rn.colIdx + offsetCol + rn.length - 1, rn.rowIdx + offsetRow)) {
				allValid = false;
				break;
			} else if (occupiedByOtherNotes(rn.colIdx + offsetCol, rn.colIdx + offsetCol + rn.length - 1,
					rn.rowIdx + offsetRow, rn)) {
				allValid = false;
				break;
			}
		} //end for
		if (allValid) {
			this.copySelectedNotesViaKeyboard(this.selectedNotesRectHS, offsetCol, offsetRow);
		}
		this.resetAllCopyRelatedVars();
	}
	
	
	public void storeSelectedNotesCopy(boolean copying) {
		this.selectedNotesRectHS = this.getAllSelectedNotes();
		
		int leftmostColIndex = Integer.MAX_VALUE;
		this.leftmostNoteForCopyPasting = null;
		
		//Save the leftmost note for pasting, and if we're cutting (not copying), then also remove the cut notes from pane
		for (RectangleNote rn : this.selectedNotesRectHS) {
			if (rn.colIdx < leftmostColIndex) {
				leftmostColIndex = rn.colIdx;
				this.leftmostNoteForCopyPasting = rn;
			}
			if (!copying) {
				this.deleteRect(rn.index, rectArr, pane);
//				this.pane.getChildren().remove(rn);
			}
		}
		System.out.println(this.selectedNotesRectHS);
	}
	
	/**
	 * 
	 * @param colStartIdx
	 * @param colEndIdx
	 * @return return a 2D array of colors pertaining to the notes contained in the provided col indices, inclusive.
	 * 
	 * TODO work with this to create more options for user
	 */
	public int[][] getPitchArray(int colStartIdx, int colEndIdx) {
		int[][] ret = new int[ROWS][colEndIdx - colStartIdx + 1];
		for (int i = 0; i < ROWS; ++i) {
			for (int j = colStartIdx; j <= colEndIdx; ++j) {
				RectangleNote rn = this.rectArr[this.convertTo1DCoord(i, j)];
				//return an array of ints pertaining to each pitch's color as mapped to colorHashMap
				if (rn != null) System.out.printf("Color at %s, %s = %s", i, j, ColorIntMap.colorHashMap.get(rn.color));
				ret[i][j-colStartIdx] = (rn == null ? -1 : ColorIntMap.colorHashMap.get(rn.color));
				
			} //end for j
		} //end for i
		for (int i = 0; i < ROWS; ++i)
			System.out.println(Arrays.toString(ret[i]));
		return ret;
	}
	
	public int getColor(int col, int row) {
		RectangleNote rn = this.rectArr[this.convertTo1DCoord(row, col)];
		if (rn==null) return -1; //-1 indicates there is no note here
		if (ColorIntMap.colorHashMap.get(rn.color)==null) throw new RuntimeException(""
				+ "Unexpected error: The current note's color does not have a corresponding integer value mapped to it.");
		return ColorIntMap.colorHashMap.get(rn.color);
	}
	
	public boolean setColor(int col, int row, int colorInt) {
		int rectIndex = this.convertTo1DCoord(row, col);
		RectangleNote rn = this.rectArr[rectIndex];
		if (rn == null) {
			//create new note of length 1, then color it
			rn = new RectangleNote(col * this.widthPerCell, row * this.heightPerCell, this.widthPerCell, this.heightPerCell,
					col, row, 1, false, false, Color.GREEN, Color.GREEN, rectIndex, false);
		}
		rn.setColor(colorInt);
		if (ColorIntMap.colorHashMap.get(rn.color) == colorInt) return true;
		return false;
	}
	
	public boolean isStartOfNote(int col, int row) {
		int rectIndex = this.convertTo1DCoord(row, col);
		RectangleNote rn = this.rectArr[rectIndex];
		if (rn == null || rn.colIdx != col) return false;
		return true;
	}
	
	public void muteSelectedNotes(boolean mute) {
		muteSelectedNotes(this.getAllSelectedNotes(), mute);
	}
	
	private void muteSelectedNotes(HashSet<RectangleNote> rnHS, boolean mute) {
		for (RectangleNote rn : rnHS) {
			rn.setMute(mute);
			rn.setSelected(false);
		}
	}
	
	/**
	 * Inserts (and shifts cols to the right) the given pattern of notes (rnHS), beginning at startColIdx and up to
	 * (and including) endColIdx, skipping by colOffset each time.
	 * note: assumes startColIdx <= endColIdx.
	 * The no. of times to insert the given pattern of notes equals (endColIdx - startColIdx)/colOffset + 1. 
	 * @param rnHS
	 * @param startColIdx
	 * @param colOffset
	 * @param endColIdx
	 */
	public void insertPattern(HashSet<RectangleNote> rnHS, int startColIdx, int colOffset, int endColIdx) {
		if (startColIdx > endColIdx) return;
		int numOfTimesToInsert = 1 + ((endColIdx - startColIdx) / colOffset);
		int leftMostIndex = Integer.MAX_VALUE;
		for (RectangleNote rn : rnHS) {
			leftMostIndex = Math.min(leftMostIndex, rn.colIdx);
		}
		
		int numColsToInsert = 0;
		ArrayList<WrapperNote> wnAL = new ArrayList<>();
		for (RectangleNote rn : rnHS) {
			numColsToInsert = Math.max(numColsToInsert, rn.colIdx - leftMostIndex + rn.length);
			wnAL.add(new WrapperNote(this.computePitchFromRow(rn.rowIdx), 
									startColIdx + (rn.colIdx - leftMostIndex), 
									rn.color, 
									rn.origColor, 
									rn.length)
									);
		}
		
		int currColIdx = startColIdx;
		System.out.println("currColIdx : " + currColIdx);
		for (int i = 0; i < numOfTimesToInsert; ++i) {
			this.pianoRollGUI.insertCells(numColsToInsert, currColIdx, true, wnAL);
			if (i + 1 < numOfTimesToInsert) {
				for (WrapperNote wn : wnAL) {
					wn.setIdx(wn.getColIdx() + numColsToInsert + colOffset);
				}
				currColIdx = currColIdx + numColsToInsert + colOffset;
				System.out.println("currColIdx : " + currColIdx);
			}
			
		}//end for i
	}
	
	/**
	 * Insert selected notes into the active column, shifting the other columns to the right
	 */
	public void insertNotes() {
		insertNotes(this.selectedNotesRectHS, this.getActiveColumn());
	}
	/**
	 * Insert notes into the spot right before specified column and push out the colIdx + columns to its right
	 * @param rnAL
	 * @param colIdx
	 */
	private void insertNotes(HashSet<RectangleNote> rnHS, int colIdx) {
		//Go thru the notes and find out the leftmost index
		int numColsToInsert = 0;
		int leftMostIndex = Integer.MAX_VALUE;
		if (rnHS == null || rnHS.isEmpty()) return;
//		int leftMostIndex = rnAL.get(0).colIdx;
		for (RectangleNote rn : rnHS) {
			leftMostIndex = Math.min(leftMostIndex, rn.colIdx);
		}
		
		ArrayList<WrapperNote> wnAL = new ArrayList<>();
		
		//Go thru the notes again, and check each note's length to see how much column space we need to make
		//Also create arraylist of WrapperNotes, so we can feed it into insertCells() method in PianoRollGUI
		for (RectangleNote rn : rnHS) {
			numColsToInsert = Math.max(numColsToInsert, rn.colIdx - leftMostIndex + rn.length);
			wnAL.add(new WrapperNote(this.computePitchFromRow(rn.rowIdx), 
									colIdx + (rn.colIdx - leftMostIndex), 
									rn.color, 
									rn.origColor, 
									rn.length)
									);
		}
		
		this.pianoRollGUI.insertCells(numColsToInsert, colIdx, true, wnAL);		
	}
	
	public void zoomOut() {
		zoom(false);
	}
	
	public void zoomIn() {
		zoom(true);
	}
	
	private void zoom(boolean zoomin) {
		double epsilon = 0.00001;
		double zoomRateWidth = (ScorePane.MAX_WIDTH_PER_CELL - ScorePane.MIN_WIDTH_PER_CELL) / 20.0;
		double zoomRateHeight = (ScorePane.MAX_HEIGHT_PER_CELL - ScorePane.MIN_HEIGHT_PER_CELL) / 20.0;
		
		double newWidthPerCell = (zoomin ? this.widthPerCell + zoomRateWidth : this.widthPerCell - zoomRateWidth);
		double newHeightPerCell = (zoomin ? this.heightPerCell + zoomRateHeight : this.heightPerCell - zoomRateHeight);
		
		if (newWidthPerCell > ScorePane.MAX_WIDTH_PER_CELL + epsilon ||
			newWidthPerCell < ScorePane.MIN_WIDTH_PER_CELL - epsilon ||
			newHeightPerCell > ScorePane.MAX_HEIGHT_PER_CELL + epsilon ||
			newHeightPerCell < ScorePane.MIN_HEIGHT_PER_CELL - epsilon) {
			return;
		}
		
		this.widthPerCell = newWidthPerCell;
		this.heightPerCell = newHeightPerCell;
		
		//re-draw measure
		reDrawMeasure();
	}
	
	public void reDrawMeasure() {
		redrawMeasure(col, this.numCellsPerMeasure, this.measureOffset, ts, measureNum, pianoRollGUI, prev, next, activeCol, rectArr, columnRectArr, 
	    		heightPerCell, widthPerCell);
	}
	
	public int getColsPerMeasure() {
		return this.numCellsPerMeasure;
	}
	
	private void selectNotesInMouseRect(int xStart, int xEnd, int yStart, int yEnd) {
		for (int i = 0; i < this.rectArr.length; ++i) {
    		//Change selected note rectangles to black, UNLESS it's a melody.
    		RectangleNote rn = rectArr[i];
    		if (rn == null || rn.isMelody) continue;
    		System.out.println(rn.colIdx + " " + rn.length);
//    		if (intersects(rn.startIdx, rn.startIdx + rn.length - 1, xStart, xEnd) &&
//    			intersects(this.getRow(i), this.getRow(i), yStart, yEnd)) {
    		if (containedInGreyRect(rn, this.selectionMouseRect, this.getRow(i))) {
//    			rn.setFill(Color.BLACK);
//    			rn.color = Color.BLACK;
//    			rn.isSelected = true;
    			rn.setSelected(true);
    		} //end if
    	} //end for i
	}
	
	private boolean containedInGreyRect(RectangleNote rn, SelectedNotesRectangle snRect, int row) {
		if (snRect == null) return false;
		return (intersects(rn.colIdx, rn.colIdx + rn.length - 1, snRect.xStart, snRect.xEnd) &&
    			intersects(row,row, snRect.yStart, snRect.yEnd));
	}
	
//	private void shadeCol(int colStart, int colEnd, int rowStart, int rowEnd) {
//		this.selectionMouseRect = new SelectedNotesRectangle(colStart, colEnd, rowStart, rowEnd);
//		//Don't add this to screen. Just adds confusion.
//	}
	
	private void deSelectAll() {
		for (RectangleNote rn : this.rectArr) {
			if (rn == null || !rn.isSelected) continue;
//			rn.setFill(rn.isMelody ? Color.DARKRED : Color.GREEN);
//			rn.color = rn.isMelody ? Color.DARKRED : Color.GREEN;
//			rn.isSelected = false;
			rn.setSelected(false);
		}
		
		//Also de-shade
		this.removeSelectedNotesGreyRect();
		
		//Finally set hashset to null
//		this.selectedNotesRectHS = null;
	}
	
	private void removeSelectedNotesGreyRect() {
		if (this.selectionMouseRect != null) {
			this.pane.getChildren().remove(selectionMouseRect);
			selectionMouseRect = null;
		}
	}
	
	private boolean intersects(int fromA, int toA, int fromB, int toB) {
		return !(toA < fromB || fromA > toB);
	}
	
	private SelectedNotesRectangle getSelectionRect(MouseEvent e) {
		if (selectionMouseRect == null) { 
			this.selectionMouseRect = new SelectedNotesRectangle(e.getX(), e.getY(), 0,0);
			this.selectionMouseRect.setFill(Color.TRANSPARENT);
			this.selectionMouseRect.setStroke(Color.DARKSLATEGRAY);
			this.selectionMouseRect.setStrokeWidth(3);
			this.pane.getChildren().add(selectionMouseRect);
			this.selectionRectX = selectionMouseRect.getX();
			this.selectionRectY = selectionMouseRect.getY();
			
		}
		return this.selectionMouseRect;
	}
	
	/**
	 * Creates a note on the measure, at the specified column index.
	 * Captures the duration of the note and specifies the length of the note accordingly.
	 * @param n
	 * @param colIdx
	 * @param durationPerCell
	 */
	public void createNote(WrapperNote n, int colIdx, int durationPerCell, boolean playback) {
		if (n.getPitch() == -1) return;
		int lengthOfNote = n.getDuration() / durationPerCell;
		int rowIdx = this.computeRow(n.getPitch());
		int startIdx = this.convertTo1DCoord(rowIdx, colIdx);
		int endIdx = startIdx + lengthOfNote - 1;
		this.createNote(startIdx, endIdx, playback, n.getColorInt(), n.getOrigColorInt());
	}
	
	public void createNote(Note n, int colIdx, int durationPerCell, boolean playback) {
		createNote(new WrapperNote(n, colIdx), colIdx, durationPerCell,playback);
	}
	
	/**
	 * Overloaded helper, invoked by the public createNote() method.
	 * @param startIdx
	 * @param endIdx
	 */
	private void createNote(int startIdx, int endIdx, boolean playback, int color, int origColor) {
		if (startIdx < 0 || startIdx >= rectArr.length || endIdx < 0 || endIdx >= rectArr.length) {
			System.out.println("ERROR: index out of bounds, one or more MIDI notes aren't supported by this sequencer");
			return;
		}
		
		//Delete any existing note in this range
		if (this.rectArr[startIdx] != null) {
			this.pane.getChildren().remove(rectArr[startIdx]);
		}
		if (this.rectArr[endIdx] != null) {
			this.pane.getChildren().remove(rectArr[endIdx]);
		}
		
		for (int i = startIdx; i <= endIdx; ++i) this.rectArr[i] = null;
		
		
		//Create two notes then combine them.
		this.createRect(getCol(startIdx), widthPerCell, getRow(startIdx), heightPerCell, this.pane, this.rectArr, startIdx);
		this.createRect(getCol(endIdx), widthPerCell, getRow(endIdx), heightPerCell, this.pane, this.rectArr, endIdx);
		
		RectangleNote rn = this.combineRect(this.rectArr[startIdx], this.rectArr[endIdx]);
//		rn.index = startIdx;
		this.pane.getChildren().remove(this.rectArr[startIdx]);
		this.pane.getChildren().remove(this.rectArr[endIdx]);
		this.rectArr[startIdx] = null;
		this.rectArr[endIdx] = null;

		rn.setColor(color);
		rn.setOrigColor(origColor);
		
		for (int i = startIdx; i <= endIdx; ++i) {
			this.rectArr[i] = rn;
		}
//		if (startIdx == endIdx) rn.setFill(Color.GREEN);
		this.pane.getChildren().add(rn);
		if (playback) this.play(computePitch(endIdx));
	}
	
	//Notate pitch given the array index.
//	private void createPitch(int rect1DCoordinateIdx) {
//		//convert the 1d index to 2-d rowindex and colindex, then create the rectangle.
//		this.createRect(getCol(rect1DCoordinateIdx), widthPerCell, getRow(rect1DCoordinateIdx), heightPerCell, pane, rectArr, rect1DCoordinateIdx);
//	}
	
	private int getRow(int rect1DCoordinateIdx) {
		return rect1DCoordinateIdx / this.col;
	}
	private int getCol(int rect1DCoordinateIdx) {
		return rect1DCoordinateIdx % this.col;
	}
	
	public int getNumCells() {
		return this.col;
	}
	
	public void setNumColsPerMeasure(int n) {
		this.numCellsPerMeasure = n;
	}
	
	public int getNumColsPerMeasure() {
		return this.numCellsPerMeasure;
	}
	
	public void setMeasureOffset(int o) {
		this.measureOffset = o;
	}
	public int getOffset() {
		return this.measureOffset;
	}
	
	public double getWidthPerCell() {
		return this.widthPerCell;
	}
	public double getHeightPerCell() {
		return this.heightPerCell;
	}
	
	public void setWidthPerCell(double n) {
		this.widthPerCell = n;
	}
	
	public void setHeightPerCell(double n) {
		this.heightPerCell = n;
	}
	
	public void setPrev(ScorePane prev) {
		this.prev = prev;
	}
	
	public void setNext(ScorePane next) {
		this.next = next;
	}
	
	public ScorePane getPrev() {
		return this.prev;
	}
	
	public ScorePane getNext() {
		return this.next;
	}
	
	public void lockMelodyNotes(boolean lock) {
		//Get the top note in each column and lock them.
		for (int col = 0; col < this.col; ++col) {
			int index = this.convertTo1DCoord(0, col);
			for (int i = index; i < this.rectArr.length; i += this.col) {
				if (this.rectArr[i] != null) {
					if (col > 0 && this.rectArr[i - 1] == this.rectArr[i]) {}
					else this.rectArr[i].setMelody(lock);
					break;
				}
			} //end for i
		} //end for col
	}
	
	private RectangleNote combineRect(RectangleNote r1, RectangleNote r2) {
		int startIdx = Math.min(r1.colIdx, r2.colIdx);
		int length = Math.max(r1.colIdx + r1.length, r2.colIdx + r2.length) - startIdx;
		return new RectangleNote(Math.min(r1.getX(), r2.getX()), r1.getY(), 
				this.widthPerCell, this.heightPerCell, startIdx, r2.rowIdx, length,
				r1.isMelody, r1.isSelected, r1.color, r1.origColor, r1.index);
	}
	
	public void deleteAllNotes() {
		for (int i = 0; i < this.rectArr.length; i++) {
			this.deleteRect(i, rectArr, this.pane);
		}
	}
	
	private void deleteRect(int index, RectangleNote[] rectArr, Pane pane) {
		RectangleNote rect = this.rectArr[index];
		if (rect != null && !rect.isMelody) {
			pane.getChildren().remove(rect);
//	    	rectArr[index] = null;
	    	int[] rowcol = this.convertToRowCol(index);
	    	int r = rowcol[0];
	    	for (int i = rect.colIdx; i < rect.colIdx + rect.length; ++i) {
	    		rectArr[(this.convertTo1DCoord(r, i))] = null;
	    	}
// 	    	rect = null;
		}
	}
	
	private int[] convertToRowCol(int index) {
		int row = index / this.col;
		int col = index % this.col;
		return new int[] {row, col};
	}
	
	private void createRect(int colIndex, double widthPerCell, int rowIndex, double heightPerCell, Pane pane, 
			RectangleNote[] rectArr, int rectArrIndex) {
		createRect(colIndex, widthPerCell, rowIndex, heightPerCell, pane, rectArr, rectArrIndex, 1, true);
	}
	
	private void createRect(int colIndex, double widthPerCell, int rowIndex, double heightPerCell, Pane pane, 
			RectangleNote[] rectArr, int rectArrIndex, int length, boolean createColRectangle) {
		createRect(colIndex, widthPerCell, rowIndex, heightPerCell, pane, rectArr, rectArrIndex, 1, true,
				false, false, Color.GREEN, Color.GREEN);
	}
	
	private void createRect(int colIndex, double widthPerCell, int rowIndex, double heightPerCell, Pane pane, 
			RectangleNote[] rectArr, int rectArrIndex, int length, boolean createColRectangle,
			boolean isMelody, boolean isSelected, Color color, Color origColor) {
		if (rectArr[rectArrIndex] == null) {
			RectangleNote rect = new RectangleNote(colIndex*widthPerCell, rowIndex*heightPerCell, widthPerCell, 
													heightPerCell, colIndex, rowIndex, length,
													isMelody, isSelected, color, origColor, rectArrIndex);
	    	pane.getChildren().add(rect);
	    	for (int i = rect.index; i <= rect.index + length - 1; ++i)	rectArr[i] = rect;
	    	if (createColRectangle)	this.createColRect(colIndex, widthPerCell, ROWS, heightPerCell, pane);
		}
	}
	
	private void clearColRect() {
//		for (int i = 0; i < this.columnRectArr.length; ++i) {
		if (this.activeCol == -1) return;
		this.pane.getChildren().remove(this.columnRectArr[this.activeCol]);
		columnRectArr[this.activeCol] = null;
		this.activeCol = -1;
//		}
	}
	
	private void createColRect(int colIndex, double widthPerCell, int totalRows, double heightPerCell, Pane pane) {
		System.out.println("activeCol, colIndex: " + this.activeCol + " " + colIndex);
//		if (this.activeCol == colIndex) {
//			System.out.println("column rect already here. No need to do anything");
//			return;
//		}
		
		this.clearColRect();
		if (colIndex < 0 || colIndex >= this.col) {
			System.out.println("Cannot create a column rectangle as it's out of index bounds.");
			return;
		}
		Rectangle rect = new Rectangle(colIndex*widthPerCell, 0, widthPerCell, totalRows * heightPerCell);
		rect.setStroke(Color.BROWN);
		rect.setStrokeWidth(3);
		rect.setFill(Color.TRANSPARENT);
		pane.getChildren().add(rect);
		columnRectArr[colIndex] = rect;
		this.activeCol = colIndex;
		System.out.println("activeCol, colIndex: " + this.activeCol + " " + colIndex);
		System.out.println("Column Rect created at column " + colIndex);
		pianoRollGUI.updateInfoPane();
	}
	
	private void createRowRect(int rowIndex, double widthPerCell, double totalCellWidth, double heightPerCell, Pane pane) {
		Rectangle rect = new Rectangle(0, rowIndex * heightPerCell, totalCellWidth, heightPerCell);
		rect.setFill(Color.ALICEBLUE);
		pane.getChildren().add(rect);
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
	
	/**
	 * Delete pitch on current column index only
	 * @param pitch
	 * @param colIdx
	 */
	public void deletePitch(int pitch, int colIdx) {
		int row = this.computeRow(pitch);
//		System.out.println("Row: " + row);
		int index = convertTo1DCoord(row, colIdx);
//		System.out.println("Index: " + index);
		this.deleteRect(index, this.rectArr, this.pane);
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
	
	public int convertTo1DCoord(int rowIndex, int colIndex) {
		return rowIndex * this.col + colIndex;
	}
	
	public int getActiveColumn() {
//		for (int i = 0; i < this.columnRectArr.length; ++i) {
//			if (columnRectArr[i] != null) {
//				return i;
//			}
//		}
//		return -1;
		return this.activeCol;
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
		this.createColRect(colIndex, this.widthPerCell, ROWS, this.heightPerCell, this.pane);
	}
	
//	public double getMeasureWidth() {
//		return this.getWidth();
//	}
	
	public boolean advanceActiveColumn(boolean forward) {
		int activeColumn = getActiveColumn() + (forward ? 1 : -1);
		if (activeColumn >= this.col || activeColumn < 0) {
			return false;
		} else {
			this.clearColRect();
			this.createColRect(activeColumn, this.widthPerCell, ROWS, this.heightPerCell, this.pane);
			return true;
		}
	}
	
	public int getMeasureNum() {
		return measureNum;
	}
	
	public void setMeasureNum(int measureNum) {
		this.measureNum = measureNum;
		this.lbl.setText("Measure " + this.measureNum);
	}


	public void play(int pitch) {
		play(pitch, true);
	}
	
	public void play(int pitch, boolean realTimePlayback) {
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
		if (realTimePlayback) this.pianoRollGUI.playSound(pitch);
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
//		return ROWS - row + PianoRollGUI.MIN_PITCH;		
		return computePitchFromRow(row);
	}
	
	private int computePitchFromRow(int row) {
		return ROWS - row + PianoRollGUI.MIN_PITCH;
	}
	
	/**
	 * Looks at this measure and converts all the chords into a ChordSequence object.
	 * NOTE: If any chord in a given column is "invalid" (see below comments), then that chord is treated as null
	 * @return ChordSequence object
	 */
//	public ChordSequence getChordSeq() {
//		ChordSequence chordSeq = new ChordSequence(this.col);
//
//		for (int i = 0; i < chordSeq.length(); ++i) {
//			//get all notes in the current column and create a Chord.
//			//If the Chord is illegal or empty, e.g. it consists of more than 4 notes
//			//(we'll later change this to allow up to 5 notes), then set it to null
//			ArrayList<Integer> pitchAL = getAllNotesInColumn(i);
//			System.out.println(pitchAL);
//			if (pitchAL == null || pitchAL.size() == 0 || pitchAL.size() > 4) {
//				chordSeq.setChord(i, null);
//			} else {
//				Note[] noteArr = convertToNotes(pitchAL);
//				Chord c = new Chord(noteArr);
//				chordSeq.setChord(i, c);
//			}
//		} //end for i
//	 	
//		return chordSeq;
//	}
	
	public void findNextSelectedPattern() {
		HashSet<RectangleNote> hs = this.getAllSelectedNotes();
		if (hs == null || hs.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("No selected notes");
			alert.setContentText("Select one or more notes and try again.");
			alert.showAndWait();
			return;
		}
		
		ArrayList<RectangleNote> notesAL = this.patternSearch(hs);
		this.deSelectAll();
		if (notesAL != null && !notesAL.isEmpty()) {
			for (RectangleNote rn : notesAL) rn.setSelected(true);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Pattern not found");
			alert.setContentText("Reached end of score. Next instance of selected pattern not found.");
			alert.showAndWait();
		}
	}
	
	public ArrayList<RectangleNote> patternSearch(HashSet<RectangleNote> rnHS) {
		ArrayList<RectangleNote> rnAL = new ArrayList<>(rnHS);
		Collections.sort(rnAL);
		return patternSearch(rnAL);
	}
	
	/**
	 * Searches for identical patterns to the input parameter of Notes
	 * @param rnAL a notes whose pattern to be searched for. Assumed that this list is sorted.
	 * @return a list of a matching pattern (if one exists) that occurs subsequent to the input parameter
	 */
	public ArrayList<RectangleNote> patternSearch(ArrayList<RectangleNote> rnAL) {
		if (rnAL == null || rnAL.size() == 0) return null;
		
		//Begin search on the column right after the last note in the given parameter
		//by initializing rnALArr[k] = All the Notes contained in the (k + startColIndex)-th column index.
		int startSearchColIndex = rnAL.get(rnAL.size() - 1).colIdx + 1;
		ArrayList<RectangleNote>[] rnALArr = new ArrayList[this.col - startSearchColIndex];
		for (int i = startSearchColIndex; i < this.col; ++i) {
			rnALArr[i - startSearchColIndex] = getAllRectangleNotesInColumn(i);
		}
		
		//Now make up an array of arraylists of the given parameter, rnAL
		ArrayList<RectangleNote>[] givenParamRNALArr = new ArrayList[startSearchColIndex - rnAL.get(0).colIdx];
		int startColIndexOfGivenAL = rnAL.get(0).colIdx;
		int index = 0;
		for (int i = startColIndexOfGivenAL; i < startSearchColIndex; ++i) {
			//Get all notes in the i-th column into an arraylist
			if (givenParamRNALArr[i - startColIndexOfGivenAL] == null) 
				givenParamRNALArr[i - startColIndexOfGivenAL] = new ArrayList<RectangleNote>();
			while (rnAL.size() > index && rnAL.get(index).colIdx == i) {
				givenParamRNALArr[i - startColIndexOfGivenAL].add(rnAL.get(index));
				index++;
			}
		}
		
		ArrayList<RectangleNote> ret = null;
		for (int i = 0; i < rnALArr.length - givenParamRNALArr.length; ++i) {
			//TODO consider implementing similarity measure (e.g. 90% match), and option for allowing transposed patterns too
			ret = getALIfEqualPatternFound(givenParamRNALArr, rnALArr, i);
			if (ret != null) return ret;
		}
		
		return null;
	}

	private ArrayList<RectangleNote> getALIfEqualPatternFound(ArrayList<RectangleNote>[] sourceArr, ArrayList<RectangleNote>[] destArr, int startIdx) {
		int currIdxForDestArr = startIdx;
		ArrayList<RectangleNote> ret = new ArrayList<>();
		for (int i = 0; i < sourceArr.length; ++i) {
			ArrayList<RectangleNote> tmpAL = checkIfcontainsAll(sourceArr[i], destArr[currIdxForDestArr]); 
			if (tmpAL == null) return null;
			else ret.addAll(tmpAL);
			currIdxForDestArr++;
		}
		return ret;
	}
	
	private ArrayList<RectangleNote> checkIfcontainsAll(ArrayList<RectangleNote> sourceAL, ArrayList<RectangleNote> destAL) {
		int destALIdx = 0;
		ArrayList<RectangleNote> ret = new ArrayList<>();
		for (RectangleNote rn : sourceAL) {
			if (destAL.size() <= destALIdx) return null;
			while (destAL.size() > destALIdx && destAL.get(destALIdx).rowIdx < rn.rowIdx) destALIdx++;
			if (destAL.size() <= destALIdx || destAL.get(destALIdx).rowIdx != rn.rowIdx ||
					destAL.get(destALIdx).length != rn.length) return null;
			ret.add(destAL.get(destALIdx));
			destALIdx++;
		}
		return ret;
	}
	
	private ArrayList<RectangleNote> getAllRectangleNotesInColumn(int colIndex) {
		ArrayList<RectangleNote> retAL = new ArrayList<>();
		for (int i = colIndex; i < this.rectArr.length; i += this.col) {
			if (rectArr[i] != null && rectArr[i].colIdx == colIndex) retAL.add(rectArr[i]);
		}
		
		return retAL;
	}
	

	
//	private Note[] convertToNotes(ArrayList<Integer> pitchAL) {
//		Note[] ret = new Note[pitchAL.size()];
//		for (int i = 0; i < ret.length; ++i) {
//			ret[i] = new Note(pitchAL.get(i), this.pianoRollGUI.getTempo());
//		}
//		return ret;
//	}
	
	/**
	 * Return a list of all notes in a specific row, spanning the start and end columns, inclusive.
	 * @param rowIdx
	 * @param startColIdx
	 * @param endColIdx
	 * @return
	 */
	public ArrayList<WrapperNote> getAllNotesInRow(int rowIdx, int startColIdx, int endColIdx) {
		ArrayList<WrapperNote> ret = new ArrayList<>();
		int i = startColIdx;
		while (i <= endColIdx) {
			int k = this.convertTo1DCoord(rowIdx, i);
			if (this.rectArr[k] != null) {
				WrapperNote n = new WrapperNote(this.computePitchFromRow(rowIdx), i,
												ColorIntMap.colorHashMap.get(this.rectArr[k].color),
												ColorIntMap.colorHashMap.get(this.rectArr[k].origColor));
//				n.setIdx(i);
				System.out.println(n);
				
				int j = i+1;
				int kk = k+1;
				int length = 1;
				while (j < this.col) {
					if (rectArr[kk] != rectArr[k]) {
						break;
					}
					j++;
					kk++;
					length++;
				} //end while
				
				n.setDuration(length);
				ret.add(n);
				
				i = j-1;
			} //end if
			i++;
		} //end for i
		return ret;
	}
	
	public ArrayList<WrapperNote> getAllNotesInRow(int rowIndex) {
		return getAllNotesInRow(rowIndex, 0, this.col - 1);
	}
	
	//Return all notes in given column in ascending order, in int format
	public ArrayList<Integer> getAllPitchesInColumn(int colIndex, boolean excludeMuteNotes) {
		ArrayList<Integer> ret = new ArrayList<>();
		for (int i = colIndex; i < this.rectArr.length; i += this.col) {
			if (this.rectArr[i] != null && !this.rectArr[i].isMute) {
				int pitch = this.computePitch(i);
				ret.add(pitch);
			}
		}
		Collections.reverse(ret);  //This sorts the pitches in ascending order
		return ret;
	}
	
	public ArrayList<WrapperNote> getAllNotesInColumn(int colIndex, boolean excludeMuteNotes) {
		ArrayList<WrapperNote> ret = new ArrayList<>();
		for (int i = colIndex; i < this.rectArr.length; i += this.col) {
			if (this.rectArr[i] != null && !this.rectArr[i].isMute) {
				WrapperNote wn = new WrapperNote(this.computePitch(i), rectArr[i].colIdx, 
												rectArr[i].color, rectArr[i].origColor, 
												rectArr[i].length);
				ret.add(wn);
			}
		}
		return ret;
	}
	
	public HashSet<RectangleNote> getAllSelectedNotes() {
		HashSet<RectangleNote> ret = new HashSet<>();
		for (int i = 0; i < this.rectArr.length; ++i) {
			if (rectArr[i] != null && this.rectArr[i].isSelected) {
				ret.add(rectArr[i]);
			}
		}
//		System.out.println(ret);
		return ret;
	}
	
//	public ArrayList<WrapperNote> getAllSelectedNotesInGreyRect() {
//		ArrayList<WrapperNote> ret = new ArrayList<>();
//		if (this.selectionMouseRect == null) return ret;
//		for (int i = 0; i < this.rectArr.length; ++i) {
//			if (rectArr[i] != null && this.containedInGreyRect(rectArr[i], selectionMouseRect, this.getRow(i))) {
//				WrapperNote n = new WrapperNote(this.computePitchFromRow(this.getRow(i)), 
//												this.getCol(i), 
//												ColorIntMap.colorHashMap.get(this.rectArr[i].color),
//												ColorIntMap.colorHashMap.get(this.rectArr[i].origColor));
//				ret.add(n);
//			}
//		}
//		System.out.println(ret);
//		return ret;
//	}
	
	
	
	public void deleteSelectedNotes() {
		for (int i = 0; i < this.rectArr.length; ++i) {
//			if (rectArr[i] != null && this.containedInGreyRect(rectArr[i], selectedNotesGreyRect, this.getRow(i))) {
			if (rectArr[i] != null && rectArr[i].isSelected) {
				this.deleteRect(i, rectArr, this.pane);
			}
		}
	}
	
	public void colorSelectedNotes(int colorInt) {
		for (int i = 0; i < this.rectArr.length; ++i) {
			if (rectArr[i] != null && rectArr[i].isSelected) {
				rectArr[i].setColor(colorInt);
//				rectArr[i].setSelected(false);
			}
		} //end for i
		this.deSelectAll();
	}
	
	/**
	 * @param rowIdx
	 * @param colStartIdx
	 * @param colEndIdx
	 * @return true IFF 1) a pitch is CONTAINED in the start column AND
	 * 				    2) that pitch is held to the end column
	 */
	public boolean isHeld(int pitch, int colStartIdx, int colEndIdx) {
		//First find out if there is a pitch in the given row, col, for both starting and ending indices.
		if (colStartIdx < 0) return false;
		int rowIdx = this.computeRow(pitch);
		int start = this.convertTo1DCoord(rowIdx, colStartIdx);
		int end = this.convertTo1DCoord(rowIdx, colEndIdx);
		
		if (this.rectArr[start] == null) return false;
		
		//If the note is held, then the SAME RectangleNote object should be contained in both.
		if (this.rectArr[start] == this.rectArr[end]) return true;
		return false;
		
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
