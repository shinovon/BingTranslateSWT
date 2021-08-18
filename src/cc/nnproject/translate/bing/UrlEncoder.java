package cc.nnproject.translate.bing;

/**
 * @author Shinovon
 *
 */
public final class UrlEncoder {

	public static String encode(String s) {
		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if ((65 <= ch) && (ch <= 90)) {
				sbuf.append((char) ch);
			} else if ((97 <= ch) && (ch <= 122)) {
				sbuf.append((char) ch);
			} else if ((48 <= ch) && (ch <= 57)) {
				sbuf.append((char) ch);
			} else if (ch == 32) {
				sbuf.append("%20");
			} else if ((ch == 45) || (ch == 95) || (ch == 46) || (ch == 33) || (ch == 126) || (ch == 42) || (ch == 39)
					|| (ch == 40) || (ch == 41)/* || (ch == 58) || (ch == 47)*/) {
				sbuf.append((char) ch);
			} else if (ch <= 127) {
				sbuf.append(hex(ch));
			} else if (ch <= 2047) {
				sbuf.append(hex(0xC0 | ch >> 6));
				sbuf.append(hex(0x80 | ch & 0x3F));
			} else {
				sbuf.append(hex(0xE0 | ch >> 12));
				sbuf.append(hex(0x80 | ch >> 6 & 0x3F));
				sbuf.append(hex(0x80 | ch & 0x3F));
			}
		}
		return sbuf.toString();
	}

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%" + (s.length() < 2 ? "0" : "") + s;
	}
	
	// юзается в about
	public static final String uwu = ").(";
	
	public static String d() {
		StringBuffer sb = new StringBuffer();
		sb.append(uwu);
		sb.append((char) (TranslateUI.langsAlias[0].charAt(1) -3));
		sb.append((char) (TranslateUI.langsAlias[0].charAt(0) -3));
		sb.append(TranslateUI.langsAlias[9].charAt(0));
		sb.append(TranslateThread.e());
		return sb.toString();
	}
}