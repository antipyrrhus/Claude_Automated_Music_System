package customapi;
import java.util.BitSet;
import java.util.HashMap;

import javafx.application.Application;
import javafx.scene.paint.Color;
import pianoroll.*;

/**
 * This class is an API intended to make certain custom functions available to the end user for querying and modifying notes
 * on the ScorePane, without such users having to tinker with the ScorePane class directly.
 */
public class CustomFunctions extends SuperCustomFunctions{
//	private ScorePane scorePane;  //class for displaying / modifying notes.
//	private CustomFunctionsPane cfp; //GUI for the end user
//	private HashMap<Integer, String> commandsStrHM; //Maps an index value to the name of a method in this class
//	private HashMap<String, Runnable> commandsHM;   //Maps the name of a method in this class to the actual executable function
////	private static final Color[] intToColorArr = ColorIntMap.intToColorArr; //Int to color map
//	private static final Color[] intToRGBArr = ColorIntMap.getIntToRGBArr(); //More robust RGB color map
	
	/**
	 * Constructor
	 * @param scorePane
	 * @param instrument
	 */
	public CustomFunctions(ScorePane scorePane, CustomFunctionsPane cfp) {
		super(scorePane, cfp);
		init();
//		this.scorePane = scorePane;
//		this.cfp = cfp;
//		this.commandsHM = new HashMap<>();
//		this.commandsStrHM = new HashMap<>();
	}
		
	private void init() {
		/* Mapping between index and function name (String) */
		int index = 0;
		String str = "";
		
		/*********************************************************************************************************************
		 * -= Begin custom template =-
		 * 
		 * Here, the end user may use the below template for each method you wish to be displayed on the GUI.
		 * Edit the str name with the method name and signature.
		 * Edit the x in this.getAndValidateIntParamArr(x), depending on how many params the method takes.
		 * Edit the this.getNoteFeaturesStr() method depending on x.
		 * 
		 * NOTE: For now, every custom method MUST take in params of type int only. 
		 * (TODO) Might need to later change the above constraint, if needed.
		 * 
		 * NOTE 2: End user should not invoke methods from classes other than those in the current class and 
		 * current package (customapi) when creating custom (non-utility) methods, as it may result in unexpected behavior.
		 *********************************************************************************************************************/
		
		this.registerCustomFunc(
				"rampVolumeGivenMeasureRange(int startMeasureIdx, int endMeasureIdx, int volumeStart, int volumeEnd)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(4);
					this.rampVolumeGivenMeasureRange(params[0], params[1], params[2], params[3]);
				},
				0
		);
		
		
		
//		str = "rampVolumeGivenMeasureRange(int startMeasureIdx, int endMeasureIdx, int volumeStart, int volumeEnd)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(4);
//			this.rampVolumeGivenMeasureRange(params[0], params[1], params[2], params[3]);
//		});
		
		
//		str = "readColor(int col, int row)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(2);
//			System.out.println(this.readColor(params[0], params[1]));
//		});
//		
//		
//		str = "setVolumeGivenMeasureRange(int startMeasureIdx, int endMeasureIdx, int volume)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(3);
//			this.setVolumeGivenMeasureRange(params[0], params[1], params[2]);
//		});
//		
//		
//		str = "changeInstrument(int instrument, int mChannel)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(2);
//			this.changeInstrument(params[0], params[1]);
//		});
//		
//		str = "clearAll()";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
////			int[] params = this.getAndValidateIntParamArr(0); //not needed since this method has no param
//			this.clearAll();
//		});
//		
//		str = "circleOfXth(int startRow, int color, int x)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(3);
//			this.circleOfXth(params[0], params[1], params[2]);
//		});
//		
//		str = "doubleCircleOfXth(int startRowA, int startRowB, int colorA, int colorB, int xA, int xB)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(6);
//			this.doubleCircleOfXth(params[0], params[1], params[2], params[3], params[4], params[5]);
//		});
//		
//		
//		str = "changeVolume(int colorInt, int newVol)";
//		this.commandsStrHM.put(index++, str);
//		this.commandsHM.put(str.substring(0, str.indexOf("(")), () -> {
//			int[] params = this.getAndValidateIntParamArr(2);
//			this.changeVolume(params[0], params[1]);
//		});
	}
	
	/******************************************************************************************************************
	 * -= End of custom template =-
	 ******************************************************************************************************************/
	

	
	
	/******************************************************************************************************************
	 * -= Begin custom methods =-
	 * 
	 * Here, the end user may create custom methods for computational compositions.
	 * Write the custom method, then use the template near the top of this class to display it in the GUI.
	 * 
	 * NOTE: For now, every custom method MUST take in params of type int only, but
	 * the no. of parameters may be arbitrary.
	 * (TODO) Might need to later change the above constraint, if needed.
	 ******************************************************************************************************************/
		
	
