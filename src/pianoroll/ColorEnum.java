package pianoroll;

import javafx.scene.paint.Color;

/**
 * This class is currently not used. But might come in handy later.
 * @author PB
 *
 */
public enum ColorEnum {
	
	/* Static bindings for certain colors to notes in various states */
	DEFAULT(Color.BLACK), 
	LOCKED(Color.DARKRED), 
	MUTE(Color.GRAY), 
	SELECTED(Color.BLACK);
	
	/* Private constructors to bind these colors */
	private final Color c;
	
	private ColorEnum(Color c) {
		this.c = c;
	}
	
	/* Now other classes can call e.g. ColorEnum.LOCKED.getColor() to get the corresponding color */
	public Color getColor() {
		return this.c;
	}
}
