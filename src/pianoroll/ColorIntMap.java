package pianoroll;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class ColorIntMap {
	//For use by inner class RectangleNote for translating colors to ints and vice versa
	public static final Color[] intToColorArr = {Color.GREEN, Color.AQUA,
			Color.CHARTREUSE, Color.DARKCYAN, Color.DARKORANGE, Color.CRIMSON, Color.ORCHID, Color.OLIVE, Color.DEEPPINK,
			Color.GOLD, Color.GREENYELLOW, Color.PINK, Color.DARKRED, Color.BLACK, Color.GREY};
	public static final HashMap<Color, Integer> colorHashMap = new HashMap<Color,Integer>() 
	{
		private static final long serialVersionUID = 1L;
		{	     
			for (int i = 0; i < intToColorArr.length; ++i) {
				put(intToColorArr[i], i);
			}
		}
	};
}
