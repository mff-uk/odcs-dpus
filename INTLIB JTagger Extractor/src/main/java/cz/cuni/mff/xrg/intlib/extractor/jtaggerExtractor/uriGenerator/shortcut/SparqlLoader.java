package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut;

import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.IntLibLink;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jakub Starka
 */
public class SparqlLoader {
    
     public static File tempDir; 

     private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SparqlLoader.class);
    
    
    public static File getCacheDir() {
        //String dirName = System.getProperty("java.io.tmpdir");
        File cacheDir = new File(tempDir, "intLib");
        cacheDir.mkdirs();
        
        return cacheDir;
    }
    
    public static File getFile(int fileType) {
        
//        File file;
//        if (context != null) {
//            file = new File(getCacheDir(context.getWorkingDir()), settings.get(fileType).get(FILENAME));
//        } else {
//            file = new File(getCacheDir(), settings.get(fileType).get(FILENAME));
//        }
        
        File file = new File(getCacheDir(), settings.get(fileType).get(FILENAME));
        if (file.exists() && file.isFile() && file.length() != 0) {
            return file;
        } else {
            return new File(getCacheDir(), settings.get(fileType).get(FILENAME));
        }
        
    }
    
    public static void renewCache(int interval) throws UnsupportedEncodingException {
        Calendar cal = Calendar.getInstance();
        
        for (HashMap<Integer, String> item: settings.values()) {
            File file = new File(getCacheDir(), item.get(FILENAME));
            Long lastModified = file.lastModified();
            Long now = cal.getTimeInMillis();

            if (lastModified + interval * 1000 < now) {
                sparqlQuery(
                        item.get(SPARQL_ENDPOINT), 
                        "default-graph-uri=" + URLEncoder.encode(item.get(GRAPH_URI), "UTF-8") +
                            "&query=" + URLEncoder.encode(item.get(QUERY), "UTF-8") +
                            "&format=" + URLEncoder.encode("csv", "UTF-8"),
                        item.get(FILENAME));
            }
        }
    }
    
    public static void sparqlQuery(String targetURL, String urlParameters, String cacheName) {
        
        URL url;
        HttpURLConnection connection = null;  
        try {
        //Create connection
        url = new URL(targetURL);
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", 
            "application/x-www-form-urlencoded");

        connection.setRequestProperty("Content-Length", "" + 
                Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");  

        connection.setUseCaches (false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
        wr.writeBytes (urlParameters);
        wr.flush ();
        wr.close ();

        //Get Response	
        
        if (connection.getResponseCode() != 200) {
            return;
        }
        
        File tempFile = new File(getCacheDir(), cacheName + ".tmp");
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer(); 
        while((line = rd.readLine()) != null) {
            bw.write(line);
            bw.write("\n");
            response.append(line);
            response.append('\r');
        }
        bw.close();
        rd.close();
        
        File cacheFile = new File(getCacheDir(), cacheName);
        tempFile.renameTo(cacheFile);

        } catch (Exception e) {

        e.printStackTrace();

        } finally {

        if(connection != null) {
            connection.disconnect(); 
        }
        }
        
    }
    
    private static HashMap<Integer, HashMap<Integer, String> > settings;
    
    public static final int EXPRESSION_LIST = 0;
    public static final int ACT_LIST = 1;
    public static final int COURT_LIST = 2;
    
    public static final int SPARQL_ENDPOINT = 0;
    public static final int GRAPH_URI = 1;
    public static final int QUERY = 2;
    public static final int FILENAME = 3;
    
    static {
        
        String[][]  load = {
            {
                "http://linked.opendata.cz/sparql",
                "http://linked.opendata.cz/resource/dataset/psp.cz/2013-03-31/enriched",
                "PREFIX lex: <http://purl.org/lex#> \n" + 
                    "PREFIX frbr: <http://purl.org/vocab/frbr/core#> \n" +
                    "PREFIX dcterms:  <http://purl.org/dc/terms/> \n" +
                    "prefix my:<my_> \n\n" +

                    "select distinct ?s " +
                    "from <http://linked.opendata.cz/resource/dataset/psp.cz/2013-03-31/enriched> " +
                    "where {" + 
                    "?s a frbr:Expression } ",
                "expressionList.csv"
            },
            {
                "http://linked.opendata.cz/sparql",
                "http://linked.opendata.cz/resource/dataset/psp.cz/2013-03-31/enriched",
                "prefix lex: <http://purl.org/lex#>\n\n" +
                    "select distinct ?act, ?title,?number where {?act a lex:Act; dcterms:title ?title; dcterms:identifier ?number}",
                "actList.csv"

            },
            {
                "http://linked.opendata.cz/sparql",
                "http://linked.opendata.cz/resource/dataset/court/cz",
                "SELECT DISTINCT ?s WHERE {?s a ?Court } LIMIT 100",
                "courtList.csv"
            }
        };
        
        settings = new HashMap<>();
        
        for (int i = 0; i < 3; i ++) {
            HashMap<Integer, String> data = new HashMap<Integer , String>();
            data.put(SPARQL_ENDPOINT, load[i][0]);
            data.put(GRAPH_URI, load[i][1]);
            data.put(QUERY, load[i][2]);
            data.put(FILENAME, load[i][3]);
            settings.put(i, data);
        }
        
    }
    
    
}
