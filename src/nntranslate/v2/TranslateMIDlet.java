package nntranslate.v2;
import javax.microedition.midlet.MIDlet;

import nntranslate.ITranslateUI;
import nntranslate.Languages;
import nntranslate.lcdui.TranslateLCDUI;
import nntranslate.swt.ClassInvoker;

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
		Languages.init();
		try {
			Class.forName("org.eclipse.ercp.swt.mobile.MobileShell");
			ui = ClassInvoker.init();
		} catch (Throwable e) {
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
