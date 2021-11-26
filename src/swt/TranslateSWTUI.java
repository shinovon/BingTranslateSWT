package swt;

import java.io.IOException;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.ercp.swt.mobile.MobileDevice;
import org.eclipse.ercp.swt.mobile.MobileShell;
import org.eclipse.ercp.swt.mobile.ScreenEvent;
import org.eclipse.ercp.swt.mobile.ScreenListener;
import org.eclipse.ercp.swt.mobile.SortedList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import Util;
import Languages;
import StringUtils;
import ITranslateUI;
import TranslateBingThread;
import cc.nnproject.translate.bing.app.TranslateBingMIDlet;

public class TranslateSWTUI implements Runnable, SelectionListener, ITranslateUI, ScreenListener, ControlListener {
	
	private static final String model = System.getProperty("microedition.platform");
	//private static final boolean is93 = model.indexOf("n=3.2") != -1;
	private static final boolean is94 = model.indexOf("n=5.0") != -1;
	
	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent ev) {
			to = Languages.getSelectedLang(comboTo.getSelectionIndex())[1];
			from = Languages.getSelectedLang(comboFrom.getSelectionIndex())[1];
			try {
				inputText = textIn.getText();
			} catch (Throwable e) {
			}
			translateThread.schedule();
		}
	};

	private final SelectionListener selectionListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent ev) {
			
		}
		public void widgetSelected(SelectionEvent ev) {
			int in;
			int out;
			Languages.setLastSelected(in = comboFrom.getSelectionIndex(), out = comboTo.getSelectionIndex());
			to = Languages.getSelectedLang(in)[1];
			from = Languages.getSelectedLang(out)[1];
			/*
			try {
				inputText = textIn.getText();
			} catch (Throwable e) {
			}
			*/
			if(ev.widget instanceof Combo) {
				Languages.save();
				translateThread.clearLastInput();
			}
			translateThread.schedule();
		}
	};

	protected String inputText;

	private Display display;
	private MobileShell shell;

	public boolean exiting;

	private Composite parent;

	private Command exitcmd;

	private Command aboutcmd;
	private Command langscmd;
	private Command clearcmd;
	private Command copycmd;
	private Command pastecmd;

	private Combo comboFrom;
	private Combo comboTo;
	
	private Text textIn;
	private Text textOut;

	private Button reverseBtn;

	private Composite centerComp;
	private Button clearBtn;
	private Button copyBtn;
	private Button pasteBtn;

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private String from;
	private String to;

	private Composite textComp;
	private Composite textCenterComp;

	private Shell langsShell;
	private SortedList langsList;

	//private boolean landscape;

	private Command langsDoneCmd;

	public TranslateSWTUI() {
		new Thread(this, "Main SWT Thread").start();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	    if (e.widget == langsList && e.widget != null) {
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
		comboFrom.select(Languages.getSelectedIndex(sf));
		comboTo.select(Languages.getSelectedIndex(st));
		Languages.save();
	}
	
	private void updateLangsPosition() {
		if(langsShell == null) return;
		Rectangle bgdBnds = shell.getBounds();
		langsShell.setBounds(bgdBnds.x+1, bgdBnds.y+1, bgdBnds.width-1, bgdBnds.height-1);
	}

	public void widgetSelected(SelectionEvent ev) {
		if (ev.widget == exitcmd)
			exit();
		if (ev.widget == langscmd) {
			if(langsShell == null) {
				langsShell = new Shell(shell, SWT.BORDER | SWT.TITLE | SWT.MODELESS);
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
		if (ev.widget == langsDoneCmd) {
			shell.removeControlListener(this);
			langsDoneCmd.dispose();
			langsList.dispose();
			langsList = null;
			langsShell.setVisible(false);
			langsShell.dispose();
			langsShell = null;
			shell.forceActive();
			shell.forceFocus();
		}
		if (ev.widget == aboutcmd) {
			StringBuffer sb = new StringBuffer();
			// Bing
			sb.append(('b' + "").toUpperCase());
			sb.append('i');
			sb.append(e.charAt(0));
			sb.append((char) (e.charAt(1) - 7));
			sb.append(' ');
			// Translate
			sb.append(('t' + "").toUpperCase());
			sb.append('r');
			sb.append((char) ('b' - 1));
			sb.append(e.charAt(1));
			String b = "Maho pidoras, u know.";
			sb.append(b.charAt(11));
			sb.append((char) (e.charAt(1) - 2));
			sb.append((char) ('c' - 2));
			sb.append('t');
			sb.append('e');
			sb.append('\n');
			// Made
			sb.append(b.charAt(0));
			sb.append(b.charAt(1));
			sb.append((char) (b.charAt(1) + 3));
			sb.append('e');
			sb.append(' ');
			// by
			sb.append((char) (b.charAt(1) + 1));
			sb.append('y');
			sb.append(' ');
			// shinovon
			try {
				sb.append(StringUtils.split(Util.get(Util.uwu), '!')[1]);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			sb.append('&');
			sb.append(' ');
			sb.append("Feodor0090");
			// site
			/*
			sb.append(Util.uwu.charAt(2));
			sb.append(e);
			sb.append((char) (langsAlias[0].charAt(1) -5));
			sb.append(Util.d().substring(3, 6));
			sb.append(langsAlias[2].charAt(1));
			sb.append((char) (langsAlias[2].charAt(0) +1));
			try {
				sb.append(StringUtils.split(Util.get(Util.uwu), '!')[2]);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			sb.append(Util.uwu.charAt(1));
			sb.append((char) (langsAlias[2].charAt(0) +1));
			sb.append((char) (langsAlias[2].charAt(0) +1));
			sb.append(Util.uwu.charAt(0));
			*/
			// "Bing Translate\nMade by shinovon (nnproject.cc)"
			msg(sb.toString());
		}
		if (ev.widget == copyBtn || ev.widget == copycmd) {
			textOut.copy();
		}
		if (ev.widget == pastecmd) {
			textIn.paste();
		}
		if (ev.widget == clearBtn || ev.widget == clearcmd) {
			textIn.setText("");
			textOut.setText("");
		}
		if (ev.widget == reverseBtn) {
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
		display = new Display();
		translateThread.start();
		try {
			MobileDevice.getMobileDevice().getScreens()[0].addEventListener(this);
		} catch (Exception e) {
		}
		shell = new MobileShell(display, SWT.NONE, MobileShell.SMALL_STATUS_PANE);
		shell.setText("Bing Translate");
		//shell.setImage(new Image(display, getClass().getResourceAsStream("/page_exported.png")));
		shell.setLayout(new FillLayout());
		parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());
		exitcmd = new Command(shell, Command.EXIT, 1);
		exitcmd.setText("Exit");
		exitcmd.addSelectionListener(this);
		/*
		String s = System.getProperty("microedition.platform");
		
		if(s != null && s.indexOf("platform_version=3.2") > -1) {
			Command group = new Command(shell, Command.COMMANDGROUP, 1);
			group.setText("App");
			aboutcmd = new Command(group, Command.GENERAL, 1);
			aboutcmd.setText("About");
			aboutcmd.addSelectionListener(this);
		} else {
		*/
		Command sets = new Command(shell, Command.COMMANDGROUP, 2);
		sets.setText("Settings");
		Command text = new Command(shell, Command.COMMANDGROUP, 3);
		text.setText("Text");
		langscmd = new Command(sets, Command.GENERAL, 1);
		langscmd.setText("Set languages");
		langscmd.addSelectionListener(this);
		aboutcmd = new Command(shell, Command.GENERAL, 1);
		aboutcmd.setText("About");
		aboutcmd.addSelectionListener(this);
		clearcmd = new Command(text, Command.GENERAL, 3);
		clearcmd.setText("Clear");
		clearcmd.addSelectionListener(this);
		copycmd = new Command(text, Command.GENERAL, 2);
		copycmd.setText("Copy output");
		copycmd.addSelectionListener(this);
		pastecmd = new Command(text, Command.GENERAL, 1);
		pastecmd.setText("Paste input");
		pastecmd.addSelectionListener(this);
		//}
		init();
		shell.open();
		while (!exiting) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
		translateThread.interrupt();
	}
	
	public String getText() {
		/*
		display.syncExec(new Runnable() {
			public void run() {
				try {
					inputText = textIn.getText();
				} catch (Throwable e) {
				}
			}
		});
		*/
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
		
		RowData comboLayout = new RowData();
		comboLayout.width = 150;
		comboLayout.height = 46;
		int comboStyle = SWT.DROP_DOWN;
		if(shell.getSize().x < 300) comboStyle = SWT.NONE;
		comboFrom = new Combo(centerComp, comboStyle);
		comboFrom.setLayoutData(comboLayout);
		comboFrom.setItems(Languages.getLangNames());
		comboFrom.select(Languages.getLastFrom());
		comboFrom.addSelectionListener(selectionListener);
		
		reverseBtn = new Button(centerComp, SWT.CENTER);
		reverseBtn.setText("<>");
		reverseBtn.addSelectionListener(this);
	    reverseBtn.setLayoutData(new RowData(40, 44));
	    
		comboTo = new Combo(centerComp, comboStyle);
		comboTo.setLayoutData(comboLayout);
		comboTo.setItems(Languages.getLangNames());
		comboTo.select(Languages.getLastTo());
		comboTo.addSelectionListener(selectionListener);
		
		reinit(-1);
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
			w = Math.max(sx, sy);
			h = Math.min(sx, sy);
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
		}
		textIn.addModifyListener(modifyListener);
		//textIn.addSelectionListener(selectionListener);
		try {
			if(ti != null) textIn.setText(ti);
			if(to != null) textOut.setText(to);
		} catch (Throwable e) {
		}
		upd();
	}

	private void upd() {
		//textOut.redraw();
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
		//landscape = (event.orientation == Screen.LANDSCAPE);
		reinit(event.orientation);
		updateLangsPosition();
	}

	public void controlMoved(ControlEvent e) {
		updateLangsPosition();
	}

	public void controlResized(ControlEvent e) {
		updateLangsPosition();
	}

}