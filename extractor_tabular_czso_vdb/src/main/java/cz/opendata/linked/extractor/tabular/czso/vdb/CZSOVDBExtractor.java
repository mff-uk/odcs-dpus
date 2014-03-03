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
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

@AsExtractor
public class CZSOVDBExtractor extends ConfigurableBase<CZSOVDBExtractorConfig> implements
		ConfigDialogProvider<CZSOVDBExtractorConfig> {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(CZSOVDBExtractor.class);
	
	private final String baseODCSPropertyURI = "http://linked.opendata.cz/ontology/odcs/tabular/";

	@InputDataUnit(name = "table")
	public FileDataUnit tableFile;
	
	@OutputDataUnit(name = "triplifiedTable")
	public RDFDataUnit triplifiedTable;

	public CZSOVDBExtractor() {
		super(CZSOVDBExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<CZSOVDBExtractorConfig> getConfigurationDialog() {
		return new CZSOVDBExtractorDialog();
	}

	private void processTabularFile(File tableFile, DPUContext context)	{
		
		if ( tableFile == null )	{
			context.sendMessage(MessageType.ERROR, "No file found in the input file data unit.");
        	return;
		}
		
		String tableFileName = tableFile.getName();
		
		LinkedHashMap<Integer, String> propertyMap = this.config.getColumnPropertyMap();
		if ( propertyMap == null )	{
			log.warn("No mapping of table columns to RDF properties has been specified.");
			propertyMap = new LinkedHashMap<Integer, String>();
		}
		LinkedHashMap<Integer[], String> dimensionValueMap = this.config.getDimensionValueMap();
		HashMap<Integer, HashMap<Integer, String>> optimizedDimensionValueMap = new HashMap<Integer, HashMap<Integer, String>>(); 
		if ( dimensionValueMap == null )	{
			log.warn("No mapping of cells to dimension value properties has been specified.");
		} else {
			for (Integer[] coordinates : dimensionValueMap.keySet()) {
				if ( coordinates != null )	{
					Integer row = new Integer(coordinates[0]);
					Integer column = new Integer(coordinates[1]);
					
					HashMap<Integer, String> rowMap;
					if ( optimizedDimensionValueMap.containsKey(row) )	{
						rowMap = optimizedDimensionValueMap.get(row);
					} else {
						rowMap = new HashMap<Integer, String>();
						optimizedDimensionValueMap.put(row, rowMap);
					}
					rowMap.put(column, dimensionValueMap.get(coordinates));
				}
			}
		}
		ArrayList<String[]> globalPropertyValueTypeTriples = new ArrayList<String[]>();
		String baseURI = this.config.getBaseURI();
		if ( baseURI == null || "".equals(baseURI) )	{
			log.warn("No base for URIs of resources extracted from rows of the table has been specified. Default base will be applied (http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/)");
			baseURI = "http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/";
		}
		int columnWithURISupplement = this.config.getColumnWithURISupplement();
		if ( columnWithURISupplement < 0 )	{
			log.warn("No column with values supplementing the base for URIs of resources extracted from rows of the table has been specified. Row number (starting at 0) will be used instead.");
			columnWithURISupplement = -1;
		}
		int dataStartAtRow = this.config.getDataStartAtRow();
		if ( dataStartAtRow < 0 )	{
			log.warn("Row where the data start was not specified. Zero is used instead.");
			columnWithURISupplement = 0;
		}
		
		try	{
		
			Workbook wb = WorkbookFactory.create(tableFile);
			Sheet sheet = wb.getSheetAt(0);
			
			int dataEndAtRow = sheet.getLastRowNum();
		
			URI propertyRow = triplifiedTable.createURI(baseODCSPropertyURI + "row");
			
			for ( int rowCounter = 0; rowCounter <= dataEndAtRow; rowCounter++ )	{
				
				Row row = sheet.getRow(rowCounter);
				
				if ( row != null)	{
				
					Resource subj = null;
					HashMap<Integer, String> rowMapWithDimensionValues = optimizedDimensionValueMap.get(new Integer(rowCounter));
					
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
						
						subj = triplifiedTable.createURI(baseURI + suffixURI);
						
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
								
								if ( value[0] == null || "".equals(value[0]) )	{
									URI obj = triplifiedTable.createURI("http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
									triplifiedTable.addTriple(subj, triplifiedTable.createURI(propertyURI), obj);
								} else {
							        Value obj = triplifiedTable.createLiteral(value[0], triplifiedTable.createURI(value[1]));
							        triplifiedTable.addTriple(subj, triplifiedTable.createURI(propertyURI), obj);
								}
								
							}
							
							if ( value[0] != null && !"".equals(value[0]) && rowMapWithDimensionValues != null )	{
								
								String propertyURI = rowMapWithDimensionValues.get(key);
								if ( propertyURI != null && !"".equals(propertyURI) )	{
									
									String[] globalPropertyValueTriple = new String[3];
									globalPropertyValueTriple[0] = propertyURI;
									globalPropertyValueTriple[1] = value[0];
									globalPropertyValueTriple[2] = value[1];
									globalPropertyValueTypeTriples.add(globalPropertyValueTriple);
									
								}
								
							}
							
						}
						
					}
					
					if ( subj != null )	{
					
						Value rowvalue = triplifiedTable.createLiteral(new Integer(rowCounter).toString(), triplifiedTable.createURI("http://www.w3.org/2001/XMLSchema#int"));
				        triplifiedTable.addTriple(subj, propertyRow, rowvalue);
				        
				        for (String[] globalPropertyValueTriple : globalPropertyValueTypeTriples) {
							triplifiedTable.addTriple(subj, triplifiedTable.createURI(globalPropertyValueTriple[0]), triplifiedTable.createLiteral(globalPropertyValueTriple[1], triplifiedTable.createURI(globalPropertyValueTriple[2])));
						}
				        
					}
			        
				}
			        
//				if ( (rowCounter % 1000) == 0 )	{
					log.debug("Row number " + rowCounter + " processed.");
//				}
				
				if (context.canceled()) {
		       		log.info("DPU cancelled");
		       		
		       		return;
		       	}
				
			}
			
		} catch (IOException e)	{
			context.sendMessage(MessageType.ERROR, "I/O exception when creating a workbook from the file with a table " + tableFile.getName() + ".");
        	return;
		} catch (InvalidFormatException e)	{
			context.sendMessage(MessageType.ERROR, "Invalid format of the file with a table " + tableFile.getName() + " (it is not a XLS or XLSX file).");
        	return;
		} catch (IllegalArgumentException e)	{
			context.sendMessage(MessageType.ERROR, e.getMessage());
        	return;
		}
		
	}
	
	@Override
	public void execute(DPUContext context) throws DPUException,
			DataUnitException {
		
		DirectoryHandler rootHandler = tableFile.getRootDir();
		File tableFile = null;
		for (Handler handler : rootHandler) {
			if ( handler instanceof FileHandler )	{
				FileHandler fileHandler = (FileHandler) handler;
				tableFile = fileHandler.asFile();
				this.processTabularFile(tableFile, context);
			}
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
