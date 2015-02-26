package cz.opendata.unifiedviews.dpus.distributionMetadata;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class DistributionMetadataVocabulary {

    public static final String STR_METADATA_GRAPH = "http://unifiedviews.eu/resources/dpu/transformer/metadata/";

    public static final String SCHEMA = "http://schema.org/";

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";
    
    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String POD = "https://project-open-data.cio.gov/v1.1/schema/#";

    public static final String WDRS = "http://www.w3.org/2007/05/powder-s#";

    public static final URI DCAT_DISTRIBUTION_CLASS;

    public static final URI DCAT_DATASET_CLASS;

    public static final URI DCAT_DISTRIBUTION;

    public static final URI DCAT_DOWNLOADURL;

    public static final URI DCAT_ACCESSURL;

    public static final URI DCAT_MEDIATYPE;

    public static final URI VOID_DATASET;

    public static final URI SCHEMA_ENDDATE;

    public static final URI SCHEMA_STARTDATE;

    public static final URI POD_DISTRIBUTION_DESCRIBREBYTYPE;

    public static final URI WDRS_DESCRIBEDBY;

    public static final URI XSD_DATE;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        DCAT_DISTRIBUTION_CLASS = valueFactory.createURI(DCAT + "Distribution");
        DCAT_DATASET_CLASS = valueFactory.createURI(DCAT + "Dataset");
        DCAT_DISTRIBUTION = valueFactory.createURI(DCAT + "distribution");
        DCAT_DOWNLOADURL = valueFactory.createURI(DCAT + "downloadURL");
        DCAT_ACCESSURL = valueFactory.createURI(DCAT + "accessURL");
        DCAT_MEDIATYPE = valueFactory.createURI(DCAT + "mediaType");
        VOID_DATASET = valueFactory.createURI(VOID + "Dataset");
        XSD_DATE = valueFactory.createURI(XSD + "date");
        SCHEMA_ENDDATE = valueFactory.createURI(SCHEMA + "endDate");
        SCHEMA_STARTDATE = valueFactory.createURI(SCHEMA + "startDate");
        POD_DISTRIBUTION_DESCRIBREBYTYPE = valueFactory.createURI(POD + "distribution-describedByType");
        WDRS_DESCRIBEDBY = valueFactory.createURI(WDRS + "describedBy");

    }
}
