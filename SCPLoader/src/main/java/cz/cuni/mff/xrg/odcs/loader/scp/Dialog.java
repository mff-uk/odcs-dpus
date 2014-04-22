package cz.cuni.mff.xrg.odcs.loader.scp;

import com.vaadin.ui.PasswordField;
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
	
	private TextField txtHost;
	
	private TextField txtPort;
	
	private TextField txtUser;
	
	private PasswordField txtPassword;
	
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

		txtHost = new TextField();
		txtHost.setWidth("100%");
		txtHost.setHeight("-1px");
		txtHost.setCaption("Host:");
		txtHost.setRequired(true);
		mainLayout.addComponent(txtHost);

		txtPort = new TextField();
		txtPort.setWidth("100%");
		txtPort.setHeight("-1px");
		txtPort.setCaption("Port:");
		txtPort.setRequired(true);
		mainLayout.addComponent(txtPort);
		
		txtUser = new TextField();
		txtUser.setWidth("100%");
		txtUser.setHeight("-1px");
		txtUser.setCaption("Username:");
		txtUser.setRequired(true);
		mainLayout.addComponent(txtUser);
		
		txtPassword = new PasswordField();
		txtPassword.setWidth("100%");
		txtPassword.setHeight("-1px");
		txtPassword.setCaption("Password:");
		txtPassword.setRequired(true);
		txtPassword.setNullRepresentation("");
		mainLayout.addComponent(txtPassword);
		
		txtDestination = new TextField();
		txtDestination.setWidth("100%");
		txtDestination.setHeight("-1px");
		txtDestination.setCaption("Destination: (must not end with '/')");
		txtDestination.setRequired(true);
		txtDestination.setNullRepresentation("");
		mainLayout.addComponent(txtDestination);
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration c) throws ConfigException {
		txtHost.setValue(c.getHostname());
		txtPort.setValue(c.getPort().toString());
		txtUser.setValue(c.getUsername());
		txtPassword.setValue(c.getPassword());
		txtDestination.setValue(c.getDestination());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtHost.isValid()) {
			throw new ConfigException("Output file name must be specified!");
		}
		
		Configuration cnf = new Configuration();
		cnf.setHostname(txtHost.getValue());
		try {
			cnf.setPort(Integer.parseInt(txtPort.getValue()));
		} catch (NumberFormatException e) {
			throw new ConfigException("Port must be a number.");
		}
		cnf.setUsername(txtUser.getValue());
		cnf.setPassword(txtPassword.getValue());
		
		// update path
		String dest = txtDestination.getValue().replace('\\', '/');
		if (dest.endsWith("/")) {
			dest = dest.substring(0, dest.length() - 1);
		}		
		cnf.setDestination(dest);
		
		return cnf;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Upload into ");
		desc.append(txtUser.getValue());
		desc.append("@");
		desc.append(txtHost.getValue());
		desc.append(":");
		desc.append(txtDestination.getValue());
		
		return desc.toString();
	}
	
}
