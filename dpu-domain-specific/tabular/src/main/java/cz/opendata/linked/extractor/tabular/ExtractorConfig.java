package cz.opendata.linked.extractor.tabular;

import java.util.LinkedHashMap;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class ExtractorConfig extends DPUConfigObjectBase {

	private LinkedHashMap<String, String> columnPropertyMap;

	private String baseURI;

	private String columnWithURISupplement;

	private String encoding;
	
	private String quoteChar;
	
	private String delimiterChar;
	
	private String eofSymbols;
	
	private int rowLimit;

	private boolean isDBF;
	
	private boolean isCSV;
	
	public ExtractorConfig() {
		this.columnPropertyMap = null;
		this.baseURI = null;
		this.columnWithURISupplement = null;
		this.encoding = null;
		this.quoteChar = null;
		this.delimiterChar = null;
		this.eofSymbols = null;
		this.rowLimit = 0;
		this.isCSV = false;
		this.isDBF = false;
	}

	public ExtractorConfig(LinkedHashMap<String, String> columnPropertyMap,
			String baseURI, String columnWithURISupplement, String encoding, String quoteChar, String delimiterChar, String eofSymbols, int rowLimit, boolean isDBF, boolean isCSV) {
		this.columnPropertyMap = columnPropertyMap;
		this.baseURI = baseURI;
		this.columnWithURISupplement = columnWithURISupplement;
		this.encoding = encoding;
		this.quoteChar = quoteChar;
		this.delimiterChar = delimiterChar;
		this.eofSymbols = eofSymbols;
		this.rowLimit = rowLimit;
		this.isCSV = isCSV;
		this.isDBF = isDBF;
	}

	public LinkedHashMap<String, String> getColumnPropertyMap() {
		return this.columnPropertyMap;
	}

	public String getBaseURI() {
		return this.baseURI;
	}

	public String getColumnWithURISupplement() {
		return this.columnWithURISupplement;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setColumnPropertyMap(
			LinkedHashMap<String, String> columnPropertyMap) {
		this.columnPropertyMap = columnPropertyMap;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public void setColumnWithURISupplement(String columnWithURISupplement) {
		this.columnWithURISupplement = columnWithURISupplement;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
	}

	public String getDelimiterChar() {
		return delimiterChar;
	}

	public void setDelimiterChar(String delimiterChar) {
		this.delimiterChar = delimiterChar;
	}

	public String getEofSymbols() {
		return eofSymbols;
	}

	public void setEofSymbols(String eofSymbols) {
		this.eofSymbols = eofSymbols;
	}

	public int getRowLimit() {
		return rowLimit;
	}

	public void setRowLimit(int rowLimit) {
		this.rowLimit = rowLimit;
	}

	public boolean isDBF() {
		return isDBF;
	}

	public void setDBF(boolean isDBF) {
		this.isDBF = isDBF;
	}

	public boolean isCSV() {
		return isCSV;
	}

	public void setCSV(boolean isCSV) {
		this.isCSV = isCSV;
	}

}
