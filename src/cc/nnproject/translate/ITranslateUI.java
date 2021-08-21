package cc.nnproject.translate;

public interface ITranslateUI {
	
	public static final String[] langs = new String[] { "Русский (Russian)", "Українська (Ukrainian)", "Беларуская (Belarusian)", "Қазақша (Kazakh)", "English", "Español", "Français", "Italian", "Deutsch", "日本 (Japanese)", "中国人 (Chinese)"};

	public static final String[] langsAlias = new String[] { "ru", "uk", "be", "kk", "en", "es", "fr", "it", "de", "ja", "zh-CN" };

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
}
