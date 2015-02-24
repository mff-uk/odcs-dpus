/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.intlib.senddatatorextractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomasknap
 */
public class RextractorClientSender {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RextractorClientSender.class);
       
     /**
     * Default used encoding.
     */
    protected static final String encode = "UTF-8";
     /**
     * Represents prefix of the OK response code (could be 200, but also 204,
     * etc)
     */
    private static final int HTTP_OK_RESPONSE_PREFIX = 2;
    private static final int HTTP_MOVED_RESPONSE_PREFIX = 3;
    /**
     * Represent http error code needed authorisation for connection using HTTP.
     */
    protected static final int HTTP_UNAUTORIZED_RESPONSE = 401;
    /**
     * Represent http error code returns when inserting data in bad format.
     */
    protected static final int HTTP_BAD_RESPONSE = 400;

    private String targetRextractorServer;
    
    RextractorClientSender(String targetRextractorServer) {
        this.targetRextractorServer = targetRextractorServer;
    }
    
//    private File storeFiles(String entry, String rootDirPath, String suffix, String queryBase) {
//        
//        
//          //query = "http://odcs.xrg.cz/prod-rextractor/?command=export-document&doc_id=pr0182-1993_0404-2012";
//   
//            entry = "pr" + entry; 
//            String query = queryBase + entry;
//            log.debug("Processing query: {} for entry: " + queryBase, entry);
//            
//            //get the file content
//            File newFile = new File(rootDirPath + File.separator + entry + suffix);
//            callService(query, newFile);
//            
//            log.debug("New file has path: {}", rootDirPath + File.separator + entry + suffix);
//            
////            PrintWriter out = null;
////            try {
////                out = new PrintWriter(newFile);
////            } catch (FileNotFoundException ex) {
////                log.error(ex.getLocalizedMessage());
////            }
////            out.println(resFileContent);
////            log.debug("Written to: " + rootDirPath + File.separator + entry + suffix);
//            return newFile;
//             
//        
//        
//    }
    
     public int sendFile(String symbolicName, String filePath) {
         
        //        String query = "http://odcs.xrg.cz/prod-rextractor/?command=export-document&doc_id=pr0182-1993_0404-2012";
        String query = targetRextractorServer + "/?command=document-submit";
//         String query = "http://odcs.xrg.cz/devel-rextractor/?command=document-submit";
        
        //prepareID of the submitted document from symbolic name (in the form of "prXX"
        log.debug("Symbolic name is: {}", symbolicName);
        
        String documentID = createNewSymbolicName(symbolicName);
        if (documentID == null) {
            log.error("Problem processing input with symbolic name {} - cannot construct document ID for rextractor", symbolicName);
             return 1; 
        }
        
        log.debug("Document ID is: {}", documentID);
        
        return callService(documentID, filePath, query);
        //log.debug("List of documents: " + response);
         
        
     }
    
     
      
     private String createNewSymbolicName(String sn) {
        
          //input: Export_HTML\predpisy\1992\0357\pr0357-1992_0420-2003.xml
          //desired output: pr0357-1992_0420-2003 
    

        Pattern pattern = Pattern.compile("pr[0-9]+[^.]*");
        // in case you would like to ignore case sensitivity,
        // you could use this statement:
        // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sn);
        // check all occurance
        while (matcher.find()) {
            //there is only one match
             String temp = sn.substring(matcher.start(), matcher.end());
             return temp;
        }
         
         
        return null;
        
    }
     
        
      //Returns 0 if everything ok, 1 in case of problems
    public int callService(String documentID, String filePath, String query) {
        String result = null;
        String method = "POST";

            
        
        URL call = null;
        try {
            call = new URL(query);
        } catch (MalformedURLException e) {
            final String message = "Malfolmed URL exception by construct extract URL. ";
        }
            
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) call.openConnection();
            httpConnection.setRequestMethod(method);

            httpConnection.setRequestProperty("Connection", "keep-alive");
            httpConnection.setRequestProperty("Cache-Control", "max-age=0");


            //httpConnection.setDoInput(true); default
            if ("POST".equals(method)) {
                httpConnection.setDoOutput(true); //needed for POST
            } else {
                httpConnection.setDoOutput(false);
            }
            httpConnection.setInstanceFollowRedirects(true);

            //prepare data to be POSTED:
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("doc_id", documentID);
            
            //load data (doc) to be submitted:
//                object = new String(Files.readAllBytes(decodedPath), StandardCharsets.UTF_8);
            String fileContent = Utils.readFile(filePath, StandardCharsets.UTF_8);          
            params.put("doc_content", fileContent);
           
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        
            //end of data preparation
            
            
            if ("POST".equals(method)) { //write params if it is POST
                try (OutputStream os = httpConnection.getOutputStream()) {
                    os.write(postDataBytes);
                    os.flush();
                }
            }
            
            //RESPONSE

            int httpResponseCode = httpConnection.getResponseCode();
            log.debug("Response code : " + httpResponseCode);
             log.debug(httpConnection.getResponseMessage());

            int firstResponseNumber = getFirstResponseNumber(
                    httpResponseCode);

            if (firstResponseNumber != HTTP_OK_RESPONSE_PREFIX && firstResponseNumber != HTTP_MOVED_RESPONSE_PREFIX) {

                StringBuilder message = new StringBuilder(
                        httpConnection.getHeaderField(0));


                if (httpResponseCode == HTTP_UNAUTORIZED_RESPONSE) {
                    message.append(
                            ". Your USERNAME and PASSWORD for connection is wrong.");
                } else if (httpResponseCode == HTTP_BAD_RESPONSE) {
                    message.append(
                            ". Inserted data has wrong format.");

                } else {
                }
                log.debug("Response Code: " + String.valueOf(httpResponseCode));
//                log.debug(message.toString());

//				throw new InsertPartException(
//						message.toString() + "\n\n" + "URL endpoint: " + endpointURL
//						.toString() + " POST content: " + parameters);
                //throw new RDFException(message.toString());
            }

        } catch (UnknownHostException e) {
            log.error(e.getLocalizedMessage());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }


        log.debug("\n\nReading from the connection... ");
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(
                    httpConnection.getInputStream(), Charset.forName(
                    encode));


            BufferedReader br = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream(), Charset.forName(
                    encode)));
            
            
                //it is first query, can be parse into string
                 String thisLine;
                StringBuilder sb = new StringBuilder();
                while ((thisLine = br.readLine()) != null) { // while loop begins here
                    sb.append(thisLine);
                } // end while 

                result = sb.toString();
                
                if (result.contains("ERROR")) {
                    log.error("Failed: {}", result);
                    return 1;
                }
                else {
                    log.debug("Result: {}", result);
                    return 0;
                }
            
            
        } catch (IOException e) {

            log.error(e.getLocalizedMessage());


        }

//        log.debug("Result :" + result);
        return 0;
    }
    
    
     /**
     * Returns the first digit of the http response code.
     *
     * @param httpResponseCode number of HTTP response code
     * @return The first digit of the http response code.
     */
    private static int getFirstResponseNumber(int httpResponseCode) {

        try {
            int firstNumberResponseCode = Integer.valueOf((String.valueOf(
                    httpResponseCode)).substring(0, 1));

            return firstNumberResponseCode;

        } catch (NumberFormatException e) {
                     
            return 0;
        }
    }

        
    
}

