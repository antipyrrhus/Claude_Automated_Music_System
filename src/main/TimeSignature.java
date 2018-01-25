package main;

/**
 * Simple class to represent time signature of a musical piece.
 * @author Yury Park
 */
public class TimeSignature {
	int numer;
	int denom;
	
	public TimeSignature(int beats, int noteduration) {
		this.numer = beats;
		this.denom = noteduration;
	}
	
	public int getNumer() {
		return numer;
	}

	public void setNumer(int numer) {
		this.numer = numer;
	}

	public int getDenom() {
		return denom;
	}

	public void setDenom(int denom) {
		this.denom = denom;
	}
}
