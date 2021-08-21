package cc.nnproject.translate.bing;

import javax.microedition.midlet.MIDlet;

import cc.nnproject.translate.ITranslateUI;
import cc.nnproject.translate.bing.lcdui.TranslateUIBingLCD;
import cc.nnproject.translate.bing.swt.TranslateUIBingSWT;

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
		try {
			Class.forName("org.eclipse.ercp.swt.mobile.MobileShell");
			ui = new TranslateUIBingSWT();
		} catch (Throwable e) {
			ui = new TranslateUIBingLCD();
			/*
			final Command exit = new Command("Exit", Command.EXIT, 1);
			Alert a = new Alert("", "", null, null);
			a.setString("Your device does not support ESWT");
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
