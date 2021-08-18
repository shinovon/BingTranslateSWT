package cc.nnproject.translate.bing;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.ercp.swt.mobile.MobileShell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Text;

import cc.nnproject.translate.TranslateBingMIDlet;

public class TranslateUI implements Runnable, SelectionListener {

	static final String[] langs = new String[] { "Русский (Russian)", "Українська (Ukrainian)", "Беларуская (Belarusian)", "Қазақша (Kazakh)", "English", "Español", "Français", "Italian", "Deutsch", "日本 (Japanese)", "中国人 (Chinese)"};

	static final String[] langsAlias = new String[] { "ru", "uk", "be", "kk", "en", "es", "fr", "it", "de", "ja", "zh-CN" };

	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent ev) {
			to = langsAlias[comboTo.getSelectionIndex()];
			from = langsAlias[comboFrom.getSelectionIndex()];
			translateThread.schedule();
		}
	};

	private final SelectionListener selectionListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent ev) {
			
		}
		public void widgetSelected(SelectionEvent ev) {
			to = langsAlias[comboTo.getSelectionIndex()];
			from = langsAlias[comboFrom.getSelectionIndex()];
			try {
				inputText = textIn.getText();
			} catch (Exception e) {
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

	private Combo comboFrom;
	private Combo comboTo;
	private Text textIn;

	private Text textOut;

	private Button reverseBtn;

	private Composite centerComp;

	private TranslateThread translateThread = new TranslateThread(this);

	private Command aboutcmd;

	private String from;
	private String to;

	private int lastHeight;

	private Button clearBtn;

	private Composite textComp;
	private Composite textCenterComp;

	public TranslateUI() {
		new Thread(this, "Main SWT Thread").start();
	}

	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void widgetSelected(SelectionEvent ev) {
		if (ev.widget == exitcmd) exit();
		if(ev.widget == aboutcmd) msg("Bing Translate\nMade by shinovon (nnproject.cc)");
		if (ev.widget == reverseBtn) {
			int in = comboFrom.getSelectionIndex();
			comboFrom.select(comboTo.getSelectionIndex());
			comboTo.select(in);
			try {
				inputText = textIn.getText();
			} catch (Exception e) {
			}
			translateThread.schedule();
		}
	}
	
	void msg(final String s) {
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
		shell = new MobileShell(display, SWT.NONE, 2);
		shell.setLayout(new FillLayout());
		parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());
		exitcmd = new Command(shell, Command.OK, 1);
		exitcmd.setText("Exit");
		exitcmd.addSelectionListener(this);
		aboutcmd = new Command(shell, Command.EXIT, 1);
		aboutcmd.setText("About");
		aboutcmd.addSelectionListener(this);
		init();
		shell.open();
		while (!exiting) {
			if(lastHeight != shell.getSize().x) {
				reinit();
				lastHeight = shell.getSize().x;
			}
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
		translateThread.interrupt();
	}
	
	String getText() {
		display.syncExec(new Runnable() {
			public void run() {
				try {
					inputText = textIn.getText();
				} catch (Exception e) {
				}
			}
		});
		return inputText;
	}
	
	void setText(final String s) {
		display.syncExec(new Runnable() {
			public void run() {
				textOut.setText(s);
			}
		});
	}

	String getFromLang() {
		display.syncExec(new Runnable() {
			public void run() {
				from = langsAlias[comboFrom.getSelectionIndex()];
			}
		});
		return from;
	}

	String getToLang() {
		display.syncExec(new Runnable() {
			public void run() {
				to = langsAlias[comboTo.getSelectionIndex()];
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
		
		comboFrom = new Combo(centerComp, SWT.DROP_DOWN);
		comboFrom.setLayoutData(comboLayout);
		comboFrom.setItems(langs);
		comboFrom.select(0);
		comboFrom.addSelectionListener(selectionListener);
		
		reverseBtn = new Button(centerComp, SWT.CENTER);
		reverseBtn.setText("<>");
		reverseBtn.addSelectionListener(this);
	    reverseBtn.setLayoutData(new RowData(40, 44));
	    
		comboTo = new Combo(centerComp, SWT.DROP_DOWN);
		comboTo.setLayoutData(comboLayout);
		comboTo.setItems(langs);
		comboTo.select(1);
		comboTo.addSelectionListener(selectionListener);
		
		reinit();
	}
	
	public void reinit() {
		int w = shell.getSize().x;
		int h = shell.getSize().y;
		String ti = "";
		if(textIn != null) ti = textIn.getText();
		String to = "";
		if(textOut != null) to = textOut.getText();
		if(w > h && w > 600) {
			if(textIn != null) textIn.dispose();
			textIn = null;
			if(textOut != null) textOut.dispose();
			textOut = null;
			if(clearBtn != null) clearBtn.dispose();
			clearBtn = null;
			if(textCenterComp != null) textCenterComp.dispose();
			textCenterComp = null;
			if(textComp != null) textComp.dispose();
			textComp = null;
			
			final GridData fill = new GridData();
			fill.horizontalAlignment = GridData.FILL;
			fill.grabExcessHorizontalSpace = true;
			fill.grabExcessVerticalSpace = true;
			fill.verticalAlignment = GridData.FILL;
			GridData fillVertical = new GridData();
			fillVertical.horizontalAlignment = GridData.CENTER;
			fillVertical.grabExcessVerticalSpace = true;
			fillVertical.verticalAlignment = GridData.FILL;

			RowData comboLayout = new RowData();
			comboLayout.width = 280;
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout);
			comboTo.setLayoutData(comboLayout);

			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			textComp = new Composite(parent, SWT.NONE);
			textComp.setLayout(layout);
			textComp.setLayoutData(fill);

			textIn = new Text(textComp, SWT.WRAP | SWT.MULTI);
			textIn.addModifyListener(modifyListener);
			textIn.addSelectionListener(selectionListener);
			textIn.setLayoutData(fill);
			textIn.setText(ti);
			
			textCenterComp = new Composite(textComp, SWT.NONE);
			textCenterComp.setLayoutData(fillVertical);
			RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
			rowLayout.pack = false;
			rowLayout.wrap = false;
			textCenterComp.setLayout(rowLayout);

			textOut = new Text(textComp, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
			textOut.setLayoutData(fill);
			textOut.setText(to);
			
			textIn.moveAbove(textCenterComp);
			textOut.moveBelow(textCenterComp);
			
			clearBtn = new Button(textCenterComp, SWT.CENTER);
			clearBtn.setText("x");
			clearBtn.setLayoutData(new RowData(40, 44));
			clearBtn.addSelectionListener(this);
			
			centerComp.moveAbove(textComp);
		} else {
			if(textIn != null) textIn.dispose();
			textIn = null;
			if(textOut != null) textOut.dispose();
			textOut = null;
			if(clearBtn != null) clearBtn.dispose();
			clearBtn = null;
			if(textCenterComp != null) textCenterComp.dispose();
			textCenterComp = null;
			if(textComp != null) textComp.dispose();
			textComp = null;
			
			final GridData fill = new GridData();
			fill.horizontalAlignment = GridData.FILL;
			fill.grabExcessHorizontalSpace = true;
			fill.grabExcessVerticalSpace = true;
			fill.verticalAlignment = GridData.FILL;
			
			textIn = new Text(parent, SWT.WRAP | SWT.MULTI);
			textIn.setLayoutData(fill);
			textIn.addModifyListener(modifyListener);
			textIn.addSelectionListener(selectionListener);
			textIn.setText(ti);
			
			textOut = new Text(parent, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
			textOut.setLayoutData(fill);
			textOut.setText(to);
			
			RowData comboLayout = new RowData();
			comboLayout.width = 150;
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout);
			comboTo.setLayoutData(comboLayout);
			
			centerComp.moveBelow(textIn);
		}
		upd();
	}

	private void upd() {
		//textOut.redraw();
		parent.layout();
		shell.layout();
	}

	public void exit() {
		exiting = true;
		Display.getDefault().wake();
		TranslateBingMIDlet.midlet.destroyApp(true);
		TranslateBingMIDlet.midlet.notifyDestroyed();
	}

	public void sync() {
		display.syncExec(new Runnable() {
			public void run() {
				upd();
			}
		});
	}

}
