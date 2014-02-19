package cz.opendata.linked.extractor.tabular;

import java.util.Map;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class ExtractorConfig extends DPUConfigObjectBase {

	private static final long serialVersionUID = 6979581350385466975L;
	
	private Map<String, String> columnPropertyMap;
	
	private String baseURI;
	
	private String columnWithURISupplement;
	
	private String encoding;

	public ExtractorConfig()	{
		this.columnPropertyMap = null;
		this.baseURI = null;
		this.columnWithURISupplement = null;
		this.encoding = null;
	}
	
	public ExtractorConfig(Map<String, String> columnPropertyMap, String baseURI, String columnWithURISupplement, String encoding) {
		
		this.columnPropertyMap = columnPropertyMap;
		this.baseURI = baseURI;
		this.columnWithURISupplement = columnWithURISupplement;
		this.encoding = encoding;
	
	}
	
	public Map<String,String> getColumnPropertyMap()	{
		
		return this.columnPropertyMap;
		
	}
	
	public String getBaseURI()	{
		
		return this.baseURI;
		
	}
	
	public String getColumnWithURISupplement()	{
		
		return this.columnWithURISupplement;
		
	}
	
	public String getEncoding()	{
		
		return this.encoding;
		
	}
    
}
