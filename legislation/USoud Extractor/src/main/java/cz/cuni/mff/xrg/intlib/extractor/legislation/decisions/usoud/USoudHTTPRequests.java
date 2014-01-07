/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tomasknap
 */
public class USoudHTTPRequests {

    /**
     * Default used encoding.
     */
    protected static final String encode = "UTF-8";
    /**
     * Represent successfully connection using HTTP.
     */
    protected static final int HTTP_OK_RESPONSE = 200;
    /**
     * Represent http error code needed authorisation for connection using HTTP.
     */
    protected static final int HTTP_UNAUTORIZED_RESPONSE = 401;
    /**
     * Represent http error code returns when inserting data in bad format.
     */
    protected static final int HTTP_BAD_RESPONSE = 400;

    private HttpURLConnection getHttpURLConnection(URL call) throws IOException {

        int retryCount = 0;

        HttpURLConnection httpConnection;

        while (true) {
            try {
                httpConnection = (HttpURLConnection) call.openConnection();
                return httpConnection;
            } catch (IOException e) {
            }
        }
    }

    /**
     *
     *
     *
     *
     * /
     *
     **
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        URL call = null;
        try {
            call = new URL("http://nalus.usoud.cz/Search/Search.aspx");
        } catch (MalformedURLException e) {
            final String message = "Malfolmed URL exception by construct extract URL. ";
        }

        String parameters = "";
        System.out.println("\nFirst call");
        String cookie = httpConnection(call, parameters,false, null, "GET", null);
        System.out.println("\nUsing cookie " + cookie + " for the second call");
        
        
        //1 day
        //parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICgyNClkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyAr2ko7QXKmGm1UHQYiPJgZaXZgM%3D&__EVENTVALIDATION=%2FwEWjAEC67yBsgMCjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDp4PhtPBf48n1PD9%2Bm5M5MoCrCXs&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=1&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";

        //last 7 days
        //parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";
        parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";
        //parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=1000&ctl00%24MainContent%24resultsFontSize=10";
        System.out.println("\nSecond call");
        httpConnection(call, parameters, true, cookie, "POST", null);
        
           System.out.println("\nThird call");
            call = null;
        try {
            call = new URL("http://nalus.usoud.cz/Search/Results.aspx?page=0");
        } catch (MalformedURLException e) {
            final String message = "Malfolmed URL exception by construct extract URL. ";
        }
        httpConnection(call, parameters, true, cookie, "GET", null);
        
        
        
           System.out.println("\nFourth testing call to get one of the resulting documents");
            call = null;
        try {
            call = new URL("http://nalus.usoud.cz/Search/Word.aspx?id=81253&searchnumber=2&tms=635196788449136250&downloadDoc=1");
        } catch (MalformedURLException e) {
            final String message = "Malfolmed URL exception by construct extract URL. ";
        }
        //httpConnection(call, parameters, true, cookie, "GET", "/tmp/test.doc");
        
        
        



    }

    private static String httpConnection(URL call, String parameters, boolean useCookie, String cookie, String method, String storeToFile) {
        String resultingCookie = null;
        
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) call.openConnection();
            httpConnection.setRequestMethod(method);
            httpConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            
            if ("POST".equals(method)) {
                httpConnection.setRequestProperty("Content-Length", ""
                    + Integer.toString(parameters.getBytes().length));
            }
            
            if (useCookie) {
                httpConnection.setRequestProperty("Cookie", cookie);
            }
            httpConnection.setRequestProperty("Connection", "keep-alive");
            httpConnection.setRequestProperty("Cache-Control", "max-age=0");
      
            
            //httpConnection.setDoInput(true); default
            if ("POST".equals(method)) {
                httpConnection.setDoOutput(true); //needed for POST
            }             
            else { 
                httpConnection.setDoOutput(false);
            }
            httpConnection.setInstanceFollowRedirects(false);
            //            System.out.println("headers for request : ");
            //            Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
            //            for (String s : headerFields.keySet()) {
            //                System.out.println(s + " : " + headerFields.get(s));
            //            }
            Map<String, List<String>> requestProperties = httpConnection.getRequestProperties();
            for (String s : requestProperties.keySet()) {
                System.out.println(s + " : " + requestProperties.get(s));
            }
            
            //System.out.println(httpConnection.getRequestProperties());
            if ("POST".equals(method)) { //write params if it is POST
                try (OutputStream os = httpConnection.getOutputStream()) {
                    os.write(parameters.getBytes());
                    os.flush();
                }
            }


            //RESPONSE

            int httpResponseCode = httpConnection.getResponseCode();
            System.out.println("Response code : " + httpResponseCode);
            System.out.println(httpConnection.getResponseMessage());

            System.out.println("headers of the response : ");
            
            Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
            for (String s : headerFields.keySet()) {
                System.out.println(s + " : " + headerFields.get(s));
            }

            if (!useCookie) {
                System.out.println("\nSet-Cookie header: ");
                List<String> cookies = headerFields.get("Set-Cookie");
                resultingCookie = cookies.get(0);
                System.out.println(resultingCookie);
            }


            if (httpResponseCode != HTTP_OK_RESPONSE) {

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


//				throw new InsertPartException(
//						message.toString() + "\n\n" + "URL endpoint: " + endpointURL
//						.toString() + " POST content: " + parameters);
                //throw new RDFException(message.toString());
            }

        } catch (UnknownHostException e) {
            final String message = "Unknown host: ";
        } catch (IOException e) {
            final String message = "Endpoint URL stream cannot be opened. ";
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        if (useCookie){
        System.out.println("\n\nReading from the connection... ");
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(
                    httpConnection.getInputStream(), Charset.forName(
                    encode));


            BufferedReader br = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream(), Charset.forName(
                    encode)));
            String thisLine;
            StringBuffer sb = new StringBuffer();
            while ((thisLine = br.readLine()) != null) { // while loop begins here
                System.out.println(thisLine);
                sb.append(thisLine);
            } // end while 

              if (storeToFile != null) {
                    storeStringToTempFile(sb.toString(), storeToFile, StandardCharsets.UTF_8);
              }
            
//            try (Scanner scanner = new Scanner(inputStreamReader)) {
//
//
//
//                while (scanner.hasNext()) {
//                    String line = scanner.next();
//                    System.out.println(line);
//
//
//                }
//            }

        } catch (IOException e) {

            final String message = "Http connection cannot open the stream. ";


        }
        }
        else {
            System.out.println("\nOutput message not interesting, just getting cookie");
        }
        
        return resultingCookie;
    }
    
     public static File storeStringToTempFile(String s, String filePath, Charset charset) {

        if (s == null || s.isEmpty()) {
            //log.warn("Nothing to be stored to a file");
            return null;
        }

        if (filePath == null || filePath.isEmpty()) {
            //log.error("File name is missing");
            return null;
        }

        //log.debug("File content is: {}", s);

        //prepare temp file where the a is stored
        File configFile = new File(filePath);

        if (configFile == null) {
            //log.error("Created file is null or empty, although the original string was non-empty .");
            return null;
        }

        
        try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath(), charset)) {
            writer.write(s, 0, s.length());
        } catch (IOException x) {
            //log.error("IOException: %s%n", x);
        }

        return configFile;


    }
    
}
