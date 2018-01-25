package main;
import java.util.Arrays;
//import java.util.HashMap;

/**
	 * Chord class. Consists of 1 thru 4 notes.
	 * REM: all Chord constructors should be composed of exactly 4 non-null notes, including the melody,
	 * and should be in ascending pitch order. However, for debugging purposes,
	 * the constructor accepts it if the melody is null.
	 * However, the constructor (and particularly the checkValid() method) will explicitly check
	 * to see that the first 3 notes are non-null, and that the notes are in ascending pitch order.
	 * although 
	 * @author Yury Park
	 */
	public class Chord {
		private Note[] notes;  //Notes comprising this chord
		private int numNotes;
//		private ChordInterval ci; //associated chord interval with this chord
		private Interval intv;
		
		/**
		 * Constructor with up to 4 notes. Assumes that n1 is the lowest note.
		 * @param n1 bass
		 * @param n2 tenor
		 * @param n3 mezzo
		 * @param n4 soprano (melody)
		 */
		public Chord(Note n1, Note n2, Note n3, Note n4) {
			this.notes = new Note[] {n1, n2, n3, n4};
			this.numNotes = countNotes();
//			this.ci = computeChordInterval();
			this.intv = new Interval(this);
			checkValid();
		}
		
		/**
		 * Constructor given a Note array, which may be of length <= 4.
		 * @param nArr
		 */
		public Chord(Note...nArr) {
			if (nArr.length == 4) {
				this.notes = nArr;
			} else {
				this.notes = new Note[]{null, null, null, null};
				for (int i = 0; i < nArr.length; ++i) {
					this.notes[i] = nArr[i];
				}
				Note lastNote = nArr[nArr.length - 1];
				for (int i = nArr.length; i < this.notes.length; ++i) {
					this.notes[i] = lastNote.copy();
				}
			}
			this.numNotes = countNotes();
//			this.ci = computeChordInterval();
			this.intv = new Interval(this);
			checkValid();
		}
		
		/**
		 * Constructor given the interval and the lowest note
		 * @param ci ChordInterval
		 * @param baseNote lowest note
		 */
//		public Chord(ChordInterval ci, Note baseNote) {
//			Interval intv = ci.getBaseInterval();
//			this.ci = ci;
//			if (intv.length() == 0) {
//				
//				this.notes = new Note[] {
//						baseNote,
//						new Note(baseNote.getPitch()), 
//						new Note(baseNote.getPitch()), 
//						new Note(baseNote.getPitch())
//						};
//			} else {
//				this.notes = new Note[4];
//				Arrays.fill(this.notes, null);
//				this.notes[0] = baseNote;
//				for (int i = 1; i <= intv.length(); ++i) {
//					this.notes[i] = new Note(baseNote.getPitch() + intv.get(i-1));
//				}
//			}
//			//end if/else
////			System.out.println(this.ci);
////			System.out.println(computeInterval());
////			System.out.println(this);
//			this.numNotes = countNotes();
//			if (!this.ci.equals(computeChordInterval())) throw new RuntimeException();
//			checkValid();
//		}
		public Chord(Interval intv, Note baseNote, int duration) {
			this(intv, baseNote);
			this.setDuration(duration);
		}
		public Chord(Interval intv, Note baseNote) {
//			Interval intv = ci.getBaseInterval();
//			this.ci = ci;
			this.intv = intv;
			if (intv.length() == 0) {
				
				this.notes = new Note[] {
						baseNote,
						new Note(baseNote.getPitch()), 
						new Note(baseNote.getPitch()), 
						new Note(baseNote.getPitch())
						};
			} else {
				this.notes = new Note[4];
				Arrays.fill(this.notes, null);
				this.notes[0] = baseNote;
				for (int i = 1; i <= intv.length(); ++i) {
					this.notes[i] = new Note(baseNote.getPitch() + intv.get(i-1));
				}
				for (int i = intv.length() + 1; i < 4; ++i) {
					this.notes[i] = this.notes[i-1].copy();
				}
			}
			//end if/else
//			System.out.println(this.ci);
//			System.out.println(computeInterval());
//			System.out.println(this);
			this.numNotes = countNotes();
//			if (!this.ci.equals(computeChordInterval())) throw new RuntimeException();
			checkValid();
		}
		public Chord (Interval intv, int basePitch, int duration) {
			this(intv, new Note(basePitch, duration), duration);
		}
		
		private void setDuration(int duration) {
			for (Note note : this.getNotes()) {
				note.setDuration(duration);
			}
		}
		
		/**
		 * Checks that this Chord consists of notes in ascending pitch order,
		 * and that at least the first 3 notes (pertaining to the harmony minus the melody)
		 * are non-null. Throws an Exception if not.
		 */
		private void checkValid() {
//			if (this.numNotes < 3) throw new RuntimeException("Valid chord must contain at least 3 notes");
//			
//			for (int i = 0; i < this.notes.length - 1; ++i) {
//				if (this.notes[i] == null) throw new RuntimeException("First 3 notes must be non-null");
//				if (this.notes[i+1] == null) continue;
//				if (this.notes[i].getPitch() > this.notes[i+1].getPitch())
//					throw new RuntimeException("Notes must be in ascending pitch order");
//			}
			if (this.checkValidBoolean() == false) 
				throw new RuntimeException("Invalid chord. Must contain at least 3 notes"
						+ " and notes must be in ascending pitch order.");
		}
		
		public boolean checkValidBoolean() {
			if (this.numNotes < 3) return false;
			
			for (int i = 0; i < this.notes.length - 1; ++i) {
				if (this.notes[i].getPitch() < 0) return true;		//valid "rest" note.
				if (this.notes[i] == null) return false;
				if (this.notes[i+1] == null) continue;
				if (this.notes[i].getPitch() > this.notes[i+1].getPitch() &&
						this.notes[i+1].getPitch() >= 0)
					//Notes must be in ascending pitch order
					return false;
			}
			return true;
		}
		
		private int countNotes() {
			int ret = 0;
			for (int i = 0; i < this.notes.length; ++i) {
				if (this.notes[i] == null) continue;
				ret++;
			}
			return ret;
		}
		
		/**
		 * Change a specified note of the chord to a different pitch
		 * @param i
		 * @param pitch
		 */
		public void changeNotePitch(int i, int pitch) {
			this.notes[i].setPitch(pitch);
			this.intv = new Interval(this);
		}
		
		public int getNumNotes() {
			return numNotes;
		}

		/**
		 * Sets a Note at a given position index
		 * @param index
		 * @param note
		 */
		public void setNote(int index, Note note) {
			this.notes[index] = note;
		}
		
		/**
		 * Computes the interval for the current Chord
		 * @return the ChordInterval associated with the current Chord
		 */
//		private ChordInterval computeChordInterval() {
//			return new ChordInterval(this); //use ChordInterval constructor
//		}
		
		/**
		 * Distance function to compute the "Hamming distance" between this chord and another chord c
		 * If the total no. of notes comprising current chord differs from that comprising the other chord,
		 * just computes the sum of total distance between corresponding non-null notes,
		 * ignoring the rest.
		 * This method ONLY checks the first 3 notes of the Chord, ignoring the Soprano part.
		 * This is because we cannot control the Soprano, which is provided to us.
		 * So there is no point to computing the hamming distance between adjacent sopranos
		 * for purposes of computing the best accompanying harmony.
		 * 
		 * @param c a Chord
		 * @return Total Hamming distance computed in terms of semitonic intervals
		 */
		public int computeDistance(Chord c) {
			//initialize distance to return
			int ret = 0;
			for (int i = 0; i < this.notes.length - 1; ++i) {
				//Check to make sure the Notes are non-null
				Note n1 = this.getNotes()[i];
				Note n2 = c.getNotes()[i];
				//Compute distance if both notes are non-null
				//Otherwise, just move on to the next loop iteration
				if (n1 != null && n2 != null) {
					ret += Math.abs(n1.getPitch() - n2.getPitch());
				}
			}
			//end for
			return ret;
		}
		
		/**
		 * Given another Chord c and a Transition map, gets the transition weight (if any)
		 * associated with transitioning from this Chord to Chord c
		 * 
		 * UPDATE: method moved to Transition class and accessed as static method
		 * 
		 * @param c the other Chord
		 * @param map Transition map
		 * @return weight associated with transitioning from this Chord to Chord c 
		 */
//		public double getTransitionWeight(
//				Chord c, HashMap<Interval, HashMap<Interval, Integer[]>> map) {
//			
//			Interval intv1 = this.ci.getBaseInterval();
//			Interval intv2 = c.getCi().getBaseInterval();
//			HashMap<Interval, Integer[]> transitionMap = map.get(intv1);
//			Integer[] arr = transitionMap.get(intv2);
//			if (arr == null) return 0; 
//			
////			int semitone_steps = arr[0];
//			int weight = arr[1];
//			return weight;
//		}
		
		/**
		 * Inverts the first 3 notes upwards if possible. (excluding the melody which is the 4th note)
		 * (intended to be used for calculating the harmony without
		 * changing the melody (which constitutes the 4th note)
		 * @return true IFF inversion successful
		 */
		public boolean invertUp() {
			//Get first note and raise it by an octave
			//as many times as needed until it is above the 3rd pitch
			int currPitch = this.notes[0].getPitch();
			while(currPitch < this.notes[2].getPitch()) {
				currPitch += Harmonizer.SCALE;
			}
			//Check if the pitch is above the melody 
			//(assuming we're given the melody -- if we have no melody, return false). 
			//If the pitch is above the melody, we can't invert this chord up. Just return false
			if (this.notes[3] != null && currPitch > this.notes[3].getPitch()) {
				return false;
			}
			
			//If we get this far, we can perform the inversion.
			this.notes[0] = this.notes[1];
			this.notes[1] = this.notes[2];
			this.notes[2] = new Note(currPitch);
//			this.ci = computeChordInterval();
			this.intv = new Interval(this);
			return true;
		}
		
		/**
		 * inverts all 4 notes upwards. (intended to be used for figuring out the type of chord this is)
		 * Assumes that we're given all 4 notes including the melody. If not, just returns false
		 * @return true IFF inversion successful
		 */
		public boolean invertUpAll() {
			//Check that the melody exists. If not return false
			if(this.notes[3] == null) return false;
			
			//We'll try to invert the first note
//			Note tmpNote = this.notes[0];
			int currPitch = this.notes[0].getPitch();
			while(currPitch < this.notes[3].getPitch()) {
				currPitch += Harmonizer.SCALE;
			}
			this.notes[0] = this.notes[1];
			this.notes[1] = this.notes[2];
			this.notes[2] = this.notes[3];
			this.notes[3] = new Note(currPitch);
//			this.ci = computeChordInterval();
			this.intv = new Interval(this);
			return true;
		}
		
		/**
		 * This performs an inversion of the Chord regardless of how many notes it consists of.
		 * @return true IFF inversion successful
		 */
		public boolean invertAny() {
			if (this.notes[0] == null || this.notes[1] == null) return false;
			if (this.notes[2] == null) {
				int currPitch = this.notes[0].getPitch();
				while(currPitch < this.notes[1].getPitch()) {
					currPitch += Harmonizer.SCALE;
				}
				this.notes[0] = this.notes[1];
				this.notes[1] = new Note(currPitch);
//				this.ci = computeChordInterval();
				this.intv = new Interval(this);
				return true;
			} else if (this.notes[3] == null) {
				int currPitch = this.notes[0].getPitch();
				while(currPitch < this.notes[2].getPitch()) {
					currPitch += Harmonizer.SCALE;
				}
				this.notes[0] = this.notes[1];
				this.notes[1] = this.notes[2];
				this.notes[2] = new Note(currPitch);
//				this.ci = computeChordInterval();
				this.intv = new Interval(this);
				return true;
			} else {
				return invertUpAll();
			}
		}
		
		/**
		 * Tries to invert the first 3 notes downward.
		 * @return true IFF inversion successful
		 */
		public boolean invertDown() {
			//3rd note is replaced as many octaves down as possible until its pitch is 
			//below the first note, then if that pitch is legal (pitch value >= 0),
			//it is placed into 1st position, shifting the 1st and 2nd notes to the right.
			int currPitch = this.notes[2].getPitch();
			while (currPitch > this.notes[0].getPitch()) {
				currPitch -= Harmonizer.SCALE;
			}
			//Pitch cannot fall below 0.
			if (currPitch < 0) {
				return false;
			}
			
			//If we get this far, the note can be lowered
			this.notes[2] = this.notes[1];
			this.notes[1] = this.notes[0];
			this.notes[0] = new Note(currPitch);
//			this.ci = computeChordInterval();
			this.intv = new Interval(this);
			return true;
		}
		
		/**
		 * Getter for the array of Notes comprising this Chord
		 * @return the notes array associated with this Chord
		 */
		public Note[] getNotes() {
			return notes;
		}
		
		
		public Chord copy() {
			Note[] thisNoteArr = this.notes;
			Note bass = thisNoteArr[0];
			Note tenor = thisNoteArr[1];
			Note mezzo = thisNoteArr[2];
			Note melody = thisNoteArr[3];
			Chord c = new Chord(
					bass == null ? null : new Note(bass.getPitch(), bass.getDuration(), bass.getVolume()),
					tenor == null ? null: new Note(tenor.getPitch(), tenor.getDuration(), tenor.getVolume()),
					mezzo == null ? null: new Note(mezzo.getPitch(), mezzo.getDuration(), mezzo.getVolume()),
					melody == null? null: new Note(melody.getPitch(), melody.getDuration(), melody.getVolume())
					);
			return c;
		}

		public Interval getIntv() {
			return intv;
		}

		public Note getNote(int i) {
			if (i < 0 || i >= this.notes.length) throw new RuntimeException();
			return this.notes[i];
		}
		/**
		 * Getter for ChordInterval
		 * @return ChordInterval associated with this Chord
		 */
//		public ChordInterval getCi() {
//			return ci;
//		}
		
//		public Interval getBaseInterval() {
//			return ci.getBaseInterval();
//		}

//		@Override
//		public String toString() {
//			return Arrays.toString(notes) + " <<" + ci + ">>";
//		}
		
		@Override
		public String toString() {
			return Arrays.toString(notes) + " " + this.intv;
		}
	}
	//end class Chord