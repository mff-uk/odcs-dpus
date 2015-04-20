package cz.opendata.unifiedviews.dpus.distributionMetadata;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class DistributionMetadataConfig_V1 {

    private String datasetURI = "";

    private String distributionURI = "";

    private boolean useDatasetURIfromInput = true;

    /**
     * Language used for {@link title_cs}, {@link desc_cs}
     */
    private String language_orig = "";

    private String title_orig = "";

    private String title_en = "";

    private String desc_orig = "";

    private String desc_en = "";

    private String license = "";

    private String sparqlEndpointUrl = "";

    private String mediaType = "";

    private String downloadURL = "";

    private String accessURL = "";

    private Collection<String> exampleResources = new LinkedList<>();

    private boolean useNow = true;

    private Date modified = new Date();

    private Date issued = new Date();
    
    private boolean titleFromDataset = true;
    
    private boolean generateDistroURIFromDataset = true;

    private boolean originalLanguageFromDataset = true;

    private boolean issuedFromDataset = true;

    private boolean descriptionFromDataset = true;

    private boolean licenseFromDataset = true;
    
    private boolean schemaFromDataset = true;
    
    private boolean useTemporal = true;

    private boolean useNowTemporalEnd = false;

    private boolean temporalFromDataset = true;
    
    //private boolean spatialFromDataset = true;

    private Date temporalEnd = new Date();

    private Date temporalStart = new Date();

    //private String spatial = "";
    
    private String schema = "";

    private String schemaType = "";

    public DistributionMetadataConfig_V1() {

    }

    public String getDatasetURI() {
        return datasetURI;
    }

    public void setDatasetURI(String datasetURI) {
        this.datasetURI = datasetURI;
    }

    public String getLanguage_orig() {
        return language_orig;
    }

    public void setLanguage_orig(String language_orig) {
        this.language_orig = language_orig;
    }

    public String getTitle_orig() {
        return title_orig;
    }

    public void setTitle_orig(String title_orig) {
        this.title_orig = title_orig;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getDesc_orig() {
        return desc_orig;
    }

    public void setDesc_orig(String desc_orig) {
        this.desc_orig = desc_orig;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public void setDesc_en(String desc_en) {
        this.desc_en = desc_en;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Collection<String> getExampleResources() {
        return exampleResources;
    }

    public void setExampleResources(
            LinkedList<String> exampleResources) {
        this.exampleResources = exampleResources;
    }

    public boolean isUseNow() {
        return useNow;
    }

    public void setUseNow(boolean useNow) {
        this.useNow = useNow;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

	public Date getTemporalEnd() {
		return temporalEnd;
	}

	public void setTemporalEnd(Date temporalEnd) {
		this.temporalEnd = temporalEnd;
	}

	public Date getTemporalStart() {
		return temporalStart;
	}

	public void setTemporalStart(Date temporalStart) {
		this.temporalStart = temporalStart;
	}

    public boolean isUseNowTemporalEnd() {
        return useNowTemporalEnd;
    }

    public void setUseNowTemporalEnd(boolean useNowTemporalEnd) {
        this.useNowTemporalEnd = useNowTemporalEnd;
    }

//	public String getSpatial() {
//		return spatial;
//	}
//
//	public void setSpatial(String spatial) {
//		this.spatial = spatial;
//	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public String getAccessURL() {
		return accessURL;
	}

	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(String schemaType) {
		this.schemaType = schemaType;
	}
	
	public String getDistributionURI() {
		return distributionURI;
	}

	public void setDistributionURI(String distributionURI) {
		this.distributionURI = distributionURI;
	}

	public boolean isUseDatasetURIfromInput() {
		return useDatasetURIfromInput;
	}

	public void setUseDatasetURIfromInput(boolean useDatasetURIfromInput) {
		this.useDatasetURIfromInput = useDatasetURIfromInput;
	}

	public boolean isTitleFromDataset() {
		return titleFromDataset;
	}

	public void setTitleFromDataset(boolean titleFromDataset) {
		this.titleFromDataset = titleFromDataset;
	}

	public boolean isDescriptionFromDataset() {
		return descriptionFromDataset;
	}

	public void setDescriptionFromDataset(boolean descriptionFromDataset) {
		this.descriptionFromDataset = descriptionFromDataset;
	}

	public boolean isLicenseFromDataset() {
		return licenseFromDataset;
	}

	public void setLicenseFromDataset(boolean licenseFromDataset) {
		this.licenseFromDataset = licenseFromDataset;
	}

	public boolean isTemporalFromDataset() {
		return temporalFromDataset;
	}

	public void setTemporalFromDataset(boolean temporalFromDataset) {
		this.temporalFromDataset = temporalFromDataset;
	}

//	public boolean isSpatialFromDataset() {
//		return spatialFromDataset;
//	}
//
//	public void setSpatialFromDataset(boolean spatialFromDataset) {
//		this.spatialFromDataset = spatialFromDataset;
//	}

	public boolean isSchemaFromDataset() {
		return schemaFromDataset;
	}

	public void setSchemaFromDataset(boolean schemaFromDataset) {
		this.schemaFromDataset = schemaFromDataset;
	}

	public boolean isOriginalLanguageFromDataset() {
		return originalLanguageFromDataset;
	}

	public void setOriginalLanguageFromDataset(boolean originalLanguageFromDataset) {
		this.originalLanguageFromDataset = originalLanguageFromDataset;
	}

	public boolean isIssuedFromDataset() {
		return issuedFromDataset;
	}

	public void setIssuedFromDataset(boolean issuedFromDataset) {
		this.issuedFromDataset = issuedFromDataset;
	}

	public boolean isGenerateDistroURIFromDataset() {
		return generateDistroURIFromDataset;
	}

	public void setGenerateDistroURIFromDataset(boolean generateDistroURIFromDataset) {
		this.generateDistroURIFromDataset = generateDistroURIFromDataset;
	}

	public String getSparqlEndpointUrl() {
		return sparqlEndpointUrl;
	}

	public void setSparqlEndpointUrl(String sparqlEndpointUrl) {
		this.sparqlEndpointUrl = sparqlEndpointUrl;
	}

	public boolean isUseTemporal() {
		return useTemporal;
	}

	public void setUseTemporal(boolean useTemporal) {
		this.useTemporal = useTemporal;
	}
	
}
