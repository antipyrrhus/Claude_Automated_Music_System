package customapi;
import java.util.BitSet;
import java.util.HashMap;
import javafx.scene.paint.Color;
import pianoroll.ColorIntMap;
import pianoroll.CustomFunctionsPane;
import pianoroll.ScorePane;

/**
 * An abstract Superclass for CustomFunctions class. This is only here
 * in order to enable dynamic re-loading of CustomFunctions class (so that the end user can edit the 
 * CustomFunctions class and can dynamically re-load the updated GUI without having to re-start the entire program)
 * and to be able to cast it as its SuperClass.
 * 
 * This Superclass is necessary because apparently with dynamic reloading and instantiation thereof,
 * you must cast it either to a superclass or to an interface. In other words, the CustomFunctions class
 * cannot be cast to itself.
 */
public abstract class SuperCustomFunctions {
	
	private final ScorePane scorePane;  //class for displaying / modifying notes.
	private final CustomFunctionsPane cfp; //GUI for the end user
	protected HashMap<Integer, String> commandsStrHM; //Maps an index value to the name of a method in this class
	protected HashMap<String, Runnable> commandsHM;   //Maps the name of a method in this class to the actual executable function
//	protected static final Color[] intToColorArr = ColorIntMap.intToColorArr; //Int to color map
	private static final Color[] intToRGBArr = ColorIntMap.getIntToRGBArr(); //More robust RGB color map
	private static final HashMap<Color, Integer> rgbHashMap = ColorIntMap.getRGBHashMap();
	
	/**
	 * Constructor
	 * @param scorePane
	 * @param instrument
	 */
	public SuperCustomFunctions(ScorePane scorePane, CustomFunctionsPane cfp) {
		this.scorePane = scorePane;
		this.cfp = cfp;
		this.commandsHM = new HashMap<>();
		this.commandsStrHM = new HashMap<>();
	}
	
	
	/**
	 * Invoked by CustomFunctionsPane class. Once a user selects a function and inputs the correct list of parameters,
	 * the corresponding function in this class is executed.
	 * @param s
	 */
	public final void runCommand(String s) {
		if (this.commandsHM.get(s) == null) throw new RuntimeException("Method name is invalid.");
		commandsHM.get(s).run();
	}
	
	/**
	 * Invoked by CustomFunctionsPane class, in order to make a list of available functions in this class
	 * and display it to the end user.
	 * Looks up the String that's associated with the given integer value,
	 * where the String indicates the name of a function in this class.
	 * @param i
	 * @return name of function mapped to the int
	 */
	public final String getCommandsStr(int i) {
		return this.commandsStrHM.get(i);
	}
	
	/**
	 * Invoked by CustomFunctions subclass. Enables a custom function written by end user to be visible
	 * and to be invoked using the Piano Roll GUI.
	 * @param str Name of custom method signature
	 * @param index the method index (for mapping purposes)
	 * @param r Runnable, representing the method itself
	 * @param type TODO currently not used. Placeholder for possible changes in the parameter types in the future.
	 *             For right now, every parameter is an int type.
	 */
	protected void registerCustomFunc(String str, int index, Runnable r, int type) {
		this.commandsStrHM.put(index, str);
		this.commandsHM.put(str.substring(0, str.indexOf("(")), r);
	}

	protected static Color[] getIntToRGBArr() {
		return intToRGBArr;
	}

	/**********************************************************************************************************************
	 * -= Begin utility methods =-
	 * 
	 * These are utility methods, unlikely to be directly called from the Custom GUI.
	 * 
	 * NOTE: Caution should be taken before modifying any of these utility methods.
	 * - Some utility methods invoke methods from classes outside of this package. It is not recommended
	 *   that the end user modify classes outside of this customapi package.
	 * - Some utility methods are called by other classes, such as runCommand() which is invoked by
	 *   CustomFunctionsPane, in order to execute the custom methods from the GUI interface.
	 * - However, most utility methods are used by other custom methods in this class.
	 ***********************************************************************************************************************/
	
	/**
	 * Returns the index of the column that constitutes the start of the given measure index.
	 * (Measure index starts at 0, as per usual). Takes account of measure offset value, if any.
	 * @param measureIdx
	 * @return
	 */
	protected final int getStartColIdxOfMeasure(int measureIdx) {
		int colsPerMeasure = this.getNumColsPerMeasure();
		int measureOffset = this.getMeasureOffset();
		
		int startIndex = (measureOffset == 0 ? measureIdx * colsPerMeasure : 
							measureIdx == 0 ? 0 : (measureIdx - 1) * colsPerMeasure + measureOffset);
		return startIndex;
	}
	
