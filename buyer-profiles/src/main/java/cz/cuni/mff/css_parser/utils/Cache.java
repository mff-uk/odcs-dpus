package cz.cuni.mff.css_parser.utils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
/**
 *  Document cache. It stores downloaded files to hard drive.
 * 
 *  @author Jakub Klímek
 */

public class Cache {
	
    public static Logger logger;
    
    public static RDFDataUnit stats;
	
    private static String BPOprefix = "http://linked.opendata.cz/ontology/domain/buyer-profiles/";
    private static String xsdPrefix = "http://www.w3.org/2001/XMLSchema#";
    
    public static Validator validator;

    public static int validXML = 0;
    public static int invalidXML = 0;
    
    private static boolean validate(String file, String url)
	{
		try {
			// parse an XML document into a DOM tree
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			ByteArrayInputStream is = new ByteArrayInputStream(file.getBytes("UTF-8"));
			org.w3c.dom.Document document = parser.parse(is);

			// validate the DOM tree
			try {
			    
				Source xmlSource = new StreamSource( new StringReader(file));
				validator.validate(xmlSource);
			    return true;
			} catch (SAXException e) {
			    // instance document is invalid!
				logger.debug("Invalid XML: " + e.getLocalizedMessage());
				stats.addTriple(stats.createURI(url.toString()), stats.createURI(BPOprefix + "invalidMessage"), stats.createLiteral(e.getLocalizedMessage()));
				return false;
			}
		}
		catch (Exception e)
		{
			logger.debug("Invalid XML: " + e.getLocalizedMessage());
			//e.printStackTrace();
			return false;
		}
	}
    
    private static String getURLContent(String p_sURL) throws IOException
	{
	    URL oURL;
	    String sResponse = null;

        oURL = new URL(p_sURL);
        oURL.openConnection();
    	sResponse = IOUtils.toString(oURL, "UTF-8");

	    return sResponse;
	}

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

    private static int downloaded = 0;

    //private static String basePath = "D:\\scraper\\";
    private static String basePath = "./cache/";
    
    public static boolean rewriteCache = false;

    private static HashSet<String> s = new HashSet<String>();

