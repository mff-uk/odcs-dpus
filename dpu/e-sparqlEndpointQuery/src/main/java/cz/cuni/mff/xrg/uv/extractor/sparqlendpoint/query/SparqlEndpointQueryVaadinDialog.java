package cz.cuni.mff.xrg.uv.extractor.sparqlendpoint.query;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for SparqlEndpointQuery.
 *
 * @author Petr Škoda
 */
public class SparqlEndpointQueryVaadinDialog extends AbstractDialog<SparqlEndpointQueryConfig_V1> {

    private TextArea txtSelectQuery;
    
    private TextField txtSparqlEndpoint;
    
    private TextArea txtQueryTemplate;

    public SparqlEndpointQueryVaadinDialog() {
        super(SparqlEndpointQuery.class);
    }

    @Override
    public void setConfiguration(SparqlEndpointQueryConfig_V1 c) throws DPUConfigException {
        txtSelectQuery.setValue(c.getSelectQuery());
        txtSparqlEndpoint.setValue(c.getEndpoint());
        txtQueryTemplate.setValue(c.getQueryTemplate());
    }

    @Override
    public SparqlEndpointQueryConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlEndpointQueryConfig_V1 c = new SparqlEndpointQueryConfig_V1();

        c.setSelectQuery(txtSelectQuery.getValue());
        c.setEndpoint(txtSparqlEndpoint.getValue());
        c.setQueryTemplate(txtQueryTemplate.getValue());
        
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtSelectQuery = new TextArea("Placeholders select query:");
        txtSelectQuery.setSizeFull();
        mainLayout.addComponent(txtSelectQuery);
        mainLayout.setExpandRatio(txtSelectQuery, 0.5f);

        txtSparqlEndpoint = new TextField("Remote sparql endpoint:");
        txtSparqlEndpoint.setSizeFull();
        mainLayout.addComponent(txtSparqlEndpoint);
        mainLayout.setExpandRatio(txtSparqlEndpoint, 0.5f);

        txtQueryTemplate = new TextArea("Template of remote query:");
        txtQueryTemplate.setSizeFull();
        mainLayout.addComponent(txtQueryTemplate);
        mainLayout.setExpandRatio(txtQueryTemplate, 0.5f);
        
        setCompositionRoot(mainLayout);
    }
}
