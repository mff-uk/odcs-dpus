package cz.cuni.mff.css_parser.utils;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.openrdf.model.ValueFactory;
/**
 *  Document cache. It stores downloaded files to hard drive.
 * 
 *  @author Jakub Klímek
 */

public class Cache {
    
    public static Logger logger;
    
    public static SimpleRdfWrite stats;
    
    private static String BPOprefix = "http://linked.opendata.cz/ontology/domain/buyer-profiles/";
    
    private static String xsdPrefix = "http://www.w3.org/2001/XMLSchema#";
    
    public static Validator validator;
    
    public static boolean validate = false; 

    public static int validXML = 0;
    public static int invalidXML = 0;
    public static long timeValidating = 0;
    
    
    private static boolean validate(String file, String url) throws OperationFailedException
    {
        java.util.Date date = new java.util.Date();
        long start = date.getTime();
        ValueFactory valueFactory = stats.getValueFactory();
        try {
            
            logger.debug("Loading file for validation: " + url);
            Source xmlSource = new StreamSource( new StringReader(file));
            logger.debug("XSD Validation starts: " + url);
            validator.validate(xmlSource);

            java.util.Date date2 = new java.util.Date();
            long end2 = date2.getTime();
            timeValidating += (end2-start);
            logger.debug("Valid XML (" + (end2-start) + " ms): " + url);
            
            stats.add(valueFactory.createURI(url.toString()), 
                    valueFactory.createURI(BPOprefix + "validationTime"), 
                    valueFactory.createLiteral(Long.toString(end2-start), 
                    valueFactory.createURI(xsdPrefix + "integer")));

            return true;
        } catch (SAXException e) {
            // instance document is invalid!
            java.util.Date date3 = new java.util.Date();
            long end3 = date3.getTime();
            timeValidating += (end3-start);
            
            logger.debug("Invalid XML (" + (end3-start) + " ms): " + url);
            stats.add(valueFactory.createURI(url.toString()), 
                    valueFactory.createURI(BPOprefix + "invalidMessage"), 
                    valueFactory.createLiteral(e.getLocalizedMessage()));
            stats.add(valueFactory.createURI(url.toString()), 
                    valueFactory.createURI(BPOprefix + "validationTime"), 
                    valueFactory.createLiteral(Long.toString(end3-start), 
                    valueFactory.createURI(xsdPrefix + "integer")));
            return false;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
    }
    
    private static String getURLContent(String p_sURL) throws IOException
    {
        URL oURL;
        String sResponse = null;

        logger.debug("Getting " + p_sURL);
        
        oURL = new URL(p_sURL);
        oURL.openConnection();
        logger.debug("Calling toString " + p_sURL);
        try {
        	sResponse = IOUtils.toString(oURL, "UTF-8");
        }
        catch (Exception e)
        {
        	logger.error("Exception in toString: " + e.getLocalizedMessage(), e);
        	throw e;
        }
        logger.debug("Got " + p_sURL);

        return sResponse;
    }

    public static void setTrustAllCerts() throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) {    }
                public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) {    }
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
            logger.error("SSL connection failure", e);
        }
    }    

    private static int downloaded = 0;

    //private static String basePath = "D:\\scraper\\";
    private static String basePath = "./cache/";
    
    public static boolean rewriteCache = false;

    private static HashSet<String> s = new HashSet<>();

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
    private static int timeout = 30000;
    
    public static Document getDocument(URL url, int maxAttempts, String datatype) throws IOException, InterruptedException, OperationFailedException {   
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
            
                    //out = getURLContent(url.toString());
                	logger.debug("Opening connection to: " + url);

                	URLConnection conn = url.openConnection();
                	conn.setConnectTimeout(timeout);
                	conn.setReadTimeout(timeout);
                	
                	logger.debug("Getting stream");
                	InputStream is = conn.getInputStream();
                    
                	logger.debug("Got stream");

                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    logger.debug("Got reader");
 
                    StringBuilder builder = new StringBuilder();
                    logger.debug("Got StringBuilder");

                    String line;
                    int linenum = 0;
                    while ( (line = br.readLine()) != null) { 
                        if (linenum % 10 == 0 ) logger.debug("Got line: " + ++linenum);
                    	builder.append(line);
                    }
                     
                    logger.debug("Closing reader");

                    br.close();

                    logger.debug("Closing stream");
                    is.close();
                    
                    out = builder.toString();
                    logger.debug("String created");

                    java.util.Date date2= new java.util.Date();
                    lastDownload = date2.getTime();
                    logger.debug("Downloaded URL (attempt " + attempt + ") in " + (lastDownload - curTS) + " ms : " + url.toString());
                    break;
                } catch (SocketTimeoutException ex) {
                    java.util.Date date3= new java.util.Date();
                    long failed = date3.getTime();
                    logger.info("Timeout (attempt " + attempt + ") in " + (failed - curTS)+ " : " + url.getHost() + url.getFile());
                    
                    //Comment to retry when timeout
                    out = "";
                    if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
                    {
                    	FileUtils.write(hFile, out, "UTF-8");
                        break;
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
                        out = "";
                        if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
                        {
                        	FileUtils.write(hFile, out, "UTF-8");
                            break;
                        }
                    }
                    else if (ex instanceof FileNotFoundException)
                    {
                         logger.info("File not found: " + ex.getMessage() + " " + url);
                        if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
                        {
                        	FileUtils.write(hFile, out, "UTF-8");
                        }
                        out = "";
                        break;
                    }
                    Thread.sleep(interval);
                    }
                attempt++;
        }
        if (attempt == maxAttempts) {
            logger.warn("Warning. Max attempts reached. Skipping: " + url.getHost() + url.getPath());
            if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
            {
            	FileUtils.write(hFile, out, "UTF-8");
            }
            out = "";
        }
        try 
        {
        	FileUtils.write(hFile, out, "UTF-8");
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
        out = FileUtils.readFileToString(hFile, "UTF-8");
    }
    
    ValueFactory valueFactory = stats.getValueFactory();
    if (out.length() == 0) {
        logger.debug("Not working: " + url.toString());
        stats.add(valueFactory.createURI(url.toString()), 
                valueFactory.createURI(BPOprefix + "notWorking"), 
                valueFactory.createLiteral("true", valueFactory.createURI(xsdPrefix + "boolean")));
        return null;
    }
    else {
        Document outdoc = null;
        if (datatype.equals("xml"))
        {
            if (validate) {
                if (validate(out, url.toString()))
                {
                    //logger.info("Valid XML: " + url.toString());
                    validXML++;
                    stats.add(valueFactory.createURI(url.toString()), 
                            valueFactory.createURI(BPOprefix + "validXML"), 
                            valueFactory.createLiteral("true", valueFactory.createURI(xsdPrefix + "boolean")));
                }
                else {
                    //logger.warn("Invalid XML: " + url.toString());
                    invalidXML++;
                    stats.add(valueFactory.createURI(url.toString()), 
                            valueFactory.createURI(BPOprefix + "validXML"), 
                            valueFactory.createLiteral("false", valueFactory.createURI(xsdPrefix + "boolean")));
                }
            }
            outdoc = Jsoup.parse(new ByteArrayInputStream(out.getBytes("UTF-8")), "UTF-8", host, Parser.xmlParser());            
        }
        else outdoc = Jsoup.parse(new ByteArrayInputStream(out.getBytes("UTF-8")), "UTF-8", host);    
        
        return outdoc;
    }
  }

}
