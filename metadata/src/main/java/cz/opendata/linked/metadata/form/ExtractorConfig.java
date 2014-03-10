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

	public URL datasetURI;

	public String title_cs, title_en, desc_cs, desc_en;

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
	
	public URL sparqlEndpoint, dataDump;
	
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
			licenses.add(new URL("http://opendatacommons.org/licenses/pddl/1-0/"));
			sparqlEndpoint = new URL("http://linked.opendata.cz/sparql");
			dataDump = new URL("http://linked.opendata.cz/dump/");
			modified = new Date();
			possibleSources.add(new URL("http://linked.opendata.cz"));
			possibleLicenses.add(new URL("http://opendatacommons.org/licenses/pddl/1-0/"));
			possibleLicenses.add(new URL("http://creativecommons.org/licenses/by/3.0/lu/"));
			possiblePublishers.add(new URL("http://opendata.cz"));
			publishers.add(new URL("http://opendata.cz"));
			possibleAuthors.add(new URL("http://purl.org/klimek#me"));
			possibleAuthors.add(new URL("http://opendata.cz/necasky#me"));
			possibleAuthors.add(new URL("http://mynarz.net/#jindrich"));
			licenses.add(new URL("http://opendatacommons.org/licenses/pddl/1-0/"));
		} catch (MalformedURLException e) { }
	}

}
