package cc.nnproject.translate.bing;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import cc.nnproject.translate.bing.swt.TranslateUISWT;

public class TranslateBingMIDlet extends MIDlet {

	public static TranslateBingMIDlet midlet;
	private boolean started;

	public TranslateBingMIDlet() {
		midlet = this;
	}

	public void destroyApp(boolean b) {

	}

	protected void pauseApp() {

	}

	protected void startApp() {
		if(started)
			return;
		started = true;
		try {
			new TranslateUISWT();
		} catch (Throwable e) {
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
		}
	}

}
