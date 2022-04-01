import java.io.IOException;
import java.util.Vector;

public class TranslateThread extends AbstractTranslateThread {

	private String lastInput;
	private String lastTranslated;
	private String engine;
	private String instance;
	private String proxy;

	public TranslateThread(ITranslateUI ui) {
		super(ui);
		this.engine = "google";
	}
	
	public void setEngine(String e) {
		engine = e;
	}
	
	public void setInstance(String inst) {
		instance = inst;
	}

	public void setProxy(String s) {
		proxy = s;
	}

	protected void translate() {
		if(d) {
			d = false;
			engine = Languages.getCurrentEngine();
			ui.setDownloading(true);
			try {
				ui.setLanguages(getTargetLanguages(engine));
				ui.downloadingDone();
			} catch (Exception e) {
				ui.downloadingError(e.toString());
				e.printStackTrace();
			}
			ui.setDownloading(false);
			return;
		}
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
		if(engine == null) {
			ui.setText("No engine set!");
			return;
		}
		ui.setTranslating(true);
		//ui.setText("Loading..");
		String from = ui.getFromLang();
		String to = ui.getToLang();
		if(engine.equals("bing")) {
			String req = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?appId=037C394ED1EA70440C3B5E07FA0A6A837DCE47A9&from=" + from + "&to=" + to + "&text=" + Util.encodeURL(s);
			if(proxy != null && proxy.length() > 0) {
				req = proxy + Util.encodeURL(req);
			}
			try {
				String r = Util.get(req);
				if(r.charAt(1) == '"' && r.charAt(r.length() - 1) == '"')
					r = r.substring(2, r.length() - 1);
				else if(r.charAt(0) == '"' && r.charAt(r.length() - 1) == '"')
					r = r.substring(1, r.length() - 1);
				r = StringUtils.replace(r, "\\u2029", "\n");
				r = StringUtils.replace(r, "\\n", "\n");
				r = StringUtils.cut(r, "\\r");
				lastTranslated = r;
				ui.setText(r);
				ui.sync();
			} catch (Throwable e) {
				ui.setText("Error!");
				ui.sync();
				ui.msg("Translation failed\n" + e.toString());
			}
		} else {
			String req = "https://" + instance + "/api/translate/?engine="+engine+"&from=" + from + "&to=" + to + "&text=" + Util.encodeURL(s);
			if(proxy != null && proxy.length() > 0) {
				req = proxy + Util.encodeURL(req);
			}
		    try {
				String r = Util.get(req);
				lastTranslated = r;
				ui.setText(r);
				ui.sync();
			} catch (Throwable e) {
				ui.setText("Error!");
				ui.sync();
				ui.msg("Translation failed\n" + e.toString());
			}
		}
		ui.setTranslating(false);
	    Languages.save();
	}
	/*
	public String[][] getSourceLanguages(String en) throws IOException {
		if(en == null) {
			en = engine;
		}
		if(en.equals("bing")) {
			return BingLanguages.SUPPORTED_LANGUAGES;
		} else {
			String req = "https://" + instance + "/api/source_languages/?engine="+engine;
			if(proxy != null && proxy.length() > 0) {
				req = proxy + Util.encodeURL(req);
			}
			return parseLanguages(Util.get(req));
		}
	}
	*/
	public String[][] getTargetLanguages(String en) throws IOException {
		if(en == null) {
			en = engine;
		}
		if(en.equals("bing")) {
			return BingLanguages.SUPPORTED_LANGUAGES;
		} else {
			String req = "https://" + instance + "/api/target_languages/?engine="+engine;
			if(proxy != null && proxy.length() > 0) {
				req = proxy + Util.encodeURL(req);
			}
			return parseLanguages(Util.get(req));
		}
	}
	
	private String[][] parseLanguages(String s) {
		Vector v = new Vector();
		String[] sr = StringUtils.split(StringUtils.cut(s, "\r"), '\n');
		for(int i = 0; i < sr.length; i++) {
			System.out.println(sr[i]);
			if(sr[i].length() == 0) continue;
			v.addElement(new String[] { sr[i+1], sr[i] });
			i++;
		}
		Object[] vv = new Object[v.size()];
		v.copyInto(vv);
		String[][] res = new String[vv.length][2];
		v = null;
		for(int i = 0; i < vv.length; i++) {
			res[i] = (String[]) vv[i];
		}
		return res;
	}
	
	public void clearLastInput() {
		lastInput = null;
	}

}
