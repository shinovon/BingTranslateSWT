package cc.nnproject.translate;

public abstract class AbstractTranslateThread extends Thread {
	
	protected ITranslateUI ui;
	private boolean b;
	private int i;

	public AbstractTranslateThread(ITranslateUI ui) {
		super("Translate Thread");
		this.ui = ui;
	}

	public final void run() {
		try {
			while(ui.running()) {
				if(b) {
					if(i > 0) i--;
					else {
						translate();
						b = false;
					}
				}
				Thread.sleep(500L);
				Thread.yield();
			}
		} catch (InterruptedException e) {
		}
	}

	public final void schedule() {
		schedule(5);
	}

	public final void now() {
		schedule(0);
	}

	public final void schedule(int i) {
		b = true;
		this.i = i;
	}
	
	protected abstract void translate();

}