	/**
	 * Helper method to ensure that a pitch doesn't go out of bounds. 
	 * Performs mod operation and returns the new row index, if needed.
	 * @param row
	 * @return
	 */
	protected final int modRow(int row) {
		if (row < 0) row = this.getTotalNumOfRows() + row;
		else if (row >= this.getTotalNumOfRows()) row = row - this.getTotalNumOfRows();
		return row;
	}
	
	/**
	 * Clears the score pane of all notes
	 */
	protected void clearAll() {
		for (int row = 0; row < this.getTotalNumOfRows(); ++row) {
			for (int col = 0; col < this.getTotalNumOfCols(); ++col) {
				this.setColor(col, row, -1);
			}
		} //end for row
	}
	
	/**
	 * @return total no. of columns in the scorepane
	 */
	protected final int getTotalNumOfCols() {
		return this.scorePane.getCol();
	}
	
	/**
	 * @return total no. of rows in the scorepane (should return 88, to simulate piano)
	 */
	protected final int getTotalNumOfRows() {
		return ScorePane.ROWS;
	}
	
	/**
	 * Helper function to ensure (col, row) isn't out of range
	 * @param col
	 * @param row
	 */
	protected final boolean isValidColRow(int col, int row) {
		if (row < 0 || row >= ScorePane.ROWS || col < 0 || col >= scorePane.getCol())
			return false;
		return true;
	}
	
	
	/**
	 * Helper function to ensure given color int value isn't out of range
	 * If c == -1, this indicates a "null" note.
	 * @param c
	 */
	protected final boolean isValidColor(int c) {
		if (c < -1 || c >= intToRGBArr.length) return false;
		return true;
	}
	
	/**
	 * Checks whether the given instrument int value is out of range
	 * @param instrument
	 * @return
	 */
	protected final boolean isValidInstrument(int instrument) {
		return scorePane.isValidInstrument(instrument);
	}

	/**
	 * Checks whether the channel index is out of range.
	 * @param channel
	 * @return
	 */
	protected final boolean isValidMidiChannel(int channel) {
		if (channel < 0 || channel >= scorePane.getMidiChannelLength()) return false;
		return true;
	}
	
	protected final boolean isValidVolume(int vol) {
		if (vol < 0 || vol > scorePane.getMaxVol()) return false;
		return true;
	}
	
