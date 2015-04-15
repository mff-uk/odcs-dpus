package cz.cuni.mff.xrg.uv.addressmapper;

import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;

import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

public class AddressMapperVaadinDialog extends AbstractDialog<AddressMapperConfig_V1> {

    private TextField txtRuianUri;

    private TextField txtSolrUri;

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

        txtRuianUri = new TextField();
        txtRuianUri.setWidth("100%");
        txtRuianUri.setHeight("-1px");
        txtRuianUri.setCaption("Ruian URI:");
        txtRuianUri.setRequired(true);
        txtRuianUri.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtRuianUri);
        
        txtSolrUri = new TextField();
        txtSolrUri.setWidth("100%");
        txtSolrUri.setHeight("-1px");
        txtSolrUri.setCaption("Solr query URI (sample: http://ruian.linked.opendata.cz/solr/ruian/query):");
        txtSolrUri.setRequired(true);
        txtSolrUri.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtSolrUri);

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
        txtRuianUri.setValue(c.getRuainEndpoint());
        txtSolrUri.setValue(c.getSolrEndpoint());
        txtAddressPredicate.setValue(c.getAddressPredicate());
    }

    @Override
    protected AddressMapperConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtRuianUri.isValid()) {
            throw new DPUConfigException("Invalid SPARQL endpoint URI.");
        }
        if (!txtSolrUri.isValid()) {
            throw new DPUConfigException("Invalid SOLR endpoint URI.");
        }
        if (!txtAddressPredicate.isValid()) {
            throw new DPUConfigException("Invalid address predicate URI.");
        }

        final AddressMapperConfig_V1 cnf = new AddressMapperConfig_V1();
        cnf.setRuainEndpoint(txtRuianUri.getValue());
        cnf.setSolrEndpoint(txtSolrUri.getValue());
        cnf.setAddressPredicate(txtAddressPredicate.getValue());
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
