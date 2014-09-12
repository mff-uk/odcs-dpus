package cz.opendata.linked.geocoder;

import java.io.File;
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
import cz.opendata.linked.geocoder.lib.Geocoder;
import cz.opendata.linked.geocoder.lib.Geocoder.GeoProvider;
import cz.opendata.linked.geocoder.lib.Geocoder.GeoProviderFactory;
import cz.opendata.linked.geocoder.lib.Position;
import org.openrdf.model.*;
import org.openrdf.query.TupleQueryResult;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);
    
    @DataUnit.AsInput(name = "Schema.org addresses")
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

    @Override
    protected void innerExecute() throws DPUException, DataUnitException
    {
        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        SimpleRdfWrite geoValueFacWrap = SimpleRdfFactory.create(outGeo, context);        
        final ValueFactory geoValueFac = geoValueFacWrap.getValueFactory();
        
        SimpleRdfRead sAddressesWrap = SimpleRdfFactory.create(sAddresses, context);
        final ValueFactory addrValueFac = sAddressesWrap.getValueFactory();
        
        String geoCache = new File(context.getGlobalDirectory(), "cache/geocoder.cache").getAbsolutePath();
        String countQuery = "PREFIX s: <http://schema.org/> "
                + "SELECT (COUNT (*) as ?count) "
                + "WHERE "
                + "{"
                    + "?address a s:PostalAddress . "
                +  "}"; 
        String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
                + "CONSTRUCT {?address ?p ?o}"
                + "WHERE "
                + "{"
                    + "?address a s:PostalAddress ;"
                    + "            ?p ?o . "
                +  "}"; 
        String sOrgQuery = "PREFIX s: <http://schema.org/> "
                + "SELECT DISTINCT * "
                + "WHERE "
                + "{"
                    + "{?address a s:PostalAddress . } "
                    + "UNION { ?address s:streetAddress ?street . } "
                    + "UNION { ?address s:addressRegion ?region . } "
                    + "UNION { ?address s:addressLocality ?locality . } "
                    + "UNION { ?address s:postalCode ?postal . } "
                    + "UNION { ?address s:addressCountry ?country . } "
                + " }";

        LOG.debug("Geocoder init");
        Geocoder.loadCacheIfEmpty(geoCache);
        GeoProvider gisgraphy = GeoProviderFactory.createXMLGeoProvider(
                config.getGeocoderURI() + "/fulltext/fulltextsearch?allwordsrequired=false&from=1&to=1&q=",
                "/response/result/doc[1]/double[@name=\"lat\"]",
                "/response/result/doc[1]/double[@name=\"lng\"]",
                0);
        LOG.debug("Geocoder initialized");

        int total = 0;
        try (ConnectionPair<TupleQueryResult> countres = sAddressesWrap.executeSelectQuery(countQuery)) {
            total = Integer.parseInt(countres.getObject().next().getValue("count").stringValue());
            LOG.info("Found " + total + " addresses.");
        } catch (NumberFormatException | QueryEvaluationException e) {
            LOG.error("Failed to query and parse value.", e);
        }

        //Schema.org addresses
        LOG.debug("Executing Schema.org query: " + sOrgQuery);
        //MyTupleQueryResult res = sAddresses.executeSelectQueryAsTuples(sOrgQuery);

        LOG.debug("Starting geocoding.");
        try (ConnectionPair<Graph> resGraph = sAddressesWrap.executeConstructQuery(sOrgConstructQuery)) {
            geocode(resGraph.getObject(), context, total, gisgraphy, geoValueFac, addrValueFac, geoValueFacWrap, geoCache, start);
        }
    }

    private void geocode(
            Graph resGraph, DPUContext context,
            int total, GeoProvider gisgraphy,
            final ValueFactory geoValueFac, final ValueFactory addrValueFac,
            SimpleRdfWrite geoValueFacWrap,
            String geoCache, long start) throws OperationFailedException {

        int count = 0;
        int failed = 0;
        
        URI geoURI = geoValueFac.createURI("http://schema.org/geo");
        URI geocoordsURI = geoValueFac.createURI("http://schema.org/GeoCoordinates");
        URI postalAddressURI = addrValueFac.createURI("http://schema.org/PostalAddress");
        URI streetAddressURI = addrValueFac.createURI("http://schema.org/streetAddress");
        URI addressRegionURI = addrValueFac.createURI("http://schema.org/addressRegion");
        URI addressLocalityURI = addrValueFac.createURI("http://schema.org/addressLocality");
        URI addressCountryURI = addrValueFac.createURI("http://schema.org/addressCountry");
        URI postalCodeURI = addrValueFac.createURI("http://schema.org/postalCode");
        URI xsdDouble = geoValueFac.createURI("http://www.w3.org/2001/XMLSchema#double");
        URI xsdDecimal = geoValueFac.createURI("http://www.w3.org/2001/XMLSchema#decimal");
        URI longURI = geoValueFac.createURI("http://schema.org/longitude");
        URI latURI = geoValueFac.createURI("http://schema.org/latitude");        
        
        URI[] propURIs = new URI [] {streetAddressURI, addressLocalityURI, addressRegionURI, postalCodeURI, addressCountryURI};
        Iterator<Statement> it = resGraph.match(null, RDF.TYPE, postalAddressURI);
        while (it.hasNext() && !context.canceled())
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
            /*            }
            
            String[] props = new String [] {"street", "locality", "region", "postal", "country"};
            while (res.hasNext() && !context.canceled())
            {
            count++;
            BindingSet s = res.next();
            StringBuilder addressToGeoCode = new StringBuilder();

            for (String currentBinding : props)
            {
            Value currentValue = s.getValue(currentBinding);
            if (s.hasBinding(currentBinding) && currentValue != null)
            {
            //logger.trace("Currently " + currentBinding);
            String currentValueString = currentValue.stringValue();
            //logger.trace("Value " + currentValueString);
            addressToGeoCode.append(currentValueString);
            addressToGeoCode.append(" ");
            }
            }*/

            String address = addressToGeoCode.toString();
            LOG.debug("Address to geocode (" + count + "/" + total + "): " + address);

            Position pos = Geocoder.locate(address, gisgraphy);

            Double latitude;
            Double longitude;

            if (pos != null)
            {
                latitude = pos.getLatitude();
                longitude = pos.getLongitude();
                LOG.debug("Located " + address + " Latitude: " + latitude + " Longitude: " + longitude);

                String uri = currentAddressURI.stringValue();
//                    String uri = s.getValue("address").stringValue();
                URI addressURI = geoValueFac.createURI(uri);
                URI coordURI = geoValueFac.createURI(uri+"/geocoordinates");

                geoValueFacWrap.add(addressURI, geoURI , coordURI);
                geoValueFacWrap.add(coordURI, RDF.TYPE, geocoordsURI);
                geoValueFacWrap.add(coordURI, longURI, geoValueFac.createLiteral(longitude.toString()/*, xsdDecimal*/));
                geoValueFacWrap.add(coordURI, latURI, geoValueFac.createLiteral(latitude.toString()/*, xsdDecimal*/));
            }
            else {
                failed++;
                LOG.warn("Failed to locate: " + address);
            }
        }
        if (context.canceled()) LOG.info("Cancelled");        
        
        LOG.info("Geocoding done.");
        
        if (config.isRewriteCache()) {
            LOG.debug("Saving geo cache");
            Geocoder.saveCache(geoCache);
            LOG.debug("Geo cache saved.");
        }
        
        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();

        context.sendMessage(DPUContext.MessageType.INFO, "Processed " + count + " in " + (end-start) + "ms, failed attempts: " + failed);
    }

}