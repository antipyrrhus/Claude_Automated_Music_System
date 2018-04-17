package customapi;

//import java.util.HashMap;

import javafx.scene.paint.Color;
import pianoroll.ColorIntMap;

public enum ColorEnum {
	LOCKED, SELECTED, MUTED;
//	private static final HashMap<Color, Integer> colorToIntMap = ColorIntMap.colorHashMap;
	private static final Color[] intToColorArr = ColorIntMap.intToColorArr;
	
	
	public static ColorEnum checkColorProperty(int color) {
		if (intToColorArr[color] == Color.DARKRED) {
			return LOCKED;
		} else if (intToColorArr[color] == Color.BLACK) {
			return SELECTED;
		} else if (intToColorArr[color] == Color.GREY) {
			return MUTED;
		} else {
			return null;
		}
	}
	
}
