package cc.nnproject.translate.swt;

import javax.microedition.rms.RecordStore;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.ercp.swt.mobile.MobileDevice;
import org.eclipse.ercp.swt.mobile.MobileShell;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.Languages;
import cc.nnproject.translate.TranslateBingThread;
import cc.nnproject.translate.bing.app.TranslateBingMIDlet;

public class TranslateSWTUI implements Runnable, SelectionListener, ITranslateUI, ScreenListener, ControlListener, FocusListener, ModifyListener {

	private final boolean is94 = System.getProperty("microedition.platform").indexOf("n=5.0") != -1;
	private final String version = TranslateBingMIDlet.midlet.getAppProperty("MIDlet-Version");

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private Display display;
	private MobileShell shell;

	private boolean exiting;

	private Composite parent;

	private Command exitcmd;

	private Combo comboFrom;
	private Combo comboTo;

	private Text textIn;
	private Text textOut;

	private Button reverseBtn;

	private Composite centerComp;
	private Button clearBtn;
	private Button copyBtn;
	private Button pasteBtn;

	private String inputText;

	private String from;
	private String to;

	private Composite textComp;
	private Composite textCenterComp;

	private Shell langsShell;
	private SortedList langsList;

	private Command langsDoneCmd;

	private MenuItem clearMenuItem;
	private MenuItem copyMenuItem;
	private MenuItem pasteMenuItem;
	private MenuItem aboutMenuItem;
	private MenuItem fullListMenuItem;
	private MenuItem langsMenuItem;
	private boolean fullscreenLangs;

	public TranslateSWTUI() {
		new Thread(this, "Main SWT Thread").start();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		if(e.widget == langsList) {
			updateLangs();
		}
	}

	private void updateLangs() {
		Languages.setSelected(langsList.getSelection());
		Languages.setLastSelected(comboFrom.getSelectionIndex(), comboTo.getSelectionIndex());
		String st = comboTo.getText();
		String sf = comboFrom.getText();
		comboFrom.setItems(Languages.getLangNames());
		comboTo.setItems(Languages.getLangNames());
		int ti;
		int fi;
		comboFrom.select(ti = Languages.getSelectedIndex(sf));
		comboTo.select(fi = Languages.getSelectedIndex(st));
		Languages.setLastSelected(fi, ti);
		Languages.save();
	}

	private void updateLangsPosition() {
		Rectangle bgdBnds = shell.getBounds();
		if(langsShell != null)
			langsShell.setBounds(bgdBnds.x + 1, bgdBnds.y + 1, bgdBnds.width - 1, bgdBnds.height - 1);
		if(outLangsShell != null)
			outLangsShell.setBounds(bgdBnds.x + 1, bgdBnds.y + 1, bgdBnds.width - 1, bgdBnds.height - 1);
		if(inLangsShell != null)
			inLangsShell.setBounds(bgdBnds.x + 1, bgdBnds.y + 1, bgdBnds.width - 1, bgdBnds.height - 1);
	}

