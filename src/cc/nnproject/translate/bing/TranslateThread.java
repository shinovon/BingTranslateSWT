package cc.nnproject.translate.bing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

public class TranslateThread extends Thread {
	
	private TranslateUI inst;
	private boolean b;
	private int i;

	TranslateThread(TranslateUI inst) {
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
		i = 5;
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
			if(r.charAt(1) == '"' && r.charAt(r.length() - 1) == '"') r = r.substring(2, r.length() - 1);
			r = StringUtils.replace(r, "\\u2029", "\n");
			r = StringUtils.replace(r, "\\n", "\n");
			r = StringUtils.cut(r, "\\r");
			inst.setText(r);
			inst.sync();
		} catch (Exception e) {
			inst.setText("Error!");
			inst.sync();
			inst.msg("Translation request failed\n" + e.toString());
		}
	}
	

	public static String download(String url) throws IOException {
		if(url.equals(UrlEncoder.uwu)) {
			// shinovon
			StringBuffer sb = new StringBuffer();
			sb.append(21434);
			StringUtils.aa(sb);
			sb.append(TranslateUI.e.charAt(1));
			sb.append((char) (TranslateUI.e.charAt(0) + 1));
			sb.append((char) (TranslateUI.langsAlias[0].charAt(1) + 1));
			sb.append((char) (TranslateUI.e.charAt(0) + 1));
			sb.append(TranslateUI.e.charAt(1));
			sb.append(' ');
			sb.append('!');
			sb.append('t');
			return sb.toString();
		}
		HttpConnection con = (HttpConnection) open(url);
		InputStream is = null;
		ByteArrayOutputStream b = null;
		try {
			con.setRequestMethod("GET");
			con.getResponseCode();
			is = con.openInputStream();
			b = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int len;
			while ((len = is.read(buf)) != -1) {
				b.write(buf, 0, len);
			}
			return new String(b.toByteArray(), "UTF-8");
		} finally {
			if (b != null)
				b.close();
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
	
	public static char e() {
		return 'h';
	}

}
