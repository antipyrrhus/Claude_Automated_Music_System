package pianoroll;

import convertmidi.MidiFile;
import javafx.scene.control.Button;
import main.Note;

public class NoteButton_DELETE extends Button implements Comparable<NoteButton_DELETE> {
	private boolean activated;
	private Note note;
	private int i, j;
	
	public NoteButton_DELETE(int i, int j) {
		this("", i, j);
	}
	
	/**
	 * 
	 * @param str label associated with the button, if any
	 * @param i row of the pitch location
	 * @param j col of the pitch location
	 */
	public NoteButton_DELETE(String str, int i, int j) {
		this(str, i+21, -1, i, j);	//Set pitch offset by the amount given (otherwise the lowest pitch sounds too low)
	}
	
	public NoteButton_DELETE(String str, int pitch, int duration, int i, int j) {
		super(str);
		//Initialize note with the given pitch
		this.note = new Note(pitch, duration == -1 ? MidiFile.CROTCHET : duration);
		this.activated = false;
		this.i = i; //row
		this.j = j; //col
	}

	public int getPitch() {
		return this.note.getPitch();
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean b) {
		this.activated = b;
	}



	public Note getNote() {
		return note;
	}



	public int getI() {
		return i;
	}



	public int getJ() {
		return j;
	}

	@Override
	public int compareTo(NoteButton_DELETE nb) {
		if (this.i - nb.i != 0) return this.i - nb.i;
		return this.j - nb.j;
	}
	
}
