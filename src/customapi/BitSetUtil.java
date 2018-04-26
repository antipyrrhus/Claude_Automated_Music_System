package customapi;
import java.util.BitSet;

/**
 * 
 * Some utility methods involving BitSet. Used by NoteFeatures, etc. to convert note features to binary string and so on.
 *
 */
public class BitSetUtil {

	/**
	 * Converts an int value (e.g. 2683) to BitSet object.
	 * @param n
	 * @return
	 */
	public static BitSet convertIntToBitSet(int n) {
		if (n < 0) return new BitSet();
		String binaryStr = Integer.toBinaryString(n);
		return convertStrToBitSet(binaryStr);
	}
	
	/**
	 * Copies the bits from one bitset to the other. All the bits in bsSource are copied over to
	 * bsDest, starting at bsDest's given index number.
	 * @param bsSource
	 * @param bsDest
	 * @param startIdx
	 * @return the resulting BitSet
	 */
	public static BitSet copyBits(BitSet bsSource, BitSet bsDest, int startIdx) {
//		System.out.println(bsSource.length());
		int index = startIdx;
		for (int i = 0; i < bsSource.length(); ++i) {
			bsDest.set(index++, bsSource.get(i));
		}
		return bsDest;
	}
	
	
	/**
	 * Converts A string binary representation (e.g. "100100111") to BitSet object.
	 * Assumes that the given binary String is in right format (e.g. contains only 0 and 1).
	 * @param s
	 * @return
	 */
	public static BitSet convertStrToBitSet(String s) {
		int index = 0;
		BitSet bs = new BitSet();
		for (int i = s.length() - 1; i >= 0; --i) {
			char currBitInt = s.charAt(i);
			if (currBitInt == '1') {
				bs.set(index);
			}
			index++;
		} //end for i
		return bs;
	}
	
	/**
	 * Converts the bitset to a binary string consisting of 0's and 1's
	 * @param bs
	 * @return
	 */
	public static String convertBitsetToBinaryStr(BitSet bs) {
		return convertBitsetToBinaryStr(bs, 0, bs.length());
	}
	
	/**
	 * Helper method. Converts the bitset to a binary string consisting of 0's and 1's.
	 * Only converts the bitset's indices in the range [start, end).
	 * @param bs
	 * @param start
	 * @param end
	 * @return
	 */
	public static String convertBitsetToBinaryStr(BitSet bs, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = end - 1; i >= start; --i) {
			sb.append(bs.get(i) == true ? "1" : "0");
		}
		
		//If the string is empty, just return a single '0'.
		return sb.length() == 0 ? "0" : sb.toString();
	}
	
	/**
	 * Converts the bitset to a int value and returns the String representation thereof
	 * @param bs
	 * @return
	 */
	public static String convertBitsetToIntStr(BitSet bs) {
		return convertBitsetToIntStr(bs, 0, bs.length());
	}
	/**
	 * Converts the bitset to an integer value, from the start (inclusive) to end (exclusive) index, and
	 * returns the String representation thereof
	 * @param bs
	 * @param start
	 * @param end
	 * @return
	 */
	public static String convertBitsetToIntStr(BitSet bs, int start, int end) {
		String str = convertBitsetToBinaryStr(bs, start, end); //first convert to binary string using overloaded method
		return Integer.parseInt(str, 2) + ""; //built in method to convert binary string to base 2 decimal 
	} 
}
