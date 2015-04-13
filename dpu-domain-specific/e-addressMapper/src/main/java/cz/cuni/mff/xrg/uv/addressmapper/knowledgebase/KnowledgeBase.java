package cz.cuni.mff.xrg.uv.addressmapper.knowledgebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.utils.Utils;

/**
 * TODO: Add fault tolerance!
 *
 * @author Škoda Petr
 */
public class KnowledgeBase {

    private static final Logger LOG = LoggerFactory.getLogger(KnowledgeBase.class);

    /**
     * %s - grouping field
     */
    private static String UNIQUE_CSV_QUERY = "&rows=10&wt=csv&group=true&group.field=%s&group.main=true";

    private static String VUSC = "nazevvusc";

    private static String OBEC = "nazevobce";

    private static String ULICE = "nazevulice";

    private static String CASTOBCE = "nazevcastobce";

    private String solrEndpoint;

    private static final Map<String, String> okresToVusc;
    
    static {
        okresToVusc = new HashMap<>();
        // Ruian endpoint does not contains data about okres.
        okresToVusc.put("Žďár nad Sázavou", "Kraj Vysočina");
        okresToVusc.put("Jihlava", "Kraj Vysočina");
        okresToVusc.put("Třebíč", "Kraj Vysočina");
        okresToVusc.put("Havlíčkův Brod", "Kraj Vysočina");
        okresToVusc.put("Pelhřimov", "Kraj Vysočina");
        okresToVusc.put("Znojmo", "Jihomoravský kraj");
        okresToVusc.put("Brno-venkov", "Jihomoravský kraj");
        okresToVusc.put("Vyškov", "Jihomoravský kraj");
        okresToVusc.put("Břeclav", "Jihomoravský kraj");
        okresToVusc.put("Blansko", "Jihomoravský kraj");
        okresToVusc.put("Brno-město", "Jihomoravský kraj");
        okresToVusc.put("Hodonín", "Jihomoravský kraj");
        okresToVusc.put("Prostějov", "Olomoucký kraj");
        okresToVusc.put("Přerov", "Olomoucký kraj");
        okresToVusc.put("Šumperk", "Olomoucký kraj");
        okresToVusc.put("Olomouc", "Olomoucký kraj");
        okresToVusc.put("Jeseník", "Olomoucký kraj");
        okresToVusc.put("Opava", "Moravskoslezský kraj");
        okresToVusc.put("Ostrava-město", "Moravskoslezský kraj");
        okresToVusc.put("Bruntál", "Moravskoslezský kraj");
        okresToVusc.put("Nový Jičín", "Moravskoslezský kraj");
        okresToVusc.put("Frýdek-Místek", "Moravskoslezský kraj");
        okresToVusc.put("Karviná", "Moravskoslezský kraj");
        okresToVusc.put("Zlín", "Zlínský kraj");
        okresToVusc.put("Kroměříž", "Zlínský kraj");
        okresToVusc.put("Vsetín", "Zlínský kraj");
        okresToVusc.put("Uherské Hradiště", "Zlínský kraj");
        okresToVusc.put("Hlavní město Praha", "Hlavní město Praha");
        okresToVusc.put("Praha-východ", "Středočeský kraj");
        okresToVusc.put("Mladá Boleslav", "Středočeský kraj");
        okresToVusc.put("Rakovník", "Středočeský kraj");
        okresToVusc.put("Beroun", "Středočeský kraj");
        okresToVusc.put("Příbram", "Středočeský kraj");
        okresToVusc.put("Kolín", "Středočeský kraj");
        okresToVusc.put("Kutná Hora", "Středočeský kraj");
        okresToVusc.put("Nymburk", "Středočeský kraj");
        okresToVusc.put("Benešov", "Středočeský kraj");
        okresToVusc.put("Kladno", "Středočeský kraj");
        okresToVusc.put("Mělník", "Středočeský kraj");
        okresToVusc.put("Praha-západ", "Středočeský kraj");
        okresToVusc.put("Písek", "Jihočeský kraj");
        okresToVusc.put("Strakonice", "Jihočeský kraj");
        okresToVusc.put("Tábor", "Jihočeský kraj");
        okresToVusc.put("Český Krumlov", "Jihočeský kraj");
        okresToVusc.put("Prachatice", "Jihočeský kraj");
        okresToVusc.put("České Budějovice", "Jihočeský kraj");
        okresToVusc.put("Jindřichův Hradec", "Jihočeský kraj");
        okresToVusc.put("Plzeň-jih", "Plzeňský kraj");
        okresToVusc.put("Tachov", "Plzeňský kraj");
        okresToVusc.put("Klatovy", "Plzeňský kraj");
        okresToVusc.put("Rokycany", "Plzeňský kraj");
        okresToVusc.put("Domažlice", "Plzeňský kraj");
        okresToVusc.put("Plzeň-sever", "Plzeňský kraj");
        okresToVusc.put("Plzeň-město", "Plzeňský kraj");
        okresToVusc.put("Karlovy Vary", "Karlovarský kraj");
        okresToVusc.put("Cheb", "Karlovarský kraj");
        okresToVusc.put("Sokolov", "Karlovarský kraj");
        okresToVusc.put("Louny", "Ústecký kraj");
        okresToVusc.put("Ústí nad Labem", "Ústecký kraj");
        okresToVusc.put("Litoměřice", "Ústecký kraj");
        okresToVusc.put("Děčín", "Ústecký kraj");
        okresToVusc.put("Chomutov", "Ústecký kraj");
        okresToVusc.put("Most", "Ústecký kraj");
        okresToVusc.put("Teplice", "Ústecký kraj");
        okresToVusc.put("Liberec", "Liberecký kraj");
        okresToVusc.put("Jablonec nad Nisou", "Liberecký kraj");
        okresToVusc.put("Semily", "Liberecký kraj");
        okresToVusc.put("Česká Lípa", "Liberecký kraj");
        okresToVusc.put("Jičín", "Královéhradecký kraj");
        okresToVusc.put("Náchod", "Královéhradecký kraj");
        okresToVusc.put("Trutnov", "Královéhradecký kraj");
        okresToVusc.put("Hradec Králové", "Královéhradecký kraj");
        okresToVusc.put("Rychnov nad Kněžnou", "Královéhradecký kraj");
        okresToVusc.put("Pardubice", "Pardubický kraj");
        okresToVusc.put("Ústí nad Orlicí", "Pardubický kraj");
        okresToVusc.put("Chrudim", "Pardubický kraj");
        okresToVusc.put("Svitavy", "Pardubický kraj");
    }

