package cz.opendata.unifiedviews.dpus.datasetMetadata;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class DatasetMetadataVocabulary {

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";
    
    public static final String ADMS = "http://www.w3.org/ns/adms#";
    
    public static final String VCARD = "http://www.w3.org/2006/vcard/ns#";

    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final URI VCARD_VCARD_CLASS;

    public static final URI VCARD_HAS_EMAIL;

    public static final URI DCAT_KEYWORD;

    public static final URI DCAT_LANDING_PAGE;

    public static final URI DCAT_THEME;

    public static final URI DCAT_DATASET_CLASS;

    public static final URI SCHEMA_ENDDATE;

    public static final URI SCHEMA_STARTDATE;

    public static final URI ADMS_CONTACT_POINT;

    public static final URI XSD_DATE;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        DCAT_KEYWORD = valueFactory.createURI(DCAT + "keyword");
        DCAT_THEME = valueFactory.createURI(DCAT + "theme");
        DCAT_DATASET_CLASS = valueFactory.createURI(DCAT + "Dataset");
        ADMS_CONTACT_POINT = valueFactory.createURI(ADMS + "contactPoint");
        DCAT_LANDING_PAGE = valueFactory.createURI(DCAT + "landingPage");
        VCARD_VCARD_CLASS = valueFactory.createURI(VCARD + "VCard");
        VCARD_HAS_EMAIL = valueFactory.createURI(VCARD + "hasEmail");
        XSD_DATE = valueFactory.createURI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createURI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createURI(SCHEMA + "startDate");

    }
}
