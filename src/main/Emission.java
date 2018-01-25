package main;

import java.util.HashMap;

/**
 * A static class that keeps track of emission weights for a given Interval.
 * 
 * Accessed by other classes as needed.
 * @author Yury Park
 */
public class Emission {
	private static HashMap<Interval, Double> map = new HashMap<>();
	private static double multiplier = 1; //TODO have this setting be customizable
	
	public static void addEmission(Interval intv, double weight) {
		map.put(intv,  weight);
	}
	
	public static void addKey(Interval intv) {
		map.put(intv, null);
	}
	
	public static void setMultiplier(double d) {
		multiplier = d;
	}
	
	public static void print() {
		System.out.println(map);
	}
	
	public static boolean containsKey(Interval intv) {
		return map.containsKey(intv);
	}
	
	public static Double getWeight(Chord c) {
		return getWeight(c.getIntv());
	}
	
	public static double getMultiplier() {
		return multiplier;
	}

	public static Double getWeight(Interval intv) {
		return map.get(intv) == null ? 0d : map.get(intv);
	}
	
	/**
	 * Converts the weights to probabilities
	 * @param cumulativeWeight the total weight of all the intervals.
	 */
	public static void setProbabilities(double cumulativeWeight) {
		for (Interval intv : map.keySet()) {
			double currWeight = map.get(intv);
			System.out.println("currWeight before converting to probability: " + currWeight);
			addEmission(intv, currWeight / cumulativeWeight);
		}
	}
	
	/**
	 * This method should only run after setProbabilities() method has been executed.
	 * Otherwise it won't work.
	 * @return
	 */
	public static boolean probabilitiesAddtoOne() {
		double total_probability = 0.0;
		for (Interval intv : map.keySet()) {
			double currWeight = map.get(intv);
			if (currWeight != Double.NEGATIVE_INFINITY) {
				if (currWeight < 0) return false;
				total_probability += currWeight;
			}
		}
		return (Math.abs(total_probability - 1.0) < 0.0001);
	}
	
	/**
	 * This method should only run after setProbabilities() method has been executed.
	 * @return a random interval according to the given probability distribution.
	 */
	public static Interval getRandomIntv() {
		double pr = Math.random();
		double cumulativeProbability = 0.0;
		for (Interval intv : map.keySet()) {
			double currProb = map.get(intv);
			if (currProb != Double.NEGATIVE_INFINITY) {
				cumulativeProbability += currProb;
				if (pr <= cumulativeProbability) return intv;
			}
		}
		throw new RuntimeException("No interval has been chosen. This shouldn't happen.");
	}
}