	/**
	 * Returns the color at the selected (col, row)
	 * @param col
	 * @param row
	 * @return color int value (See pianoroll/ColorIntMap.java)
	 */
	protected final int readColor(int col, int row) {
		if (!this.isValidColRow(col, row)) return -1;
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
	protected final boolean setColor(int col, int row, int c) {
		if (!this.isValidColor(c) || !this.isValidColRow(col, row)) return false;
		if (c == -1) return scorePane.setColor(col, row, c);
		else return scorePane.setColor(col,  row, intToRGBArr[c]);
	}
	
	/**
	 * Looks up the CustomFunctionsPane class, and from it reads the list of parameters that are intended
	 * for one of the functions in this class (assumed to be an array of ints).
	 * Then, makes sure that there are a sufficient number of parameters; if not throws an exception.
	 * @param num
	 * @return list of parameters
	 */
	protected final int[] getAndValidateIntParamArr(int num) {
		int[] params = this.cfp.getParams();
		if (params.length < num) throw new RuntimeException("Invalid parameters: no. of parameters must be >= " + num);
		return params;
	}
	
	/**
	 * Sets the note located at (col, row), if any, to the specified midi channel.
	 * Each midi channel (out of a total of 16 channels) can be assigned a distinct instrument.
	 * @param col
	 * @param row
	 * @param channel
	 */
	protected final void setNoteToChannel(int col, int row, int channel) {
		if (this.isValidColRow(col, row) && this.isValidMidiChannel(channel)) {
			scorePane.setNoteToChannel(col, row, channel);
		}
	}
	
	/**
	 * Returns the total number of columns per a single measure.
	 * @return
	 */
	protected final int getNumColsPerMeasure() {
		return scorePane.getColsPerMeasure();
	}
	
	/**
	 * Returns the offset (no. of columns) at the beginning before the first measure bar.
	 * @return
	 */
	protected final int getMeasureOffset() {
		return scorePane.getMeasureOffset();
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
	protected final boolean setDuration(int col, int row, int d) {
		if (!this.isValidColRow(col, row)) return false;
		if (d > this.getTotalNumOfCols() - col) return false;
		return scorePane.setDuration(d, col, row);
	}
	
	/**
	 * Changes the specified channel's instrument.
	 * @param instrument
	 * @param mChannel midi channel whose instrument to change
	 */
	protected final void changeInstrument(int instrument, int mChannel) {
		//Instrument change will fail if the instrument integer parameter is out of range
		if (!isValidInstrument(instrument) || !this.isValidMidiChannel(mChannel)) return;
		this.scorePane.changeInstrument(instrument, mChannel);
	}
	
	/**
	 * Returns the column at which the note queried at (col, row) begins.
	 * Used to figure out whether the queried note is held (sustained) for multiple columns
	 * If there is no note at this location, returns -1
	 * @param col
	 * @param row
	 * @return the start column of the given note at (col, row)
	 */
	protected final int getStartCol(int col, int row) {
		//This indicates that there is a null note here.
		if (!this.isValidColRow(col, row)) return -1;
		return scorePane.getStartCol(col, row);
	}
	
	/**
	 * Reads and returns a NoteFeatures object containing features of the note at (col, row).
	 * If the note is null, then the resulting NoteFeatures object's noteIsNull() method will return true.
	 * @param col
	 * @param row
	 * @return
	 */
	protected final NoteFeatures getNoteFeatures(int col, int row) {
//		if (!this.isValidColRow(col, row)) return null;
		//The below initialization step checks for null note at (col, row), so above line of code unnecessary.
		return new NoteFeatures(scorePane.getNote(col,  row));
	}
	
	/**
	 * Given a notefeatures object, notates it on the scorepane (assuming it's a valid note).
	 * If it's a valid note and there is another note at the position it would occupy, then
	 * overwrites it depending on the boolean param.
	 * @param nf
	 * @param overwrite
	 */
	protected final void notateFromNoteFeatures(NoteFeatures nf, boolean overwrite) {
		if (nf.getNoteIsNull()) return;
		int col = nf.getColStartIdx();
		int row = nf.getRowIdx();
		
		//If this (col,row) already contains another note, and overwrite == true, then delete that note first
		if (this.readColor(col, row) >= 0) {
			if (overwrite) this.setColor(col, row, -1);
			else return;
		}
		
		this.setColor(col, row, this.convertColorToInt(nf.getColor()));
		this.setDuration(col, row, nf.getDuration());
		this.setNoteToChannel(col, row, nf.getMidiChannel());
		this.setVolume(col, row, nf.getVolume());
	}
	
	/**
	 * Performs an xor between 2 bitsets. Can be used to play around with stencil bits attribute in NoteFeatures
	 * @param b1
	 * @param b2
	 * @return
	 */
	protected final BitSet xorBitSets(BitSet b1, BitSet b2) {
		BitSet b1Copy = (BitSet)b1.clone(); 
		b1Copy.xor(b2);
		return b1Copy;
	}

	/**
	 * 
	 * @return Tempo value
	 */
	protected final int getTempo() {
		return this.scorePane.getTempo();
	}
	
	/**
	 * Set tempo value
	 * @param tempo
	 */
	protected final void setTempo(int tempo) {
		//No need to check for valid tempo; the method below does it for you
		this.scorePane.setTempo(tempo);
	}
	
	/**
	 * Get the current column that is active
	 * @return
	 */
	protected final int getCurrentActiveColumn() {
		return this.scorePane.getActiveColumn();
	}
	
	/**
	 * Set the active column to specified index
	 * @param colIndex
	 */
	protected final void setActiveColumn(int colIndex) {
		if (this.isValidColRow(colIndex, 0)) this.scorePane.setActiveColumn(colIndex);
	}
	
	/**
	 * 
	 * @param color
	 * @return
	 */
	protected final int convertColorToInt(Color color) {
		Integer colorInt = rgbHashMap.get(color);
		if (colorInt == null) return -1;
		return colorInt;
	}
	
	protected final int getTotalNumOfColors() {
		return intToRGBArr.length;
	}
	
	/**
	 * Sets the note located at (col, row), if any, to the specified volume.
	 * @param col
	 * @param row
	 * @param volume
	 */
	protected final void setVolume(int col, int row, int volume) {
		if (!this.isValidColRow(col, row) || !this.isValidVolume(volume)) return;
		scorePane.setNoteVolume(col,row,volume);
	}
	
	/**
	 * Starts playback from the given column index.
	 * @param colIndex
	 */
	protected final void startPlayBack(int colIndex) {
		this.setActiveColumn(colIndex);
		if (this.getCurrentActiveColumn() == colIndex) scorePane.startPlayBack();
	}
	
	protected final int computeRowFromPitch(int pitch) {
		return scorePane.computeRow(pitch);
	}
	
	/**
	 * Stops playback
	 */
	protected final void stopPlayBack() {
		this.scorePane.stopPlayBack();
	}
	
	/**********************************************************************************************************************
	 * End utility methods
	 **********************************************************************************************************************/
}
