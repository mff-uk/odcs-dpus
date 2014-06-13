package cz.opendata.linked.extractor.tabular.czso.vdb;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import cz.cuni.mff.xrg.odcs.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
import java.util.*;
import org.openrdf.model.*;
import org.slf4j.Logger;

@AsExtractor
public class CZSOVDBExtractor extends ConfigurableBase<CZSOVDBExtractorConfig> implements
		ConfigDialogProvider<CZSOVDBExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(
			CZSOVDBExtractor.class);
	
	private final String baseODCSPropertyURI = "http://linked.opendata.cz/ontology/odcs/tabular/";

	@InputDataUnit(name = "tables")
	public FileDataUnit tableFiles;
	
	@OutputDataUnit(name = "triplifiedTables")
	public WritableRDFDataUnit triplifiedTables;

	public CZSOVDBExtractor() {
		super(CZSOVDBExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<CZSOVDBExtractorConfig> getConfigurationDialog() {
		return new CZSOVDBExtractorDialog();
	}

	private void processTabularFile(File tableFile, DPUContext context) throws OperationFailedException {
				
		String tableFileName = tableFile.getName();
		
		LinkedHashMap<Integer, String> propertyMap = this.config.getColumnPropertyMap();
		if ( propertyMap == null )	{
			LOG.warn("No mapping of table columns to RDF properties has been specified.");
			propertyMap = new LinkedHashMap<>();
		}
		LinkedHashMap<Coordinates, String> fixedValueMap = this.config.getFixedValueMap();
		HashMap<Integer, HashMap<Integer, String>> optimizedFixedValueMap = new HashMap<>(); 
		if ( fixedValueMap == null )	{
			LOG.warn("No mapping of cells to fixed value properties has been specified.");
		} else {
			for (Coordinates coordinates : fixedValueMap.keySet()) {
				if ( coordinates != null )	{
					Integer column = new Integer(coordinates.column);
					Integer row = new Integer(coordinates.row);
					
					HashMap<Integer, String> rowMap;
					if ( optimizedFixedValueMap.containsKey(row) )	{
						rowMap = optimizedFixedValueMap.get(row);
					} else {
						rowMap = new HashMap<>();
						optimizedFixedValueMap.put(row, rowMap);
					}
					rowMap.put(column, fixedValueMap.get(coordinates));
				}
			}
		}
		ArrayList<String[]> fixedPropertyValueTypeTriples = new ArrayList<>();
		String baseURI = this.config.getBaseURI();
		if ( baseURI == null || "".equals(baseURI) )	{
			LOG.warn("No base for URIs of resources extracted from rows of the table has been specified. Default base will be applied (http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/)");
			baseURI = "http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/";
		}
		int columnWithURISupplement = this.config.getColumnWithURISupplement();
		if ( columnWithURISupplement < 0 )	{
			LOG.warn("No column with values supplementing the base for URIs of resources extracted from rows of the table has been specified. Row number (starting at 0) will be used instead.");
			columnWithURISupplement = -1;
		}
		int dataStartAtRow = this.config.getDataStartAtRow();
		if ( dataStartAtRow < 0 )	{
			LOG.warn("Row where the data start was not specified. Zero is used instead.");
			columnWithURISupplement = 0;
		}
		
		final SimpleRdfWrite triplifiedTableWrap = new SimpleRdfWrite(triplifiedTables, context);
		triplifiedTableWrap.setPolicy(AddPolicy.BUFFERED);
		final ValueFactory valueFactory = triplifiedTableWrap.getValueFactory();
		
		try	{		
			Workbook wb = WorkbookFactory.create(tableFile);			
			int numberOfSheets = wb.getNumberOfSheets();
			
			for ( int i = 0; i < numberOfSheets; i++ )	{
				
				Sheet sheet = wb.getSheetAt(0);
				String sheetURI = "sheet/" + i + "/";
				int dataEndAtRow = sheet.getLastRowNum();
				URI propertyRow = valueFactory.createURI(baseODCSPropertyURI + "row");
				
				for ( int rowCounter = 0; rowCounter <= dataEndAtRow; rowCounter++ )	{
					
					Row row = sheet.getRow(rowCounter);
					
					if ( row != null)	{
					
						Resource subj = null;
						HashMap<Integer, String> rowMapWithDimensionValues = optimizedFixedValueMap.get(new Integer(rowCounter));
						
						int columnEnd = row.getLastCellNum();
						
						if ( rowCounter >= dataStartAtRow )	{
							
							String suffixURI;
							if ( columnWithURISupplement >= 0 )	{
								String[] value = this.getCellValue(row.getCell(columnWithURISupplement));
								if ( value[0] != null )	{
									suffixURI = value[0].replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
								} else {
									suffixURI = (new Integer(rowCounter)).toString();
								}
							} else {
								suffixURI = (new Integer(rowCounter)).toString();
							}							
							subj = valueFactory.createURI(baseURI + sheetURI + "row/" + suffixURI);														
						}
						
						for ( int columnCounter = 0; columnCounter <= columnEnd; columnCounter++ )	{
							
							Cell cell = row.getCell(columnCounter);
							if ( cell != null )	{
								
								String[] value = this.getCellValue(cell);
								
								Integer key = new Integer(columnCounter);
								
								if ( rowCounter >= dataStartAtRow )	{
								
									String propertyURI;
		
									if ( propertyMap.containsKey(key) )	{
										propertyURI = propertyMap.get(key);
									} else {
										propertyURI = baseODCSPropertyURI + "column" + key.toString();
									}
									
									Value obj;
									if ( value[0] == null || "".equals(value[0]) )	{
										obj = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
									} else {
								        obj = valueFactory.createLiteral(value[0], valueFactory.createURI(value[1]));
									}
									
									triplifiedTableWrap.add(subj, valueFactory.createURI(propertyURI), obj);
								}
								
								if ( value[0] != null && !"".equals(value[0]) && rowMapWithDimensionValues != null )	{
									
									String propertyURI = rowMapWithDimensionValues.get(key);
									if ( propertyURI != null && !"".equals(propertyURI) )	{
										
										String[] globalPropertyValueTriple = new String[3];
										globalPropertyValueTriple[0] = propertyURI;
										globalPropertyValueTriple[1] = value[0];
										globalPropertyValueTriple[2] = value[1];
										fixedPropertyValueTypeTriples.add(globalPropertyValueTriple);										
									}									
								}								
							}							
						}
						
						if ( subj != null )	{						
							Value rowvalue = valueFactory.createLiteral(new Integer(rowCounter).toString(), valueFactory.createURI("http://www.w3.org/2001/XMLSchema#int"));
					        triplifiedTableWrap.add(subj, propertyRow, rowvalue);
							
					        for (String[] globalPropertyValueTriple : fixedPropertyValueTypeTriples) {
								triplifiedTableWrap.add(
										subj, 
										valueFactory.createURI(globalPropertyValueTriple[0]),
										valueFactory.createLiteral(globalPropertyValueTriple[1], valueFactory.createURI(globalPropertyValueTriple[2])));
							}							
						}				        
					}
				        
					if ( (rowCounter % 1000) == 1 )	{
						LOG.debug("Row number " + rowCounter + " of sheet " + i + " processed.");
					}
					
					if (context.canceled()) {
			       		LOG.info("DPU cancelled");			       		
			       		return;
			       	}					
				}				
				LOG.debug("All " + dataEndAtRow + " rows of sheet " + i + " processed.");			
			}			
			LOG.debug("All sheets processed.");
		} catch (IOException e)	{
			context.sendMessage(MessageType.ERROR, "I/O exception when creating a workbook", 
					"I/O exception when creating a workbook from the file with a table " + tableFile.getName() + ".", e);
		} catch (InvalidFormatException e)	{
			context.sendMessage(MessageType.ERROR, "Invalid format of the file with a table " + tableFile.getName() + " (it is not a XLS or XLSX file).");
		} catch (IllegalArgumentException e)	{
			context.sendMessage(MessageType.ERROR, e.getMessage());
		}
		
		triplifiedTableWrap.flushBuffer();
	}
	
	@Override
	public void execute(DPUContext context) throws DPUException,
			DataUnitException {
						
		// do we found at least one file?
		boolean fileFound = false;
		
		// extract from all files				
		Iterator<Handler> iter = tableFiles.getRootDir().getFlatIterator();
		while(iter.hasNext()) {
			Handler handler = iter.next();
			if ( handler instanceof FileHandler ) {
				fileFound = true;
				FileHandler fileHandler = (FileHandler) handler;
				context.sendMessage(MessageType.INFO, "Processing file " + handler.getName(), 
						"Processing file " + handler.getRootedPath());
				// export data
				processTabularFile(fileHandler.asFile(), context);
			}				
		}

		
		if (!fileFound)	{
			context.sendMessage(MessageType.ERROR, "No file found in the input file data unit.");
		}				
		
	}
	
	/**
	 * Gets the value of the cell typed with an XSD type.
	 * @param cell The cell from which the value will be extracted.
	 * @return An array of length 2 - 0 position contains the value as a string, 1 position contains the full URI of its XSD type according to the type of the processed cell.
	 * @throws IllegalArgumentException when the cell contains a formula
	 */
	private String[] getCellValue(Cell cell) throws IllegalArgumentException	{
		String[] result = new String[2];
		result[0] = null;
		result[1] = null;
		
		switch ( cell.getCellType() )	{
		
		case Cell.CELL_TYPE_STRING:
			result[0] = cell.getStringCellValue();
			result[1] = "http://www.w3.org/2001/XMLSchema#string";
			return result;
		case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
            	Date date = cell.getDateCellValue();
            	result[0] = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss").format(date);
    			result[1] = "http://www.w3.org/2001/XMLSchema#dateTime";
    			return result;
            } else {
            	String value = (new Double(cell.getNumericCellValue())).toString();
            	try	{
            		Integer.parseInt(value);
            	} catch (NumberFormatException e)	{
            		result[0] = value; 
            		result[1] = "http://www.w3.org/2001/XMLSchema#decimal";
            		return result;
            	}
            	result[0] = value; 
        		result[1] = "http://www.w3.org/2001/XMLSchema#int";
            	return result;
            }
        case Cell.CELL_TYPE_BOOLEAN:
            if ( cell.getBooleanCellValue() )	{
            	result[0] = "true"; 
            } else {
            	result[0] = "false";
            }
            result[1] = "http://www.w3.org/2001/XMLSchema#xs:boolean";
            return result;
        case Cell.CELL_TYPE_FORMULA:
            throw new IllegalArgumentException("The cell contains a formula " + cell.getCellFormula());
		}		
		return result;
	}
	
	
}
