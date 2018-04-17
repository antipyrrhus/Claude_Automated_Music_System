package convertmidi;

/*
 * A simple Java class that writes a MIDI file
 * Base code provided by Kevin Boone (http://kevinboone.net/javamidi.html)
 * Edited by Yury Park for use with Harmonizer.java.
 */

import java.io.*;
import java.io.FileReader;
import java.util.*;

import javax.sound.midi.*;

import main.*;
import pianoroll.WrapperNote;

public class MidiFile
{
	// Note lengths
	//  We are working with 32 ticks to the crotchet. So
	//  all the other note lengths can be derived from this
	//  basic figure. Note that the longest note we can
	//  represent with this code is one tick short of a 
	//  two semibreves (i.e., 8 crotchets)

//	public static final int SEMIQUAVER = 4;  //16th note
//	public static final int QUAVER = 8;      //8th note
//	public static final int CROTCHET = 16;   //quarter note
//	public static final int MINIM = 32;      //half note
//	public static final int SEMIBREVE = 64;  //whole note
//	public static final int MAX_VOL = 127;
	
	//Trying the following durations instead and see what happens
	public static final int TRIPLET_16th = 2;
	public static final int SEMIQUAVER = 3;  //16th note
	public static final int TRIPLET_8TH = 4;
	public static final int QUAVER = 6;      //8th note
	public static final int TRIPLET_QUARTER = 8;
	public static final int CROTCHET = 12;   //quarter note
	public static final int MINIM = 24;      //half note
	public static final int SEMIBREVE = 48;  //whole note
	public static final int MIN_DURATION = 1;
	public static final int MAX_DURATION = SEMIBREVE;
	public static final int MAX_VOL = 127;
	
	public static final int MULT = 64; //multipler for real-time playback
	
//	private int instrument;

	// Standard MIDI file header, for one-track file
	// 4D, 54... are just magic numbers to identify the
	//  headers
	// Note that because we're only writing one track, we
	//  can for simplicity combine the file and track headers
	static final int header[] = new int[]
			{
					0x4d, 0x54, 0x68, 0x64, 0x00, 0x00, 0x00, 0x06,
					0x00, 0x00, // single-track format
					0x00, 0x01, // one track
					0x00, 0x10, // 16 ticks per quarter
					0x4d, 0x54, 0x72, 0x6B
			};

	// Standard footer
	static final int footer[] = new int[]
			{
					0x01, 0xFF, 0x2F, 0x00
			};

	// A MIDI event to set the tempo
	static final int tempoEvent[] = new int[]
			{
					0x00, 0xFF, 0x51, 0x03,
					0x0F, 0x42, 0x40 // Default 1 million usec per crotchet
			};

	// A MIDI event to set the key signature. This is irrelevant to
	//  playback, but necessary for editing applications 
	static final int keySigEvent[] = new int[]
			{
					0x00, 0xFF, 0x59, 0x02,
					0x00, // C
					0x00  // major
			};


	// A MIDI event to set the time signature. This is irrelevant to
	//  playback, but necessary for editing applications 
	static final int timeSigEvent[] = new int[]
			{
					0x00, 0xFF, 0x58, 0x04,
					0x04, // numerator
					0x02, // denominator (2==4, because it's a power of 2)
					0x30, // ticks per click (not used)
					0x08  // 32nd notes per crotchet 
			};

	private MidiChannel[] mChannels;

	// The collection of events to play, in time order
	protected Vector<int[]> playEvents;

	/** Construct a new MidiFile with an empty playback event list */
	public MidiFile()
	{
//		this.instrument = 0;
		playEvents = new Vector<int[]>();
		try{
			/* Create a new Sythesizer and open it. Most of 
			 * the methods you will want to use to expand on this 
			 * example can be found in the Java documentation here: 
			 * https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Synthesizer.html
			 */
			Synthesizer midiSynth = MidiSystem.getSynthesizer(); 
			midiSynth.open();

			//get and load default instrument and channel lists
			//        Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
			//        midiSynth.loadInstrument(instr[150]);//load an instrument
			mChannels = midiSynth.getChannels();

			//change instrument (optional if you want to just use the piano)
			//        mChannels[0].programChange(0);
			//        mChannels[1].programChange(0);
		} catch (MidiUnavailableException mue) {
			mue.printStackTrace();
		}
	}


