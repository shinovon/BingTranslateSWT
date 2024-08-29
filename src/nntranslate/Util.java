/*
 * Copyright (c) 2021-2024 Arman Jussupgaliyev
 */
package nntranslate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

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
	
	public static String get(String url) throws IOException {
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
			if(b != null) b.close();
			if(is != null) is.close();
			if(con != null) con.close();
		}
	}

	private static ContentConnection open(String url) throws IOException {
		ContentConnection con = (ContentConnection) Connector.open(url);
		((HttpConnection) con).setRequestProperty("User-Agent", "TranslateApp/2.0");
		return con;
	}
	
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
}