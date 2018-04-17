package pianoroll;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

//A wrapper class to indicate each note on the measure.
//Later this will support notes with different durations
public class RectangleNote extends Rectangle implements Comparable<RectangleNote> {
	int colIdx, rowIdx, length, index;  //index = rectArrIndex
	Color color, origColor;  //color = current color (including red, black). origColor = pitch color
	boolean isMelody, isSelected, isMute;
	private final int DARKRED_IDX = 12, BLACK_IDX = 13, GREY_IDX = 14;

	//		RectangleNote(double x, double y, double width, double height) {
	//			super(x,y,width,height);
	//		}

	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length) {
		super(x,y,width * length,height);
		this.colIdx = startIdx;
		this.length = length;
//		this.rowIdx = (int)Math.round(y / heightPerCell);
		this.rowIdx = rowIdx;
		System.out.printf("Created new RectagleNote with startIdx %s and length %s\n", startIdx, length);
		color = Color.GREEN;
		origColor = color;
		isMelody = false;
		isMute = false;
		isSelected = false;
		this.setFill(color);
		this.setStroke(Color.WHITE);
	}
	
	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length,
			boolean isMelody, boolean isSelected, Color color, Color origColor) {
		this(x,y,width,height,startIdx,rowIdx,length);
		this.color = color;
		this.origColor = origColor;
		this.isSelected = isSelected;
		this.isMelody = isMelody;
		this.setFill(color);
	}
	
	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length,
			boolean isMelody, boolean isSelected, Color color, Color origColor, int rect1DIndex) {
		this(x,y,width,height,startIdx,rowIdx,length,isMelody,isSelected,color,origColor);
		this.index = rect1DIndex;
	}
	
	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length,
			boolean isMelody, boolean isSelected, Color color, Color origColor, int rect1DIndex, boolean isMute) {
		this(x,y,width,height,startIdx,rowIdx,length,isMelody,isSelected,color,origColor,rect1DIndex);
		this.setMute(isMute);
	}

	/**
	 * Overloaded constructor
	 * @param colIndex
	 * @param rowIndex
	 * @param width rectangle's width
	 * @param height rectangle's height
	 * @param length no. of columns that the rectangle spans (note's duration)
	 */
//	RectangleNote(int colIndex, int rowIndex, double width, double height, int length) {
//		this(colIndex*widthPerCell, rowIndex*heightPerCell, width, height, colIndex, rowIndex, length);
//	}

//	RectangleNote(int colIndex, int rowIndex, double width, double height, int length, 
//			boolean isMelody, boolean isSelected, Color color, Color origColor) {
//		this(colIndex, rowIndex, width, height, length);
//		this.color = color;
//		this.origColor = origColor;
//		//			this.setFill(color);
//		this.setMelody(isMelody);
//		this.setSelected(isSelected);
//	}

	/**
	 * @return a copy of this object.
	 */
	public RectangleNote copy() {
		return new RectangleNote(this.getX(), this.getY(), this.getWidth() / this.length, this.getHeight(),
				this.colIdx, this.rowIdx, this.length,
				this.isMelody, this.isSelected, this.color, this.origColor, this.index, this.isMute);
	}
	
	public void setSelected(boolean selected) {
		if (this.isMelody && selected == true) {
			this.isSelected = false;
			return;
		}
		this.isSelected = selected;
		color = selected ? Color.BLACK : this.isMelody ? Color.DARKRED : this.isMute ? Color.GREY : origColor;
		this.setFill(color);

	}

	public void setMelody(boolean melody) {
		this.isMelody = melody;
		color = melody ? Color.DARKRED : origColor;
		this.setFill(color);
		if (melody) this.setSelected(false);
	}
	
	public void setMute(boolean mute) {
		this.isMute = mute;
		if (mute) {
			color = ColorIntMap.intToColorArr[GREY_IDX];
		}
		else if (this.isMelody) color = Color.DARKRED;
		else color = origColor;
		this.setFill(color);
	}

	public boolean isMute() {
		return this.isMute; 
	}
	//User-defined color setting
	public void setColor(int c) {
		if (this.isMelody) return;
		this.color = ColorIntMap.intToColorArr[c];
		this.setFill(color);
		if (c == this.BLACK_IDX) this.isSelected = true;
		else if (c == this.DARKRED_IDX) {
			this.isMelody = true;
			this.isSelected = false;
		} else if (c == this.GREY_IDX) {
			this.isMute = true;
			this.isSelected = false;
		}
		else {
			this.origColor = color;
		}
	}
	
	public void setOrigColor(int c) {
		this.origColor = ColorIntMap.intToColorArr[c];
	}

	@Override
	public int compareTo(RectangleNote rn) {
		if (this.colIdx == rn.colIdx) {
			return this.rowIdx - rn.rowIdx;
		}
		return this.colIdx - rn.colIdx;
	}
}