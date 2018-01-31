package convertmidi;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;

public class Test {

	/****************************************************************************************************************/
	/** Binary search for a sorted array that's been rotated
	 * @param arr
	 * @param target
	 * @return
	 */
	public static int search(int[] arr, int target) {
		if (arr == null || arr.length == 0) return -1;  //base case for empty array
		int pivIndex = searchPivot(arr, 0, arr.length - 1);
		System.out.println("Pivot index: " + pivIndex);
		if (pivIndex == -1) {
			return Arrays.binarySearch(arr, 0, arr.length, target);
		}
		return Math.max(Arrays.binarySearch(arr, 0, pivIndex+1, target),
				Arrays.binarySearch(arr, pivIndex+1, arr.length, target));
	}
	//Helper method for search() method. Finds the pivot (max. value) assuming a sorted, rotated array
	//If the array is rotated all the way around (so that all elements are back in orig. position,
	//and thus the array is sorted), then there is no pivot and returns -1.
	private static int searchPivot(int[] arr, int li, int hi) {
		if (li == hi) return -1; //base case, pivot not found
		int mid = (li+hi+1)/2;  //round up middle index
		if (arr[li] > arr[mid]) {  //If true, pivot lies somewhere between li and mid
			if (mid == li + 1) return li;  //if lowindex and midindex are adjacent, we found our pivot index
			return searchPivot(arr, li, mid);  //tCheck the range between li and mid
		} else {
			return searchPivot(arr, mid, hi);
		}
	}

	/****************************************************************************************************************/
	/**
	 * Given a list of sequentially increasing numbers {1, ..., n}, finds the number of derangements.
	 * @param n
	 * @return
	 */
	public static BigInteger getDerangements(int n) {
		/* Consider base cases:
		 * n = 1. Then we have {1}.     There are 0 possible derangements.
		 * n = 2. Then we have {1,2}.   There are 1 possible derangements.
		 * n = 3. Then we have {1,2,3}. There are {2,3,1}, {3,1,2}, ergo 2 derangements.
		 * */

		//Now consider n = 4: {1,2,3,4}
		//How many permutations such that no digit is in its original index?
		//There are 3 possible choices for the first index. (i.e. 2, 3, 4)
		//WLOG, assume that 4 is assigned to first index. {4, __, __, __}
		//Then remaining sets of numbers to be assigned are {1,2,3}, and the indices remaining are 2,3,4.
		//Assume that index 4 is assigned with the number 1, so that we have {4, __, __, 1}
		//In this case, there is 1 choice for each of the remaining indices. Namely {4,3,2,1}. This is like solving
		//the derangements problem for the case where n = 2.
		//Now assume that index 4 is NOT assigned with the number 1...then we have 2 choices for each of the remaining indices:
		//{4, (1 or 3), (1 or 2), (2 or 3)}. This is like solving the derangements for n = 3.

		/* So let's use dynamic programming.
		 * d[i] = no. of derangements for the case where n = i. */
		BigInteger[] d = new BigInteger[n+1];
		d[1] = BigInteger.ZERO;
		d[2] = BigInteger.ONE;
		d[3] = new BigInteger("2");

		for (int i = 4; i <= n; ++i) {
			BigInteger dTmp = d[i-1].add(d[i-2]);
			d[i] = new BigInteger(i-1+"").multiply(dTmp);
		}
		return d[n];
	}

	/****************************************************************************************************************/
	public class ListNode {
		int val;
		ListNode next;
		ListNode(int x) { 
			val = x;
		}
	}

	/**
	 * Checks whether the current Linked List (beginning with the head node) is a palindrome.
	 * @param head
	 * @return true IFF it's a palindrome
	 */
	public boolean isPalindrome(ListNode head) {
		//Easy solution using O(n) space.
		Stack<ListNode> s = new Stack<>();
		ListNode curr = head;
		while (curr != null) {
			s.add(curr);
			curr = curr.next;
		}

		curr = head;
		while(curr != null) {
			if (curr.val != s.pop().val) return false;
			curr = curr.next;
		}
		return true;
	}
	//end public boolean isPalindrome

