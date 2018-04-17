package convertmidi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

import main.Chord_NoLimit;
import main.Note;


public class ReadMidi {
	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	private ArrayList<Chord_NoLimit> cAL;
	private int gcd;
	private long[] lastTickArr;
	private long lastTick;
	
	
	private class MidiNoteInfo implements Comparable<MidiNoteInfo> {
		private boolean on;
		private int pitch;
		private long tick;
		
		MidiNoteInfo(boolean on, int pitch, long tick) {
			this.on = on;
			this.pitch = (pitch == 255 ? -1 : pitch);
			this.tick = tick;
		}
		
		@Override
		public int compareTo(MidiNoteInfo mni) {
			if (this.tick == mni.tick) {
				return this.pitch - mni.pitch;
			}
			return (this.tick - mni.tick < 0 ? -1 : 1);
		}
		
		@Override
		public String toString() {
			return String.format("(tick %s : %s, %s)", this.tick, this.pitch, this.on ? "ON" : "OFF");
		}
		
		
	}
	
	/**
	 * Constructor.
	 */
	public ReadMidi(File file) throws IOException, InvalidMidiDataException {
		Sequence sequence = null;
		sequence = MidiSystem.getSequence(file);
		

//		int trackNumber = 0;
		//This array contains the final tick (pertaining to the final note or chord in a track).
		//Since there may be multiple tracks, this array keeps track of the same for each track.
		//(Though, currently we only support a single track, so this will be an array size of 1.)
		this.lastTickArr = new long[sequence.getTracks().length];
		int maxTrackSize = 0;
		for (Track t : sequence.getTracks()) {
			maxTrackSize = Math.max(maxTrackSize, t.size());
		}
		
		ArrayList<MidiNoteInfo> mniAL = new ArrayList<>();
		
		for (int i=0; i < maxTrackSize; i++) {
			for (Track track :  sequence.getTracks()) {
				
				if (i >= track.size()) continue;
				MidiEvent event = track.get(i);
				System.out.print("i = " + i + ", @" + event.getTick() + " ");
				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
					System.out.print("Channel: " + sm.getChannel() + " ");
					
					//If the current note starts at a different tick, then we have a new chord.
					//Add the chord we have so far to the arraylist, then instantiate a new chord.
					
//					if (i > 0 && track.get(i-1).getTick() != event.getTick()) {
//						cAL.add(c);
//						tickAL.add(track.get(i-1).getTick());
//						c = new Chord_NoLimit(event.getTick());
//					}
					
					//Add note to the current chord
					if (sm.getCommand() == NOTE_ON) {
						int key = sm.getData1();
						int octave = (key / 12)-1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						int velocity = sm.getData2();
						
						System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//						lastTickArr[trackNumber-1] = event.getTick();
//						lastTick = Math.max(lastTick, event.getTick());
//						Note n = null;
//						if (key == 255) {
//							n = new Note();
//						} else {
//							n = new Note(key);
//						}
						MidiNoteInfo mni = new MidiNoteInfo(velocity == 0 ? false : true, key, event.getTick());
						mniAL.add(mni);
//						c.addNote(n);
						
					//Search for the note in one of the previous chords and edit its duration
					//Uses binary search for efficiency
					} else if (sm.getCommand() == NOTE_OFF) {
						int key = sm.getData1();
						int octave = (key / 12)-1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						int velocity = sm.getData2();
						System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//						lastTickArr[trackNumber-1] = event.getTick();
//						lastTick = Math.max(lastTick, event.getTick());
						MidiNoteInfo mni = new MidiNoteInfo(false, key, event.getTick());
						mniAL.add(mni);
//						for (int j = cAL.size() - 1; j >= 0; --j) {
//							Chord_NoLimit c_tmp = cAL.get(j);
//							Note n = c_tmp.binarySearchNote(key == 255 ? -1 : key);
//							if (n != null) {
//								n.setDuration((int)(event.getTick() - tickAL.get(j)));
//								break;
//							}
//						}
					} else {
						System.out.println("Command:" + sm.getCommand());
					} //end if (sm.getCommand() == NOTE_ON) / else if / else
				} else {
					System.out.println("Other message: " + message.toString());
				} // end if (message instanceof ShortMessage) / else
			} //end for (int i=0; i < track.size(); i++)
		} //end for (Track track :  sequence.getTracks())
		
		
		Collections.sort(mniAL);
		System.out.println(mniAL);
		
		
		/**
		 * Read all note on, note off sequences and converts them into Chord_NoLimit objects and puts them into ArrayList
		 */
		this.cAL = new ArrayList<>();  //array list of Chords
		
