package cc.nnproject.translate.bing.lcdui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.bing.TranslateBingMIDlet;
import cc.nnproject.translate.bing.TranslateBingThread;

public class TranslateUIBingLCD implements Runnable, ITranslateUI {

	private Display display;

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private boolean exiting;
	
	public TranslateUIBingLCD() {
		new Thread(this, "Main LCDUI Thread").start();
	}

	public void run() {
		display = Display.getDisplay(TranslateBingMIDlet.midlet);
		translateThread.start();
		// TODO
	}

	public String getText() {
		// TODO
		return null;
	}

	public void setText(String s) {
		// TODO
	}

	public String getFromLang() {
		// TODO
		return null;
	}

	public String getToLang() {
		// TODO
		return null;
	}

	public void sync() {
		// TODO
	}

	public void msg(String s) {
		Alert a = new Alert("", "", null, null);
		a.setString(s);
		display.setCurrent(a);
	}

	public boolean running() {
		return !exiting;
	}

	public void exit() {
		exiting = true;
		TranslateBingMIDlet.midlet.notifyDestroyed();
	}

}