	/**
	 * Alt. solution to the above using O(1) space
	 * @param head
	 * @return
	 */
	public boolean isPalindrome2(ListNode head) {
		//Alt. solution using O(1) space.
		//Figure out the middle point of the linked list by having 2 pointers, one that goes twice as fast
		ListNode curr = head;
		ListNode curr2x = head;

		//e.g. consider { 4 -> 2 -> 3 -> 2 -> 4 -> (null) }  (not counting the implied null node, this list is odd sized)
		while (curr2x != null && curr2x.next != null) {
			curr = curr.next;
			curr2x = curr2x.next.next;
		}

		/* Once the above loop is over, the position of curr should be the middle position of the list
		 * if the list size is odd, and middle position + 1 if the list size is even.
		 * If the list size is odd, we don't need to worry about the middle index for purposes of
		 * figuring out palindrome, so we'll shift curr by 1 more.
		 * reverse the 2nd half of the list beginning with curr 
		 */
		if (curr2x != null) curr = curr.next;
		curr = reverseLinkedList(curr);

		/* Now compare the reversed 2nd half (starting with curr)
		 * with the unreversed 1st half (starting with head) */
		while (curr != null) {
			if (head.val != curr.val) return false;
			head = head.next;
			curr = curr.next;
		}
		return true;
	}

	
	private static ListNode reverseLinkedList(ListNode curr) {
		//e.g. {3 -> 4 -> 2 -> 6 -> null} becomes {null <- 3 <- 4 <- 2 <- 6}, where the initial value of curr = 3.
		//After reversal, the final value of curr = 6 (aka this is the new head node). We will return this.
		ListNode left = null;

		while (curr != null) {
			ListNode right = curr.next;
			curr.next = left;
			left = curr;
			curr = right;
		}
		return left;  //the last non-null value is returned as the new "head" of the reversed list.
	}


	/****************************************************************************************************************/
	/* Find the SECOND smallest value in the tree. If none, return -1. 
	 * The binary tree is defined as follows:
	 * Each node has either ZERO or TWO children nodes.
	 * If it has 2 children nodes, the parent node's value equals the SMALLER of the two children nodes. 
	 * Example tree: */

	//     2
	//    / \
	//   2   5
	//  /\   /\
	// 3  2 5  7

	/**
	 * Definition for a binary tree node.
	 * public class TreeNode {
	 *     int val;
	 *     TreeNode left;
	 *     TreeNode right;
	 *     TreeNode(int x) { val = x; }
	 * }
	 */
	public class TreeNode {
		int val;
		TreeNode left;
		TreeNode right;
		TreeNode(int x) { val = x; }
	}

	public int findSecondMinimumValue(TreeNode root) {
		if (root == null || root.left == null && root.right == null) return -1;
		return findMinValue(root, root.val);
	}

	/**
	 * Helper method. Find min. value in the tree (starting at the subroot)
	 * that is > thresholdValue. If no such value exists, -1.
	 */
	private int findMinValue(TreeNode subroot, int thresholdValue) {
		if (subroot == null) return -1;
		if (subroot.val > thresholdValue) return subroot.val;
		int val1 = findMinValue(subroot.left,  thresholdValue);
		int val2 = findMinValue(subroot.right, thresholdValue);
		if (val1 == -1) return val2;
		if (val2 == -1) return val1;
		return Math.min(val1, val2);
	}

	/****************************************************************************************************************/
	/**
	 * Given a binary tree containing digits from 0-9 only, each root-to-leaf path could represent a number.
	 * An example is the root-to-leaf path 1->2->3 which represents the number 123.
	 * Find the total sum of all root-to-leaf numbers.
	 * For example,
	 *     1
	 *    / \
	 *   2   3
	 *   
	 *   The root-to-leaf path 1->2 represents the number 12.
	 *   The root-to-leaf path 1->3 represents the number 13.
	 *   Return the sum = 12 + 13 = 25.
	 * @param root
	 * @return
	 */
	public int sumNumbers(TreeNode root) {
		return sum(root, 0);
	}

