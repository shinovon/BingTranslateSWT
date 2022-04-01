
public abstract class AbstractTranslateThread extends Thread {
	
	protected ITranslateUI ui;
	protected boolean r;
	private boolean b;
	private int i;
	protected boolean d;

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

	/** Поставить таймер на 2.5 секунд */
	public final void schedule() {
		schedule(5);
	}
	
	public final void scheduleRetext() {
		schedule(1);
		r = true;
	}
	
	public final void setDownload() {
		d = true;
	}

	/** Поставить таймер на 0 секунд <br>
	 * перевод осуществится при следующем шаге */
	public final void now() {
		schedule(0);
	}

	/** Выполнить перевод через i*0.5 секунд */
	public final void schedule(int i) {
		b = true;
		this.i = i;
	}
	
	protected abstract void translate();

}
