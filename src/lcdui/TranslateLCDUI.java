package lcdui;

import javax.microedition.lcdui.*;
import ITranslateUI;
import Languages;
import TranslateBingThread;
import cc.nnproject.translate.bing.app.TranslateBingMIDlet;

public class TranslateLCDUI implements Runnable, ITranslateUI, CommandListener, ItemCommandListener {

	private Display display;
	private Form form;
	
	private Command translateCmd = new Command("Translate", Command.OK, 3);
	private Command setLangInCmd = new Command("Change in", Command.OK, 1);
	private Command setLangOutCmd = new Command("Change out", Command.OK, 1);
	private Command listChangeCmd = new Command("Change", Command.OK, 1);
	private Command langsDoneCmd = new Command("Done", Command.OK, 1);
	private Command exitCmd = new Command("Exit", Command.EXIT, 1);
	private Command langsCmd = new Command("Edit visible languages", Command.SCREEN, 23);

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private boolean exiting;
	private TextField textIn;
	private TextField textOut;
	private List listLangIn;
	private List listLangOut;
	private StringItem setLangInBtn;
	private StringItem setLangOutBtn;
	private List listLangs;
	
	private String from;
	private String to;
	
	public TranslateLCDUI() {
		new Thread(this, "Main LCDUI Thread").start();
	}

	public void run() {
		listLangIn = new List("Input language", List.EXCLUSIVE);
		listLangOut = new List("Output language", List.EXCLUSIVE);
		listLangs = new List("Languages", List.MULTIPLE);
		String[] a = Languages.getLangNames();
		for(int i = 0; i < a.length; i++) {
			listLangIn.append(a[i], null);
			listLangOut.append(a[i], null);
		}
		a = Languages.SUPPORTED_LANGUAGE_NAMES;
		for(int i = 0; i < a.length; i++) {
			listLangs.append(a[i], null);
		}
		listLangs.setSelectedFlags(Languages.getSelected());
		listLangs.addCommand(langsDoneCmd);
		listLangIn.addCommand(listChangeCmd);
		listLangOut.addCommand(listChangeCmd);
		listLangIn.setSelectedIndex(Languages.getLastFrom(), true);
		listLangOut.setSelectedIndex(Languages.getLastTo(), true);
		from = Languages.getSelectedLang(Languages.getLastFrom())[1];
		to = Languages.getSelectedLang(Languages.getLastTo())[1];
		listLangs.setCommandListener(this);
		listLangIn.setCommandListener(this);
		listLangOut.setCommandListener(this);
		display = Display.getDisplay(TranslateBingMIDlet.midlet);
		translateThread.start();
		form = new Form("Bing Translate");
		form.addCommand(translateCmd);
		form.addCommand(exitCmd);
		form.addCommand(langsCmd);
		form.setCommandListener(this);
		form.append(textIn = new TextField("", "", 512, TextField.ANY));
		textIn.setLabel("Input");
		form.append(textOut = new TextField("", "", 512, TextField.ANY | TextField.UNEDITABLE));
		textOut.setLabel("Output");
		form.append(setLangInBtn = new StringItem("", "", StringItem.BUTTON));
		setLangInBtn.setText("In: " + Languages.getSelectedLang(listLangIn.getSelectedIndex())[0]);
		setLangInBtn.setDefaultCommand(setLangInCmd);
		setLangInBtn.setItemCommandListener(this);
		form.append(setLangOutBtn = new StringItem("", "", StringItem.BUTTON));
		setLangOutBtn.setText("Out: " + Languages.getSelectedLang(listLangOut.getSelectedIndex())[0]);
		setLangOutBtn.setDefaultCommand(setLangOutCmd);
		setLangOutBtn.setItemCommandListener(this);
		//form.append("\nBy shinovon & Feodor0090\nnnp.nnchan.ru");
		display.setCurrent(form);
	}

	public String getText() {
		return textIn.getString();
	}

	public void setText(String s) {
		textOut.setString(s);
	}

	public String getFromLang() {
		return from;
	}

	public String getToLang() {
		return to;
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
		translateThread.interrupt();
		TranslateBingMIDlet.midlet.notifyDestroyed();
	}

	public void commandAction(Command c, Displayable d) {
		if(c == translateCmd) translateThread.now();
		if(c == exitCmd) exit();
		if(c == langsCmd) {
			display.setCurrent(listLangs);
		}
		if(c == langsDoneCmd) {
			boolean[] b = new boolean[listLangs.size()];
			int size = listLangs.getSelectedFlags(b);
			Languages.setSelected(b, size);
			Languages.save();
			listLangIn.deleteAll();
			listLangOut.deleteAll();
			String[] a = Languages.getLangNames();
			for(int i = 0; i < a.length; i++) {
				listLangIn.append(a[i], null);
				listLangOut.append(a[i], null);
			}
			display.setCurrent(form);
		}
		if(c == listChangeCmd) {
			Languages.setLastSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
			Languages.save();
			setLangInBtn.setText("In: " + Languages.getSelectedLang(listLangIn.getSelectedIndex())[0]);
			setLangOutBtn.setText("Out: " + Languages.getSelectedLang(listLangOut.getSelectedIndex())[0]);
			from = Languages.getSelectedLang(listLangIn.getSelectedIndex())[1];
			to = Languages.getSelectedLang(listLangOut.getSelectedIndex())[1];
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

	public void error(String s) {
		// TODO Auto-generated method stub
		
	}

	public void setTranslating(boolean state) {
		// TODO Auto-generated method stub
		
	}

}
