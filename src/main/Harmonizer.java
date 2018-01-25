package main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import convertmidi.MidiFile;  //Importing class that can create midi files

/**
 * @author Yury Park
 */
public class Harmonizer {
	
	//Key to int bindings (Update: moved to KeySignature class)
//	private final int C=0, Cs=1, D=2, Ds=3, E=4, F=5, Fs=6, G=7, Gs=8, A=9, As=10, B=11;
	
	//Major / Harmonic minor / Melodic minor / Natural minor to int bindings
	//(Update: moved to KeySignature class)
//	private final int MAJ = 0, HAR = 1, MEL = 2, NAT = 3;
	
	/* scales[i][j] = indicates a scale with key i as the tonic, and type j where
	 * j = 0 indicates major, j = 1 thru 3 indicates different types of minors
	 * and scales[i][j][k] = 1 if the k-th note is included in the given scale with tonic i
	 * and type j, 0 otherwise. */
	private int[][][] scales;
	private boolean debugOn;
	//One of the heuristics for scoring function -- don't want adj. harmonies to be too far apart
	private int max_dist_between_harmonies;
	
	//Another heuristic -- don't want harmony to be too far apart from melody
	private int max_dist_from_melody;
	
	private MidiFile mf; //helper class to output harmonies to midi
	public static final int SCALE = 12;	//using 12-tone scale
	
	/* These are the semitonic intervals involved in a major scale. 
	 * This can be transposed to any key signature. */
	private final int[] MAJ_INTV = {2, 2, 1, 2, 2, 2, 1};

	/* These are the semitonic intervals involved in an ascending melodic minor scale,
	 * and can be transposed to any key signature. */
	private final int[] MIN_MEL_INTV = {2, 1, 2, 2, 2, 2, 1};

	/* These are the semitonic intervals involved in a natural minor scale, 
	 * and can be transposed to any key signature. */
	private final int[] MIN_NAT_INTV = {2, 1, 2, 2, 1, 2, 2};

	/* These are the semitonic intervals involved in a harmonic minor scale (notice the
	 * augmented 2nd near the end), and can be transposed to any key signature. */
	private final int[] MIN_HAR_INTV = {2, 1, 2, 2, 1, 3, 1};
	
	/* noteAppeared[i] = true IFF note i % Harmonizer.SCALE appeared in a chord.
	 * Planning to use this to help detect changes in key signature during
	 * a chord sequence. UPDATE just use local variable */
//	private boolean[] noteAppeared;
	
	//array that holds scores for each chord in a chord sequence. Used as part of scoring function.
//	private double[] scoreArr;
	
	/* Multipler weight for penalty distance score */
	private double distMultiplier;		//TODO allow user to set this?
	
	//TODO decide which weight to give greater precedence to (probably transition? Or user can decide?)
	//TODO bias harmony towards tonic for beginning / ending measures?
	//TODO consider harmonizing every measure, or every strong downbeat, or every note....
	//     (to do this, we need time signature information and first beat of the note in the first measure)
	//     and when we skip notes, do we consider those skipped melodies in helping decide the next harmony?
	//TODO show MC is ergodic (aperiodic, strongly connected)
	//TODO have a GUI to emulate a music score for user to draw in notes, rests, beat, etc?
	//TODO consider not changing the lowest note of the harmony very often
	
	/* TODO how the scoring function might work:
	 * Consider having the scoring function be composed of a combination of separate scores for
	 * TYPE (e.g. unison, triad...), DISTANCE, EMISSION, TRANSITION, and MISC 
	 * (e.g. last harmony should be a resolution chord) and assign a multiplier weight
	 * to each type of score depending on user preference? 
	 * 
	 * 1) If a chord is logistically illegal (e.g. base note above tenor, or 
	 * base note for next harmony above tenor's current position, etc.), score is -infinity
	 * 2) If a chord is illegal due to the interval's emission or transition being -infinity,
	 * then score is -infinity
	 * 3) if a chord is legal:
	 *    - begin at score of 0
	 *    - add baseline preference score depending on whether the chord is a unison, diad...
	 *    - add emission weight for the chord interval
	 *    - subtract penalty score based on the distance of this harmony compared to prev harmony
	 *      and the max. preferred distance limit (preset)
	 *    - add transition weight based on prev -> curr chord
	 *    - TODO consider subtract penalty score (preset) if the next melody would result in a dissonance of
	 *      a semitonic interval (1) with one of the notes in the current harmony, unless
	 *      the next melody is included in the current harmony (or perhaps the next melody
	 *      is also a semitone away from the current melody?), or unless there is a 
	 *      positive transition weight between the current harmony and a possible next harmony
	 *      that contains the next melody.
	 *    - If a chord pertains to the last melody, then the chord should be a resolution chord
	 *      such as I major, or i minor...and its inversions. So add a heavy penalty if not.
	 *      (or maybe have the user set preferred resolution chord like C Eb G B?)
	 *    - Consider biasing aganist a major chord in a key turning into a minor chord
	 *      in that same key, i.e. C major into a C minor (and any inversions thereof),
	 *      unless the melody justifies it. Add penalty (preset) as MISC or just add this 
	 *      to settings as a transition penalty?
	 *    - TODO consider taking rhythm of the melody into consideration, and when skipping
	 *      some melodies, how does that change the harmonization strategy? Possible answer:
	 *      For a given subset of a sequence of melodies which we need to harmonize with
	 *      a single chord, try to form a chord with those melodies first and eliminate
	 *      "least important, e.g. neighboring etc" notes from the melody and use that as baseline?
	 *    - TODO as part of the scoring function, consider the no. of times key signature changes
	 *      and try to minimize that? Might use my algorithm from Project 2 for keeping a list of
	 *      possible key signatures for a chord sequence where the winner is the ones that
	 *      minimize total no. of key signature changes. Perhaps only use major scale and 
	 *      harmonic minor scale (and do away with natural / melodic minor) for simplicity
	 *    
	 */
	/* 
	 * markov chain / simulated annealing?

Have some type of scoring function (how to make this?) for harmony given current melody, previous few harmonic sequences, and the subsequent few melodic sequences?

- valid / invalid degrees / chord sequences
- key signatures / changes therein?
- For more ideas, read: http://nurykabe.com/dump/programming/processing/harmonyGenerator/

- Markov Chain:
  Input: B = {b1, b2, ..., bn} = sequence of melodies
  State space = Omega = {H1, H2, ..., Hm}, where any given state Hi (for 1 <= i <= m) = {h1, h2, ..., hn} = sequence of corresponding chords for each melody,
                and where a given chord h_j (for 1 <= j <= n) = {d1, d2, d3} = {bass note, tenor note, and mezzosoprono note}
  We're assuming that we're working with a total of 127 notes. Thus, |Omega| = m = (127^3)^n = 127^(3n), since each chord h_j has 127^3 choices,
  and there are a total of n chords.

- Have a Markov Chain as follows: From any given state Ht \in Omega, do the following:
  1. Choose chord h_j at random, where 1 <= j <= n.
  2. Choose one of the 3 positions of the chord, d_k, at random (where 1 <= k <= 3). That is, choose bass, tenor, or mezzosoprano
  3. For the chosen position d_k, choose a note value r at random (where 1 <= r <= 127)
  4. Set d_k = r with probability p (how is p derived? discuss later....). If d_k is set to r, then set Xt+1 with h_j as modified above.
  5. Otherwise, set Xt+1 = Xt.

  Mixing time? Well if transition probability p were the same for every note value r, then this would be bound by something like coupon collector?
  (can prove this via path coupling -- choose the same h_j and the same d_k and the same r)

	 * */
	
