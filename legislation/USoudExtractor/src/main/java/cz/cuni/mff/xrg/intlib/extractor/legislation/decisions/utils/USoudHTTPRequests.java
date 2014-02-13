/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.Extractor;
import static cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.Extractor.fromStream;
import static cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.Extractor.printProcessErrorOutput;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud.ExtractorConfig;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tomasknap
 */
public class USoudHTTPRequests {

    private File cacheRoot;
    private DPUContext context;
    private String jarContentPath;
    private ExtractorConfig config;

    public USoudHTTPRequests(DPUContext context, String jarContentPath, ExtractorConfig config) {

        this.cacheRoot = context.getGlobalDirectory();

        this.context = context;
        this.jarContentPath = jarContentPath;

        this.config = config;

    }
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            USoudHTTPRequests.class);
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
     * Returns list of file names which should be outputted by the extractor
     *
     * @return
     */
    public List<FileRecord> downloadData() {

        List<FileRecord> resultingFileNames = new ArrayList<FileRecord>();

        URL call = null;
        try {
            call = new URL("http://nalus.usoud.cz/Search/Search.aspx");
        } catch (MalformedURLException e) {
            final String message = "Malfolmed URL exception by construct extract URL. ";
        }

        String parameters = "";
        log.debug("\nFirst call to get a cookie");
        String cookie = getCookie(call, parameters, "GET");
        log.debug("\nUsing cookie " + cookie + " for the second call");


        //1 day
        //parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICgyNClkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyAr2ko7QXKmGm1UHQYiPJgZaXZgM%3D&__EVENTVALIDATION=%2FwEWjAEC67yBsgMCjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDp4PhtPBf48n1PD9%2Bm5M5MoCrCXs&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=1&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";

        //last 7 days
        //parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";
        parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";
        //parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=1000&ctl00%24MainContent%24resultsFontSize=10";
        log.debug("********************");
        log.debug("\nSecond call to get a response, but only getting info where the resp page is ");
        log.debug("********************");
        httpConnection(call, parameters, cookie, "POST");

        int numberFilesMatched = 0;
        int totalNumberFilesMatched = 0;
        int page = 0;
        do {

            numberFilesMatched = 0;
            page++;

            //adjust params for every call (iterate over pages)
            parameters = "__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTQzMzc2NDk3Mg9kFgJmD2QWAgIDD2QWCAIHDw8WAh4HRW5hYmxlZGhkZAIJDw8WAh4EVGV4dAUOTmFsZXplbsOpICg2MilkZAILDw8WBB8BBRRPZGxvxb5lbsOpIHbDvXNsZWRreR8AaGRkAg8PZBYCZg9kFiACAw8PFgIeB1Zpc2libGVoZGQCBQ8QDxYCHwJoZGQWAWZkAgcPZBYCAisPEA8WAh8AZ2QPFjxmAgECAgIDAgQCBQIGAgcCCAIJAgoCCwIMAg0CDgIPAhACEQISAhMCFAIVAhYCFwIYAhkCGgIbAhwCHQIeAh8CIAIhAiICIwIkAiUCJgInAigCKQIqAisCLAItAi4CLwIwAjECMgIzAjQCNQI2AjcCOAI5AjoCOxY8EAUBMQUBMWcQBQEyBQEyZxAFATMFATNnEAUBNAUBNGcQBQE1BQE1ZxAFATYFATZnEAUBNwUBN2cQBQE4BQE4ZxAFATkFATlnEAUCMTAFAjEwZxAFAjExBQIxMWcQBQIxMgUCMTJnEAUCMTMFAjEzZxAFAjE0BQIxNGcQBQIxNQUCMTVnEAUCMTYFAjE2ZxAFAjE3BQIxN2cQBQIxOAUCMThnEAUCMTkFAjE5ZxAFAjIwBQIyMGcQBQIyMQUCMjFnEAUCMjIFAjIyZxAFAjIzBQIyM2cQBQIyNAUCMjRnEAUCMjUFAjI1ZxAFAjI2BQIyNmcQBQIyNwUCMjdnEAUCMjgFAjI4ZxAFAjI5BQIyOWcQBQIzMAUCMzBnEAUCMzEFAjMxZxAFAjMyBQIzMmcQBQIzMwUCMzNnEAUCMzQFAjM0ZxAFAjM1BQIzNWcQBQIzNgUCMzZnEAUCMzcFAjM3ZxAFAjM4BQIzOGcQBQIzOQUCMzlnEAUCNDAFAjQwZxAFAjQxBQI0MWcQBQI0MgUCNDJnEAUCNDMFAjQzZxAFAjQ0BQI0NGcQBQI0NQUCNDVnEAUCNDYFAjQ2ZxAFAjQ3BQI0N2cQBQI0OAUCNDhnEAUCNDkFAjQ5ZxAFAjUwBQI1MGcQBQI1MQUCNTFnEAUCNTIFAjUyZxAFAjUzBQI1M2cQBQI1NAUCNTRnEAUCNTUFAjU1ZxAFAjU2BQI1NmcQBQI1NwUCNTdnEAUCNTgFAjU4ZxAFAjU5BQI1OWcQBQI2MAUCNjBnZGQCCw8WAh8CZ2QCFQ9kFhYCCw8PFgIfAWVkZAI1Dw8WAh8BZWRkAjsPDxYCHwFlZGQCQQ8PFgYeBVdpZHRoGwAAAAAAQGVAAQAAAB8BZR4EXyFTQgKAAmRkAkQPDxYEHwFlHwJoZGQCSA8PFgIfAWVkZAJPDw8WAh8BZWRkAlgPDxYCHwFlZGQCXg8PFgIfAWVkZAJkDw8WAh8BZWRkAmoPDxYCHwFlZGQCFw8WAh8CaGQCGQ8WAh8CaGQCGw8WAh8CaGQCHQ8PFgIeDU9uQ2xpZW50Q2xpY2sFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIfDw8WAh8FBSJpZiAoIWlucHV0SXNWYWxpZCgpKSByZXR1cm4gZmFsc2U7ZGQCIQ8PFgIfBQUiaWYgKCFpbnB1dElzVmFsaWQoKSkgcmV0dXJuIGZhbHNlO2RkAiMPDxYCHwUFImlmICghaW5wdXRJc1ZhbGlkKCkpIHJldHVybiBmYWxzZTtkZAIvDw8WAh8AaGRkAjEPDxYCHwBoZGQCMw8PFgIfAGhkZAI3DxBkDxYNZgIBAgICAwIEAgUCBwIIAgkCCgILAgwCDRYNEGRkZxBkZGcQZGRoEGRkZxBkZGcQZGRnEGRkZxBkZGcQZGRoEGRkaBBkZGgQZGRoEGRkZxYBZmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhgFGGN0bDAwJE1haW5Db250ZW50JG5hbGV6eQUaY3RsMDAkTWFpbkNvbnRlbnQkdXNuZXNlbmkFImN0bDAwJE1haW5Db250ZW50JHN0YW5vdmlza2FfcGxlbmEFIWN0bDAwJE1haW5Db250ZW50JGplbl9wdWJsaWtvdmFuYQUdY3RsMDAkTWFpbkNvbnRlbnQkcHJhdm5pX3ZldGEFGmN0bDAwJE1haW5Db250ZW50JGFic3RyYWt0BRhjdGwwMCRNYWluQ29udGVudCRuYXZldGkFF2N0bDAwJE1haW5Db250ZW50JHZ5cm9rBRxjdGwwMCRNYWluQ29udGVudCRvZHV2b2RuZW5pBS1jdGwwMCRNYWluQ29udGVudCRhcmd1bWVudGFjZV91c3Rhdm5paG9fc291ZHUFJGN0bDAwJE1haW5Db250ZW50JG9kbGlzbmVfc3Rhbm92aXNrbwUnY3RsMDAkTWFpbkNvbnRlbnQkZGxlX2RhdGFfenByaXN0dXBuZW5pBRZjdGwwMCRNYWluQ29udGVudCRpbmZvBTBjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbjUxJGltZ19idXRfQ2xlYXIFMGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uNTIkaW1nX2J1dF9DbGVhcgUwY3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b241MyRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTgkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjU1JGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXIxJGltZ19idXRfQ2xlYXIFNGN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI5JGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NCRpbWdfYnV0X0NsZWFyBTVjdGwwMCRNYWluQ29udGVudCRDaXNlbG5pa0J1dHRvbkNsZWFyNTYkaW1nX2J1dF9DbGVhcgU1Y3RsMDAkTWFpbkNvbnRlbnQkQ2lzZWxuaWtCdXR0b25DbGVhcjYwJGltZ19idXRfQ2xlYXIFNWN0bDAwJE1haW5Db250ZW50JENpc2VsbmlrQnV0dG9uQ2xlYXI1NyRpbWdfYnV0X0NsZWFyW22kuCDMd1YnFIpWrfUE3WDtC4w%3D&__EVENTVALIDATION=%2FwEWjAECi%2Bqlyg4CjpiQsAgC0dya6QgCwvqBvQcC%2FZvwmwECmojl4QwCo9PbkQkCtcr10g8Cn%2BfP%2BggCleuEugsC3Kq%2BJgLS%2F9euDgKn8dejAQL%2FyMTRBQK4sqC1DQKHweXMDQKNs5blAgLqlrmqAgLrlrmqAgLolrmqAgLplrmqAgLulrmqAgLvlrmqAgLslrmqAgL9lrmqAgLylrmqAgLqlvmpAgLqlvWpAgLqlvGpAgLqls2pAgLqlsmpAgLqlsWpAgLqlsGpAgLqlt2pAgLqlpmqAgLqlpWqAgLrlvmpAgLrlvWpAgLrlvGpAgLrls2pAgLrlsmpAgLrlsWpAgLrlsGpAgLrlt2pAgLrlpmqAgLrlpWqAgLolvmpAgLolvWpAgLolvGpAgLols2pAgLolsmpAgLolsWpAgLolsGpAgLolt2pAgLolpmqAgLolpWqAgLplvmpAgLplvWpAgLplvGpAgLpls2pAgLplsmpAgLplsWpAgLplsGpAgLplt2pAgLplpmqAgLplpWqAgLulvmpAgLulvWpAgLulvGpAgLuls2pAgLulsmpAgLulsWpAgLulsGpAgLult2pAgLulpmqAgLulpWqAgLvlvmpAgL9h%2FyEBQLo%2Ba7iDQKE%2BIv5BALOmvfJAgKA57nnCQLQ7ZuAAwLf%2FavNAgK%2BgIjJBgLI5vKTAwLFkvCLDQKWzL2ZDQKLkJ%2FaDQKZ3%2BzwCALR7ZfhDAKmiOqKDgLR7cOrBALRovG%2FAgL92on6DwLhwMmxCwLq9M4BAvKkq%2FcEAvDcyt8HArfE%2BvYNArnIIQLR26rJAQKZ2rKMAwLnm7zfDwKCqf6eAgLy9%2BXDBwKR6o%2BcBgKn%2B%2FLFBAKz7NCvAwKK%2FNTmDgL7v9SSBwKE2eLGBwLkwIvWDwLvkrjgCwKL54yXDALsg7aCBwKThpeHCgKqhv2pCwL4nvmMDQLJxafADALMh7ubBQLMh8%2F2DQLMh%2BPRBgLMh9fTCQLMh%2BuuAgKitsrmDQKhtsrmDQKntsrmDQKmtsrmDQKltsrmDQK0tsrmDQK7tsrmDQKitorlDQKauqWICgKbuqWICgKZuqWICgKNuqWICgLMufOwDgLMufuwDgLMucOwDvVWgOlyVKKwVeOLKIu6P5nR6lYm&ctl00%24MainContent%24nalezy=on&ctl00%24MainContent%24usneseni=on&ctl00%24MainContent%24stanoviska_plena=on&ctl00%24MainContent%24naveti=on&ctl00%24MainContent%24vyrok=on&ctl00%24MainContent%24oduvodneni=on&ctl00%24MainContent%24odlisne_stanovisko=on&ctl00%24MainContent%24dle_data_zpristupneni=on&ctl00%24MainContent%24zpristupneno_pred=7&ctl00%24MainContent%24text=&ctl00%24MainContent%24citace=&ctl00%24MainContent%24popularni_nazev=&ctl00%24MainContent%24typ_rizeni=&ctl00%24MainContent%24decidedFrom=&ctl00%24MainContent%24decidedTo=&ctl00%24MainContent%24publicationFrom=&ctl00%24MainContent%24publicationTo=&ctl00%24MainContent%24submissionFrom=&ctl00%24MainContent%24submissionTo=&ctl00%24MainContent%24soudce_zpravodaj=&ctl00%24MainContent%24soudce_stanovisko=&ctl00%24MainContent%24navrhovatel=&ctl00%24MainContent%24affected_organ_type=&ctl00%24MainContent%24affected_organ_spec=&ctl00%24MainContent%24actkind=&ctl00%24MainContent%24actkindnumber_txt=&ctl00%24MainContent%24actkindname_txt=&ctl00%24MainContent%24actkindclause_txt=&ctl00%24MainContent%24vyrok_multi=&ctl00%24MainContent%24vztah_k_predpisum=&ctl00%24MainContent%24predmet_rizeni=&ctl00%24MainContent%24klicove_slovo=&ctl00%24MainContent%24poznamka=&ctl00%24MainContent%24but_search=Vyhledat&ctl00%24MainContent%24razeni=2&ctl00%24MainContent%24resultsPageSize=20&ctl00%24MainContent%24resultsFontSize=10";
            log.debug("********************");
            log.debug("\nThird call to get the first list of the results");
            log.debug("********************");
            call = null;
            try {
                call = new URL("http://nalus.usoud.cz/Search/Results.aspx?page=" + String.valueOf(page - 1));
            } catch (MalformedURLException e) {
                final String message = "Malfolmed URL exception by construct extract URL. ";
            }
            String response = httpConnection(call, parameters, cookie, "GET");
            //log.info("RESULT: {}", response);
//            try {
//                DataUnitUtils.storeStringToTempFile(response, cacheRoot.getCanonicalPath().toString()+ File.separator + "testResp");
//            } catch (IOException ex) {
//                Logger.getLogger(USoudHTTPRequests.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
            //process the result - T
            String patternString1 = "(Word.aspx[^\"]*downloadDoc=1)";

            Pattern pattern = Pattern.compile(patternString1);
            Matcher matcher = pattern.matcher(response);


            while (matcher.find()) {
                log.info("Found matching link to decision: " + matcher.group(1));
                String matchedLink = matcher.group(1);

                //get id from the matched String
                String id = matchedLink.substring(matchedLink.indexOf("id"));
                id = id.substring(0, id.indexOf("&"));
                log.debug("Id: {}", id);

                //prepare the cache record
                String cacheRecordPath = null;
                try {
                    cacheRecordPath = cacheRoot.getCanonicalPath().toString() + File.separator + id;
                } catch (IOException ex) {
                    Logger.getLogger(USoudHTTPRequests.class.getName()).log(Level.SEVERE, null, ex);
                }
                log.debug("File to cache record: ", cacheRecordPath);
                File file = new File(cacheRecordPath);



                //test whether the cache already contains the record
//                if (file.exists()) {
//                    log.debug("File {} already in cache", cacheRecordPath);
//                }
//                else {
                //record not  exists in cache, it has to be obtained




                // Primo HTML:  "Karta.aspx?id=82220&searchnumber=1&tms=635273970628900000"


                //STEP 1: invoke another http request to get the card - metadata (WORD)
                // http://nalus.usoud.cz/Search/Karta.aspx?downloadDoc=1&id=82200
                log.debug("Not found in cache. Call to get metadatas");
                call = null;
                try {
                    String matchedLinkKarta = matchedLink.replaceAll("Word", "Karta");
                    matchedLinkKarta = matchedLinkKarta.substring(0, matchedLinkKarta.indexOf("&downloadDoc=1"));
                    call = new URL("http://nalus.usoud.cz/Search/" + matchedLinkKarta);
                } catch (MalformedURLException e) {
                    log.error("Malfolmed URL exception by construct extract URL. ");
                }

                String metaContent = httpConnection(call, parameters, cookie, "GET");

                int metaStart = metaContent.indexOf("<table width='95%' border='1' cellpadding='3' cellspacing='0' class='recordCardTable'>");
                int metaEnd = metaContent.indexOf("</table>");
                int fromBeg = 0;
                while (((metaEnd + fromBeg) < metaStart) && (metaEnd > -1)) {
                    metaContent = metaContent.substring(metaEnd + "</table>".length());
                    fromBeg += metaEnd + "</table>".length();
                    metaEnd = metaContent.indexOf("</table>");

                }
                if (metaEnd < 0) {
                    log.error("Strange structure of metadata for id {}, end of table not found", id);
                }
                String finalMetaContent = metaContent.substring(metaContent.indexOf("<table width='95%' border='1' cellpadding='3' cellspacing='0' class='recordCardTable'>"), metaContent.indexOf("</table>") + "</table>".length());
                //prepare metadata elem needed later during jTaggerAnot
                finalMetaContent = "<metadata>" + finalMetaContent + "</metadata>";

                //for debugging purpose
                DataUnitUtils.storeStringToTempFile(finalMetaContent, context.getWorkingDir() + File.separator + id + "-metadata", Charset.forName("UTF-8"));

//                
//                    //STEP 1: invoke another http request to get the card - metadata (WORD)
//                    // http://nalus.usoud.cz/Search/Karta.aspx?downloadDoc=1&id=82200
//                    log.debug("Not found in cache. Call to get metadatas");
//                    call = null;
//                    try {
//                         call = new URL("http://nalus.usoud.cz/Search/Karta.aspx?downloadDoc=1&" + id);
//                    } catch (MalformedURLException e) {
//                        log.error("Malfolmed URL exception by construct extract URL. ");
//                    }
//
//                    String fileContent = httpConnection(call, parameters, cookie, "GET"); 
//                    
////                    //store it for debugging purposes
////                    String tmpFileContentPath = context.getWorkingDir() + File.separator + id + "-metadata";
////                    DataUnitUtils.storeStringToTempFile(fileContent, tmpFileContentPath, Charset.forName("Cp1250"));
//                    
//                    //DataUnitUtils.storeStringToTempFile(fileContent, cacheRecordPath + "-metadata", Charset.forName("Cp1250"));
//                    //convert to TXT
//                    WordTxtConvertor convertor = new WordTxtConvertor(context, jarContentPath);
//                    String txtMetadataContent = convertor.convertToTxt(fileContent, id + "-metadata");
//                    if (txtMetadataContent == null) {
//                        //TODO
//                    }
//                    
//                    //STEP 1B: add spaces



                //STEP 2A: invoke another http request to get the file content (WORD)
                log.debug("Not found in cache. Call to get the data");
                call = null;
                try {
                    //call = new URL("http://nalus.usoud.cz/Search/Word.aspx?id=81253&searchnumber=2&tms=635196788449136250&downloadDoc=1");
                    call = new URL("http://nalus.usoud.cz/Search/" + matchedLink);
                } catch (MalformedURLException e) {
                    log.error("Malfolmed URL exception by construct extract URL. ");
                    continue;
                }

                String fileContent = httpConnection(call, parameters, cookie, "GET");

                //STEP 2B: convert to TXT
                WordTxtConvertor convertor = new WordTxtConvertor(context, jarContentPath);
                String txtFileContent = convertor.convertToTxt(fileContent, id + "-data");
                if (txtFileContent == null || txtFileContent.length() == 0) {
                    log.error("No result of the conversion of the file content. Skipping judgment with ID: {}. ", id);
                    continue;
                }

                //STEP 2C prepare expression URI from the file content (header)
                //prepare expression URI from the content of the file 
                //get the first couple of chars
                String sjTemp = txtFileContent.substring(0, txtFileContent.indexOf(" ze dne")).trim();
                String datumRozhodnutiTemp = txtFileContent.substring(txtFileContent.indexOf("ze dne ") + "ze dne ".length(), txtFileContent.indexOf("str."));
                String expressionURI = formatSpisovouZnackuForURI(sjTemp, datumRozhodnutiTemp, this.config);
                if (expressionURI == null) {
                    log.error("Expression URI cannot be built. Skipping judgment with ID {}", id);
                    continue;

                }

                //TODO STEP 2D strip everything before the line "Česká republika"
                String finalTxtFileContent = txtFileContent.substring(txtFileContent.indexOf("Česká republika"));
                //String finalTxtFileContent = finalTxtFileContentTemp.substring(txtFileContent.indexOf("ze dne"));



                //STEP 3: merge and store (added encoding at the end)
                DataUnitUtils.storeStringToTempFile(finalMetaContent + System.lineSeparator() + finalTxtFileContent, cacheRecordPath, Charset.forName("UTF-8"));

//                }

                numberFilesMatched++;
                resultingFileNames.add(new FileRecord(cacheRecordPath, expressionURI));

                //TODO temporal hack
                break;


            }

            //Karta zobrazeni: Karta.aspx?id=82200&searchnumber=1&tms=635271261748118750 
            //Karta stazeni: http://nalus.usoud.cz/Search/Karta.aspx?downloadDoc=1&id=82200



            log.info("Number of files matched for page {}: {}", page, numberFilesMatched);
            totalNumberFilesMatched += numberFilesMatched;

            //end when no further files extracted on the given page
        } while (numberFilesMatched > 0);

        log.info("Total number of files matched: {}", totalNumberFilesMatched);
        log.info("Result size: {}", resultingFileNames.size());

        return resultingFileNames;

    }

    //Returns response of the HTTP query
    private static String httpConnection(URL call, String parameters, String cookie, String method) {
        String result = null;

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


            httpConnection.setRequestProperty("Cookie", cookie);

            httpConnection.setRequestProperty("Connection", "keep-alive");
            httpConnection.setRequestProperty("Cache-Control", "max-age=0");


            //httpConnection.setDoInput(true); default
            if ("POST".equals(method)) {
                httpConnection.setDoOutput(true); //needed for POST
            } else {
                httpConnection.setDoOutput(false);
            }
            httpConnection.setInstanceFollowRedirects(false);
            //            System.out.println("headers for request : ");
            //            Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
            //            for (String s : headerFields.keySet()) {
            //                System.out.println(s + " : " + headerFields.get(s));
            //            }
//            Map<String, List<String>> requestProperties = httpConnection.getRequestProperties();
//            for (String s : requestProperties.keySet()) {
//                System.out.println(s + " : " + requestProperties.get(s));
//            }

            //System.out.println(httpConnection.getRequestProperties());
            if ("POST".equals(method)) { //write params if it is POST
                try (OutputStream os = httpConnection.getOutputStream()) {
                    os.write(parameters.getBytes());
                    os.flush();
                }
            }


            //RESPONSE

            int httpResponseCode = httpConnection.getResponseCode();
            log.debug("Response code : " + httpResponseCode);
            log.debug(httpConnection.getResponseMessage());

            //System.out.println("headers of the response : ");

//            Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
//            for (String s : headerFields.keySet()) {
//                log.debug.println(s + " : " + headerFields.get(s));
//            }

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
                log.debug("Response Code: {}", String.valueOf(httpResponseCode));
                log.error(message.toString());

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
                //System.out.println(thisLine);
                sb.append(thisLine);
            } // end while 

            result = sb.toString();

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


        return result;
    }

    private static String getCookie(URL call, String parameters, String method) {

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


            httpConnection.setRequestProperty("Connection", "keep-alive");
            httpConnection.setRequestProperty("Cache-Control", "max-age=0");


            //httpConnection.setDoInput(true); default
            if ("POST".equals(method)) {
                httpConnection.setDoOutput(true); //needed for POST
            } else {
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


            System.out.println("\nSet-Cookie header: ");
            List<String> cookies = headerFields.get("Set-Cookie");
            resultingCookie = cookies.get(0);
            System.out.println(resultingCookie);

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

                log.error(message.toString());
                return null;

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


        //    System.out.println("\nOutput message not interesting, just getting cookie");


        return resultingCookie;
    }

//    private static String httpConnection(URL call, String parameters, boolean useCookie, String cookie, String method, String storeToFile) {
//        String resultingCookie = null;
//        
//        HttpURLConnection httpConnection = null;
//        try {
//            httpConnection = (HttpURLConnection) call.openConnection();
//            httpConnection.setRequestMethod(method);
//            httpConnection.setRequestProperty("Content-Type",
//                    "application/x-www-form-urlencoded");
//            
//            if ("POST".equals(method)) {
//                httpConnection.setRequestProperty("Content-Length", ""
//                    + Integer.toString(parameters.getBytes().length));
//            }
//            
//            if (useCookie) {
//                httpConnection.setRequestProperty("Cookie", cookie);
//            }
//            httpConnection.setRequestProperty("Connection", "keep-alive");
//            httpConnection.setRequestProperty("Cache-Control", "max-age=0");
//      
//            
//            //httpConnection.setDoInput(true); default
//            if ("POST".equals(method)) {
//                httpConnection.setDoOutput(true); //needed for POST
//            }             
//            else { 
//                httpConnection.setDoOutput(false);
//            }
//            httpConnection.setInstanceFollowRedirects(false);
//            //            System.out.println("headers for request : ");
//            //            Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
//            //            for (String s : headerFields.keySet()) {
//            //                System.out.println(s + " : " + headerFields.get(s));
//            //            }
//            Map<String, List<String>> requestProperties = httpConnection.getRequestProperties();
//            for (String s : requestProperties.keySet()) {
//                System.out.println(s + " : " + requestProperties.get(s));
//            }
//            
//            //System.out.println(httpConnection.getRequestProperties());
//            if ("POST".equals(method)) { //write params if it is POST
//                try (OutputStream os = httpConnection.getOutputStream()) {
//                    os.write(parameters.getBytes());
//                    os.flush();
//                }
//            }
//
//
//            //RESPONSE
//
//            int httpResponseCode = httpConnection.getResponseCode();
//            System.out.println("Response code : " + httpResponseCode);
//            System.out.println(httpConnection.getResponseMessage());
//
//            System.out.println("headers of the response : ");
//            
//            Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
//            for (String s : headerFields.keySet()) {
//                System.out.println(s + " : " + headerFields.get(s));
//            }
//
//            if (!useCookie) {
//                System.out.println("\nSet-Cookie header: ");
//                List<String> cookies = headerFields.get("Set-Cookie");
//                resultingCookie = cookies.get(0);
//                System.out.println(resultingCookie);
//            }
//
//
//            if (httpResponseCode != HTTP_OK_RESPONSE) {
//
//                StringBuilder message = new StringBuilder(
//                        httpConnection.getHeaderField(0));
//
//
//                if (httpResponseCode == HTTP_UNAUTORIZED_RESPONSE) {
//                    message.append(
//                            ". Your USERNAME and PASSWORD for connection is wrong.");
//                } else if (httpResponseCode == HTTP_BAD_RESPONSE) {
//                    message.append(
//                            ". Inserted data has wrong format.");
//
//                } else {
//                }
//
//
////				throw new InsertPartException(
////						message.toString() + "\n\n" + "URL endpoint: " + endpointURL
////						.toString() + " POST content: " + parameters);
//                //throw new RDFException(message.toString());
//            }
//
//        } catch (UnknownHostException e) {
//            final String message = "Unknown host: ";
//        } catch (IOException e) {
//            final String message = "Endpoint URL stream cannot be opened. ";
//            if (httpConnection != null) {
//                httpConnection.disconnect();
//            }
//        }
//
//        if (useCookie){
//        System.out.println("\n\nReading from the connection... ");
//        try {
//            InputStreamReader inputStreamReader = new InputStreamReader(
//                    httpConnection.getInputStream(), Charset.forName(
//                    encode));
//
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(
//                    httpConnection.getInputStream(), Charset.forName(
//                    encode)));
//            String thisLine;
//            StringBuffer sb = new StringBuffer();
//            while ((thisLine = br.readLine()) != null) { // while loop begins here
//                System.out.println(thisLine);
//                sb.append(thisLine);
//            } // end while 
//
//              if (storeToFile != null) {
//                    storeStringToTempFile(sb.toString(), storeToFile, StandardCharsets.UTF_8);
//              }
//            
////            try (Scanner scanner = new Scanner(inputStreamReader)) {
////
////
////
////                while (scanner.hasNext()) {
////                    String line = scanner.next();
////                    System.out.println(line);
////
////
////                }
////            }
//
//        } catch (IOException e) {
//
//            final String message = "Http connection cannot open the stream. ";
//
//
//        }
//        }
//        else {
//            System.out.println("\nOutput message not interesting, just getting cookie");
//        }
//        
//        return resultingCookie;
//    }
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

    /**
     * Input is the spisova znacka take from the file content
     *
     * @param sjTemp
     * @return URI for expression
     * (http://linked.opendata.cz/resource/legislation/cz/decision/2013/7-Tdo-681-2013/expression/cz/decision/2013/7-Tdo-681-2013/cs)
     */
    private String formatSpisovouZnackuForURI(String sjTemp, String datumRozhodnutiTemp, ExtractorConfig config) {

        String prefix = "http://linked.opendata.cz/resource/legislation/cz/decision/";
        String decisionID = sjTemp.trim().replaceAll(" ", "-").replaceAll("\\.", "-").replaceAll("/", "-");
        //last four digits
        String year = datumRozhodnutiTemp.substring(datumRozhodnutiTemp.length() - 4);
        if (year.length() != 4) {
            log.warn("Problem getting year for the expression URI, year is {}. Skipping this file", year);
            return null;
        }
        String result = prefix + year + "/" + decisionID + "/expression/cz/decision/" + year + "/" + decisionID + "/cs";
        return result;





//       //TODO adjust fileName, so that expression may be constructed
//            String fileName = filePath; 
//            
//            String subject = config.getDecisionPrefix() + fileName;
//            String newSubjectPrefix = subject.substring(0, subject.lastIndexOf("/"));
//            String decisionID = subject.substring(subject.lastIndexOf("-") + 1, subject.lastIndexOf(".txt")).replaceAll("_", "-");
//            //String decisionID = prepare
//          
//            String year = decisionID.substring(decisionID.lastIndexOf("-") + 1);
//            String newSubject = newSubjectPrefix + "/" + year + "/" + decisionID + "/expression";
//            
//            //adjust the subject based on the latest adjustment:\
//            // add /cz/decision/2010/22-Cdo-4430-2010/cs
//            newSubject = newSubject + "/cz/decision/" + year + "/" + decisionID + "/cs";


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
            log.error(e.getLocalizedMessage());
            log.debug(
                    "Strange response code. First char of response code set to 0");
            return 0;
        }
    }
}
