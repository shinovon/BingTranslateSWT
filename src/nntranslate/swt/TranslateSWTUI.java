package nntranslate.swt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.media.PlayerListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.media.Manager;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.ercp.swt.mobile.MobileDevice;
import org.eclipse.ercp.swt.mobile.MobileShell;
import org.eclipse.ercp.swt.mobile.QueryDialog;
import org.eclipse.ercp.swt.mobile.ScreenEvent;
import org.eclipse.ercp.swt.mobile.ScreenListener;
import org.eclipse.ercp.swt.mobile.SortedList;
import org.eclipse.ercp.swt.mobile.TaskTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.mid.ui.Clipboard;

import nntranslate.ITranslateUI;
import nntranslate.Languages;
import nntranslate.v2.TranslateMIDlet;
import nntranslate.TranslateThread;
import nntranslate.Util;

public class TranslateSWTUI
		implements Runnable, SelectionListener, ITranslateUI, ScreenListener, ControlListener, FocusListener, TraverseListener, PaintListener, ModifyListener, PlayerListener {
	
	public static final String name = "Translate v2";
	public static final String setsrms = "gtsets";

	private static final String model = System.getProperty("microedition.platform");
	private static final boolean is93 = model.indexOf("n=3.2") != -1;
	private static final boolean is94 = model.indexOf("n=5.0") != -1;

	public void modifyText(ModifyEvent ev) {
		to = Languages.getLangFromIndex(comboTo.getSelectionIndex())[0];
		from = Languages.getLangFromIndex(comboFrom.getSelectionIndex())[0];
		try {
			inputString = textIn.getText();
		} catch (Throwable e) {
		}
		textIn.redraw();
		translateThread.schedule();
	}

	private final SelectionListener selectionListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent ev) {
			if(fullscreenLangs && !is93) {

				int in;
				int out;
				if(ev.widget == inLangsList) {

					comboReset();
					
					Languages.setSelected(in = Languages.getLangFromName(inLangsList.getSelection()[0]),
							out = comboTo.getSelectionIndex());
					to = Languages.getLangFromIndex(in)[0];
					from = Languages.getLangFromIndex(out)[0];
					comboFrom.select(in);
					Languages.save();
					
					translateThread.clearLastInput();
					inLangsList.dispose();
					inLangsList = null;
					inLangsShell.setVisible(false);
					inLangsShell.dispose();
					inLangsShell = null;
					inLangsDoneCmd.dispose();
					inLangsDoneCmd = null;
					shell.forceActive();
					shell.forceFocus();
					textIn.forceFocus();
					comboFrom.setVisible(true);
				} else if(ev.widget == outLangsList) {
					comboReset();
					
					Languages.setSelected(in = comboFrom.getSelectionIndex(), 
							out = Languages.getLangFromName(outLangsList.getSelection()[0]));
					to = Languages.getLangFromIndex(in)[0];
					from = Languages.getLangFromIndex(out)[0];
					comboTo.select(out);
					Languages.save();
					
					translateThread.clearLastInput();
					outLangsList.dispose();
					outLangsList = null;
					outLangsShell.setVisible(false);
					outLangsShell.dispose();
					outLangsShell = null;
					outLangsDoneCmd.dispose();
					outLangsDoneCmd = null;
					shell.forceActive();
					shell.forceFocus();
					textIn.forceFocus();
					comboTo.setVisible(true);
				}
				translateThread.schedule();
			}
		}

		public void widgetSelected(SelectionEvent ev) {
			int in;
			int out;
			/*
			 * try { inputText = textIn.getText(); } catch (Throwable e) { }
			 */
			if (ev.widget instanceof Combo) {
				Languages.setSelected(in = comboFrom.getSelectionIndex(),
						out = comboTo.getSelectionIndex());
				to = Languages.getLangFromIndex(in)[0];
				from = Languages.getLangFromIndex(out)[0];
				Languages.save();
				translateThread.clearLastInput();
				translateThread.schedule();
			} else if (ev.widget == outLangsDoneCmd) {
				
				comboReset();
				/*
				Languages.setSelected(in = comboFrom.getSelectionIndex(), 
						out = Languages.getLangFromName(outLangsList.getSelection()[0]));
				to = Languages.getLangFromIndex(in)[1];
				from = Languages.getLangFromIndex(out)[1];
				comboTo.select(out);
				Languages.save();
				translateThread.clearLastInput();
				*/
				outLangsList.dispose();
				outLangsList = null;
				outLangsShell.setVisible(false);
				outLangsShell.dispose();
				outLangsShell = null;
				outLangsDoneCmd.dispose();
				outLangsDoneCmd = null;
				shell.forceActive();
				shell.forceFocus();
				textIn.forceFocus();
				comboTo.setVisible(true);
			} else if (ev.widget == inLangsDoneCmd) {
				comboReset();
				/*
				Languages.setSelected(in = Languages.getLangFromName(inLangsList.getSelection()[0]),
						out = comboTo.getSelectionIndex());
				to = Languages.getLangFromIndex(in)[1];
				from = Languages.getLangFromIndex(out)[1];
				comboFrom.select(in);
				Languages.save();
				translateThread.clearLastInput();
				*/
				inLangsList.dispose();
				inLangsList = null;
				inLangsShell.setVisible(false);
				inLangsShell.dispose();
				inLangsShell = null;
				inLangsDoneCmd.dispose();
				inLangsDoneCmd = null;
				shell.forceActive();
				shell.forceFocus();
				textIn.forceFocus();
				comboFrom.setVisible(true);
			} else if(ev.widget == inLangsList) {
				Languages.setSelected(in = Languages.getLangFromName(inLangsList.getSelection()[0]),
						out = comboTo.getSelectionIndex());
				to = Languages.getLangFromIndex(in)[0];
				from = Languages.getLangFromIndex(out)[0];
				comboFrom.select(in);
			} else if(ev.widget == outLangsList) {
				Languages.setSelected(in = comboFrom.getSelectionIndex(), 
						out = Languages.getLangFromName(outLangsList.getSelection()[0]));
				to = Languages.getLangFromIndex(in)[0];
				from = Languages.getLangFromIndex(out)[0];
				comboTo.select(out);
			}
		}
	};

	protected String inputString;

	Display display;
	MobileShell shell;

	public boolean exiting;

	Composite parent;

	Command exitcmd;

	Combo comboFrom;
	Combo comboTo;

	Text textIn;
	Text textOut;

	Button reverseBtn;

	Composite centerComp;
	Button clearBtn;
	Button copyOutBtn;
	Button pasteInBtn;

	TranslateThread translateThread = new TranslateThread(this);

	String from;
	String to;

	Composite textComp;
	Composite textCenterComp;
	
	Composite fromComp;
	Composite toComp;
	Button ttsFromBtn;
	Button ttsToBtn;
	Button clearOutBtn;
	Button clearInBtn;

	//Shell langsShell;
	//SortedList langsList;

	// boolean landscape;

	//Command langsDoneCmd;

	MenuItem aboutMenuItem;
	MenuItem fullListMenuItem;
	//MenuItem langsMenuItem;
	boolean fullscreenLangs;
	boolean bingDesign = true;
	MenuItem engineMenuItem;
	MenuItem[] menuEngines;
	MenuItem instMenuItem;
	MenuItem proxyMenuItem;
	MenuItem reverseMenuItem;
	MenuItem uiMenuItem;
	MenuItem fontMenuItem;
	MenuItem clearLangsMenuItem;
	
	Command ttsincmd;
	Command ttsoutcmd;
	Command copyincmd;
	Command pasteincmd;
	Command copyoutcmd;
	Command clearoutcmd;

	boolean pc;

	Button copyInBtn;

	FontData font;

	boolean ttsPlaying;
	Player ttsplayer;
	String outString;

	Shell outLangsShell;
	SortedList outLangsList;
	Shell inLangsShell;
	SortedList inLangsList;
	Command inLangsDoneCmd;
	Command outLangsDoneCmd;
	
	long comboInitTime;

	TaskTip dlTask;
	protected TaskTip errorTask;
	TaskTip ttstask;
	
	public TranslateSWTUI() {
		new Thread(this, "Main SWT Thread").start();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		/*
		if (e.widget == langsList) {
			updateLangs();
		}
		*/
	}
