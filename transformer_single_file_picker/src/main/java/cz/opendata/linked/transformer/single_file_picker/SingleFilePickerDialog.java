package cz.opendata.linked.transformer.single_file_picker;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class SingleFilePickerDialog extends BaseConfigDialog<SingleFilePickerConfig> {

	private static final long serialVersionUID = 4526219476366177231L;
	
	private VerticalLayout mainLayout;
	
    private TextField tfPath;
    private Label lPath = new Label("Path to file");
    
	public SingleFilePickerDialog() {
		super(SingleFilePickerConfig.class);
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
        
        this.mainLayout.addComponent(this.lPath);
        this.tfPath = new TextField();
        this.tfPath.setWidth("100%");
        this.mainLayout.addComponent(this.tfPath);
        
        return this.mainLayout;
        
	}
	
	@Override
	public void setConfiguration(SingleFilePickerConfig conf) throws ConfigException {
		
		this.tfPath.setValue(conf.getPath());
		
	}

	@Override
	public SingleFilePickerConfig getConfiguration() throws ConfigException {
		
		return new SingleFilePickerConfig(this.tfPath.getValue());
		
	}

}
