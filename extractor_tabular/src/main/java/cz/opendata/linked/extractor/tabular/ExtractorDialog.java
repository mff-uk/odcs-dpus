package cz.opendata.linked.extractor.tabular;

import java.util.LinkedHashMap;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
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
	
    private TextField tfBaseURI;
    
    private TextField tfColumnWithURISupplement;

    private TextField tfEncoding;
    
	public ExtractorDialog() {
		super(ExtractorConfig.class);
		buildMainLayout();
        setCompositionRoot(this.mainLayout);
	}
	
	private VerticalLayout buildMainLayout() {

		this.setWidth("100%");
        this.setHeight("100%");
		
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setImmediate(false);
		this.mainLayout.setWidth("100%");
		this.mainLayout.setHeight("100%");
		this.mainLayout.setMargin(false);

		this.baseFormLayout = new FormLayout();
		this.baseFormLayout.setSizeUndefined();
				
        this.tfBaseURI = new TextField("Resource URI base");
        this.baseFormLayout.addComponent(this.tfBaseURI);
        tfBaseURI.setRequired(true);
        tfBaseURI.setRequiredError("Resource URI base must be supplied.");
        
        this.tfColumnWithURISupplement = new TextField("Key column");
        this.baseFormLayout.addComponent(this.tfColumnWithURISupplement);
        
        this.tfEncoding = new TextField("Encoding");
        this.baseFormLayout.addComponent(this.tfEncoding);
        
        this.mainLayout.addComponent(this.baseFormLayout);
        
        this.mainLayout.addComponent(new Label("Column to property URI mappings"));
        
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
		//return new ExtractorConfig(new LinkedHashMap<String, String>(), this.tfBaseURI.getValue(), this.tfColumnWithURISupplement.getValue(), this.tfEncoding.getValue());
		
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
		
		return new ExtractorConfig(columnPropertiesMap, baseURI, columnWithURISupplement, encoding);
	}

}
