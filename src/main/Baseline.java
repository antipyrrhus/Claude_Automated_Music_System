package main;

/**
 * Simple static class for keeping track of Baseline preference weights for
 * unisons, diads, triads and tetrads.
 * @author Yury Park
 */
public class Baseline {
	public static final int UNISON = 0, DIAD = 1, TRIAD = 2, TETRAD = 4;
	private static double[] weights = new double[4];  //weights[i] = the weight assigned to the type of chord
	
	public static double getWeight(int i) {
		if (i < 0 || i >= weights.length) throw new RuntimeException();
		return weights[i];
	}
	public static void setWeight(int i, double weight) {
		if (i < 0 || i >= weights.length) throw new RuntimeException();
		weights[i] = weight;
	}
}
