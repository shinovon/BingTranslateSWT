package cc.nnproject.translate;

import javax.microedition.rms.RecordStore;

public class Languages {

	public static final String[] SUPPORTED_LANGUAGE_ALIAS = new String[]
	{
		"af",
		"am",
		"ar",
		"as",
		"az",
		"ba",
		"bg",
		"bn",
		"bo",
		"bs",
		"ca",
		"cs",
		"cy",
		"da",
		"de",
		"dv",
		"el",
		"en",
		"es",
		"et",
		"fa",
		"fi",
		"fil",
		"fj",
		"fr",
		"fr-CA",
		"ga",
		"gu",
		"he",
		"hi",
		"hr",
		"ht",
		"hu",
		"hy",
		"id",
		"is",
		"it",
		"iu",
		"ja",
		"ka",
		"kk",
		"km",
		"kmr",
		"kn",
		"ko",
		"ku",
		"ky",
		"lo",
		"lt",
		"lv",
		"lzh",
		"mg",
		"mi",
		"mk",
		"ml",
		"mn-Cyrl",
		"mn-Mong",
		"mr",
		"ms",
		"mt",
		"mww",
		"my",
		"nb",
		"ne",
		"nl",
		"or",
		"otq",
		"pa",
		"pl",
		"prs",
		"ps",
		"pt",
		"pt-PT",
		"ro",
		"ru",
		"sk",
		"sl",
		"sm",
		"sq",
		"sr-Cyrl",
		"sr-Latn",
		"sv",
		"sw",
		"ta",
		"te",
		"th",
		"ti",
		"tk",
		"tlh-Latn",
		"tlh-Piqd",
		"to",
		"tr",
		"tt",
		"ty",
		"ug",
		"uk",
		"ur",
		"uz",
		"vi",
		"yua",
		"yue",
		"zh-Hans",
		"zh-Hant"
	};

	public static final String[] SUPPORTED_LANGUAGE_NAMES = new String[]
	{
		"Afrikaans",
		"አማርኛ (Amharic)",
		"العربية (Arabic)",
		"অসমীয়া (Assamese)",
		"Azərbaycan (Azerbaijani)",
		"Bashkir",
		"Български (Bulgarian)",
		"বাংলা (Bangla)",
		"བོད་སྐད་ (Tibetan)",
		"Bosnian",
		"Català (Catalan)",
		"Čeština (Czech)",
		"Cymraeg (Welsh)",
		"Dansk (Danish)",
		"Deutsch (German)",
		"ދިވެހިބަސް (Divehi)",
		"Ελληνικά (Greek)",
		"English",
		"Español (Spanish)",
		"Eesti (Estonian)",
		"فارسی (Persian)",
		"Suomi (Finnish)",
		"Filipino",
		"Na Vosa Vakaviti (Fijian)",
		"Français (French)",
		"Français (Canada)",
		"Gaeilge (Irish)",
		"ગુજરાતી (Gujarati)",
		"עברית (Hebrew)",
		"हिन्दी (Hindi)",
		"Hrvatski (Croatian)",
		"Haitian Creole",
		"Magyar (Hungarian)",
		"Հայերեն (Armenian)",
		"Indonesia (Indonesian)",
		"Íslenska (Icelandic)",
		"Italiano (Italian)",
		"ᐃᓄᒃᑎᑐᑦ (Inuktitut)",
		"日本語 (Japanese)",
		"ქართული (Georgian)",
		"Қазақ Тілі (Kazakh)",
		"ខ្មែរ (Khmer)",
		"Kurdî (Bakur) / Kurdish (Northern)",
		"ಕನ್ನಡ (Kannada)",
		"한국어 (Korean)",
		"Kurdî (Navîn) / Kurdish (Central)",
		"Kyrgyz",
		"ລາວ (Lao)",
		"Lietuvių (Lithuanian)",
		"Latviešu (Latvian)",
		"中文 (文言文) / Chinese (Literary)",
		"Malagasy",
		"Te Reo Māori (Māori)",
		"Македонски (Macedonian)",
		"മലയാളം (Malayalam)",
		"Mongolian (Cyrillic)",
		"ᠮᠣᠩᠭᠣᠯ ᠬᠡᠯᠡ / Mongolian (Traditional)",
		"मराठी (Marathi)",
		"Melayu (Malay)",
		"Malti (Maltese)",
		"Hmong Daw",
		"မြန်မာ / Myanmar (Burmese)",
		"Norsk Bokmål (Norwegian)",
		"नेपाली (Nepali)",
		"Nederlands (Dutch)",
		"ଓଡ଼ିଆ (Odia)",
		"Hñähñu (Querétaro Otomi)",
		"ਪੰਜਾਬੀ (Punjabi)",
		"Polski (Polish)",
		"دری (Dari)",
		"پښتو (Pashto)",
		"Português (Brasil)",
		"Português (Portugal)",
		"Română (Romanian)",
		"Русский (Russian)",
		"Slovenčina (Slovak)",
		"Slovenščina (Slovenian)",
		"Gagana Sāmoa (Samoan)",
		"Shqip (Albanian)",
		"Српски (ћирилица) / Serbian (Cyrillic)",
		"Srpski (latinica) / Serbian (Latin)",
		"Svenska (Swedish)",
		"Kiswahili (Swahili)",
		"தமிழ் (Tamil)",
		"తెలుగు (Telugu)",
		"ไทย (Thai)",
		"ትግር (Tigrinya)",
		"Türkmen Dili (Turkmen)",
		"Klingon (Latin)",
		"Klingon (pIqaD)",
		"Lea Fakatonga (Tongan)",
		"Türkçe (Turkish)",
		"Татар (Tatar)",
		"Reo Tahiti (Tahitian)",
		"ئۇيغۇرچە (Uyghur)",
		"Українська (Ukrainian)",
		"اردو (Urdu)",
		"Uzbek (Latin)",
		"Tiếng Việt (Vietnamese)",
		"Yucatec Maya",
		"粵語 (繁體) / Cantonese (Traditional)",
		"中文 (简体) / Chinese Simplified",
		"繁體中文 (繁體) / Chinese Traditional"
	};

