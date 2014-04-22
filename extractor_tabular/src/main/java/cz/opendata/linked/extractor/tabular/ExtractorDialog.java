package cz.opendata.linked.extractor.tabular;

import com.vaadin.ui.*;
import java.util.LinkedHashMap;

import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button.ClickEvent;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	private static final long serialVersionUID = -2276716135089984872L;
	
	private VerticalLayout mainLayout;
	
	private FormLayout baseFormLayout;
	
	private GridLayout propertiesGridLayout;
	
	private OptionGroup ogInputFileType;
	
    private TextField tfBaseURI;
    
    private TextField tfColumnWithURISupplement;

    private TextField tfEncoding;
    
    private TextField tfQuoteChar;
    
    private TextField tfDelimiterChar;
    
    private TextField tfEOFSymbols;
    
	public ExtractorDialog() {
		super(ExtractorConfig.class);
		buildMainLayout();
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(mainLayout);
		setCompositionRoot(panel);
	}
	
	private VerticalLayout buildMainLayout() {

		this.setWidth("100%");
        this.setHeight("100%");
		
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setImmediate(false);
		this.mainLayout.setWidth("100%");
		this.mainLayout.setHeight("-1px");
		this.mainLayout.setMargin(false);

		this.baseFormLayout = new FormLayout();
		this.baseFormLayout.setSizeUndefined();
				
		this.ogInputFileType = new OptionGroup("Choose the input type:");
		this.ogInputFileType.addItem("CSV");
		this.ogInputFileType.addItem("DBF");
		this.ogInputFileType.setValue("CSV");
		this.baseFormLayout.addComponent(this.ogInputFileType);
		
        this.tfBaseURI = new TextField("Resource URI base");
        this.baseFormLayout.addComponent(this.tfBaseURI);
        tfBaseURI.setRequired(true);
        tfBaseURI.setRequiredError("Resource URI base must be supplied.");
        
        this.tfColumnWithURISupplement = new TextField("Key column");
        this.baseFormLayout.addComponent(this.tfColumnWithURISupplement);
        
        this.tfEncoding = new TextField("Encoding (for DBF)");
        this.baseFormLayout.addComponent(this.tfEncoding);
        
        this.tfQuoteChar = new TextField("Quote char (for CSV)");
        this.baseFormLayout.addComponent(this.tfQuoteChar);
        
        this.tfDelimiterChar = new TextField("Delimiter char (for CSV)");
        this.baseFormLayout.addComponent(this.tfDelimiterChar);
        
        this.tfEOFSymbols = new TextField("End of line symbols (for CSV)");
        this.baseFormLayout.addComponent(this.tfEOFSymbols);
        
        this.baseFormLayout.addComponent(new Label("Column to property URI mappings"));
        
        this.mainLayout.addComponent(this.baseFormLayout);
        
        this.propertiesGridLayout = new GridLayout(2,2);
        this.propertiesGridLayout.setWidth("100%");
        this.propertiesGridLayout.setColumnExpandRatio(0, 1);
        this.propertiesGridLayout.setColumnExpandRatio(1, 6);
        
        this.addColumnToPropertyMappingsHeading();
        
        TextField tfColumnName = new TextField();
        this.propertiesGridLayout.addComponent(tfColumnName);
        tfColumnName.setWidth("100%");
        
        TextField tfPropertyURI = new TextField();
        this.propertiesGridLayout.addComponent(tfPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        this.mainLayout.addComponent(this.propertiesGridLayout);
        
        Button bAddColumnToPropertyMapping = new Button("Add mapping");
        bAddColumnToPropertyMapping.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -8609995802749728232L;

			@Override
			public void buttonClick(ClickEvent event) {
				addColumnToPropertyMapping(null, null);
			}
		});
        this.mainLayout.addComponent(bAddColumnToPropertyMapping);
        
        return this.mainLayout;
        
	}
	
	private void addColumnToPropertyMapping(String columnName, String propertyURI)	{
		//int rowCount = this.propertiesGridLayout.getRows();
		
		TextField tfColumnName = new TextField();
        //this.propertiesGridLayout.addComponent(tfColumnName, 0, rowCount);
		this.propertiesGridLayout.addComponent(tfColumnName);
        tfColumnName.setWidth("100%");
        
        TextField tfPropertyURI = new TextField();
        //this.propertiesGridLayout.addComponent(tfPropertyURI, 1, rowCount);
        this.propertiesGridLayout.addComponent(tfPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        if ( columnName != null )	{
        	tfColumnName.setValue(columnName);
        }
        
        if ( propertyURI != null )	{
        	tfPropertyURI.setValue(propertyURI);
        }
	}
	
	private void removeAllColumnToPropertyMappings()	{
		this.propertiesGridLayout.removeAllComponents();
		this.addColumnToPropertyMappingsHeading();
	}
	
	private void addColumnToPropertyMappingsHeading()	{
		this.propertiesGridLayout.addComponent(new Label("Column name"));
        this.propertiesGridLayout.addComponent(new Label("Property URI"));
	}

	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		
		if ( conf.getBaseURI() == null )	{
			this.tfBaseURI.setValue("");
		} else {
			this.tfBaseURI.setValue(conf.getBaseURI());
		}
		
		if ( conf.getColumnWithURISupplement() == null )	{
			this.tfColumnWithURISupplement.setValue("");
		} else {
			this.tfColumnWithURISupplement.setValue(conf.getColumnWithURISupplement());
		}
		
		if ( conf.getEncoding() == null )	{
			this.tfEncoding.setValue("");
		} else {
			this.tfEncoding.setValue(conf.getEncoding());
		}
		
		if ( conf.getQuoteChar() == null )	{
			this.tfQuoteChar.setValue("");
		} else {
			this.tfQuoteChar.setValue(conf.getQuoteChar());
		}
		
		if ( conf.getDelimiterChar() == null )	{
			this.tfDelimiterChar.setValue("");
		} else {
			this.tfDelimiterChar.setValue(conf.getDelimiterChar());
		}
		
		if ( conf.getEofSymbols() == null )	{
			this.tfEOFSymbols.setValue("");
		} else {
			this.tfEOFSymbols.setValue(conf.getEofSymbols());
		}
		
		if ( conf.isDBF() == true )	{
			this.ogInputFileType.setValue("DBF");
		} else {
			this.ogInputFileType.setValue("CSV");
		}
		
		if ( conf.getColumnPropertyMap() != null )	{
			
			LinkedHashMap<String, String> columnPropertyMap = conf.getColumnPropertyMap();
			
			this.removeAllColumnToPropertyMappings();
			
			// add mappings
			for (String key : columnPropertyMap.keySet()) {
				this.addColumnToPropertyMapping(key, columnPropertyMap.get(key));
			}
			
			// add one empty mapping
			this.addColumnToPropertyMapping(null, null);
			
		}
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		//TODO Validate filled values.
		
		LinkedHashMap<String, String> columnPropertiesMap = new LinkedHashMap<String, String>();
		
		// the first row is heading !
		for ( int row = 1; row < this.propertiesGridLayout.getRows(); row++ )	{
			
			String columnName = ((TextField)this.propertiesGridLayout.getComponent(0, row)).getValue();
			String propertyURI = ((TextField)this.propertiesGridLayout.getComponent(1, row)).getValue();
			
			if ( columnName != null && columnName.length() > 0 && propertyURI != null && propertyURI.length() > 0 )	{
				columnPropertiesMap.put(columnName, propertyURI);
			}
			
		}
		
		String baseURI = this.tfBaseURI.getValue();
		if ( baseURI == null || baseURI.length() == 0)	{
			baseURI = null;
		}
		
		String columnWithURISupplement = this.tfColumnWithURISupplement.getValue();
		if ( columnWithURISupplement == null || columnWithURISupplement.length() == 0)	{
			columnWithURISupplement = null;
		}
		
		String encoding = this.tfEncoding.getValue();
		if ( encoding == null || encoding.length() == 0)	{
			encoding = null;
		}
		
		String inputFileType = (String) this.ogInputFileType.getValue();
		boolean isDBF = false;
		boolean isCSV = false;
		if ( "DBF".equals(inputFileType) )	{
			isDBF = true;
		} else {
			isCSV = true;
		}
		
		String quoteChar = this.tfQuoteChar.getValue();
		if ( quoteChar == null || quoteChar.length() == 0)	{
			quoteChar = null;
		} else {
			quoteChar = quoteChar.substring(0, 1);
		}
		
		String delimiterChar = this.tfDelimiterChar.getValue();
		if ( delimiterChar == null || delimiterChar.length() == 0)	{
			delimiterChar = null;
		} else {
			delimiterChar = delimiterChar.substring(0, 1);
		}
		
		String eofSymbols = this.tfEOFSymbols.getValue();
		if ( eofSymbols == null || eofSymbols.length() == 0)	{
			eofSymbols = null;
		}
		
		return new ExtractorConfig(columnPropertiesMap, baseURI, columnWithURISupplement, encoding, quoteChar, delimiterChar, eofSymbols, isDBF, isCSV);
	}

}
