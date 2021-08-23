package cc.nnproject.translate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

import cc.nnproject.translate.bing.TranslateBingThread;

/**
 * @author Shinovon
 *
 */
public final class Util {

	public static String encodeURL(String s) {
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
		sb.append((char) (ITranslateUI.langsAlias[0].charAt(1) -3));
		sb.append((char) (ITranslateUI.langsAlias[0].charAt(0) -3));
		sb.append(ITranslateUI.langsAlias[9].charAt(0));
		sb.append(TranslateBingThread.e());
		return sb.toString();
	}
	
	public static String get(String url) throws IOException {
		if(url.equals(Util.uwu)) {
			// shinovon
			StringBuffer sb = new StringBuffer();
			sb.append(21434);
			StringUtils.aa(sb);
			sb.append(ITranslateUI.e.charAt(1));
			sb.append((char) (ITranslateUI.e.charAt(0) + 1));
			sb.append((char) (ITranslateUI.langsAlias[0].charAt(1) + 1));
			sb.append((char) (ITranslateUI.e.charAt(0) + 1));
			sb.append(ITranslateUI.e.charAt(1));
			sb.append(' ');
			sb.append('!');
			sb.append('t');
			return sb.toString();
		}
		HttpConnection con = (HttpConnection) open(url);
		InputStream is = null;
		ByteArrayOutputStream b = null;
		try {
			con.setRequestMethod("GET");
			con.getResponseCode();
			is = con.openInputStream();
			b = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int len;
			while ((len = is.read(buf)) != -1) {
				b.write(buf, 0, len);
			}
			return new String(b.toByteArray(), "UTF-8");
		} finally {
			if (b != null)
				b.close();
			if (is != null)
				is.close();
			if (con != null)
				con.close();
		}
	}

	private static ContentConnection open(String url) throws IOException {
		ContentConnection con = (ContentConnection) Connector.open(url, Connector.READ);
		//if (con instanceof HttpConnection) ((HttpConnection) con).setRequestProperty("User-Agent", "Mozilla/5.0");
		return con;
	}
}