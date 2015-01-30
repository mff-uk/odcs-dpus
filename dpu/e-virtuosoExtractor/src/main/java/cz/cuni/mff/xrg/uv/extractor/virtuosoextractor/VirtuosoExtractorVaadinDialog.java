package cz.cuni.mff.xrg.uv.extractor.virtuosoextractor;

import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.validator.UrlValidator;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog.
 */
public class VirtuosoExtractorVaadinDialog extends AdvancedVaadinDialogBase<VirtuosoExtractorConfig_V1> {

    private TextField txtServerUrl;

    private TextField txtUsername;

    private PasswordField txtPassword;

    private TextField txtGraphUri;

    private TextField txtOutputPath;

    public VirtuosoExtractorVaadinDialog() {
        super(VirtuosoExtractorConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(VirtuosoExtractorConfig_V1 c) throws DPUConfigException {
        txtServerUrl.setValue(c.getServerUrl());
        txtUsername.setValue(c.getUsername());
        txtPassword.setValue(c.getPassword());
        txtGraphUri.setValue(c.getGraphUri());
        txtOutputPath.setValue(c.getOutputPath());
    }

    @Override
    public VirtuosoExtractorConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtServerUrl.isValid() || !txtGraphUri.isValid()) {
            throw new DPUConfigException("Server url and graph uri must be valid.");
        }

        final VirtuosoExtractorConfig_V1 c = new VirtuosoExtractorConfig_V1();
        c.setServerUrl(txtServerUrl.getValue());
        c.setUsername(txtUsername.getValue());
        c.setPassword(txtPassword.getValue());
        c.setGraphUri(txtGraphUri.getValue());
        c.setOutputPath(txtOutputPath.getValue());
        return c;
    }

    private void buildLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        txtServerUrl = new TextField("Server JDBC connection address");
        txtServerUrl.setWidth("100%");
        mainLayout.addComponent(txtServerUrl);

        txtUsername = new TextField("Username");
        txtUsername.setWidth("100%");
        mainLayout.addComponent(txtUsername);

        txtPassword = new PasswordField("Password");
        txtPassword.setWidth("100%");
        mainLayout.addComponent(txtPassword);

        txtGraphUri = new TextField("Graph URI");
        txtGraphUri.setWidth("100%");
        txtGraphUri.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtGraphUri);

        txtOutputPath = new TextField("Output path");
        txtOutputPath.setWidth("100%");
        mainLayout.addComponent(txtOutputPath);

        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);

        setCompositionRoot(panel);
    }
}
