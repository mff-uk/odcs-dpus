package cz.cuni.mff.xrg.odcs.transformer.converter.rdf.csv;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {
	
	private String targetPath = "/out.csv";
	
	private String query = "CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o }";

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
}
