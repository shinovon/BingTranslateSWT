package cc.nnproject.translate.bing.lcdui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.bing.TranslateBingMIDlet;
import cc.nnproject.translate.bing.TranslateBingThread;

public class TranslateUIBingLCD implements Runnable, ITranslateUI, CommandListener, ItemCommandListener {

	private Display display;
	private Form form;
	
	private Command translateCmd = new Command("Translate", Command.OK, 1);
	private Command setLangInCmd = new Command("Change in", Command.OK, 1);
	private Command setLangOutCmd = new Command("Change out", Command.OK, 1);
	private Command listChangeCmd = new Command("Change", Command.OK, 1);

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private boolean exiting;
	private TextField textIn;
	private TextField textOut;
	private List listLangIn;
	private List listLangOut;
	private StringItem setLangInBtn;
	private StringItem setLangOutBtn;
	
	public TranslateUIBingLCD() {
		new Thread(this, "Main LCDUI Thread").start();
	}

	public void run() {
		listLangIn = new List("Input language", List.EXCLUSIVE);
		listLangOut = new List("Output language", List.EXCLUSIVE);
		for(int i = 0; i < langs.length; i++) {
			listLangIn.append(langs[i], null);
			listLangOut.append(langs[i], null);
		}
		listLangIn.addCommand(listChangeCmd);
		listLangOut.addCommand(listChangeCmd);
		listLangIn.setSelectedIndex(0, true);
		listLangOut.setSelectedIndex(4, true);
		listLangIn.setCommandListener(this);
		listLangOut.setCommandListener(this);
		display = Display.getDisplay(TranslateBingMIDlet.midlet);
		translateThread.start();
		form = new Form("Bing Translate");
		form.addCommand(translateCmd);
		form.setCommandListener(this);
		form.append(textIn = new TextField("", "", 512, TextField.ANY));
		form.append(textOut = new TextField("", "", 512, TextField.ANY | TextField.UNEDITABLE));
		form.append(setLangInBtn = new StringItem("", "", StringItem.BUTTON));
		setLangInBtn.setText("Input language: " + langs[listLangIn.getSelectedIndex()]);
		setLangInBtn.setDefaultCommand(setLangInCmd);
		setLangInBtn.setItemCommandListener(this);
		form.append(setLangOutBtn = new StringItem("", "", StringItem.BUTTON));
		setLangOutBtn.setText("Output language: " + langs[listLangOut.getSelectedIndex()]);
		setLangOutBtn.setDefaultCommand(setLangOutCmd);
		setLangOutBtn.setItemCommandListener(this);
		form.append("\nShinovon (nnproject.cc)\nrunning LCDUI Version");
		display.setCurrent(form);
	}

	public String getText() {
		return textIn.getString();
	}

	public void setText(String s) {
		textOut.setString(s);
	}

	public String getFromLang() {
		return langsAlias[listLangIn.getSelectedIndex()];
	}

	public String getToLang() {
		return langsAlias[listLangOut.getSelectedIndex()];
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
		if(c == listChangeCmd) {
			setLangInBtn.setText("Input language: " + langs[listLangIn.getSelectedIndex()]);
			setLangOutBtn.setText("Output language: " + langs[listLangOut.getSelectedIndex()]);
			display.setCurrent(form);
		}
	}

	public void commandAction(Command c, Item item) {
		if(c == setLangInCmd) {
			display.setCurrent(listLangIn);
		} else if(c == setLangOutCmd) {
			display.setCurrent(listLangOut);
		}
		
	}

}
