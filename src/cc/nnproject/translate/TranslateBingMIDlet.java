package cc.nnproject.translate;

import javax.microedition.midlet.MIDlet;

import cc.nnproject.translate.bing.TranslateBing;

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
		new TranslateBing();
	}

}
