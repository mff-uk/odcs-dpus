package cz.cuni.mff.xrg.uv.addressmapper;

import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;

import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

public class AddressMapperVaadinDialog extends AbstractDialog<AddressMapperConfig_V1> {

    private TextField txtRuianUri;

    public AddressMapperVaadinDialog() {
        super(AddressMapper.class);
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        txtRuianUri = new TextField();
        txtRuianUri.setWidth("100%");
        txtRuianUri.setHeight("-1px");
        txtRuianUri.setCaption("Ruian URI:");
        txtRuianUri.setRequired(true);
        txtRuianUri.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtRuianUri);
        mainLayout.setExpandRatio(txtRuianUri, 0);

        Panel mainPanel = new Panel();
        mainPanel.setSizeFull();
        mainPanel.setContent(mainLayout);

        setCompositionRoot(mainPanel);
    }

    @Override
    protected void setConfiguration(AddressMapperConfig_V1 c) throws DPUConfigException {
        txtRuianUri.setValue(c.getRuainEndpoint());
    }

    @Override
    protected AddressMapperConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtRuianUri.isValid()) {
            throw new DPUConfigException("Invalid SPARQL endpoint URI.");
        }

        final AddressMapperConfig_V1 cnf = new AddressMapperConfig_V1();
        cnf.setRuainEndpoint(txtRuianUri.getValue());
        return cnf;
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("RUIAN endpoint: ");
        desc.append(txtRuianUri.getValue());
        return desc.toString();
    }

}