	public int sum(TreeNode n, int s){
		if (n == null) return 0;
		if (n.right == null && n.left == null) return s*10 + n.val;
		return sum(n.left, s*10 + n.val) + sum(n.right, s*10 + n.val);
	}

	/****************************************************************************************************************/
	/**
	 * Find the longest palindrome substring in s.
	 * @param s
	 * @return
	 */
	public String longestPalindrome(String s) {
		//Let d[i][j] == true IFF substring(i...j), inclusive, forms a palindrome.
		//Recursive substructure:
		//d[i][j] = true if charat(i) == charat(j) && d[i+1][j-1] == true.
		//base cases: d[i][i] = true for all i, and d[i][i+1] = true IFF charat(i) == charat(i+1)
		boolean[][] d = new boolean[s.length()][s.length()];
		int longestSoFar = 1;
		String ret = s.charAt(0) + "";

		for (int i = 0; i < s.length(); ++i) {
			d[i][i] = true;
			if (i + 1 >= s.length()) break;  //prevent index out of range error
			if (s.charAt(i) == s.charAt(i+1)) {
				d[i][i+1] = true;
				longestSoFar = 2;
				ret = s.substring(i, i+2);
			}    
		}
		//end for i

		int k = 2;
		while (k < s.length()) {
			for (int i = 0; i < s.length(); ++i) {
				int j = i + k;
				if (j >= s.length()) break;  //break out of for loop (not while loop) when j goes out of index bound
				d[i][j] = (s.charAt(i) == s.charAt(j) && d[i+1][j-1] == true);
				if (d[i][j] == true && j-i+1 > longestSoFar) {
					longestSoFar = j-i+1;
					ret = s.substring(i, j+1);
				}
			}
			//end for i
			k++;
		}
		//end while
		return ret;
	}

	/****************************************************************************************************************/
	/**
	 * Find the longest palindrome subSEQUENCE in s.
	 * @param s
	 * @return
	 */
	public int longestPalindromeSubseq(String s) {
		//Let d[i][j] = longest possible palindromic subsequence within the ith char and jth char, inclusive.
		/*
		 * Recursive substructure:
		 * d[i][j] = 2 + max(d[i+1][j-1]) if ith and jth chars match, otherwise max of either d[i][j-1] or d[i+1][j]
		 * 
		 * Base cases:
		 * d[i][i] = 1
		 * d[i][i+1] = 2 if charAt(i) == charAt(i+1), otherwise 1.
		 */

		int n = s.length();
		int[][] d = new int[n][n];
		for (int i = 0; i < n; ++i) {
			d[i][i] = 1;
			if (i + 1 < n) {
				d[i][i+1] = (s.charAt(i) == s.charAt(i+1) ? 2 : 1);
			}
		}
		//end for i


		int k = 2;
		while (k < n) {
			for (int i = 0; i < n; ++i) {
				int j = i + k;
				if (j >= n) break;
				d[i][j] = (s.charAt(i) != s.charAt(j) ? Math.max(d[i][j-1], d[i+1][j]) : 2 + d[i+1][j-1]);
			}
			k++;
		}
		//end while

		return d[0][n-1];
	}
	
	/****************************************************************************************************************/
	/* Given a tree (not necessarily binary) of Nodes (containing String values),
	 * find a String that appears at least once on most of the levels of the tree.
	 * 
	 * For ex., for the given tree:
	 *                 a
	 *            a  b   b  b
	 *           c  b b      a
	 * The expected answer is "a", because it appears on all 3 levels of the tree.
	 * */
	class Node {
		LinkedList<Node> children;
		String val;
	}
	
