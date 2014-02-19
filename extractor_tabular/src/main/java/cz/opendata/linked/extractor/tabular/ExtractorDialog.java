package cz.opendata.linked.extractor.tabular;

import java.util.HashMap;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 * TODO Currently, it is not possible to configure mapping of columns to user defined RDF properties.
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	private static final long serialVersionUID = -2276716135089984872L;
	
	private VerticalLayout mainLayout;
	
    private TextField tfBaseURI;
    private Label lBaseURI = new Label("Base for URIs of extracted resources");
    
    private TextField tfColumnWithURISupplement;
    private Label lColumnWithURISupplement = new Label("Column with values supplementing the base for URIs");
    
    private TextField tfEncoding;
    private Label lEncoding = new Label("Encoding");

	public ExtractorDialog() {
		super(ExtractorConfig.class);
		buildMainLayout();
        setCompositionRoot(this.mainLayout);
	}
	
	private VerticalLayout buildMainLayout() {

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setImmediate(false);
		this.mainLayout.setWidth("100%");
		this.mainLayout.setHeight("100%");
		this.mainLayout.setMargin(false);

        this.setWidth("100%");
        this.setHeight("100%");
        
        this.mainLayout.addComponent(this.lBaseURI);
        this.tfBaseURI = new TextField();
        this.tfBaseURI.setWidth("100%");
        this.mainLayout.addComponent(this.tfBaseURI);
        
        this.mainLayout.addComponent(this.lColumnWithURISupplement);
        this.tfColumnWithURISupplement = new TextField();
        this.tfColumnWithURISupplement.setWidth("100%");
        this.mainLayout.addComponent(this.tfColumnWithURISupplement);
        
        this.mainLayout.addComponent(this.lEncoding);
        this.tfEncoding = new TextField();
        this.tfEncoding.setWidth("100%");
        this.mainLayout.addComponent(this.tfEncoding);
        
        return this.mainLayout;
        
	}

	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		this.tfBaseURI.setValue(conf.getBaseURI());
		this.tfColumnWithURISupplement.setValue(conf.getColumnWithURISupplement());
		this.tfEncoding.setValue(conf.getEncoding());
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		//TODO Validate filled values.
		return new ExtractorConfig(new HashMap<String, String>(), this.tfBaseURI.getValue(), this.tfColumnWithURISupplement.getValue(), this.tfEncoding.getValue());
	}

}
