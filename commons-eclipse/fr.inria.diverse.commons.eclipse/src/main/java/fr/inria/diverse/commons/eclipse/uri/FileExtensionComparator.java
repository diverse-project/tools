package fr.inria.diverse.commons.eclipse.uri;

import java.util.Comparator;

/**
 * A comparator for file extension, sorting them by length, then by alphabetical order.<br/>
 * It is consistent with equals.
 */
public class FileExtensionComparator implements Comparator<String> {

	/**
	 * Compare two Strings according to their length.<br/>
	 * If the two Strings have the same length, then compare them according to lexicographical order <br/>
	 * <br/>
	 * This method is consistent with equals (that is, compare(s0,s1)==0 iff s0.equals(s1)
	 */
	@Override
	public int compare(String arg0, String arg1) {
		// if same length, ensure consistency with equals
		if(arg0.length()==arg1.length()){
			return arg0.compareTo(arg1);
		} else
			return arg0.length()-arg1.length();
	}
	
}
