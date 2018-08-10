package customapi;
import java.util.BitSet;

import convertmidi.MidiFile;
import javafx.scene.paint.Color;
import pianoroll.*;

public class NoteFeatures {
	private int colStartIdx, rowIdx, duration, pitchVal, midiChannel, volume, stencilBits;
	/* stencilBits is a special int value that is intended for the end user to play around with. */
	private static final int stencilBitsLength = 16; //
	private Color color;
	private boolean noteIsNull;
	
	/**
	 * No-arg constructor, representing a "null" note.
	 */
	public NoteFeatures() {
		this(null);
	}
	
	/**
	 * Constructor from a RectangleNote object
	 * @param rn
	 */
	public NoteFeatures(RectangleNote rn) {
		if (rn == null) {
			this.noteIsNull = true;
			colStartIdx = rowIdx = duration = pitchVal = -1;
		} else {
			this.noteIsNull = false;
			this.colStartIdx = rn.getColIdx();
			this.rowIdx = rn.getRowIdx();
			this.duration = rn.getLength();
			this.pitchVal = ScorePane.ROWS - this.rowIdx + PianoRollGUI.MIN_PITCH;
			this.color = rn.getColor();
			this.midiChannel = rn.getChannel();
			this.volume = rn.getVolume();
		}
	}
	
	/**
	 * Sets the stencil attribute to a particular int value.
	 * @param n
	 */
	public void setStencilBits(int n) {
		if (n < 0) throw new RuntimeException("Integer parameter must be nonnegative.");
		this.stencilBits = n;
	}
	
	/**
	 * Sets the stencil attribute via binary string modification,
	 * by setting a particular index of the BitSet version of it to true or false.
	 * @param index
	 * @param b
	 */
	public void setStencilBits(int index, boolean b) {
		if (index < 0 || index >= stencilBitsLength) throw new RuntimeException("Index parameter is out of range");
		BitSet bs = BitSetUtil.convertIntToBitSet(this.stencilBits);
		bs.set(index, b);
		this.stencilBits = Integer.parseInt(BitSetUtil.convertBitsetToIntStr(bs));
	}
	
	public int getStencilBitsInt() {
		return this.stencilBits;
	}
	
	/**
	 * Returns a BitSet version of the stencilbits attribute.
	 * @return
	 */
	public BitSet getStencilBitsBS() {
		return BitSetUtil.convertIntToBitSet(this.stencilBits);
	}
	
	/**
	 * Returns a binary string version of the stencilbits attribute.
	 * @return
	 */
	public String getStencilBitsStr() {
		return Integer.toBinaryString(this.stencilBits);
	}
	
	public boolean getNoteIsNull() {
		return this.noteIsNull;
	}
	public void setNoteIsNull(boolean noteIsNull) {
		this.noteIsNull = noteIsNull;
	}
	
	public int getColStartIdx() {
		return colStartIdx;
	}

	public void setColStartIdx(int colStartIdx) {
		this.colStartIdx = colStartIdx;
	}

	public int getRowIdx() {
		return rowIdx;
	}
	
	public void setRowIdx(int rowIdx) {
		this.rowIdx = rowIdx;
	}

	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getPitchVal() {
		return pitchVal;
	}

	public int getMidiChannel() {
		return midiChannel;
	}
	public void setMidiChannel(int midiChannel) {
		this.midiChannel = midiChannel;
	}
	
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		if (volume < 0 || volume > MidiFile.MAX_VOL) throw new RuntimeException("Volume param out of range");
		this.volume = volume;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
//	public void setColor(int colorInt) {
//		if (colorInt < 0 || colorInt >= ColorIntMap.getIntToRGBArr().length) throw new RuntimeException("Color int param out of range");
//		this.color = ColorIntMap.getIntToRGBArr()[colorInt];
//	}
//	public int getColorInt() {
//		return ColorIntMap.getRGBHashMap().get(color);
//	}
	
	@Override
	public String toString() {
				return String.format("(%s,%s,%s,%s,%s,%s,%s,%s)", this.noteIsNull, colStartIdx, rowIdx, duration, pitchVal,
							  this.midiChannel, this.volume, ColorEnum.getColorInt(color), this.stencilBits);
	}
}
