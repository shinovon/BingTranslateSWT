/*
 * Copyright (c) 2021-2024 Arman Jussupgaliyev
 */
package nntranslate;

import javax.microedition.rms.RecordStore;

public class Languages {
	
	public static final String[] engines = new String[] {
			"Google",
			"Deepl",
			"Reverso",
			"Libre",
			"Bing"
	};

	private static String[][] langs = new String[][] {
		{"ru", "Russian"},
		{"en", "English"}
	};

	private static String[] lastFrom;
	private static String[] lastTo;
	
	private static String currentEngine = "google";

	private static String instance = "simplytranslate.org";

	private static String proxy = "http://nnp.nnchan.ru/glype/browse.php?u=";

	private static String[] langNames;

	static {
	}
	
	public static void save() {
		try {
			RecordStore.deleteRecordStore("gtsl");
		} catch (Exception e) {
		}
		try {
			String s = currentEngine + "," + lastFrom[0] + "," + lastTo[0] + "," + instance + "," + proxy;
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
		if(i == -1) {
			i = findIndex(new String[] {"en", "english"});
		}
		lastFrom = langs[i];
		return i;
	}

	public static int getToIndex() {
		int i = findIndex(lastTo);
		if(i == -1) {
			i = findIndex(new String[] {"ru", "russian"});
		}
		lastTo = langs[i];
		return i;
	}

	private static int findIndex(String[] f) {
		if(f == null || (f[0] == null && f[1] == null)) return -1;
		for(int i = 0; i < langs.length; i++) {
			if((f[0] != null && f[0].equalsIgnoreCase(langs[i][0]))
					|| (f[1] != null && (f[1].equalsIgnoreCase(langs[i][1])
							|| langs[i][1].toLowerCase().startsWith(f[1].toLowerCase()))))
				return i;
		}
		return -1;
	}

	public static void init() {
		try {
			RecordStore r = RecordStore.openRecordStore("gtsl", false);
			String t = new String(r.getRecord(1), "UTF-8");
			r.closeRecordStore();
			String[] a = Util.split(t, ',');
			currentEngine = a[0];
			lastFrom = new String[] { a[1], null };
			lastTo = new String[] { a[2], null };
			instance = a[3];
			proxy = a[4];
			loadCachedLangs();
			updateLangs();
			getFromIndex();
			getToIndex();
		} catch (Exception e) {
			e.printStackTrace();
			updateLangs();
		}
	}

	public static void setSelected(int from, int to) {
		lastFrom = getLangFromIndex(from);
		lastTo = getLangFromIndex(to);
	}
	
	public static String[] getLangFromIndex(int from) {
		return langs[from];
	}

	public static String getCurrentEngine() {
		return currentEngine;
	}
	
	public static void setCurrentEngine(String e) {
		currentEngine = e;
		if(!needDownload()) {
			loadCachedLangs();
			updateLangs();
		}
	}

	public static String[] getLangNames() {
		return langNames;
	}
	
	public static boolean needDownload() {
		try {
			RecordStore r = RecordStore.openRecordStore("gt_"+currentEngine, false);
			r.closeRecordStore();
			return false;
		} catch (Exception e) {
		}
		return true;
	}

	private static void loadCachedLangs() {
		try {
			RecordStore r = RecordStore.openRecordStore("gt_"+currentEngine, false);
			String t = new String(r.getRecord(1), "UTF-8");
			r.closeRecordStore();
			String[] a = Util.split(t, ';');
			langs = new String[a.length][2];
			for(int i = 0; i < a.length; i++) {
				if(a[i].length() == 0) continue;
				langs[i] = Util.split(a[i], ',');
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveLangs() {
		try {
			RecordStore.deleteRecordStore("gt_"+currentEngine);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			String s = "";
			for(int i = 0; i < langs.length; i++) {
				s += langs[i][0] + "," + langs[i][1];
				if(i != langs.length - 1) s += ";";
			}
			RecordStore r = RecordStore.openRecordStore("gt_"+currentEngine, true);
			byte[] b = s.getBytes("UTF-8");
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateLangs() {
		langNames = new String[langs.length];
		for(int i = 0; i < langs.length; i++) {
			langNames[i] = langs[i][1];
		}
	}

	public static int getLangFromName(String s) {
		int i = findIndex(new String[] { null, s });
		if(i == -1) {
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

	public static String getInstance() {
		return instance;
	}

	public static String getProxy() {
		return proxy;
	}

	public static void setInstance(String s) {
		instance = s;
	}

	public static void setProxy(String s) {
		proxy = s;
	}
	
	public static void deleteAllLangs() {
		try {
			String[] s = RecordStore.listRecordStores();
			for(int i = 0; i < s.length; i++) {
				String x = s[i];
				if(x.startsWith("gt_")) {
					try {
						RecordStore.deleteRecordStore(x);
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			
		}
	}
	
}
