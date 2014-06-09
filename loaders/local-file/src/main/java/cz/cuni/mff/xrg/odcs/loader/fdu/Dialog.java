package cz.cuni.mff.xrg.odcs.loader.fdu;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 *
 * @author Škoda Petr
 */
public class Dialog extends BaseConfigDialog <Configuration> {
	
	private VerticalLayout mainLayout;
		
	private TextField txtDestination;
	
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
	
		txtDestination = new TextField();
		txtDestination.setWidth("100%");
		txtDestination.setHeight("100%");
		txtDestination.setCaption("Target directory:");
		txtDestination.setRequired(true);
		txtDestination.setNullRepresentation("");
		mainLayout.addComponent(txtDestination);
				
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration conf) throws ConfigException {
		txtDestination.setValue(conf.getDestination());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtDestination.isValid()) {
			throw new ConfigException("Target must be filled.");
		}
		
		Configuration conf = new Configuration();
		conf.setDestination(txtDestination.getValue());
		return conf;		
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Load data into ");
		desc.append(txtDestination.getValue());
		
		return desc.toString();
	}
		
}
