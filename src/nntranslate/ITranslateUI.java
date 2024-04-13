/*
 * Copyright (c) 2021-2024 Arman Jussupgaliyev
 */
package nntranslate;

public interface ITranslateUI {

	// do not inline
	// юзается в about
	public static String e = "nn";
	
	//public static final String[] langs = new String[] { "Русский (Russian)", "Українська (Ukrainian)", "Беларуская (Belarusian)", "Қазақша (Kazakh)", "English", "Español", "Français", "Italian", "Deutsch", "日本 (Japanese)", "中国人 (Chinese)"};
	//public static final String[] langsAlias = new String[] { "ru", "uk", "be", "kk", "en", "es", "fr", "it", "de", "ja", "zh-CN" };

	/** Входящий текст */
	public String getText();
	
	/** Выходящий текст */
	public void setText(String s);
	
	/** Язык ввода */
	public String getFromLang();
	
	/** Язык вывода */
	public String getToLang();
	
	/** Синхронизировать */
	public void sync();
	
	/** Показать сообщение */
	public void msg(String s);
	
	public boolean running();

	public void exit();

	public void setDownloading(boolean b);

	public void downloadingError(String s);

	public void setLanguages(String[][] l);

	public void downloadingDone();

	public void setTranslating(boolean b);
}
