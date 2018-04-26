package pianoroll;

import java.util.HashMap;

import javafx.scene.paint.Color;

public class ColorIntMap {
	//For use by inner class RectangleNote for translating colors to ints and vice versa
	public static final Color[] intToColorArr = {Color.GREEN, Color.AQUA,
			Color.CHARTREUSE, Color.DARKCYAN, Color.DARKORANGE, Color.CRIMSON, Color.ORCHID, Color.OLIVE, Color.DEEPPINK,
			Color.GOLD, Color.GREENYELLOW, Color.PINK, Color.DARKRED, Color.BLACK, Color.GREY};
	
	public static HashMap<Color, Integer> colorHashMap = new HashMap<Color,Integer>() 
	{
		private static final long serialVersionUID = 1L;
		{	     
			for (int i = 0; i < intToColorArr.length; ++i) {
				put(intToColorArr[i], i);
			}
		}
	};
	
	public static Color[] intToRGBArr = null;
	public static HashMap<Color, Integer> rgbHashMap = null;
	
	public static void initRGBColor() {
		if (intToRGBArr == null) {
			int stepsize = 15;
			int numColors = 256 / stepsize + (256 % stepsize == 0 ? 0 : 1);
			intToRGBArr = new Color[intToColorArr.length + (numColors * numColors * numColors)];
			for (int i = 0; i < intToColorArr.length; ++i) {
				intToRGBArr[i] = intToColorArr[i];
			}
			int index = intToColorArr.length;
			for (int i = 0; i < 256; i += stepsize)
				for (int j = 0; j < 256; j += stepsize) 
					for (int k = 0; k < 256; k += stepsize)
						intToRGBArr[index++] = Color.rgb(i,j,k);

			rgbHashMap = new HashMap<Color, Integer>();
			for (int i = 0; i < intToRGBArr.length; ++i) {
				rgbHashMap.put(intToRGBArr[i], i);
			}
		}
	}
	
	public static Color[] getIntToRGBArr() {
		if (intToRGBArr == null) {
			initRGBColor();
		}
		return intToRGBArr;
	}
	
	public static HashMap<Color, Integer> getRGBHashMap() {
		if (rgbHashMap == null) {
			initRGBColor();
		}
		return rgbHashMap;
	}
	
}
