package cz.cuni.mff.xrg.scraper.css_parser.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

/**
 *  Document cache. It stores downloaded files to hard drive.
 * 
 *  @author Jakub Klímek
 */
public class Cache {

    private static String getURLContentAsString(String p_sURL) throws IOException
    {
        URL oURL;
        String sResponse = null;

        oURL = new URL(p_sURL);
//        oURL.openConnection();
        try    {
            sResponse = IOUtils.toString(oURL, "UTF-8");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return sResponse;
    }
    
    private static byte[] getURLContentAsByteArray(String p_sURL) throws IOException
    {
        URL oURL;
        byte[] sResponse = null;

        oURL = new URL(p_sURL);
//        oURL.openConnection();
           
        sResponse = IOUtils.toByteArray(oURL);

        return sResponse;
    }
    
    private static void download_and_gunzip(String fromURL, File toFile) throws IOException
    {
        GZIPInputStream gis = null;
        OutputStream fos = null;
        try {
            URL oURL = new URL(fromURL);
            fos = new FileOutputStream(toFile);
            InputStream is = oURL.openStream();
            gis = new GZIPInputStream(is);
            
            IOUtils.copy(gis, fos);
        } finally {
            if (gis != null) {
                gis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    private static String getURLContentAsUnGzippedString(String p_sURL) throws IOException
    {
        byte[] bytes = getURLContentAsByteArray(p_sURL);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(bytes)), out);
        
        return new String(out.toByteArray(), "UTF-8");        
    }

    public static int errorsFetchingURL = 0;

    public static String basePath;

    public static Logger logger;
    
    //public static boolean rewriteCache;

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

    public static boolean isCached(URL url) throws IOException, InterruptedException {   
        String host = url.getHost();
        if (url.getPath().lastIndexOf("/") == -1) {
            return false;
        }

        String path;
        String file;
        if (url.getPath().lastIndexOf("/") == 0)
        {
            path = url.getPath().substring(1).replace("?", "_");
            file = url.getFile().substring(1).replace("/", "@").replace("?", "@");
            if (file.isEmpty()) return false;
        }
        else
        {
            path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
            file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
            if (file.isEmpty()) return false;
        }

        File hPath = new File(Cache.basePath, host + File.separatorChar + path);
        File hFile = new File(hPath, file);

        return (hFile.exists() && (hFile.length() > 0));
    }

    public static String getDocument(URL url, int maxAttempts, String datatype) throws IOException, InterruptedException {   
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
            path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
            file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
            if (file.isEmpty()) return null;
        }

        if (datatype.equals("gz")) file = file.substring(0, file.length() - 3);

        File hPath = new File(Cache.basePath, host + File.separatorChar + path);
        File hFile = new File(hPath, file);

        String out = null;

        if (!hFile.exists() /*|| rewriteCache*/) {
            hPath.mkdirs();
            int attempt = 0;
            while (attempt < maxAttempts) {
                java.util.Date date= new java.util.Date();
                long curTS = date.getTime();
                logger.debug("Downloading URL (attempt " + attempt + "): " + url.getHost() + url.getFile());
                if (lastDownload + interval > curTS ) {
                    logger.debug("Sleeping: " + (lastDownload + interval - curTS));
                    Thread.sleep(lastDownload + interval - curTS);
                }
                try {
                    if (datatype.equals("gz"))
                    {
                        download_and_gunzip(url.toString(), hFile);
                        //out = getURLContentAsUnGzippedString(url.toString());
                        //FileInputStream fisTargetFile = new FileInputStream(hFile);

                        //Pass path instead of content
                        out = hFile.getAbsolutePath();//IOUtils.toString(fisTargetFile, "UTF-8");
                    
                        //fisTargetFile.close();                        
                    }
                    else 
                    {
                        out = getURLContentAsString(url.toString());
                    }

                    java.util.Date date2= new java.util.Date();
                    lastDownload = date2.getTime();
                    logger.debug("Downloaded URL (attempt " + attempt + ") in " + (lastDownload - curTS) + " ms : " + url.getHost() + url.getFile());
                    break;
                
                }
                catch (SocketTimeoutException ex) {
                    java.util.Date date3= new java.util.Date();
                    long failed = date3.getTime();
                    logger.debug("Timeout (attempt " + attempt + ") in " + (failed - curTS)+ " : " + url.getHost() + url.getFile());

                } catch (java.io.IOException ex) {
                    logger.warn("Warning (retrying): " + ex.getMessage());
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
                        if (ex.getMessage().equals("HTTP error fetching URL")) errorsFetchingURL++;
                    }
                    Thread.sleep(interval);
                }
                attempt ++;
            }
            if (attempt == maxAttempts) {
                logger.warn("Warning. Max attempts reached. Skipping: " + url.getHost() + url.getPath());
                return null;
            }
            try 
            {
                if (!datatype.equals("gz")) {
                    BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
                    fw.append(out);
                    fw.close();
                }
            }
            catch (Exception e)
            {
                if (e.getClass() == InterruptedException.class)
                {
                    throw e;
                }
                else logger.warn("ERROR caching: " + e.getLocalizedMessage());
            }
        } else {
            if (datatype.equals("gz"))
            {
                out = hFile.getAbsolutePath();
            }
            else {    
                FileInputStream fisTargetFile = new FileInputStream(hFile);

                out = IOUtils.toString(fisTargetFile, "UTF-8");
            
                fisTargetFile.close();
            }
        }
        return out;
    }

}