//	public int getTotalNumOfColumns() {
//		return super.getTotalNumOfCols();
//	}
	
	/**
	 * Clears the score pane of all notes
	 */
	public void clearAll() {
		for (int row = 0; row < this.getTotalNumOfRows(); ++row) {
			for (int col = 0; col < this.getTotalNumOfCols(); ++col) {
				this.setColor(col, row, -1);
			}
		} //end for row
	}
	
	/**
	 * Draws circle of Xth, starting with the given value at column index 0 and moving upwards
	 * and using mod as needed, where X = the no. of semitones to raise the pitch every time.
	 * Note, -88 <= x <= 88. Otherwise it throws an error.
	 * @param startRow
	 * @param color
	 * @param x
	 */
	public void circleOfXth(int startRow, int color, int x) {
		//The x value shouldn't exceed the total no. of pitches. So let's set it to 88 max.
		if (Math.abs(x) > ScorePane.ROWS) throw new RuntimeException("x is out of range. constraint: -88 <= x <= 88");
		
		//check that the given startRow is valid. We don't have to check col, so we just pass in 0 for that
		if (!this.isValidColRow(0, startRow) || !this.isValidColor(color)) return;
		this.clearAll(); //clear score pane
		
		int row = startRow;
		for (int col = 0; col < this.getTotalNumOfCols(); ++col) {
			this.setColor(col, row, color);
			row -= x;
			row = this.modRow(row);
		}
	}

	/**
	 * Like circleOfXth, except with two notes played simultaneously.
	 * @param startRowA
	 * @param startRowB
	 * @param colorA
	 * @param colorB
	 * @param xA
	 * @param xB
	 */
	public void doubleCircleOfXth(int startRowA, int startRowB, int colorA, int colorB, int xA, int xB) {
		if (Math.abs(xA) > ScorePane.ROWS || Math.abs(xB) > ScorePane.ROWS) 
			throw new RuntimeException("xA or xB is out of range. constraint: -88 <= x <= 88");
		if (!this.isValidColRow(0, startRowA) || !this.isValidColRow(0, startRowB) ||
			!this.isValidColor(colorA) || !this.isValidColor(colorB)) return;
		
		this.clearAll();
		int rowA = startRowA;
		int rowB = startRowB;
		for (int col = 0; col < this.getTotalNumOfCols(); ++col) {
			this.setColor(col, rowA, colorA);
			this.setColor(col, rowB, colorB);
			rowA -= xA;
			rowB -= xB;
			rowA = this.modRow(rowA);
			rowB = this.modRow(rowB);
		}
	}
	
	/**
	 * Ramps volume from the start vol to end vol, from beginning column of start measure index up to (but not including)
	 * the beginning column of the end measure index.
	 * @param startMeasureIdx
	 * @param endMeasureIdx
	 * @param volumeStart
	 * @param volumeEnd
	 */
	public void rampVolumeGivenMeasureRange(int startMeasureIdx, int endMeasureIdx, int volumeStart, int volumeEnd) {
		int startIndex = this.getStartColIdxOfMeasure(startMeasureIdx);
		int endIndex = this.getStartColIdxOfMeasure(endMeasureIdx);
		//invoke helper method
		//Since the endIndex may be out of range (can happen in the event of a nonzero measure offset),
		//We'll take the minimum of either the end index or the total no. of columns and pass that in as parameter
		this.rampVolumeGivenColIndexRange(startIndex, Math.min(endIndex, this.getTotalNumOfCols()),
											volumeStart, volumeEnd);
	}
	
	/**
	 * Ramps volume from the start vol to end vol, from start col idx up to but not including the end col idx.
	 * @param startColIdx
	 * @param endColIdx
	 * @param volStart
	 * @param volEnd
	 */
	public void rampVolumeGivenColIndexRange(int startColIdx, int endColIdx, int volStart, int volEnd) {
		if (this.isValidColRow(startColIdx, 0) && this.isValidColRow(endColIdx - 1, 0) && 
			this.isValidVolume(volStart) && this.isValidVolume(volEnd)) {
			int numCols = endColIdx - startColIdx;
			int rampStep = (volEnd - volStart) / numCols;
			
			int currVol = volStart;
			
			for (int c = startColIdx; c < endColIdx; ++c) {
				for (int r = 0; r < this.getTotalNumOfRows(); ++r) {
					if (this.getStartCol(c, r) == c) { //there is a note here, and it begins at column index c
						this.setVolume(c, r, currVol);
					}
				}//end for r
				currVol += rampStep;
			}//end for c
		} //end if
	}
	
	/**
	 * Sets the volume to the specified value for every note from beginning column of starting measure index 
	 * up to (but not including) the beginning column of end index.
	 * @param startMeasureNum
	 * @param endMeasureNum
	 * @param volume
	 */
	public void setVolumeGivenMeasureRange(int startMeasureIdx, int endMeasureIdx, int volume) {
		int startIndex = this.getStartColIdxOfMeasure(startMeasureIdx);
		int endIndex = this.getStartColIdxOfMeasure(endMeasureIdx);
		//invoke helper method
		//Since the endIndex may be out of range (can happen in the event of a nonzero measure offset),
		//We'll take the minimum of either the end index or the total no. of columns and pass that in as parameter
		this.setVolumeGivenColIndexRange(startIndex, Math.min(endIndex, this.getTotalNumOfCols()), volume);
	}
	
	/**
	 * Sets the volume to specified value for every note from start column (inclusive) to end column index (exclusive).
	 * @param startColIdx
	 * @param endColIdx
	 * @param volume
	 */
	public void setVolumeGivenColIndexRange(int startColIdx, int endColIdx, int volume) {
		//We'll check to see whether the start col index is valid, as well as the volume.
		//We also check to see whether end col index - 1 is valid, since the end index is exclusive. 
		if (this.isValidColRow(startColIdx, 0) && this.isValidColRow(endColIdx - 1, 0) && this.isValidVolume(volume)) {
			for (int i = startColIdx; i < endColIdx; ++i) {
				for (int j = 0; j < this.getTotalNumOfRows(); ++j) {
					if (this.getStartCol(i, j) == i) { //there is a note here, and it begins at column index i
						this.setVolume(i, j, volume);
					}
				} //end for j (row)
			} //end for i (col)
		} //end if
	}
	
	public void changeVolume(int colorInt, int newVol) {
		for (int c = 0; c < this.getTotalNumOfCols(); ++c) {
			for (int r = 0; r < this.getTotalNumOfRows(); ++r) {
				if (this.readColor(c, r) == colorInt) {
					this.setVolume(c, r, newVol);
				}
			}
		} //end for c
		
	}
	
	
	
	
	//TODO
	private BitSet xorBitSets(BitSet b1, BitSet b2,
			boolean xorColIdx, boolean xorRowIdx,
			boolean xorDuration, boolean xorColor,
			boolean xorMidiChannel, boolean xorVolume,
			boolean copyFirstBSIfNoXOR) {
		BitSet b1Copy = (BitSet)b1.clone(); 
		b1Copy.xor(b2);
		return null;
	}
	
	
	
	
	/**********************************************************************************************************************
	 * End custom methods
	 **********************************************************************************************************************/
	
	
	
	
	
	
