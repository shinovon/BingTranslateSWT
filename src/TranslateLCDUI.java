/*
 * Copyright (c) 2021-2026 Arman Jussupgaliyev
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

//#ifndef NO_NOKIAUI
import com.nokia.mid.ui.Clipboard;
//#endif

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
//#ifndef NO_NOKIAUI
	private static final Command copyCmd = new Command("Copy", Command.ITEM, 2);
	private static final Command pasteCmd = new Command("Paste", Command.ITEM, 3);
//#endif
	
	private static final Command listDoneCmd = new Command("Done", Command.OK, 1);
	
	private static final Command backCmd = new Command("Back", Command.BACK, 1);
	
	private static final Command hyperlinkCmd = new Command("Open", Command.ITEM, 2);
	
//#ifndef NO_NOKIAUI
	private static boolean clipboard;
//#endif

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
	private ChoiceGroup proxyChoice;
	
	public TranslateLCDUI() {
		new Thread(this).start();
	}

	public void run() {
		display = Display.getDisplay(Translate.midlet);

//#ifndef NO_NOKIAUI
		clipboard = false;
		try {
			if (System.getProperty("com.nokia.mid.ui.version") != null) {
				Class.forName("com.nokia.mid.ui.Clipboard");
				clipboard = true;
			}
		} catch (Throwable ignored) {}
//#endif
		
		listLangIn = new List("Input language", List.EXCLUSIVE);
		listLangOut = new List("Output language", List.EXCLUSIVE);
		listLangIn.addCommand(listDoneCmd);
		listLangOut.addCommand(listDoneCmd);
		listLangIn.setCommandListener(this);
		listLangOut.setCommandListener(this);
		
		updateLangs();
		
		translateThread.start();
		translateThread.setEngine(Translate.engine);
		translateThread.setInstance(Translate.instance);
		translateThread.setProxy(Translate.proxyUrl);
		if (Translate.needDownload()) {
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
//#ifndef NO_NOKIAUI
		if (clipboard) {
			inField.addCommand(copyCmd);
			inField.addCommand(pasteCmd);
		}
//#endif
		
		mainForm.append(outField = new TextField("Output", "", 500, TextField.ANY | TextField.UNEDITABLE));
		outField.setItemCommandListener(this);
		outField.addCommand(ttsCmd);
//#ifndef NO_NOKIAUI
		if (clipboard) {
			outField.addCommand(copyCmd);
		}
//#endif
		
		mainForm.append(setLangInBtn = new StringItem("", "", StringItem.BUTTON));
		setLangInBtn.setText("In: " + Translate.langs[listLangIn.getSelectedIndex()][1]);
		setLangInBtn.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		setLangInBtn.setDefaultCommand(setLangInCmd);
		setLangInBtn.setItemCommandListener(this);
		mainForm.append(setLangOutBtn = new StringItem("", "", StringItem.BUTTON));
		setLangOutBtn.setText("Out: " + Translate.langs[listLangOut.getSelectedIndex()][1]);
		setLangOutBtn.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
		setLangOutBtn.setDefaultCommand(setLangOutCmd);
		setLangOutBtn.setItemCommandListener(this);
		display.setCurrent(mainForm);
	}

	public String getText() {
		if (inputText == null) {
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
		Translate.midlet.notifyDestroyed();
	}

	public void itemStateChanged(Item item) {
		if (item == inField) {
			inputText = inField.getString();
			if (inputText.trim().length() == 0)
				return;
			translateThread.schedule();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == translateCmd) {
			inputText = inField.getString();
			translateThread.now();
			return;
		}
		if (c == listDoneCmd) {
			Translate.setSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
			from = Translate.langs[Translate.getFromIndex()][0];
			to = Translate.langs[Translate.getToIndex()][0];
			setLangInBtn.setText("In: " + Translate.langs[Translate.getFromIndex()][1]);
			setLangOutBtn.setText("Out: " + Translate.langs[Translate.getToIndex()][1]);
			display.setCurrent(mainForm);
			return;
		}
		if (c == exitCmd) {
			exit();
			return;
		}
		if (c == settingsCmd) {
			if (settingsForm == null) {
				settingsForm = new Form("Settings");
				settingsForm.addCommand(backCmd);
				settingsForm.setCommandListener(this);
				
				String[] engines = Translate.ENGINES;
				String curEngine = Translate.engine;
				engineChoice = new ChoiceGroup("Translate engine", Choice.POPUP, engines, null);
				for (int i = 0; i < engines.length; i++) {
					if (engines[i].equalsIgnoreCase(curEngine)) {
						engineChoice.setSelectedIndex(i, true);
						break;
					}
				}
				settingsForm.append(engineChoice);
				
				instanceField = new TextField("Instance", Translate.instance, 100, TextField.ANY);
				settingsForm.append(instanceField);
				
				proxyField = new TextField("Proxy URL", Translate.proxyUrl, 100, TextField.ANY);
				settingsForm.append(proxyField);
				
				String[] settings;
				if (Translate.blackberry) {
					settings = new String[] { "Use proxy", "Use Wi-Fi" };
				} else {
					settings = new String[] { "Use proxy" };
				}
				
				proxyChoice = new ChoiceGroup("", ChoiceGroup.MULTIPLE, settings, null);
				proxyChoice.setSelectedIndex(0, Translate.useProxy);
				if (Translate.blackberry) proxyChoice.setSelectedIndex(1, Translate.blackberryWifi);
				settingsForm.append(proxyChoice);
			}
			display.setCurrent(settingsForm);
			return;
		}
		if (c == aboutCmd) {
			Form f = new Form("About");
			f.addCommand(backCmd);
			f.setCommandListener(this);
			StringItem s;
			try {
				f.append(new ImageItem(null, Image.createImage("/icon.png"), Item.LAYOUT_LEFT, null));
				s = new StringItem(null, "Translate v" + Translate.version);
				s.setFont(Font.getFont(0, 0, Font.SIZE_LARGE));
				s.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_VCENTER);
				f.append(s);
			} catch (IOException ignored) {}
			s = new StringItem(null, "J2ME online translator app\n\n");
			s.setFont(Font.getDefaultFont());
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			f.append(s);
			s = new StringItem("Developed by", "shinovon");
			s.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
			f.append(s);
			s = new StringItem("Web", "nnproject.cc", Item.HYPERLINK);
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
		if (c == clearLangsCmd) {
			Translate.deleteAllLangs();
			translateThread.setDownload();
			translateThread.now();
			display.setCurrent(mainForm);
			return;
		}
		if (c == backCmd) {
			if (d == settingsForm) {
				String engine = Translate.ENGINES[engineChoice.getSelectedIndex()].toLowerCase();
				String inst = instanceField.getString();
				String proxy = proxyField.getString();
				Translate.useProxy = proxyChoice.isSelected(0);
				if (Translate.blackberry) Translate.blackberryWifi = proxyChoice.isSelected(1);
				
				if (!inst.equals(Translate.instance)) {
					Translate.instance = inst;
				    Translate.deleteAllLangs();
					Translate.setCurrentEngine(engine);
					Translate.proxyUrl = proxy;
					translateThread.setInstance(inst);
					translateThread.setEngine(engine);
					translateThread.setProxy(proxy);
					translateThread.setDownload();
					translateThread.now();
				} else {
					Translate.setSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
					Translate.setCurrentEngine(engine);
					Translate.proxyUrl = proxy;
					translateThread.setEngine(engine);
					translateThread.setProxy(proxy);
					if (Translate.needDownload()) {
						translateThread.setDownload();
						translateThread.now();
					} else {
						updateLangs();
					}
				}
				Translate.save();
			}
			display.setCurrent(mainForm);
			return;
		}
		if (c == reverseCmd) {
			int n1 = listLangIn.getSelectedIndex();
			int n2 = listLangOut.getSelectedIndex();
			listLangIn.setSelectedIndex(n2, true);
			listLangOut.setSelectedIndex(n1, true);
			from = Translate.langs[n2][0];
			to = Translate.langs[n1][0];
			inField.setString(inputText = outField.getString());
			outField.setString("");
			setLangInBtn.setText("In: " + Translate.langs[n2][1]);
			setLangOutBtn.setText("Out: " + Translate.langs[n1][1]);
			translateThread.schedule();
			return;
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == setLangInCmd) {
			display.setCurrent(listLangIn);
			return;
		}
		if (c == setLangOutCmd) {
			display.setCurrent(listLangOut);
			return;
		}
		if (c == ttsCmd) {
			String s = ((TextField)item).getString();
			if (s == null) return;
			playTts(item == inField ? from : to, s);
			return;
		}
		if (c == hyperlinkCmd) {
			try {
				if (Translate.midlet.platformRequest("http://" + ((StringItem) item).getText()))
					Translate.midlet.notifyDestroyed();
			} catch (Exception ignored) {}
			return;
		}
//#ifndef NO_NOKIAUI
		if (!clipboard) return;
		if (c == copyCmd) {
			try {
				clipboard((TextField) item, true);
			} catch (Throwable ignored) {}
			return;
		}
		if (c == pasteCmd) {
			try {
				clipboard((TextField) item, false);
			} catch (Throwable ignored) {}
			return;
		}
//#endif
	}
	
//#ifndef NO_NOKIAUI
	private static void clipboard(TextField item, boolean b) {
		if (!clipboard) return;
		if (b) {
			String s = ((TextField)item).getString();
			try {
				Clipboard.copyToClipboard(s);
			} catch (Throwable ignored) {}
			return;
		}
		try {
			((TextField)item).setString(Clipboard.copyFromClipboard());
		} catch (Throwable ignored) {}
	}
//#endif
	
	private void updateLangs() {
		listLangIn.deleteAll();
		listLangOut.deleteAll();
		String[] a = Translate.langNames;
		for (int i = 0; i < a.length; i++) {
			listLangIn.append(a[i], null);
			listLangOut.append(a[i], null);
		}
		listLangIn.setSelectedIndex(Translate.getFromIndex(), true);
		listLangOut.setSelectedIndex(Translate.getToIndex(), true);
		from = Translate.langs[Translate.getFromIndex()][0];
		to = Translate.langs[Translate.getToIndex()][0];
	}

	public void setDownloading(boolean b) {
		mainForm.setTicker(b ? new Ticker("Loading TranslateMIDlet..") : null);
	}

	public void downloadingError(String s) {
		Alert a = new Alert("Downloading error");
		a.setType(AlertType.ERROR);
		a.setString(s);
		a.setTimeout(3000);
		display.setCurrent(a, mainForm);
	}

	public void setLanguages(String[][] l) {
		Translate.setSelected(listLangIn.getSelectedIndex(), listLangOut.getSelectedIndex());
		Translate.setDownloaded(l);
		Translate.updateLangs();
		updateLangs();
		Translate.save();
	}

	public void downloadingDone() {
		
	}

	public void setTranslating(boolean b) {
		mainForm.setTicker(b ? new Ticker("Translating..") : null);
	}
	
	private void playTts(String lang, String s) {
		if (s.trim().length() == 0) return;
		if (ttsPlaying) return;
		ttsPlaying = true;
		mainForm.setTicker(new Ticker("Listening.."));
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			String url = Translate.instance + "/api/tts/?engine="
			//+ TranslateMIDlet.getCurrentEngine()
					+ "google"
			+ "&lang=" + lang + "&text=" + Translate.encodeURL(s);
			if (Translate.proxyUrl != null && Translate.proxyUrl.length() > 0) {
				url = Translate.proxyUrl + Translate.encodeURL(url);
			}
			HttpConnection hc = (HttpConnection) Translate.open(url);
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
		if (END_OF_MEDIA.equals(event) || STOPPED.equals(event)) {
			if (ttsplayer == null) return;
			if (ttsPlaying) {
				mainForm.setTicker(null);
			}
			ttsPlaying = false;
			ttsplayer.deallocate();
			ttsplayer.close();
			ttsplayer = null;
		}
	}

}