	/** Write the stored MIDI events to a file */
	public void writeToFile (String filename)
			throws IOException
	{
		FileOutputStream fos = new FileOutputStream (filename);


		fos.write (intArrayToByteArray (header));

		// Calculate the amount of track data
		// _Do_ include the footer but _do not_ include the 
		// track header

		int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length
				+ footer.length;

		for (int i = 0; i < playEvents.size(); i++)
			size += playEvents.elementAt(i).length;

		// Write out the track data size in big-endian format
		// Note that this math is only valid for up to 64k of data
		//  (but that's a lot of notes) 
		int high = size / 256;
		int low = size - (high * 256);
		fos.write ((byte) 0);
		fos.write ((byte) 0);
		fos.write ((byte) high);
		fos.write ((byte) low);


		// Write the standard metadata — tempo, etc
		// At present, tempo is stuck at crotchet=60 
		fos.write (intArrayToByteArray (tempoEvent));
		fos.write (intArrayToByteArray (keySigEvent));
		fos.write (intArrayToByteArray (timeSigEvent));

		// Write out the note, etc., events
		for (int i = 0; i < playEvents.size(); i++)
		{
			fos.write (intArrayToByteArray (playEvents.elementAt(i)));
		}

		// Write the footer and close
		fos.write (intArrayToByteArray (footer));
		fos.close();
	}


	/** Convert an array of integers which are assumed to contain
      unsigned bytes into an array of bytes */
	protected static byte[] intArrayToByteArray (int[] ints)
	{
		int l = ints.length;
		byte[] out = new byte[ints.length];
		for (int i = 0; i < l; i++)
		{
			out[i] = (byte) ints[i];
		}
		return out;
	}


	/** Store a note-on event */
	public void noteOn (int delta, int note, int velocity)
	{
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x90;
		data[2] = note;
		data[3] = velocity;
		playEvents.add (data);
	}


	/** Store a note-off event */
	public void noteOff (int delta, int note)
	{
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x80;
		data[2] = note;
		data[3] = 0;
		playEvents.add (data);
	}


	/** Store a program-change event at current position */
	public void progChange (int prog)
	{
		int[] data = new int[3];
		data[0] = 0;
		data[1] = 0xC0;
		data[2] = prog;
		playEvents.add (data);
		mChannels[0].programChange(prog);
	}


	/** Store a note-on event followed by a note-off event a note length
      later. There is no delta value — the note is assumed to
      follow the previous one with no gap. */
	public void noteOnOffNow (int duration, int note, int velocity)
	{
		noteOn (0, note, velocity);
		noteOff (duration, note);
	}

	public void noteSequenceFixedVelocity (int[] sequence, int velocity)
	{
		boolean lastWasRest = false;
		int restDelta = 0;
		for (int i = 0; i < sequence.length; i += 2)
		{
			int note = sequence[i];
			int duration = sequence[i + 1];
			if (note < 0)
			{
				// This is a rest
				restDelta += duration;
				lastWasRest = true;
			}
			else
			{
				// A note, not a rest
				if (lastWasRest)
				{
					noteOn (restDelta, note, velocity);
					noteOff (duration, note);
				}
				else
				{
					noteOn (0, note, velocity);
					noteOff (duration, note);
				}
				restDelta = 0;
				lastWasRest = false;
			}
		}
	}
	
	public void play(ChordSequence cs, int instrument, int tempo) {
		this.progChange(instrument);
		for (Chord c : cs.getSeq()) {
//			if (c == null) {
//				try {
//					Thread.sleep(64 * tempo); // wait time in milliseconds to control duration. The constant is arbitrary.
//				} catch (InterruptedException ie) {
//					ie.printStackTrace();
//				}
//			} 
//			else {
				play(c, tempo);
//			}
		}
	}

