package cc.nnproject.translate.bing.swt;

import java.io.IOException;

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

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.StringUtils;
import cc.nnproject.translate.Util;
import cc.nnproject.translate.bing.TranslateBingMIDlet;
import cc.nnproject.translate.bing.TranslateBingThread;

public class TranslateUISWT implements Runnable, SelectionListener, ITranslateUI {


	// do not inline
	// юзается в about
	public static String e = "nn";
	
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
			} catch (Throwable e) {
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

	private TranslateBingThread translateThread = new TranslateBingThread(this);

	private Command aboutcmd;

	private String from;
	private String to;

	private int lastHeight;

	private Button clearBtn;

	private Composite textComp;
	private Composite textCenterComp;

	public TranslateUISWT() {
		new Thread(this, "Main SWT Thread").start();
	}

	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void widgetSelected(SelectionEvent ev) {
		if (ev.widget == exitcmd)
			exit();
		if (ev.widget == aboutcmd) {
			StringBuffer sb = new StringBuffer();
			// Bing
			sb.append((langsAlias[2].charAt(0) + "").toUpperCase());
			sb.append(langsAlias[7].charAt(0));
			sb.append(e.charAt(0));
			sb.append((char) (e.charAt(1) - 7));
			sb.append(' ');
			// Translate
			sb.append((langsAlias[7].charAt(1) + "").toUpperCase());
			sb.append(langsAlias[6].charAt(1));
			sb.append((char) ('b' - 1));
			sb.append(e.charAt(1));
			String b = "Maho pidoras, u know.";
			sb.append(b.charAt(11));
			sb.append((char) (e.charAt(1) - 2));
			sb.append((char) ('c' - 2));
			sb.append(langsAlias[7].charAt(1));
			sb.append(langsAlias[2].charAt(1));
			sb.append('\n');
			// Made
			sb.append(b.charAt(0));
			sb.append(b.charAt(1));
			sb.append((char) (b.charAt(1) + 3));
			sb.append(langsAlias[2].charAt(1));
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
			// site
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
			// "Bing Translate\nMade by shinovon (nnproject.cc)"
			msg(sb.toString());
		}
		if (ev.widget == clearBtn) {
			textIn.setText("");
			textOut.setText("");
		}
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
		lastHeight = shell.getSize().x;
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
	
	public String getText() {
		display.syncExec(new Runnable() {
			public void run() {
				try {
					inputText = textIn.getText();
				} catch (Throwable e) {
				}
			}
		});
		return inputText;
	}
	
	public void setText(final String s) {
		display.syncExec(new Runnable() {
			public void run() {
				textOut.setText(s);
			}
		});
	}

	public String getFromLang() {
		display.syncExec(new Runnable() {
			public void run() {
				from = langsAlias[comboFrom.getSelectionIndex()];
			}
		});
		return from;
	}

	public String getToLang() {
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
		comboTo.select(4);
		comboTo.addSelectionListener(selectionListener);
		
		reinit();
	}
	
	public void reinit() {
		int w = shell.getSize().x;
		int h = shell.getSize().y;
		String ti = null;
		String to = null;
		try {
			if(textIn != null) ti = textIn.getText();
			if(textOut != null) to = textOut.getText();
		} catch (Throwable e) {
			if(inputText != null) ti = inputText;
		}
		if(w > h && w > 600) {
			// 640x360 (album)
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
			try {
				if(ti != null) textIn.setText(ti);
				if(to != null) textOut.setText(to);
			} catch (Throwable e) {
			}
			textIn.addModifyListener(modifyListener);
			textIn.addSelectionListener(selectionListener);
			
			textIn.moveAbove(textCenterComp);
			textOut.moveBelow(textCenterComp);
			
			clearBtn = new Button(textCenterComp, SWT.CENTER);
			clearBtn.setText("x");
			clearBtn.setLayoutData(new RowData(40, 44));
			clearBtn.addSelectionListener(this);
			
			centerComp.moveAbove(textComp);
		} else if(w > h && w > 300) {
			// 320x240 (album) 9.3*
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
			
			textIn = new Text(parent, SWT.BORDER);
			textIn.setLayoutData(fill);
			
			textOut = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
			textOut.setLayoutData(fill);
			try {
				if(ti != null) textIn.setText(ti);
				if(to != null) textOut.setText(to);
			} catch (Throwable e) {
			}
			textIn.addModifyListener(modifyListener);
			textIn.addSelectionListener(selectionListener);
			
			RowData comboLayout = new RowData();
			comboLayout.width = 120;
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout); 
			comboTo.setLayoutData(comboLayout);
			
			centerComp.moveBelow(textIn);
		} else if(w < 300) {
			// 240x320 (portrait) 9.3*
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
			
			textIn = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
			textIn.setLayoutData(fill);
			
			textOut = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.READ_ONLY);
			textOut.setLayoutData(fill);
			try {
				if(ti != null) textIn.setText(ti);
				if(to != null) textOut.setText(to);
			} catch (Throwable e) {
			}
			textIn.addModifyListener(modifyListener);
			textIn.addSelectionListener(selectionListener);
			
			RowData comboLayout = new RowData();
			comboLayout.width = 90;
			comboLayout.height = 46;
			comboFrom.setLayoutData(comboLayout);
			comboTo.setLayoutData(comboLayout);
			
			centerComp.moveBelow(textIn);
		} else {
			// 360x640 (portrait)
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
			
			textIn = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
			textIn.setLayoutData(fill);
			
			textOut = new Text(parent, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
			textOut.setLayoutData(fill);
			try {
				if(ti != null) textIn.setText(ti);
				if(to != null) textOut.setText(to);
			} catch (Throwable e) {
			}
			
			textIn.addModifyListener(modifyListener);
			textIn.addSelectionListener(selectionListener);
			
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
				int w = shell.getSize().x;
				if(w < 600 && w > 300) {
					shell.redraw();
					parent.redraw();
					textOut.redraw();
				}
			}
		});
	}

	public boolean running() {
		// TODO Auto-generated method stub
		return false;
	}

}
