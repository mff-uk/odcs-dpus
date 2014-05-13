package cz.cuni.mff.xrg.odcs.loader.ftp;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import javax.swing.text.html.ListView;

/**
 *
 * @author Škoda Petr
 */
public class Dialog extends BaseConfigDialog<Configuration> {

	private VerticalLayout mainLayout;

	private TextField txtHost;

	private TextField txtPort;

	private TextField txtUser;

	private PasswordField txtPassword;

	private CheckBox chbUseSFTP;

	private ComboBox cmbProtocol;

	private CheckBox chbImplicit;

	private TextField txtDestination;

	public Dialog() {
		super(Configuration.class);
		buildLayout();
	}

	private void buildLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
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
		txtUser.setRequired(false);
		txtUser.setNullRepresentation("");
		mainLayout.addComponent(txtUser);

		txtPassword = new PasswordField();
		txtPassword.setWidth("100%");
		txtPassword.setHeight("-1px");
		txtPassword.setCaption("Password:");
		txtPassword.setRequired(false);
		txtPassword.setNullRepresentation("");
		mainLayout.addComponent(txtPassword);

		chbUseSFTP = new CheckBox();
		chbUseSFTP.setCaption("Use SFTP");
		mainLayout.addComponent(chbUseSFTP);

		cmbProtocol = new ComboBox();
		cmbProtocol.setWidth("100%");
		cmbProtocol.setHeight("-1px");
		cmbProtocol.setCaption("Protocol:");
		cmbProtocol.setRequired(false);
		cmbProtocol.setNewItemsAllowed(false);
		cmbProtocol.setNullSelectionAllowed(false);
		cmbProtocol.addItem("TLS");
		cmbProtocol.addItem("SSL");
		
		mainLayout.addComponent(cmbProtocol);

		chbImplicit = new CheckBox();
		chbImplicit.setCaption("Is implicit");
		mainLayout.addComponent(chbImplicit);

		txtDestination = new TextField();
		txtDestination.setWidth("100%");
		txtDestination.setHeight("-1px");
		txtDestination.setCaption("Destination: ");
		txtDestination.setRequired(true);
		txtDestination.setNullRepresentation("");
		mainLayout.addComponent(txtDestination);

		setCompositionRoot(mainLayout);

		// ...
		chbUseSFTP.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if ((Boolean) event.getProperty().getValue() == true) {
					cmbProtocol.setEnabled(true);
					chbImplicit.setEnabled(true);
				} else {
					cmbProtocol.setEnabled(false);
					chbImplicit.setEnabled(false);
				}
			}
		});
	}

	@Override
	protected void setConfiguration(Configuration c) throws ConfigException {
		txtHost.setValue(c.getHost());
		txtPort.setValue(Integer.toString(c.getPort()));
		txtUser.setValue(c.getUser());
		txtPassword.setValue(c.getPassword());
		chbUseSFTP.setValue(c.isSFTP());
		if (c.isSFTP()) {
			cmbProtocol.setEnabled(true);
			chbImplicit.setEnabled(true);
			cmbProtocol.setValue(c.getProtocol());
			chbImplicit.setValue(c.isImplicit());
		} else {
			cmbProtocol.setEnabled(false);
			chbImplicit.setEnabled(false);
			cmbProtocol.setValue("TLS");
			chbImplicit.setValue(false);
		}

		txtDestination.setValue(c.getTargetPath());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtHost.isValid()) {
			throw new ConfigException("Output file name must be specified!");
		}

		Configuration cnf = new Configuration();
		cnf.setHost(txtHost.getValue());
		try {
			cnf.setPort(Integer.parseInt(txtPort.getValue()));
		} catch (NumberFormatException e) {
			throw new ConfigException("Port must be a number.");
		}
		cnf.setUser(txtUser.getValue());
		cnf.setPassword(txtPassword.getValue());
		cnf.setSFTP(chbUseSFTP.getValue());
		if (chbUseSFTP.getValue() == true) {
			cnf.setProtocol((String)cmbProtocol.getValue());
			cnf.setImplicit(chbImplicit.getValue());
		}
		cnf.setTargetPath(getDestination());

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
		desc.append(getDestination());

		return desc.toString();
	}

	private String getDestination() {
		String dest = txtDestination.getValue().replace('\\', '/');
		if (dest.endsWith("/")) {
			dest = dest.substring(0, dest.length() - 1);
		}
		return dest;
	}

}
