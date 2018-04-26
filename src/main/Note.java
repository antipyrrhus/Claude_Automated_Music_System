package main;
import java.util.*;
import convertmidi.MidiFile;

/**
	 * Note class.
	 * @author Yury Park
	 */
	public class Note implements Comparable<Note> {
		private String name;  //e.g. "C#", "F ", "E ", "D#", etc.
		private int pitch;    //pitch value
		private int octave;   //how high of a pitch is this?
		private int volume;   //127 = max. volume
		private int duration; //e.g. is this a quarter note? a 16th note? Etc.
		private int channel; //which midi channel (PianoRollGUI's mChannels[] variable) will be used to playback this note?
		
		//For converting integer to pitch name and vice versa
		private final String[] NAME_ARR = {"C ", "C#", "D ", "D#", "E ", "F ", "F#", "G ", "G#", "A ", "A#", "B "};
		private final HashMap<String, Integer> MAP = new HashMap<String, Integer>() {
			private static final long serialVersionUID = 1L;
		{
			put("C ",0);
			put("C#",1);
			put("D ",2);
			put("D#",3);
			put("E ",4);
			put("F ",5);
			put("F#",6);
			put("G ",7);
			put("G#",8);
			put("A ",9);
			put("A#",10);
			put("B ",11);
		}};
		
		/**
		 * Constructor for a "null" note (rest)
		 */
		public Note() {
			this.name = "R ";
			this.octave = -1;
			this.pitch = -1;
			this.duration = MidiFile.CROTCHET;
			this.volume = 0;
			this.channel = 0;
		}
		
		/**
		 * Constructor
		 * @param i pitch value
		 */
		public Note(int i) {
			this();
			if (i < 0) { //indicates rest note
				return;
			}
			this.name = NAME_ARR[i % Harmonizer.SCALE];
			this.octave = i/Harmonizer.SCALE + 1;  //We'll designate the lowest possible octave as 1
			this.pitch = i;
			this.duration = MidiFile.CROTCHET; //by default, assume quarter note duration 
			this.volume = MidiFile.MAX_VOL;    //by default, assume max volume
		}
		
		/**
		 * Overloaded constructor
		 * @param s String assumed to be exactly 3 or 4 length, 
		 *          e.g. "C#10" indicates C#, 10 octaves up, or "F 3" indicates F, 3 octaves up
		 */
		public Note(String s) {
			this.name = s.substring(0, 2);
			this.octave = Integer.parseInt(s.substring(2));
			this.pitch = (this.octave-1) * Harmonizer.SCALE + MAP.get(this.name);
			this.duration = MidiFile.CROTCHET;  //by default, assume quarter note duration 
			this.volume = MidiFile.MAX_VOL;	    //by default, assume max volume
			this.channel = 0;
		}
		
		/**
		 * Overloaded constructor
		 * @param i pitch value
		 * @param duration note's duration
		 */
		public Note(int i, int duration) {
			this(i);
			this.duration = duration;
		}
		
		/**
		 * Overloaded constructor
		 * @param s note's name
		 * @param duration note's duration
		 */
		public Note(String s, int duration) {
			this(s);
			this.duration = duration;
		}
		
		/**
		 * Overloaded constructor
		 * @param i pitch value
		 * @param duration note's duration
		 * @param volume note's volume
		 */
		public Note(int i, int duration, int volume) {
			this(i, duration);
			this.volume = volume;
		}
		
		/**
		 * Overloaded constructor
		 * @param s note's name
		 * @param duration note's duration
		 * @param volume note's volume
		 */
		public Note(String s, int duration, int volume) {
			this(s, duration);
			this.volume = volume;
		}

		public int getVolume() {
			return volume;
		}

		public void setVolume(int volume) {
			this.volume = volume;
		}

		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public String getName() {
			return name;
		}

		public void setPitch(int pitch) {
			this.pitch = pitch;
			this.name = NAME_ARR[pitch % Harmonizer.SCALE];
			this.octave = pitch/Harmonizer.SCALE + 1;
		}

		public int getPitch() {
			return pitch;
		}

		public int getChannel() {
			return this.channel;
		}

		public void setChannel(int channel) {
			this.channel = channel;
		}
		
		public int getOctave() {
			return octave;
		}
		
		public boolean equalsKey(Note otherNote) {
			return this.pitch % Harmonizer.SCALE == otherNote.getPitch() % Harmonizer.SCALE;
		}
		
		public Note copy() {
			return new Note(this.pitch, this.duration, this.volume);
		}
		
		@Override
		public int hashCode() {
			int result = 1;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Note other = (Note) obj;
			if (duration != other.duration)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (octave != other.octave)
				return false;
			if (pitch != other.pitch)
				return false;
			if (volume != other.volume)
				return false;
			return true;
		}
		
		public boolean equalsIgnoreOctave(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Note other = (Note) obj;
			if (duration != other.duration)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			
			if (pitch % Harmonizer.SCALE != other.pitch % Harmonizer.SCALE)
				return false;
			if (volume != other.volume)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("%s%s(%s)-%s", this.name, this.octave, this.pitch, this.duration);
		}

		@Override
		public int compareTo(Note n) {
			if (this.pitch == n.pitch) {
				return this.duration - n.duration;
			} else return this.pitch - n.pitch;
		}
	}
	//end class Note