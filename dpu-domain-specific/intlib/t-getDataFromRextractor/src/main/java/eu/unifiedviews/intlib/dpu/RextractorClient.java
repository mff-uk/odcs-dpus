/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.intlib.dpu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tomasknap
 */
public class RextractorClient {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RextractorClient.class);

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

    RextractorClient(String targetRextractorServer) {
        this.targetRextractorServer = targetRextractorServer;
    }

    private File storeFiles(String entry, String rootDirPath, String suffix, String queryBase) {

        //query = "http://odcs.xrg.cz/prod-rextractor/?command=export-document&doc_id=pr0182-1993_0404-2012";

        entry = "pr" + entry;
        String query = queryBase + entry;
        log.debug("Processing query: {} for entry: " + queryBase, entry);

        //get the file content
        File newFile = new File(rootDirPath + File.separator + entry + suffix);
        callService(query, newFile);

        log.debug("New file has path: {}", rootDirPath + File.separator + entry + suffix);

        //            PrintWriter out = null;
        //            try {
        //                out = new PrintWriter(newFile);
        //            } catch (FileNotFoundException ex) {
        //                log.error(ex.getLocalizedMessage());
        //            }
        //            out.println(resFileContent);
        //            log.debug("Written to: " + rootDirPath + File.separator + entry + suffix);
        return newFile;

    }

    public List<File> prepareFiles(String rootDirPath, String from, String to) {

        List<File> resultingFiles = new ArrayList<>();

        //        String query = "http://odcs.xrg.cz/prod-rextractor/?command=export-document&doc_id=pr0182-1993_0404-2012";
        //        String query = "http://odcs.xrg.cz/prod-rextractor/?command=list-export&fromDate=" + from + "&toDate=" + to;

        String query = targetRextractorServer + "/?command=list-export&fromDate=" + from + "&toDate=" + to;

        String response = callService(query, null);
        //log.debug("List of documents: " + response);

        //pars res, get each input from res.
        if (response == null) {
            log.info("No file to be processed");
            return resultingFiles;
        }
        String[] entries = response.split("pr");

        //store documents (HTML) - documents
        for (String entry : entries) {

            if (entry.length() == 0)
                continue;

            File newFile = storeFiles(entry, rootDirPath, ".html", targetRextractorServer + "/?command=export-document&doc_id=");
            resultingFiles.add(newFile);
        }

        //store documents (XML) - descriptions
        for (String entry : entries) {

            if (entry.length() == 0)
                continue;

            File newFile = storeFiles(entry, rootDirPath, ".xml", targetRextractorServer + "/?command=export-description&doc_id=");
            resultingFiles.add(newFile);
        }

        return resultingFiles;

    }

    //Returns response of the HTTP query
    public String callService(String query, File outputFile) {
        String result = null;
        String method = "GET";

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
            httpConnection.setInstanceFollowRedirects(false);

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

            if (outputFile != null) {
                //it is a second query - store to file, no string returned
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                String thisLine;
                //            StringBuilder sb = new StringBuilder();
                while ((thisLine = br.readLine()) != null) { // while loop begins here
                    //                sb.append(thisLine);
                    writer.write(thisLine);
                    writer.newLine();
                } // end while 

                writer.flush();
                writer.close();

                //           result = sb.toString();

            }
            else {
                //it is first query, can be parse into string
                String thisLine;
                StringBuilder sb = new StringBuilder();
                while ((thisLine = br.readLine()) != null) { // while loop begins here
                    sb.append(thisLine);
                } // end while 

                result = sb.toString();
            }

            //            try (Scanner scanner = new Scanner(inputStreamReader)) {
            //
            //
            //
            //                while (scanner.hasNext()) {
            //                    String line = scanner.next();
            //                    log.debug(line);
            //
            //
            //                }
            //            }

        } catch (IOException e) {

            log.error(e.getLocalizedMessage());

        }

        //        log.debug("Result :" + result);
        return result;
    }

    /**
     * Returns the first digit of the http response code.
     * 
     * @param httpResponseCode
     *            number of HTTP response code
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
