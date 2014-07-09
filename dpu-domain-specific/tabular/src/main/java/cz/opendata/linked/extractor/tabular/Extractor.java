package cz.opendata.linked.extractor.tabular;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfHeader;
import org.jamel.dbf.utils.DbfUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.*;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import java.util.*;

import org.slf4j.Logger;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

@AsExtractor
public class Extractor extends ConfigurableBase<ExtractorConfig> implements
		ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);
	
	private final String baseODCSPropertyURI = "http://linked.opendata.cz/ontology/odcs/tabular/";

	@InputDataUnit(name = "table")
	public FileDataUnit tableFile;
	
	@OutputDataUnit(name = "triplifiedTable")
	public WritableRDFDataUnit triplifiedTableT;
	
	public Extractor() {
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext context) throws DPUException,
			DataUnitException {

		SimpleRdfWrite triplifiedTableWrap = SimpleRdfFactory.create(triplifiedTableT, context);
		triplifiedTableWrap.setPolicy(AddPolicy.BUFFERED);
		
		// do we found at least one file?
		boolean fileFound = false;
		
		// extract from all files				
		Iterator<Handler> iter = tableFile.getRootDir().getFlatIterator();
		while(iter.hasNext()) {
			Handler handler = iter.next();
			if ( handler instanceof FileHandler ) {
				fileFound = true;
				FileHandler fileHandler = (FileHandler) handler;
				context.sendMessage(MessageType.INFO, "Processing file " + handler.getName(), 
						"Processing file " + handler.getRootedPath());
				// export data
				proceedFile(context, triplifiedTableWrap, fileHandler.asFile());
			}				
		}

		triplifiedTableWrap.flushBuffer();		
		
		if (!fileFound)	{
			context.sendMessage(MessageType.ERROR, "No file found in the input file data unit.");
		}

	}
	
	private void proceedFile(DPUContext context, SimpleRdfWrite triplifiedTableWrap, File tableFile) throws OperationFailedException {
		final ValueFactory valueFactory = triplifiedTableWrap.getValueFactory();
		
		String tableFileName = tableFile.getName();
		
		Map<String, String> columnPropertyMap = this.config.getColumnPropertyMap();
		if ( columnPropertyMap == null )	{
			LOG.warn("No mapping of table columns to RDF properties have been specified.");
			columnPropertyMap = new HashMap<>();
		}
		String baseURI = this.config.getBaseURI();
		if ( baseURI == null || "".equals(baseURI) )	{
			LOG.info("No base for URIs of resources extracted from rows of the table has been specified. Default base will be applied (http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/)");
			baseURI = "http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/";
		}
		String columnWithURISupplement = this.config.getColumnWithURISupplement();
		if ( columnWithURISupplement == null || "".equals(columnWithURISupplement) )	{
			LOG.info("No column with values supplementing the base for URIs of resources extracted from rows of the table has been specified. Row number (starting at 0) will be used instead.");
			columnWithURISupplement = null;
		}
				
		URI propertyRow = valueFactory.createURI(baseODCSPropertyURI + "row");		
		if ( this.config.isCSV() )	{
			
			String quoteChar = this.config.getQuoteChar();
			String delimiterChar = this.config.getDelimiterChar();
			String eofSymbols = this.config.getEofSymbols();
			
			if ( quoteChar == null || "".equals(quoteChar) )	{
				quoteChar = "\"";
				LOG.info("No quote char supplied. Default quote char '\"' will be used.");
			}
			
			if ( delimiterChar == null || "".equals(delimiterChar) )	{
				delimiterChar = "\"";
				LOG.info("No delimiter char supplied. Default delimiter char ',' will be used.");
			}
			
			if ( eofSymbols == null || "".equals(eofSymbols) )	{
				eofSymbols = "\n";
				LOG.info("No end of line symbols supplied. Default end of line symbols '\\n' will be used.");
			}
			
			final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder(quoteChar.charAt(0), delimiterChar.charAt(0), eofSymbols).build();
			
			ICsvListReader listReader = null;
			try	{
				
				listReader = new CsvListReader(new BufferedReader(new InputStreamReader(new FileInputStream(tableFile), config.getEncoding())), CSV_PREFERENCE);
								
				final String[] header = listReader.getHeader(true);
				int columnWithURISupplementNumber = -1;
    			URI[] propertyMap = new URI[header.length];
    			for ( int i = 0; i < header.length; i++ )	{
    				String fieldName = header[i];
    				if ( columnWithURISupplement != null && columnWithURISupplement.equals(fieldName) )	{
    					columnWithURISupplementNumber = i;
    				}
    				if ( columnPropertyMap.containsKey(fieldName) )	{
    					propertyMap[i] = valueFactory.createURI(columnPropertyMap.get(fieldName));
    				} else {
    					fieldName = this.convertStringToURIPart(fieldName);
    					propertyMap[i] = valueFactory.createURI(baseODCSPropertyURI + fieldName);
    				}
    			}
				
				List<String> row = listReader.read();
				
				int rowno = 0;
                while( row != null ) {
                	
                	if ( config.getRowLimit() > 0 )	{
                		if ( rowno >= config.getRowLimit() )	{
                			break;
                		}
                	}

                	String suffixURI;
    				if ( columnWithURISupplementNumber >= 0 )	{
    					suffixURI = this.convertStringToURIPart(row.get(columnWithURISupplementNumber));
    				} else {
    					suffixURI = (new Integer(rowno)).toString();
    				}
    				
    				Resource subj = valueFactory.createURI(baseURI + suffixURI);
    				
    				int i = 0;
    				for (String strValue : row) {
    					if ( strValue == null || "".equals(strValue) )	{
    						URI obj = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
    						triplifiedTableWrap.add(subj, propertyMap[i], obj);
    					} else {
    				        Value obj = valueFactory.createLiteral(strValue);
    				        triplifiedTableWrap.add(subj, propertyMap[i], obj);
    					}
    					i++;
					}
    			        					
    		        Value rowvalue = valueFactory.createLiteral(String.valueOf(rowno));
    		        triplifiedTableWrap.add(subj, propertyRow, rowvalue);
    				
    				if ( (rowno % 1000) == 0 )	{
    					LOG.debug("Row number {} processed.", rowno);
    				}
    				    				
    				rowno++;
    				row = listReader.read();
    				
    				if (context.canceled()) {
    		       		LOG.info("DPU cancelled");
    		       		listReader.close();
    		       		return;
    		       	}
                }
			} catch (IOException e)	{
				context.sendMessage(MessageType.ERROR, "IO exception during processing the input CSV file.");
	        	return;
			} finally {
				
				if ( listReader != null )	{
					try	{
						listReader.close();
					} catch (IOException e)	{
						context.sendMessage(MessageType.ERROR, "IO exception when closing the reader of the input CSV file.");
			        	return;
					}
				}
				
			}
			
		} else if ( this.config.isDBF() )	{
		
			String encoding = this.config.getEncoding();
			if ( encoding == null || "".equals(encoding) )	{
				DbfReaderLanguageDriver languageDriverReader = new DbfReaderLanguageDriver(tableFile);
				DbfHeaderLanguageDriver languageDriverHeader = languageDriverReader.getHeader();
				languageDriverHeader.getLanguageDriver();
				languageDriverReader.close();
				
				//TODO Make proper mapping of DBF encoding codes to Java codes. Until this is repaired, we set UTF-8. We suppose that DPUs have set the encoding explicitly by the user.
				encoding = "UTF-8";
			}
			if (!Charset.isSupported(encoding))	{
				context.sendMessage(MessageType.ERROR, "Charset " + encoding + " is not supported.");
	        	return;
			}
			
			DbfReader reader = new DbfReader(tableFile);
			DbfHeader header = reader.getHeader();
	
			int columnWithURISupplementNumber = -1;
			URI[] propertyMap = new URI[header.getFieldsCount()];
			for ( int i = 0; i < header.getFieldsCount(); i++ )	{
				DbfField field = header.getField(i);
				String fieldName = field.getName();
				
				LOG.debug("Filed: {} type: {} len: {}", field.getName(), field.getDataType(), field.getFieldLength());
				
				if ( columnWithURISupplement != null && columnWithURISupplement.equals(fieldName) )	{
					columnWithURISupplementNumber = i;
				}
				if ( columnPropertyMap.containsKey(fieldName) )	{
					propertyMap[i] = valueFactory.createURI(columnPropertyMap.get(fieldName));
				} else {
					fieldName = this.convertStringToURIPart(fieldName);
					propertyMap[i] = valueFactory.createURI(baseODCSPropertyURI + fieldName);
				}
			}
			
			Object[] row = null;
			int rowno = 0;
						
			while ( (row = reader.nextRecord()) != null )	{
				
				if ( config.getRowLimit() > 0 )	{
            		if ( rowno >= config.getRowLimit() )	{
            			break;
            		}
            	}
				
				String suffixURI;
				if ( columnWithURISupplementNumber >= 0 )	{
					suffixURI = this.convertStringToURIPart(this.getCellValue(row[columnWithURISupplementNumber], encoding));
				} else {
					suffixURI = (new Integer(rowno)).toString();
				}
				
				Resource subj = valueFactory.createURI(baseURI + suffixURI);
				
				for ( int i = 0; i < row.length; i++ )	{
			        					
					String strValue = this.getCellValue(row[i], encoding);
					if ( strValue == null || "".equals(strValue) )	{
						URI obj = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
						triplifiedTableWrap.add(subj, propertyMap[i], obj);
					} else {
				        Value obj = valueFactory.createLiteral(this.getCellValue(row[i], encoding));
				        triplifiedTableWrap.add(subj, propertyMap[i], obj);
					}
			       	
				}
				 
		        Value rowvalue = valueFactory.createLiteral(this.getCellValue(rowno, encoding));
		        triplifiedTableWrap.add(subj, propertyRow, rowvalue);
				
				if ( (rowno % 1000) == 0 )	{
					LOG.debug("Row number {} processed.", rowno);
				}
				rowno++;
				
				if (context.canceled()) {
		       		LOG.info("DPU cancelled");
		       		reader.close();
		       		return;
		       	}
				
			}
			
			reader.close();
		}
	}
	
	private String getCellValue(Object cell, String encoding)	{
		if (cell instanceof Date)	{
			return ((Date) cell).toString();
		} else if (cell instanceof Float)	{
			return ((Float) cell).toString();
		} else if (cell instanceof Boolean)	{
			return ((Boolean) cell).toString();
		} else if (cell instanceof Number)	{
			return ((Number) cell).toString();
		} else {
			try	{
				return new String(DbfUtils.trimLeftSpaces((byte[]) cell), encoding);
			} catch (UnsupportedEncodingException ex)	{
				//	ignored, solved earlier when reading encoding of the file
				return "";
			}
		}
	}
	
	private String convertStringToURIPart(String part)	{
		return part.replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
	}
	
}
