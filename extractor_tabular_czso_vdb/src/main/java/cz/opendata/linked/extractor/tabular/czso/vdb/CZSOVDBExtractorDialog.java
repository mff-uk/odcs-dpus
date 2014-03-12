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
	
	private GridLayout fixedValueMapGridLayout;
	
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
		this.mainLayout.setHeight("-1px");
		this.mainLayout.setMargin(false);

		this.baseFormLayout = new FormLayout();
		this.baseFormLayout.setSizeUndefined();
				
        this.tfBaseURI = new TextField("Resource URI base");
        this.baseFormLayout.addComponent(this.tfBaseURI);
        
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
        
        this.addPropertyMappingsHeading();
        
        TextField tfColumnNumber = new TextField();
        this.propertiesGridLayout.addComponent(tfColumnNumber);
        tfColumnNumber.setWidth("100%");
        
        TextField tfPropertyURI = new TextField();
        this.propertiesGridLayout.addComponent(tfPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        this.mainLayout.addComponent(this.propertiesGridLayout);
        
        Button bAddRowToPropertyMapping = new Button("Add mapping");
        bAddRowToPropertyMapping.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -8609995802749728232L;

			@Override
			public void buttonClick(ClickEvent event) {
				addRowToPropertyMapping(null, null);
			}
		});
        this.mainLayout.addComponent(bAddRowToPropertyMapping);
        
        this.mainLayout.addComponent(new Label("Cell to property URI with fixed value mappings"));
        
        this.fixedValueMapGridLayout = new GridLayout(3,2);
        this.fixedValueMapGridLayout.setWidth("100%");
        this.fixedValueMapGridLayout.setColumnExpandRatio(0, 1);
        this.fixedValueMapGridLayout.setColumnExpandRatio(1, 1);
        this.fixedValueMapGridLayout.setColumnExpandRatio(2, 5);
        
        this.addFixedValueMappingsHeading();
        
        TextField tfFixedValueMappingColumnNumber = new TextField();
        this.fixedValueMapGridLayout.addComponent(tfFixedValueMappingColumnNumber);
        tfColumnNumber.setWidth("100%");
        
        TextField tfFixedValueMappingRowNumber = new TextField();
        this.fixedValueMapGridLayout.addComponent(tfFixedValueMappingRowNumber);
        tfColumnNumber.setWidth("100%");
        
        TextField tfFixedValueMappingPropertyURI = new TextField();
        this.fixedValueMapGridLayout.addComponent(tfFixedValueMappingPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        this.mainLayout.addComponent(this.fixedValueMapGridLayout);
        
        Button bAddRowToFixedValueMapping = new Button("Add mapping");
        bAddRowToFixedValueMapping.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -8609995802749728232L;

			@Override
			public void buttonClick(ClickEvent event) {
				addRowToFixedValueMapping(null, null, null);
			}
		});
        this.mainLayout.addComponent(bAddRowToFixedValueMapping);
        
        
        return this.mainLayout;
        
	}
	
	private void addRowToPropertyMapping(Integer columnNumber, String propertyURI)	{
		
		TextField tfColumnName = new TextField();
		this.propertiesGridLayout.addComponent(tfColumnName);
        tfColumnName.setWidth("100%");
        
        TextField tfPropertyURI = new TextField();
        this.propertiesGridLayout.addComponent(tfPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        if ( columnNumber != null && columnNumber.intValue() >= 0 )	{
        	tfColumnName.setValue(columnNumber.toString());
        }
        
        if ( propertyURI != null )	{
        	tfPropertyURI.setValue(propertyURI);
        }
	}
	
	private void removeAllColumnToPropertyMappings()	{
		this.propertiesGridLayout.removeAllComponents();
		this.addPropertyMappingsHeading();
	}
	
	private void addPropertyMappingsHeading()	{
		this.propertiesGridLayout.addComponent(new Label("Column number"));
        this.propertiesGridLayout.addComponent(new Label("Property URI"));
	}
	
	private void addRowToFixedValueMapping(Integer columnNumber, Integer rowNumber, String propertyURI)	{
		
		TextField tfColumnNumber = new TextField();
		this.fixedValueMapGridLayout.addComponent(tfColumnNumber);
        tfColumnNumber.setWidth("100%");
        
        TextField tfRowNumber = new TextField();
		this.fixedValueMapGridLayout.addComponent(tfRowNumber);
		tfRowNumber.setWidth("100%");
        
        TextField tfPropertyURI = new TextField();
        this.fixedValueMapGridLayout.addComponent(tfPropertyURI);
        tfPropertyURI.setWidth("100%");
        
        if ( columnNumber != null && columnNumber.intValue() >= 0 )	{
        	tfColumnNumber.setValue(columnNumber.toString());
        }
        
        if ( rowNumber != null && rowNumber.intValue() >= 0 )	{
        	tfRowNumber.setValue(rowNumber.toString());
        }
        
        if ( propertyURI != null )	{
        	tfPropertyURI.setValue(propertyURI);
        }
	}
	
	private void removeAllFixedValueMappings()	{
		this.fixedValueMapGridLayout.removeAllComponents();
		this.addFixedValueMappingsHeading();
	}
	
	private void addFixedValueMappingsHeading()	{
		this.fixedValueMapGridLayout.addComponent(new Label("Column number"));
		this.fixedValueMapGridLayout.addComponent(new Label("Row number"));
        this.fixedValueMapGridLayout.addComponent(new Label("Property URI"));
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
				this.addRowToPropertyMapping(key, columnPropertyMap.get(key));
			}
			
			// add one empty mapping
			this.addRowToPropertyMapping(null, null);
			
		}
		
		if ( conf.getFixedValueMap() != null )	{

			LinkedHashMap<Coordinates, String> fixedValueMap = conf.getFixedValueMap();
			
			this.removeAllFixedValueMappings();
			
			// add mappings
			for ( Coordinates key : fixedValueMap.keySet() )	{
				this.addRowToFixedValueMapping(key.column, key.row, fixedValueMap.get(key));
			}
			
			// add one empty mapping
			this.addRowToFixedValueMapping(null, null, null);
			
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
		
		LinkedHashMap<Coordinates, String> fixedValueMap = new LinkedHashMap<Coordinates, String>();
		// the first row is heading of the mapping table!
		for ( int row = 1; row < this.fixedValueMapGridLayout.getRows(); row++ )	{
			try {
				
				Integer columnNumber = new Integer(((TextField)this.fixedValueMapGridLayout.getComponent(0, row)).getValue());
				Integer rowNumber = new Integer(((TextField)this.fixedValueMapGridLayout.getComponent(1, row)).getValue());
				String propertyURI = ((TextField)this.fixedValueMapGridLayout.getComponent(2, row)).getValue();
				
				if ( columnNumber != null && columnNumber.intValue() >= 0 && rowNumber != null && rowNumber.intValue() >= 0 && propertyURI != null && propertyURI.length() > 0 )	{
				
					Coordinates coordinates = new Coordinates(columnNumber, rowNumber);
					fixedValueMap.put(coordinates, propertyURI);

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
				
		return new CZSOVDBExtractorConfig(columnPropertiesMap, fixedValueMap, baseURI, columnWithURISupplement, dataStartAtRow);
	}

}
