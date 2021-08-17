package cc.nnproject.translate.bing;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.ercp.swt.mobile.MobileShell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import cc.nnproject.translate.TranslateBingMIDlet;

public class TranslateBing implements Runnable, SelectionListener {
	
	private Display display;
	private MobileShell shell;
	
	private boolean exiting;
	
	private Composite parent;
	
	private Command exitcmd;

	TranslateBing() {
		new Thread(this).start();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

	public void widgetSelected(SelectionEvent e) {
		if(e.widget == exitcmd)
			exit();
	}

	public void run() {
		display = new Display();
		shell = new MobileShell(display, SWT.NONE, 2);
		shell.setLayout(new GridLayout());
		parent = new Composite(shell, SWT.NONE);
		init();
		while (!exiting) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

	private void init() {
		exitcmd = new Command(shell, Command.EXIT, 1);
		exitcmd.setText("Exit");
		exitcmd.addSelectionListener(this);
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
	}

	private void upd() {
		parent.layout();
		shell.layout();
	}
	
	public void exit() {
		exiting = true;
		Display.getDefault().wake();
		TranslateBingMIDlet.midlet.destroyApp(true);
		TranslateBingMIDlet.midlet.notifyDestroyed();
	}

}
