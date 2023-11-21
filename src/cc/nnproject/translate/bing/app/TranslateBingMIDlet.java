package cc.nnproject.translate.bing.app;

import javax.microedition.midlet.MIDlet;

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.Languages;
import cc.nnproject.translate.lcdui.TranslateLCDUI;
import cc.nnproject.translate.swt.ClassInvoker;

public class TranslateBingMIDlet extends MIDlet {

	public static TranslateBingMIDlet midlet;
	private boolean started;
	private ITranslateUI ui;

	public TranslateBingMIDlet() {
		midlet = this;
	}

	public void destroyApp(boolean b) {
		ui.exit();
	}

	protected void pauseApp() {

	}

	protected void startApp() {
		if(started)
			return;
		started = true;
		Languages.init(true);
		try {
			Class.forName("org.eclipse.ercp.swt.mobile.MobileShell");
			ClassInvoker.init();
		} catch (Throwable e) {
			//Languages.init(false);
			ui = new TranslateLCDUI();
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