	//keySignature = {i, j} where 0<=i<=11 (C thru B, semitonic) and 0<=j<=1 (major/minor)
	//this represents the best estimate of the current key signature of a melody - harmony combo
	//(while keeping in mind that key signatures can change during a piece, albeit temporarily)
	//TODO consider keeping a "best estimate" of the key signature of the melody phrases
	//as we go along and tailoring the harmony accordingly
	private int[] keySignature;  //TODO maybe eliminate this?
	
	
	
	
	/**
	 * Default constructor.
	 */
	public Harmonizer(String filename) {
		this.mf = new MidiFile();
		this.debugOn = true;
		this.distMultiplier = 0.1; //TODO allow user to edit htis?
		this.max_dist_between_harmonies = Harmonizer.SCALE / 2; //TODO testing
		this.max_dist_from_melody = Harmonizer.SCALE + Harmonizer.SCALE/2; //TODO testing
		Emission.setMultiplier(1);  //TODO testing
		Transition.setMultiplier(3);//TODO testing
		KeySignature.setMultiplier(5); //TODO testing

		/* Build all possible scales for every key (major + 3 types of minors) */
		this.scales = new int[Harmonizer.SCALE][4][Harmonizer.SCALE];
		for (int i = 0; i < Harmonizer.SCALE; ++i) {
			int[] maj_scale = this.buildScale(i, this.MAJ_INTV);
			int[] min_har_scale = this.buildScale(i, this.MIN_HAR_INTV);
			int[] min_mel_scale = this.buildScale(i, this.MIN_MEL_INTV);
			int[] min_nat_scale = this.buildScale(i, this.MIN_NAT_INTV);
			scales[i] = new int[][]{maj_scale, min_har_scale, min_mel_scale, min_nat_scale};
		}
		if (debugOn) {
			System.out.println("Testing C minor natural scale: " + 
								Arrays.toString(scales[KeySignature.C][KeySignature.NAT]));
			System.out.println("Testing A# minor harmonic scale: " + 
								Arrays.toString(scales[KeySignature.As][KeySignature.HAR]));
		}
		//Initialize all intervals
		
		//unison
		Transition.addKey(new Interval(new int[] {}));
		
		//diad intervals
		for (int i = 1; i < Harmonizer.SCALE; ++i) {
			Interval tmpInterval = new Interval(new int[] {i});
			Transition.addKey(tmpInterval); //add the key only to the map, with null as the value
			Emission.addKey(tmpInterval);
			//add triads
			for (int j = i+1; j < Harmonizer.SCALE; ++j) {
				tmpInterval = new Interval(new int[] {i,j});
				Transition.addKey(tmpInterval);
				Emission.addKey(tmpInterval);
				//add tetrads
				for (int k = j+1; k < Harmonizer.SCALE; ++k) {
					tmpInterval = new Interval(new int[] {i,j,k});
					Transition.addKey(tmpInterval);
					Emission.addKey(tmpInterval);
				}
			}
		}
		//end for
		
		Transition.print(); //prints the HashMap in Transition class
		Emission.print();
		
		//Checking some methods in Transition class
		Interval testInterval = new Interval(new int[] {2, 5, 7});
		Interval testInterval2 = new Interval(new int[] {3, 1, 2});
//		System.out.println("Testing out some methods for Transition class...");
//		System.out.println(Transition.containsKey(testInterval)); //expected true
//		System.out.println(Transition.getValue(testInterval)); //expected null (since key was just initialized)
//		System.out.println(Transition.containsKey(testInterval2));//expected false
//		System.out.println(Transition.getValue(testInterval2));//expected null (since key doesn't exist)
		
		//Read in from default.dat settings file and populate  the Emission / Transition weights.
		//and baseline preference for unisons, diads, triads, or tetrads
		File file = new File(filename);
		double cumulativeEmissionWeight = 0.0;
		try  {
			BufferedReader br = new BufferedReader(new FileReader(file));
			try {
				String line = br.readLine().trim();

				while (line != null) {
					if (line.startsWith("Baseline")) {
						String[] lineArr = line.split(" ");
						for (int i = 1; i < lineArr.length; ++i) {
							Baseline.setWeight(i-1, Double.parseDouble(lineArr[i]));
							if (debugOn)
								System.out.println("Baseline weights: " + Baseline.getWeight(i-1));
						}
						
					} else if (line.startsWith("Emission")) {
						String arr = line.substring(line.indexOf("["), line.indexOf("]")+1);
						Double weight = Double.parseDouble(line.substring(line.indexOf("]")+1, line.indexOf("/")).trim());
//						System.out.println(arr);
//						System.out.println(weight);
						if (arr.equals("[]")) { //empty array, unison
							Emission.addEmission(new Interval(new int[] {}), weight);
						} else {
							Interval intv = new Interval(this.getArrElements(arr));
							Emission.addEmission(intv, weight);
						}
						if (weight != Double.NEGATIVE_INFINITY) {
							cumulativeEmissionWeight += weight;
						}
					} else if (line.startsWith("Transition")) {
						int index_1stbracketend = line.indexOf("]") + 1;
						int index_2ndbracketstart = line.indexOf("[", index_1stbracketend);
						int index_2ndbracketend = line.indexOf("]", index_2ndbracketstart) + 1;
						String arr1 = line.substring(line.indexOf("["), index_1stbracketend);
						String arr2 = line.substring(index_2ndbracketstart, index_2ndbracketend);
						String offset_weight = line.substring(index_2ndbracketend, line.indexOf("//")).trim();
						String[] offset_weight_arr = offset_weight.split(" ");
						int offset = Integer.parseInt(offset_weight_arr[0]);
						double weight = Double.parseDouble(offset_weight_arr[1]);
						Interval i1 = new Interval(new int[] {});
						Interval i2 = new Interval(new int[] {});
						if (!arr1.equals("[]")) {
							i1.setArr(this.getArrElements(arr1));
						} 
						
						if (!arr2.equals("[]")) {
							i2.setArr(this.getArrElements(arr2));
						}
						if (debugOn)
							System.out.println("arr1: " + arr1 + " arr2: " + arr2 + " offset and weight: " + offset + " " + weight);
						
						Transition.addTransition(i1, i2, offset, weight);
					}
					line = br.readLine();
				}
				//end while
				
				/* Now that we read from the settings file, we want to re-compute
				 * the PROBABILITY of each emission. */
				Emission.setProbabilities(cumulativeEmissionWeight);
				System.out.println("Printing list of emissions...");
				Emission.print(); //testing
				if (!Emission.probabilitiesAddtoOne()) {
					br.close();
					throw new RuntimeException();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			br.close();
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException ioe2) {
			ioe2.printStackTrace();
		}
		//end try / catch
		
		//testing Transition & Emission weights
		if (debugOn) {
			System.out.println("Printing out all transitions and emissions...");
			Transition.print(); //prints the HashMap in Transition class
			Emission.print();
		}
		
		//testing out transition / emission weight lookup with some chords (comment out when done)
		Chord c1 = new Chord(new Note(60), new Note(62), new Note(65), new Note(69));
		Chord c2 = new Chord(new Note(59), new Note(62), new Note(65), new Note(67));
		Chord c3 = new Chord(new Note(60), new Note(63), new Note(66), new Note(68));
		System.out.println("Testing Transition / Emission weights...");
		System.out.println(Transition.getWeight(c1, c2));
		System.out.println(Transition.getWeight(c1, c3));
		System.out.println(Emission.getWeight(c1.getIntv()));
		
		c1 = new Chord(new Note(48), new Note(51), new Note(54), new Note(57));
		c2 = new Chord(new Note(49+12), new Note(53+12), new Note(58+12), null);
		c3 = new Chord(new Note(50-24), new Note(55), new Note(58), null);
		Chord c4 = new Chord(new Note(49-24), new Note(54), new Note(58+12), null);
		Chord c5 = new Chord(new Note(58-12), new Note(61), new Note(65), null);
//		System.out.println("Testing more Transition weights...");
//		System.out.println(Transition.getWeight(c1, c2));
//		System.out.println(Transition.getWeight(c1, c3));
//		System.out.println(Transition.getWeight(c1, c4));
//		System.out.println(Transition.getWeight(c1, c5));
	}
	//public Harmonizer
	
	/**
	 * Overloaded constructor.
	 * @param maxdist Max. allowed hamming distance between two successive harmony chords.
	 *                Any harmony whose distance with the prev harmony exceeds this value is illegal. 
	 */
	public Harmonizer(String filename, boolean debugOn) {
		this(filename);
		this.debugOn = debugOn;
	}
	
	/**
	 * Overloaded constructor.
	 * @param filename
	 * @param debugOn
	 * @param distMultiplier
	 * @param max_dist_from_melody
	 * @param max_dist_between_harmonies
	 * @param emissionMult
	 * @param transMult
	 * @param ksMult
	 */
	public Harmonizer(String filename, boolean debugOn, double distMultiplier,
			int max_dist_from_melody, int max_dist_between_harmonies,
			double emissionMult, double transMult, double ksMult) {
		this(filename, debugOn);
		if (distMultiplier > 0)
			this.distMultiplier = distMultiplier;
		if (max_dist_from_melody > 0)
			this.max_dist_from_melody = max_dist_from_melody;
		if (max_dist_between_harmonies > 0)
			this.max_dist_between_harmonies = max_dist_between_harmonies;
		Emission.setMultiplier(emissionMult);
		Transition.setMultiplier(transMult);
		KeySignature.setMultiplier(ksMult);
	}
	
	/**
	 * Helper method to be invoked by Constructor.
	 * @param s String pertaining to settings file pertaining to an array
	 * @return an int array (indicating an Interval notation) as described by the given String s
	 */
	private int[] getArrElements(String s) {
		s = s.substring(1, s.length() - 1);
		String[] values = s.split(",");
		int[] valuesInt = new int[values.length];
		for (int i = 0; i < valuesInt.length; ++i) {
			valuesInt[i] = Integer.parseInt(values[i].trim());
		}
		return valuesInt;
	}
	
	private double sum(double[] arr) {
		double ret = 0;
		for (double d : arr) {
			ret += d;
		}
		return ret;
	}
	
	/**
	 * Given a sequence of melodies, return a sequence of chords.
	 * TODO For now, harmonize every note.
	 * Later, consider harmonizing according to rhythm, e.g. only harmonize every other beat,
	 * or every downbeat...or some other customized rhythm
	 * 
	 * @param notesequence Note array, indicating sequence of melodies
	 * @return sequence of chords to accompany the melody sequence
	 */
	public ChordSequence harmonize(Note[] notesequence, int max_iter, int c) {
//		this.scoreArr = new double[notesequence.length];
		
		/* Try breaking up the notesequence into phrases (chunks) where
		 * each chunk constitutes one or two "best guesses" at key signature,
		 * until such time where the key signature must change, then repeat the process.
		 * Try to minimize the no. of total key signature changes.
		 * 
		 * Below custom method uses dynamic programming + some other tricks to return
		 * a single "optimal" sequence of key signatures.
		 */
//		ArrayList<KeySignature> guessedKS_AL = this.computeKS(notesequence); //TODO try greedy
		ArrayList<Double> scoresAL = new ArrayList<>();
		ArrayList<KeySignature> guessedKS_AL = this.computeKS_Greedy_inclRest(notesequence);
		
			try {
				Thread.sleep(3000); //just for debugging re: computing key signature
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		//Begin with a chord sequence consisting of a random valid interval for each chord. 
		int lowestPitch = searchLowest(notesequence).getPitch();  //custom method
		
		//Make up chord sequence of chords equaling the no. of melodies, consisting of unisons
		ChordSequence cs = new ChordSequence(notesequence.length);
		for (int i = 0; i < cs.length(); ++i) {
			//get current melody and its duration & volume
			Note currNote = notesequence[i];
			int currPitch = currNote.getPitch();
			int duration = currNote.getDuration();
			int vol = currNote.getVolume();
			
			if (currPitch < 0) { //if null note, set the chord itself to null
				cs.setChord(i,  null);
			} else {
				/* Set current chord to be a unison: */
				cs.setChord(i, new Chord(new Note(lowestPitch, duration, vol),
							 			 new Note(lowestPitch, duration, vol),
							 			 new Note(lowestPitch, duration, vol),
							 			 new Note(currPitch, duration, vol))
						   );
			}
			
		}
		//end for i
		if (!isLegal(cs)) throw new RuntimeException("Sanity check failed. Initial chordsequence is apparently invalid.");
		
		
		/* TODO In case the chordsequence contains rests (null chords),
		 * let's set up a new chordsequence that contains notes only (no rests),
		 * then run MCMC and then fill in the rests in the final chordsequence.
		 * Doing this preserves some of the crucial transitions between adjacent non-rest notes
		 * for purposes of scoring. */
		ArrayList<Chord> tmpCSAL = new ArrayList<>();
		for (int i = 0; i < cs.length(); ++i) {
			if (cs.getChord(i) == null) continue;
			Chord ch_copy = cs.getChord(i).copy();
			tmpCSAL.add(ch_copy);
		}
		Chord[] cArr_norest = new Chord[tmpCSAL.size()];
		tmpCSAL.toArray(cArr_norest);
		ChordSequence cs_norest = new ChordSequence(cArr_norest);
		
		double currScore = computeScoreWithoutKSChanges(cs_norest, guessedKS_AL);
		double bestScore = currScore;
		if (debugOn)
			System.out.println("currScore: " + currScore);
		
		
		//Run MCMC with Metropolis-Hastings. Change the no. of iterations once done testing
		/* UPDATE: don't worry about distance functions and the like.
		 * Just get the best QUALITY harmonies. Must change the scoring function as well.
		 * Also, change an entire chord, not just one note. */
		for (int i = 0; i < max_iter; ++i) {
			double T = c / Math.sqrt(i);
			System.out.printf("===============================ITERATION %s===============================\n"
					+ "c = %s, T = %s", i, c, T);
			Random r = new Random();
			int rand_chord_index = r.nextInt(cs_norest.length());
			if (debugOn)
				System.out.printf("INDEX %s CHOSEN\n", rand_chord_index);
			Chord rand_chord = cs_norest.getChord(rand_chord_index);
			if (debugOn) System.out.println("random chord chosen: " + rand_chord);
			if (rand_chord == null || this.isRest(rand_chord)) {
				i--;
				continue;
			}
			
			/* Old method changed a single note at a time. Commented out, as we now
			 * change entire chord at a time */
//			int rand_note_index = r.nextInt(3);
//			if (debugOn) System.out.println("random index and note chosen: " + rand_note_index + " " + rand_chord.getNote(rand_note_index));
////			Note rand_note = rand_chord.getNote(rand_note_index);
//			int old_pitch = rand_chord.getNote(rand_note_index).getPitch();
//			int new_pitch = -1;
//			if (rand_note_index == 0) { //bass note. Choose pitch between 0 and tenor pitch inclusive
//				new_pitch = r.nextInt(rand_chord.getNote(rand_note_index+1).getPitch() + 1);
//			} else {  //tenor or alto note. Choose pitch between lower note's and upper note's pitch inclusive
//				int lower_note_pitch = rand_chord.getNote(rand_note_index - 1).getPitch();
//				int higher_note_pitch = rand_chord.getNote(rand_note_index + 1).getPitch();
//				int legal_bound =  higher_note_pitch - lower_note_pitch + 1;
//				new_pitch = r.nextInt(legal_bound) + lower_note_pitch;
//			}
//			
//			if (new_pitch == -1) throw new RuntimeException(""
//					+ "New pitch should be nonnegative at this point. Something's wrong.");
//			
//			if (debugOn) System.out.println("old and new pitch: " + old_pitch + " " + new_pitch);
			
			/* Choose 3 random integers between 0 and 11 (since there are 12 pitch classes)
			 * and re-assign them to chord in nondecreasing order. */
			int melody_curr_pitch = rand_chord.getNote(3).getPitch();
			int pitch1 = r.nextInt(Harmonizer.SCALE) + 48;
			int pitch2 = r.nextInt(Harmonizer.SCALE) + 48;
			int pitch3 = r.nextInt(Harmonizer.SCALE) + 48;
			
			/* UPDATE TODO: choose a random valid interval from emissions */
			Interval newIntv = Emission.getRandomIntv();
			System.out.println("newIntv: " + newIntv);
			Chord newChord = null;
			if (newIntv.length() == 0) {
				newChord = new Chord(newIntv, rand_chord.getNote(3).copy(), rand_chord.getNote(3).getDuration());
			} else {
				newChord = new Chord(newIntv, rand_chord.getNote(3).getPitch() - newIntv.get(newIntv.length() - 1), rand_chord.getNote(3).getDuration());
			}
			
//			int[] newChord_pitchArr = {pitch1, pitch2, pitch3};
//			Arrays.sort(newChord_pitchArr);
//			
//			int[] oldChord_pitchArr = {
//					rand_chord.getNote(0).getPitch(),
//					rand_chord.getNote(1).getPitch(),
//					rand_chord.getNote(2).getPitch(),
//					};
			
			//Mutate the chord now
			cs_norest.setChord(rand_chord_index, newChord);
//			rand_chord.changeNotePitch(0, newChord_pitchArr[0]);
//			rand_chord.changeNotePitch(1, newChord_pitchArr[1]);
//			rand_chord.changeNotePitch(2, newChord_pitchArr[2]);
//			rand_chord.changeNotePitch(rand_note_index, new_pitch);
			if (debugOn) System.out.println("Chord has been changed to: " + rand_chord);
			
			currScore = computeScoreWithoutKSChanges(cs_norest, guessedKS_AL);
			if (debugOn) System.out.println("currScore: " + currScore);
			if (currScore == Double.NEGATIVE_INFINITY) {  //illegal
				if (bestScore == Double.NEGATIVE_INFINITY) {
					i--;
					scoresAL.add(currScore);
					continue;
				}
//				cs = backupCS;
//				rand_chord.changeNotePitch(rand_note_index, old_pitch);
//				rand_chord.changeNotePitch(0, oldChord_pitchArr[0]);
//				rand_chord.changeNotePitch(1, oldChord_pitchArr[1]);
//				rand_chord.changeNotePitch(2, oldChord_pitchArr[2]);
				cs_norest.setChord(rand_chord_index, rand_chord);
				if (debugOn) System.out.println("Chord has been restored to: " + rand_chord);
				scoresAL.add(bestScore);
//				System.out.println("Chordsequence restored to: " + cs);
				continue;
			}
			if (currScore > bestScore) {
				bestScore = currScore;
				scoresAL.add(bestScore);
			} else {
				double delta_distance = bestScore - currScore;
				if (debugOn) System.out.printf("bestScore, currScore: %s, %s, iteration: %s, T: %s\n", bestScore, currScore, i, T);
				if (debugOn) System.out.println("Probability of moving to a worse state: " + Math.exp(-1 * delta_distance / T));
				if (r.nextDouble() < Math.exp(-1 * delta_distance / T)) {
					bestScore = currScore;
					scoresAL.add(bestScore);
				} else {
//					rand_chord.changeNotePitch(rand_note_index, old_pitch);
//					rand_chord.changeNotePitch(0, oldChord_pitchArr[0]);
//					rand_chord.changeNotePitch(1, oldChord_pitchArr[1]);
//					rand_chord.changeNotePitch(2, oldChord_pitchArr[2]);
					cs_norest.setChord(rand_chord_index, rand_chord);
					if (debugOn) System.out.println("Chord has been restored to: " + rand_chord);
					scoresAL.add(bestScore);
//					cs = backupCS;
//					System.out.println("Chordsequence restored to: " + cs);
				}
				
			}
		}
		//end i
		
		if (debugOn) System.out.println("Final ChordSequence (no rest): " + cs_norest); //testing
//		this.computeKeySignatureChanges(cs);
		
		/* Now add rest chords back in */
		ArrayList<Chord> cAL = new ArrayList<>(Arrays.asList(cs_norest.getSeq()));
		for (int i = 0; i < cs.length(); ++i) {
			if (cs.getChord(i) == null) {
				cAL.add(i, null);
			}
		}
		
		Chord[] cArr = new Chord[cAL.size()];
		cAL.toArray(cArr);
		ChordSequence final_cs = new ChordSequence(cArr);
		System.out.println("Final ChordSequence (rests added): " + final_cs); //testing
		System.out.println("Final score: " + bestScore);
		
		/* Transform null chords (pertaining to "rest" melodies) to chords
		 * consisting of null notes, that preserves information re: the duration of
		 * each rest melody */
		for (int i = 0; i < final_cs.length(); ++i) {
			Chord chord = final_cs.getChord(i);
			if (chord == null) {
				int currNoteDuration = notesequence[i].getDuration();
				chord = new Chord(new Note(-1, currNoteDuration),
								  new Note(-1, currNoteDuration),
								  new Note(-1, currNoteDuration),
								  new Note(-1, currNoteDuration)
								  );
				final_cs.setChord(i, chord);
			}
		}
		writeToFile(scoresAL);
		return final_cs;
	}
	
	private void writeToFile(ArrayList<Double> scoresAL) {
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
				"SCORES" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".txt"
				)));
			for (Double score : scoresAL) {
				pw.println(score);
			}
			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
//	private void computeNewState(ChordSequence cs) {
//		Chord prev = null;
//		for (int i = 0; i < cs.length(); ++i) {
//			Interval rand_intv = Emission.getRandomIntv();
//			System.out.println("rand_intv: " + rand_intv);
//			Chord curr = cs.getChord(i);
//			if (curr == null) {
//				prev = null;
//				continue;
//			}
//			int melody_pitch = curr.getNote(3).getPitch();
//			int[] newChordPitchesArr = this.getChordPitchArr(melody_pitch, rand_intv);
//			mutateChord(curr, newChordPitchesArr);
//			Chord nextChord = i == cs.length() - 1 ? null : cs.getChord(i+1);
//			Note nextMelody = nextChord == null ? null : nextChord.getNote(3);
//			if (!this.isLegal(curr, prev, nextMelody)) {
//				i--;
//			} else {
//				prev = curr;
//			}
//		}
//		
//	}
	
//	private int[] getChordPitchArr(int melody_pitch, Interval intv) {
//		int[] newChordPitchesArr = {melody_pitch,melody_pitch,melody_pitch};
//		if (intv.length() == 0) return newChordPitchesArr;
//		int index = newChordPitchesArr.length - 1;
//		int index_pitch = melody_pitch;
//		for (int j = intv.length() - 1; j >= 0; --j) {
//			int curr = intv.get(j);
//			int prev = (j == 0 ? 0 : intv.get(j-1));
//			newChordPitchesArr[index] = index_pitch - (curr-prev);
//			index_pitch = newChordPitchesArr[index];
//			index--;
//		}
//		while (index >= 0) {
//			newChordPitchesArr[index] = newChordPitchesArr[index+1];
//			index--;
//		}
//		return newChordPitchesArr;
//	}
	
	/**
	 * Changes the bass, tenor and alto notes of a given chord to the pitch values contained in the arr,
	 * then randomly brings the note(s) down by an octave or more and/or swaps alto and tenor
	 * The bass note is not swapped, but may be dropped by an octave or more.
	 * @param ch
	 * @param arr
	 */
//	private void mutateChord(Chord ch, int[] arr) {
//		/* Check that the the lowest pitch value is nonnegative */
//		if (arr[0] < 0) return;
//		
//		for (int i = 0; i < arr.length; ++i) {
//			ch.changeNotePitch(i, arr[i]);
//		}
//		
//		/* Now with some probability bring down the note(s) by an octave or more as long
//		 * as legal */
//		for (int i = 0; i < 3; ++i) {
//			if (new Random().nextInt(2) == 0) continue;
//			Note n = ch.getNote(i);
//			int numLegalOctaves = 0;
//			if (i == 0) {
//				numLegalOctaves = n.getPitch() / Harmonizer.SCALE;	
//			} else {
//				numLegalOctaves = (n.getPitch() - ch.getNote(i-1).getPitch()) / Harmonizer.SCALE;
//			}
//			int numOctavesToDrop = new Random().nextInt(numLegalOctaves + 1);
//			n.setPitch(n.getPitch() - numOctavesToDrop*Harmonizer.SCALE);
//		}
//		//end for
//		
//		/* Finally, swap alto and tenor with probability 50%, if legal 
//		 * by either lowering the alto or upping the tenor or both */
//		if (new Random().nextInt(2) == 0) {
//			Note bass = ch.getNote(0);
//			Note tenor = ch.getNote(1);
//			Note alto = ch.getNote(2);
//			Note melody = ch.getNote(3);
//			int numLegalOctavesUpTenor = (melody.getPitch() - tenor.getPitch()) / Harmonizer.SCALE;
//			int numLegalOctavesDownAlto = (alto.getPitch() - bass.getPitch()) / Harmonizer.SCALE;
//			int numOctavesToUpTenor = new Random().nextInt(numLegalOctavesUpTenor + 1);
//			int numOctavesToDownAlto = new Random().nextInt(numLegalOctavesDownAlto + 1);
//			int tenorNewPitch = tenor.getPitch() + numOctavesToUpTenor * Harmonizer.SCALE;
//			int altoNewPitch = alto.getPitch() - numOctavesToDownAlto * Harmonizer.SCALE;
//			alto.setPitch(altoNewPitch);
//			tenor.setPitch(tenorNewPitch);
//			if (tenorNewPitch > altoNewPitch) {
//				ch.setNote(1, alto);
//				ch.setNote(2, tenor);
//			}
//		}
//	}
	
	
	/**
	   * Method: buildScale
	   * @param i pertains to the tonic of the scale, where 0 = C, 1 = C#, ..., 11 = B.
	   * @param key semitonic intervals pertaining to either major, minor melodic, minor harmonic, or minor natural scale.
	   * @return an array of notes consisting of a single octave,
	   *         pertaining to a scale consistent with the given tonic and key signature / scale.
	   */
	int[] buildScale(int i, int[] key) {
		//i.e. major key = {2, 2, 1, 2, 2, 2, 1}
		int[] ret = new int[Harmonizer.SCALE];

		//Start at i and, for each note i that appears in the scale, set ret[i] = 1 for that note.
		int i_copy = i;
		int k = 0;
		while (i_copy < Harmonizer.SCALE) {
			ret[i_copy] = 1;
			i_copy += key[k++];
			if (k >= key.length) k = 0;
		}

		//Now go backward from i and fill in the beginning parts of the scale for completeness
		i_copy = i;
		k = key.length - 1;
		while (i_copy >= 0) {
			ret[i_copy] = 1;
			i_copy -= key[k--];
			if (k < 0) k = key.length - 1;
		}
		return ret;
	}
	
	/**
	 * Method: computeDiff
	 * @param scale a major or minor scale in some given key as tonic
	 * @param noteAppeared boolean array for notes 0 thru 11 (C thru B) where
	 *        noteAppeared[i] = true IFF note i appeared
	 * @return the number of notes in noteAppeared that do NOT pertain to the given scale.
	 */
	
	int computeDiff(int[] scale, boolean[] noteAppeared) {
		int ret = 0;
		for (int i = 0; i < noteAppeared.length; ++i) {
			if (noteAppeared[i] == false) continue;
			if (scale[i] == 0) ret += 1; //if note i appeared but is NOT in the scale, increment
		}
		return ret;
	}
	
//	private int computeKeySignatureChanges(ChordSequence cs) {
//		int keySignatureChanges = 0;
//		int modeChangeInSameTonic = 0;  //TODO consider implementing this later
//		int typesOfChanges = 0;			//TODO consider implementing this later
////		ArrayList<HashSet<KeySignature>> ksContendersAL = new ArrayList<>();
//		
//		/* Count up the no. of key signature changes throughout this chord sequence.
//		 * Consider a few different ways of adjusting the score in this regard:
//		 * 1) The fewer the key signature changes, the better. (For now implement this one
//		 *    as it's easiest to implement. TODO consider the other ways listed below)
//		 * 2) The fewer changes in major / minor mode in the SAME key signature (tonic note), 
//		 *    the better. (challenge: we'll usually have several contenders for key signature
//		 *    and mode. So most of the time a change won't solely involve major -> minor
//		 *    (or vice versa) in the same tonic. 
//		 * 3) The fewer TYPES of key signature changes, the better. For example,
//		 *    change from C -> G -> C -> G -> C is better than C -> G -> E -> F# -> C
//		 *    even though both have the same total no. of changes, the latter one goes through
//		 *    more key signatures than the former. So the former is preferred.
//		 *    (challenge: same problem as in 2) above)
//		 */
//		/* Each iteration (except for i = 0) while going thru each Chord in the ChordSequence,
//		 * we'll first gather the possible set of key signatures for that Chord,
//		 * then update currValidSetOfKS = intersection of itself
//		 * and ksHS (see below loop). If the intersection results in an empty set, it means
//		 * that the key signature form the prev chord to current chord MUST have changed.
//		 * If there is such a change, we'll update currValidSetOfKS = ksHS and go on. */
//		HashSet<KeySignature> currValidSetOfKS = new HashSet<KeySignature>();
//		
//		/* Each element of the below AL will be a set of KeySignature objects
//		 * indicating the set of possible (most likely) key signatures for
//		 * a particular Chord in the given ChordSequence. */
//		
//		for (int i = 0; i < cs.length(); ++i) {
//			HashSet<KeySignature> ksHS = computeKS(cs.getChord(i));
////			ksContendersAL.add(ksHS);
//			
//			
//			if (i == 0) currValidSetOfKS = ksHS;
//			else {
////				System.out.println("Intersecting two sets : " + currValidSetOfKS + " " + ksHS);
//				/* currValidSetOfKS = currValidSetOfKS /intersect ksHS */
//				currValidSetOfKS.retainAll(ksHS);
//				if (currValidSetOfKS.isEmpty()) {
//					System.out.println("KEY SIGNATURE CHANGE");
//					currValidSetOfKS = ksHS;
//					//Update counter
//					keySignatureChanges++;
//				}
//			}
//			//testing
////			System.out.printf("List of Key signatures for Chord %s is: %s\n", cs.getChord(i), ksHS);
////			System.out.printf("currValidSetOfKS: %s\n", currValidSetOfKS);
//		}
////		System.out.println("ksContendersAL: " + ksContendersAL);
//		return keySignatureChanges;
//	}
	
	/**
	 * @return list of possible key signatures for given note
	 */
	private HashSet<KeySignature> computeKS(Note note) {
		if (note.getPitch() < 0) // null note
			return null;
		boolean[] noteAppeared = new boolean[Harmonizer.SCALE];
		
		noteAppeared[note.getPitch() % Harmonizer.SCALE] = true;
		return getContenders(noteAppeared, this.scales);
	}
	
	/**
	 * Given a notesequence, compute the maximally efficient list of
	 * key signatures such that the no. of changes in key signatures is minimized
	 * In the event of a tie, we'll give preference to the same key signature (if one exists)
	 * that both begins and ends the notesequence, because in general it makes sense for
	 * a given musical piece to begin and end in the same key signature.
	 * TODO also consider an alternate metric: instead of just counting the total no. of changes
	 * in key signature, consider how many DIFFERENT key signatures / scales the melodic sequence
	 * goes through, and choose the one that undergoes the fewest number of DISTINCT
	 * key signatures / scales. 
	 * For example, C major -> F melodic minor -> E major -> A# major is "worse" than
	 * C major -> E major -> C major -> E major -> C major although the former has 4 changes
	 * to the latter's 5, because the latter has fewer distinct number of key signatures. 
	 * @param notesequence
	 * @return list of key signatures pertaining to each index of the given notesequence.
	 */
//	private ArrayList<KeySignature> computeKS(Note[] notesequence) {
//		ArrayList<HashSet<KeySignature>> listOfContenderKS = new ArrayList<>();
//		//Start by adding all possible contenders for each note into arraylist
//		for (int i = 0; i < notesequence.length; ++i) {
//			listOfContenderKS.add(computeKS(notesequence[i]));
//		}
//		
//		//Choose a single key signature per index such that the number of changes
//		//in key signatures is minimized.
//		/* Try dynamic programming with recursive substructure as follows:
//		 * D[i][j] = the minimum total no. of unavoidable key changes that results from notesequence[i]
//		 * to notesequence[-1] (where notesequence[-1] denotes the last note in the sequence)
//		 * assuming that key signature / scale #j is assigned to notesequence[i].
//		 * if #j cannot be legally assigned to notesequence[i], then set D[i][j] = infinity
//		 * And we work BACKWARDS, i.e. from i = notesequence.length - 1 down to i = 0.
//		 * 
//		 * Also, let C[i] = the minimum total no. of unavoidable key changes regardless of
//		 * whatever key signature that is assigned.
//		 * */
//		int totalKSCombo = 48; //there are 48 possible keysignature / scale combinations
//		int[][] D = new int[listOfContenderKS.size()][totalKSCombo];
//		int[] C = new int[listOfContenderKS.size()];
//		Arrays.fill(C, Integer.MAX_VALUE);
//		for (int i = 0; i < D.length; ++i) {
//			Arrays.fill(D[i], Integer.MAX_VALUE);  //initialize D[i][0...47] to infinity for all i
//		}
//		
//		/* We may or may not end up needing this, but let's also try keeping
//		 * a set of key signatures to accompany the array above. */ 
//		HashSet<KeySignature>[] C_HS = (HashSet<KeySignature>[]) new HashSet[listOfContenderKS.size()];
//		
//		/* We now initialize the last element of the array D[n-1][.] and will work backwards.
//		 * Specifically, for all j, set D[n-1][j] to 0 IFF j can be
//		 * legally assigned to notesequence[n-1], where n = notesequence.length, 
//		 * otherwise D[n-1][j] remains at infinity.
//		 */
//		
//		C[listOfContenderKS.size() - 1] = 0;  //for the last element, no. of key changes is automatically 0.
//
//		for (int j = 0; j < totalKSCombo; ++j) {
//			KeySignature ks = KeySignature.intToKeySignature(j); //static custom method
//			HashSet<KeySignature> hs_lastindex = listOfContenderKS.get(listOfContenderKS.size() - 1);
//			if (hs_lastindex.contains(ks)) {
//				D[listOfContenderKS.size() - 1][j] = 0;
//				if (C_HS[listOfContenderKS.size() - 1] == null) {
//					C_HS[listOfContenderKS.size() - 1] = new HashSet<>();
//				}
//				C_HS[listOfContenderKS.size() - 1].add(ks);
//			}
//		}
////		System.out.printf("C_HS[%s] = %s\n", listOfContenderKS.size() - 1, C_HS[listOfContenderKS.size() - 1]);
//		
//		/* Now use the recursive substructure to fill in the array backwards */
//		for (int i = listOfContenderKS.size() - 2; i >= 0; --i) {
//			HashSet<KeySignature> hs1 = listOfContenderKS.get(i);
//			HashSet<KeySignature> hs2 = listOfContenderKS.get(i+1);
//			
//			/* We'll go thru every key signature in hs1 and see how many key changes
//			 * are necessary for each key signature. Then take the minimum */
//			int min_sofar = Integer.MAX_VALUE;  	//to keep track of D[i][k]
//			for (int k = 0; k < totalKSCombo; ++k) {
//				KeySignature ks = KeySignature.intToKeySignature(k);
//				if (!hs1.contains(ks)) {
//					continue;
//				}
//				
//				if (hs2.contains(ks)) {
//					D[i][k] = D[i+1][k];
////					System.out.printf("hs2 contains the key signature %s. No need for key change.\n", ks);
////					System.out.printf("D[%s][%s] set to %s\n", i, k, D[i][k]);
//				} else {
//					D[i][k] = (C[i+1] == Integer.MAX_VALUE ? Integer.MAX_VALUE : 1 + C[i+1]);
////					System.out.printf("hs2 does not contain the key signature %s. Key change!\n", ks);
////					System.out.printf("D[%s][%s] set to %s\n", i, k, D[i][k]);
//				}
//				
//				/* Now go thru all D[i][k] for all k and set as C[i] = min(D[i][k]) for all k */
//				if (D[i][k] < min_sofar) {
////					System.out.printf("Minimum value of D[%s][%s] found: %s, which is smaller than min_sofar of %s.\n",
////							i, k, D[i][k], min_sofar);
//					min_sofar = D[i][k];
//					C[i] = D[i][k];
////					System.out.printf("C[%s] is set to %s.\n", i, C[i]);
//					C_HS[i] = new HashSet<>();
//					C_HS[i].add(KeySignature.intToKeySignature(k));
////					System.out.printf("C_HS[%s] is set to %s.\n", i, C_HS[i]);
//					
//				} else if (D[i][k] == min_sofar) {
////					System.out.printf("Minimum value of D[%s][%s] found: %s, which is equal to min_sofar of %s.\n",
////							i, k, D[i][k], min_sofar);
//					if (C_HS[i] == null) C_HS[i] = new HashSet<>();
//					C_HS[i].add(KeySignature.intToKeySignature(k));
////					System.out.printf("C_HS[%s] is set to %s.\n", i, C_HS[i]);
//				}
//			}
//			//end for k
//		}
//		//end for i
//		
//		/* Testing */
////		for (int i = 0; i < notesequence.length; ++i) {
////			System.out.println(notesequence[i] + " " + C[i] + " " + C_HS[i]);
////		}
//		
//		/* It's time to choose an arraylist consisting of a SINGLE "optimal" key signature
//		 * for each note in the melody sequence. Go thru the C[i] array one more time,
//		 * and take note of C[0] plus each other element C[i] where C[i] < C[i-1].
//		 * Basically, save every element C[i] which represents the FIRST instance of each
//		 * increment in the no. of key signatures.
//		 * */
//		ArrayList<HashSet<KeySignature>> al = new ArrayList<>();
//		al.add(C_HS[0]);
//		for (int i = 0; i < C_HS.length-1; ++i) {
//			if (C[i] > C[i+1]) {
//				al.add(C_HS[i+1]);
//			}
//		}
////		System.out.println(al);
//		
//		/* Do an intersection betweeen al[i] and al[i+2] to shrink down the sets.
//		 * This is a quick way to reduce the number of key signatures in each set.
//		 * (there's no point to intersecting al[i] and al[i+1] because they are
//		 * necessarily disjoint. 
//		 * Also account for the time where the intersection results in empty set! */
//		for (int i = 0; i < al.size(); ++i) {
//			for (int j = i+1; j < al.size(); ++j) {
//				HashSet<KeySignature> tmpHS = new HashSet<>(al.get(i));
//				tmpHS.retainAll(al.get(j));
//				if (tmpHS.isEmpty()) continue;
//				
//				al.set(i, tmpHS);
//				al.set(j, tmpHS);
//			}
//		}
////		for (int i = 0; i < al.size(); ++i) {
////			if (i + 2 >= al.size()) continue;
////			HashSet<KeySignature> tmpHS = new HashSet<>(al.get(i));
////			tmpHS.retainAll(al.get(i+2));
////			if (tmpHS.isEmpty()) continue;
////			
////			al.set(i, tmpHS);
////			al.set(i+2, tmpHS);
////		}
//		System.out.println("After intersections: " + al);
//		
//		for (int i = 0; i < notesequence.length; ++i) {
//			System.out.println(notesequence[i] + " " + C[i] + " " + C_HS[i]);
//		}
//		
//		/* If we still have multiple keysignature elements, 
//		 * choose one at random and stick with it */
//		for (int i = 0; i < al.size(); ++i) {
//			HashSet<KeySignature> tmpHS = al.get(i);
//			System.out.println("tmpHS before mutation: " + tmpHS);
//			int size = tmpHS.size();
//			int index = new Random().nextInt(size);
//			int k = 0;
//			for (KeySignature ks : tmpHS) {
//				if (k == index) {
//					tmpHS.clear();
//					tmpHS.add(ks);
//					System.out.println("tmpHS after mutation: " + tmpHS);
//					al.set(i, tmpHS);
//					break;
//					
//				}
//				k++;
//			}
//			//end for
//		}
//		
//		/* Now make every element of al consist of single key signature each */
//		C_HS[0] = al.get(0);
//		int index = 0;
//		for (int i = 1; i < C_HS.length; ++i) {
//			if (C[i] < C[i-1]) {
//				C_HS[i] = al.get(++index);
//			}
//			else {
//				C_HS[i] = al.get(index);
//			}
//		}
////		System.out.println("C_HS is now: " + Arrays.toString(C_HS));
//		
//		/* Put the final keysignature sequence into an arraylist to return */
//		ArrayList<KeySignature> ret = new ArrayList<>();
//		for (int i = 0; i < C_HS.length; ++i) {
//			for (KeySignature ks : C_HS[i]) {
//				ret.add(ks);
//			}
//		}
//		
//		if (ret.size() != notesequence.length) throw new RuntimeException();
//		System.out.println("Returning arraylist: " + ret);
//		return ret; 		//placeholder
//	}
	
	/**
	 * Greedy version of computeKS. This one allows for rests in the notesequence.
	 * @param notesequence
	 * @return
	 */
	private ArrayList<KeySignature> computeKS_Greedy_inclRest(Note[] notesequence) {
		ArrayList<Note> al_norest = new ArrayList<Note>();
		for (int i = 0; i < notesequence.length; ++i) {
			Note currNote = notesequence[i];
			if (currNote.getPitch() < 0) {
				continue;
			}
			al_norest.add(currNote);
		}
		//end for i
		Note[] noteseq_norest = al_norest.toArray(new Note[al_norest.size()]);
		ArrayList<KeySignature> ks_AL_norest = this.computeKS_Greedy(noteseq_norest);
		
		ArrayList<KeySignature> ret = new ArrayList<>();
		int index = 0;
		for (int i = 0; i < notesequence.length; ++i) {
			if (notesequence[i].getPitch() < 0) {
				ret.add(null);
				continue;
			}
			ret.add(ks_AL_norest.get(index++));
		}
		return ret;
	}
	
	/**
	 * Another approach to computing key signature sequence of a melody,
	 * this time trying greedy algorithm
	 * @param notesequence
	 * @return
	 */
	private ArrayList<KeySignature> computeKS_Greedy (Note[] notesequence) {
//		int totalKSCombo = 48; //there are 48 possible keysignature / scale combinations
//		int[][] D = new int[notesequence.length][totalKSCombo];
//		int[] C = new int[notesequence.length];
//		Arrays.fill(C, Integer.MAX_VALUE);
//		for (int i = 0; i < D.length; ++i) {
//			Arrays.fill(D[i], Integer.MAX_VALUE);  //initialize D[i][0...47] to infinity for all i
//		}
		
		HashSet<KeySignature>[] arr = new HashSet[notesequence.length];
		
		/* Going forward, get set of key signatures for each melody and keep intersecting them
		 * until we have empty set */
		int prevChangeIndex = 0;
		for (int i = 0; i < notesequence.length-1; ++i) {
			
			HashSet<KeySignature> ksHS_currMelody = this.computeKS(notesequence[i]);
			HashSet<KeySignature> ksHS_nextMelody = this.computeKS(notesequence[i+1]);
			if (i == 0) {
				arr[0] = ksHS_currMelody;
			}
			/* Do intersection with next melody */
			HashSet<KeySignature> intersectHS = new HashSet<>(arr[i]);
			intersectHS.retainAll(ksHS_nextMelody);
			if (intersectHS.isEmpty()) { //unavoidable key change
				arr[i+1] = ksHS_nextMelody;
//				for (int j = i-1; j >= prevChangeIndex; --j) {
//					arr[j] = arr[i];
//				}
//				prevChangeIndex = i+1;
			} else {
				arr[i+1] = intersectHS;
			}
			
//			System.out.println(ksHS_currMelody + " added to arraylist");
		}
		//end for i
		
		ArrayList<HashSet<KeySignature>> al = new ArrayList<>();
		
		for (int i = arr.length - 1; i > 0; --i) {
			
			HashSet<KeySignature> currKSHS = arr[i];
			HashSet<KeySignature> prevKSHS = arr[i-1];
			if (i == arr.length - 1) {
				al.add(0, currKSHS);
			}
			HashSet<KeySignature> intersectHS = new HashSet<>(prevKSHS);
			intersectHS.retainAll(currKSHS);
			if (intersectHS.isEmpty()) {
				al.add(0, prevKSHS);
				continue;
			}
			arr[i-1] = arr[i];
		}
		if (debugOn) System.out.println("ArrayList: " + al);
		
		if (debugOn) System.out.println("Array (Greedy): ");
		for (HashSet<KeySignature> hs : arr) {
			System.out.println(hs + " ");
		}
		
		/* Now do intersection between every pair in al to pare down the key signatures further */
		for (int i = 0; i < al.size(); ++i) {
			for (int j = i+1; j < al.size(); ++j) {
				HashSet<KeySignature> tmpHS = new HashSet<>(al.get(i));
				tmpHS.retainAll(al.get(j));
				if (tmpHS.isEmpty()) continue;
				
				al.set(i, tmpHS);
				al.set(j, tmpHS);
			}
		}
		if (debugOn) System.out.println("After intersections: " + al);
		
		/* If we still have multiple keysignature elements, 
		 * choose one at random and stick with it */
		outerLoop:
		for (int i = al.size() - 1; i >= 0; --i) {
			HashSet<KeySignature> tmpHS = al.get(i);
			if (debugOn) System.out.println("tmpHS before mutation: " + tmpHS);
			
			if (i == al.size() - 1) {
				/* If last note ends in tonic or perfect fifth of a key signature
				 * that is in the hashset, then choose that one. Otherwise, choose at random. */
				Note lastMelody = notesequence[notesequence.length - 1];
				int lastMelody_tonic = lastMelody.getPitch() % Harmonizer.SCALE;
				int lastMelody_fifth = lastMelody_tonic + 7;
				for (KeySignature ks: tmpHS) {
					if (ks.getTonic() == lastMelody_tonic || ks.getTonic() + 7 == lastMelody_fifth) {
						tmpHS.clear();
						tmpHS.add(ks);
						al.set(i, tmpHS);
						continue outerLoop;
					}
				}
			}
			int size = tmpHS.size();
			int index = new Random().nextInt(size);
			int k = 0;
			for (KeySignature ks : tmpHS) {
				if (k == index) {
					tmpHS.clear();
					tmpHS.add(ks);
					if (debugOn) System.out.println("tmpHS after mutation: " + tmpHS);
					al.set(i, tmpHS);
					break;
					
				}
				k++;
			}
			//end for
		}
		if (debugOn) System.out.println("After reducing key signatures to 1 each:\n" + al);
		
		ArrayList<KeySignature> ret = new ArrayList<>();
		int index = 0;
		for (int i = 0; i < arr.length; ++i) {
			HashSet<KeySignature> hsks = new HashSet<>(arr[i]);
			hsks.retainAll(al.get(index));
			if (hsks.isEmpty()) {
				index++;
				for (KeySignature ks : al.get(index)) ret.add(ks);
			} else {
				for (KeySignature ks: al.get(index)) ret.add(ks);
			}
		}
		if (debugOn) System.out.println("finished computing KS for notesequence: " + Arrays.toString(notesequence));
		System.out.println("computeKS greedy: returning ret :\n" + ret);
		if (ret.size() != notesequence.length) throw new RuntimeException();
		return ret;
	}
	
//	private int computeDiff(int[] scale1, int[] scale2) {
//		int diff = 0;
//		for (int i = 0; i < scale1.length; ++i) {
//			if (scale1[i] != scale2[i]) diff++;
//		}
//		return diff;
//	}
	
	/**
	 * Computes the possible key signatures (along with major / minor modes) for the given Chord.
	 * @param c Chord
	 * @return set of possible key signatures and modes
	 */
	private HashSet<KeySignature> computeKS(Chord c) {
		boolean[] noteAppeared = new boolean[Harmonizer.SCALE];
		
		//Begin by getting all the pitches in the given chord modulo 12
		//(for simplicity, we assume the lowest possible pitch)
		for (Note note : c.getNotes()) {
			noteAppeared[note.getPitch() % Harmonizer.SCALE] = true;
		}
		
		return getContenders(noteAppeared, this.scales);
	}
	
	private HashSet<KeySignature> getContenders(boolean[] noteAppeared, int[][][] scales) {
		/* Go thru every key signature, major and minor and compare with counter. 
		 * We want the one with the least total difference.
		 * diff[i][j] = the no. of notes in counter that do NOT match the scale with 
		 * note i as the tonic, and where j (0 <= j <= 3) pertains to major or minor scales
		 * in accordance with KeySignature class (MAJ = 0, HAR = 1, MEL = 2, NAT = 3).
		 * i <= 12 because there are 12 semitones in an octave, so this covers every possible tonic.
		 */
		int[][] diff = new int[Harmonizer.SCALE][4];
	    int min_diff = Integer.MAX_VALUE;  //used to keep track of the minimum difference
	    
	    //these will keep track of the key signatures / scales that are most likely to be the best fit.
	    HashSet<KeySignature> contenders = new HashSet<KeySignature>(); 
	    for (int i = 0; i < Harmonizer.SCALE; ++i) {
			for (int j = 0; j < diff[i].length; ++j) {
				diff[i][j] = computeDiff(scales[i][j], noteAppeared);
	
//				System.out.printf("diff[%s][%s] = %s\n", i, j, diff[i][j]);
				if (min_diff > diff[i][j]) {   //If there is a new unique winner so far, clear the list of contenders.
					min_diff = diff[i][j];
					contenders.clear();
					contenders.add(new KeySignature(i, j));
				} else if (min_diff == diff[i][j]) {  //If there is another contender that ties the current min_diff, add to the list of contenders
					contenders.add(new KeySignature(i,j));
				}
			}
			//end for j
	    }
	    //end for i
//	    System.out.println("contenders: " + contenders);
		return contenders;
	}
	
//	private double computeScoreWKeySignatureChanges(ChordSequence cs, ArrayList<KeySignature> guessedKS_AL) {
//		int keySignatureChanges = this.computeKeySignatureChanges(cs);
//		return computeScoreWithoutKSChanges(cs, guessedKS_AL) - KeySignature.getMultiplier() * keySignatureChanges;  
//	}
	
	private double computeScoreWithoutKSChanges(ChordSequence cs, ArrayList<KeySignature> guessedKS_AL) {
		double[] scoreArr = computeScore(cs, guessedKS_AL);
		return sum(scoreArr);
	}
	
	/**
	 * Computes score for a given sequence of chords
	 * @param cs ChordSequence object
	 * @return array of fitness scores for each chord in cs 
	 */
	private double[] computeScore(ChordSequence cs, ArrayList<KeySignature> guessedKS_AL) {
		double[] score = new double[cs.length()];
		
		for (int i = 0; i < cs.length(); ++i) {
			score[i] = computeScore(cs, i, guessedKS_AL.get(i));
		}
		return score;
	}
	
	private boolean isRest(Chord c) {
		for (Note note : c.getNotes()) {
			if (note.getPitch() < 0) {
				return true;
			}
		}
		return false;
	}
		
	/**
	 * 
	 * @param cs
	 * @param i
	 * @return
	 */
	private double computeScore(ChordSequence cs, int i, KeySignature melody_ks) {
		Chord prevC = (i == 0 ? null : cs.getChord(i-1));
		Chord currC = cs.getChord(i);
		if (currC == null || isRest(currC)) return 0;
		Chord nextC = (i == cs.length() - 1 ? null : cs.getChord(i+1));
		Note prevMelody = (prevC == null ? null : prevC.getNote(3));
		Note currMelody = currC.getNote(3);
		Note nextMelody = (nextC == null ? null : nextC.getNote(3));
		if (!this.isLegal(currC, prevC, nextMelody)) {
			if (debugOn) System.out.println("ILLEGAL prevC or currC: " + prevC + " " + currC);
			return Double.NEGATIVE_INFINITY;
		}
		
		/* Emission score 
		 * TODO since we now pick interval randomly according to their emission probabilities, can we dispense with this? */
		Double emission = Emission.getWeight(currC);
//		System.out.println("Emission score: " + emission);
		if (emission == null) emission = 0.0;
		
		/* Check to see if current chord is compatible with the key signature of melody */
		boolean isCompatibleKeySignature = true;
		HashSet<KeySignature> ksHSforCurrC = this.computeKS(currC);
		double penalty_incompatibleKS = 0;
		if (!ksHSforCurrC.contains(melody_ks)) {
			isCompatibleKeySignature = false;
		}
		
		Double transition = 0.0;
		
		double distScore = 0;
		if (prevC != null) {
			/* Transition score */
			transition = Transition.getWeight(prevC, currC);
			if (transition == null) transition = 0.0;
			
			/* If the transition itself is good but it does not resolve to
			 * the correct key signature, set it to 0. */
			if (transition > 0.0 && !isCompatibleKeySignature) {
				transition = 0.0;
			}
			
			/* Distance score between harmonies (adj. harmonies shouldn't be too far apart) 
			 * Penalty is applied otherwise such that distScore becomes negative */
			int dist = prevC.computeDistance(currC);
			if (dist > this.max_dist_between_harmonies) {
				distScore = (this.max_dist_between_harmonies - dist) * this.distMultiplier;
			}
		}
		
		/* Distance score for current harmony & current melody (harmony shouldn't be too
		 * far from the melody) */
		int dist = currC.getNote(3).getPitch() - currC.getNote(0).getPitch();
		if (dist > this.max_dist_from_melody) {
			int penalty = this.max_dist_from_melody - dist;  //penalty is negative
			
			distScore = (distScore + penalty) * this.distMultiplier;
		}

		//Add baseline weight depending on whether current chord is unison, diad, triad or tetrad
		//This can be set in SettingsGUI
		Interval currIntv = currC.getIntv();
		double baseWeight = Baseline.getWeight(currIntv.length());
//		System.out.println("baseWeight for " + currIntv + ": " + baseWeight);
		
		/* If the chord is not compatible with the current melody's key signature,
		 * give a penalty */
		if (!isCompatibleKeySignature) {
			penalty_incompatibleKS = -1 * KeySignature.getMultiplier();
		}
//		HashSet<KeySignature> ksHSforCurrC = this.computeKS(currC);
//		double penalty_incompatibleKS = 0;
//		if (!ksHSforCurrC.contains(melody_ks)) {
//			penalty_incompatibleKS = -1 * KeySignature.getMultiplier();
//		}
		
		
		/* Total up the score and return 
		 * UPDATE: forget distance penalty */
		double score = baseWeight + Emission.getMultiplier() * emission +
				   Transition.getMultiplier() * transition +
				   penalty_incompatibleKS;
		if (debugOn) System.out.printf("Score for chord %s at index %s = %s\n", currC, i, score);
		return score;
	}
	
	/**
	 * Returns the lowest note out of the Note array
	 * @param notesequence Note array
	 * @return the note with lowest pitch
	 */
	private Note searchLowest(Note[] notesequence) {
		Note ret = notesequence[0];
		if (ret.getPitch() < 0) ret = null;
		for (int i = 1; i < notesequence.length; ++i) {
			if (ret == null) {
				ret = notesequence[i];
				continue;
			}
			if (notesequence[i].getPitch() >= 0 && notesequence[i].getPitch() < ret.getPitch()) {
				ret = notesequence[i];
			}
		}
		return ret;
	}
	
	/**
	 * Given a current Chord and prev Chord
	 * determines whether the current Chord is legal.
	 * Assumes that both Chords consist of exactly 4 Notes each, including the melody
	 * @param currC
	 * @param prevC
	 * @return true IFF current Chord is legal
	 */
	public boolean isLegal(Chord currC, Chord prevC, Note nextMelody) {
		if (currC == null) return true;
		//Return false if emission weight is -infinity
		Double emissionWeight = Emission.getWeight(currC);
		if (emissionWeight == Double.NEGATIVE_INFINITY) {
			if (debugOn) System.out.println("emissionWeight for currC is negative infinity");
			return false;
		}
		
		//Return false if transition weight from prev chord to current chord is -infinity
		Double transWeight = Transition.getWeight(prevC, currC);
		if (transWeight == Double.NEGATIVE_INFINITY) {
			if (debugOn) System.out.println("transitionWeight is -infinity for prevC and currC");
			return false;
		}
		
		//Make sure the chord consists of 4 notes, including the melody, and that
		//they are in increasing (or at least nondecreasing) pitch order
//		Note[] currNotes = currC.getNotes();
//		Note[] prevNotes = (prevC == null? null : prevC.getNotes());
//		for (int i = 0; i < currNotes.length - 1; ++i) {
//			if (currNotes[i] == null || currNotes[i+1] == null) {
//				if (debugOn) System.out.println("chord doesn't consist of 4 notes");
//				return false;
//			}
//			if (currNotes[i].getPitch() > currNotes[i+1].getPitch()) {
//				if (debugOn) System.out.println("chord is not in nondecreasing order");
//				return false;
//			}
//		}
		
		//Next check hamming distance between current and previous chord.
		//If the distance exceeds max. allowed distance return false
		//Recall that the computeDistance method only checks the first 3 notes of the chord,
		//ignoring the melody, since the melody is given to us so it doesn't make sense to
		//take the adjacent melody distance into account.
		//UPDATE: placed this under scoring function instead of outlawing it
//		if (prevC != null) {
//			int hammingDist = currC.computeDistance(prevC);
//			int hammingDist_Equivalent = prevC.computeDistance(currC); //sanity check
//			if (hammingDist != hammingDist_Equivalent) {
//				throw new RuntimeException("Something wrong with computeDistance() method in Chord class");
//			}
//			if (hammingDist > this.max_dist_between_harmonies) return false;
//		}
		
		//1. Check curr note's bass isn't higher up than prev note's tenor
		//2. Check curr note's tenor is neither higher up than prev note's mezzo
		//nor is lower than prev note's bass
		//3. Check curr note's mezzo is neither higher up than prev note's soprano
		//nor is lower than prev note's tenor
		//4. Check curr note's soprano is not lower than prev note's mezzo
		//5. Check curr note's alto is not higher than next melody
//		if (prevNotes != null) {
//			for (int i = 0; i < currNotes.length; ++i) {
//				if (i > 0 && i < currNotes.length - 1) {
//					if (currNotes[i].getPitch() < prevNotes[i-1].getPitch()) {
//						if (debugOn) System.out.println("Current note's part is lower than prev note's lower part" + currNotes[i] + " " + prevNotes[i-1]);
//						return false;
//					}
//					if (currNotes[i].getPitch() > prevNotes[i+1].getPitch()) {
//						if (debugOn) System.out.println("Current note's part is higher than prev note's higher part" + currNotes[i] + " " + prevNotes[i+1]);
//						return false;
//					}
//				} else if (i > 0) {
//					if (currNotes[i].getPitch() < prevNotes[i-1].getPitch()) {
//						if (debugOn) System.out.println("Current note's part is lower than prev note's lower part" + currNotes[i] + " " + prevNotes[i-1]);
//						return false;
//					}
//				} else if (i < currNotes.length - 1) {
//					if (currNotes[i].getPitch() > prevNotes[i+1].getPitch()) {
//						if (debugOn) System.out.println("Current note's part is higher than prev note's higher part" + currNotes[i] + " " + prevNotes[i+1]);
//						return false;
//					}
//				}
//			}
//			//end for
//		}
//		if (nextMelody != null) {
//			if (currNotes[2].getPitch() > nextMelody.getPitch()) {
//				if (debugOn) System.out.println("Current note's alto is higher than next melody");
//				return false;
//			}
//		}
		return true; //placeholder
	}
	
	/**
	 * Given a ChordSequence, checks whether the entire sequence is legal
	 * @param seq ChordSequence
	 * @return true IFF legal
	 */
	public boolean isLegal(ChordSequence seq) {
		Chord prevC = null;
		for (int i = 0;  i < seq.length(); ++i) {
			if (!isLegal(seq.getChord(i),
					     prevC,
					     i == seq.length() - 1 ? null: seq.getChord(i+1) == null ? null : seq.getChord(i+1).getNote(3))) {
//					     )) {
				return false;
			}
			prevC = seq.getChord(i);
		}
		return true; //placeholder
	}

	public int getMax_dist_between_harmonies() {
		return max_dist_between_harmonies;
	}

	public void setMax_dist_between_harmonies(int max_dist_between_harmonies) {
		this.max_dist_between_harmonies = max_dist_between_harmonies;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Harmonizer harmonizer = new Harmonizer("default.dat");
		harmonizer.debugOn = false;
		
		//Checking the two constructors for class Note are both valid for pitch 0 thru 87
		//(since there are 88 keys on a keyboard...why not try them all)
		
		System.out.println("Checking two constructors for Note class to see if they're valid...");
		for (int i = 0; i < 88; ++i) {
			//constructor using pitch value
			Note n1 = new Note(i);
			String nStr1 = n1.toString();
			
			//constructor using the String e.g. "A#2" indicating that the note is A#, 2nd octave
			Note n2 = new Note(nStr1.substring(0, nStr1.indexOf('(')));
			String nStr2 = n2.toString();
			
			//Note n1 and n2 are supposed to be equal. Are they? Check. If not, throw exception
			System.out.println(nStr1 + " " + nStr2);
			if (!nStr1.equals(nStr2)) throw new RuntimeException();
		}
		System.out.println("The two constructors for Note class are valid! Moving on...\n");
		
		
		//Creating a sequence of melody for testing
//		Note n1 = new Note("C 7", MidiFile.SEMIQUAVER*3);
//		Note n2 = new Note("D 7", MidiFile.SEMIQUAVER);
//		Note n3 = new Note("E 7", MidiFile.QUAVER);
//		Note n4 = new Note("D 7", MidiFile.QUAVER);
//		Note n5 = new Note("F 7", MidiFile.QUAVER);
//		Note n6 = new Note("E 7", MidiFile.QUAVER);
//		Note n7 = new Note("D 7", MidiFile.SEMIQUAVER);
//		Note n8 = new Note("B 6", MidiFile.SEMIQUAVER);
//		Note n9 = new Note("C 7", MidiFile.QUAVER);
//		
//		Note[] seq = {n1, n2, n3, n4, n5, n6, n7, n8, n9};
		Note[] seq = new Note[]{
				new Note(72, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(-1, 8),
				new Note(77, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(71, 8),
				new Note(72, 8),
				new Note(-1, 8),
				new Note(81, 8),
				new Note(-1, 8),
				new Note(79, 8),
				new Note(-1, 8),
				new Note(77, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(72, 8),
				new Note(79, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(71, 8),
				new Note(67, 8),
				new Note(-1, 8),
				new Note(77, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(71, 8),
				new Note(67, 8),
				new Note(-1, 8),
				new Note(79, 8),
				new Note(-1, 8),
				new Note(77, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(78, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(78, 8),
				new Note(79, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(84, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(83, 8),
				new Note(83, 8),
				new Note(81, 8),
				new Note(79, 8),
				new Note(-1, 8),
				new Note(81, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(79, 8),
				new Note(79, 8),
				new Note(77, 8),
				new Note(76, 8),
				new Note(-1, 8),
				new Note(74, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(77, 8),
				new Note(79, 8),
				new Note(81, 8),
				new Note(77, 8),
				new Note(74, 8),
				new Note(72, 8),
				new Note(-1, 8),
				new Note(76, 8),
				new Note(74, 8),
				new Note(72, 8),
				};
		seq = new Note[]{
				new Note(65, 8),
				new Note(72, 8),
				new Note(72, 8),
				new Note(67, 8),
				new Note(65, 8),
				new Note(65, 8),
				new Note(60, 8),
				new Note(65, 8),
				new Note(65, 8),
				new Note(67, 8),
				new Note(65, 8),
				new Note(-1, 8),
				new Note(65, 8),
				new Note(72, 8),
				new Note(72, 8),
				new Note(67, 8),
				new Note(65, 8),
				new Note(65, 8),
				new Note(60, 8),
				new Note(65, 8),
				new Note(65, 8),
				new Note(67, 8),
				new Note(65, 8),
				new Note(-1, 8),
				new Note(65, 8),
				new Note(72, 8),
				new Note(70, 8),
				new Note(65, 8),
				new Note(67, 8),
				new Note(63, 8),
				new Note(63, 8),
				new Note(70, 8),
				new Note(68, 8),
				new Note(61, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				new Note(60, 8),
				new Note(61, 8),
				new Note(63, 8),
				new Note(65, 8),
				new Note(67, 8),
				new Note(68, 8),
				new Note(70, 8),
				new Note(72, 8),
				new Note(70, 8),
				new Note(64, 8),
				new Note(-1, 8),
				new Note(-1, 8),
				};
		//Checking harmonize() method (for now just checking that it returns a valid chordsequence)
//		ChordSequence cs = harmonizer.harmonize(seq, 20000, 80);
//		MidiFile mf = new MidiFile();
//		mf.progChange(49);
//		mf.play(cs);
//		mf.saveToMidi(cs, "test2.mid");
//		
//		System.out.println(cs);
		
//		System.out.println("Checking the invertUp(), invertDown(), and invertUpAll() methods for Chord class...");
//		//Checking invert chord methods
//		Chord c = cs.getSeq()[0];
//		System.out.printf("Using the following chord to check invert methods: %s\n", c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		
//		System.out.println("\nNow checking that the invertDown() method correctly prevents the lowest pitch from going below 0...");
//		//checking that the invertDown() method prevents the lowest pitch from going below 0
//		System.out.println("invertDown() is called 100 times...");
//		for (int i = 0; i < 100; ++i) {
//			c.invertDown();
//		}
//		System.out.println("Chord after 100 invert downs: " + c);
//		
//		c = cs.getSeq()[1];
//		System.out.printf("\nNow using the following chord to check invert methods: %s\n", c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertUp();
//		System.out.printf("Chord after inverting up: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertDown();
//		System.out.printf("Chord after inverting down: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		c.invertUpAll();
//		System.out.printf("Chord after invertUpAll method (inverts all 4 notes including melody: %s\n",c);
//		
//		System.out.println("\nNow checking that the invertDown() method correctly prevents the lowest pitch from going below 0...");
//		//checking that the invertDown() method prevents the lowest pitch from going below 0
//		System.out.println("invertDown() is called 100 times...");
//		for (int i = 0; i < 100; ++i) {
//			c.invertDown();
//		}
//		System.out.println("Chord after 100 invert downs: " + c);
//		
//		//Checking ChordInterval constructor, invert() and computeInterval() methods
////		c = cs.getSeq()[0]; //resetting c to the 0th index element of the chordsequence for testing
////		System.out.printf("\nResetting chord to the following prior to ChordInterval test: %s\n", c);
////		ChordInterval ci = new ChordInterval(c);
////		System.out.printf("ChordInterval after construction: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		System.out.println();
//		
////		c = cs.getSeq()[1]; //resetting c to the 0th index element of the chordsequence for more testing
////		System.out.printf("Resetting chord to the following prior to ChordInterval test: %s\n", c);
////		ci = new ChordInterval(c);
////		System.out.printf("ChordInterval after construction: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		System.out.println();
//		
////		System.out.println("Now testing ChordInterval constructor using interval {3,6,10} as parameter...");
////		ci = new ChordInterval(new int[] {3,6,10});
////		System.out.printf("ChordInterval after construction: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		System.out.println();
//		
////		System.out.println("Now testing ChordInterval constructor using chord consisting just of unisons...");
////		ci = new ChordInterval(new Chord(new Note(0), new Note(12), new Note(24), null));
////		System.out.printf("ChordInterval after construction: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		System.out.println();
//		
////		System.out.println("Now testing ChordInterval constructor using chord consisting of just a diad (with the rest being unisons)...");
////		ci = new ChordInterval(new Chord(new Note(0), new Note(2), new Note(24), null));
////		System.out.printf("ChordInterval after construction: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		ci.invert();
////		System.out.printf("ChordInterval after inversion: %s\n", ci);
////		System.out.println();
//		
//		
//		//Test the new invert methods in Chord class and make sure associated ChordInterval
//		//is also inverted correctly
//		c = new Chord(new Note("C 1"), new Note("G#1"), new Note("F#2"), new Note("D 3"));
//		System.out.println("Created new chord for testing: " + c);
//		c.invertUp();
//		System.out.println("After invertUp(): " + c);
//		c.invertUp();
//		System.out.println("After invertUp(): " + c);
//		c.invertUp();
//		System.out.println("After invertUp(): " + c);
//		c.invertUp();
//		System.out.println("After invertUp(): " + c);
//		c.invertUp();
//		System.out.println("After invertUp(): " + c);
//		c.invertUp();
//		System.out.println("After invertUp(): " + c);
//		c.invertUpAll();
//		System.out.println("After invertUpAll(): " + c);
//		c.invertUpAll();
//		System.out.println("After invertUpAll(): " + c);
//		c.invertUpAll();
//		System.out.println("After invertUpAll(): " + c);
//		c.invertUpAll();
//		System.out.println("After invertUpAll(): " + c);
//		c.invertUpAll();
//		System.out.println("After invertUpAll(): " + c);
//		c.invertDown();
//		System.out.println("After invertDown(): " + c);
//		c.invertDown();
//		System.out.println("After invertDown(): " + c);
//		c.invertDown();
//		System.out.println("After invertDown(): " + c);
//		c.invertDown();
//		System.out.println("After invertDown(): " + c);
//		c.invertDown();
//		System.out.println("After invertDown(): " + c);
//		System.out.println();
//		
//		
//		//Testing new Chord constructor
////		System.out.println("Testing new Chord constructor that takes in ChordInterval and base note as parameters...");
////		ci = new ChordInterval(new int[] {7,9,11});
////		c = new Chord(ci, new Note("C 1"));
////		System.out.println(c);
//		
//		//Testing MidiFile's play() method and hamming distance method between 2 chords
////		c = new Chord(new Note(60), new Note(65), new Note(65), new Note(69));
////		long t = System.currentTimeMillis();
////		harmonizer.mf.play(c);
////		System.out.println("Time elapsed: "+ (System.currentTimeMillis() - t));
////		t = System.currentTimeMillis();
////		harmonizer.mf.play(c);
////		System.out.println("Time elapsed: "+ (System.currentTimeMillis() - t));
////		t = System.currentTimeMillis();
////		harmonizer.mf.play(c);
////		System.out.println("Time elapsed: "+ (System.currentTimeMillis() - t));
////		t = System.currentTimeMillis();
//		
//		Chord c2 = new Chord(new Note(62), new Note(65), new Note(70), null);
//		System.out.printf("Computing hamming distance between %s and %s..\n",
//				          c, c2);
//		System.out.println(c.computeDistance(c2));
//		System.out.println(c2.computeDistance(c));  //should be equal to the above
//		
//		c = new Chord(new Note(60), new Note(65), new Note(69), null);
//		System.out.println(c.computeDistance(c2));
//		System.out.println(c2.computeDistance(c));  //should be equal to the above
//		
//		System.out.println("\nNow that ChordInterval class is gone, re-testing some Chord methods...");
//		c = new Chord(new Note(48), new Note(55), new Note(64), new Note(74));
//		System.out.println("Playing following chord: " + c);
////		harmonizer.mf.play(c);
////		harmonizer.mf.play(c);
//		c.invertAny();
//		System.out.println("Playing Chord after inversion: " + c);
////		harmonizer.mf.play(c);
////		harmonizer.mf.play(c);
//		
//		
//		//Testing buildScale() method
//		for (int i = 0; i < Harmonizer.SCALE; ++i) {
//			System.out.println(Arrays.toString(harmonizer.buildScale(i, harmonizer.MAJ_INTV)));
//			System.out.println(Arrays.toString(harmonizer.buildScale(i, harmonizer.MIN_HAR_INTV)));
//			System.out.println(Arrays.toString(harmonizer.buildScale(i, harmonizer.MIN_MEL_INTV)));
//			System.out.println(Arrays.toString(harmonizer.buildScale(i, harmonizer.MIN_NAT_INTV)));
//		}
//		
//		
//		
//		//testing KeySignature int to KeySignature / vice versa methods
//		for (int i = 0; i <= 47; ++i) {
//			KeySignature ks = KeySignature.intToKeySignature(i);
//			int intKs = KeySignature.keySignatureToInt(ks);
//			if (i != intKs) throw new RuntimeException();
//		}
//		
//		//TODO test computeKS method given notesequence
//		seq = new Note[] {
//				new Note("C 6", MidiFile.CROTCHET),
//				new Note("D 6", MidiFile.CROTCHET),
//				new Note("E 6", MidiFile.CROTCHET),
//				new Note("F 6", MidiFile.CROTCHET),
//				new Note("G#6", MidiFile.SEMIBREVE),
//				new Note("C 6", MidiFile.CROTCHET),
//				new Note("D 6", MidiFile.CROTCHET),
//				new Note("E 6", MidiFile.CROTCHET),
//				new Note("G#6", MidiFile.CROTCHET),
//				new Note("G 6", MidiFile.SEMIBREVE),
//				new Note("C 6", MidiFile.CROTCHET),
//				new Note("D 6", MidiFile.CROTCHET),
//				new Note("E 6", MidiFile.CROTCHET),
//				new Note("F 6", MidiFile.CROTCHET),
//				new Note("A#6", MidiFile.SEMIBREVE),
//				
//				new Note("A#6", MidiFile.CROTCHET),
//				new Note("G#6", MidiFile.CROTCHET),
//				new Note("G 6", MidiFile.CROTCHET),
//				new Note("F 6", MidiFile.CROTCHET),
//				new Note("G 6", MidiFile.SEMIBREVE),
//			
//				new Note("A#6", MidiFile.CROTCHET),
//				new Note("G#6", MidiFile.CROTCHET),
//				new Note("G 6", MidiFile.CROTCHET),
//				new Note("F 6", MidiFile.CROTCHET),
//				new Note("G#6", MidiFile.CROTCHET),
//				new Note("G 6", MidiFile.CROTCHET),
//				new Note("F 6", MidiFile.CROTCHET),
//				new Note("D#6", MidiFile.CROTCHET),
//				};
//		harmonizer.mf.play(seq);
//		harmonizer.computeKS(seq);
//		harmonizer.harmonize(seq, 5000, 70);
//		
//		seq = new Note[]{
//				new Note(58, 4),
//				new Note(60, 4),
//				new Note(62, 4),
//				new Note(63, 4),
//				new Note(62, 4),
//				new Note(60, 4),
//				new Note(59, 4),
//				new Note(60, 4),
//				new Note(63, 4),
//				new Note(56, 2),
//				new Note(56, 2 + 4*2 + 4*4),
//				new Note(60, 4),
//				new Note(62, 4),
//				new Note(63, 4),
//				new Note(65, 4),
//				new Note(63, 4),
//				new Note(62, 4),
//				new Note(61, 4),
//				new Note(62, 4),
//				new Note(70, 4),
//				new Note(61, 2),
//				new Note(61, 2 + 4*2 + 4*4),
//				};
////		harmonizer.mf.play(seq);
////		harmonizer.computeKS(seq);
//		
//		seq = new Note[]{
//				new Note(60, 16),
//				new Note(64, 16),
//				new Note(64, 16),
//				new Note(64, 16),
//				new Note(62, 16),
//				new Note(64, 16),
//				new Note(60, 16),
//				new Note(60, 16),
//				new Note(65, 16),
//				new Note(64, 16),
//				new Note(62, 16),
//				new Note(60, 16),
//				new Note(62, 16),
//				new Note(62, 16),
//				new Note(60, 16),
//				new Note(59, 16),
//				new Note(57, 16),
//				new Note(55, 16),
//				new Note(60, 16),
//				new Note(64, 16),
//				new Note(64, 16),
//				new Note(64, 16),
//				new Note(62, 16),
//				new Note(64, 16),
//				new Note(60, 16),
//				new Note(60, 16),
//				new Note(65, 16),
//				new Note(64, 16),
//				new Note(62, 16),
//				new Note(60, 16),
//				new Note(62, 16),
//				new Note(62, 16),
//				new Note(60, 16),
//				};
////		harmonizer.computeKS(seq);
//		
//		seq = new Note[]{
//				new Note(63, 16),
//				new Note(65, 16),
//				new Note(67, 16),
//				new Note(63, 16),
//				new Note(70, 16),
//				new Note(70, 16),
//				new Note(68, 16),
//				new Note(67, 16),
//				new Note(71, 16),
//				new Note(67, 16),
//				new Note(65, 16),
//				new Note(63, 16),
//				new Note(65, 16),
//				new Note(67, 16),
//				new Note(68, 16),
//				new Note(67, 16),
//				new Note(65, 16),
//				new Note(65, 16),
//				new Note(63, 16),
//				new Note(62, 16),
//				new Note(63, 16),
//				new Note(62, 16),
//				new Note(63, 16),
//				new Note(70, 16),
//				new Note(70, 16),
//				new Note(62, 16),
//				new Note(63, 16),
//				new Note(70, 16),
//				new Note(71, 16),
//				new Note(67, 16),
//				new Note(65, 16),
//				new Note(63, 16),
//				new Note(62, 16),
//				new Note(63, 16),
//				new Note(62, 16),
//				new Note(63, 16),
//				new Note(65, 16),
//				new Note(63, 16),
//				new Note(65, 16),
//				new Note(67, 16),
//				new Note(63, 16),
//				new Note(63, 16),
//				new Note(65, 16),
//				new Note(67, 16),
//				new Note(63, 16),
//				new Note(70, 16),
//				new Note(70, 16),
//				new Note(68, 16),
//				new Note(67, 16),
//				new Note(71, 16),
//				new Note(71, 16),
//				new Note(70, 16),
//				new Note(68, 16),
//				new Note(67, 16),
//				new Note(62, 16),
//				new Note(58, 16),
//				new Note(65, 16),
//				new Note(67, 16),
//				new Note(60, 16),
//				new Note(70, 16),
//				new Note(68, 16),
//				new Note(67, 16),
//				new Note(58, 16),
//				new Note(67, 16),
//				new Note(65, 16),
//				new Note(63, 16),
//				new Note(63, 16),
//				};
////		harmonizer.mf.play(seq);
////		harmonizer.computeKS(seq);
//		harmonizer.computeKS_Greedy_inclRest(seq);
//		harmonizer.harmonize(seq, 1000, 70);
		

	}
}
