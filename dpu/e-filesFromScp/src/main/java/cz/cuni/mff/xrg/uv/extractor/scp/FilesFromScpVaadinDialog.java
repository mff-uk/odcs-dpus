package cz.cuni.mff.xrg.uv.extractor.scp;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;

/**
 * @author Škoda Petr
 */
public class FilesFromScpVaadinDialog extends BaseConfigDialog<FilesFromScpConfig_V1> {

    private VerticalLayout mainLayout;

    private TextField txtHost;

    private TextField txtPort;

    private TextField txtUser;

    private PasswordField txtPassword;

    private TextField txtSource;

    private CheckBox chbSoftFail;

    public FilesFromScpVaadinDialog() {
        super(FilesFromScpConfig_V1.class);
        buildLayout();
    }

    private void buildLayout() {
        setSizeFull();

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");

        txtHost = new TextField();
        txtHost.setWidth("100%");
        txtHost.setHeight("-1px");
        txtHost.setCaption("Host");
        txtHost.setRequired(true);
        mainLayout.addComponent(txtHost);

        txtPort = new TextField();
        txtPort.setWidth("100%");
        txtPort.setHeight("-1px");
        txtPort.setCaption("Port");
        txtPort.setRequired(true);
        mainLayout.addComponent(txtPort);

        txtUser = new TextField();
        txtUser.setWidth("100%");
        txtUser.setHeight("-1px");
        txtUser.setCaption("Username");
        txtUser.setRequired(true);
        mainLayout.addComponent(txtUser);

        txtPassword = new PasswordField();
        txtPassword.setWidth("100%");
        txtPassword.setHeight("-1px");
        txtPassword.setCaption("Password");
        txtPassword.setRequired(true);
        txtPassword.setNullRepresentation("");
        mainLayout.addComponent(txtPassword);

        txtSource = new TextField();
        txtSource.setWidth("100%");
        txtSource.setHeight("-1px");
        txtSource.setCaption("Source directory");
        txtSource.setRequired(true);
        txtSource.setNullRepresentation("");
        mainLayout.addComponent(txtSource);

        mainLayout.addComponent(new Label("Note: You need rights to access the target directory and all its parent directories"));

        chbSoftFail = new CheckBox();
        chbSoftFail.setCaption("Soft failure");
        mainLayout.addComponent(chbSoftFail);

        mainLayout.addComponent(new Label("If 'Soft failure' is checked and upload failed, then pipeline continue, otherwise the pipeline is stopped."));

        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);

        setCompositionRoot(panel);
    }

    @Override
    protected void setConfiguration(FilesFromScpConfig_V1 c) throws DPUConfigException {
        txtHost.setValue(c.getHostname());
        txtPort.setValue(c.getPort().toString());
        txtUser.setValue(c.getUsername());
        txtPassword.setValue(c.getPassword());
        txtSource.setValue(c.getSource());
        chbSoftFail.setValue(c.isSoftFail());
    }

    @Override
    protected FilesFromScpConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtHost.isValid()) {
            throw new DPUConfigException("Host must be specified!");
        }

        FilesFromScpConfig_V1 cnf = new FilesFromScpConfig_V1();
        cnf.setHostname(txtHost.getValue());
        try {
            cnf.setPort(Integer.parseInt(txtPort.getValue()));
        } catch (NumberFormatException e) {
            throw new DPUConfigException("Port must be a number.");
        }
        cnf.setUsername(txtUser.getValue());
        cnf.setPassword(txtPassword.getValue());
        cnf.setSource(getSource());
        cnf.setSoftFail(chbSoftFail.getValue());
        return cnf;
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("Download from ");
        desc.append(txtUser.getValue());
        desc.append("@");
        desc.append(txtHost.getValue());
        desc.append(":");
        desc.append(getSource());

        return desc.toString();
    }

    private String getSource() {
        String dest = txtSource.getValue().replace('\\', '/');
        if (dest.endsWith("/")) {
            dest = dest.substring(0, dest.length() - 1);
        }
        return dest;
    }
}