	/**
	 * Given a Chord, plays it for the specified duration at the specified volume.
	 * ASSUMES that all the Notes in the chord have the same volume and duration,
	 * and only looks at the first Note's volume / duration and applies it to the entire chord.
	 * @param c Chord consisting of Note objects.
	 */
	public void play(Chord c, int tempo) {
		if (c == null) {
			try {
				Thread.sleep(64 * tempo); // wait time in milliseconds to control duration. The constant is arbitrary.
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			return;
		}
		Note[] notes = c.getNotes();
		int vol = notes[0].getVolume();
		int duration = notes[0].getDuration();
		for (int i = 0; i < notes.length; ++i) {
			if (notes[i] == null) continue;
			this.mChannels[0].noteOn(notes[i].getPitch(), vol);
		}
		try {
			Thread.sleep(64 * duration); // wait time in milliseconds to control duration. The constant is arbitrary.
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		for (int i = 0; i < notes.length; ++i) {
			if (notes[i] == null) continue;
			this.mChannels[0].noteOff(notes[i].getPitch());
		}	
	}
	
	/**
	 * Plays a sequence of Notes
	 * @param notesequence array of single notes
	 */
	public void play(Note[] notesequence) {
		for (int i = 0; i < notesequence.length; ++i) {
			if (notesequence[i] == null) continue;
			Note note = notesequence[i];
			int duration = note.getDuration();
			int pitch = note.getPitch();
//			System.out.println(pitch);
			int volume = note.getVolume();
			this.mChannels[0].noteOn(pitch, volume);
			try {
				Thread.sleep(64 * duration); // wait time in milliseconds to control duration. The constant is arbitrary.
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			this.mChannels[0].noteOff(pitch);
		}
	}
	
	public void saveToMidi(ChordSequence cs, String filename) {
		if (filename == null || filename.isEmpty()) filename = "test1.mid";
		int currDuration = 0;
		for (Chord c : cs.getSeq()) {
			for (Note n : c.getNotes()) {
				currDuration = n.getDuration();
				this.noteOn(0, n.getPitch(), n.getVolume());
			}
			for (Note n : c.getNotes()) {
				this.noteOff(currDuration, n.getPitch());
				currDuration = 0;
			}
		}
		try {
			this.writeToFile(filename);
		}catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Reads from a save file (.dat), and outputs the data in the form of a MIDI (.mid) file.
	 * @param file
	 */
	public boolean saveToMidi(File fileToReadFrom, String midiPathAndFileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileToReadFrom));
			Integer.parseInt(br.readLine());  //gcd
			int tempo = Integer.parseInt(br.readLine());
			Integer.parseInt(br.readLine());  //num. of Cols
			br.readLine().split(" ");  //For key signature
			int midi_instrument = Integer.parseInt(br.readLine());
			Integer.parseInt(br.readLine()); //boolean chordBuilderMode
			
			this.progChange(midi_instrument);
			
			ArrayList<WrapperNote> wnAL = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(" ");
				WrapperNote wnOn = new WrapperNote(Integer.parseInt(arr[0]));
				wnOn.setDuration(Integer.parseInt(arr[1]));
				wnOn.setIdx(Integer.parseInt(arr[2]));
				wnAL.add(wnOn);
				
				//Also we'll add a dummy wrappernote indicating when the above note should be set to OFF.
				WrapperNote wnOff = new WrapperNote(wnOn.getPitch());
				wnOff.setNoteOff(true);
//				wnOff.setDuration(wnOn.getDuration());
				wnOff.setIdx(wnOn.getColIdx() + wnOn.getDuration());
				wnAL.add(wnOff);
			}
			br.close();
			
			//Start by sorting the wrappernotes in the order of their column indices
			//(or, if their column indices are the same, in the order of their duration)
			//(or, if both the above are the same, the OFF notes should come before the ON notes)
			Collections.sort(wnAL, new Comparator<WrapperNote>() {
				@Override
				public int compare(WrapperNote n1, WrapperNote n2) {
					if (n1.getColIdx() == n2.getColIdx()) {
						if (n1.getDuration() == n2.getDuration()) {
							return n1.isNoteOff() == n2.isNoteOff() ? 0 : n1.isNoteOff() ? -1 : 1;
						} else {
							return n1.getDuration() - n2.getDuration();
						}
					} else {
						return n1.getColIdx() - n2.getColIdx();
					}
				} //end public int compare
			}); //end Collections.sort
			
			
			//Now go thru each note and compute note on, note off in order
			int i = 0;
			while (i < wnAL.size()) {
				WrapperNote currNote = wnAL.get(i);
				WrapperNote prevNote = null;
				if (i > 0) prevNote = wnAL.get(i-1);
//				WrapperNote nextNote = null;
//				if (i + 1 < wnAL.size()) nextNote = wnAL.get(i+1);
				
				if (i == 0) {  //In this case the first WrapperNote should be an ON note. Notate it.
					this.noteOn(currNote.getColIdx(), currNote.getPitch(), currNote.getVolume());
				} else {
					if (currNote.isNoteOff()) { //OFF note
						this.noteOff(tempo * (currNote.getColIdx() - prevNote.getColIdx()), currNote.getPitch());
					} else {
						this.noteOn(tempo * (currNote.getColIdx() - prevNote.getColIdx()), currNote.getPitch(), currNote.getVolume());
					}
				} //end if (i == 0) / else
				i++;
				
			} //end while
			this.writeToFile(midiPathAndFileName);
			return true;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Test method — creates a file test1.mid when the class
      is executed */
	public static void main (String[] args)
			throws Exception
	{
		
		for (int i = 0; i < 100; ++i) {
			int pitch = (int)(Math.random() * 12 + 60);
			System.out.printf("new Note(%s, 4),\n", pitch);
		}
//		
		MidiFile mf = new MidiFile();
//
//		// Test 1 — play a C major chord
//
//		// Turn on all three notes at start-of-track (delta=0)
//		// the last 2 parameters pertain to the pitch, and the volume (127 = maximum)
		mf.noteOn (0, 60, 127);
		mf.noteOn (0, 64, 127);
		mf.noteOn (0, 67, 127);
//
//		// Turn off all three notes after one minim. 
//		// NOTE delta value is cumulative — only _one_ of
//		//  these note-offs has a non-zero delta. The second and
//		//  third events are relative to the first
		mf.noteOff (MINIM, 60);
		mf.noteOff (0, 64);
		mf.noteOff (0, 67);
//
//		//play another 3-note chord after a brief rest (CROTCHET)
		mf.noteOn (CROTCHET, 60, 127);
		mf.noteOn (0, 65, 127);
		mf.noteOn (0, 69, 127);
//
//		//turn off the chord after one minim
		mf.noteOff (MINIM, 60);
		mf.noteOff (0, 65);
		mf.noteOff (0, 69);
//
//		//and so on...
//
//
//		// Test 2 — play a scale using noteOnOffNow
//		//  We don't need any delta values here, so long as one
//		//  note comes straight after the previous one 
//
//		//Chromatic scale. Each note lasts for CROTCHET duration.
		for (int i = 60; i <= 72; ++i) {
			mf.noteOnOffNow (CROTCHET, i, 127);
		}
//
//		//C major scale
		mf.noteOnOffNow (QUAVER, 60, 127);
		mf.noteOnOffNow (QUAVER, 62, 127);
		mf.noteOnOffNow (QUAVER, 64, 127);
		mf.noteOnOffNow (QUAVER, 65, 127);
		mf.noteOnOffNow (QUAVER, 67, 127);
		mf.noteOnOffNow (QUAVER, 69, 127);
		mf.noteOnOffNow (QUAVER, 71, 127);
		mf.noteOnOffNow (QUAVER, 72, 127);
//
//		// Test 3 — play a short tune using noteSequenceFixedVelocity
//		//  Note the rest inserted with a note value of -1
//
		int[] sequence = new int[]
				{
						60, QUAVER + SEMIQUAVER,
						65, SEMIQUAVER,
						70, CROTCHET + QUAVER,
						69, QUAVER,
						65, TRIPLET_8TH,
						62, TRIPLET_8TH,
						67, TRIPLET_8TH,
						72, MINIM + QUAVER,
						-1, SEMIQUAVER,
						72, SEMIQUAVER,
						76, SEMIBREVE,
				};
//
//		// What the heck — use a different instrument for a change
		mf.progChange (10);
//
		mf.noteSequenceFixedVelocity (sequence, 127);

		mf.writeToFile ("test1.mid");
//		
//		Note[] seq = new Note[]{
//		new Note(65, 8),
//		new Note(72, 8),
//		new Note(72, 8),
//		new Note(67, 8),
//		new Note(65, 8),
//		new Note(65, 8),
//		new Note(60, 8),
//		new Note(65, 8),
//		new Note(65, 8),
//		new Note(67, 8),
//		new Note(65, 8),
//		new Note(-1, 8),
//		new Note(65, 8),
//		new Note(72, 8),
//		new Note(72, 8),
//		new Note(67, 8),
//		new Note(65, 8),
//		new Note(65, 8),
//		new Note(60, 8),
//		new Note(65, 8),
//		new Note(65, 8),
//		new Note(67, 8),
//		new Note(65, 8),
//		new Note(-1, 8),
//		new Note(65, 8),
//		new Note(72, 8),
//		new Note(70, 8),
//		new Note(65, 8),
//		new Note(67, 8),
//		new Note(63, 8),
//		new Note(63, 8),
//		new Note(70, 8),
//		new Note(68, 8),
//		new Note(61, 8),
//		new Note(-1, 8),
//		new Note(-1, 8),
//		new Note(60, 8),
//		new Note(61, 8),
//		new Note(63, 8),
//		new Note(65, 8),
//		new Note(67, 8),
//		new Note(68, 8),
//		new Note(70, 8),
//		new Note(72, 8),
//		new Note(70, 8),
//		new Note(64, 8),
//		new Note(-1, 8),
//		new Note(-1, 8),
//		};
//		
//		mf.play(seq);
	}
}