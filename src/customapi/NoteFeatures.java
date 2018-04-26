package customapi;
//import java.util.Arrays;
import java.util.BitSet;

import convertmidi.MidiFile;
import javafx.scene.paint.Color;
import pianoroll.*;

public class NoteFeatures {
	private int colStartIdx, rowIdx, duration, pitchVal, midiChannel, volume, numAllowedCols;
	private Color color;
	private boolean noteIsNull;
	private static int  bitsForNullNoteOrNot, bitsForColStartIdx,bitsForRowIdx,bitsForDuration,
						bitsForColor,bitsForMidiChannel,bitsForVolume;
	private static int[] bitsSetAsideIntervalArr;
	
	
	/**
	 * No-arg constructor, representing a "null" note.
	 */
	public NoteFeatures() {
		this(ScorePane.MAX_CELLS);
	}
	
	/**
	 * "Blank" constructor, representing a "null" note.
	 * @param numCols the total number of columns. This serves as a constraint to the note's colStartIdx attribute,
	 *        when / if this object is constructed using a BitSet as a parameter. See overloaded constructors below.
	 */
	public NoteFeatures(int numCols) {
		this.numAllowedCols = numCols;
		this.noteIsNull = true;
		/* If the below static vars are not initialized, do so now. Only need to initialize these once
		 * as they're static and are invariant no matter how many objects of this class are instantiated.
		 * The below vars define clearly which bits pertain to which features. */
		//value of bitsForNullNoteOrNot is capped at 1, since this feature is a boolean, so need only 1 bit.
		//value of colStartIdx is capped at ScorePane.MAX_CELLS - 1, so set aside enough bits for this
		//value of rowIdx is capped at ScorePane.ROWS - 1. Set aside enough bits
		//value of duration is capped at same as colStartIdx
		//value of pitchVal is capped at ROWS - 0 + PianoRollGUI.MIN_PITCH
		//value of color is capped at ColorIntMap.intToRGBArr.length - 1
		//value of midiChannel is capped at 15 (aka PianoRollGUI's getNumMidiInstrumentChannels() method)
		//value of volume is capped at MidiFile.MAX_VOL = 127
		if (bitsForNullNoteOrNot == 0) {
			bitsForNullNoteOrNot = 1;
			bitsForColStartIdx = this.getNumOfBinaryBitsNeededForInt(ScorePane.MAX_CELLS - 1);
			bitsForRowIdx = this.getNumOfBinaryBitsNeededForInt(ScorePane.ROWS - 1);
			bitsForDuration = bitsForColStartIdx;
			//pitch value is tied to row index, so no need to set aside bits for this
//			bitsForPitchVal = this.getNumOfBinaryBitsNeededForInt(ScorePane.ROWS - 0 + PianoRollGUI.MIN_PITCH);
			bitsForColor = this.getNumOfBinaryBitsNeededForInt(ColorIntMap.getIntToRGBArr().length - 1);
			bitsForMidiChannel = this.getNumOfBinaryBitsNeededForInt(15);
			bitsForVolume = this.getNumOfBinaryBitsNeededForInt(MidiFile.MAX_VOL);
			
			
			/* Now initialize bitsSetAsideArr, which is an interval representation of the no. of bits set aside
			 * for each of the above variables, in the order given. This can be useful for converting
			 * a BitSet to NoteFeatures and vice versa. */
			int[] tmpArr = new int[] {bitsForNullNoteOrNot, bitsForColStartIdx, bitsForRowIdx, bitsForDuration,
					                     bitsForColor, bitsForMidiChannel, bitsForVolume};
			bitsSetAsideIntervalArr = new int[7];
			int index = 0;
			for (int i = 0; i < tmpArr.length; ++i) {
				index += tmpArr[i];
				bitsSetAsideIntervalArr[i] = index;
			} //end for i
			
			//testing
//			System.out.println(Arrays.toString(tmpArr));
//			System.out.println(Arrays.toString(bitsSetAsideIntervalArr));
		} //end if
	}
	
	/**
	 * Constructor from a RectangleNote object
	 * @param rn
	 */
	public NoteFeatures(RectangleNote rn, int numCols) {
		this(numCols);
		if (rn == null) {
			this.noteIsNull = true;
			colStartIdx = rowIdx = duration = pitchVal = -1;
			return;
		}
		this.noteIsNull = false;
		this.colStartIdx = rn.getColIdx();
		this.rowIdx = rn.getRowIdx();
		this.duration = rn.getLength();
		this.pitchVal = ScorePane.ROWS - this.rowIdx + PianoRollGUI.MIN_PITCH;
		this.color = rn.getColor();
		this.midiChannel = rn.getChannel();
		this.volume = rn.getVolume();
	}

