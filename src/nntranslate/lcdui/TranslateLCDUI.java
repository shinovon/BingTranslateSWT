package nntranslate.lcdui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

import org.eclipse.ercp.swt.mobile.TaskTip;
import org.eclipse.swt.SWT;

import nntranslate.ITranslateUI;
import nntranslate.Languages;
import nntranslate.v2.TranslateMIDlet;
import nntranslate.TranslateThread;
import nntranslate.Util;
//TODO: lcdui part
public class TranslateLCDUI implements Runnable, ITranslateUI, CommandListener, ItemCommandListener, ItemStateListener {

	private Display display;
	private Form form;
	
	private Command translateCmd = new Command("Translate", Command.OK, 3);
	private Command setLangInCmd = new Command("Change in", Command.OK, 1);
	private Command setLangOutCmd = new Command("Change out", Command.OK, 1);
	private Command listChangeCmd = new Command("Change", Command.OK, 1);
	private Command langsDoneCmd = new Command("Done", Command.OK, 1);
	private Command exitCmd = new Command("Exit", Command.EXIT, 1);
	private Command settingsCmd = new Command("Settings", Command.SCREEN, 3);
	private Command aboutCmd = new Command("About", Command.SCREEN, 5);
	private Command ttsCmd = new Command("Listen", Command.ITEM, 4);

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
	private String outText;
	
	private boolean ttsPlaying;
	private Player ttsplayer;
	
	public TranslateLCDUI() {
		new Thread(this).start();
	}

	public void run() {
		listLangIn = new List("Input language", List.EXCLUSIVE);
		listLangOut = new List("Output language", List.EXCLUSIVE);
		String[] a = Languages.getLangNames();
		for(int i = 0; i < a.length; i++) {
			listLangIn.append(a[i], null);
			listLangOut.append(a[i], null);
		}
		listLangIn.addCommand(listChangeCmd);
		listLangOut.addCommand(listChangeCmd);
		listLangIn.setSelectedIndex(Languages.getFromIndex(), true);
		listLangOut.setSelectedIndex(Languages.getToIndex(), true);
		from = Languages.getLangFromIndex(Languages.getFromIndex())[1];
		to = Languages.getLangFromIndex(Languages.getToIndex())[1];
		listLangIn.setCommandListener(this);
		listLangOut.setCommandListener(this);
		
		display = Display.getDisplay(TranslateMIDlet.midlet);
		translateThread.start();
		
		form = new Form("Bing Translate v2");
		form.addCommand(translateCmd);
		form.addCommand(exitCmd);
		form.addCommand(settingsCmd);
		form.addCommand(aboutCmd);
		form.setCommandListener(this);
		
		form.append(inField = new TextField("", "", 512, TextField.ANY));
		inField.setLabel("Input");
		inField.addCommand(ttsCmd);
		
		form.append(outField = new TextField("", "", 512, TextField.ANY | TextField.UNEDITABLE));
		outField.setLabel("Output");
		outField.addCommand(ttsCmd);
		
		form.append(setLangInBtn = new StringItem("", "", StringItem.BUTTON));
		setLangInBtn.setText("In: " + Languages.getLangFromIndex(listLangIn.getSelectedIndex())[0]);
		setLangInBtn.setDefaultCommand(setLangInCmd);
		setLangInBtn.setItemCommandListener(this);
		form.append(setLangOutBtn = new StringItem("", "", StringItem.BUTTON));
		setLangOutBtn.setText("Out: " + Languages.getLangFromIndex(listLangOut.getSelectedIndex())[0]);
		setLangOutBtn.setDefaultCommand(setLangOutCmd);
		setLangOutBtn.setItemCommandListener(this);
		display.setCurrent(form);
	}

	public String getText() {
		if(inputText == null) {
			inputText = inField.getString();
		}
		return inputText;
	}

	public void setText(String s) {
		outField.setString(outText = s);
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
			translateThread.schedule();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == translateCmd) {
			inputText = inField.getString();
			translateThread.now();
			return;
		}
		if(c == exitCmd) {
			exit();
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
	}

	public void setDownloading(boolean b) {
		form.setTicker(b ? new Ticker("Loading languages..") : null);
	}

	public void downloadingError(String s) {
		Alert a = new Alert("Downloading error");
		a.setType(AlertType.ERROR);
		a.setString(s);
		a.setTimeout(3000);
		display.setCurrent(a);
	}

	public void setLanguages(String[][] l) {
		
	}

	public void downloadingDone() {
		
	}

	public void setTranslating(boolean b) {
		form.setTicker(b ? new Ticker("Translating..") : null);
	}
	
	private void playTts(String lang, String s) {
		if(s.trim().length() == 0) return;
		if(ttsPlaying) return;
		ttsPlaying = true;
		form.setTicker(new Ticker("Listening"));
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
			ttsplayer.addPlayerListener(new PlayerListener() {
				public void playerUpdate(Player p, String event, Object eventData) {
					if(END_OF_MEDIA.equals(event) || STOPPED.equals(event)) {
						if(ttsplayer == null) return;
						if(ttsPlaying) {
							form.setTicker(null);
						}
						ttsPlaying = false;
						ttsplayer.deallocate();
						ttsplayer.close();
						ttsplayer = null;
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			ttsPlaying = false;
			form.setTicker(null);
			downloadingError(e.toString());
		}
	}

}
