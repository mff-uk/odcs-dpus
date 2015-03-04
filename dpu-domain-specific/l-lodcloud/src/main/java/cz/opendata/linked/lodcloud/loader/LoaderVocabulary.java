package cz.opendata.linked.lodcloud.loader;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class LoaderVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";
    
    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String POD = "https://project-open-data.cio.gov/v1.1/schema/#";

    public static final String WDRS = "http://www.w3.org/2007/05/powder-s#";

    public static final String ADMS = "http://www.w3.org/ns/adms#";
    
    public static final String VCARD = "http://www.w3.org/2006/vcard/ns#";

    public static final URI DCAT_DISTRIBUTION_CLASS;

    public static final URI DCAT_DATASET_CLASS;

    public static final URI DCAT_DISTRIBUTION;

    public static final URI DCAT_DOWNLOADURL;

    public static final URI DCAT_ACCESSURL;

    public static final URI DCAT_MEDIATYPE;

    public static final URI VOID_DATASET_CLASS;

    public static final URI VOID_EXAMPLERESOURCE;

    public static final URI VOID_TRIPLES;

    public static final URI VOID_DATADUMP;

    public static final URI VOID_SPARQLENDPOINT;

    public static final URI SCHEMA_ENDDATE;

    public static final URI SCHEMA_STARTDATE;

    public static final URI POD_DISTRIBUTION_DESCRIBREBYTYPE;

    public static final URI WDRS_DESCRIBEDBY;

    public static final URI XSD_DATE;

    public static final URI VCARD_VCARD_CLASS;

    public static final URI VCARD_HAS_EMAIL;

    public static final URI DCAT_KEYWORD;

    public static final URI DCAT_LANDING_PAGE;

    public static final URI DCAT_THEME;

    public static final URI ADMS_CONTACT_POINT;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        DCAT_KEYWORD = valueFactory.createURI(DCAT + "keyword");
        DCAT_THEME = valueFactory.createURI(DCAT + "theme");
        DCAT_DISTRIBUTION_CLASS = valueFactory.createURI(DCAT + "Distribution");
        DCAT_DATASET_CLASS = valueFactory.createURI(DCAT + "Dataset");
        DCAT_DISTRIBUTION = valueFactory.createURI(DCAT + "distribution");
        DCAT_DOWNLOADURL = valueFactory.createURI(DCAT + "downloadURL");
        DCAT_ACCESSURL = valueFactory.createURI(DCAT + "accessURL");
        DCAT_MEDIATYPE = valueFactory.createURI(DCAT + "mediaType");
        VOID_DATASET_CLASS = valueFactory.createURI(VOID + "Dataset");
        VOID_EXAMPLERESOURCE = valueFactory.createURI(VOID + "exampleResource");
        VOID_DATADUMP = valueFactory.createURI(VOID + "dataDump");
        VOID_TRIPLES = valueFactory.createURI(VOID + "triples");
        VOID_SPARQLENDPOINT = valueFactory.createURI(VOID + "sparqlEndpoint");
        XSD_DATE = valueFactory.createURI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createURI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createURI(SCHEMA + "startDate");
        POD_DISTRIBUTION_DESCRIBREBYTYPE = valueFactory.createURI(POD + "distribution-describedByType");
        WDRS_DESCRIBEDBY = valueFactory.createURI(WDRS + "describedBy");
        ADMS_CONTACT_POINT = valueFactory.createURI(ADMS + "contactPoint");
        DCAT_LANDING_PAGE = valueFactory.createURI(DCAT + "landingPage");
        VCARD_VCARD_CLASS = valueFactory.createURI(VCARD + "VCard");
        VCARD_HAS_EMAIL = valueFactory.createURI(VCARD + "hasEmail");

    }
}