	/**
	 * Constructor from a BitSet object.
	 * @param bs
	 */
	public NoteFeatures(BitSet bs) {
		this(bs, ScorePane.MAX_CELLS);
	}
	/**
	 * Overloaded constructor from a BitSet object.
	 * @param bs
	 */
	public NoteFeatures(BitSet bs, int numCols) {
		this(numCols);
		/* Remember:
		 * [0, bitsSetAsideIntervalArr[0])                          : index range for bits holding whether note is null
		 * [bitsSetAsideIntervalArr[0], bitsSetAsideIntervalArr[1]) : index range for bits holding the col start idx.
		 * [bitsSetAsideIntervalArr[1], bitsSetAsideIntervalArr[2]) : index range for bits holding row idx.
		 * [bitsSetAsideIntervalArr[2], bitsSetAsideIntervalArr[3]) : index range for bits holding duration of note
		 * [bitsSetAsideIntervalArr[3], bitsSetAsideIntervalArr[4]) : index range for bits holding color int value
		 * [bitsSetAsideIntervalArr[4], bitsSetAsideIntervalArr[5]) : index range for bits holding Midi Channel
		 * [bitsSetAsideIntervalArr[5], bitsSetAsideIntervalArr[6]) : index range for bits holding volume
		 *  */
		
		/* If this first bit is 0, then note is null and we're done. */
		if (Integer.parseInt(BitSetUtil.convertBitsetToIntStr(bs, 0, bitsSetAsideIntervalArr[0])) == 0) {
			this.noteIsNull = true;
			colStartIdx = rowIdx = duration = pitchVal = -1;
			return;
		}
		
		//If we make it this far the note is not null, but it may violate other constraints. Read on..
		this.noteIsNull = false;
		
		/* col index may exceed the allowed column range. If so, do a mod. */
		this.colStartIdx = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(
							bs, bitsSetAsideIntervalArr[0], bitsSetAsideIntervalArr[1]));
		if (this.colStartIdx >= this.numAllowedCols) this.colStartIdx %= this.numAllowedCols;
		
