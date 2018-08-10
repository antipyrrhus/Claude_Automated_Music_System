package customapi;
import java.util.Random;
import javafx.application.Application;
import pianoroll.*;

/**
 * This class is an API intended to make certain custom functions available to the end user for querying and modifying notes
 * on the ScorePane, without such users having to tinker with the ScorePane class or other classes directly.
 */
public class CustomFunctions extends SuperCustomFunctions{
	
	/**
	 * Constructor
	 * @param scorePane
	 * @param instrument
	 */
	public CustomFunctions(ScorePane scorePane, CustomFunctionsPane cfp) {
		super(scorePane, cfp);
		init();
	}
		
	private void init() {
		/* Mapping between index and function name (String) */
		int index = 0;

		/*********************************************************************************************************************
		 * -= Begin custom template =-
		 * 
		 * Here, the end user may use the below template for each method you wish to be displayed on the GUI.
		 * Edit the str name with the method name and signature.
		 * Edit the x in this.getAndValidateIntParamArr(x), depending on how many params the method takes.
		 * Edit the actual method with the correct number of parameters.
		 * Leave the last parameter (the number '0') as is for now. This indicates the type of parameters,
		 * and is currently not relevant as all params must be int type.
		 * 
		 * NOTE: For now, every custom method MUST take in params of type int only. 
		 * Might need to later change the above constraint, if needed.
		 * 
		 * NOTE 2: End user should not invoke methods from classes other than those in the current class and 
		 * its superclass, to help prevent unexpected behavior.
		 *********************************************************************************************************************/
		//Calls inherited method from SuperCustomFunctions
		
		this.registerCustomFunc(
				"alternateVolume(int measures, int volumeStart, int volumeEnd)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(3);
					this.alternateVolume(params[0], params[1], params[2]);
				},
				0
		);
		
