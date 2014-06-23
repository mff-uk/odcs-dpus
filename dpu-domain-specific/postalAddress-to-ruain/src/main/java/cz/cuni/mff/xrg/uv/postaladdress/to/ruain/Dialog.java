package cz.cuni.mff.xrg.uv.postaladdress.to.ruain;

import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

public class Dialog extends BaseConfigDialog<Configuration> {
	
	private VerticalLayout mainLayout;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}
	
	private void buildMainLayout() {
		setWidth("100%");
		setHeight("100%");

		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		
		
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration c) throws ConfigException {
		
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		Configuration cnf = new Configuration();
		
		return cnf;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
				
		return desc.toString();
	}

}