	public String freqStr(Node head) {
	    //Set up map to keep count of "vertical" freq of each String in the tree.
		
		//We'll use breadth-first search to keep track of the strings on each level of the tree.
		//Since we want to only count each string once per level (we don't care about Strings that
		//appear more than once on a given level -- we only care whether a String appears at least once
		//on a level), we'll also use a HashSet in addition to the map.
	    HashMap<String, Integer> map = new HashMap<String, Integer>();
	    HashSet<String> strSet = new HashSet<>();
	    Queue<Node> q = new LinkedList<Node>();  //for BFS
	    q.add(head);
	    int numChildren = 0;  //initialize
	    int numNodes = 1;     //there is only one node at the root level. We'll decrement this by one each step of the below loop.
	    
	    while (!q.isEmpty()) {
	        if (numNodes == 0) {  //This means we have gone down to the next lower level of the tree.
	            for (String str : strSet) {  //Get all Strings that appeared on this level and save their freq to the map
	                Integer num = map.get(str); 
	                if (num == null || num == 0) {
	                    map.put(str, 1);
	                } else {
	                    map.put(str, num+1);
	                }
	            }
	            //end for
	            strSet = new HashSet<>();  //re-initialize the Set to keep track of Strings on the next lower level
	            numNodes = numChildren;    //These are the total no. of nodes on the next lower level
	            numChildren = 0;           //Then re-initialize numChildren to count up the no. of nodes on the next-next lower level
	        }
	        //end if
	        
	        Node curr = q.remove();
	        strSet.add(curr.val);  //Add the String to the HashSet
	        
	        q.addAll(curr.children);  //Add all the children of this node to the Queue 
	        numChildren += curr.children.size();  //increment the numChildren count accordingly
	        numNodes--;
	    }
	    
	    int maxFreq = 0;
	    String ret = "";
	    for (String str : map.keySet()) {
	        if (map.get(str) > maxFreq) {
	            maxFreq = map.get(str);
	            ret = str;
	        }
	    }
	    return ret;
	 }
	
	/****************************************************************************************************************/
	/* Given an array of distinct ints, count how many ways there are to "make change" for N, 
	 * given that you can use an unlimited number of each int.
	 */
	public int count(int[] arr, int N) {
	    
	    /* d[i][j] = total no. of ways to "make change" for the given j, by using an unlimited number of the first i coins */
	    
	    //d[i][0] = 1 for all i
	    //d[0][j] = 0 for all j >= 1
	    
	    //N = 4 , [5, 1, 2]
	    int[][] d = new int[arr.length + 1][N+1];
	    
	    //base cases
	    for (int i = 0; i <= arr.length; ++i) {
	        d[i][0] = 1;
	    }
	    
	    for (int j = 1; j <= N; ++j) {
	        d[0][j] = 0;
	    }
	    
	    for (int i = 1; i <= arr.length; ++i) {
	        for (int j = 1; j <= N; ++j) {
	            //check if the integer value is less than amount needed
	            if (arr[i-1] <= j) {
	                d[i][j] = d[i-1][j] + d[i][j-arr[i-1]];
	            } else {
	                d[i][j] = d[i-1][j];
	            }
	        }
	        //end for j
	    }
	    //end for i
	    return d[arr.length][N];
	}
	
	/****************************************************************************************************************/
	/* Given an array of distinct ints, return a list of all possible ways to "make change" for N
	 * provided you can use an unlimited number of each "coin" in the array. */
	public List<List<Integer>> combinationSum(int[] arr, int N) {
        List<List<Integer>> ret = new LinkedList<>();
        recur(ret, arr, N, 0, new LinkedList<Integer>());
        return ret;
    }
    
    private void recur(List<List<Integer>> ret, int[] arr, int N, int currIndex, LinkedList<Integer> list) {
        if (N == 0 && list.size() > 0) {
            ret.add((LinkedList<Integer>)list.clone());
            return;
        }
        if (N < 0) {
            return;
        }
        
        //N > 0
        for (int i = currIndex; i < arr.length; ++i) {
            list.add(arr[i]);
            recur(ret, arr, N - arr[i], i, list);
            list.remove(list.size() - 1);
        }
    }

    
    public int[] searchRange(int[] nums, int target) {
        int index = Arrays.binarySearch(nums, target);
        if (index < 0) return new int[]{-1,-1};
        
        //We have AN index, but we don't know the left and right indices. Look for both via binary search.
        System.out.println("index : " + index);
        //left index
        int leftIdx = bsearch(nums, 0, index, target, true);
        //right index
        int rightIdx = bsearch(nums, index, nums.length - 1, target, false);
        
        
        return new int[]{leftIdx, rightIdx};
    }
    
