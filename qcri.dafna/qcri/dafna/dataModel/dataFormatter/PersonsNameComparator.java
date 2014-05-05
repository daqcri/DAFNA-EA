package qcri.dafna.dataModel.dataFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import qcri.dafna.dataModel.data.Globals;

public class PersonsNameComparator {
	/**
	 * Checking for the exact names list, regardless of the order.
	 * @param namesList1 The first list of names
	 * @param namesList2 The second list of names
	 * @return True is the two lists contain the same set of names, false otherwise.
	 */
	static boolean sameNames(String namesList1, String namesList2) {
		String[] n1 = namesList1.split(Globals.cleanedListDelimiter);
		String[] tempN2 = namesList2.split(Globals.cleanedListDelimiter);
		List<String> n2 = new ArrayList<String>(Arrays.asList(tempN2));
		if (n1.length != tempN2.length) {
			return false;
		}
		boolean found = true;
		for (String name1 : n1) {
			if (!found) {
				return false;
			}
			found = false;
			for (int i = 0; i < n2.size(); i++) {
				if (sameName(name1, n2.get(i)) > 0) {
					found = true;
					// to remove the conflicts if names are stated twice in the same claim.
					n2.remove(i);
					break;
				}
			}
		}
		return found;
	}

	/**
	 * compare the two names. Check whether one of the name is only the initial, the abbreviation or the exact match of the second name.
	 * @param name1
	 * @param name2
	 * @return
	 */
	private static boolean sameNamePartOrInitial(String name1, String name2) {
		if (name1.equals(name2)) {
			return true;
		}
		if (name1.length() == 1 && name2.startsWith(name1)) {
			return true;
		}
		if (name2.length() == 1 && name1.startsWith(name2)) {
			return true;
		}
		if (name1.contains(name2) || name2.contains(name1)) {
			return true;
		}
		return false;
	}
	/**
	 * return 1 if the names are exact,
	 * return 0.5 if one name is only the first(or last) name as the second name.
	 * return 0 otherwise;
	 * @param name1
	 * @param name2
	 * @return
	 */
	public static double sameName(String name1, String name2) {
		String[] parts1,parts2;
		String fname1 = "", lname1 = "",fname2 = "", lname2 = "";

		parts1 = name1.split(" ");
		if (parts1.length > 0) {
			fname1 = parts1[0];
			if (parts1.length > 1) {
				lname1 = parts1[1];
			}
		}
		parts2 = name2.split(" ");
		if (parts2.length > 0) {
			fname2 = parts2[0];
			if (parts2.length > 1) {
				lname2 = parts2[1];
			}
		}

		fname1 = fname1.replace(".", "");
		lname1 = lname1.replace(".", "");
		fname2 = fname2.replace(".", "");
		lname2 = lname2.replace(".", "");

		if (fname1.equals(fname2) && lname1.equals(lname2)) {
			return 1;
		}
		// assume the two names have the same order
		if ( (sameNamePartOrInitial(fname1, fname2) && lname1.equals(lname2)) ||
				(sameNamePartOrInitial(lname1, lname2) && fname1.equals(fname2)) ){
			return 1;
		}
		// assume the two names have different order
		if ( (sameNamePartOrInitial(fname1, lname2) && lname1.equals(fname2)) ||
				(sameNamePartOrInitial(lname1, fname2) && lname2.equals(fname1))){
			return 1;
		}
		if (fname1.equals(fname2) && 
				((lname1.isEmpty() || lname2.isEmpty()))) {
			return 0.5;
		}
		// if the name is missing the last name.
		if (fname1.equals(lname2) && lname1.isEmpty()) {
			return 0.5;
		}
		if (fname2.equals(lname1) && lname2.isEmpty()) {
			return 0.5;
		}
		return 0;
	}

	public static double computePersonsNamesListSimilarity(String names1, String names2) {
		double common = 0, x = 0;
		double result;
		/**
		 * Similarity between value1 and value2 = influence of value2 on value1  
		 * = number of common names / number of names in value2  
		 */
		String[] n1 = names1.split(Globals.cleanedListDelimiter);
		String[] tempN2 = names2.split(Globals.cleanedListDelimiter);
		List<String> n2 = new ArrayList<String>(Arrays.asList(tempN2));
		x = n2.size();
		if (n1.length == 2 && ( sameName(n1[1], "et al")==1 || sameName(n1[1], "al et")==1)) {
			if (tempN2.length > 1 &&  sameName(tempN2[1], "et al")==0 && sameName(tempN2[1], "al et")==0) {
				for (int i = 0; i < n2.size(); i ++) {
					if (sameName(n1[0], n2.get(i)) > 0) {
						return 1;
					}
				}
			}

		}
		double d;
		for (String name1 : n1) {
			for (int i = 0; i < n2.size(); i ++) {
				d = sameName(name1, n2.get(i));
				if (d > 0) {
					common = common + d;
					n2.remove(i);
					break;
				}
			}
		}
		result = common/x;
		return result;
	}

	public static int commonCount(String names1, String names2) {
		return commonCount(Arrays.asList(names1), Arrays.asList(names2));
	}
	public static int commonCount(List<String> names1, List<String> names2) {
		int common = 0;
		double d;
		for (String name1 : names1) {
			for (int i = 0; i < names2.size(); i ++) {
				d = sameName(name1, names2.get(i));
				if (d > 0) {
					common++;
					break;
				}
			}
		}
		return common;
	}

	public static boolean containName(List<String> namesList, String name) {
		double d;
			for (int i = 0; i < namesList.size(); i ++) {
				d = sameName(name, namesList.get(i));
				if (d > 0) {
					return true;
				}
			}
		return false;
	}

	public static double computeJaccardListofNames(String list1, String list2) {
		List<String> names1 = Arrays.asList(list1.split(Globals.cleanedListDelimiter));
		List<String> names2 = new ArrayList<String>(Arrays.asList(list2.split(Globals.cleanedListDelimiter)));
		int size2 = names2.size();
		int common = 0;
		double d;
		for (String name1 : names1) {
			for (int i = 0; i < names2.size(); i ++) {
				d = sameName(name1, names2.get(i));
				if (d > 0) {
					common++;
					names2.remove(i);
					break;
				}
			}
		}
		d = names1.size() + size2 - common;
		return (double)common/d;
	}

	/**
	 * Add the new names in the namesToAdd string into the give list object.
	 * @param oldNames
	 * @param namesToAdd
	 */
//	public static void addAllNames(List<String> oldNames, String namesToAdd) {
//		List<String> newNames = new ArrayList<String>();
//		double d;
//		String[] n1 = namesToAdd.split(Globals.cleanedListDelimiter);
//
//		for (String name1 : n1) {
//			for (int i = 0; i < oldNames.size(); i ++) {
//				d = sameName(name1, oldNames.get(i));
//				if (d > 0) { // name exist in the list
//					continue;
//				} else { // name does not exist in the list
//					newNames.add(name1);
//				}
//			}
//		}
//		oldNames.addAll(newNames);
//	}
}