		/* ergo for row index. */
		this.rowIdx = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(
				bs, bitsSetAsideIntervalArr[1], bitsSetAsideIntervalArr[2]));
		if (this.rowIdx >= ScorePane.ROWS) this.rowIdx %= ScorePane.ROWS;  //mod just in case row index is out of range
		
		/* the duration may be too long and exceed the allowed column range. In that case
		 * just shrink the duration so that it's legal. */
		this.duration = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(
				bs, bitsSetAsideIntervalArr[2], bitsSetAsideIntervalArr[3]));
		if (this.colStartIdx + this.duration > this.numAllowedCols) this.duration = this.numAllowedCols - this.colStartIdx;
		
		/* Pitch value is tied to rowIdx, so BitSet doesn't separately need to store this.
		   As long as the row index is valid, the pitch value should also be valid. */
		this.pitchVal = ScorePane.ROWS - this.rowIdx + PianoRollGUI.MIN_PITCH;
		
		/* For colorIndex, to prevent out of range error we will mod it by the length of the color map if needed */
		int colorIndex = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(
				bs, bitsSetAsideIntervalArr[3], bitsSetAsideIntervalArr[4]));
		if (colorIndex >= ColorIntMap.getIntToRGBArr().length) colorIndex %= ColorIntMap.getIntToRGBArr().length;
		this.color = ColorIntMap.getIntToRGBArr()[colorIndex];
		
		//For midichannel and volume, we don't have to worry about going over allowed range.
		//for channel, we set aside 4 bits (max 1111), which equals 15 at most. (there are 0 thru 15 midi channels)
		//for volume, we set aside 7 bits (max 1111111), which equals 127 at most, (max volume is 127, see MidiFile.MAX_VOL)
		//But just in case things change in the future, we'll do a mod anyway
		this.midiChannel = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(
				bs, bitsSetAsideIntervalArr[4], bitsSetAsideIntervalArr[5]));
		if (midiChannel >= MidiFile.MAX_MIDI_CHANNELS) this.midiChannel %= MidiFile.MAX_MIDI_CHANNELS;
		this.volume = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(
				bs, bitsSetAsideIntervalArr[5], bitsSetAsideIntervalArr[6]));
		if (volume > MidiFile.MAX_VOL) this.volume %= MidiFile.MAX_VOL;
	}
	
	/**
	 * @return a BitSet representation of this Note Feature object
	 */
	public BitSet getBitSet() {
		BitSet bitset = new BitSet();
		if (this.noteIsNull() || this.colStartIdx == -1 || this.rowIdx == -1 || this.duration <= 0 || this.pitchVal < 0) {
			bitset.set(0, false);
			return bitset;
		}
		
		//Convert this.noteIsNull to bitset (true = 1, false = 0), then copy those bits over starting at index 0
		int index = 0;
		bitset.set(index ,true);  //As checked above, this note is not null. Set index 0 to true.
		index += bitsForNullNoteOrNot;
		
		//Convert the colidx to bitset, then copy those bits over starting at index 1
		BitSet bsColStart = BitSetUtil.convertIntToBitSet(this.colStartIdx);
		bitset = BitSetUtil.copyBits(bsColStart, bitset, index);
		index += bitsForColStartIdx;
		
		//Convert the rowidx to bitset, then copy those bits over starting at index 1 + bitsForColStartIdx
		//This is because we're always preserving a total of bitsForColStartIdx bits for the colIdx no matter what,
		//in order to define clearly which bits pertain to which features.
		bitset = BitSetUtil.copyBits(BitSetUtil.convertIntToBitSet(this.rowIdx), bitset, index);
		index += bitsForRowIdx;
		
		//Convert duration to bitset, then copy those bits over starting at index 1 + bitsForColStartIdx + bitsForRowIdx.
		//Rest of this method follows similar logic.
		bitset = BitSetUtil.copyBits(BitSetUtil.convertIntToBitSet(this.duration), bitset, index);
		index += bitsForDuration;
				
		bitset = BitSetUtil.copyBits(BitSetUtil.convertIntToBitSet(ColorIntMap.getRGBHashMap().get(this.color)),
										bitset, index);
		index += bitsForColor;
		
		bitset = BitSetUtil.copyBits(BitSetUtil.convertIntToBitSet(this.midiChannel), bitset, index);
		index += bitsForMidiChannel;
		
		bitset = BitSetUtil.copyBits(BitSetUtil.convertIntToBitSet(this.volume), bitset, index);
		index += bitsForVolume;
		
		return bitset;
	}
	
	/**
	 * Returns a binary string version of this note's features
	 * @return
	 */
	public String getBitSetBinaryString() {
		return BitSetUtil.convertBitsetToBinaryStr(this.getBitSet());
	}
	
	/**
	 * Returns now many binary bits are needed for the given integer
	 * @param n
	 * @return
	 */
	private int getNumOfBinaryBitsNeededForInt(int n) {
		return  BitSetUtil.convertBitsetToBinaryStr(BitSetUtil.convertIntToBitSet(n)).length();
	}
		
	public int getMidiChannel() {
		return midiChannel;
	}

	public int getVolume() {
		return volume;
	}

	public boolean noteIsNull() {
		return this.noteIsNull;
	}
	
	public int getStartIdx() {
		return colStartIdx;
	}

	public int getRowIdx() {
		return rowIdx;
	}

	public int getDuration() {
		return duration;
	}

	public int getPitchVal() {
		return pitchVal;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
				return String.format("%s,%s,%s,%s,%s,%s,%s,%s", this.noteIsNull, colStartIdx, rowIdx, duration, pitchVal,
							  ColorIntMap.getRGBHashMap().get(color), this.midiChannel, this.volume);
	}
	
	/**
	 * Just testing some bitset methods. Can comment out this method once done.
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Testing that some of the note features and bitset util methods work as expected.
		BitSet bs =  new BitSet();
		for (int i = 0; i < 1000000; ++i) {
			if (i % 10000 == 0) System.out.println("Now on iteration i = " + i);
			bs = BitSetUtil.convertIntToBitSet(i);
			String intS = BitSetUtil.convertBitsetToIntStr(bs);
			if (Integer.parseInt(intS) != i) throw new RuntimeException("Something is wrong!");
			String s = BitSetUtil.convertBitsetToBinaryStr(bs);
			String s2 = Integer.toBinaryString(i);
			if (!s.equals(s2)) 
				throw new RuntimeException(String.format(""
						+ "Something is wrong with one or more of the bitset conversion methods for i = %s. s = %s, s2 = %s",
						i, s, s2));
			int length = BitSetUtil.convertBitsetToBinaryStr(BitSetUtil.convertIntToBitSet(i)).length();
			if (length != s2.length()) throw new RuntimeException("Something is wrong!");
		}
		
		//Testing conversion between NoteFeatures and BitSet and vice versa.
		NoteFeatures nf = new NoteFeatures();
		nf.color = Color.BLACK;
		nf.colStartIdx = 12;
		nf.rowIdx = 87;
		nf.duration = 1;
		nf.midiChannel = 13;
		nf.noteIsNull = false;
		nf.pitchVal = ScorePane.ROWS - nf.rowIdx + PianoRollGUI.MIN_PITCH;
		nf.volume = 0;
		
		System.out.println(nf.toString());
		System.out.println(nf.getBitSet());
		System.out.println(nf.getBitSetBinaryString());
		
		//Check if nf2 == nf
		NoteFeatures nf2 = new NoteFeatures(nf.getBitSet());
		System.out.println(nf2.toString());
		System.out.println(nf2.getBitSet());
		System.out.println(nf2.getBitSetBinaryString());
		
		if (!nf2.getBitSetBinaryString().equals(nf.getBitSetBinaryString())) throw new RuntimeException("Something is Wrong!");
		
		//If we get this far without throwing any exception, we're OK
		System.out.println("Test complete! No errors found.");
	}
	
	
}
