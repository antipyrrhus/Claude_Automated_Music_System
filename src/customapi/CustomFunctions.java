package customapi;
import javafx.application.Application;
import javafx.scene.paint.Color;
import pianoroll.*;

/**
 * This class is an API intended to make certain custom functions available to the end user for querying and modifying notes
 * on the ScorePane, without such users having to tinker with the ScorePane class directly.
 */
public class CustomFunctions {
	private ScorePane scorePane;  //class for displaying / modifying notes.
	private int instrument;
	private static final Color[] intToColorArr = ColorIntMap.intToColorArr;
	
	
	/**
	 * Constructor
	 * @param scorePane
	 * @param instrument
	 */
	public CustomFunctions(ScorePane scorePane, int instrument) {
		this.scorePane = scorePane;
		this.instrument = instrument;
	}
	
	/**
	 * Changes the instrument on the score pane.
	 * @param instrument
	 */
	public void changeInstrument(int instrument) {
		//Instrument change will fail if the instrument integer parameter is out of range
		if (this.scorePane.changeInstrument(instrument) == true) {
			this.instrument = instrument;
		}
	}
	
	/**
	 * Reads and returns a String representation of the current instrument, plus features of the
	 * note located at (col, row). If the note is null, its feature is represented simply as "-".
	 * See NoteFeatures class for more details.
	 * @param col
	 * @param row
	 * @return String representation of note's features
	 */
	public String readNoteFeatures(int col, int row) {
		this.checkValidColRow(col, row);
		return this.instrument + "," + new NoteFeatures(scorePane.getNote(col, row)).toString();
	}
	
	/**
	 * Returns a string to indicate the color value of the note at (col, row), along with a "1" or "0" at the end
	 * to indicate respectively that the note is either a sustained (held) continuation of a note that started earlier,
	 * or not. Examples:
	 * 
	 * "-10" : indicates a null note
	 * "01"  : indicates a sustained note with color value 0
	 * "101" : indicates a sustained note with color value 10
	 * "100" : indicates a non-sustained note with color value 10  
	 * 
	 * @param col
	 * @param row
	 * @return a String value
	 */
	public String readNoteVal(int col, int row) {
		int color = this.readColor(col,row);
		if (color == -1) return "-10";  //color == -1 means null note
		int startCol = this.getStartCol(col, row);
		if (startCol > col) throw new RuntimeException("Something is wrong. A note's start column should never be larger than col.");
		if (startCol == col) return color + "0"; //this is NOT a continuation of previously held note
		return color + "1";  //startCol < col, meaning this is a continuation of previously held note
	}
	
	/**
	 * Returns the color at the selected (col, row)
	 * @param col
	 * @param row
	 * @return color int value (See pianoroll/ColorIntMap.java)
	 */
	public int readColor(int col, int row) {
		this.checkValidColRow(col, row);
		return scorePane.getColor(col,row);
	}
	
	/**
	 * Sets the note at location (col, row) to the color which is mapped to the given int value.
	 * If there is no note at that location, creates a note of length 1 and then colors it.
	 * Note: if c == -1, then deletes the note instead.
	 * @param col
	 * @param row
	 * @param c
	 */
	public boolean setColor(int col, int row, int c) {
		this.checkValidColor(c);
		this.checkValidColRow(col, row);
//		if (scorePane.setColor(col, row, c) == false) {
//			//setting color failed for some reason. This can happen, for example, if
//			//the note at this location is locked against editing
//			System.out.println(ColorEnum.checkColorProperty(readColor(col,row))); //testing
//			System.out.println(ColorEnum.checkColorProperty(c)); //testing
//		}
		return scorePane.setColor(col, row, c);
	}
	
	/**
	 * Returns the column at which the note queried at (col, row) begins.
	 * Used to figure out whether the queried note is held (sustained) for multiple columns
	 * @param col
	 * @param row
	 * @return the start column of the given note at (col, row)
	 */
	public int getStartCol(int col, int row) {
		//This indicates that there is a null note here.
		//Update: not needed, the below method takes care of this case
//		if (readColor(col, row) == -1) return -1; 
		this.checkValidColRow(col, row);
		return scorePane.getStartCol(col, row);
	}
	
	/**
	 * Sets a new duration (sustained or "held" duration" of the note at (col, row))
	 * 
	 * If there is no note at this location, does nothing and returns false.
	 * 
	 * If there is a note, then tries to modify its duration, but will abort and return false
	 * in the event that doing so would affect other notes  (e.g. extending its duration would overwrite another note) 
	 * or would cause the note to extend beyond the total number of columns in the ScorePane.
	 * 
	 * If there is a note and d == 0, then deletes that note (has the same effect as setColor() method with -1
	 * as the color parameter) and returns true.
	 * 
	 * @param d the duration to set
	 * @param col
	 * @param row
	 * @return true IFF there is a note at (col, row) AND its duration was successfully modified.
	 */
	public boolean setDuration(int d, int col, int row) {
		this.checkValidColRow(col, row);
		return scorePane.setDuration(d, col, row);
	}
	
	/**
	 * @return total no. of columns in the scorepane
	 */
	public int getTotalNumOfCols() {
		return this.scorePane.getCol();
	}
	
	/**
	 * @return total no. of rows in the scorepane (should return 88, to simulate piano)
	 */
	public int getTotalNumOfRows() {
		return ScorePane.ROWS;
	}
	
	
	/**
	 * Helper function to ensure (col, row) isn't out of range
	 * @param col
	 * @param row
	 */
	private void checkValidColRow(int col, int row) {
		if (row < 0 || row >= ScorePane.ROWS || col < 0 || col >= scorePane.getCol())
			throw new RuntimeException("Invalid col / row range");
	}
	
	/**
	 * Helper function to ensure given color int value isn't out of range
	 * If c == -1, this indicates a "null" note.
	 * @param c
	 */
	private void checkValidColor(int c) {
		if (c < -1 || c >= intToColorArr.length) throw new RuntimeException("Invalid color integer value.");
	}
	
	public static void main(String[] args) throws Exception {
		Application.launch(PianoRollGUI.class, args);
	}
}
