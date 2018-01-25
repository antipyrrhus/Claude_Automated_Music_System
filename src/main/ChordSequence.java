package main;
import java.util.*;

/**
	 * ChordSequence class. A sequence of chords.
	 * @author Yury Park
	 * TODO revisit this class to see if it's necesssary
	 */
	public class ChordSequence {
		private Chord[] seq;
		
		public ChordSequence(int n) {
			this.seq = new Chord[n];
		}
		
		public ChordSequence(Chord[] seq) {
			this.seq = seq;
		}
		
		public void setChord(int i, Chord c) {
			this.seq[i] = c;
		}
		
		public int length() {
			return this.seq.length;
		}
		
		public Chord getChord(int i) {
			return this.seq[i];
		}
		
		public Chord[] getSeq() {
			return seq;
		}
		
		public ChordSequence copy() {
			Chord[] copyChordArr = new Chord[this.seq.length];
			for (int i = 0; i < this.seq.length; ++i) {
				if (this.seq[i] == null) {
					copyChordArr[i] = null;
				} else {
					Chord c_copy = this.seq[i].copy();
					copyChordArr[i] = c_copy;
				}
			}
			return new ChordSequence(copyChordArr);
		}

		@Override
		public String toString() {
			return Arrays.toString(seq);
		}
	}
	//end class ChordSequence