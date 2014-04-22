package cz.cuni.mff.xrg.odcs.transformer.zipper;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * Configuration dialog.
 * 
 * @author Škoda Petr
 */
public class Dialog extends BaseConfigDialog<Configuration> {
	
	private VerticalLayout mainLayout;

	private TextField txtFileName;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}
	
	private void buildMainLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		
		// top-level component properties
		setWidth("100%");
		setHeight("100%");
				
		txtFileName = new TextField();
		txtFileName.setWidth("100%");
		txtFileName.setHeight("-1px");
		txtFileName.setCaption("Output file name: (without .zip extension)");
		txtFileName.setRequired(true);
		mainLayout.addComponent(txtFileName);
		
		mainLayout.addComponent(new Label("Use '/' in file name to denote directory."));
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration c) throws ConfigException {
		txtFileName.setValue(c.getFileName());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtFileName.isValid()) {
			throw new ConfigException("Output file name must be specified!");
		}
		Configuration cnf = new Configuration();
		cnf.setFileName(getFilePath());
		return cnf;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Pack input into '");
		desc.append(getFilePath());
		desc.append(".zip");
		desc.append("'");
		
		return desc.toString();
	}
	
	/**
	 * Return file path with '/' as separators.
	 * @return 
	 */
	private String getFilePath() {
		return txtFileName.getValue().replaceAll("\\\\", "/");
	}
	
}
