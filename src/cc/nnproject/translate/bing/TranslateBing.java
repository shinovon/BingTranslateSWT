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

public class TranslateBing implements Runnable, SelectionListener {

	private static final String[] langs = new String[] { "Русский", "English", "Español" };

	private static final String[] langsAlias = new String[] { "ru", "en", "es" };

	private final ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent ev) {
			to = langsAlias[comboTo.getSelectionIndex()];
			from = langsAlias[comboFrom.getSelectionIndex()];
			inputText = textIn.getText(); 
			translateThread.schedule();
		}
	};

	private final SelectionListener selectionListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent ev) {
			
		}
		public void widgetSelected(SelectionEvent ev) {
			to = langsAlias[comboTo.getSelectionIndex()];
			from = langsAlias[comboFrom.getSelectionIndex()];
			inputText = textIn.getText(); 
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

	public TranslateBing() {
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
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
		translateThread.interrupt();
	}
	
	String getText() {
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
		final GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;
		final GridData fillHorizontal = new GridData();
		fillHorizontal.horizontalAlignment = GridData.FILL;
		fillHorizontal.grabExcessHorizontalSpace = true;
		fillHorizontal.verticalAlignment = GridData.CENTER;
		final GridData buttonLayout = new GridData();
		buttonLayout.grabExcessHorizontalSpace = true;
		buttonLayout.verticalAlignment = GridData.CENTER;
		textIn = new Text(parent, SWT.WRAP | SWT.MULTI);
		textIn.setLayoutData(layoutData);
		textIn.addModifyListener(modifyListener);
		textIn.addSelectionListener(selectionListener);
		centerComp = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		RowLayout rowLayout = new RowLayout();
	    //rowLayout.wrap = false;
	    //rowLayout.pack = false;
	    rowLayout.justify = true;
	    rowLayout.marginTop = 5;
	    rowLayout.marginBottom = 5;
	    rowLayout.marginLeft = 1;
	    rowLayout.marginRight = 1;
	    rowLayout.spacing = 1;
		centerComp.setLayout(rowLayout);
		//centerComp.setLayout(gridLayout);
		centerComp.setLayoutData(fillHorizontal);
		RowData comboLayout = new RowData();
		comboLayout.width = 150;
		comboLayout.height = 40;
		comboFrom = new Combo(centerComp, SWT.DROP_DOWN);
		comboFrom.setLayoutData(comboLayout);
		comboFrom.setItems(langs);
		comboFrom.select(0);
		comboFrom.addSelectionListener(selectionListener);
		GridData gridData = new GridData();
	    gridData.widthHint = 20;
		reverseBtn = new Button(centerComp, SWT.CENTER);
		reverseBtn.setText("<>");
		reverseBtn.addSelectionListener(this);
		//reverseBtn.setLayoutData(gridData);
	    reverseBtn.setLayoutData(new RowData(40, 40));
		comboTo = new Combo(centerComp, SWT.DROP_DOWN);
		comboTo.setLayoutData(comboLayout);
		comboTo.setItems(langs);
		comboTo.select(1);
		comboTo.addSelectionListener(selectionListener);
		textOut = new Text(parent, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textOut.setLayoutData(layoutData);
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