	private static int[] langIndexes;
	private static String[] langNames;
	private static String[] langAliases;

	private static int lastFrom;
	private static int lastTo;
	
	public static void setSelected(int[] a) {
		langIndexes = a;
		langNames = new String[a.length];
		langAliases = new String[a.length];
		for(int i = 0; i < a.length; i++) {
			int x = a[i];
			langNames[i] = SUPPORTED_LANGUAGE_NAMES[x];
			langAliases[i] = SUPPORTED_LANGUAGE_ALIAS[x];
		}
	}
	public static void setSelected(boolean[] a, int count) {
		int[] b = new int[count];
		int j = 0;
		for(int i = 0; i < a.length; i++) {
			boolean x = a[i];
			if(x) {
				b[j++] = i;
			}
		}
		langIndexes = b;
		langNames = new String[count];
		langAliases = new String[count];
		for(int i = 0; i < count; i++) {
			int x = b[i];
			langNames[i] = SUPPORTED_LANGUAGE_NAMES[x];
			langAliases[i] = SUPPORTED_LANGUAGE_ALIAS[x];
		}
	}
	
	public static void save() {
		try {
			RecordStore.deleteRecordStore("blangs");
		} catch (Exception e) {
		}
		try {
			String s = lastFrom + "," + lastTo + ",";
			if(langIndexes != null) {
				int i = 0;
				while(true) {
					s += langIndexes[i];
					i++;
					if(i == langIndexes.length) {
						break;
					}
					s += ",";
				}
			}
			RecordStore r = RecordStore.openRecordStore("blangs", true);
			byte[] b = s.getBytes("UTF-8");
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String[] getSupportedLang(int i) {
		return new String[] { SUPPORTED_LANGUAGE_NAMES[i], SUPPORTED_LANGUAGE_ALIAS[i] };
	}
	
	public static String[] getSelectedLang(int i) {
		return new String[] { langNames[i], langAliases[i] };
	}
	
	public static String[] getLangNames() {
		return langNames;
	}

	public static int getLastFrom() {
		return lastFrom;
	}

	public static int getLastTo() {
		return lastTo;
	}

	public static void init(boolean b) {
		if(b) {
			try {
				RecordStore r = RecordStore.openRecordStore("blangs", false);
				String t = new String(r.getRecord(1), "UTF-8");
				r.closeRecordStore();
				String[] a = StringUtils.split(t, ',');
				int l = a.length - 2;
				langIndexes = new int[l];
				langNames = new String[l];
				langAliases = new String[l];
				for(int i = 0; i < a.length; i++) {
					if(a[i].length() == 0) continue;
					int x = Integer.parseInt(a[i]);
					if(i == 0) {
						lastFrom = x;
						continue;
					}
					if(i == 1) {
						lastTo = x;
						continue;
					}
					int j = i - 2;
					langIndexes[j] = x;
					langNames[j] = SUPPORTED_LANGUAGE_NAMES[x];
					langAliases[j] = SUPPORTED_LANGUAGE_ALIAS[x];
				}
			} catch (Exception e) {
				e.printStackTrace();
				lastFrom = 0;
				lastTo = 3;
				setSelected(new int[] { 
						82-8, 
						103-8, 
						48-8, 
						25-8, 
						26-8, 
						32-8, 
						44-8,
						22-8,
						46-8, 
						101, 
						102 });
			}
		} else {
			try {
				RecordStore r = RecordStore.openRecordStore("blangs", false);
				String t = new String(r.getRecord(1), "UTF-8");
				r.closeRecordStore();
				String[] a = StringUtils.split(t, ',');
				for(int i = 0; i < a.length; i++) {
					if(a[i].length() == 0) continue;
					int x = Integer.parseInt(a[i]);
					if(i == 0) {
						lastFrom = x;
						continue;
					}
					if(i == 1) {
						lastTo = x;
						continue;
					}
					break;
				}
			} catch (Exception e) {
				lastFrom = 74;
				lastTo = 14;
			}
		}
	}

	public static void setSelected(String[] a) {
		int[] r = new int[a.length];
		for(int i = 0; i < SUPPORTED_LANGUAGE_NAMES.length; i++) {
			String s = SUPPORTED_LANGUAGE_NAMES[i];
			for(int j = 0; j < a.length; j++) {
				if(s.equals(a[j])) {
					r[j] = i;
					break;
				}
			}
		}
		setSelected(r);
	}

	public static void setLastSelected(int from, int to) {
		lastFrom = from;
		lastTo = to;
	}

	public static int getSupportedIndex(String s) {
		for(int i = 0; i < SUPPORTED_LANGUAGE_NAMES.length; i++) {
			String s2 = SUPPORTED_LANGUAGE_NAMES[i];
			if(s2.equals(s)) return i;
		}
		return 0;
	}

	public static int getSelectedIndex(String s) {
		for(int i = 0; i < langNames.length; i++) {
			String s2 = langNames[i];
			if(s2.equals(s)) return i;
		}
		return 0;
	}

	public static boolean[] getSelected() {
		boolean[] r = new boolean[SUPPORTED_LANGUAGE_NAMES.length];
		for(int i = 0; i < r.length; i++) {
			for(int j = 0; j < langIndexes.length; j++) {
				if(langIndexes[j] == i) {
					r[i] = true;
					break;
				}
			}
		}
		return r;
	}
	
}
