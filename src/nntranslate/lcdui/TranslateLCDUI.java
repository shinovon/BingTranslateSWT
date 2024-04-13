/*
 * Copyright (c) 2021-2024 Arman Jussupgaliyev
 */
package nntranslate.lcdui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

import com.nokia.mid.ui.Clipboard;

import nntranslate.ITranslateUI;
import nntranslate.Languages;
import nntranslate.v2.TranslateMIDlet;
import nntranslate.TranslateThread;
import nntranslate.Util;

public class TranslateLCDUI implements Runnable, ITranslateUI, CommandListener, ItemCommandListener, ItemStateListener, PlayerListener {

	private Display display;
	private Form mainForm;
	
	private static final Command exitCmd = new Command("Exit", Command.EXIT, 1);
	private static final Command translateCmd = new Command("Translate", Command.OK, 3);
	private static final Command reverseCmd = new Command("Reverse", Command.SCREEN, 5);
	private static final Command settingsCmd = new Command("Settings", Command.SCREEN, 6);
	private static final Command aboutCmd = new Command("About", Command.SCREEN, 7);
	private static final Command clearLangsCmd = new Command("Clear langs cache", Command.SCREEN, 8);
	
	private static final Command setLangInCmd = new Command("Change", Command.OK, 1);
	private static final Command setLangOutCmd = new Command("Change", Command.OK, 1);
	
	private static final Command ttsCmd = new Command("Listen", Command.ITEM, 4);
	private static final Command copyCmd = new Command("Copy", Command.ITEM, 2);
	private static final Command pasteCmd = new Command("Paste", Command.ITEM, 3);
	
	private static final Command listDoneCmd = new Command("Done", Command.OK, 1);
	
	private static final Command backCmd = new Command("Back", Command.BACK, 1);
	
	private static final Command hyperlinkCmd = new Command("Open", Command.ITEM, 2);

	private TranslateThread translateThread = new TranslateThread(this);

	private boolean exiting;
	private TextField inField;
	private TextField outField;
	private List listLangIn;
	private List listLangOut;
	private StringItem setLangInBtn;
	private StringItem setLangOutBtn;
	
	private String from;
	private String to;
	private String inputText;
	
	private boolean ttsPlaying;
	private Player ttsplayer;
	private Form settingsForm;
	private ChoiceGroup engineChoice;
	private TextField instanceField;
	private TextField proxyField;
	
	public TranslateLCDUI() {
		new Thread(this).start();
	}

	public void run() {
		display = Display.getDisplay(TranslateMIDlet.midlet);
		
		boolean clipboard = false;
		try {
			Class.forName("com.nokia.mid.ui.Clipboard");
			clipboard = true;
		} catch (Throwable e) {}
		
		listLangIn = new List("Input language", List.EXCLUSIVE);
		listLangOut = new List("Output language", List.EXCLUSIVE);
		listLangIn.addCommand(listDoneCmd);
		listLangOut.addCommand(listDoneCmd);
		listLangIn.setCommandListener(this);
		listLangOut.setCommandListener(this);
		
		updateLangs();
		
		translateThread.start();
		translateThread.setEngine(Languages.getCurrentEngine());
		translateThread.setInstance(Languages.getInstance());
		translateThread.setProxy(Languages.getProxy());
		if(Languages.needDownload()) {
			translateThread.setDownload();
			translateThread.now();
		}
		
		mainForm = new Form("Translate v2");
		mainForm.addCommand(translateCmd);
		mainForm.addCommand(exitCmd);
		mainForm.addCommand(settingsCmd);
		mainForm.addCommand(aboutCmd);
		mainForm.addCommand(reverseCmd);
		mainForm.addCommand(clearLangsCmd);
		mainForm.setCommandListener(this);
		mainForm.setItemStateListener(this);
		
		mainForm.append(inField = new TextField("Input", "", 500, TextField.ANY));
		inField.setItemCommandListener(this);
		inField.addCommand(ttsCmd);
		if(clipboard) {
			inField.addCommand(copyCmd);
			inField.addCommand(pasteCmd);
		}
		
		mainForm.append(outField = new TextField("Output", "", 500, TextField.ANY | TextField.UNEDITABLE));
		outField.setItemCommandListener(this);
		outField.addCommand(ttsCmd);
		if(clipboard) {
			outField.addCommand(copyCmd);
		}
		
		mainForm.append(setLangInBtn = new StringItem("", "", StringItem.BUTTON));
		setLangInBtn.setText("In: " + Languages.getLangFromIndex(listLangIn.getSelectedIndex())[1]);
		setLangInBtn.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		setLangInBtn.setDefaultCommand(setLangInCmd);
		setLangInBtn.setItemCommandListener(this);
		mainForm.append(setLangOutBtn = new StringItem("", "", StringItem.BUTTON));
		setLangOutBtn.setText("Out: " + Languages.getLangFromIndex(listLangOut.getSelectedIndex())[1]);
		setLangOutBtn.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		setLangOutBtn.setDefaultCommand(setLangOutCmd);
		setLangOutBtn.setItemCommandListener(this);
		display.setCurrent(mainForm);
	}

