package cc.nnproject.translate;

public class TranslateBingThread extends AbstractTranslateThread {

	private String lastInput;
	private String lastTranslated;

	public TranslateBingThread(ITranslateUI ui) {
		super(ui);
	}

	protected void translate() {
		String s = ui.getText();
		if(r && lastInput != null && lastInput.equals(s)) {
			r = false;
			if(lastTranslated != null) ui.setText(lastTranslated);
			return;
		}
		if(lastInput != null && lastInput.equals(s) && lastTranslated != null) return;
		lastInput = s;
		ui.sync();
		if(s == null || s.length() < 2) return;
		ui.setTranslating(true);
//		ui.setText("Processing..");
		String from = ui.getFromLang();
		String to = ui.getToLang();
		String req = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?appId=037C394ED1EA70440C3B5E07FA0A6A837DCE47A9&from=" + from + "&to=" + to + "&text=" + Util.encodeURL(s);
	    try {
			String r = Util.get(req).trim();
			if(r.charAt(1) == '"' && r.charAt(r.length() - 1) == '"') {
				r = r.substring(2, r.length() - 1);
			} else if(r.charAt(0) == '"' && r.charAt(r.length() - 1) == '"') {
				r = r.substring(1, r.length() - 1);
			}
			r = StringUtils.replace(r, "\\u2029", "\n");
			r = StringUtils.replace(r, "\\n", "\n");
			r = StringUtils.replace(r, "\\r", "");
			lastTranslated = r;
			ui.setText(r);
			ui.sync();
		} catch (Throwable e) {
			ui.setText("Error!");
			ui.sync();
			ui.error(e.toString());
			ui.msg("Translation failed\n" + e.toString());
		}
		ui.setTranslating(false);
	    Languages.save();
	}
	
	public void clearLastInput() {
		lastInput = null;
	}
	
	public static char e() {
		return 'h';
	}

}