		this.registerCustomFunc(
				"changeColorAll(int colorOffset)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(1);
					this.changeColorAll(params[0]);
				},
				0
		);
		
		this.registerCustomFunc(
				"clearAll()",
				index++,
				() -> {
//					int[] params = this.getAndValidateIntParamArr(1);
					this.clearAll();
				},
				0
		);
		
		this.registerCustomFunc(
				"circleOfXth(int startRow, int color, int x)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(3);
					this.circleOfXth(params[0], params[1], params[2]);
				},
				0
		);
		
		
		this.registerCustomFunc(
				"doubleCircleOfXth(int startRowA, int startRowB, int colorA, int colorB, int xA, int xB)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(6);
					this.doubleCircleOfXth(params[0], params[1], params[2], params[3], params[4], params[5]);
				},
				0
		);
		
		this.registerCustomFunc(
				"rampVolumeGivenMeasureRange(int startMeasureIdx, int endMeasureIdx, int volumeStart, int volumeEnd)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(4);
					this.rampVolumeGivenMeasureRange(params[0], params[1], params[2], params[3]);
				},
				0
		);
		
		this.registerCustomFunc(
				"randomDanceSample(int tonicKey, int major)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(2);
					this.randomDanceSample(params[0], params[1]);
				},
				0
		);
		
		this.registerCustomFunc(
				"setTempo(int tempo)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(1);
					this.setTempo(params[0]);
				},
				0
		);
		
		this.registerCustomFunc(
				"setVolume(int col, int row, int volume)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(3);
					this.setVolume(params[0], params[1], params[2]);
				},
				0 
		);
		
		this.registerCustomFunc(
				"startPlayBack(int colIndex)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(1);
					this.startPlayBack(params[0]);
				},
				0
		);
		
		this.registerCustomFunc(
				"stopPlayBack()",
				index++,
				() -> {
//					int[] params = this.getAndValidateIntParamArr(0);
					this.stopPlayBack();
				},
				0
		);
		
		this.registerCustomFunc(
				"setInstrumentAll(int instrument, int channel)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(2);
					this.setInstrumentAll(params[0], params[1]);
				},
				0
		);
		
		this.registerCustomFunc(
				"setInstrumentSingleChannel(int instrument, int channel)",
				index++,
				() -> {
					int[] params = this.getAndValidateIntParamArr(2);
					this.setInstrumentSingleChannel(params[0], params[1]);
				},
				0
		);
		
		
	} //end private void init()
	
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
	 ******************************************************************************************************************/

	/**
	 * Sets all notes in score pane to the given instrument and midi channel. 
	 * @param instrument
	 * @param channel
	 */
	public void setInstrumentAll(int instrument, int channel) {
		super.stopPlayBack();
		super.changeInstrument(instrument, channel);
		if (super.isValidInstrument(instrument)) {
			for (int c = 0; c < super.getTotalNumOfCols(); ++c) {
				for (int r = 0; r < super.getTotalNumOfRows(); ++r) {
					if (super.readColor(c, r) != -1 && super.getStartCol(c, r) == c) {
						super.setNoteToChannel(c, r, channel);
					}
				} //end for r
			} //end for c
		} //end if
		super.startPlayBack(super.getCurrentActiveColumn());
	}
	
	/**
	 * Sets all notes that currently belong to the specified channel to the specified instrument
	 * @param instrument
	 * @param channel
	 */
	public void setInstrumentSingleChannel(int instrument, int channel) {
		super.stopPlayBack();
		if (super.isValidInstrument(instrument) && super.isValidMidiChannel(channel)) {
			super.changeInstrument(instrument, channel);
//			for (int c = 0; c < super.getTotalNumOfCols(); ++c) {
//				for (int r = 0; r < super.getTotalNumOfRows(); ++r) {
//					if (super.readColor(c, r) != -1 && super.getStartCol(c, r) == c && super.getNoteChannel(c, r) == channel) {
//						super.setNoteToChannel(c, r, channel);
//					}
//				} //end for r
//			} //end for c
		}//end if
		super.startPlayBack(super.getCurrentActiveColumn());
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
	
	public void changeVolumeGivenColor(int colorInt, int newVol) {
		for (int c = 0; c < this.getTotalNumOfCols(); ++c) {
			for (int r = 0; r < this.getTotalNumOfRows(); ++r) {
				if (this.readColor(c, r) == colorInt) {
					this.setVolume(c, r, newVol);
				}
			}
		} //end for c	
	}
	
	public void alternateVolume(int measures, int volumeStart, int volumeEnd) {
		if (measures <= 0) return;
		boolean everyOtherIter = true;
		for (int i = 0; i < this.getTotalNumOfCols(); i += measures) {
			if (everyOtherIter) this.rampVolumeGivenMeasureRange(i, i + measures, volumeStart, volumeEnd);
			else this.rampVolumeGivenMeasureRange(i, i + measures, volumeEnd, volumeStart);
			everyOtherIter = !everyOtherIter;
		}
	}
	
	public void changeColorAll(int colorOffset) {
		for (int c = 0; c < this.getTotalNumOfCols(); ++c) {
			for (int r = 0; r < this.getTotalNumOfRows(); ++r) {
				int color = this.readColor(c, r);
				if (color >= 0 && this.getStartCol(c, r) == c) {
					int newColor = color + colorOffset;
					if (newColor < 0) newColor = -newColor;
					if (newColor >= this.getTotalNumOfColors()) newColor %= this.getTotalNumOfColors();
					this.setColor(c, r, newColor);
				}
			}
		} //end for c
	}
	
	/**
	 * 
	 * @param tonicKey 0 = C, 1 = C#, etc.
	 * @param major 1 if major, 0 if not
	 */
	public void randomDanceSample(int tonicKey, int major) {
	
		/* Have a constant drum track with some rhythm, and set a base and melody channel too. 
		 * Base should play random non repeating notes that belong to specified key signature and mode, 
		 * within a single octave. melody should be some soothing instrument, ie techno, and follow some 
		 * set rhythm, and be constrained within one or two octaves, and play some possibly repeating notes pertaining
		 * to that same key signature. Or alternatively, have the melody play some ascending sequence of nonrepeating
		 * notes, belonging to that key signature, with some possiblity of skipping some notes in that sequence. 
		 * Perhaps, e.g. in C major, have the melody play c d e f g a b c, followed by e g a c d f g b
		 * followed by a b c e g b d e followed by f a b c d f g a , folllowed by c... and so on */
		this.clearAll();
		
		//Two types of drum beats
		int lowDrumRow = 0;
		int highDrumRow = 10;
		
		//Set channels and some arbitrary colors for each instrument
		int drumChannel = 0;
		int drumColor = 0;
		int bassChannel = 1;
		int bassColor = 0;
		int bassTonicPitch = PianoRollGUI.MIN_PITCH + 4 + tonicKey % 12 + (12*1); //1 octave from bottom
		int altoChannel = 2;
		int altoColor = 0;
		int altoTonicPitch = PianoRollGUI.MIN_PITCH + 4 + tonicKey % 12 + (12*4); //4 octaves from bottom
		int padChannel = 3;
		int padColor = 0;
		int padTonicPitch = PianoRollGUI.MIN_PITCH + 4 + tonicKey % 12 + (12*3); //3 octaves from bottom
		
		this.changeInstrument(234, drumChannel);
		this.changeInstrument(33, bassChannel);
		this.changeInstrument(88, altoChannel);
		this.changeInstrument(89, padChannel);
		
		int[] majorIntv = {0, 2, 4, 5, 7, 9, 11, 12, 14, 16};
		int[] minorIntv = {0, 2, 3, 5, 7, 8, 10, 12, 14, 15};  //natural minor
		int[] intv;
		if (major == 1) {
			intv = majorIntv;
		} else {
			intv = minorIntv;
		}
		
		Random r = new Random();
		int pickupOffset = this.getMeasureOffset();
		int totalCols = this.getTotalNumOfCols();
		int colsPerMeasure = this.getNumColsPerMeasure();
		
		int prevBassRow = -1;
		int prevAltoRow = -1;
		int crashIter = 0;
		int crashFreq = 4;
		
		//Start at the first downbeat and notate each instrument
		for (int i = pickupOffset; i < totalCols; ++i) {
			if (i % colsPerMeasure == pickupOffset) { //this means we're at the first downbeat of the measure
				if (crashIter % crashFreq == 0) {
					//Fixed drum channel
					this.setColor(i, 51, 1567);
					this.setNoteToChannel(i, 51, 9);
				}
				crashIter++;
				
				//low drum beat
				this.setColor(i, lowDrumRow, drumColor);
				this.setNoteToChannel(i, lowDrumRow, drumChannel);
				
				//tonic bass note
				this.setColor(i, this.computeRowFromPitch(bassTonicPitch), bassColor);
				this.setNoteToChannel(i, this.computeRowFromPitch(bassTonicPitch), bassChannel);
				
				//pad chord
				int randomChordLength = r.nextInt(2) + 3;
				int[] padSequence = new Random().ints(0, intv.length).distinct().limit(randomChordLength).toArray();
				for (int p : padSequence) {
					int padRow = this.computeRowFromPitch(padTonicPitch + intv[p]);
					this.setColor(i, padRow, padColor);
					this.setNoteToChannel(i, padRow, padChannel);
					this.setDuration(i, padRow, colsPerMeasure);
				}
					
			} else { //not at the first downbeat of the measure
				
				int drumRow, bassRow, altoRow;
				if (r.nextDouble() < 2.0/3) {
					//low drum beat with probability 2/3
					drumRow = lowDrumRow;
					altoRow = -1; //skip alto this time
				} else {
					 drumRow = highDrumRow;
					 do {
						 int randAltoIndex = r.nextInt(intv.length);
						 altoRow = this.computeRowFromPitch(altoTonicPitch + intv[randAltoIndex]);
					 } while (altoRow == prevAltoRow);
				}
				this.setColor(i, drumRow, drumColor);
				this.setNoteToChannel(i, drumRow, drumChannel);
				
				//up to an octave + 3rd major up from the tonic note for bassRow
				//Make sure the pitch selected belongs to the scale, and is non-repeating
				do {
					int bassRandomIdx = r.nextInt(intv.length);
					bassRow = this.computeRowFromPitch(bassTonicPitch + intv[bassRandomIdx]);
				} while (bassRow == prevBassRow);
				
				if (bassRow >= 0) prevBassRow = bassRow;
				System.out.println(bassRow + " ");
				this.setColor(i, bassRow, bassColor);
				this.setNoteToChannel(i, bassRow, bassChannel);
				
				
				if (this.readColor(i, altoRow) == -1) {
					this.setColor(i, altoRow, altoColor);
					this.setNoteToChannel(i, altoRow, altoChannel);
					if (altoRow >= 0) prevAltoRow = altoRow;
				}				
			}
		}
	}
	
	/**********************************************************************************************************************
	 * End custom methods
	 **********************************************************************************************************************/
	
	/**
	 * Running this launches the GUI. This should not be changed.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Application.launch(PianoRollGUI.class, args);
	}
}