	public void widgetSelected(SelectionEvent ev) {
		if(ev.widget == exitcmd) {
			exit();
			return;
		}
		if(ev.widget == langsMenuItem) {
			if(langsShell == null) {
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
			return;
		}
		if(ev.widget == langsDoneCmd) {
			updateLangs();
			langsDoneCmd.dispose();
			langsList.dispose();
			langsList = null;
			langsShell.setVisible(false);
			langsShell.dispose();
			langsShell = null;
			shell.forceActive();
			shell.forceFocus();
			return;
		}
		if(ev.widget == aboutMenuItem) {
			msg("Bing Translate v" + version + "\nBy shinovon & Feodor0090\nnnp.nnchan.ru");
			return;
		}
		if(ev.widget == copyBtn || ev.widget == copyMenuItem) {
			textOut.copy();
			try {
				Clipboard.copyToClipboard(textOut.getText());
			} catch (Throwable e) {
			}
			return;
		}
		if(ev.widget == pasteBtn || ev.widget == pasteMenuItem) {
			textIn.paste();
			return;
		}
		if(ev.widget == clearBtn || ev.widget == clearMenuItem) {
			textIn.setText("");
			textOut.setText("");
			return;
		}
		if(ev.widget == reverseBtn) {
			int newIn = comboFrom.getSelectionIndex();
			int newOut = comboTo.getSelectionIndex();
			comboFrom.select(newOut);
			comboTo.select(newIn);
			to = Languages.getSelectedLang(newIn)[1];
			from = Languages.getSelectedLang(newOut)[1];
			String outText = "";
			try {
				outText = textOut.getText();
				inputText = outText;
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
		if(ev.widget == fullListMenuItem) {
			fullscreenLangs = fullListMenuItem.getSelection();
			try {
				RecordStore.deleteRecordStore("bsets");
			} catch (Exception e) {
			}
			try {
				RecordStore r = RecordStore.openRecordStore("bsets", true);
				byte[] b = new byte[] { (byte) (fullscreenLangs ? 1 : 0) };
				r.addRecord(b, 0, b.length);
				r.closeRecordStore();
			} catch (Exception e) {
			}
			comboFrom.dispose();
			comboFrom = null;
			reverseBtn.dispose();
			reverseBtn = null;
			comboTo.dispose();
			comboTo = null;
			
			initCombos();
			
			reinit(-1);
			return;
		}
		int in;
		int out;
		if(ev.widget instanceof Combo) {
			Languages.setLastSelected(in = comboFrom.getSelectionIndex(), out = comboTo.getSelectionIndex());
			to = Languages.getSelectedLang(in)[1];
			from = Languages.getSelectedLang(out)[1];
			Languages.save();
			translateThread.clearLastInput();
			translateThread.schedule();
			return;
		}
		if(ev.widget == outLangsDoneCmd) {
			comboReset();
			Languages.setLastSelected(in = comboFrom.getSelectionIndex(), out = Languages.getSelectedIndex(outLangsList.getSelection()[0]));
			to = Languages.getSelectedLang(in)[1];
			from = Languages.getSelectedLang(out)[1];
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
			translateThread.schedule();
			return;
		}
		if(ev.widget == inLangsDoneCmd) {
			comboReset();
			Languages.setLastSelected(in = Languages.getSelectedIndex(inLangsList.getSelection()[0]), out = comboTo.getSelectionIndex());
			to = Languages.getSelectedLang(in)[1];
			from = Languages.getSelectedLang(out)[1];
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
			translateThread.schedule();
			return;
		}
	}

	public void msg(final String s) {
		display.syncExec(new Runnable() {
			public void run() {
				MessageBox msg = new MessageBox(shell);
				msg.setMessage(s);
				msg.open();
			}
		});
	}

	public void run() {
		try {
			RecordStore r = RecordStore.openRecordStore("bsets", false);
			byte[] b = r.getRecord(1);
			r.closeRecordStore();
			if(b.length > 0) {
				fullscreenLangs = b[0] == 1;
			}
		} catch (Exception e) {
		}
		display = new Display();
		translateThread.start();
		try {
			MobileDevice.getMobileDevice().getScreens()[0].addEventListener(this);
		} catch (Exception e) {
		}
		shell = new MobileShell(display, SWT.NONE, MobileShell.SMALL_STATUS_PANE);
		shell.setText("Bing Translate");
//		shell.setImage(new Image(display, getClass().getResourceAsStream("/page_exported.png")));
		shell.setLayout(new FillLayout());
		parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());
		exitcmd = new Command(shell, Command.EXIT, 1);
		exitcmd.setText("Exit");
		exitcmd.addSelectionListener(this);

//		String s = System.getProperty("microedition.platform");
//		if(s != null && s.indexOf("platform_version=3.2") > -1) {
//			Command group = new Command(shell, Command.COMMANDGROUP, 1);
//			group.setText("App");
//			aboutcmd = new Command(group, Command.GENERAL, 1);
//			aboutcmd.setText("About");
//			aboutcmd.addSelectionListener(this);
//		} else {
//			Command sets = new Command(shell, Command.COMMANDGROUP, 2);
//			sets.setText("Settings");
//			Command text = new Command(shell, Command.COMMANDGROUP, 3);
//			text.setText("Text");
//			langscmd = new Command(sets, Command.GENERAL, 1);
//			langscmd.setText("Set languages");
//			langscmd.addSelectionListener(this);
//			aboutcmd = new Command(shell, Command.GENERAL, 1);
//			aboutcmd.setText("About");
//			aboutcmd.addSelectionListener(this);
//			clearcmd = new Command(text, Command.GENERAL, 3);
//			clearcmd.setText("Clear");
//			clearcmd.addSelectionListener(this);
//			copycmd = new Command(text, Command.GENERAL, 2);
//			copycmd.setText("Copy output");
//			copycmd.addSelectionListener(this);
//			pastecmd = new Command(text, Command.GENERAL, 1);
//			pastecmd.setText("Paste input");
//			pastecmd.addSelectionListener(this);
//		}
		Menu menu = shell.getMenuBar();
		if(menu == null)
			menu = new Menu(shell, SWT.BAR);

		MenuItem textMenuItem = new MenuItem(menu, SWT.CASCADE);
		textMenuItem.setText("Text");
		Menu textMenu = new Menu(shell, SWT.DROP_DOWN);
		textMenuItem.setMenu(textMenu);
		clearMenuItem = new MenuItem(textMenu, SWT.PUSH);
		clearMenuItem.addSelectionListener(this);
		clearMenuItem.setText("Clear");
		copyMenuItem = new MenuItem(textMenu, SWT.PUSH);
		copyMenuItem.addSelectionListener(this);
		copyMenuItem.setText("Copy output");
		pasteMenuItem = new MenuItem(textMenu, SWT.PUSH);
		pasteMenuItem.addSelectionListener(this);
		pasteMenuItem.setText("Paste input");

		MenuItem setsMenuItem = new MenuItem(menu, SWT.CASCADE);
		setsMenuItem.setText("Settings");
		Menu setsMenu = new Menu(shell, SWT.DROP_DOWN);
		setsMenuItem.setMenu(setsMenu);
		fullListMenuItem = new MenuItem(setsMenu, SWT.CHECK);
		fullListMenuItem.addSelectionListener(this);
		fullListMenuItem.setText("Fullscreen lang. list");
		fullListMenuItem.setSelection(fullscreenLangs);
		langsMenuItem = new MenuItem(setsMenu, SWT.PUSH);
		langsMenuItem.addSelectionListener(this);
		langsMenuItem.setText("Select languages");

		aboutMenuItem = new MenuItem(menu, SWT.PUSH);
		aboutMenuItem.addSelectionListener(this);
		aboutMenuItem.setText("About");

		shell.setMenuBar(menu);

		init();
		shell.open();
		while(!exiting) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		translateThread.interrupt();
	}

	public String getText() {
//		display.syncExec(new Runnable() {
//			public void run() {
//				try {
//					inputText = textIn.getText();
//				} catch (Throwable e) {
//				}
//			}
//		});
		return inputText;
	}

	public void setText(final String s) {
		display.syncExec(new Runnable() {
			public void run() {
				try {
					textOut.setText(s);
				} catch (Throwable e) {
					translateThread.scheduleRetext();
				}
			}
		});
	}

	public String getFromLang() {
		display.syncExec(new Runnable() {
			public void run() {
				from = Languages.getSelectedLang(comboFrom.getSelectionIndex())[1];
			}
		});
		return from;
	}

	public String getToLang() {
		display.syncExec(new Runnable() {
			public void run() {
				to = Languages.getSelectedLang(comboTo.getSelectionIndex())[1];
			}
		});
		return to;
	}

	private void init() {
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
		
		initCombos();

		reinit(-1);
	}
	
	private void initCombos() {
		RowData comboLayout = new RowData();
		comboLayout.width = 150;
		comboLayout.height = 46;
		int comboStyle = SWT.DROP_DOWN;
		if(shell.getSize().x < 300)
			comboStyle = SWT.NONE;
//		if(fullscreenLangs)
//			comboStyle |= SWT.READ_ONLY;
		comboFrom = new Combo(centerComp, comboStyle);
		comboFrom.setLayoutData(comboLayout);
		comboFrom.setItems(Languages.getLangNames());
		comboFrom.select(Languages.getLastFrom());
		comboFrom.addSelectionListener(this);

		reverseBtn = new Button(centerComp, SWT.CENTER);
		reverseBtn.setText("<>");
		reverseBtn.addSelectionListener(this);
		reverseBtn.setLayoutData(new RowData(40, 44));

		comboTo = new Combo(centerComp, comboStyle);
		comboTo.setLayoutData(comboLayout);
		comboTo.setItems(Languages.getLangNames());
		comboTo.select(Languages.getLastTo());
		comboTo.addSelectionListener(this);
		
		comboReset();
	}

	public void reinit(int orientation) {
		int w;
		int h;
		int sx = shell.getSize().x;
		int sy = shell.getSize().y;
		if(orientation == -1) {
			// first init
			w = sx;
			h = sy;
		} else if(orientation == 0) {
			// portrait
			w = Math.min(sx, sy);
			h = Math.max(sx, sy);
		} else {
			// landscape
			w = sx;
			h = sy;
//			w = Math.max(sx, sy);
//			h = Math.min(sx, sy);
		}
		String ti = null;
		String to = null;
		try {
			if(textOut != null) to = textOut.getText();
			if(textIn != null) ti = textIn.getText();
		} catch (Throwable e) {
			if(inputText != null) ti = inputText;
		}
		// dispose all
		if(textIn != null) textIn.dispose();
		textIn = null;
		if(textOut != null) textOut.dispose();
		textOut = null;
		if(clearBtn != null) clearBtn.dispose();
		clearBtn = null;
		if(copyBtn != null) copyBtn.dispose();
		copyBtn = null;
		if(pasteBtn != null) pasteBtn.dispose();
		pasteBtn = null;
		if(textCenterComp != null) textCenterComp.dispose();
		textCenterComp = null;
		if(textComp != null) textComp.dispose();
		textComp = null;

		if(w > h && w > 500) {
			// 640x360 (album)

			final GridData fill = new GridData();
			fill.horizontalAlignment = GridData.FILL;
			fill.grabExcessHorizontalSpace = true;
			fill.grabExcessVerticalSpace = true;
			fill.verticalAlignment = GridData.FILL;
			GridData fillVertical = new GridData();
			fillVertical.horizontalAlignment = GridData.CENTER;
			fillVertical.grabExcessVerticalSpace = true;
			fillVertical.verticalAlignment = GridData.FILL;

			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			textComp = new Composite(parent, SWT.NONE);
			textComp.setLayout(layout);
			textComp.setLayoutData(fill);

			textIn = new Text(textComp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
			textIn.setLayoutData(fill);

			textCenterComp = new Composite(textComp, SWT.NONE);
			textCenterComp.setLayoutData(fillVertical);
			RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
			rowLayout.pack = false;
			rowLayout.wrap = false;
			textCenterComp.setLayout(rowLayout);

			textOut = new Text(textComp, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
			textOut.setLayoutData(fill);

			textIn.moveAbove(textCenterComp);
			textOut.moveBelow(textCenterComp);

			RowData centerButtonLayout = new RowData(40, 44);
			clearBtn = new Button(textCenterComp, SWT.CENTER);
			clearBtn.setText("x");
			clearBtn.setLayoutData(centerButtonLayout);
			clearBtn.addSelectionListener(this);

			copyBtn = new Button(textCenterComp, SWT.CENTER);
			copyBtn.setText("C");
			copyBtn.setLayoutData(centerButtonLayout);
			copyBtn.addSelectionListener(this);

			pasteBtn = new Button(textCenterComp, SWT.CENTER);
			pasteBtn.setText("V");
			pasteBtn.setLayoutData(centerButtonLayout);
			pasteBtn.addSelectionListener(this);

			RowData comboLayout = new RowData();
			if(is94) {
				comboLayout.width = 210;
			} else {
				comboLayout.width = 280;
			}
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout);
			comboTo.setLayoutData(comboLayout);

			centerComp.moveAbove(textComp);
			centerComp.layout();
		} else if(w > h && w > 300) {
			// 320x240 (album) 9.3*

			final GridData fill = new GridData();
			fill.horizontalAlignment = GridData.FILL;
			fill.grabExcessHorizontalSpace = true;
			fill.grabExcessVerticalSpace = true;
			fill.verticalAlignment = GridData.FILL;

			textIn = new Text(parent, SWT.BORDER);
			textIn.setLayoutData(fill);

			textOut = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
			textOut.setLayoutData(fill);

			RowData comboLayout = new RowData();
			comboLayout.width = 120;
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout);
			comboTo.setLayoutData(comboLayout);

			centerComp.moveBelow(textIn);
		} else if(w < 300) {
			// 240x320 (portrait) 9.3*

			final GridData fill = new GridData();
			fill.horizontalAlignment = GridData.FILL;
			fill.grabExcessHorizontalSpace = true;
			fill.grabExcessVerticalSpace = true;
			fill.verticalAlignment = GridData.FILL;

			textIn = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
			textIn.setLayoutData(fill);

			textOut = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY);
			textOut.setLayoutData(fill);
			try {
				if(ti != null) textIn.setText(ti);
				if(to != null) textOut.setText(to);
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

			final GridData fill = new GridData();
			fill.horizontalAlignment = GridData.FILL;
			fill.grabExcessHorizontalSpace = true;
			fill.grabExcessVerticalSpace = true;
			fill.verticalAlignment = GridData.FILL;

			textIn = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
			textIn.setLayoutData(fill);

			textOut = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
			textOut.setLayoutData(fill);

			RowData comboLayout = new RowData();
			comboLayout.width = 150;
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout);
			comboTo.setLayoutData(comboLayout);

			centerComp.moveBelow(textIn);
			centerComp.layout();
		}
		textIn.addModifyListener(this);
//		textIn.addSelectionListener(selectionListener);
		try {
			if(ti != null) textIn.setText(ti);
			if(to != null) textOut.setText(to);
		} catch (Throwable e) {
		}
		upd();
		
		if(fullscreenLangs) {
			comboFrom.addFocusListener(this);
			comboTo.addFocusListener(this);
		}
	}

	private void upd() {
//		textOut.redraw();
		parent.layout();
		shell.layout();
		shell.redraw();
		shell.update();
	}

	public void exit() {
		exiting = true;
		Display.getDefault().wake();
		TranslateBingMIDlet.midlet.notifyDestroyed();
	}

	public void sync() {
		display.syncExec(new Runnable() {
			public void run() {
				int w = shell.getSize().x;
				if(w < 600 && w > 300) {
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
		reinit(event.orientation);
		updateLangsPosition();
	}

	public void controlMoved(ControlEvent event) {
		updateLangsPosition();
	}

	public void controlResized(ControlEvent event) {
		updateLangsPosition();
	}

	private Shell outLangsShell;
	private SortedList outLangsList;
	private Shell inLangsShell;
	private SortedList inLangsList;
	private Command inLangsDoneCmd;
	private Command outLangsDoneCmd;
	
	private long comboInitTime;
	
	protected TaskTip errorTask;
	protected TaskTip dlTask;

	public void focusGained(FocusEvent e) {
		if(fullscreenLangs && e.widget instanceof Combo) {
			if((System.currentTimeMillis() - comboInitTime) < 300) return;
			if(e.widget == comboTo) {
				if(outLangsShell == null) {
					outLangsShell = new Shell(shell, SWT.BORDER | SWT.TITLE | SWT.MODELESS);
					outLangsShell.setText("Output language");
					outLangsShell.setLayout(new FillLayout());
					outLangsShell.addControlListener(this);
					outLangsList = new SortedList(outLangsShell, SWT.SINGLE | SWT.V_SCROLL);
//					outLangsList.addSelectionListener(this);
					outLangsDoneCmd = new Command(outLangsShell, Command.EXIT, 1);
					outLangsDoneCmd.setText("Done");
					outLangsDoneCmd.addSelectionListener(this);
				} else return;
				outLangsList.setItems(comboTo.getItems());
				outLangsList.select(comboTo.getItem(comboTo.getSelectionIndex()));
				outLangsShell.setVisible(true);
				outLangsShell.forceActive();
				outLangsList.showSelection();
				outLangsList.forceFocus();
			} else if(e.widget == comboFrom) {
				if(inLangsShell == null) {
					inLangsShell = new Shell(shell, SWT.BORDER | SWT.TITLE | SWT.MODELESS);
					inLangsShell.setText("Input language");
					inLangsShell.setLayout(new FillLayout());
					inLangsShell.addControlListener(this);
					inLangsList = new SortedList(inLangsShell, SWT.SINGLE | SWT.V_SCROLL);
					inLangsDoneCmd = new Command(inLangsShell, Command.EXIT, 1);
					inLangsDoneCmd.setText("Done");
					inLangsDoneCmd.addSelectionListener(this);
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

	public void error(final String s) {
		display.asyncExec(new Runnable() {
			public void run() {
				if(dlTask != null) {
					dlTask.setVisible(false);
				}
				errorTask = new TaskTip(shell, SWT.NONE);
				errorTask.setText(s.indexOf("IOException") != -1 ? "No network access" : "Unknown error!");
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

	public void modifyText(ModifyEvent event) {
		to = Languages.getSelectedLang(comboTo.getSelectionIndex())[1];
		from = Languages.getSelectedLang(comboFrom.getSelectionIndex())[1];
		try {
			inputText = textIn.getText();
		} catch (Throwable e) {
		}
		translateThread.schedule();
	}

}
