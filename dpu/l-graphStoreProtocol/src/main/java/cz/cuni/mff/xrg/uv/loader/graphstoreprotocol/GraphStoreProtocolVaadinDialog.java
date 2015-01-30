package cz.cuni.mff.xrg.uv.loader.graphstoreprotocol;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.FaultToleranceWrap;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.validator.UrlValidator;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * TODO Petr: Fuseki repository should disable authentication
 *  Virtuoso should disable multiple select, update sparql selection
 *
 * @author Škoda Petr
 */
public class GraphStoreProtocolVaadinDialog extends AdvancedVaadinDialogBase<GraphStoreProtocolConfig_V1> {

    private TextField txtEndpointSelect;
    
    private TextField txtEndpointUpdate;
    
    private TextField txtEndpointCRUD;
    
    private TextField txtTargetGraph;

    private ComboBox boxResporitoryType;

    private CheckBox checkAuthUse;

    private TextField txtAuthUsername;
    
    private PasswordField txtAuthPassword;

    public GraphStoreProtocolVaadinDialog() {
        super(GraphStoreProtocolConfig_V1.class, AddonInitializer.create(new FaultToleranceWrap()));

        buildMainLayout();
        buildAuthLayout();
    }

    @Override
    public void setConfiguration(GraphStoreProtocolConfig_V1 c) throws DPUConfigException {
        boxResporitoryType.setValue(c.getRepositoryType());
        txtEndpointSelect.setValue(c.getEndpointSelect());
        txtEndpointUpdate.setValue(c.getEndpointUpdate());
        txtEndpointCRUD.setValue(c.getEndpointCRUD());
        txtTargetGraph.setValue(c.getTargetGraphURI());
        // Authentication
        checkAuthUse.setValue(c.isUseAuthentification());
        txtAuthUsername.setValue(c.getUserName());
        txtAuthPassword.setValue(c.getPassword());
    }

    @Override
    public GraphStoreProtocolConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtEndpointSelect.isValid() || !txtEndpointUpdate.isValid() ||
                !txtEndpointCRUD.isValid() || !txtTargetGraph.isValid()) {
            throw new DPUConfigException("Invalid input.");
        }
        final GraphStoreProtocolConfig_V1 c = new GraphStoreProtocolConfig_V1();
        c.setRepositoryType((GraphStoreProtocolConfig_V1.RepositoryType)boxResporitoryType.getValue());
        c.setEndpointSelect(txtEndpointSelect.getValue());
        c.setEndpointUpdate(txtEndpointUpdate.getValue());
        c.setEndpointCRUD(txtEndpointCRUD.getValue());
        c.setTargetGraphURI(txtTargetGraph.getValue());
        // Authentication
        c.setUseAuthentification(checkAuthUse.getValue());
        c.setUserName(txtAuthUsername.getValue());
        c.setPassword(txtAuthPassword.getValue());
        return c;
    }

    private void buildMainLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        boxResporitoryType = new ComboBox("Repository type");
        for (GraphStoreProtocolConfig_V1.RepositoryType type: GraphStoreProtocolConfig_V1.RepositoryType.values()) {
            boxResporitoryType.addItem(type);
        }
        boxResporitoryType.setInvalidAllowed(false);
        boxResporitoryType.setNullSelectionAllowed(false);
        boxResporitoryType.setTextInputAllowed(false);
        mainLayout.addComponent(boxResporitoryType);

        txtEndpointSelect = new TextField("Select endpoint URL");
        txtEndpointSelect.setWidth("100%");
        txtEndpointSelect.addValidator(new UrlValidator(false));
        txtEndpointSelect.setImmediate(true);
        mainLayout.addComponent(txtEndpointSelect);

        txtEndpointUpdate = new TextField("Update endpoint URL");
        txtEndpointUpdate.setWidth("100%");
        txtEndpointUpdate.addValidator(new UrlValidator(false));
        txtEndpointUpdate.setImmediate(true);
        mainLayout.addComponent(txtEndpointUpdate);

        txtEndpointCRUD = new TextField("CRUD endpoint URL");
        txtEndpointCRUD.setWidth("100%");
        txtEndpointCRUD.addValidator(new UrlValidator(false));
        txtEndpointCRUD.setImmediate(true);
        mainLayout.addComponent(txtEndpointCRUD);

        txtTargetGraph = new TextField("Target graph");
        txtTargetGraph.setWidth("100%");
        txtTargetGraph.addValidator(new UrlValidator(false));
        txtTargetGraph.setImmediate(true);
        mainLayout.addComponent(txtTargetGraph);  

        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);

        setCompositionRoot(panel);
    }

    private void buildAuthLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        checkAuthUse = new CheckBox("Use authentication");
        checkAuthUse.setImmediate(true);
        checkAuthUse.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                txtAuthUsername.setEnabled((Boolean)event.getProperty().getValue());
                txtAuthPassword.setEnabled((Boolean)event.getProperty().getValue());
            }
        });
        mainLayout.addComponent(checkAuthUse);
        
        txtAuthUsername = new TextField("Username");
        txtAuthUsername.setWidth("100%");
        txtAuthUsername.setImmediate(true);
        mainLayout.addComponent(txtAuthUsername);

        txtAuthPassword = new PasswordField("Password");
        txtAuthPassword.setWidth("100%");
        txtAuthPassword.setImmediate(true);
        mainLayout.addComponent(txtAuthPassword);

        addTab(mainLayout, "Authentication ");
    }

}
