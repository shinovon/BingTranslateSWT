package cc.nnproject.translate.bing;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

public class TranslateThread extends Thread {
	
	private TranslateBing inst;
	private boolean b;
	private int i;

	TranslateThread(TranslateBing inst) {
		super("Translate Thread");
		this.inst = inst;
	}

	public void run() {
		try {
			while(!inst.exiting) {
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

	public void schedule() {
		b = true;
		i = 4;
	}

	public void now() {
		b = true;
		i = 0;
	}

	private void translate() {
		String s = inst.getText();
		inst.sync();
		if(s == null || s.length() < 2) return;
		inst.setText("Processing..");
		String from = inst.getFromLang();
		String to = inst.getToLang();
	    String req = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate?appId=037C394ED1EA70440C3B5E07FA0A6A837DCE47A9&from=" + from + "&to=" + to + "&text=" + UrlEncoder.encode(s);
	    try {
			String r = download(req);
			if(r.startsWith("\"") && r.endsWith("\"")) r = r.substring(1, r.length() - 1);
			inst.setText(r);
			inst.sync();
		} catch (Exception e) {
			inst.setText(e.toString());
			inst.sync();
		}
	}
	

	public static String download(String url) throws IOException {
		System.out.println("GET " + url);
		HttpConnection con = (HttpConnection) open(url);

		InputStream is = null;
		try {
			con.setRequestMethod("GET");
			con.getResponseCode();
			is = con.openInputStream();
			StringBuffer sb = new StringBuffer();
			byte[] buf = new byte[2048];
			int len;
			while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "UTF-8"));
			}
			return sb.toString();
		} finally {
			if (is != null)
				is.close();
			if (con != null)
				con.close();
		}
	}

	public static ContentConnection open(String url) throws IOException {
		ContentConnection con = (ContentConnection) Connector.open(url, Connector.READ);
		//if (con instanceof HttpConnection) ((HttpConnection) con).setRequestProperty("User-Agent", "Mozilla/5.0");
		return con;
	}

}