	public String getText() {
		if(inputText == null) {
			inputText = inField.getString();
		}
		return inputText;
	}

	public void setText(String s) {
		outField.setString(s);
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
		TranslateMIDlet.midlet.notifyDestroyed();
	}

	public void itemStateChanged(Item item) {
		if(item == inField) {
			inputText = inField.getString();
			if(inputText.trim().length() == 0)
				return;
			translateThread.schedule();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == translateCmd) {
			inputText = inField.getString();
			translateThread.now();
			return;
		}
		if(c == listDoneCmd) {
			Languages.setSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
			from = Languages.getLangFromIndex(Languages.getFromIndex())[0];
			to = Languages.getLangFromIndex(Languages.getToIndex())[0];
			display.setCurrent(mainForm);
			return;
		}
		if(c == exitCmd) {
			exit();
			return;
		}
		if(c == settingsCmd) {
			if(settingsForm == null) {
				settingsForm = new Form("Settings");
				settingsForm.addCommand(backCmd);
				settingsForm.setCommandListener(this);
				
				String[] engines = Languages.engines;
				String curEngine = Languages.getCurrentEngine();
				engineChoice = new ChoiceGroup("Translate engine", Choice.POPUP, engines, null);
				for(int i = 0; i < engines.length; i++) {
					if(engines[i].equalsIgnoreCase(curEngine)) {
						engineChoice.setSelectedIndex(i, true);
						break;
					}
				}
				settingsForm.append(engineChoice);
				
				instanceField = new TextField("Instance", Languages.getInstance(), 100, TextField.ANY);
				settingsForm.append(instanceField);
				
				proxyField = new TextField("Proxy URL", Languages.getProxy(), 100, TextField.ANY);
				settingsForm.append(proxyField);
			}
			display.setCurrent(settingsForm);
			return;
		}
		if(c == aboutCmd) {
			Form f = new Form("About");
			f.addCommand(backCmd);
			f.setCommandListener(this);
			StringItem s;
			try {
				f.append(new ImageItem(null, Image.createImage("/icon.png"), Item.LAYOUT_LEFT, null));
				s = new StringItem(null, "Translate v" + TranslateMIDlet.midlet.getAppProperty("MIDlet-Version"));
				s.setFont(Font.getFont(0, 0, Font.SIZE_LARGE));
				s.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_VCENTER);
				f.append(s);
			} catch (IOException e) {
			}
			s = new StringItem(null, "J2ME online translator app\n\n");
			s.setFont(Font.getDefaultFont());
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			f.append(s);
			s = new StringItem("Developed by", "shinovon");
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			f.append(s);
			s = new StringItem("Web", "nnp.nnchan.ru", Item.HYPERLINK);
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			s.setDefaultCommand(hyperlinkCmd);
			s.setItemCommandListener(this);
			f.append(s);
			s = new StringItem("Donate", "boosty.to/nnproject/donate", Item.HYPERLINK);
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			s.setDefaultCommand(hyperlinkCmd);
			s.setItemCommandListener(this);
			f.append(s);
			s = new StringItem("Chat", "t.me/nnmidletschat", Item.HYPERLINK);
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			s.setDefaultCommand(hyperlinkCmd);
			s.setItemCommandListener(this);
			f.append(s);
			
			display.setCurrent(f);
			return;
		}
		if(c == clearLangsCmd) {
			Languages.deleteAllLangs();
			translateThread.setDownload();
			translateThread.now();
			display.setCurrent(mainForm);
			return;
		}
		if(c == backCmd) {
			if(d == settingsForm) {
				String engine = Languages.engines[engineChoice.getSelectedIndex()].toLowerCase();
				String inst = instanceField.getString();
				String proxy = proxyField.getString();
				
				if(!inst.equals(Languages.getInstance())) {
					Languages.setInstance(inst);
				    Languages.deleteAllLangs();
					Languages.setCurrentEngine(engine);
					Languages.setProxy(proxy);
					translateThread.setInstance(inst);
					translateThread.setEngine(engine);
					translateThread.setProxy(proxy);
					translateThread.setDownload();
					translateThread.now();
				} else {
					Languages.setSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
					Languages.setCurrentEngine(engine);
					Languages.setProxy(proxy);
					translateThread.setEngine(engine);
					translateThread.setProxy(proxy);
					if(Languages.needDownload()) {
						translateThread.setDownload();
						translateThread.now();
					} else {
						updateLangs();
					}
				}
				Languages.save();
			}
			display.setCurrent(mainForm);
			return;
		}
		if(c == reverseCmd) {
			int n1 = listLangIn.getSelectedIndex();
			int n2 = listLangOut.getSelectedIndex();
			listLangIn.setSelectedIndex(n2, true);
			listLangOut.setSelectedIndex(n1, true);
			from = Languages.getLangFromIndex(n2)[0];
			to = Languages.getLangFromIndex(n1)[0];
			inField.setString(inputText = outField.getString());
			outField.setString("");
			setLangInBtn.setText("In: " + Languages.getLangFromIndex(n2)[1]);
			setLangOutBtn.setText("Out: " + Languages.getLangFromIndex(n1)[1]);
			translateThread.schedule();
			return;
		}
	}

	public void commandAction(Command c, Item item) {
		if(c == setLangInCmd) {
			display.setCurrent(listLangIn);
			return;
		}
		if(c == setLangOutCmd) {
			display.setCurrent(listLangOut);
			return;
		}
		if(c == ttsCmd) {
			String s = ((TextField)item).getString();
			if(s == null) return;
			playTts(item == inField ? from : to, s);
			return;
		}
		if(c == copyCmd) {
			String s = ((TextField)item).getString();
			try {
				Clipboard.copyToClipboard(s);
			} catch (Throwable e) {}
			return;
		}
		if(c == pasteCmd) {
			try {
				((TextField)item).setString(Clipboard.copyFromClipboard());
			} catch (Throwable e) {}
			return;
		}
	}
	
	private void updateLangs() {
		listLangIn.deleteAll();
		listLangOut.deleteAll();
		String[] a = Languages.getLangNames();
		for(int i = 0; i < a.length; i++) {
			listLangIn.append(a[i], null);
			listLangOut.append(a[i], null);
		}
		listLangIn.setSelectedIndex(Languages.getFromIndex(), true);
		listLangOut.setSelectedIndex(Languages.getToIndex(), true);
		from = Languages.getLangFromIndex(Languages.getFromIndex())[0];
		to = Languages.getLangFromIndex(Languages.getToIndex())[0];
	}

	public void setDownloading(boolean b) {
		mainForm.setTicker(b ? new Ticker("Loading languages..") : null);
	}

	public void downloadingError(String s) {
		Alert a = new Alert("Downloading error");
		a.setType(AlertType.ERROR);
		a.setString(s);
		a.setTimeout(3000);
		display.setCurrent(a, mainForm);
	}

	public void setLanguages(String[][] l) {
		Languages.setSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
		Languages.setDownloaded(l);
		Languages.updateLangs();
		updateLangs();
		Languages.save();
	}

	public void downloadingDone() {
		
	}

	public void setTranslating(boolean b) {
		mainForm.setTicker(b ? new Ticker("Translating..") : null);
	}
	
	private void playTts(String lang, String s) {
		if(s.trim().length() == 0) return;
		if(ttsPlaying) return;
		ttsPlaying = true;
		mainForm.setTicker(new Ticker("Listening.."));
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			String url = "https://" + Languages.getInstance() + "/api/tts/?engine="
			//+ Languages.getCurrentEngine()
					+ "google"
			+ "&lang=" + lang + "&text=" + Util.encodeURL(s);
			if(Languages.getProxy() != null && Languages.getProxy().length() > 0) {
				url = Languages.getProxy() + Util.encodeURL(url);
			}
			HttpConnection hc = (HttpConnection) Connector.open(url);
			hc.setRequestMethod("GET");
			InputStream is = hc.openInputStream();
			byte[] b = new byte[1024];
			int i;
			while ((i = is.read(b)) != -1) {
				bos.write(b, 0, i);
			}
			is.close();
			hc.close();
			ttsplayer = Manager.createPlayer(new ByteArrayInputStream(bos.toByteArray()), "audio/mpeg");
			bos.close();
			ttsplayer.realize();
			ttsplayer.prefetch();
			((VolumeControl) ttsplayer.getControl("VolumeControl")).setLevel(100);
			ttsplayer.start();
			ttsplayer.addPlayerListener(this);
		} catch (Exception e) {
			e.printStackTrace();
			ttsPlaying = false;
			mainForm.setTicker(null);
			downloadingError(e.toString());
		}
	}
	
	public void playerUpdate(Player p, String event, Object eventData) {
		if(END_OF_MEDIA.equals(event) || STOPPED.equals(event)) {
			if(ttsplayer == null) return;
			if(ttsPlaying) {
				mainForm.setTicker(null);
			}
			ttsPlaying = false;
			ttsplayer.deallocate();
			ttsplayer.close();
			ttsplayer = null;
		}
	}

}
