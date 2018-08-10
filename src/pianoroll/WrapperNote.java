package pianoroll;

import javafx.scene.paint.Color;
import main.Note;

public class WrapperNote extends Note {
	private int colIdx, colorInt, origColorInt;
	private boolean isNoteOff;	//To indicate that this is a dummy pitch indicating NOTE OFF for transcribing to midi purposes. 
	
	public WrapperNote(int pitch) {
		super(pitch);
		this.isNoteOff = false;
//		this.colorInt = ColorIntMap.colorHashMap.get(Color.GREEN);
//		this.colorInt = ColorIntMap.rgbHashMap.get(Color.GREEN);
		this.colorInt = ColorEnum.DEFAULT.getColorInt();
	}
	
	public WrapperNote(Note n, int colIdx) {
		this(n.getPitch());
		this.colIdx = colIdx;
	}
	
	public WrapperNote(int pitch, int colIdx, int color) {
		super(pitch);
		this.isNoteOff = false;
		this.colorInt = color;
		this.origColorInt = color;
		this.colIdx = colIdx;
	}
	
	public WrapperNote(int pitch, int colIdx, int color, int origColor) {
		this(pitch, colIdx, color);
		this.origColorInt = origColor;
	}
	
	public WrapperNote(int pitch, int colIdx, int color, int origColor, int duration) {
		this(pitch,colIdx,color,origColor);
		this.setDuration(duration);
	}
	
	public WrapperNote(int pitch, int colIdx, Color color, Color origColor, int duration) {
//		this(pitch, colIdx, ColorIntMap.colorHashMap.get(color), ColorIntMap.colorHashMap.get(origColor), duration);
		
//		this(pitch, colIdx, ColorIntMap.rgbHashMap.get(color), ColorIntMap.rgbHashMap.get(origColor), duration);
		this(pitch, colIdx, ColorEnum.getColorInt(color), ColorEnum.getColorInt(origColor), duration);
		
	}
	
	public WrapperNote(int pitch, int colIdx, Color color, Color origColor, int duration, int volume, int channel) {
		this(pitch, colIdx, color, origColor, duration);
		this.setChannel(channel);
		this.setVolume(volume);
	}
	
	public WrapperNote(RectangleNote rn) {
		this(ScorePane.ROWS - rn.getRowIdx() + PianoRollGUI.MIN_PITCH, rn.getColIdx(), rn.color, rn.origColor, rn.getLength(),
				rn.getVolume(), rn.getChannel());
	}
	
	public int getOrigColorInt() {
		return origColorInt;
	}

	public void setOrigColorInt(int origColorInt) {
		this.origColorInt = origColorInt;
	}

	public void setColorInt(int color) {
		this.colorInt = color;
	}
	
	public int getColorInt() {
		return colorInt;
	}

	public void setIdx(int colIdx) {
		this.colIdx = colIdx;
	}
	
	public int getColIdx() {
		return this.colIdx;
	}
	
	public void setNoteOff(boolean val) {
		this.isNoteOff = val;
	}
	
	public boolean isNoteOff() {
		return this.isNoteOff;
	}
	
	@Override
	public String toString() {
		return super.toString() + " colIdx : " + this.colIdx;
	}
}