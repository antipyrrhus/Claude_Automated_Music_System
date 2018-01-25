package main;

import java.util.*;

/**
 * A static class that keeps track of transition weights from a given Interval
 * to other Intervals. Represented as a map from an Interval to a set of maps,
 * with each map consisting of a neighboring Interval and an array containing
 * the offset (semitonic interval from the lowest note of current chord to 
 * that of the next chord) and weight value for that transition.
 * 
 * Accessed by other classes as needed.
 * @author Yury Park
 */
public class Transition {
	private static HashMap<Interval, HashMap<IntervalAndOffset, Double>> map = new HashMap<>();
	private static double multiplier = 1; //TODO have this setting be customizable
	
	private static class IntervalAndOffset {
		private Interval interval;
		private int offset;
		
		IntervalAndOffset(Interval intv, int offset) {
			this.interval = intv;
			this.offset = offset;
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
			IntervalAndOffset other = (IntervalAndOffset) obj;
			if (interval == null) {
				if (other.interval != null)
					return false;
			} else if (!interval.equals(other.interval))
				return false;
			if (Math.abs(offset - other.offset) % Harmonizer.SCALE != 0)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return this.interval.toString() + " " + this.offset;
		}
		
	}
	
	public static void addTransition(Interval intv1, Interval intv2, int offset, double weight) {
		HashMap<IntervalAndOffset, Double> hm = map.get(intv1);
		if (hm == null) {
			hm = new HashMap<>();
		}
		hm.put(new IntervalAndOffset(intv2, offset), weight);
		map.put(intv1, hm);
	}
	
	public static void addKey(Interval intv) {
		map.put(intv, null);
//		System.out.println(intv + " added to map.");
	}
	
	public static Double getWeight(Interval i1, Interval i2, int offset) {
		HashMap<IntervalAndOffset, Double> hm = map.get(i1);
		if (hm == null) return 0d;
		Double ret = hm.get(new IntervalAndOffset(i2, offset));
		return ret == null ? 0d : ret;
	}
	
	public static Double getWeight(Chord c1, Chord c2) {
		if (c1 == null || c2 == null) return 0d;
//		HashMap<IntervalAndOffset, Double> hm = map.get(c1.getBaseInterval());
		HashMap<IntervalAndOffset, Double> hm = map.get(c1.getIntv());
		if (hm == null) return 0d;
		int offset = c2.getNotes()[0].getPitch() - c1.getNotes()[0].getPitch();
//		System.out.println("offset = " + offset);
//		IntervalAndOffset io = new IntervalAndOffset(c2.getBaseInterval(), offset);
		IntervalAndOffset io = new IntervalAndOffset(c2.getIntv(), offset);
//		System.out.println("io = " + io);
		return hm.get(io) == null ? 0d : hm.get(io);
	}
	
	public static void setMultiplier(double d) {
		multiplier = d;
	}
	
	public static double getMultiplier() {
		return multiplier;
	}

	public static void print() {
		System.out.println(map);
	}
	
	public static boolean containsKey(Interval intv) {
		return map.containsKey(intv);
	}
	
	public static HashMap<IntervalAndOffset, Double> getValue(Interval intv) {
		return map.get(intv);
	}
}