/*
	private void updateLangs() {
		//Languages.setSelected(langsList.getSelection());
		Languages.setSelected(comboFrom.getSelectionIndex(), comboTo.getSelectionIndex());
		Languages.updateLangs();
		//String st = comboTo.getText();
		//String sf = comboFrom.getText();
		comboFrom.setItems(Languages.getLangNames());
		comboTo.setItems(Languages.getLangNames());
		comboFrom.select(Languages.getFromIndex());
		comboTo.select(Languages.getToIndex());
		//Languages.setSelected(fi, ti);
		Languages.save();
	}
*/
	private void updateLangsPosition() {
		Rectangle bgdBnds = shell.getBounds();
		//if (langsShell != null)
		//	langsShell.setBounds(bgdBnds.x + 1, bgdBnds.y + 1, bgdBnds.width - 1, bgdBnds.height - 1);
		if (outLangsShell != null)
			outLangsShell.setBounds(bgdBnds.x + 1, bgdBnds.y + 1, bgdBnds.width - 1, bgdBnds.height - 1);
		if (inLangsShell != null)
			inLangsShell.setBounds(bgdBnds.x + 1, bgdBnds.y + 1, bgdBnds.width - 1, bgdBnds.height - 1);
	}

	public void widgetSelected(SelectionEvent ev) {
		if (ev.widget == exitcmd) {
			exit();
			return;
		}
		/*
		if (ev.widget == langsMenuItem) {
			if (langsShell == null) {
				langsShell = new Shell(shell, SWT.BORDER | SWT.TITLE | SWT.MODELESS);
				langsShell.setText("Languages");
				langsShell.setLayout(new FillLayout());
				langsShell.addControlListener(this);
				langsList = new SortedList(langsShell, SWT.MULTI | SWT.V_SCROLL, SortedList.FILTER);
				langsList.setItems(Languages.SUPPORTED_LANGUAGE_NAMES);
				langsList.setSelection(Languages.getLangNames());
				langsDoneCmd = new Command(langsShell, Command.EXIT, 1);
				langsDoneCmd.setText("Done");
				langsDoneCmd.addSelectionListener(this);
			}
			langsShell.setVisible(true);
			langsShell.forceActive();
			langsList.showSelection();
			langsList.forceFocus();
		}
		*/
		/*
		if (ev.widget == langsDoneCmd) {
			updateLangs();
			langsDoneCmd.dispose();
			langsList.dispose();
			langsList = null;
			langsShell.setVisible(false);
			langsShell.dispose();
			langsShell = null;
			shell.forceActive();
			shell.forceFocus();
		}
		*/
		if (/* ev.widget == aboutcmd || */ev.widget == aboutMenuItem) {
			msg(name + " v"+TranslateMIDlet.midlet.getAppProperty("MIDlet-Version")+"\nBy shinovon (nnproject)");
			return;
		}
		if (ev.widget == copyOutBtn || ev.widget == copyoutcmd) {
			textOut.copy();
			try {
				Clipboard.copyToClipboard(textOut.getText());
			} catch (Throwable e) {
			}
			return;
		}
		if (ev.widget == copyInBtn || ev.widget == copyincmd) {
			textIn.copy();
			try {
				Clipboard.copyToClipboard(textIn.getText());
			} catch (Throwable e) {
			}
			return;
		}
		if (ev.widget == pasteInBtn || ev.widget == pasteincmd ) {
			textIn.paste();
			return;
		}
		if (ev.widget == clearBtn) {
			textIn.setText("");
			textOut.setText("");
			return;
		}
		if (ev.widget == clearInBtn) {
			textIn.setText("");
			return;
		}
		if (ev.widget == clearOutBtn || ev.widget == clearoutcmd) {
			textOut.setText("");
			return;
		}
		if (ev.widget == reverseBtn || ev.widget == reverseMenuItem) {
			int newIn = comboFrom.getSelectionIndex();
			int newOut = comboTo.getSelectionIndex();
			comboFrom.select(newOut);
			comboTo.select(newIn);
			to = Languages.getLangFromIndex(newIn)[0];
			from = Languages.getLangFromIndex(newOut)[0];
			String outText = "";
			try {
				outText = textOut.getText();
				inputString = outText;
			} catch (Exception e) {
			}
			try {
				textIn.setText(outText);
				textOut.setText("");
			} catch (Exception e) {
			}
			translateThread.schedule();
			return;
		}
		if(ev.widget == ttsFromBtn || ev.widget == ttsincmd) {
			/*String s = Languages.getCurrentEngine();
			if(!s.equals("google")) {
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage("TTS is only available for Google");
				mb.open();
			} else {*/
			String ss = inputString;
			try {
				inputString = ss = textIn.getText();
			} catch (Exception e) {
			}
			if(ss != null) playTts(from, ss);
				
			//}
			return;
		}
		if(ev.widget == ttsToBtn || ev.widget == ttsoutcmd) {
			/*
			String s = Languages.getCurrentEngine();
			if(!s.equals("google")) {
				MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				mb.setMessage("TTS is only available for Google");
				mb.open();
			} else {
			*/
			String ss = null;
			try {
				ss = textOut.getText();
			} catch (Exception e) {
			}
			if(ss != null) playTts(to, ss);
			//}
			return;
		}
		if (ev.widget == fullListMenuItem) {
			fullscreenLangs = fullListMenuItem.getSelection();
			saveSets();
			comboFrom.dispose();
			comboFrom = null;
				if(reverseBtn != null) {
				reverseBtn.dispose();
				reverseBtn = null;
			}
			comboTo.dispose();
			comboTo = null;
			
			initCombos(false);
			
			reinit(-1);
			return;
		}
		if(ev.widget == instMenuItem) {
			QueryDialog dialog = new QueryDialog(shell, SWT.NONE,
					QueryDialog.STANDARD);
			dialog.setPromptText("Instance:", Languages.getInstance());
			String s = dialog.open();
			if (s == null) {
				return;
			}
			Languages.setInstance(s);
			translateThread.setInstance(s);
		    Languages.save();
		    Languages.deleteAllLangs();
			translateThread.setDownload();
			translateThread.now();
			return;
		}
		if(ev.widget == fontMenuItem) {
			FontDialog fd = new FontDialog(shell);
			if(font != null) {
				fd.setFontList(new FontData[] { font });
			}
			FontData font = fd.open();
			if(font != null) {
				if(textIn != null) {
					textIn.setFont(new Font(display, font));
				}
				if(textOut != null) {
					textOut.setFont(new Font(display, font));
				}
				this.font = font;
			}
			return;
		}
		if(ev.widget == uiMenuItem) {
			bingDesign = uiMenuItem.getSelection();
			saveSets();
			reinit(-1);
			return;
		}
		if(ev.widget == proxyMenuItem) {
			QueryDialog dialog = new QueryDialog(shell, SWT.NONE,
					QueryDialog.STANDARD);
			String s = Languages.getProxy();
			if(s == null) s = "";
			dialog.setPromptText("Proxy URL:", s);
			s = dialog.open();
			if (s == null) {
				return;
			}
			Languages.setProxy(s);
			translateThread.setProxy(s);
		    Languages.save();
			return;
		}
		if(ev.widget == clearLangsMenuItem) {
			Languages.deleteAllLangs();
			translateThread.setDownload();
			translateThread.now();
			return;
		}
		if(ev.widget instanceof MenuItem) {
			String engine = ((MenuItem) ev.widget).getText().toLowerCase();
			Languages.setSelected(comboFrom.getSelectionIndex(), comboTo.getSelectionIndex());
			Languages.setCurrentEngine(engine);
			translateThread.setEngine(engine);
		    Languages.save();
			if(Languages.needDownload()) {
				translateThread.setDownload();
				translateThread.now();
			} else {
				try {
					comboFrom.setItems(Languages.getLangNames());
					comboTo.setItems(Languages.getLangNames());
					comboFrom.select(Languages.getFromIndex());
					comboTo.select(Languages.getToIndex());
					Languages.save();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private void playTts(String lang, String s) {
		if(s.trim().length() == 0) return;
		if(ttsPlaying) return;
		ttsPlaying = true;
		if(ttstask == null)
			ttstask = new TaskTip(shell, SWT.NONE);
		ttstask.setText("Listening");
		ttstask.setVisible(true);
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
			ttstask.setVisible(false);
			ttstask.dispose();
			ttstask = null;
			downloadingError(e.toString());
			ttsPlaying = false;
		}
	}
	
	public void playerUpdate(Player p, String event, Object eventData) {
		if(END_OF_MEDIA.equals(event) || STOPPED.equals(event)) {
			if(ttsplayer == null) return;
			if(ttstask != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						ttstask.setVisible(false);
						ttstask.dispose();
						ttstask = null;
					}
				});
			}
			ttsPlaying = false;
			ttsplayer.deallocate();
			ttsplayer.close();
			ttsplayer = null;
		}
	}

	private void saveSets() {
		try {
			RecordStore.deleteRecordStore(setsrms);
		} catch (Exception e) {
		}
		try {
			RecordStore r = RecordStore.openRecordStore(setsrms, true);
			byte[] b = new byte[] { (byte) (fullscreenLangs ? 1 : 0), (byte) (bingDesign ? 1 : 0) };
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
		}
	}

	public void msg(final String s) {
		display.syncExec(new Runnable() {
			public void run() {
				MessageBox msg = new MessageBox(shell, SWT.NONE);
				msg.setMessage(s);
				msg.open();
			}
		});
	}
	
	public Image getResImg(String s) {
		s = "/" + s + ".png";
		return new Image(display, TranslateSWTUI.class.getResourceAsStream(s));
	}

	public void run() {
		try {
			RecordStore r = RecordStore.openRecordStore(setsrms, false);
			byte[] b = r.getRecord(1);
			r.closeRecordStore();
			if (b.length >= 1) fullscreenLangs = b[0] == 1;
			if (b.length >= 2) bingDesign = b[1] == 1;
		} catch (Exception e) {
		}
		display = new Display();
		translateThread.start();
		translateThread.setEngine(Languages.getCurrentEngine());
		translateThread.setInstance(Languages.getInstance());
		translateThread.setProxy(Languages.getProxy());
		if(Languages.needDownload()) {
			translateThread.setDownload();
			translateThread.now();
		}
		try {
			MobileDevice.getMobileDevice().getScreens()[0].addEventListener(this);
		} catch (Exception e) {
		}
		shell = new MobileShell(display, SWT.NONE, MobileShell.SMALL_STATUS_PANE);
		shell.setText(name);
		// shell.setImage(new Image(display,
		// getClass().getResourceAsStream("/page_exported.png")));
		shell.setLayout(new FillLayout());
		parent = new Composite(shell, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		parent.setLayout(gl);
		exitcmd = new Command(shell, Command.EXIT, 1);
		exitcmd.setText("Exit");
		exitcmd.addSelectionListener(this);
		/*
		 * String s = System.getProperty("microedition.platform");
		 * 
		 * if(s != null && s.indexOf("platform_version=3.2") > -1) { Command group = new
		 * Command(shell, Command.COMMANDGROUP, 1); group.setText("App"); aboutcmd = new
		 * Command(group, Command.GENERAL, 1); aboutcmd.setText("About");
		 * aboutcmd.addSelectionListener(this); } else {
		 */
		/*
		 * Command sets = new Command(shell, Command.COMMANDGROUP, 2);
		 * sets.setText("Settings"); Command text = new Command(shell,
		 * Command.COMMANDGROUP, 3); text.setText("Text"); langscmd = new Command(sets,
		 * Command.GENERAL, 1); langscmd.setText("Set languages");
		 * langscmd.addSelectionListener(this); aboutcmd = new Command(shell,
		 * Command.GENERAL, 1); aboutcmd.setText("About");
		 * aboutcmd.addSelectionListener(this); clearcmd = new Command(text,
		 * Command.GENERAL, 3); clearcmd.setText("Clear");
		 * clearcmd.addSelectionListener(this); copycmd = new Command(text,
		 * Command.GENERAL, 2); copycmd.setText("Copy output");
		 * copycmd.addSelectionListener(this); pastecmd = new Command(text,
		 * Command.GENERAL, 1); pastecmd.setText("Paste input");
		 * pastecmd.addSelectionListener(this);
		 */
		// }
		Menu menu = shell.getMenuBar();
		if (menu == null)
			menu = new Menu(shell, SWT.BAR);

		MenuItem setsMenuItem = new MenuItem(menu, SWT.CASCADE);
		setsMenuItem.setText("Settings");
		Menu setsMenu = new Menu(shell, SWT.DROP_DOWN);
		setsMenuItem.setMenu(setsMenu);
		fullListMenuItem = new MenuItem(setsMenu, SWT.CHECK);
		fullListMenuItem.addSelectionListener(this);
		fullListMenuItem.setText("Fullscreen lang. list");
		fullListMenuItem.setSelection(fullscreenLangs);
		/*langsMenuItem = new MenuItem(setsMenu, SWT.PUSH);
		langsMenuItem.addSelectionListener(this);
		langsMenuItem.setText("Select languages");
*/
		engineMenuItem = new MenuItem(setsMenu, SWT.CASCADE);
		engineMenuItem.setText("Translate engine");
		Menu enginesMenu = new Menu(shell, SWT.DROP_DOWN);
		engineMenuItem.setMenu(enginesMenu);
		String[] engines = Languages.engines;
		menuEngines = new MenuItem[engines.length];
		for(int i = 0; i < engines.length; i++) {
			MenuItem mi = createEngineItem(engines[i], enginesMenu);
			menuEngines[i] = mi;
		}
		instMenuItem = new MenuItem(setsMenu, SWT.PUSH);
		instMenuItem.setText("Set ST Instance");
		instMenuItem.addSelectionListener(this);
		
		proxyMenuItem = new MenuItem(setsMenu, SWT.PUSH);
		proxyMenuItem.setText("Set proxy");
		proxyMenuItem.addSelectionListener(this);
		
		clearLangsMenuItem = new MenuItem(setsMenu, SWT.PUSH);
		clearLangsMenuItem.setText("Refresh languages");
		clearLangsMenuItem.addSelectionListener(this);
		
		uiMenuItem = new MenuItem(setsMenu, SWT.CHECK);
		uiMenuItem.setText("Legacy UI");
		uiMenuItem.addSelectionListener(this);
		uiMenuItem.setSelection(bingDesign);
		
		//fontMenuItem = new MenuItem(setsMenu, SWT.PUSH);
		//fontMenuItem.setText("Select font");
		//fontMenuItem.addSelectionListener(this);
		
		reverseMenuItem = new MenuItem(menu, SWT.PUSH);
		reverseMenuItem.addSelectionListener(this);
		reverseMenuItem.setText("Reverse");
		
		aboutMenuItem = new MenuItem(menu, SWT.PUSH);
		aboutMenuItem.addSelectionListener(this);
		aboutMenuItem.setText("About");

		shell.setMenuBar(menu);

		init();
		shell.open();
		comboReset();
		while (!exiting) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		translateThread.interrupt();
	}

	private MenuItem createEngineItem(String s, Menu m) {
		MenuItem i = new MenuItem(m, SWT.RADIO);
		i.addSelectionListener(this);
		i.setText(s);
		i.setSelection(Languages.getCurrentEngine().equalsIgnoreCase(s));
		return i;
	}

	public String getText() {
		/*
		 * display.syncExec(new Runnable() { public void run() { try { inputText =
		 * textIn.getText(); } catch (Throwable e) { } } });
		 */
		return inputString;
	}

	public void setText(final String s) {
		outString = s;
		display.syncExec(new Runnable() {
			public void run() {
				try {
					textOut.setText(s);
					textOut.redraw();
				} catch (Throwable e) {
					translateThread.scheduleRetext();
				}
			}
		});
	}

	public String getFromLang() {
		display.syncExec(new Runnable() {
			public void run() {
				from = Languages.getLangFromIndex(comboFrom.getSelectionIndex())[0];
			}
		});
		return from;
	}

	public String getToLang() {
		display.syncExec(new Runnable() {
			public void run() {
				to = Languages.getLangFromIndex(comboTo.getSelectionIndex())[0];
			}
		});
		return to;
	}

	private void init() {
		if(bingDesign) {
			final GridData fillHorizontal = new GridData();
			fillHorizontal.horizontalAlignment = GridData.FILL;
			fillHorizontal.grabExcessHorizontalSpace = true;
			fillHorizontal.verticalAlignment = GridData.CENTER;
	
			centerComp = new Composite(parent, SWT.NONE);
			RowLayout rowLayout = new RowLayout();
	
			rowLayout.justify = true;
			rowLayout.marginTop = 5;
			rowLayout.marginBottom = 5;
			rowLayout.marginLeft = 1;
			rowLayout.marginRight = 1;
			rowLayout.spacing = 1;
			centerComp.setLayout(rowLayout);
			centerComp.setLayoutData(fillHorizontal);
		}
		
		initCombos(false);

		reinit(-1);
		comboReset();
	}
	
	private void initCombos(boolean forceBing) {
		comboReset();
		if(comboTo != null) {
			comboTo.dispose();
			comboTo = null;
		}
		if(comboFrom != null) {
			comboFrom.dispose();
			comboFrom = null;
		}
		if(bingDesign || forceBing) {
			if(centerComp == null) {
				if(toComp != null) {
					toComp.dispose();
					toComp = null;
				}
				if(fromComp != null) {
					fromComp.dispose();
					fromComp = null;
				}
				final GridData fillHorizontal = new GridData();
				fillHorizontal.horizontalAlignment = GridData.FILL;
				fillHorizontal.grabExcessHorizontalSpace = true;
				fillHorizontal.verticalAlignment = GridData.CENTER;
		
				centerComp = new Composite(parent, SWT.NONE);
				RowLayout rowLayout = new RowLayout();
		
				rowLayout.justify = true;
				rowLayout.marginTop = 5;
				rowLayout.marginBottom = 5;
				rowLayout.marginLeft = 1;
				rowLayout.marginRight = 1;
				rowLayout.spacing = 1;
				centerComp.setLayout(rowLayout);
				centerComp.setLayoutData(fillHorizontal);
			}
			RowData comboLayout = new RowData();
			comboLayout.width = pc ? 120 : 150;
			comboLayout.height = 46;
			int comboStyle = SWT.DROP_DOWN;
			if (shell.getSize().x < 300)
				comboStyle = SWT.NONE;
			//if(fullscreenLangs)
			//	comboStyle |= SWT.READ_ONLY;
			comboFrom = new Combo(centerComp, comboStyle);
			comboFrom.setLayoutData(comboLayout);
	
			reverseBtn = new Button(centerComp, SWT.CENTER);
			reverseBtn.setText("<>");
			if(!pc) reverseBtn.setLayoutData(new RowData(40, 44));
	
			comboTo = new Combo(centerComp, comboStyle);
			comboTo.setLayoutData(comboLayout);
		} else {
			if(centerComp != null ) {
				centerComp.dispose();
				centerComp = null;
			}
			if(fromComp == null) {
				fromComp = new Composite(parent, SWT.NONE);
				toComp = new Composite(parent, SWT.NONE);
				
				final GridData fillHorizontal = new GridData();
				fillHorizontal.horizontalAlignment = GridData.FILL;
				fillHorizontal.grabExcessHorizontalSpace = true;
				fillHorizontal.verticalAlignment = GridData.CENTER;
		
				GridLayout gridLayout = new GridLayout();
				//rowLayout.justify = true;
				gridLayout.horizontalSpacing = 3;
				gridLayout.verticalSpacing = 0;
				gridLayout.marginWidth = 0;
				gridLayout.numColumns = 4;
				gridLayout.marginTop = 0;
				gridLayout.marginBottom = 0;
				gridLayout.marginLeft = 0;
				gridLayout.marginRight = 0;
				
				toComp.setLayout(gridLayout);
				toComp.setLayoutData(fillHorizontal);
				
				fromComp.setLayout(gridLayout);
				fromComp.setLayoutData(fillHorizontal);
			}
			GridData comboLayout = new GridData();
			//comboLayout.width = pc ? 120 : 150;
			comboLayout.heightHint = 46;
			comboLayout.horizontalAlignment = GridData.FILL;
			comboLayout.grabExcessHorizontalSpace = true;
			comboFrom = new Combo(fromComp, 0);
			comboFrom.setLayoutData(comboLayout);
	
			comboTo = new Combo(toComp, 0);
			comboTo.setLayoutData(comboLayout);
		}
		try {
			comboFrom.setItems(Languages.getLangNames());
			comboFrom.select(Languages.getFromIndex());
			comboFrom.addSelectionListener(selectionListener);
			comboTo.setItems(Languages.getLangNames());
			comboTo.select(Languages.getToIndex());
			comboTo.addSelectionListener(selectionListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		comboReset();
		
	}

	public void reinit(int orientation) {
		int w;
		int h;
		int sx = shell.getSize().x;
		int sy = shell.getSize().y;
		if (orientation == -1) {
			// first init
			w = sx;
			h = sy;
		} else if (orientation == 0) {
			// portrait
			w = Math.min(sx, sy);
			h = Math.max(sx, sy);
		} else {
			// landscape
			w = sx;
			h = sy;
			// w = Math.max(sx, sy);
			// h = Math.min(sx, sy);
		}
		String ti = null;
		String to = null;
		try {
			if (textOut != null)
				to = textOut.getText();
			if (textIn != null)
				ti = textIn.getText();
		} catch (Throwable e) {
			if (inputString != null)
				ti = inputString;
		}
		// dispose all
		if (textIn != null)
			textIn.dispose();
		textIn = null;
		if (textOut != null)
			textOut.dispose();
		textOut = null;
		if (clearBtn != null)
			clearBtn.dispose();
		clearBtn = null;
		if (copyOutBtn != null)
			copyOutBtn.dispose();
		copyOutBtn = null;
		if (pasteInBtn != null)
			pasteInBtn.dispose();
		pasteInBtn = null;
		if (textCenterComp != null)
			textCenterComp.dispose();
		textCenterComp = null;
		if (textComp != null)
			textComp.dispose();
		textComp = null;
		
		if (ttsFromBtn != null)
			ttsFromBtn.dispose();
		ttsFromBtn = null;
		if (ttsToBtn != null)
			ttsToBtn.dispose();
		ttsToBtn = null;
		if (clearOutBtn != null)
			clearOutBtn.dispose();
		clearOutBtn = null;
		if (clearInBtn != null)
			clearInBtn.dispose();
		clearInBtn = null;
		if (copyInBtn != null)
			copyInBtn.dispose();
		copyInBtn = null;
		

		if(ttsincmd != null) ttsincmd.dispose();
		ttsincmd = null;
		if(ttsoutcmd != null) ttsoutcmd.dispose();
		ttsoutcmd = null;
		
		if(copyincmd != null) copyincmd.dispose();
		copyincmd = null;
		if(pasteincmd != null) pasteincmd.dispose();
		pasteincmd = null;
		
		if(copyoutcmd != null) copyoutcmd.dispose();
		copyoutcmd = null;
		if(clearoutcmd != null) clearoutcmd.dispose();
		clearoutcmd = null;
		
		if(bingDesign || w > h) {
			if(centerComp == null) {
				initCombos(true);
			}
			if (w > h && w > 500) {
				// 640x360 (album)
	
				GridData fillVertical = new GridData();
				fillVertical.horizontalAlignment = GridData.CENTER;
				fillVertical.grabExcessVerticalSpace = true;
				fillVertical.verticalAlignment = GridData.FILL;
	
				GridLayout layout = new GridLayout();
				layout.numColumns = 3;
				textComp = new Composite(parent, SWT.NONE);
				textComp.setLayout(layout);
				fill(textComp);
	
				textIn = new Text(textComp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
				fill(textIn);
	
				textCenterComp = new Composite(textComp, SWT.NONE);
				textCenterComp.setLayoutData(fillVertical);
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.pack = false;
				rowLayout.wrap = false;
				textCenterComp.setLayout(rowLayout);
	
				textOut = new Text(textComp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
				fill(textOut);
	
				textIn.moveAbove(textCenterComp);
				textOut.moveBelow(textCenterComp);
	
				RowData comboLayout = new RowData();
				if (is94) {
					comboLayout.width = 210;
				} else {
					comboLayout.width = 280;
				}
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
				centerComp.moveAbove(textComp);
				centerComp.layout();
			} else if (w > h && w > 300) {
				// 320x240 (album) 9.3*
	
				textIn = new Text(parent, SWT.BORDER);
				fill(textIn);
	
				textOut = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
				fill(textOut);
	
				RowData comboLayout = new RowData();
				comboLayout.width = 120;
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
				centerComp.moveBelow(textIn);
			} else if (w < 300) {
				// 240x320 (portrait) 9.3*

	
				textIn = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
				fill(textIn);
	
				textOut = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY);
				fill(textOut);
				try {
					if (ti != null)
						textIn.setText(ti);
					if (to != null)
						textOut.setText(to);
				} catch (Throwable e) {
				}
	
				RowData comboLayout = new RowData();
				comboLayout.width = 90;
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
				centerComp.moveBelow(textIn);
			} else {
				// 360x640 (portrait) and others
	
				textIn = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
				fill(textIn);
	
				textOut = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
				fill(textOut);
	
				RowData comboLayout = new RowData();
				comboLayout.width = 150;
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
				centerComp.moveBelow(textIn);
				centerComp.layout();
			}
		} else {
			if(centerComp != null) {
				initCombos(false);
			}
			if (w > h && w > 500) {
				// 640x360 (album)
	
				GridData fillVertical = new GridData();
				fillVertical.horizontalAlignment = GridData.CENTER;
				fillVertical.grabExcessVerticalSpace = true;
				fillVertical.verticalAlignment = GridData.FILL;
	
				GridLayout layout = new GridLayout();
				layout.numColumns = 3;
				textComp = new Composite(parent, SWT.NONE);
				textComp.setLayout(layout);
				fill(textComp);
	
				textIn = new Text(textComp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
				fill(textIn);
	
				textCenterComp = new Composite(textComp, SWT.NONE);
				textCenterComp.setLayoutData(fillVertical);
				RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
				rowLayout.pack = false;
				rowLayout.wrap = false;
				textCenterComp.setLayout(rowLayout);
	
				textOut = new Text(textComp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
				fill(textOut);
	
				textIn.moveAbove(textCenterComp);
				textOut.moveBelow(textCenterComp);
				
				RowData comboLayout = new RowData();
				if (is94) {
					comboLayout.width = 210;
				} else {
					comboLayout.width = 280;
				}
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
				centerComp.moveAbove(textComp);
				centerComp.layout();
			} else if (w > h && w > 300) {
				// 320x240 (album) 9.3*
	
				textIn = new Text(parent, SWT.BORDER);
				fill(textIn);
	
				textOut = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
				fill(textOut);
	
				RowData comboLayout = new RowData();
				comboLayout.width = 120;
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
				centerComp.moveBelow(textIn);
			} else if (w < 300) {
				// 240x320 (portrait) 9.3*
	
				textIn = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
				fill(textIn);
	
				textOut = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY);
				fill(textOut);
				try {
					if (ti != null)
						textIn.setText(ti);
					if (to != null)
						textOut.setText(to);
				} catch (Throwable e) {
				}
	
				RowData comboLayout = new RowData();
				comboLayout.width = 90;
				comboLayout.height = 46;
				comboFrom.setLayoutData(comboLayout);
				comboTo.setLayoutData(comboLayout);
	
			} else {
				// 360x640 (portrait) and others
	
	
				textIn = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
				fill(textIn);
	
				textOut = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
				fill(textOut);
	
				GridData comboLayout = new GridData();
				//comboLayout.width = pc ? 120 : 150;
				comboLayout.heightHint = 46;
				comboLayout.grabExcessHorizontalSpace = true;
				comboLayout.horizontalAlignment = GridData.FILL;
				comboFrom.setLayoutData(comboLayout);
				
				comboTo.setLayoutData(comboLayout);
/*
				copyInBtn = new Button(fromComp, SWT.PUSH);
				copyInBtn.setText("C");
				if(!pc) copyInBtn.setLayoutData(new GridData(48, 44));
				copyInBtn.addSelectionListener(this);

				clearInBtn = new Button(fromComp, SWT.PUSH);
				clearInBtn.setText("X");
				if(!pc) clearInBtn.setLayoutData(new GridData(48, 44));
				clearInBtn.addSelectionListener(this);
				*/
				
				/*
				ttsFromBtn = new Button(fromComp, SWT.PUSH);
				ttsFromBtn.setText("TTS");
				if(!pc) ttsFromBtn.setLayoutData(new GridData(48, 44));
				ttsFromBtn.addSelectionListener(this);
				*/
				
/*
				copyOutBtn = new Button(toComp, SWT.PUSH);
				copyOutBtn.setText("C");
				if(!pc) copyOutBtn.setLayoutData(new GridData(48, 44));
				copyOutBtn.addSelectionListener(this);
				
				clearOutBtn = new Button(toComp, SWT.PUSH);
				clearOutBtn.setText("X");
				if(!pc) clearOutBtn.setLayoutData(new GridData(48, 44));
				clearOutBtn.addSelectionListener(this);
				*/
				
				/*
				ttsToBtn = new Button(toComp, SWT.PUSH);
				ttsToBtn.setText("TTS");
				if(!pc) ttsToBtn.setLayoutData(new GridData(48, 44));
				ttsToBtn.addSelectionListener(this);
				*/

				fromComp.moveAbove(textIn);
				toComp.moveBelow(textIn);
				fromComp.layout();
				toComp.layout();
			}
		}

		
		copyincmd = new Command(textIn, Command.GENERAL, 3);
		copyincmd.setText("Copy");
		copyincmd.addSelectionListener(this);
		
		pasteincmd = new Command(textIn, Command.GENERAL, 2);
		pasteincmd.setText("Paste");
		pasteincmd.addSelectionListener(this);
		
		
		copyoutcmd = new Command(textOut, Command.GENERAL, 3);
		copyoutcmd.setText("Copy");
		copyoutcmd.addSelectionListener(this);
		
		clearoutcmd  = new Command(textOut, Command.GENERAL, 2);
		clearoutcmd.setText("Clear");
		clearoutcmd.addSelectionListener(this);
		
		
		ttsincmd = new Command(textIn, Command.GENERAL, 1);
		ttsincmd.setText("Listen");
		ttsincmd.addSelectionListener(this);
		
		ttsoutcmd = new Command(textOut, Command.GENERAL, 1);
		ttsoutcmd.setText("Listen");
		ttsoutcmd.addSelectionListener(this);
		
		if(is93) { //
			textIn.addPaintListener(this);
			textOut.addPaintListener(this);
		}
		textIn.addModifyListener(this);
		// textIn.addSelectionListener(selectionListener);
		comboReset();
		try {
			if (ti != null)
				textIn.setText(ti);
			if (to != null)
				textOut.setText(to);
		} catch (Throwable e) {
		}
		upd();

		comboReset();
		if(fullscreenLangs) {
			comboFrom.addFocusListener(this);
			comboTo.addFocusListener(this);
		}
	}

	// workaround for buggy text fields on 93
	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		Rectangle b = ((Text)e.widget).getBounds();
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    gc.fillRectangle(0,0,b.width-1,b.height-1);
		gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
	    String s = e.widget == textOut ? outString : e.widget == textIn ? inputString : null;
	    if(s != null)
	    	gc.drawText(s, 2, 0);
	}

	private void fill(Control c) {
		final GridData fill = new GridData();
		fill.horizontalAlignment = GridData.FILL;
		fill.grabExcessHorizontalSpace = true;
		fill.grabExcessVerticalSpace = true;
		fill.verticalAlignment = GridData.FILL;
		c.setLayoutData(fill);
	}

	private void upd() {
		// textOut.redraw();
		parent.layout();
		shell.layout();
		shell.redraw();
		shell.update();
	}

	public void exit() {
		exiting = true;
		Display.getDefault().wake();
		TranslateMIDlet.midlet.notifyDestroyed();
	}

	public void sync() {
		display.syncExec(new Runnable() {
			public void run() {
				int w = shell.getSize().x;
				if (w < 600 && w > 300) {
					shell.redraw();
					shell.update();
				}
			}
		});
	}

	public boolean running() {
		return !exiting;
	}

	static ITranslateUI _init() {
		return new TranslateSWTUI();
	}

	public void screenActivated(ScreenEvent event) {

	}

	public void screenDeactivated(ScreenEvent event) {

	}

	public void screenOrientationChanged(ScreenEvent event) {
		// landscape = (event.orientation == Screen.LANDSCAPE);
		reinit(event.orientation);
		updateLangsPosition();
	}

	public void controlMoved(ControlEvent e) {
		updateLangsPosition();
	}

	public void controlResized(ControlEvent e) {
		updateLangsPosition();
	}

    public void keyTraversed(TraverseEvent e) {
    	if (fullscreenLangs && e.doit) {

			int in;
			int out;
			if(e.widget == inLangsList) {

				comboReset();
				
				Languages.setSelected(in = Languages.getLangFromName(inLangsList.getSelection()[0]),
						out = comboTo.getSelectionIndex());
				to = Languages.getLangFromIndex(in)[0];
				from = Languages.getLangFromIndex(out)[0];
				comboFrom.select(in);
				Languages.save();
				
				translateThread.clearLastInput();
				inLangsList.dispose();
				inLangsList = null;
				inLangsShell.setVisible(false);
				inLangsShell.dispose();
				inLangsShell = null;
				inLangsDoneCmd.dispose();
				inLangsDoneCmd = null;
				shell.forceActive();
				shell.forceFocus();
				textIn.forceFocus();
				comboFrom.setVisible(true);
			} else if(e.widget == outLangsList) {
				comboReset();
				
				Languages.setSelected(in = comboFrom.getSelectionIndex(), 
						out = Languages.getLangFromName(outLangsList.getSelection()[0]));
				to = Languages.getLangFromIndex(in)[0];
				from = Languages.getLangFromIndex(out)[0];
				comboTo.select(out);
				Languages.save();
				
				translateThread.clearLastInput();
				outLangsList.dispose();
				outLangsList = null;
				outLangsShell.setVisible(false);
				outLangsShell.dispose();
				outLangsShell = null;
				outLangsDoneCmd.dispose();
				outLangsDoneCmd = null;
				shell.forceActive();
				shell.forceFocus();
				textIn.forceFocus();
				comboTo.setVisible(true);
			}
			translateThread.schedule();
    	}
    }

	public void focusGained(FocusEvent e) {
		if (fullscreenLangs && e.widget instanceof Combo) {
			if((System.currentTimeMillis() - comboInitTime) < 700 || comboInitTime == 0) return;
			if (e.widget == comboTo) {
				if (outLangsShell == null) {
					outLangsShell = new Shell(shell, SWT.BORDER | SWT.TITLE | SWT.MODELESS);
					outLangsShell.setText("Output language");
					outLangsShell.setLayout(new FillLayout());
					outLangsShell.addControlListener(this);
					outLangsShell.addTraverseListener(this);
					outLangsList = new SortedList(outLangsShell, SWT.SINGLE | SWT.V_SCROLL);
					outLangsList.addSelectionListener(selectionListener);
					outLangsDoneCmd = new Command(outLangsShell, Command.EXIT, 1);
					outLangsDoneCmd.setText("Cancel");
					outLangsDoneCmd.addSelectionListener(selectionListener);
				} else return;
				outLangsList.setItems(comboTo.getItems());
				outLangsList.select(comboTo.getItem(comboTo.getSelectionIndex()));
				outLangsShell.setVisible(true);
				outLangsShell.forceActive();
				outLangsList.showSelection();
				outLangsList.forceFocus();
			} else if (e.widget == comboFrom) {
				if (inLangsShell == null) {
					inLangsShell = new Shell(shell, SWT.BORDER | SWT.TITLE | SWT.MODELESS);
					inLangsShell.setText("Input language");
					inLangsShell.setLayout(new FillLayout());
					inLangsShell.addControlListener(this);
					inLangsShell.addTraverseListener(this);
					inLangsList = new SortedList(inLangsShell, SWT.SINGLE | SWT.V_SCROLL);
					inLangsList.addSelectionListener(selectionListener);
					inLangsDoneCmd = new Command(inLangsShell, Command.EXIT, 1);
					inLangsDoneCmd.setText("Cancel");
					inLangsDoneCmd.addSelectionListener(selectionListener);
				} else return;
				inLangsList.setItems(comboFrom.getItems());
				inLangsList.select(comboFrom.getItem(comboFrom.getSelectionIndex()));
				inLangsShell.setVisible(true);
				inLangsShell.forceActive();
				inLangsList.showSelection();
				inLangsList.forceFocus();
			}
			((Combo)e.widget).setVisible(false);
		}
	}

	public void focusLost(FocusEvent e) {

	}
	
	public void comboReset() {
		comboInitTime = System.currentTimeMillis();
	}

	public void setDownloading(final boolean b) {
		display.asyncExec(new Runnable() {
			public void run() {
				if(b) {
					if(dlTask == null) {
						dlTask = new TaskTip(shell, SWT.INDETERMINATE);
					}
					dlTask.setText("Loading languages...");
					dlTask.setVisible(true);
				} else {
					if(dlTask != null) {
						dlTask.setVisible(false);
						dlTask.dispose();
						dlTask = null;
					}
				}
			}
		});
	}

	public void downloadingError(final String s) {
		display.asyncExec(new Runnable() {

			public void run() {
				if(dlTask != null) {
					dlTask.setVisible(false);
				}
				errorTask = new TaskTip(shell, SWT.NONE);
				errorTask.setText(s.toLowerCase().indexOf("IOException") != -1 ? "No network access" : "Unknown error");
				errorTask.setVisible(true);

				display.timerExec(4000, new Runnable() {
					public void run() {
						if(errorTask != null) {
							errorTask.setVisible(false);
							errorTask.dispose();
							errorTask = null;
						}
					}
				});
			}
		});
	}

	public void downloadingDone() {
		display.asyncExec(new Runnable() {

			public void run() {
				if(dlTask != null) {
					dlTask.setVisible(false);
				}
				errorTask = new TaskTip(shell, SWT.NONE);
				errorTask.setText("Done");
				errorTask.setVisible(true);

				display.timerExec(2500, new Runnable() {
					public void run() {
						if(errorTask != null) {
							errorTask.setVisible(false);
							errorTask.dispose();
							errorTask = null;
						}
					}
				});
			}
		});
	}

	public void setLanguages(final String[][] l) {
		display.asyncExec(new Runnable() {
			public void run() {
				try {
					//Languages.setSelected(langsList.getSelection());
					Languages.setSelected(comboFrom.getSelectionIndex(), comboTo.getSelectionIndex());
					Languages.setDownloaded(l);
					Languages.updateLangs();
					//String st = comboTo.getText();
					//String sf = comboFrom.getText();
					comboFrom.setItems(Languages.getLangNames());
					comboTo.setItems(Languages.getLangNames());
					comboFrom.select(Languages.getFromIndex());
					comboTo.select(Languages.getToIndex());
					//Languages.setSelected(fi, ti);
					Languages.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setTranslating(final boolean b) {
		display.asyncExec(new Runnable() {
			public void run() {
				if(b) {
					if(dlTask == null) {
						dlTask = new TaskTip(shell, SWT.INDETERMINATE);
					}
					dlTask.setText("Translating...");
					dlTask.setVisible(true);
				} else {
					if(dlTask != null) {
						dlTask.setVisible(false);
						dlTask.dispose();
						dlTask = null;
					}
				}
			}
		});
	}

}