    /**
     * Used for mocking.
     */
    protected KnowledgeBase() {
        // TODO Use with caution!
    }

    public KnowledgeBase(String solrEndpoint) {
        this.solrEndpoint = solrEndpoint;
    }

    public List<String> getOkres(String okres) throws KnowledgeBaseException {
        if (okres.contains(okres)) {
            return Arrays.asList(okres);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public List<String> getVusc(String vusc) throws KnowledgeBaseException {

        LOG.info("getVusc({})", vusc);

        final StringBuilder url = new StringBuilder();
        url.append(solrEndpoint);
        url.append("?q=");
        url.append(VUSC);
        url.append(":%22");
        try {
            url.append(URLEncoder.encode(vusc, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        url.append("%22&fl=");

        url.append(VUSC);
        url.append(String.format(UNIQUE_CSV_QUERY, VUSC));

        return Utils.filterExactMatch(executeQuery(url.toString()), vusc);
    }

    public List<String> getObec(String obec) throws KnowledgeBaseException {

        LOG.info("getObec({})", obec);

        final StringBuilder url = new StringBuilder();
        url.append(solrEndpoint);
        url.append("?q=");
        url.append(OBEC);
        url.append(":%22");
        try {
            url.append(URLEncoder.encode(obec, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        url.append("%22&fl=");

        url.append(OBEC);
        url.append(String.format(UNIQUE_CSV_QUERY, OBEC));

        return Utils.filterExactMatch(executeQuery(url.toString()), obec);
    }

    /**
     *
     * @param obec
     * @param okres Can be null
     * @return
     */
    public List<String> getObecInOkres(String obec, String okres) throws KnowledgeBaseException {
        // Translate to VUSC.
        final String vusc = okresToVusc.get(okres);
        return Utils.filterExactMatch(getObecInVusc(obec, vusc), obec);
    }

    /**
     *
     * @param obec
     * @param vusc Can be null
     * @return
     */
    public List<String> getObecInVusc(String obec, String vusc) throws KnowledgeBaseException {

        LOG.info("getObecInVusc({}, {})", obec, vusc);

        final StringBuilder url = new StringBuilder();
        url.append(solrEndpoint);
        url.append("?q=");
        url.append(OBEC);
        url.append(":%22");
        try {
            url.append(URLEncoder.encode(obec, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        url.append("%22&fl=");
        url.append(OBEC);
        url.append(String.format(UNIQUE_CSV_QUERY, OBEC));
        // Add restriction.
        if (vusc != null) {
            url.append("&fq=");
            url.append(VUSC);
            url.append(":%22");
            try {
                url.append(URLEncoder.encode(vusc, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            url.append("%22");
        }
        return Utils.filterExactMatch(executeQuery(url.toString()), obec);
    }

    /**
     *
     * @param ulice
     * @param obec Can be null
     * @return
     */
    public List<String> getUliceInObec(String ulice, String obec) throws KnowledgeBaseException {

        LOG.info("getUliceInObec({}, {})", ulice, obec);

        final StringBuilder url = new StringBuilder();
        url.append(solrEndpoint);
        url.append("?q=");
        url.append(ULICE);
        url.append(":%22");
        try {
            url.append(URLEncoder.encode(ulice, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        url.append("%22&fl=");
        url.append(ULICE);
        url.append(String.format(UNIQUE_CSV_QUERY, ULICE));
        // Add restriction.
        if (obec != null) {
            url.append("&fq=");
            url.append(OBEC);
            url.append(":%22");
            try {
                url.append(URLEncoder.encode(obec, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            url.append("%22");
        }
        return Utils.filterExactMatch(executeQuery(url.toString()), ulice);
    }

    /**
     *
     * @param castObce
     * @param obec Can be null
     * @return
     */
    public List<String> getCastObceInObec(String castObce, String obec) throws KnowledgeBaseException {

        LOG.info("getCastObceInObec({}, {})", castObce, obec);

        final StringBuilder url = new StringBuilder();
        url.append(solrEndpoint);
        url.append("?q=");
        url.append(CASTOBCE);
        url.append(":%22");
        try {
            url.append(URLEncoder.encode(castObce, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        url.append("%22&fl=");
        url.append(CASTOBCE);
        url.append(String.format(UNIQUE_CSV_QUERY, CASTOBCE));
        // Add restriction.
        if (obec != null) {
            url.append("&fq=");
            url.append(OBEC);
            url.append(":%22");
            try {
                url.append(URLEncoder.encode(obec, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            url.append("%22");
        }
        return Utils.filterExactMatch(executeQuery(url.toString()), castObce);
    }

    protected List<String> executeQuery(String urlAsString) throws KnowledgeBaseException {
        final URL url;
        try {
            url = new URL(urlAsString);
        } catch (MalformedURLException ex) {
            throw new KnowledgeBaseException("Invalid URI.", ex);
        }
//        LOG.info("url:\n{}", url.toString());
        final List<String> output = new ArrayList<>(10);
        try {
            output.clear();

            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            Integer responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                StringBuilder error = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    error.append("\n");
                    error.append(reader.readLine());                    
                }
                LOG.error("Response failure ({}):\n{}", responseCode, error);
                throw new KnowledgeBaseException("Recieved respnse code: " + responseCode.toString());
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                // Read header.
                reader.readLine();
                String inputLine;
                while((inputLine = reader.readLine()) != null) {
                    // Remove starting and ending '"'.
                    int start = 0;
                    int end = inputLine.length();
                    if (inputLine.startsWith("\"")) {
                        ++start;
                    }
                    if (inputLine.endsWith("\"")) {
                        --end;
                    }
                    output.add(inputLine.substring(start, end));
                }
            }
        } catch (IOException ex) {
            throw new KnowledgeBaseException("Can't read data from SOLR url: <" + urlAsString + ">", ex);
        }        
        return output;
    }

}
