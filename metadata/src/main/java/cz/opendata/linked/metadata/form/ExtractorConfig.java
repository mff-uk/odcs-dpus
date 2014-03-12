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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8719241993054209502L;

	public URL datasetURI, distroURI;

	public String title_cs = "Název datasetu";
	public String title_en = "Dataset title";
	public String desc_cs = "Popis datasetu";
	public String desc_en = "Dataset description";
	
	public String mime = "application/zip";

	public LinkedList<URL> authors = new LinkedList<URL>();
	public LinkedList<URL> possibleAuthors = new LinkedList<URL>();

	public LinkedList<URL> publishers = new LinkedList<URL>();
	public LinkedList<URL> possiblePublishers = new LinkedList<URL>();
	
	public LinkedList<URL> licenses = new LinkedList<URL>();
	public LinkedList<URL> possibleLicenses = new LinkedList<URL>();
	
	public LinkedList<URL> sources = new LinkedList<URL>();
	public LinkedList<URL> possibleSources = new LinkedList<URL>();
	
	public LinkedList<URL> exampleResources = new LinkedList<URL>();
	public LinkedList<URL> possibleExampleResources = new LinkedList<URL>();
	
	public LinkedList<URL> languages = new LinkedList<URL>();
	public LinkedList<URL> possibleLanguages = new LinkedList<URL>();

	public LinkedList<String> keywords = new LinkedList<String>();
	public LinkedList<String> possibleKeywords = new LinkedList<String>();

	public LinkedList<URL> themes = new LinkedList<URL>();
	public LinkedList<URL> possibleThemes = new LinkedList<URL>();

	public URL sparqlEndpoint, dataDump, contactPoint, periodicity;
	
	public boolean useNow = true;
	
	public Date modified = new Date();
	
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

}
