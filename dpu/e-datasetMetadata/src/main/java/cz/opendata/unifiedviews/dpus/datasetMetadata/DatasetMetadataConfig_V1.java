package cz.opendata.unifiedviews.dpus.datasetMetadata;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class DatasetMetadataConfig_V1 {

    private String datasetURI = "";

    private String distributionURI = "";

    private boolean useDatasetURIfromInput = true;

    /**
     * Language used for {@link title_cs}, {@link desc_cs}
     */
    private String language_orig = "";

    private String title_cs = "";

    private String title_en = "";

    private String desc_cs = "";

    private String desc_en = "";

    private Collection<String> authors = new LinkedList<>();

    private String publisherURI = "http://opendata.cz";
    
    private String publisherName = "Opendata.cz";

    //private Collection<String> publishers = new LinkedList<>();

    private String license = "";

    private Collection<String> sources = new LinkedList<>();

    private Collection<String> languages = new LinkedList<>();

    private Collection<String> keywords_orig = new LinkedList<>();

    private Collection<String> keywords_en = new LinkedList<>();

    private Collection<String> themes = new LinkedList<>();

    private String contactPoint = "";

    private String contactPointName = "";

    private String periodicity = "";

    private boolean useNow = true;

    private boolean useNowTemporalEnd = false;

    private Date modified = new Date();

    private Date issued = new Date();
    
    private String identifier = "";

    private String landingPage = "";
    
    private Date temporalEnd = new Date();

    private Date temporalStart = new Date();

    private String spatial = "";
    
    private String schema = "";

    public DatasetMetadataConfig_V1() {

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

    public String getTitle_cs() {
        return title_cs;
    }

    public void setTitle_cs(String title_cs) {
        this.title_cs = title_cs;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getDesc_cs() {
        return desc_cs;
    }

    public void setDesc_orig(String desc_orig) {
        this.desc_cs = desc_orig;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public void setDesc_en(String desc_en) {
        this.desc_en = desc_en;
    }

    public Collection<String> getAuthors() {
        return authors;
    }

    public void setAuthors(Collection<String> authors) {
        this.authors = authors;
    }

//    public Collection<String> getPublishers() {
//        return publishers;
//    }
//
//    public void setPublishers(Collection<String> publishers) {
//        this.publishers = publishers;
//    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Collection<String> getSources() {
        return sources;
    }

    public void setSources(Collection<String> sources) {
        this.sources = sources;
    }

    public Collection<String> getLanguages() {
        return languages;
    }

    public void setLanguages(Collection<String> languages) {
        this.languages = languages;
    }

    public Collection<String> getKeywords_orig() {
        return keywords_orig;
    }

    public void setKeywords_orig(Collection<String> keywords_orig) {
        this.keywords_orig = keywords_orig;
    }

    public Collection<String> getKeywords_en() {
        return keywords_en;
    }

    public void setKeywords_en(Collection<String> keywords_en) {
        this.keywords_en = keywords_en;
    }

    public Collection<String> getThemes() {
        return themes;
    }

    public void setThemes(Collection<String> themes) {
        this.themes = themes;
    }

    public String getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public boolean isUseNow() {
        return useNow;
    }

    public void setUseNow(boolean useNow) {
        this.useNow = useNow;
    }

    public boolean isUseNowTemporalEnd() {
        return useNowTemporalEnd;
    }

    public void setUseNowTemporalEnd(boolean useNowTemporalEnd) {
        this.useNowTemporalEnd = useNowTemporalEnd;
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

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
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

	public String getSpatial() {
		return spatial;
	}

	public void setSpatial(String spatial) {
		this.spatial = spatial;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
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

	public String getContactPointName() {
		return contactPointName;
	}

	public void setContactPointName(String contactPointName) {
		this.contactPointName = contactPointName;
	}

	public String getPublisherURI() {
		return publisherURI;
	}

	public void setPublisherURI(String publisherURI) {
		this.publisherURI = publisherURI;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
}
