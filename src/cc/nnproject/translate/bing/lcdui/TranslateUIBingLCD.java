package cc.nnproject.translate.bing.lcdui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.bing.TranslateBingMIDlet;
import cc.nnproject.translate.bing.TranslateBingThread;

public class TranslateUIBingLCD implements Runnable, ITranslateUI, CommandListener {

	private Display display;
	private Form form;
	
	private Command translateCmd = new Command("Translate", Command.OK, 1);

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private boolean exiting;
	private TextField textIn;
	private TextField textOut;
	
	public TranslateUIBingLCD() {
		new Thread(this, "Main LCDUI Thread").start();
	}

	public void run() {
		display = Display.getDisplay(TranslateBingMIDlet.midlet);
		translateThread.start();
		form = new Form("Bing Translate");
		form.addCommand(translateCmd);
		form.append(textIn = new TextField("", "", 512, TextField.ANY));
		form.append(textOut = new TextField("", "", 512, TextField.ANY | TextField.UNEDITABLE));
		// TODO
	}

	public String getText() {
		return textIn.getString();
	}

	public void setText(String s) {
		textOut.setString(s);
	}

	public String getFromLang() {
		// TODO
		return "ru";
	}

	public String getToLang() {
		// TODO
		return "en";
	}

	public void sync() {
		// по идее не нужна
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

	public void commandAction(Command c, Displayable d) {
		if(c == translateCmd) translateThread.now();
	}

}
