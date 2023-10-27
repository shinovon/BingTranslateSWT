package cc.nnproject.translate;

public interface ITranslateUI {

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

	public void error(String s);

	public void setTranslating(boolean state);
}
