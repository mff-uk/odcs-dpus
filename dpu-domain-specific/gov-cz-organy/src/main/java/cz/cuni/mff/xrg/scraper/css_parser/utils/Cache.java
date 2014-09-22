package cz.cuni.mff.xrg.scraper.css_parser.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
/**
 *  Document cache. It stores downloaded files to hard drive.
 * 
 *  @author Jakub Starka
 */
public class Cache {

    private static String getURLContent(String p_sURL) throws IOException
    {
        URL oURL;
        String sResponse = null;

        oURL = new URL(p_sURL);
        oURL.openConnection();
        try    {
            sResponse = IOUtils.toString(oURL, "UTF-8");
        }
        catch (Exception e)
        {
            logger.error(e.getLocalizedMessage(), e);
        }

        return sResponse;
    }
    
    public static int errorsFetchingURL = 0;

    public static String basePath;

    public static Logger logger;
    
    public static boolean rewriteCache;

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
            //System.out.println();
            //System.out.println(url);
            //System.out.println(url.getPath());
            path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
            //System.out.println(path);
            file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
            //System.out.println(url.getFile());
            //System.out.println(file);
            //System.out.println();
            if (file.isEmpty()) return false;
        }

        //File hHost = new File(Cache.basePath, host);
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
            //System.out.println();
            //System.out.println(url);
            //System.out.println(url.getPath());
            path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
            //System.out.println(path);
            file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
            //System.out.println(url.getFile());
            //System.out.println(file);
            //System.out.println();
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
                    /*                    System.out.println("LastDownload: " + lastDownload);
                    System.out.println("CurTS: " + curTS);
                    System.out.println("Interval: " + interval);*/
                    logger.debug("Sleeping: " + (lastDownload + interval - curTS));
                    Thread.sleep(lastDownload + interval - curTS);
                }
                try {
                    out = getURLContent(url.toString());

                java.util.Date date2= new java.util.Date();
                lastDownload = date2.getTime();
                logger.debug("Downloaded URL (attempt " + attempt + ") in " + (lastDownload - curTS) + " ms : " + url.getHost() + url.getFile());
                break;
                
                }
                catch (SocketTimeoutException ex) {
                    java.util.Date date3= new java.util.Date();
                    long failed = date3.getTime();
                    logger.debug("Timeout (attempt " + attempt + ") in " + (failed - curTS)+ " : " + url.getHost() + url.getFile());

                    //Comment to retry when timeout
                    //if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
                    //{
                    //    BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
                    //   fw.close();
                    //    return null;
                    //}

                    //END comment

                    //Thread.sleep(interval);
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
                        //This makes sure that next run will see the errorneous page as cached. Does not have to be always desirable
                        //BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
                        //fw.close();
                        //return null;
                    }
                    Thread.sleep(interval);
                }
                attempt ++;
            }
            if (attempt == maxAttempts) {
                logger.warn("Warning. Max attempts reached. Skipping: " + url.getHost() + url.getPath());
                /*throw new SocketTimeoutException();*/
                return null;
            }
            try 
            {
            	FileUtils.write(hFile, out, "UTF-8");
            }
            catch (Exception e)
            {
                if (e.getClass() == InterruptedException.class)
                {
                    throw e;
                }
                else logger.warn("ERROR caching: " + e.getLocalizedMessage(), e);
            }
        } else {
            //System.out.println("Using cache for URL: " + url.getHost() + url.getFile());

            FileInputStream fisTargetFile = new FileInputStream(hFile);

            out = IOUtils.toString(fisTargetFile, "UTF-8");
            
            fisTargetFile.close();

        }
        return out;
    }

}
