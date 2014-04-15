package cz.cuni.mff.xrg.scraper.css_parser.utils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

/**
 *  Document cache. It stores downloaded files to hard drive.
 * 
 *  @author Jakub Starka
 */
public class Cache {

	public static void setTrustAllCerts() throws Exception
	{
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) {	}
				public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) {	}
			}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance( "SSL" );
			sc.init( null, trustAllCerts, new java.security.SecureRandom() );
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier( 
				new HostnameVerifier() {
					public boolean verify(String urlHostName, SSLSession session) {
						return true;
					}
				});
		}
		catch ( Exception e ) {
			//We can not recover from this exception.
			e.printStackTrace();
		}
	}	

	private static boolean hash = false;

	private static int downloaded = 0;

	private static String basePath = "./";

	public static boolean rewriteCache = false;

	public static Logger logger;

	//private static HashSet<String> s = new HashSet<String>();

	public static void init(boolean hash) {
		Cache.hash = hash;
	}

	public static int getInterval() {
		return interval;
	}

	public static void setInterval(int interval) {
		Cache.interval = interval;
	}

	public static void setTimeout(int timeout) {
        Cache.timeout = timeout;
    }

	public static void setBaseDir(String basedir)
	{
		basePath = basedir;
	}

	private static int interval;
	private static int timeout;
	private static long lastDownload = 0;

	public static Document getDocument(URL url, int maxAttempts) throws IOException, InterruptedException {   
		String host = url.getHost();
		if (url.getPath().lastIndexOf("/") == -1) {
			return null;
		}
		String path = url.getPath().substring(1, url.getPath().lastIndexOf("/"));
		String file = "root";
		if (url.getPath().lastIndexOf("/") != (url.getPath().length() - 1))
			file = url.getFile().substring(url.getFile().lastIndexOf("/") + 1).replace("?", "@");

		//File hHost = new File(Cache.basePath, host);
		File hPath = new File(Cache.basePath, host + File.separatorChar + path);
		File hFile = null;

		if (hash) {
			try {
				byte[] urlHash = MessageDigest.getInstance("MD5").digest(file.getBytes("UTF-8"));

				StringBuilder sb = new StringBuilder(2*urlHash.length); 
				for(byte b : urlHash){ 
					sb.append(String.format("%02x", b&0xff)); 
				}
				hFile = new File(hPath, sb.toString());
			} catch (NoSuchAlgorithmException ex) {
				logger.info(ex.getLocalizedMessage());
			}
		} else {
			hFile = new File(hPath, file);
		}

		Document out = null;

		if (!hFile.exists() || rewriteCache) {
			//if (!s.contains(file)) {
			hPath.mkdirs();
			int attempt = 0;
			while (attempt < maxAttempts) {
				java.util.Date date= new java.util.Date();
				long curTS = date.getTime();
				logger.info("Downloading URL (attempt " + attempt + "): " + url.getHost() + url.getFile());
				if (lastDownload + interval > curTS ) {
					/*                    logger.info("LastDownload: " + lastDownload);
                    logger.info("CurTS: " + curTS);
                    logger.info("Interval: " + interval);*/
					logger.debug("Sleeping: " + (lastDownload + interval - curTS));
					Thread.sleep(lastDownload + interval - curTS);
				}
				try {
					out = Jsoup.parse(url, timeout);
					java.util.Date date2= new java.util.Date();
					lastDownload = date2.getTime();
					logger.info("Downloaded URL (attempt " + attempt + ") in " + (lastDownload - curTS) + " ms : " + url.getHost() + url.getFile());
					break;
				} catch (SocketTimeoutException ex) {
					java.util.Date date3= new java.util.Date();
					long failed = date3.getTime();
					logger.info("Timeout (attempt " + attempt + ") in " + (failed - curTS)+ " : " + url.getHost() + url.getFile());
					//Thread.sleep(2000);
				} catch (java.io.IOException ex) {
					logger.info("Warning (retrying): " + ex.getMessage());
					Thread.sleep(interval);
				}
				attempt ++;
			}
			if (attempt == maxAttempts) {
				logger.warn("Warning, max attemts reached. Skipping: " + url.getHost() + url.getPath());
				/*throw new SocketTimeoutException();*/
				return null;
			}
			hFile.createNewFile();
			BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
			fw.append(out.outerHtml());
			fw.close();
			//logger.info(++downloaded);
		} else {
			//logger.info("Using cache for URL: " + url.getHost() + url.getFile());
			out = Jsoup.parse(hFile, null, host);
		}
		return out;
	}

}