    public static void init() {
	File f = new File(Cache.basePath, "www.isvzus.cz/cs/Form/Display");
	File fs = new File(Cache.basePath, "www.isvzus.cz/cs/Searching");
	s.addAll(Arrays.asList(f.list()));
	s.addAll(Arrays.asList(fs.list()));
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
    private static long lastDownload = 0;
    private static int timeout;
    
    public static Document getDocument(URL url, int maxAttempts, String datatype) throws IOException, InterruptedException {   
	String host = url.getHost();
        if (url.getPath().lastIndexOf("/") == -1) {
            return null;
        }
	
    String path;
    String file;
    if (url.getPath().lastIndexOf("/") == 0)
	{
	    path = url.getPath().substring(1).replace("?", "_");
		file = url.getFile().substring(1).replace("/", "@").replace("?", "@");
		if (file.isEmpty()) return null;
	}
	else
	{
		//logger.debug(url);
		//logger.debug(url.getPath());
		path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
	    //logger.debug(path);
	    file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
	    //logger.debug(url.getFile());
	    //logger.debug(file);
	    if (file.isEmpty()) return null;
	}

	//File hHost = new File(Cache.basePath, host);
	File hPath = new File(Cache.basePath, host + File.separatorChar + path);
	File hFile = new File(hPath, file);

	String out = null;

	if (!hFile.exists() || rewriteCache) {
	//if (!s.contains(file)) {
	    hPath.mkdirs();
	    int attempt = 0;
	    while (attempt < maxAttempts) {
                java.util.Date date= new java.util.Date();
                long curTS = date.getTime();
                logger.debug("Downloading URL (attempt " + attempt + "): " + url.getHost() + url.getFile());
                if (lastDownload + interval > curTS ) {
/*                    logger.debug("LastDownload: " + lastDownload);
                    logger.debug("CurTS: " + curTS);
                    logger.debug("Interval: " + interval);*/
                	logger.debug("Sleeping: " + (lastDownload + interval - curTS));
                    Thread.sleep(lastDownload + interval - curTS);
                }
                try {
			
					if (datatype.equals("xml"))
					{
						out = getURLContent(url.toString());
					}
					else out = getURLContent(url.toString());
					
					
					
					java.util.Date date2= new java.util.Date();
				    lastDownload = date2.getTime();
		            logger.debug("Downloaded URL (attempt " + attempt + ") in " + (lastDownload - curTS) + " ms : " + url.toString());
		            break;
				} catch (SocketTimeoutException ex) {
		            java.util.Date date3= new java.util.Date();
		            long failed = date3.getTime();
		            logger.info("Timeout (attempt " + attempt + ") in " + (failed - curTS)+ " : " + url.getHost() + url.getFile());
		            
		            //Comment to retry when timeout
		            if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
		            {
		                BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
		    		    fw.close();
		    		    return null;
		            }
			    
			    //END comment
		    
		            //Thread.sleep(interval);
				} catch (java.io.IOException ex) {
		            logger.info("Warning (retrying): " + ex.getMessage() + " " + url);
		            if (
		            	ex.getMessage() == null 
		            	|| ex.getMessage().equals("HTTP error fetching URL")
		            	|| ex.getMessage().equals("Connection reset")
		            	|| ex.getMessage().startsWith("Too many redirects occurred trying to load URL")
		            	|| ex.getMessage().startsWith("Unhandled content type.")
		            	|| ex.getMessage().startsWith("handshake alert:")
		            	|| ex.getMessage().equals(url.getHost())
		            	)
		            {
		    	    	//This makes sure that next run will see the errorneous page as cached. Does not have to be always desirable
		                if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
		            	{
		                	BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
		        		    fw.close();
		                	return null;
		            	}
		            }
		            else if (ex instanceof FileNotFoundException)
		            {
	                	 logger.info("File not found: " + ex.getMessage() + " " + url);
	                	 return null;
		            }
		            Thread.sleep(interval);
		        	}
				attempt++;
	    }
	    if (attempt == maxAttempts) {
			logger.warn("Warning. Max attempts reached. Skipping: " + url.getHost() + url.getPath());
			/*throw new SocketTimeoutException();*/
			return null;
	    }
	    try 
	    {
	    	BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
		    fw.append(out);
		    fw.close();
		    logger.debug(Integer.toString(++downloaded));
	    }
	    catch (Exception e)
	    {
			if (e.getClass() == InterruptedException.class)
			{
				throw e;
			}
			else logger.error("ERROR caching: " + e.getLocalizedMessage());
	    }
	    
	} else {
	    //logger.info("Using cache for URL: " + url.getHost() + url.getFile());
		out = IOUtils.toString(new FileInputStream(hFile), "UTF-8");
	}
	
	Document outdoc = null;
	if (datatype.equals("xml"))
	{
		if (validate(out, url.toString()))
		{
			logger.info("Valid XML: " + url.toString());
			validXML++;
			stats.addTriple(stats.createURI(url.toString()), stats.createURI(BPOprefix + "validXML"), stats.createLiteral("true", xsdPrefix + "boolean"));
		}
		else {
			logger.warn("Invalid XML: " + url.toString());
			invalidXML++;
			stats.addTriple(stats.createURI(url.toString()), stats.createURI(BPOprefix + "validXML"), stats.createLiteral("false", xsdPrefix + "boolean"));
		}
		outdoc = Jsoup.parse(new ByteArrayInputStream(out.getBytes()), "UTF-8", host, Parser.xmlParser());
		
	}
	else outdoc = Jsoup.parse(new ByteArrayInputStream(out.getBytes()), "UTF-8", host);	
	return outdoc;
    }

}
