package nnproject.translatev2;


import javax.microedition.midlet.MIDlet;

import ITranslateUI;
import Languages;
import lcdui.TranslateLCDUI;
import swt.ClassInvoker;

public class TranslateMIDlet extends MIDlet {

	public static TranslateMIDlet midlet;
	private boolean started;
	private ITranslateUI ui;

	public TranslateMIDlet() {
		midlet = this;
	}

	public void destroyApp(boolean b) {
		if(ui != null) ui.exit();
	}

	protected void pauseApp() {

	}

	protected void startApp() {
		if(started)
			return;
		started = true;
		Languages.init(true);
		try {
			Class.forName("swt.TranslateSWTUI");
			Class.forName("org.eclipse.ercp.swt.mobile.MobileShell");
			ClassInvoker.init();
		} catch (Throwable e) {
			//Languages.init(false);
			//ui = new TranslateLCDUI();
			notifyDestroyed();
			/*
			final Command exit = new Command("Exit", Command.EXIT, 1);
			Alert a = new Alert("", "", null, null);
			a.setString("Your device does not support eSWT");
			a.addCommand(exit);
			a.setCommandListener(new CommandListener() {
				public void commandAction(Command c, Displayable d) {
					if(c == exit) notifyDestroyed();
				}
			});
			Display.getDisplay(this).setCurrent(a);
			*/
		}
	}

}
