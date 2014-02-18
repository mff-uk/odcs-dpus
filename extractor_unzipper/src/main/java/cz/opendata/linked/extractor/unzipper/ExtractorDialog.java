package cz.opendata.linked.extractor.unzipper;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	
	private static final long serialVersionUID = 4526219476366177231L;
	
	private VerticalLayout mainLayout;
	
    private TextField zipFileURL;
    private Label zipFileURLLabel = new Label("URL of ZIP file");
    
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
        
        this.mainLayout.addComponent(this.zipFileURLLabel);
        this.zipFileURL = new TextField();
        this.zipFileURL.setWidth("100%");
        this.mainLayout.addComponent(this.zipFileURL);
        
        return this.mainLayout;
        
	}
	
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		
		this.zipFileURL.setValue(conf.getZipFileURL());
		
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		
		return new ExtractorConfig(this.zipFileURL.getValue());
		
	}

}
