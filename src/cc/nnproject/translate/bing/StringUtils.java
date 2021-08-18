package cc.nnproject.translate.bing;
import java.util.Vector;

/**
 * Universal String Utils for J2ME
 * @author Shinovon
 */
public class StringUtils {

	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static String replaceIgnoreCase(String str, String from, String to) {
		String low = str.toLowerCase();
		int j = low.indexOf(from = from.toLowerCase());
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = low.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static String cut(String str, String find) {
		return replace(str, find, "");
	}

	public static String cut(String str, String[] cl) {
		for (int i = 0; i < cl.length; i++) {
			str = cut(str, cl[i]);
		}
		return str;
	}

	public static boolean contains(String str, String find) {
		return str.indexOf(find) != -1;
	}

	public static boolean containsIgnoreCase(String str, String find) {
		return str.toLowerCase().indexOf(find.toLowerCase()) != -1;
	}

	public static boolean safeEquals(String str, String str2) {
		return str.length() == str2.length() && str.indexOf(str2) == 0;
	}

	public static boolean equalsIgnoreCase(String str, String str2) {
		return safeEquals(str.toLowerCase(), str2.toLowerCase());
	}

	public static String cutIgnoreCase(String str, String cl) {
		return replaceIgnoreCase(str, cl, "");
	}

	public static boolean startsWithIgnoreCase(String str, String need) {
		return equalsIgnoreCase(str.substring(0, need.length()), need);
	}
	
	public static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + 1);
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}
	
	public static String[] split(String str, String d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + d.length());
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}

	public static int count(String in, char t) {
		int r = 0;
		char[] c = in.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == t) r++;
		}
		return r;
	}

	public static int count(String str, String f) {
		int i = str.indexOf(f);
		int c = 0;
		while (i != -1) {
			str = str.substring(i + f.length());
			c++;
			i = str.indexOf(f);
		}
		return c;
	}

	public static String[] splitSingle(String str, String d) {
		int i = str.indexOf(d);
		return new String[] { str.substring(0, i), str.substring(i + d.length()) };
	}

	public static String[] splitSingle(String str, char d) {
		int i = str.indexOf(d);
		return new String[] { str.substring(0, i), str.substring(i + 1) };
	}

}