//	/**********************************************************************************************************************
//	 * -= Begin utility methods =-
//	 * 
//	 * These are utility methods, unlikely to be directly called from the Custom GUI.
//	 * 
//	 * NOTE: Caution should be taken before modifying any of these utility methods.
//	 * - Some utility methods invoke methods from classes outside of this package. It is not recommended
//	 *   that the end user modify classes outside of this customapi package.
//	 * - Some utility methods are called by other classes, such as runCommand() which is invoked by
//	 *   CustomFunctionsPane, in order to execute the custom methods from the GUI interface.
//	 * - However, most utility methods are used by other custom methods in this class.
//	 ***********************************************************************************************************************/
//	
//	/**
//	 * Returns the index of the column that constitutes the start of the given measure index.
//	 * (Measure index starts at 0, as per usual). Takes account of measure offset value, if any.
//	 * @param measureIdx
//	 * @return
//	 */
//	private int getStartColIdxOfMeasure(int measureIdx) {
//		int colsPerMeasure = this.getNumColsPerMeasure();
//		int measureOffset = this.getMeasureOffset();
//		
//		int startIndex = (measureOffset == 0 ? measureIdx * colsPerMeasure : 
//							measureIdx == 0 ? 0 : (measureIdx - 1) * colsPerMeasure + measureOffset);
//		return startIndex;
//	}
//	
//	/**
//	 * Helper method to ensure that a pitch doesn't go out of bounds. 
//	 * Performs mod operation and returns the new row index, if needed.
//	 * @param row
//	 * @return
//	 */
//	private int modRow(int row) {
//		if (row < 0) row = this.getTotalNumOfRows() + row;
//		else if (row >= this.getTotalNumOfRows()) row = row - this.getTotalNumOfRows();
//		return row;
//	}
//	
//	/**
//	 * @return total no. of columns in the scorepane
//	 */
//	private int getTotalNumOfCols() {
//		return this.scorePane.getCol();
//	}
//	
//	/**
//	 * @return total no. of rows in the scorepane (should return 88, to simulate piano)
//	 */
//	private int getTotalNumOfRows() {
//		return ScorePane.ROWS;
//	}
//	
//	/**
//	 * Helper function to ensure (col, row) isn't out of range
//	 * @param col
//	 * @param row
//	 */
//	private boolean isValidColRow(int col, int row) {
//		if (row < 0 || row >= ScorePane.ROWS || col < 0 || col >= scorePane.getCol())
//			return false;
//		return true;
//	}
//	
//	
//	/**
//	 * Helper function to ensure given color int value isn't out of range
//	 * If c == -1, this indicates a "null" note.
//	 * @param c
//	 */
//	private boolean isValidColor(int c) {
//		if (c < -1 || c >= intToRGBArr.length) return false;
//		return true;
//	}
//	
//	/**
//	 * Checks whether the given instrument int value is out of range
//	 * @param instrument
//	 * @return
//	 */
//	private boolean isValidInstrument(int instrument) {
//		return scorePane.isValidInstrument(instrument);
//	}
//
//	/**
//	 * Checks whether the channel index is out of range.
//	 * @param channel
//	 * @return
//	 */
//	private boolean isValidMidiChannel(int channel) {
//		if (channel < 0 || channel >= scorePane.getMidiChannelLength()) return false;
//		return true;
//	}
//	
//	private boolean isValidVolume(int vol) {
//		if (vol < 0 || vol > scorePane.getMaxVol()) return false;
//		return true;
//	}
//	
//	/**
//	 * Returns the color at the selected (col, row)
//	 * @param col
//	 * @param row
//	 * @return color int value (See pianoroll/ColorIntMap.java)
//	 */
//	private int readColor(int col, int row) {
//		if (!this.isValidColRow(col, row)) return -1;
//		return scorePane.getColor(col,row);
//	}
//	
//	/**
//	 * Sets the note at location (col, row) to the color which is mapped to the given int value.
//	 * If there is no note at that location, creates a note of length 1 and then colors it.
//	 * Note: if c == -1, then deletes the note instead.
//	 * @param col
//	 * @param row
//	 * @param c
//	 */
//	private boolean setColor(int col, int row, int c) {
//		if (!this.isValidColor(c) || !this.isValidColRow(col, row)) return false;
//		if (c == -1) return scorePane.setColor(col, row, c);
//		else return scorePane.setColor(col,  row, intToRGBArr[c]);
//	}
//	
//	/**
//	 * Looks up the CustomFunctionsPane class, and from it reads the list of parameters that are intended
//	 * for one of the functions in this class (assumed to be an array of ints).
//	 * Then, makes sure that there are a sufficient number of parameters; if not throws an exception.
//	 * @param num
//	 * @return list of parameters
//	 */
//	private int[] getAndValidateIntParamArr(int num) {
//		int[] params = this.cfp.getParams();
//		if (params.length < num) throw new RuntimeException("Invalid parameters: no. of parameters must be >= " + num);
//		return params;
//	}
//	
//	/**
//	 * Sets the note located at (col, row), if any, to the specified midi channel.
//	 * Each midi channel (out of a total of 16 channels) can be assigned a distinct instrument.
//	 * @param col
//	 * @param row
//	 * @param channel
//	 */
//	private void setNoteToChannel(int col, int row, int channel) {
//		if (this.isValidColRow(col, row) && this.isValidMidiChannel(channel)) {
//			scorePane.setNoteToChannel(col, row, channel);
//		}
//	}
//	
//	/**
//	 * Returns the total number of columns per a single measure.
//	 * @return
//	 */
//	private int getNumColsPerMeasure() {
//		return scorePane.getColsPerMeasure();
//	}
//	
//	/**
//	 * Returns the offset (no. of columns) at the beginning before the first measure bar.
//	 * @return
//	 */
//	private int getMeasureOffset() {
//		return scorePane.getMeasureOffset();
//	}
//	
//	/**
//	 * Sets a new duration (sustained or "held" duration" of the note at (col, row))
//	 * 
//	 * If there is no note at this location, does nothing and returns false.
//	 * 
//	 * If there is a note, then tries to modify its duration, but will abort and return false
//	 * in the event that doing so would affect other notes  (e.g. extending its duration would overwrite another note) 
//	 * or would cause the note to extend beyond the total number of columns in the ScorePane.
//	 * 
//	 * If there is a note and d == 0, then deletes that note (has the same effect as setColor() method with -1
//	 * as the color parameter) and returns true.
//	 * 
//	 * @param d the duration to set
//	 * @param col
//	 * @param row
//	 * @return true IFF there is a note at (col, row) AND its duration was successfully modified.
//	 */
//	private boolean setDuration(int col, int row, int d) {
//		if (!this.isValidColRow(col, row)) return false;
//		return scorePane.setDuration(d, col, row);
//	}
//	
//	/**
//	 * Changes the specified channel's instrument.
//	 * @param instrument
//	 * @param mChannel midi channel whose instrument to change
//	 */
//	private void changeInstrument(int instrument, int mChannel) {
//		//Instrument change will fail if the instrument integer parameter is out of range
//		if (!isValidInstrument(instrument) || !this.isValidMidiChannel(mChannel)) return;
//		this.scorePane.changeInstrument(instrument, mChannel);
//	}
//	
//	/**
//	 * Returns the column at which the note queried at (col, row) begins.
//	 * Used to figure out whether the queried note is held (sustained) for multiple columns
//	 * If there is no note at this location, returns -1
//	 * @param col
//	 * @param row
//	 * @return the start column of the given note at (col, row)
//	 */
//	private int getStartCol(int col, int row) {
//		//This indicates that there is a null note here.
//		if (!this.isValidColRow(col, row)) return -1;
//		return scorePane.getStartCol(col, row);
//	}
//	
//	/**
//	 * Reads and returns a String representation of the current instrument, plus features of the
//	 * note located at (col, row). If the note is null, its feature is represented simply as "-".
//	 * See NoteFeatures class for more details.
//	 * @param col
//	 * @param row
//	 * @return String representation of note's features
//	 */
//	private String getNoteFeaturesStr(int col, int row) {
//		return new NoteFeatures(scorePane.getNote(col, row), this.getTotalNumOfCols()).getBitSetBinaryString();
//	}
//	
//	/**
//	 * Reads and returns a NoteFeatures object containing features of the note at (col, row).
//	 * If the note is null, then the resulting NoteFeatures object's noteIsNull() method will return true.
//	 * @param col
//	 * @param row
//	 * @return
//	 */
//	private BitSet getNoteFeatures(int col, int row) {
////		if (!this.isValidColRow(col, row)) return null;
//		//The below initialization step checks for null note at (col, row), so above line of code unnecessary.
//		return new NoteFeatures(scorePane.getNote(col,  row), this.getTotalNumOfCols()).getBitSet();
//	}
//	
//	/**
//	 * Given a bitset, notates it on the scorepane (assuming it's a valid note).
//	 * If it's a valid note and there is another note at the position it would occupy, then
//	 * overwrites it depending on the boolean param.
//	 * @param bs
//	 * @param overwrite
//	 */
//	private void notateFromBitset(BitSet bs, boolean overwrite) {
//		NoteFeatures nf = new NoteFeatures(bs, this.getTotalNumOfCols());
//		this.notateFromNoteFeatures(nf, overwrite);
//	}
//	
//	/**
//	 * Given a notefeatures object, notates it on the scorepane (assuming it's a valid note).
//	 * If it's a valid note and there is another note at the position it would occupy, then
//	 * overwrites it depending on the boolean param.
//	 * @param nf
//	 * @param overwrite
//	 */
//	private void notateFromNoteFeatures(NoteFeatures nf, boolean overwrite) {
//		if (nf.noteIsNull()) return;
//		int col = nf.getStartIdx();
//		int row = nf.getRowIdx();
//		
//		//If this (col,row) already contains another note, and overwrite == true, then delete that note first
//		if (this.readColor(col, row) >= 0) {
//			if (overwrite) this.setColor(col, row, -1);
//			else return;
//		}
//		
//		this.setColor(col, row, this.convertColorToInt(nf.getColor()));
//		this.setDuration(col, row, nf.getDuration());
//		this.setNoteToChannel(col, row, nf.getMidiChannel());
//		this.setVolume(col, row, nf.getVolume());
//	}
//	
//	/**
//	 * Performs an xor between 2 bitsets.
//	 * @param b1
//	 * @param b2
//	 * @return
//	 */
//	private BitSet xorBitSets(BitSet b1, BitSet b2) {
//		BitSet b1Copy = (BitSet)b1.clone(); 
//		b1Copy.xor(b2);
//		return b1Copy;
//	}
//
//	
//	private int convertColorToInt(Color color) {
//		Integer colorInt = ColorIntMap.getRGBHashMap().get(color);
//		if (colorInt == null) return -1;
//		return colorInt;
//		
//	}
//	
//	/**
//	 * Sets the note located at (col, row), if any, to the specified volume.
//	 * @param col
//	 * @param row
//	 * @param volume
//	 */
//	private void setVolume(int col, int row, int volume) {
//		if (!this.isValidColRow(col, row) || !this.isValidVolume(volume)) return;
//		scorePane.setNoteVolume(col,row,volume);
//	}
//	
//	/**
//	 * Invoked by CustomFunctionsPane class. Once a user selects a function and inputs the correct list of parameters,
//	 * the corresponding function in this class is executed.
//	 * @param s
//	 */
//	public void runCommand(String s) {
//		if (this.commandsHM.get(s) == null) throw new RuntimeException("Method name is invalid.");
//		commandsHM.get(s).run();
//	}
//	
//	/**
//	 * Invoked by CustomFunctionsPane class, in order to make a list of available functions in this class
//	 * and display it to the end user.
//	 * Looks up the String that's associated with the given integer value,
//	 * where the String indicates the name of a function in this class.
//	 * @param i
//	 * @return name of function mapped to the int
//	 */
//	public String getCommandsStr(int i) {
//		return this.commandsStrHM.get(i);
//	}
//	
//	/**********************************************************************************************************************
//	 * End utility methods
//	 **********************************************************************************************************************/
//	
	
	
	
	
	/**
	 * Running this launches the GUI. This should not be changed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Application.launch(PianoRollGUI.class, args);
	}
}