    private int bsearch(int[] nums, int low, int hi, int target, boolean findLeftIdx) {
        if (low == hi) return low;
        if (findLeftIdx && low == hi - 1) return (nums[low] == target ? low : hi);
        if (!findLeftIdx && low == hi - 1) return (nums[hi] == target ? hi : low);
        
        int mid = (low + hi) / 2;
        
        if (findLeftIdx) {
            if (nums[mid] == target) {
                if (mid == 0 || nums[mid-1] != target) return mid;
                return bsearch(nums, 0, mid, target, findLeftIdx);
            } else {
                return bsearch(nums, mid, hi, target, findLeftIdx);
            }
        } else {
            if (nums[mid] == target) {
                if (mid == nums.length - 1 || nums[mid+1] != target) return mid;
                return bsearch(nums, mid, hi, target, findLeftIdx);
            } else {
                return bsearch(nums, low, mid, target, findLeftIdx);
            }
        }
    }
    
    public List<List<Integer>> threeSum(int[] nums) {
        HashSet<List<Integer>> ret = new HashSet<>();
        Arrays.sort(nums);
        
        for (int i = 0; i < nums.length; ++i) {
            for (int j = i+1; j < nums.length; ++j) {
                int twosum = nums[i] + nums[j];
                int target = 0 - twosum;
                System.out.println(Arrays.binarySearch(nums, j+1, nums.length, target));
                if (Arrays.binarySearch(nums, j+1, nums.length, target) >= 0) {
                    int[] arr = {nums[i], nums[j], -twosum};
                    Arrays.sort(arr);
                    
                    List<Integer> list = new LinkedList<>();
                    list.add(arr[0]); list.add(arr[1]); list.add(arr[2]);
                    ret.add(list);
                }
            }   //end for j
        }    //end for i
        List<List<Integer>> retList = new LinkedList<>();
        for (List<Integer> list : ret) retList.add(list);
        return retList;
    }
    
    // This is the text editor interface. 
    // Anything you type or change here will be seen by the other person in real time.

    //Input: List<List<Integer>>, where each list is sorted
    //Output: List<Integer>, sorted.
    //n = size of the input, aka the number of lists in the list<list>
    //m = the total number of integers that is in the list of lists
    //O(m log n)

