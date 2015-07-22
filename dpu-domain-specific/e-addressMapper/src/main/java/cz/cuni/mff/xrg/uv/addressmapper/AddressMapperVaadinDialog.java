package cz.cuni.mff.xrg.uv.addressmapper;

import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;

import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

public final class AddressMapperVaadinDialog extends AbstractDialog<AddressMapperConfig_V1> {

    private TextField txtServiceEndpoint;

    private TextField txtAddressPredicate;

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

        txtServiceEndpoint = new TextField();
        txtServiceEndpoint.setWidth("100%");
        txtServiceEndpoint.setHeight("-1px");
        txtServiceEndpoint.setCaption("Service URI:");
        txtServiceEndpoint.setRequired(true);
        txtServiceEndpoint.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtServiceEndpoint);
        
        txtAddressPredicate = new TextField();
        txtAddressPredicate.setWidth("100%");
        txtAddressPredicate.setHeight("-1px");
        txtAddressPredicate.setCaption("Address predicate:");
        txtAddressPredicate.addValidator(new UrlValidator(false));
        txtAddressPredicate.setRequired(true);
        mainLayout.addComponent(txtAddressPredicate);        
        
        setCompositionRoot(mainLayout);
    }

    @Override
    protected void setConfiguration(AddressMapperConfig_V1 c) throws DPUConfigException {
        txtServiceEndpoint.setValue(c.getServiceEndpoint());
        txtAddressPredicate.setValue(c.getAddressPredicate());
    }

    @Override
    protected AddressMapperConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtServiceEndpoint.isValid()) {
            throw new DPUConfigException("Invalid Service.");
        }
        if (!txtAddressPredicate.isValid()) {
            throw new DPUConfigException("Invalid address predicate URI.");
        }

        final AddressMapperConfig_V1 cnf = new AddressMapperConfig_V1();
        cnf.setServiceEndpoint(txtServiceEndpoint.getValue());
        cnf.setAddressPredicate(txtAddressPredicate.getValue());
        return cnf;
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Address predicate: ");
        desc.append(txtAddressPredicate.getValue());
        return desc.toString();
    }

}
