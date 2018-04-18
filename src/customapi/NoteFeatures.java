package customapi;

import javafx.scene.paint.Color;
import pianoroll.*;

public class NoteFeatures {
	private int colStartIdx, rowIdx, duration, pitchVal;
	private Color color;
	private boolean isMute, isSelected, isMelody, noteIsNull;
	
	public NoteFeatures(RectangleNote rn) {
		if (rn == null) {
			this.noteIsNull = true;
			return;
		}
		this.noteIsNull = false;
		this.colStartIdx = rn.getColIdx();
		this.rowIdx = rn.getRowIdx();
		this.duration = rn.getLength();
		this.pitchVal = ScorePane.ROWS - this.rowIdx + PianoRollGUI.MIN_PITCH;
		this.color = rn.getColor();
		this.isMute = rn.isMute();
		this.isSelected = rn.isSelected();
		this.isMelody = rn.isMelody();
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

	public boolean isMute() {
		return isMute;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public boolean isMelody() {
		return isMelody;
	}

	@Override
	public String toString() {
		return this.noteIsNull ? 
				"-" : 
				String.format("%s,%s,%s,%s,%s,%s,%s,%s", colStartIdx, rowIdx, duration, pitchVal,
							  color, isMute ? "1" : "0", isSelected ? "1" : "0", isMelody ? "1" : "0");
	}
	
	
}
