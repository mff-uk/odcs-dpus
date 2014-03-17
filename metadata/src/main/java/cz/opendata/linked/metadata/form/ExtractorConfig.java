package cz.opendata.linked.metadata.form;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private static final long serialVersionUID = 8719241993054209502L;

	private URL datasetURI;

	private URL distroURI;

	private String title_cs = "Název datasetu";
	private String title_en = "Dataset title";
	private String desc_cs = "Popis datasetu";
	private String desc_en = "Dataset description";
	
	private String mime = "application/zip";

	private LinkedList<URL> authors = new LinkedList<>();
	private LinkedList<URL> possibleAuthors = new LinkedList<>();

	private LinkedList<URL> publishers = new LinkedList<>();
	private LinkedList<URL> possiblePublishers = new LinkedList<>();
	
	private LinkedList<URL> licenses = new LinkedList<>();
	private LinkedList<URL> possibleLicenses = new LinkedList<>();
	
	private LinkedList<URL> sources = new LinkedList<>();
	private LinkedList<URL> possibleSources = new LinkedList<>();
	
	private LinkedList<URL> exampleResources = new LinkedList<>();
	private LinkedList<URL> possibleExampleResources = new LinkedList<>();
	
	private LinkedList<URL> languages = new LinkedList<>();
	private LinkedList<URL> possibleLanguages = new LinkedList<>();

	private LinkedList<String> keywords = new LinkedList<>();
	private LinkedList<String> possibleKeywords = new LinkedList<>();

	private LinkedList<URL> themes = new LinkedList<>();
	private LinkedList<URL> possibleThemes = new LinkedList<>();


	private URL contactPoint;

	private URL sparqlEndpoint;

	private URL dataDump;

	private URL periodicity;
	
	private boolean useNow = true;
	
	private boolean isQb = false;

	private Date modified = new Date();
	
	@Override
    public boolean isValid() {
		return true;
    }
	
	public ExtractorConfig() {
		super();
		try {
			datasetURI = new URL("http://linked.opendata.cz/resource/dataset/");
			distroURI = new URL("http://linked.opendata.cz/resource/dataset//distribution");
			licenses.add(new URL("http://opendatacommons.org/licenses/pddl/1-0/"));
			sparqlEndpoint = new URL("http://linked.opendata.cz/sparql");
			dataDump = new URL("http://linked.opendata.cz/dump/");
			contactPoint = new URL("http://opendata.cz/contacts");
			periodicity = new URL("http://purl.org/linked-data/sdmx/2009/code#freq-M");
			modified = new Date();
			possibleSources.add(new URL("http://linked.opendata.cz"));
			possibleLicenses.add(new URL("http://opendatacommons.org/licenses/pddl/1-0/"));
			possibleLicenses.add(new URL("http://creativecommons.org/licenses/by/3.0/lu/"));
			possiblePublishers.add(new URL("http://opendata.cz"));
			publishers.add(new URL("http://opendata.cz"));
			possibleAuthors.add(new URL("http://purl.org/klimek#me"));
			possibleAuthors.add(new URL("http://opendata.cz/necasky#me"));
			possibleAuthors.add(new URL("http://mynarz.net/#jindrich"));
			possibleThemes.add(new URL("http://dbpedia.org/resource/EHealth"));
			possibleLanguages.add(new URL("http://id.loc.gov/vocabulary/iso639-1/en"));
			possibleLanguages.add(new URL("http://id.loc.gov/vocabulary/iso639-1/cs"));
			licenses.add(new URL("http://opendatacommons.org/licenses/pddl/1-0/"));
		} catch (MalformedURLException e) { }
	}

	public URL getDatasetURI() {
		return datasetURI;
	}

	public void setDatasetURI(URL datasetURI) {
		this.datasetURI = datasetURI;
	}

	public URL getDistroURI() {
		return distroURI;
	}

	public void setDistroURI(URL distroURI) {
		this.distroURI = distroURI;
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

	public void setDesc_cs(String desc_cs) {
		this.desc_cs = desc_cs;
	}

	public String getDesc_en() {
		return desc_en;
	}

	public void setDesc_en(String desc_en) {
		this.desc_en = desc_en;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public LinkedList<URL> getAuthors() {
		return authors;
	}

	public void setAuthors(LinkedList<URL> authors) {
		this.authors = authors;
	}

	public LinkedList<URL> getPossibleAuthors() {
		return possibleAuthors;
	}

	public void setPossibleAuthors(
			LinkedList<URL> possibleAuthors) {
		this.possibleAuthors = possibleAuthors;
	}

	public LinkedList<URL> getPublishers() {
		return publishers;
	}

	public void setPublishers(LinkedList<URL> publishers) {
		this.publishers = publishers;
	}

	public LinkedList<URL> getPossiblePublishers() {
		return possiblePublishers;
	}

	public void setPossiblePublishers(
			LinkedList<URL> possiblePublishers) {
		this.possiblePublishers = possiblePublishers;
	}

	public LinkedList<URL> getLicenses() {
		return licenses;
	}

	public void setLicenses(LinkedList<URL> licenses) {
		this.licenses = licenses;
	}

	public LinkedList<URL> getPossibleLicenses() {
		return possibleLicenses;
	}

	public void setPossibleLicenses(
			LinkedList<URL> possibleLicenses) {
		this.possibleLicenses = possibleLicenses;
	}

	public LinkedList<URL> getSources() {
		return sources;
	}

	public void setSources(LinkedList<URL> sources) {
		this.sources = sources;
	}

	public LinkedList<URL> getPossibleSources() {
		return possibleSources;
	}

	public void setPossibleSources(
			LinkedList<URL> possibleSources) {
		this.possibleSources = possibleSources;
	}

	public LinkedList<URL> getExampleResources() {
		return exampleResources;
	}

	public void setExampleResources(
			LinkedList<URL> exampleResources) {
		this.exampleResources = exampleResources;
	}

	public LinkedList<URL> getPossibleExampleResources() {
		return possibleExampleResources;
	}

	public void setPossibleExampleResources(
			LinkedList<URL> possibleExampleResources) {
		this.possibleExampleResources = possibleExampleResources;
	}

	public LinkedList<URL> getLanguages() {
		return languages;
	}

	public void setLanguages(LinkedList<URL> languages) {
		this.languages = languages;
	}

	public LinkedList<URL> getPossibleLanguages() {
		return possibleLanguages;
	}

	public void setPossibleLanguages(
			LinkedList<URL> possibleLanguages) {
		this.possibleLanguages = possibleLanguages;
	}

	public LinkedList<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(LinkedList<String> keywords) {
		this.keywords = keywords;
	}

	public LinkedList<String> getPossibleKeywords() {
		return possibleKeywords;
	}

	public void setPossibleKeywords(
			LinkedList<String> possibleKeywords) {
		this.possibleKeywords = possibleKeywords;
	}

	public LinkedList<URL> getThemes() {
		return themes;
	}

	public void setThemes(LinkedList<URL> themes) {
		this.themes = themes;
	}

	public LinkedList<URL> getPossibleThemes() {
		return possibleThemes;
	}

	public void setPossibleThemes(
			LinkedList<URL> possibleThemes) {
		this.possibleThemes = possibleThemes;
	}

	public URL getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	public void setSparqlEndpoint(URL sparqlEndpoint) {
		this.sparqlEndpoint = sparqlEndpoint;
	}

	public URL getDataDump() {
		return dataDump;
	}

	public void setDataDump(URL dataDump) {
		this.dataDump = dataDump;
	}

	public URL getContactPoint() {
		return contactPoint;
	}

	public void setContactPoint(URL contactPoint) {
		this.contactPoint = contactPoint;
	}

	public URL getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(URL periodicity) {
		this.periodicity = periodicity;
	}

	public boolean isUseNow() {
		return useNow;
	}

	public void setUseNow(boolean useNow) {
		this.useNow = useNow;
	}

	public boolean isIsQb() {
		return isQb;
	}

	public void setIsQb(boolean isQb) {
		this.isQb = isQb;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

}
