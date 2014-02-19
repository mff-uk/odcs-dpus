package cz.opendata.linked.extractor.tabular;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfHeader;
import org.jamel.dbf.utils.DbfUtils;
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
public class Extractor extends ConfigurableBase<ExtractorConfig> implements
		ConfigDialogProvider<ExtractorConfig> {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Extractor.class);

	@InputDataUnit(name = "table")
	public FileDataUnit tableFile;
	
	@OutputDataUnit(name = "triplifiedTable")
	public RDFDataUnit triplifiedTable;

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
		
		DirectoryHandler rootHandler = tableFile.getRootDir();
		File tableFile = null;
		for (Handler handler : rootHandler) {
			if ( handler instanceof FileHandler )	{
				FileHandler fileHandler = (FileHandler) handler;
				tableFile = fileHandler.asFile();
				break;
			}
		}
		
		if ( tableFile == null )	{
			context.sendMessage(MessageType.ERROR, "No file found in the input file data unit.");
        	return;
		}
		
		String tableFileName = tableFile.getName();
		
		Map<String, String> columnPropertyMap = this.config.getColumnPropertyMap();
		if ( columnPropertyMap == null )	{
			log.warn("No mapping of table columns to RDF properties have been specified.");
			columnPropertyMap = new HashMap<String, String>();
		}
		String baseURI = this.config.getBaseURI();
		if ( baseURI == null || "".equals(baseURI) )	{
			log.warn("No base for URIs of resources extracted from rows of the table has been specified. Default base will be applied (http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/)");
			baseURI = "http://linked.opendata.cz/resource/odcs/tabular/" + tableFileName + "/row/";
		}
		String columnWithURISupplement = this.config.getColumnWithURISupplement();
		if ( columnWithURISupplement == null || "".equals(columnWithURISupplement) )	{
			log.warn("No column with values supplementing the base for URIs of resources extracted from rows of the table has been specified. Row number (starting at 0) will be used instead.");
			columnWithURISupplement = null;
		}
		
		String encoding = this.config.getEncoding();
		if ( encoding == null || "".equals(encoding) )	{
			DbfReaderLanguageDriver languageDriverReader = new DbfReaderLanguageDriver(tableFile);
			DbfHeaderLanguageDriver languageDriverHeader = languageDriverReader.getHeader();
//			byte languageDriver = languageDriverHeader.getLanguageDriver();
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
		String[] propertyMap = new String[header.getFieldsCount()];
		for ( int i = 0; i < header.getFieldsCount(); i++ )	{
			DbfField field = header.getField(i);
			String fieldName = field.getName();
			if ( columnWithURISupplement != null && columnWithURISupplement.equals(fieldName) )	{
				columnWithURISupplementNumber = i;
			}
			if ( columnPropertyMap.containsKey(fieldName) )	{
				propertyMap[i] = columnPropertyMap.get(fieldName);
			} else {
				fieldName = fieldName.replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
				propertyMap[i] = "http://linked.opendata.cz/ontology/odcs/tabular/" + fieldName;
			}
		}
		
		Object[] row = null;
		int rowno = 0;
		while ( (row = reader.nextRecord()) != null )	{
					
			String suffixURI;
			if ( columnWithURISupplementNumber >= 0 )	{
				suffixURI = this.getCellValue(row[columnWithURISupplementNumber], encoding).replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
			} else {
				suffixURI = (new Integer(rowno)).toString();
			}
			
			Resource subj = triplifiedTable.createURI(baseURI + suffixURI);
			
			for ( int i = 0; i < row.length; i++ )	{
		        
				URI pred = triplifiedTable.createURI(propertyMap[i]);				
		        Value obj = triplifiedTable.createLiteral(this.getCellValue(row[i], encoding));
		        triplifiedTable.addTriple(subj, pred, obj);
		       	
			}
			
			if ( (rowno % 1000) == 0 )	{
				log.debug("Row number " + rowno + " processed.");
			}
			rowno++;
			
			if (context.canceled()) {
	       		log.info("DPU cancelled");
	       		reader.close();
	       		return;
	       	}
			
		}
		
		reader.close();

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
	
	
	
}