		//Below is needed for computing duration of note on and note off.
		//tickAL.get(i) = the starting tick time for the chord located in cAL.get(i)
		ArrayList<Long> tickAL = new ArrayList<>();
		Chord_NoLimit c = new Chord_NoLimit();
		lastTick = 0;
		
		for (int i = 0; i < mniAL.size(); ++i) {
			MidiNoteInfo mni = mniAL.get(i);
			//If the current note starts at a different tick, then we have a new chord.
			//Add the chord we have so far to the arraylist, then instantiate a new chord.
			if (i > 0 && mniAL.get(i-1).tick != mni.tick) {
				cAL.add(c);
				tickAL.add(mniAL.get(i-1).tick);
				c = new Chord_NoLimit(mni.tick);
			}
			
			//Add note to the current chord
			if (mni.on == true) {
				int key = mni.pitch;
//				int octave = (key / 12)-1;
//				int note = key % 12;
//				String noteName = NOTE_NAMES[note];
//				int velocity = sm.getData2();
//				System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//				lastTickArr[trackNumber-1] = event.getTick();
				lastTick = Math.max(lastTick, mni.tick);
//				Note n = null;
//				if (key == 255) {
//					n = new Note();
//				} else {
//					n = new Note(key);
//				}
				
				c.addNote(new Note(key));
				
			//Search for the note in one of the previous chords and edit its duration
			//Uses binary search for efficiency
			} else {
				int key = mni.pitch;
//				int octave = (key / 12)-1;
//				int note = key % 12;
//				String noteName = NOTE_NAMES[note];
//				int velocity = sm.getData2();
//				System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
//				lastTickArr[trackNumber-1] = event.getTick();
				lastTick = Math.max(lastTick, mni.tick);
				
				for (int j = cAL.size() - 1; j >= 0; --j) {
					Chord_NoLimit c_tmp = cAL.get(j);
					Note n = c_tmp.binarySearchNote(key == 255 ? -1 : key);
					if (n != null) {
						n.setDuration((int)(mni.tick - tickAL.get(j)));
						break;
					}
				}
			}
		}
		
		//Get the greatest common divisor for the duration of each notes. The gcd will determine the duration of a single cell
		//in the pianoroll.
		this.gcd = -1;
		for (Chord_NoLimit chord : cAL) {
			System.out.println(chord);
//			chord.setTick(chord.getTick() / 10);  //testing, delete afterwards
			for (Note n : chord.getNotesAL()) {
				if (gcd == -1) gcd = n.getDuration();
				else gcd = getGCD(gcd, n.getDuration()); 
//				n.setDuration(n.getDuration() / 10);  //testing, delete
			}
		}
		//testing
		System.out.println("lastTickArr: " + Arrays.toString(lastTickArr));
		
		if (gcd <= 0) throw new RuntimeException("ReadMidi: the note duration for one or more notes from the midi file is"
				+ " 0 or less! Something is wrong.");
		System.out.println("GCD = " + this.gcd);
//		gcd = 10;  //testing - delete afterwards
		
	} //end public ReadMidi
	
	public int getCellDuration() {
		return gcd;
	}

	private int getGCD(int a, int b) {
		if (b == 0) return a;
		return getGCD(b, a%b);
	}
	
	public ArrayList<Chord_NoLimit> getcAL() {
		return cAL;
	}

	public long getLastTick() {
		return lastTick;
	}
	
	public static void main(String[] args) throws Exception {
		Pattern pattern = MidiFileManager.loadPatternFromMidi(new File("test1.mid"));
        System.out.println(pattern);
	}

}
