/*
 * Copyright (c) 2021-2026 Arman Jussupgaliyev
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;

public class Translate extends MIDlet {
	
	public static final String[][] LANGUAGES = new String[][] {
		{"af", "Afrikaans"},
		{"az", "Azərbaycan (Azerbaijani)"},
		{"ba", "Bashkir"},
		{"bs", "Bosnian"},
		{"ca", "Català (Catalan)"},
		{"cy", "Cymraeg (Welsh)"},
		{"da", "Dansk (Danish)"},
		{"de", "Deutsch (German)"},
		{"et", "Eesti (Estonian)"},
		{"en", "English"},
		{"es", "Español (Spanish)"},
		{"fil", "Filipino"},
		{"fr-CA", "Français (Canada)"},
		{"fr", "Français (French)"},
		{"ga", "Gaeilge (Irish)"},
		{"sm", "Gagana Sāmoa (Samoan)"},
		{"ht", "Haitian Creole"},
		{"mww", "Hmong Daw"},
		{"hr", "Hrvatski (Croatian)"},
		{"otq", "Hñähñu (Querétaro Otomi)"},
		{"id", "Indonesia (Indonesian)"},
		{"it", "Italiano (Italian)"},
		{"sw", "Kiswahili (Swahili)"},
		{"tlh-Latn", "Klingon (Latin)"},
		{"tlh-Piqd", "Klingon (pIqaD)"},
		{"kmr", "Kurdî (Bakur) / Kurdish (Northern)"},
		{"ku", "Kurdî (Navîn) / Kurdish (Central)"},
		{"ky", "Kyrgyz"},
		{"lv", "Latviešu (Latvian)"},
		{"to", "Lea Fakatonga (Tongan)"},
		{"lt", "Lietuvių (Lithuanian)"},
		{"hu", "Magyar (Hungarian)"},
		{"mg", "Malagasy"},
		{"mt", "Malti (Maltese)"},
		{"ms", "Melayu (Malay)"},
		{"mn-Cyrl", "Mongolian (Cyrillic)"},
		{"fj", "Na Vosa Vakaviti (Fijian)"},
		{"nl", "Nederlands (Dutch)"},
		{"nb", "Norsk Bokmål (Norwegian)"},
		{"pl", "Polski (Polish)"},
		{"pt", "Português (Brasil)"},
		{"pt-PT", "Português (Portugal)"},
		{"ty", "Reo Tahiti (Tahitian)"},
		{"ro", "Română (Romanian)"},
		{"sq", "Shqip (Albanian)"},
		{"sk", "Slovenčina (Slovak)"},
		{"sl", "Slovenščina (Slovenian)"},
		{"sr-Latn", "Srpski (latinica) / Serbian (Latin)"},
		{"fi", "Suomi (Finnish)"},
		{"sv", "Svenska (Swedish)"},
		{"mi", "Te Reo Māori (Māori)"},
		{"vi", "Tiếng Việt (Vietnamese)"},
		{"tk", "Türkmen Dili (Turkmen)"},
		{"tr", "Türkçe (Turkish)"},
		{"uz", "Uzbek (Latin)"},
		{"yua", "Yucatec Maya"},
		{"is", "Íslenska (Icelandic)"},
		{"cs", "Čeština (Czech)"},
		{"el", "Ελληνικά (Greek)"},
		{"bg", "Български (Bulgarian)"},
		{"mk", "Македонски (Macedonian)"},
		{"ru", "Русский (Russian)"},
		{"sr-Cyrl", "Српски (ћирилица) / Serbian (Cyrillic)"},
		{"tt", "Татар (Tatar)"},
		{"uk", "Українська (Ukrainian)"},
		{"kk", "Қазақ Тілі (Kazakh)"},
		{"hy", "Հայերեն (Armenian)"},
		{"he", "עברית (Hebrew)"},
		{"ug", "ئۇيغۇرچە (Uyghur)"},
		{"ur", "اردو (Urdu)"},
		{"ar", "العربية (Arabic)"},
		{"prs", "دری (Dari)"},
		{"fa", "فارسی (Persian)"},
		{"ps", "پښتو (Pashto)"},
		{"dv", "ދިވެހިބަސް (Divehi)"},
		{"ne", "नेपाली (Nepali)"},
		{"mr", "मराठी (Marathi)"},
		{"hi", "हिन्दी (Hindi)"},
		{"as", "অসমীয়া (Assamese)"},
		{"bn", "বাংলা (Bangla)"},
		{"pa", "ਪੰਜਾਬੀ (Punjabi)"},
		{"gu", "ગુજરાતી (Gujarati)"},
		{"or", "ଓଡ଼ିଆ (Odia)"},
		{"ta", "தமிழ் (Tamil)"},
		{"te", "తెలుగు (Telugu)"},
		{"kn", "ಕನ್ನಡ (Kannada)"},
		{"ml", "മലയാളം (Malayalam)"},
		{"th", "ไทย (Thai)"},
		{"lo", "ລາວ (Lao)"},
		{"bo", "བོད་སྐད་ (Tibetan)"},
		{"my", "မြန်မာ / Myanmar (Burmese)"},
		{"ka", "ქართული (Georgian)"},
		{"ti", "ትግር (Tigrinya)"},
		{"am", "አማርኛ (Amharic)"},
		{"iu", "ᐃᓄᒃᑎᑐᑦ (Inuktitut)"},
		{"km", "ខ្មែរ (Khmer)"},
		{"mn-Mong", "ᠮᠣᠩᠭᠣᠯ ᠬᠡᠯᠡ / Mongolian (Traditional)"},
		{"lzh", "中文 (文言文) / Chinese (Literary)"},
		{"zh-Hans", "中文 (简体) / Chinese Simplified"},
		{"ja", "日本語 (Japanese)"},
		{"yue", "粵語 (繁體) / Cantonese (Traditional)"},
		{"zh-Hant", "繁體中文 (繁體) / Chinese Traditional"},
		{"ko", "한국어 (Korean)"}
	};
	
	public static final String[] ENGINES = new String[] {
			"Google",
			"iCIBA",
			"Reverso"
	};

	public static String[][] langs = new String[][] {
		{"ru", "Russian"},
		{"en", "English"}
	};

	private static String[] lastFrom;
	private static String[] lastTo;

	static String[] langNames;

	public static Translate midlet;
	private static boolean started;
	private static ITranslateUI ui;
	static String version;
	
	// settings
	public static String engine = "google";
	public static String instance = "https://simplytranslate.org";
	public static String proxyUrl = "http://nnproject.cc/hproxy.php?";
	public static boolean useProxy = true;
	public static boolean blackberryWifi;
	
	public static boolean blackberry;
	
	// region MIDlet

	public Translate() {
		midlet = this;
	}

	public void destroyApp(boolean b) {
		if (ui != null) ui.exit();
	}

	protected void pauseApp() {

	}

	protected void startApp() {
		if (started) return;
		started = true;
		try {
			version = getAppProperty("MIDlet-Version");
			{
				String p = System.getProperty("microedition.platform");
				blackberry = p != null && p.toLowerCase().startsWith("blackberry");
			}
			RecordStore r = RecordStore.openRecordStore("gtsl", false);
			String t = new String(r.getRecord(1), "UTF-8");
			r.closeRecordStore();
			String[] a = split(t, ',');
			engine = a[0];
			lastFrom = new String[] { a[1], null };
			lastTo = new String[] { a[2], null };
			instance = a[3];
			proxyUrl = a[4];
			useProxy = a.length < 6 || "true".equals(a[5]);
			blackberryWifi = a.length >= 7 && "true".equals(a[6]);
			loadCachedLangs();
			updateLangs();
			getFromIndex();
			getToIndex();
		} catch (Exception e) {
			e.printStackTrace();
			updateLangs();
		}
//#ifdef NO_SWT
//#		ui = new TranslateLCDUI();
//#else
		try {
			Class.forName("org.eclipse.ercp.swt.mobile.MobileShell");
			ui = SWTUILayer.init();
		} catch (Throwable e) {
			ui = new TranslateLCDUI();
		}
//#endif
	}
	
	// endregion
	
	public static void save() {
		try {
			RecordStore.deleteRecordStore("gtsl");
		} catch (Exception e) {
		}
		try {
			String s = engine + ',' +
				lastFrom[0] + ',' +
				lastTo[0] + ',' +
				instance + ',' +
				proxyUrl + ',' +
				useProxy + ',' +
				blackberryWifi + ','
			;
			RecordStore r = RecordStore.openRecordStore("gtsl", true);
			byte[] b = s.getBytes("UTF-8");
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getFromIndex() {
		int i = findIndex(lastFrom);
		if (i == -1) {
			i = findIndex(new String[] {"en", "english"});
		}
		lastFrom = langs[i];
		return i;
	}

	public static int getToIndex() {
		int i = findIndex(lastTo);
		if (i == -1) {
			i = findIndex(new String[] {"ru", "russian"});
		}
		lastTo = langs[i];
		return i;
	}

	private static int findIndex(String[] f) {
		if (f == null || (f[0] == null && f[1] == null)) return -1;
		for (int i = 0; i < langs.length; i++) {
			if ((f[0] != null && f[0].equalsIgnoreCase(langs[i][0]))
					|| (f[1] != null && (f[1].equalsIgnoreCase(langs[i][1])
							|| langs[i][1].toLowerCase().startsWith(f[1].toLowerCase()))))
				return i;
		}
		return -1;
	}

	public static void setSelected(int from, int to) {
		lastFrom = langs[from];
		lastTo = langs[to];
	}
	
	public static void setCurrentEngine(String e) {
		engine = e;
		if (!needDownload()) {
			loadCachedLangs();
			updateLangs();
		}
	}
	
	public static boolean needDownload() {
		try {
			RecordStore r = RecordStore.openRecordStore("gt_"+engine, false);
			r.closeRecordStore();
			return false;
		} catch (Exception ignored) {}
		return true;
	}

	private static void loadCachedLangs() {
		try {
			RecordStore r = RecordStore.openRecordStore("gt_"+engine, false);
			try {
				String t = new String(r.getRecord(1), "UTF-8");
				String[] a = split(t, ';');
				langs = new String[a.length][2];
				for (int i = 0; i < a.length; i++) {
					if (a[i].length() == 0) continue;
					langs[i] = split(a[i], ',');
				}
			} finally {
				r.closeRecordStore();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveLangs() {
		try {
			RecordStore.deleteRecordStore("gt_"+engine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String s = "";
			for (int i = 0; i < langs.length; i++) {
				s += langs[i][0] + "," + langs[i][1];
				if (i != langs.length - 1) s += ";";
			}
			RecordStore r = RecordStore.openRecordStore("gt_"+engine, true);
			byte[] b = s.getBytes("UTF-8");
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateLangs() {
		langNames = new String[langs.length];
		for (int i = 0; i < langs.length; i++) {
			langNames[i] = langs[i][1];
		}
	}

	public static int getLangFromName(String s) {
		int i = findIndex(new String[] { null, s });
		if (i == -1) {
			i = findIndex(new String[] {"en", "english"});
		}
		return i;
	}

	public static void setDownloaded(String[][] arr) {
		int l = arr.length;
		// sort
		for (int i = 0; i < l; i++) {
			for (int j = i + 1; j < l; j++) {
				if (arr[i][1] != null && arr[j][1] != null && arr[i][1].compareTo(arr[j][1]) > 0) {
					String[] tmp = arr[i];
					arr[i] = arr[j];
					arr[j] = tmp;
				}
			}
		}
		
		langs = arr;
		updateLangs();
		saveLangs();
		save();
	}
	
	public static void deleteAllLangs() {
		try {
			String[] s = RecordStore.listRecordStores();
			for (int i = 0; i < s.length; i++) {
				String x = s[i];
				if (x.startsWith("gt_")) {
					try {
						RecordStore.deleteRecordStore(x);
					} catch (Exception ignored) {}
				}
			}
		} catch (Exception ignored) {}
	}
	
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
			if (b != null) b.close();
			if (is != null) is.close();
			if (con != null) con.close();
		}
	}

	public static ContentConnection open(String url) throws IOException {
		if (blackberry && blackberryWifi) {
			url = url.concat(";deviceside=true;interface=wifi");
		}
		ContentConnection con = (ContentConnection) Connector.open(url);
		((HttpConnection) con).setRequestProperty("User-Agent", "TranslateApp/" + version + " (https://github.com/shinovon)");
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
		if (i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while (i != -1) {
			str = str.substring(i + 1);
			if ((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}

}
