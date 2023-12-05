/*
Copyright (c) 2023 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package cc.nnproject.json;

import java.util.Hashtable;
import java.util.Vector;

/**
 * JSON Library compatible with CLDC 1.1 & JDK 1.1<br>
 * Usage:<p><code>JSONObject obj = JSON.getObject(str);</code>
 * @author Shinovon
 * @version 2.0
 */
public final class JSON {

	/**
	 * Parse all members once
	 */
	public static final boolean parse_members = false;
	
	public static final Object json_null = new Object();

	public static final String FORMAT_TAB = "  ";
	
	public static final Boolean TRUE = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);

	public static JSONObject getObject(String string) throws JSONException {
		if (string == null || string.length() <= 1)
			throw new JSONException("Empty string");
		if (string.charAt(0) != '{')
			throw new JSONException("Not JSON object");
		return (JSONObject) parseJSON(string);
	}

	public static JSONArray getArray(String string) throws JSONException {
		if (string == null || string.length() <= 1)
			throw new JSONException("Empty string");
		if (string.charAt(0) != '[')
			throw new JSONException("Not JSON array");
		return (JSONArray) parseJSON(string);
	}

	static Object getJSON(Object obj) throws JSONException {
		if (obj instanceof Hashtable) {
			return new JSONObject((Hashtable) obj);
		}
		if (obj instanceof Vector) {
			return new JSONArray((Vector) obj);
		}
		if (obj == null) {
			return json_null;
		}
		return obj;
	}

	static Object parseJSON(String str) throws JSONException {
		if (str == null || (str = str.trim()).length() == 0) {
			throw new JSONException("Empty string");
		}
		char first = str.charAt(0);
		int length = str.length() - 1;
		char last = str.charAt(length);
		if (first == '{' && last != '}' || first == '[' && last != ']' || first == '"' && last != '"') {
			throw new JSONException("Unexpected end of text");
		} else if (first == '"') {
			// String
			str = str.substring(1, str.length() - 1);
			char[] chars = str.toCharArray();
			str = null;
			try {
				int l = chars.length;
				StringBuffer sb = new StringBuffer();
				int i = 0;
				// Parse string escape chars
				loop: {
					while (i < l) {
						char c = chars[i];
						switch (c) {
						case '\\': {
							next: {
								replaced: {
									if (l < i + 1) {
										sb.append(c);
										break loop;
									}
									char c1 = chars[i + 1];
									switch (c1) {
									case 'u':
										i+=2;
										String u = "" + chars[i++] + chars[i++] + chars[i++] + chars[i++];
										sb.append((char) Integer.parseInt(u, 16));
										break replaced;
									case 'x':
										i+=2;
										String x = "" + chars[i++] + chars[i++];
										sb.append((char) Integer.parseInt(x, 16));
										break replaced;
									case 'n':
										sb.append('\n');
										i+=2;
										break replaced;
									case 'r':
										sb.append('\r');
										i+=2;
										break replaced;
									case 't':
										sb.append('\t');
										i+=2;
										break replaced;
									case 'f':
										sb.append('\f');
										i+=2;
										break replaced;
									case 'b':
										sb.append('\b');
										i+=2;
										break replaced;
									case '\"':
									case '\'':
									case '\\':
									case '/':
										i+=2;
										sb.append((char) c1);
										break replaced;
									default:
										break next;
									}
								}
								break;
							}
							sb.append(c);
							i++;
							break;
						}
						default:
							sb.append(c);
							i++;
						}
					}
				}
				str = sb.toString();
				sb = null;
			} catch (Exception e) {
			}

			return str;
		} else if (first != '{' && first != '[') {
			if (str.equals("null"))
				return json_null;
			if (str.equals("true"))
				return TRUE;
			if (str.equals("false"))
				return FALSE;
			if (str.length() > 2 && str.charAt(0) == '0' && str.charAt(1) == 'x') {
				try {
					return new Integer(Integer.parseInt(str.substring(2), 16));
				} catch (Exception e) {
					try {
						return new Long(Long.parseLong(str.substring(2), 16));
					} catch (Exception e2) {
					}
				}
			}
			try {
				return new Integer(Integer.parseInt(str));
			} catch (Exception e) {
				try {
					return new Long(Long.parseLong(str));
				} catch (Exception e2) {
					try {
						return new Double(Double.parseDouble(str));
					} catch (Exception e3) {
					}
				}
			}
			return str;
		} else {
			// Parse json object or array
			int unclosed = 0;
			boolean object = first == '{';
			int i = 1;
			char nextDelimiter = object ? ':' : ',';
			boolean escape = false;
			String key = null;
			Object res = null;
			if (object) res = new Hashtable();
			else res = new Vector();
			
			for (int splIndex; i < length; i = splIndex + 1) {
				// skip all spaces
				for (; i < length - 1 && str.charAt(i) <= ' '; i++);

				splIndex = i;
				boolean quotes = false;
				for (; splIndex < length && (quotes || unclosed > 0 || str.charAt(splIndex) != nextDelimiter); splIndex++) {
					char c = str.charAt(splIndex);
					if (!escape) {
						if (c == '\\') {
							escape = true;
						} else if (c == '"') {
							quotes = !quotes;
						}
					} else escape = false;
	
					if (!quotes) {
						if (c == '{' || c == '[') {
							unclosed++;
						} else if (c == '}' || c == ']') {
							unclosed--;
						}
					}
				}

				if (quotes || unclosed > 0) {
					throw new JSONException("Corrupted JSON");
				}

				if (object && key == null) {
					key = str.substring(i, splIndex);
					key = key.substring(1, key.length() - 1);
					nextDelimiter = ',';
				} else {
					Object value = str.substring(i, splIndex).trim();
					if (parse_members) value = parseJSON(value.toString());
					else value = new JSONString(value.toString());
					if (object) {
						((Hashtable) res).put(key, value);
						key = null;
						nextDelimiter = ':';
					} else if (splIndex > i) ((Vector) res).addElement(value);
				}
			}
			return getJSON(res);
		}
	}
	
	public static boolean isNull(Object obj) {
		return obj == json_null || obj == null;
	}

	public static String escape_utf8(String s) {
		int len = s.length();
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < len) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
			case '\\':
				sb.append("\\" + c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				if (c < 32 || c > 1103) {
					String u = Integer.toHexString(c);
					for (int z = u.length(); z < 4; z++) {
						u = "0" + u;
					}
					sb.append("\\u" + u);
				} else {
					sb.append(c);
				}
			}
			i++;
		}
		return sb.toString();
	}

	public static double getDouble(Object o) throws JSONException {
		if (isNull(o)) throw new JSONException("null");
		try {
			if (o instanceof Integer)
				return ((Integer) o).intValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
			if (o instanceof Double)
				return ((Double) o).doubleValue();
		} catch (Throwable e) {
		}
		throw new JSONException("Value cast failed: " + o);
	}

	public static long getLong(Object o) throws JSONException {
		if (isNull(o)) throw new JSONException("null");
		try {
			if (o instanceof Integer)
				return ((Integer) o).longValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
			if (o instanceof Double)
				return ((Double) o).longValue();
		} catch (Throwable e) {
		}
		throw new JSONException("Value cast failed: " + o);
	}

}
