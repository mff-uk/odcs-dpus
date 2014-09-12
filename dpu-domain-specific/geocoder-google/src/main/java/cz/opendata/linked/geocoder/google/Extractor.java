package cz.opendata.linked.geocoder.google;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderStatus;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.*;

import org.apache.commons.io.*;
import org.openrdf.model.*;
import org.openrdf.query.TupleQueryResult;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);
    
    final Geocoder geocoder = new Geocoder();
    private int geocodes = 0;
    private int cacheHits = 0;
    
    @DataUnit.AsInput(name = "Saddresses")
    public RDFDataUnit sAddresses;

    @DataUnit.AsOutput(name = "Geocoordinates")
    public WritableRDFDataUnit outGeo;    
    
    public Extractor() {
        super(ExtractorConfig.class,AddonInitializer.noAddons());
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new ExtractorDialog();
    }

    private int countTodaysCacheFiles(DPUContext context)
    {
        int count = 0;

        // Directory path here
        File currentFile;
        File folder = context.getGlobalDirectory();
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
                    LOG.error(e.getLocalizedMessage());
                }
                long diff = (now.getTime() - modified.getTime()) / 1000;
                //System.out.println("Date modified: " + sdf.format(currentFile.lastModified()) + " which is " + diff + " seconds ago.");

                if (diff < (config.getHoursToCheck() * 60 * 60)) count++;
            }
        }
        LOG.info("Total of " + count + " positions cached in last " + config.getHoursToCheck() + " hours. " + (config.getLimit() - count) + " remaining.");
        return count;
    }
    
    @Override
    protected void innerExecute() throws DPUException, DataUnitException
    {
        java.util.Date date = new java.util.Date();
        long start = date.getTime();
        
        final SimpleRdfWrite geoValueFacWrap = SimpleRdfFactory.create(outGeo, context);        
        final ValueFactory geoValueFac = geoValueFacWrap.getValueFactory();
        
        final SimpleRdfRead addrValueFacWrap = SimpleRdfFactory.create(sAddresses, context);
        final ValueFactory addrValueFac = addrValueFacWrap.getValueFactory();

        String countQuery = "PREFIX s: <http://schema.org/> "
                + "SELECT (COUNT (*) as ?count) "
                + "WHERE "
                + "{"
                    + "?address a s:PostalAddress . "
                +  "}"; 
        /*String notGCcountQuery = "PREFIX s: <http://schema.org/> "
                + "SELECT (COUNT (*) as ?count) "
                + "WHERE "
                + "{"
                    + "?address a s:PostalAddress . "
                    + "FILTER NOT EXISTS {?address s:geo ?geo}"
                +  "}";*/ 
        String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
                + "CONSTRUCT {?address ?p ?o}"
                + "WHERE "
                + "{"
                    + "?address a s:PostalAddress ;"
                    + "            ?p ?o . "
//                    + "FILTER NOT EXISTS {?address s:geo ?geo}"
                +  "}"; 
        /*String sOrgQuery = "PREFIX s: <http://schema.org/> "
                + "SELECT DISTINCT * "
                + "WHERE "
                + "{"
                    + "{?address a s:PostalAddress . } "
                    + "UNION { ?address s:streetAddress ?street . } "
                    + "UNION { ?address s:addressRegion ?region . } "
                    + "UNION { ?address s:addressLocality ?locality . } "
                    + "UNION { ?address s:postalCode ?postal . } "
                    + "UNION { ?address s:addressCountry ?country . } "
                + " }";*/
        LOG.debug("Geocoder init");
        
        int total = 0;
        int ngc = 0;
        int failed = 0;
        try (ConnectionPair<TupleQueryResult> query = addrValueFacWrap.executeSelectQuery(countQuery))        
        {
            final TupleQueryResult countres = query.getObject();
            //MyTupleQueryResult countnotGC = sAddresses.executeSelectQueryAsTuples(notGCcountQuery);
            total = Integer.parseInt(countres.next().getValue("count").stringValue());
            //ngc = Integer.parseInt(countnotGC.next().getValue("count").stringValue());
            context.sendMessage(DPUContext.MessageType.INFO, "Found " + total + " addresses"/* + ngc + " not geocoded yet."*/);
        } catch (NumberFormatException | QueryEvaluationException e) {
            LOG.error("Failed to query and parse value.", e);
        }

        //Schema.org addresses
        LOG.debug("Executing Schema.org query: " + sOrgConstructQuery);
        //MyTupleQueryResult res = sAddresses.executeSelectQueryAsTuples(sOrgQuery);
        int count;
        
        // we use try catch resource for query handlig
        try (ConnectionPair<Graph> resGraphWrap = addrValueFacWrap.executeConstructQuery(sOrgConstructQuery)) {
            count = geocode(context, total, date, geoValueFac, addrValueFac, geoValueFacWrap, resGraphWrap.getObject());
        }
        
        if (context.canceled()) LOG.info("Cancelled");

           LOG.info("Geocoding done.");

        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();

        context.sendMessage(DPUContext.MessageType.INFO, "Geocoded " + count + ": Googled: "+ geocodes +" From cache: " + cacheHits + " in " + (end-start) + "ms, failed attempts: " + failed);

    }

    private int geocode(DPUContext context, int total,
            Date date, final ValueFactory geoValueFac, 
            final ValueFactory addrValueFac,
            final SimpleRdfWrite geoValueFacWrap, 
            final Graph resGraph) throws OperationFailedException {
        
        int count = 0;
        
        URI dcsource = geoValueFac.createURI("http://purl.org/dc/terms/source");
        URI googleURI = geoValueFac.createURI("https://developers.google.com/maps/documentation/javascript/geocoding");
        URI geoURI = geoValueFac.createURI("http://schema.org/geo");
        URI geocoordsURI = geoValueFac.createURI("http://schema.org/GeoCoordinates");
        URI postalAddressURI = addrValueFac.createURI("http://schema.org/PostalAddress");
        URI streetAddressURI = addrValueFac.createURI("http://schema.org/streetAddress");
        URI addressRegionURI = addrValueFac.createURI("http://schema.org/addressRegion");
        URI addressLocalityURI = addrValueFac.createURI("http://schema.org/addressLocality");
        URI addressCountryURI = addrValueFac.createURI("http://schema.org/addressCountry");
        URI postalCodeURI = addrValueFac.createURI("http://schema.org/postalCode");
        //URI xsdDouble = outGeo.createURI("http://www.w3.org/2001/XMLSchema#double");
        //URI xsdDecimal = outGeo.createURI("http://www.w3.org/2001/XMLSchema#decimal");
        URI longURI = geoValueFac.createURI("http://schema.org/longitude");
        URI latURI = geoValueFac.createURI("http://schema.org/latitude");        
        
        LOG.debug("Starting geocoding.");
        URI[] propURIs = new URI [] {streetAddressURI, addressLocalityURI, addressRegionURI, postalCodeURI, addressCountryURI};
        Iterator<Statement> it = resGraph.match(null, RDF.TYPE, postalAddressURI);
        int cachedToday = countTodaysCacheFiles(context);
        int toCache = (config.getLimit() - cachedToday);
        long lastDownload = 0;
        while (it.hasNext() && !context.canceled() && geocodes <= toCache)
        {
            count++;
            Resource currentAddressURI = it.next().getSubject();
            StringBuilder addressToGeoCode = new StringBuilder();

            for (URI currentPropURI : propURIs)
            {
                Iterator<Statement> it1 = resGraph.match(currentAddressURI, currentPropURI, null);
                
                if (it1.hasNext())
                {
                    Value currentValue = it1.next().getObject();
                    if (currentValue != null)
                    {
                        //logger.trace("Currently " + currentBinding);
                        String currentValueString = currentValue.stringValue();
                        //logger.trace("Value " + currentValueString);
                        addressToGeoCode.append(currentValueString);
                        addressToGeoCode.append(" ");
                    }
                }
            }
            
            String address = addressToGeoCode.toString();
            LOG.debug("Address to geocode (" + count + "/" + total + "): " + address);

            //CACHE
            String file = address.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-");
            File hPath = context.getGlobalDirectory();
            File hFile = new File(hPath, file);
            GeocoderRequest geocoderRequest;
            GeocodeResponse geocoderResponse;

            if (!hFile.exists())
            {
                long curTS = date.getTime();
                if (lastDownload + config.getInterval() > curTS)
                {
                    LOG.debug("Sleeping: " + (lastDownload + config.getInterval() - curTS));
                    try {
                        Thread.sleep(lastDownload + config.getInterval() - curTS);
                    } catch (InterruptedException e) {
                        LOG.info("Interrupted while sleeping");
                    }
                }

                geocoderRequest = new GeocoderRequestBuilder().setAddress(address).setLanguage("en").getGeocoderRequest();
                geocoderResponse = geocoder.geocode(geocoderRequest);
                lastDownload = date.getTime();

                geocodes++;
                GeocoderStatus s = geocoderResponse.getStatus();
                if (s == GeocoderStatus.OK) {
                    LOG.debug("Googled (" + geocodes + "): " + address);

                    try {
                        BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
                        fw.append(geocoderResponse.toString());
                        fw.close();
                    } catch (IOException e) {
                        LOG.error("Failed to write data.", e);
                    }
                    //CACHED
                }
                else if (s == GeocoderStatus.ZERO_RESULTS) {
                    LOG.warn("Zero results for: " + address);
                    continue;
                }
                else {
                    LOG.error("Status: " + geocoderResponse.getStatus() + " " + address);
                    break;
                }
            }
            else {
                cacheHits++;
                LOG.debug("From cache (" + cacheHits + "): " + address);
            }

            //READ FROM FILE - NOW IT EXISTS
            String cachedFile = null;
            try {
                cachedFile = FileUtils.readFileToString(hFile);
            } catch (IOException e) {
                LOG.error(e.getLocalizedMessage());
            }

            int indexOfLocation = cachedFile.indexOf("location=LatLng") + 16;
            String location = cachedFile.substring(indexOfLocation, cachedFile.indexOf("}", indexOfLocation));
            
            BigDecimal latitude = new BigDecimal(location.substring(location.indexOf("lat=")+4, location.indexOf(",")));
            BigDecimal longitude = new BigDecimal(location.substring(location.indexOf("lng=")+4));

            LOG.debug("Located: " + address + " Latitude: " + latitude + " Longitude: " + longitude);

            String uri = currentAddressURI.stringValue();
            URI addressURI = geoValueFac.createURI(uri);
            URI coordURI = geoValueFac.createURI(uri+"/geocoordinates/google");

            geoValueFacWrap.add(addressURI, geoURI , coordURI);
            geoValueFacWrap.add(coordURI, RDF.TYPE, geocoordsURI);
            geoValueFacWrap.add(coordURI, longURI, geoValueFac.createLiteral(longitude.toString()/*, xsdDecimal*/));
            geoValueFacWrap.add(coordURI, latURI, geoValueFac.createLiteral(latitude.toString()/*, xsdDecimal*/));
            geoValueFacWrap.add(coordURI, dcsource, googleURI);
        }
        return count;
    }


}
