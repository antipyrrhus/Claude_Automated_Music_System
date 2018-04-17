package customapi;
import java.util.HashMap;

import javafx.application.Application;
import javafx.scene.paint.Color;
import pianoroll.*;

public class CustomFunctions {
	
	private ScorePane scorePane;
	private static final HashMap<Color, Integer> colorToIntMap = ColorIntMap.colorHashMap;
	private static final Color[] intToColorArr = ColorIntMap.intToColorArr;
	
	
	
	public CustomFunctions(ScorePane scorePane) {
		this.scorePane = scorePane;
	}
	
	public int readColor(int col, int row) {
		return scorePane.getColor(col,row);
	}
	
	/**
	 * Sets the note at location (col, row) to the color which is mapped to the given int value.
	 * If there is no note at that location, creates a note of length 1 and then colors it
	 * @param col
	 * @param row
	 * @param c
	 */
	public void setColor(int col, int row, int c) {
		checkValidColor(c);
		if (scorePane.setColor(col, row, c) == false) {
			//setting color failed for some reason. This can happen, for example, if
			//the note at this location is locked against editing
			System.out.println(ColorEnum.checkColorProperty(readColor(col,row))); //testing
			System.out.println(ColorEnum.checkColorProperty(c)); //testing
		}
	}
	
	private void checkValidColor(int c) {
		if (c < 0 || c >= intToColorArr.length) throw new RuntimeException("Invalid color integer value.");
	}
	
	public boolean isStartOfNote(int col, int row) {
		return scorePane.isStartOfNote(col, row);
	}
	
//	public boolean setStartOfNote(boolean b, int col, int row) {
//		//TODO
//		return scorePane.setStartOfNote(b, col, row);
//	}
	
//	public boolean setDuration(int d, int col, int row) {
//		//TODO?
//	}
	
	public static void main(String[] args) throws Exception {
		Application.launch(PianoRollGUI.class, args);
	}
}
