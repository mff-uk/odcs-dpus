package cz.opendata.linked.extractor.tabular.czso.vdb;

import java.util.LinkedHashMap;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class CZSOVDBExtractorConfig extends DPUConfigObjectBase {

	private static final long serialVersionUID = 6979581350385466975L;
	
	private LinkedHashMap<Integer, String> columnPropertyMap;
	
	private LinkedHashMap<Coordinates, String> fixedValueMap;
	
	private String baseURI;
	
	private int columnWithURISupplement;

	private int dataStartAtRow;
	
	public CZSOVDBExtractorConfig()	{
		this.columnPropertyMap = null;
		this.baseURI = null;
		this.columnWithURISupplement = -1;
	}
	
	public CZSOVDBExtractorConfig(LinkedHashMap<Integer, String> columnPropertyMap, LinkedHashMap<Coordinates, String> fixedValueMap, String baseURI, int columnWithURISupplement, int dataStartAtRow) {
		
		this.columnPropertyMap = columnPropertyMap;
		this.fixedValueMap = fixedValueMap;
		this.baseURI = baseURI;
		this.columnWithURISupplement = columnWithURISupplement;
		this.dataStartAtRow = dataStartAtRow;
	
	}
	
	public LinkedHashMap<Integer, String> getColumnPropertyMap()	{		
		return this.columnPropertyMap;		
	}
	
	public LinkedHashMap<Coordinates, String> getFixedValueMap()	{		
		return this.fixedValueMap;		
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

	public void setColumnPropertyMap(
			LinkedHashMap<Integer, String> columnPropertyMap) {
		this.columnPropertyMap = columnPropertyMap;
	}

	public void setFixedValueMap(
			LinkedHashMap<Coordinates, String> fixedValueMap) {
		this.fixedValueMap = fixedValueMap;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public void setColumnWithURISupplement(int columnWithURISupplement) {
		this.columnWithURISupplement = columnWithURISupplement;
	}

	public void setDataStartAtRow(int dataStartAtRow) {
		this.dataStartAtRow = dataStartAtRow;
	}
	    
}
