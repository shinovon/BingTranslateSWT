package cc.nnproject.translate;

import java.util.Vector;

import cc.nnproject.translate.bing.TranslateBingThread;
import cc.nnproject.translate.bing.swt.TranslateUIBingSWT;

/**
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

	public static String cut(String str, String find) {
		return replace(str, find, "");
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
	
	// about part
	public static void aa(StringBuffer sb) {
		sb.append(TranslateUIBingSWT.langsAlias[7].charAt(0));
		sb.append(TranslateUIBingSWT.e.charAt(1));
		sb.append(TranslateBingThread.e());
		sb.append('!');
		sb.append(TranslateUIBingSWT.langsAlias[5].charAt(1));
		sb.append(TranslateBingThread.e());
		sb.append(TranslateUIBingSWT.langsAlias[7].charAt(0));
	}
	
	public static char g() {
		return 'p';
	}

}
