package cz.opendata.linked.extractor.tabular.czso.vdb;

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
public class CZSOVDBExtractorDialog extends BaseConfigDialog<CZSOVDBExtractorConfig> {
	
	private static final long serialVersionUID = -3104734516557662861L;

	private VerticalLayout mainLayout;
	
	private FormLayout baseFormLayout;
	
	private GridLayout propertiesGridLayout;
	
    private TextField tfBaseURI;
    
    private TextField tfColumnWithURISupplement;
    
    private TextField tfDataStartAtRow;
    
	public CZSOVDBExtractorDialog() {
		super(CZSOVDBExtractorConfig.class);
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
        
        this.tfDataStartAtRow = new TextField("First data row number");
        this.baseFormLayout.addComponent(this.tfDataStartAtRow);
        
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
	
	private void addColumnToPropertyMapping(Integer columnNumber, String propertyURI)	{
		
		TextField tfColumnName = new TextField();
		this.propertiesGridLayout.addComponent(tfColumnName);
        tfColumnName.setWidth("100%");
        
        TextField tfPropertyURI = new TextField();
        this.propertiesGridLayout.addComponent(tfPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        if ( columnNumber.intValue() >= 0 )	{
        	tfColumnName.setValue(columnNumber.toString());
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
	public void setConfiguration(CZSOVDBExtractorConfig conf) throws ConfigException {
		
		if ( conf.getBaseURI() == null )	{
			this.tfBaseURI.setValue("");
		} else {
			this.tfBaseURI.setValue(conf.getBaseURI());
		}
		
		if ( conf.getColumnWithURISupplement() == -1 )	{
			this.tfColumnWithURISupplement.setValue("");
		} else {
			this.tfColumnWithURISupplement.setValue(new Integer(conf.getColumnWithURISupplement()).toString());
		}
		
		if ( conf.getDataStartAtRow() <= 0 )	{
			this.tfDataStartAtRow.setValue("0");
		} else {
			this.tfDataStartAtRow.setValue(new Integer(conf.getDataStartAtRow()).toString());
		}
		
		if ( conf.getColumnPropertyMap() != null )	{
			
			LinkedHashMap<Integer, String> columnPropertyMap = conf.getColumnPropertyMap();
			
			this.removeAllColumnToPropertyMappings();
			
			// add mappings
			for (Integer key : columnPropertyMap.keySet()) {
				this.addColumnToPropertyMapping(key, columnPropertyMap.get(key));
			}
			
			// add one empty mapping
			this.addColumnToPropertyMapping(null, null);
			
		}
	}

	@Override
	public CZSOVDBExtractorConfig getConfiguration() throws ConfigException {
		//TODO Validate filled values.
		
		LinkedHashMap<Integer, String> columnPropertiesMap = new LinkedHashMap<Integer, String>();
		
		// the first row is heading of the mapping table!
		for ( int row = 1; row < this.propertiesGridLayout.getRows(); row++ )	{
			try {
				Integer columnNumber = new Integer(((TextField)this.propertiesGridLayout.getComponent(0, row)).getValue());
				String propertyURI = ((TextField)this.propertiesGridLayout.getComponent(1, row)).getValue();
				
				if ( columnNumber != null && columnNumber.intValue() >= 0 && propertyURI != null && propertyURI.length() > 0 )	{
					columnPropertiesMap.put(columnNumber, propertyURI);
				}
			} catch (NumberFormatException ex)	{
				//TODO if the value filled as the column number cannot be parsed as an Integer then tell something to the user.
			}
		}
		
		String baseURI = this.tfBaseURI.getValue();
		if ( baseURI == null || baseURI.length() == 0)	{
			baseURI = null;
		}
		
		
		int columnWithURISupplement;
		try	{
			columnWithURISupplement = new Integer(this.tfColumnWithURISupplement.getValue()).intValue();
			if ( columnWithURISupplement < 0 )	{
				columnWithURISupplement = -1;
			}
		} catch (NumberFormatException ex)	{
			//TODO if the value filled as the column with URI supplement number cannot be parsed as an Integer then tell something to the user.
			columnWithURISupplement = -1;
		}
		
		Integer dataStartAtRow;
		try	{
			dataStartAtRow = new Integer(this.tfDataStartAtRow.getValue());
			if ( dataStartAtRow < 0 )	{
				dataStartAtRow = null;
			}
		} catch (NumberFormatException ex)	{
			//TODO if the value filled as the column with URI supplement number cannot be parsed as an Integer then tell something to the user.
			dataStartAtRow = 0;
		}
				
		return new CZSOVDBExtractorConfig(columnPropertiesMap, baseURI, columnWithURISupplement, dataStartAtRow);
	}

}