    //Example: [[1 2 3 4 10] [0 2 4 8]]
    //4, iter = [4 8], [0 2]
    //
    public List<Integer> merge(List<List<Integer>> list) {
    	//base cases
    	// List<Integer> ret = new LinkedList<>();

    	if (list == null || list.size() == 0) return new LinkedList<>();

    	while (list.size() > 1) {
    		int iter = list.size() / 2;

    		for (int i = 0; i < iter; ++i) {
    			List<Integer> l1 = list.get(0);
    			List<Integer> l2 = list.get(1);
    			list.remove(1);
    			list.remove(0);
    			List<Integer> merged = mergePair(l1, l2);
    			list.add(merged);
    		}//for 
    	}//while
    		return list.get(0);
    }
    //[1/ 4 10] [2/ 3/]
    //max=3 , ret = [1 2 3 4 10]
    private List<Integer> mergePair(List<Integer> l1, List<Integer> l2) {
    	//assumes both lists are nonempty, and sorted
    	List<Integer> ret = new LinkedList<>();

    	int maxLen = Math.max(l1.size(), l2.size());
    	int index_l1 = 0;
    	int index_l2 = 0;
    	while (index_l1 < l1.size() || index_l2 < l2.size()) {
    		if (index_l1 >= l1.size()) {
    			ret.add(l2.get(index_l2++));
    		} else if (index_l2 >= l2.size()) {
    			ret.add(l1.get(index_l1++));
    		} else {
    			Integer int1 = l1.get(index_l1);
    			Integer int2 = l2.get(index_l2);
    			if (int1 <= int2) {
    				ret.add(int1);
    				index_l1++;
    			} else {
    				ret.add(int2);
    				index_l2++;
    			}
    		}//if/else
    	}//while
    	return ret;
    }

    
	public static void main(String[] args) {
		//Binary search on rotated sorted array
		System.out.println("Binary search on sorted rotated array:");
		int[] arr = {0, 1, 5, 6, 8, 11, 12, 13, 20, -3};
		System.out.println(Test.search(arr, 8));

		//Derangements
		System.out.println("Derangements:");
		for (int i = 4; i <= 20; ++i) {
			System.out.println(Test.getDerangements(i));
		}

		Test t = new Test();
		System.out.println(t.combinationSum(new int[] {1,3,5,2,10,7}, 6));
		
		int[] ret = t.searchRange(new int[] {2,2,3,3}, 3);
		for (int n : ret) {
			System.out.print(n + " ");
		}
		
		System.out.println(t.threeSum(new int[] {-4,-2,1,-5,-4,-4,4,-2,0,4,0,-2,3,1,-5,0}));
		
		//=====================================================================================
//		public class Blahblah implements Comparable<Blahblah> {
//			
//			@Override
//	        public int compareTo(Blahblah bl) {
//	            return this.value - bl.value;
//	        }
//		}
		System.out.println("Testing LinkedList's addLast, removeLast, etc.");
		LinkedList<Integer> ll = new LinkedList<>();
		ll.addLast(3);	ll.addLast(5);	ll.removeLast(); ll.removeFirst();
		
		System.out.println("Testing HashSet's containsAll() along with Arrays.asList...");
		HashSet<Integer> hs = new HashSet<>();
		
		if (hs.containsAll(new ArrayList<Integer>(Arrays.asList(1,2,3)))) { };
		
		System.out.println("Testing hashMap's getOrDefault()...");
	    HashMap<Integer, Integer> hm = new HashMap<>();
	    hm.put(1, 4);
	    hm.put(2, hm.getOrDefault(2, 0) + 1);  //Adds the map {2 = 1}
	    System.out.println(hm);  //prints out {1=4, 2=1}
	    
	    //Sorting treemap by key
	    TreeMap<String, Integer> tm = new TreeMap<>(Collections.reverseOrder());
	    tm.put("Alice", 10); tm.put("Bryan", 5); tm.put("Charlie", 3); tm.put("Darryl", 12);
	    for (String s : tm.keySet()) System.out.print(s + " ");
	    for (Integer i : tm.values()) System.out.print(i + " ");
	    System.out.println();
	    
	    //Sorting Map by value : just get all the values, then sort them.	    
	    System.out.println("Testing .subList...");
	    ArrayList<Integer> al = new ArrayList<>(Arrays.asList(0,1,2,3,4,5));
	    System.out.println(al.subList(0, 3));  //prints out {0,1,2}
	    List<Integer> L = new LinkedList<>();
	    L.add(1); L.add(2);
	    System.out.println("Max PriorityQueue");
	    PriorityQueue<Integer> pq = new PriorityQueue<>(10, Collections.reverseOrder());
	    pq.add(1); pq.add(3); pq.add(2); pq.add(100); pq.add(0);
	    Integer[] tmpArr = new Integer[]{5,1,2,8,-1};  //Note: using int[] doesn't work with the code below.
	    pq.addAll(new ArrayList<Integer>(Arrays.asList(tmpArr)));
	    pq = new PriorityQueue<>(10, Collections.reverseOrder());
	    Integer[] tmpArr2 = {1,3,5,-2,0};
	    pq.addAll(new ArrayList(Arrays.asList(tmpArr2)));  //
	    while (!pq.isEmpty()) System.out.println(pq.remove());
	    
	    ArrayList<Integer>[] alArr = new ArrayList[3];  //unavoidable cast warning when initializing array of List
//	    Arrays.binarySearch(int[] a, fromIndex, toIndex(exclusive), key);
	    
	    
	    
	    Test ts = new Test();
	    
	    List<List<Integer>> list = new LinkedList<>();
	    list.add(new LinkedList<>(Arrays.asList(1,3,6,9)));
	    list.add(new LinkedList<>(Arrays.asList(5,6,8,10)));
	    list.add(new LinkedList<>(Arrays.asList(3,6,7,9)));
	    List<Integer> merged = ts.merge(list);
	    System.out.println(merged);
	    
	    String str = "STATION,STATION_NAME,ELEVATION,LATITUDE,LONGITUDE,DATE,REPORTTPYE,HOURLYSKYCONDITIONS,HOURLYVISIBILITY,HOURLYPRSENTWEATHERTYPE,HOURLYDRYBULBTEMPF,HOURLYDRYBULBTEMPC,HOURLYWETBULBTEMPF,HOURLYWETBULBTEMPC,HOURLYDewPointTempF,HOURLYDewPointTempC,HOURLYRelativeHumidity,HOURLYWindSpeed,HOURLYWindDirection,HOURLYWindGustSpeed,HOURLYStationPressure,HOURLYPressureTendency,HOURLYPressureChange,HOURLYSeaLevelPressure,HOURLYPrecip,HOURLYAltimeterSetting,DAILYMaximumDryBulbTemp,DAILYMinimumDryBulbTemp,DAILYAverageDryBulbTemp,DAILYDeptFromNormalAverageTemp,DAILYAverageRelativeHumidity,DAILYAverageDewPointTemp,DAILYAverageWetBulbTemp,DAILYHeatingDegreeDays,DAILYCoolingDegreeDays,DAILYSunrise,DAILYSunset,DAILYWeather,DAILYPrecip,DAILYSnowfall,DAILYSnowDepth,DAILYAverageStationPressure,DAILYAverageSeaLevelPressure,DAILYAverageWindSpeed,DAILYPeakWindSpeed,PeakWindDirection,DAILYSustainedWindSpeed,DAILYSustainedWindDirection,MonthlyMaximumTemp,MonthlyMinimumTemp,MonthlyMeanTemp,MonthlyAverageRH,MonthlyDewpointTemp,MonthlyWetBulbTemp,MonthlyAvgHeatingDegreeDays,MonthlyAvgCoolingDegreeDays,MonthlyStationPressure,MonthlySeaLevelPressure,MonthlyAverageWindSpeed,MonthlyTotalSnowfall,MonthlyDeptFromNormalMaximumTemp,MonthlyDeptFromNormalMinimumTemp,MonthlyDeptFromNormalAverageTemp,MonthlyDeptFromNormalPrecip,MonthlyTotalLiquidPrecip,MonthlyGreatestPrecip,MonthlyGreatestPrecipDate,MonthlyGreatestSnowfall,MonthlyGreatestSnowfallDate,MonthlyGreatestSnowDepth,MonthlyGreatestSnowDepthDate,MonthlyDaysWithGT90Temp,MonthlyDaysWithLT32Temp,MonthlyDaysWithGT32Temp,MonthlyDaysWithLT0Temp,MonthlyDaysWithGT001Precip,MonthlyDaysWithGT010Precip,MonthlyDaysWithGT1Snow,MonthlyMaxSeaLevelPressureValue,MonthlyMaxSeaLevelPressureDate,MonthlyMaxSeaLevelPressureTime,MonthlyMinSeaLevelPressureValue,MonthlyMinSeaLevelPressureDate,MonthlyMinSeaLevelPressureTime,MonthlyTotalHeatingDegreeDays,MonthlyTotalCoolingDegreeDays,MonthlyDeptFromNormalHeatingDD,MonthlyDeptFromNormalCoolingDD,MonthlyTotalSeasonToDateHeatingDD,MonthlyTotalSeasonToDateCoolingDD";
	    String[] strArr = str.split(",");
	    System.out.println(strArr.length);
	    
	}
}
