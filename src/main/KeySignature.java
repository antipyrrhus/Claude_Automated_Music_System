package main;

import java.util.HashMap;

/**
 * Key Signature class. Contains information re: the tonic key and whether it's in major or minor.
 * Intended to be used as part of the scoring function in Harmonizer.java.
 * @author Yury Park
 *
 */
public class KeySignature {
	public static final int C=0, Cs=1, D=2, Ds=3, E=4, F=5, Fs=6, G=7, Gs=8, A=9, As=10, B=11;
	public static final String[] intToTonicArr = {"C", "Cs", "D", "Ds", "E", "F", "Fs", "G", "Gs", "A", "As", "B"};
	private static final HashMap<String,Integer> tonicToIntHM = new HashMap<String,Integer>() 
	{
		private static final long serialVersionUID = 1L;
		{	     
			for (int i = 0; i < intToTonicArr.length; ++i) {
				put(intToTonicArr[i], i);
			}
		}
	};
	public static final int MAJ = 0, HAR = 1, MEL = 2, NAT = 3;
	
	//tonic = tonic note of a given key signature. mode = major, har / mel / nat minor
	private int tonic, mode;
	
	private static double multiplier = 2;
	
	public KeySignature(int tonic, int mode) {
		this.tonic = tonic;
		this.mode = mode;
	}
	
	//overloaded constructor.
	public KeySignature(String tonicStr, String mode) {
		this(tonicToIntHM.get(tonicStr), mode.equals("MAJOR") ? 0 : mode.equals("HARMONIC MINOR") ? 1 : 
			mode.equals("MELODIC MINOR") ? 2 : 3);
	}
	
	public int getTonic() {
		return tonic;
	}

	public int getMode() {
		return mode;
	}

	public static double getMultiplier() {
		return multiplier;
	}
	
	public static KeySignature intToKeySignature (int k) {
		int key = k / 4;
		int mode = k % 4;
		return new KeySignature(key, mode);
	}
	
	public static int keySignatureToInt(KeySignature ks) {
		return ks.tonic * 4 + ks.mode;
	}
	
	/**
	 * Sets the mutiplier weight for how important this is
	 * @param multiplier
	 */
	public static void setMultiplier(double multiplier) {
		KeySignature.multiplier = multiplier;
	}

	@Override
	public String toString() {
		return String.format("%s %s", intToTonicArr[this.tonic], (this.mode == 0 ? "major" : 
												   this.mode == 1 ? "harmonic minor" :
												   this.mode == 2 ? "melodic minor" :
														  			"natural minor"));
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
		KeySignature other = (KeySignature) obj;
		if (mode != other.mode)
			return false;
		if (tonic % Harmonizer.SCALE != other.tonic % Harmonizer.SCALE)
			return false;
		return true;
	}
}
