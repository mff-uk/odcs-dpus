package cz.opendata.linked.geocoder.nominatim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
import cz.cuni.mff.xrg.odcs.rdf.help.MyTupleQueryResultIf;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	private Logger logger = LoggerFactory.getLogger(DPU.class);
	private int geocoded = 0;
	private int cacheHits = 0;
    private int count = 0;
    private int failed = 0;
	
	@InputDataUnit(name = "Schema.org addresses")
	public RDFDataUnit sAddresses;

	@OutputDataUnit(name = "Geocoordinates")
	public RDFDataUnit outGeo;

    public Extractor() {
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}
	
	public void execute(DPUContext ctx) throws DPUException
	{
		java.util.Date date = new java.util.Date();
		long start = date.getTime();

        logger.debug("Geocoder init");

        int total = countAddressesInSourceDataUnit();
        ctx.sendMessage(MessageType.INFO, "Found " + total + " addresses");

        Graph graph;
        try {
            graph = queryAddresses();
        } catch (InvalidQueryException e) {
            logger.error(e.getLocalizedMessage());
            ctx.sendMessage(MessageType.ERROR, "Failed to query for addresses, ending");
            return;
        }

        Iterator<Statement> it = graph.match(null, RDF.TYPE, sAddresses.createURI("http://schema.org/PostalAddress"));

        int allowedRequests = getNumberOfAllowedRequests(ctx);

        logger.debug("Starting geocoding, will issue " + allowedRequests + " requests");

        long lastDownload = 0;
        while (it.hasNext() && !ctx.canceled() && geocoded <= allowedRequests)
        {
            count++;
            Resource currentAddressURI = it.next().getSubject();

            Address address = Address.buildFromRdf(config, graph, currentAddressURI);

            logger.debug("Address to geocode (" + count + "/" + total + "): " + address.toString());

            File hFile = getCacheFile(ctx, address.toFilename());

            if (!hFile.exists())
            {
                lastDownload = requestAndCache(date, lastDownload, address, hFile);
            }
            else {
                cacheHits++;
                logger.debug("From cache (" + cacheHits + "): " + address);
            }

            try {
                QueryResult result = readDataFromCache(address, hFile);
                if (result == null) {
                    failed++;
                    logger.debug("Failed to geolocate (" + failed + "): " + address);
                } else {
                    logger.debug("Located: " + address + " Possibilities: " + result.getLength() + " Latitude: " + result.getLatitude() + " Longitude: " + result.getLongitude());
                    appendResultToDataUnit(result, currentAddressURI);
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        if (ctx.canceled()) {
            logger.info("Cancelled");
        }

       	logger.info("Geocoding done.");

		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		ctx.sendMessage(MessageType.INFO, "Geocoded " + count + ": Nominatim: "+ geocoded +" From cache: " + cacheHits + " in " + (end-start) + "ms, failed attempts: " + failed);
	}

    private void appendResultToDataUnit(QueryResult result, Resource currentAddressURI) {
        URI dcsource = outGeo.createURI("http://purl.org/dc/terms/source");
        URI nominatimURI = outGeo.createURI("http://nominatim.openstreetmap.org");
        URI geoURI = outGeo.createURI("http://schema.org/geo");
        URI geocoordsURI = outGeo.createURI("http://schema.org/GeoCoordinates");
        URI longURI = outGeo.createURI("http://schema.org/longitude");
        URI latURI = outGeo.createURI("http://schema.org/latitude");
        URI urlURI = outGeo.createURI("http://schema.org/url");

        String uri = currentAddressURI.stringValue();
        URI addressURI = outGeo.createURI(uri);
        URI coordURI = outGeo.createURI(uri+"/geocoordinates/nominatim");

        outGeo.addTriple(addressURI, geoURI , coordURI);
        outGeo.addTriple(coordURI, RDF.TYPE, geocoordsURI);
        outGeo.addTriple(coordURI, latURI, outGeo.createLiteral(result.getLatitude().toString()));
        outGeo.addTriple(coordURI, longURI, outGeo.createLiteral(result.getLongitude().toString()));
        outGeo.addTriple(coordURI, dcsource, nominatimURI);

        if (config.isGenerateMapUrl()) {
            URI webURI = outGeo.createURI("http://www.openstreetmap.org/search?query=#map=13/" + result.getLatitude().toString() + "/" + result.getLongitude().toString());
            outGeo.addTriple(coordURI, urlURI, webURI);
        }
    }

    private QueryResult readDataFromCache(Address address, File hFile) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Geo.class, new GeoInstanceCreator()).create();
        String cachedFile = FileUtils.readFileToString(hFile);

        Geo[] gs = gson.fromJson(cachedFile, Geo[].class);
        if (gs == null || gs.length == 0) {
            return null;
        } else {
            return new QueryResult(new BigDecimal(gs[0].lat), new BigDecimal(gs[0].lon), gs.length);
        }
    }

    private File getCacheFile(DPUContext ctx, String file) {
        File hPath = ctx.getGlobalDirectory();
        return new File(hPath, file);
    }

    private long requestAndCache(Date date, long lastDownload, Address address, File hFile) {
        long curTS = date.getTime();
        if (lastDownload + config.getInterval() > curTS)
        {
            logger.debug("Sleeping: " + (lastDownload + config.getInterval() - curTS));
            try {
                Thread.sleep(lastDownload + config.getInterval() - curTS);
            } catch (InterruptedException e) {
                logger.info("Interrupted while sleeping");
            }
        }

        String url = null;
        try {
            url = address.buildQuery();
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while building request " + e.getMessage());
            e.printStackTrace();
        }

        String out = null;
        try {
            out = getURLContent(url.toString());
        } catch (IOException e1) {
            logger.error(e1.getLocalizedMessage() +" while geocoding " +  address);
        }
        lastDownload = date.getTime();

        geocoded++;
        logger.debug("Queried Nominatim (" + geocoded + "): " + address + " as " + url);
        try {
            BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
            fw.append(out);
            fw.close();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        //CACHED
        return lastDownload;
    }

    private static String getURLContent(String p_sURL) throws IOException
    {
        URL oURL;
        String sResponse = null;

        oURL = new URL(p_sURL);
        try	{
            sResponse = IOUtils.toString(oURL, "UTF-8");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return sResponse;
    }

    private int countAddressesInSourceDataUnit() {
        String query = "PREFIX s: <http://schema.org/> "
                + "SELECT (COUNT (*) as ?count) "
                + "WHERE "
                + "{"
                    + "?address a s:PostalAddress . "
                +  "}";

        int total = 0;
        try {
            MyTupleQueryResultIf countres = sAddresses.executeSelectQueryAsTuples(query);
            total = Integer.parseInt(countres.next().getValue("count").stringValue());
        } catch (InvalidQueryException e1) {
            logger.error(e1.getLocalizedMessage());
        } catch (NumberFormatException e) {
            logger.error(e.getLocalizedMessage());
        } catch (QueryEvaluationException e) {
            logger.error(e.getLocalizedMessage());
        }
        return total;
    }

    private Graph queryAddresses() throws InvalidQueryException {
        String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
                + "CONSTRUCT {?address ?p ?o}"
                + "WHERE "
                + "{"
                + "?address a s:PostalAddress ;"
                + "			?p ?o . "
                +  "}";

        logger.debug("Executing Schema.org query: " + sOrgConstructQuery);
        return sAddresses.executeConstructQuery(sOrgConstructQuery);
    }

    private int getNumberOfAllowedRequests(DPUContext ctx) {
        int cached = countCachedRequestsInPeriod(ctx);
        return (config.getLimit() - cached);
    }

    private int countCachedRequestsInPeriod(DPUContext ctx)
    {
        int count = 0;

        // Directory path here
        File currentFile;
        File folder = ctx.getGlobalDirectory();
        if (!folder.isDirectory()) return 0;

        File[] listOfFiles = folder.listFiles();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        for (int i = 0; i < listOfFiles.length; i++)
        {
            if (listOfFiles[i].isFile())
            {
                currentFile = listOfFiles[i];

                Date now = new Date();
                Date modified = null;
                try {
                    modified = sdf.parse(sdf.format(currentFile.lastModified()));
                } catch (ParseException e) {
                    logger.error(e.getLocalizedMessage());
                }
                long diff = (now.getTime() - modified.getTime()) / 1000;

                if (diff < (config.getLimitPeriod() * 60 * 60)) count++;
            }
        }
        logger.info("Total of " + count + " positions cached in last " + config.getLimitPeriod() + " hours.");
        return count;
    }

    @Override
	public void cleanUp() {	}



    public class Response {
        public Map<String, Geo> descriptor;
        //getters&setters
        public Response () {  }
    }

    public class Geo {
        public String lat;
        public String lon;
        //getters&setters
        public Geo () {  }
    }

    public class GeoInstanceCreator implements InstanceCreator<Geo> {
        public Geo createInstance(Type type) {
            return new Geo();
        }
    }

}
