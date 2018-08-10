package pianoroll;

import javafx.scene.paint.Color;
import java.util.HashMap;
/**
 * This class is currently not used. But might come in handy later.
 * @author PB
 *
 */
public enum ColorEnum {
//	private final 
	
	
	/* Static bindings for certain colors to notes in various states */
	DEFAULT(Color.GREEN), 
	LOCKED(Color.DARKRED), 
	MUTE(Color.GRAY), 
	SELECTED(Color.BLACK);
	
	public static final Color[] intToColorArr = {Color.GREEN, Color.DARKRED, Color.BLACK, Color.GRAY};
	public static final int numOfSpecialColors = intToColorArr.length;
	private static HashMap<Color, Integer> colorHashMap = new HashMap<Color,Integer>() 
	{
		private static final long serialVersionUID = 1L;
		{	     
			for (int i = 0; i < intToColorArr.length; ++i) {
				put(intToColorArr[i], i);
			}
		}
	};
	
	/* Private constructors to bind these colors */
	private final Color c;
	
	private ColorEnum(Color c) {
		this.c = c;
	}
	
	/* Now other classes can call e.g. ColorEnum.LOCKED.getColor() to get the corresponding color */
	public Color getColor() {
		return this.c;
	}
	
	public int getColorInt() {
//		if (colorHashMap.get(this.c) != colorHashMap.get(intToColorArr[colorHashMap.get(this.c)])) throw new RuntimeException();
		return colorHashMap.get(this.c);
	}
	
	public static Color getColor(int c) {
		if (c < 0 || c >= intToColorArr.length) throw new RuntimeException("color index out of range");
		return intToColorArr[c];
	}
	
	public static int getColorInt(Color c) {
		Integer ret = colorHashMap.get(c);
		if (ret == null) throw new RuntimeException("No index no. is associated with this color");
		return ret;
	}
}
