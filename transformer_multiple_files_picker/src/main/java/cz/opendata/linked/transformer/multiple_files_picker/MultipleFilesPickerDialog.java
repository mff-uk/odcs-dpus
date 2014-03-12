package cz.opendata.linked.transformer.multiple_files_picker;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class MultipleFilesPickerDialog extends BaseConfigDialog<MultipleFilesPickerConfig> {

	private static final long serialVersionUID = 4526219476366177231L;
	
	private VerticalLayout mainLayout;
	
    private TextField tfPath;
	
	public MultipleFilesPickerDialog() {
		super(MultipleFilesPickerConfig.class);
        buildMainLayout();
        setCompositionRoot(this.mainLayout);
	}

	private void buildMainLayout() {
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setImmediate(false);
		this.mainLayout.setWidth("100%");
		this.mainLayout.setHeight("-1px");
		this.mainLayout.setMargin(false);
		this.mainLayout.setSpacing(true);

        this.setWidth("100%");
        this.setHeight("100%");
        
        this.tfPath = new TextField();
		this.tfPath.setCaption("Path to file");
        this.tfPath.setSizeFull();
		
        this.mainLayout.addComponent(this.tfPath);
	}
	
	@Override
	public void setConfiguration(MultipleFilesPickerConfig conf) throws ConfigException {
		if (conf.getPath() == null) {
			this.tfPath.setValue("");
		} else {
			this.tfPath.setValue(conf.getPath());		
		}
	}

	@Override
	public MultipleFilesPickerConfig getConfiguration() throws ConfigException {		
		return new MultipleFilesPickerConfig(this.tfPath.getValue());		
	}

}
