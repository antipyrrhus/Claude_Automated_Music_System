package main;
import java.util.*;

/**
 * This is an interval notation for a given (assumed to be 2-, 3- or 4-note) chord.
 * Accessed by ChordInterval class.
 * @author Yury Park
 */
public class Interval {
	private int[] arr;
//	private double emission; //probability that this interval shows up at all
	                         //Update: moved to Emission class
	
	/**
	 * Default constructor.
	 */
	public Interval() {
		this.arr = new int[]{};
//		this.emission = 0;
	}
	
	/**
	 * Overloaded constructor
	 * @param arr interval array
	 */
	public Interval(int[] arr) {
		this();
		this.arr = arr;
	}
	
	/**
	 * Overloaded constructor
	 * @param c a Chord object
	 */
	public Interval (Chord c) {
//		System.out.println("Chord: " + c);
		//Start by reducing the chord range to an octave
		Note[] notes = c.getNotes();
		int lowestPitch = notes[0].getPitch() % Harmonizer.SCALE;
		
		HashSet<Integer> pitchesHS = new HashSet<>(); //using HashSet to eliminate duplicate pitches
		for (int i = 0; i < notes.length; ++i) {
			if (notes[i] != null) {
				//getting lowest pitch using modulo and automatically eliminating "duplicates"
				pitchesHS.add(notes[i].getPitch() % Harmonizer.SCALE); 
			}
		}
//		System.out.println("pitchesHS:" + pitchesHS);
		ArrayList<Integer> pitchesAL = new ArrayList<>(pitchesHS); //copy to ArrayList
		
		//"Sorting" the pitches while maintaining the lowest pitch that we identified above
		for (int i = 0; i < pitchesAL.size(); ++i) {
			while (pitchesAL.get(i) < lowestPitch) {
				pitchesAL.set(i, pitchesAL.get(i) + Harmonizer.SCALE);
			}
		}
		Collections.sort(pitchesAL); //sort from lowest to highest pitch
//		System.out.println("pitchesAL after sort:" + pitchesAL);
		
		//if pitchesAL just contains one pitch (or less), just return null since this means 
		//the chord is just a unison, and thus interval notation is empty.
		if (pitchesAL.size() <= 1) this.arr = new int[] {};
//		this.emission = 0;
		
		//Get ready to represent this in interval notation
		this.arr = new int[pitchesAL.size() - 1];
		
		int tmpInterval = pitchesAL.get(0);
		for (int i = 1; i < pitchesAL.size(); ++i) {
			this.arr[i-1] = pitchesAL.get(i) - tmpInterval;
		}
	}
	
	/**
	 * Inverts the interval up
	 * @return new interval after one inversion
	 */
	public Interval invert() {
		//If interval is empty, nothing to invert.
		if (this.arr.length < 1) return new Interval();
		
		/* For example, an interval of {4,7,10} (e.g. chord C E G Bb assuming tonic note is C)
		 * will be inverted to {3,6,8} (E G Bb C for tonic note E, or alternatively,
		 * C D# F# G# for tonic C -- the tonic note doesn't really matter, unlike in Chord class)
		 * 
		 * The inversion occurs by taking the first element of the original interval 
		 * (in this case 4), and subtracting every element by that number to get {0 3 6},
		 * then removing 0 and adding 12 - 4 at the end to get {3,6,8}
		 */
		int n = this.arr.length;
		int tmpInterval = this.arr[0];
		
		int[] ret = new int[n];
		for (int i = 1; i < this.arr.length; ++i) {
			ret[i-1] = this.arr[i]-tmpInterval;
		}
		ret[n-1] = Harmonizer.SCALE - tmpInterval;
		return new Interval(ret);
	}
	
	
	/**
	 * @return the length of this.arr
	 */
	public int length() {
		return this.arr.length;
	}
	
	/**
	 * Checks that the given index is valid; if not, throws exception
	 * @param i index
	 */
	public void check(int i) {
		if (i < 0 || i >= this.arr.length) throw new RuntimeException("Index out of range");
	}
	
	/**
	 * Getter.
	 * @param i index
	 * @return the element in the i-th index
	 */
	public int get(int i) {
		check(i);
		return this.arr[i];
	}
	
	public void setArr(int[] arr) {
		this.arr = arr;
	}
	
	/**
	 * Setter. Sets the element at index i to the new value k
	 * @param i index
	 * @param k new value
	 */
	public void set(int i, int k) {
		check(i);
		this.arr[i] = k;
	}

	
	public int[] getArr() {
		return arr;
	}

//	public double getEmission() {
//		return emission;
//	}

//	public void setEmission(double emission) {
//		this.emission = emission;
//	}

	@Override
	protected Interval clone() {
		return new Interval(this.arr.clone());
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interval other = (Interval) obj;
		return (Arrays.equals(arr, other.arr));
	}

	@Override
	public String toString() {
		return Arrays.toString(this.arr);
	}	
}
