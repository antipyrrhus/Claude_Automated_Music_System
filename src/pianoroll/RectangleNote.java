package pianoroll;

import convertmidi.MidiFile;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

//A wrapper class to indicate each note on the measure.
//Later this will support notes with different durations
public class RectangleNote extends Rectangle implements Comparable<RectangleNote> {
	int colIdx, rowIdx, length, index, channel, volume, origVolume;  //index = rectArrIndex
	Color color, origColor;  //color = current color (including red, black). origColor = pitch color
//	boolean isMelody, isSelected, isMute;
	boolean isSelected;
//	private final int DARKRED_IDX = 12, BLACK_IDX = 13, GREY_IDX = 14;

	//		RectangleNote(double x, double y, double width, double height) {
	//			super(x,y,width,height);
	//		}

	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length) {
		super(x,y,width * length,height);
		this.volume = MidiFile.MAX_VOL;
		this.origVolume = volume;
		this.colIdx = startIdx;
		this.length = length;
//		this.rowIdx = (int)Math.round(y / heightPerCell);
		this.rowIdx = rowIdx;
		System.out.printf("Created new RectagleNote with startIdx %s and length %s\n", startIdx, length);
//		color = Color.GREEN;
		color = ColorEnum.DEFAULT.getColor();
		origColor = color;
//		isMelody = false;
//		isMute = false;
		isSelected = false;
		channel = 0;
		this.setFill(color);
		this.setStroke(Color.WHITE);
	}
	


	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length,
			boolean isSelected, Color color, Color origColor) {
		this(x,y,width,height,startIdx,rowIdx,length);
		this.color = color;
		this.origColor = origColor;
		this.isSelected = isSelected;
//		this.isMelody = isMelody;
		this.setFill(color);
	}
	
	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length,
			boolean isSelected, Color color, Color origColor, int rect1DIndex) {
		this(x,y,width,height,startIdx,rowIdx,length,isSelected,color,origColor);
		this.index = rect1DIndex;
	}
	
	RectangleNote(double x, double y, double width, double height, int startIdx, int rowIdx, int length,
			boolean isSelected, Color color, Color origColor, int rect1DIndex, int channel, int volume) {
		this(x,y,width,height,startIdx,rowIdx,length,isSelected,color,origColor,rect1DIndex);
//		this.setMute(isMute);
		this.setChannel(channel);
		this.setVolume(volume);
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public int getChannel() {
		return this.channel;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
		this.origVolume = volume;
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
//				this.isMelody, this.isSelected, this.color, this.origColor, this.index, this.isMute,
				this.isSelected, this.color, this.origColor, this.index, 
				this.channel, this.volume);
	}
	
	public void setSelected(boolean selected) {
//		if (this.isMelody && selected == true) {
//			this.isSelected = false;
//			return;
//		}
		this.isSelected = selected;
		color = selected ? ColorEnum.SELECTED.getColor() : this.isMute() ? ColorEnum.MUTE.getColor() : origColor;
		this.setFill(color);

	}

//	public void setMelody(boolean melody) {
//		this.isMelody = melody;
//		color = melody ? Color.DARKRED : origColor;
//		this.setFill(color);
//		if (melody) this.setSelected(false);
//	}
	
	public void setMute(boolean mute) {
//		this.isMute = mute;
		if (mute) {
			this.volume = 0;
//			color = ColorIntMap.intToRGBArr[GREY_IDX];
			color = ColorEnum.MUTE.getColor();
			int tmpDebugCheckColor = ColorEnum.MUTE.getColorInt();
			System.out.println(tmpDebugCheckColor);
		} else {
//			if (this.origVolume == 0) this.origVolume = MidiFile.MAX_VOL;
			this.volume = this.origVolume;
			color = origColor;
		}
		this.setColor(color);
//		else if (this.isMelody) color = Color.DARKRED;
		
	}

	public boolean isMute() {
//		return this.isMute; 
		return (this.volume == 0);
	}
	//User-defined color setting
	public void setColor(int c) {
		if (c < 0 || c >= ColorEnum.numOfSpecialColors) throw new RuntimeException();
		setColor(ColorIntMap.intToRGBArr[c]);
	}
	
	public void setColor(Color c) {
//		if (this.isMelody) return;
		this.color = c;
		this.setFill(color);
		if (c == ColorEnum.SELECTED.getColor()) {
			this.isSelected = true;
		}
//		else if (c == Color.DARKRED) {
//			this.isMelody = true;
//			this.isSelected = false;
//		}
		else if (c == ColorEnum.MUTE.getColor()) {
//			this.isMute = true;
			this.isSelected = false;
		}
		else if (c == ColorEnum.DEFAULT.getColor()) {
			//TODO?
		}
		else if (c == ColorEnum.LOCKED.getColor()) {
			//TODO?
		}
		else {
			this.origColor = color;
		}
	}
	
	public int getColIdx() {
		return colIdx;
	}

	public int getRowIdx() {
		return rowIdx;
	}

	public int getLength() {
		return length;
	}

	public Color getColor() {
		return color;
	}

//	public boolean isMelody() {
//		return isMelody;
//	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setOrigColor(int c) {
		this.origColor = ColorIntMap.intToRGBArr[c];
	}

	@Override
	public int compareTo(RectangleNote rn) {
		if (this.colIdx == rn.colIdx) {
			return this.rowIdx - rn.rowIdx;
		}
		return this.colIdx - rn.colIdx;
	}
}