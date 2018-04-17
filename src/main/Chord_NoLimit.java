package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Chord_NoLimit {

	private ArrayList<Note> notesAL;  //Notes comprising this chord
	private Interval intv;
	private long tick;

	public Chord_NoLimit(Note...nArr) {
		this(0, nArr);
	}
	
	/**
	 * Instantiates a new chord with the given note Array, and with the tick information
	 * to keep track of where the Chord is located within a given MIDI file.
	 * 
	 * @param tick
	 * @param nArr
	 */
	public Chord_NoLimit(long tick, Note...nArr) {
		this.tick = tick;
		HashSet<Note> hs = new HashSet<>();
		
		//Take care of duplicate notes
		for (Note n : nArr) {
			hs.add(n);
		}
		
		this.notesAL = new ArrayList<Note>(hs);
		Collections.sort(notesAL);
		int index = 0;
		
		//The above hashset only gets rid of duplicate notes. But we want to also get rid of
		//notes that have same pitch but different duration (only preserve the note with longest duration)
		while (index+1 < notesAL.size()) {
			if (notesAL.get(index).getPitch() == notesAL.get(index+1).getPitch()) {
				notesAL.remove(index);
			} else index++;
		}
		this.intv = new Interval(this);
	}
	
	public long getTick() {
		return tick;
	}

	public void setTick(long tick) {
		this.tick = tick;
	}

	public Interval getIntv() {
		return intv;
	}

	public int getNumNotes() {
		return notesAL.size();
	}
	
	/**
	 * Adds note to the arraylist such that all notes remain sorted
	 * @param n
	 */
	public void addNote(Note n) {
		if (this.notesAL.isEmpty()) {
			notesAL.add(n);
		} else {
			for (int i = 0; i < this.notesAL.size(); ++i) {
				Note currN = notesAL.get(i);
				if (n.getPitch() == currN.getPitch()) {
					if (n.getDuration() > currN.getDuration()) {
						notesAL.set(i, n);
					}
					this.intv = new Interval(this);
					return;
				} else if (n.getPitch() < currN.getPitch()) {
					notesAL.add(i, n);
					this.intv = new Interval(this);
					return;
				}
			} //end for
			notesAL.add(n);
			this.intv = new Interval(this);
		}
	}
	
	
//	public void setNote(int index, Note note) {
//		this.notesAL.set(index, note);
//		this.intv = new Interval(this);
//	}
	
	public ArrayList<Note> getNotesAL() {
		return notesAL;
	}

	public Note getNote(int index) {
		return this.notesAL.get(index);
	}

	/**
	 * Assumes the arraylist of notes is sorted, and that there are no duplicate pitches.
	 * @param pitch
	 * @return
	 */
	public Note binarySearchNote(int pitch) {
		return binarySearchNote(0, notesAL.size() - 1, pitch);
	}
	
	private Note binarySearchNote(int lft, int rgt, int pitch) {
		if (lft > rgt) return null;
		
		int mid = (lft + rgt)/2;
		Note currN = notesAL.get(mid);
		if (currN.getPitch() == pitch) return currN;
		else if (currN.getPitch() < pitch) {
			return binarySearchNote(mid+1, rgt, pitch);
		} else return binarySearchNote(lft, mid-1, pitch);
	}

	@Override
	public String toString() {
		return "Chord_NoLimit [tick=" + tick + ", notesAL=" + notesAL + ", intv=" + intv + "]";
	}
}
