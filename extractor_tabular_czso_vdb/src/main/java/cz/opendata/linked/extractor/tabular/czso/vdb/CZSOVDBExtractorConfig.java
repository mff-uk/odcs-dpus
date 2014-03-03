package cz.opendata.linked.extractor.tabular.czso.vdb;

import java.util.LinkedHashMap;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class CZSOVDBExtractorConfig extends DPUConfigObjectBase {

	private static final long serialVersionUID = 6979581350385466975L;
	
	private LinkedHashMap<Integer, String> columnPropertyMap;
	
	private LinkedHashMap<Integer[], String> dimensionValueMap;
	
	private String baseURI;
	
	private int columnWithURISupplement;

	private int dataStartAtRow;
	
	public CZSOVDBExtractorConfig()	{
		this.columnPropertyMap = null;
		this.baseURI = null;
		this.columnWithURISupplement = -1;
	}
	
	public CZSOVDBExtractorConfig(LinkedHashMap<Integer, String> columnPropertyMap, LinkedHashMap<Integer[], String> dimensionValueMap, String baseURI, int columnWithURISupplement, int dataStartAtRow) {
		
		this.columnPropertyMap = columnPropertyMap;
		this.dimensionValueMap = dimensionValueMap;
		this.baseURI = baseURI;
		this.columnWithURISupplement = columnWithURISupplement;
		this.dataStartAtRow = dataStartAtRow;
	
	}
	
	public LinkedHashMap<Integer, String> getColumnPropertyMap()	{
		
		return this.columnPropertyMap;
		
	}
	
	public LinkedHashMap<Integer[], String> getDimensionValueMap()	{
		
		return this.dimensionValueMap;
		
	}
	
	public String getBaseURI()	{
		
		return this.baseURI;
		
	}
	
	public int getColumnWithURISupplement()	{
		
		return this.columnWithURISupplement;
		
	}
	
	public int getDataStartAtRow()	{
		
		return this.dataStartAtRow;
		
	}
    
}
