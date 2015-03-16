package cz.cuni.mff.xrg.uv.transformer.sparql.linker;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 *
 * @author Škoda Petr
 */
public class SparqlLinkerVaadinDialog extends AbstractDialog<SparqlLinkerConfig_V1> {

    private TextArea txtQuery;

    private CheckBox checkPerGraph;

    public SparqlLinkerVaadinDialog() {
        super(SparqlLinker.class);
    }

    @Override
    public void setConfiguration(SparqlLinkerConfig_V1 c) throws DPUConfigException {
        txtQuery.setValue(c.getQuery());
        checkPerGraph.setValue(c.isPerGraph());
    }

    @Override
    public SparqlLinkerConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlLinkerConfig_V1 c = new SparqlLinkerConfig_V1();
        if (txtQuery.getValue().isEmpty()) {
            throw new DPUConfigException("Query must not be empty.");
        }
        c.setQuery(txtQuery.getValue());
        c.setPerGraph(checkPerGraph.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        checkPerGraph = new CheckBox("Per-graph execution");
        checkPerGraph.setWidth("100%");
        mainLayout.addComponent(checkPerGraph);
        mainLayout.setExpandRatio(checkPerGraph, 0.0f);

        txtQuery = new TextArea("SPARQL construct query");
        txtQuery.setSizeFull();
        txtQuery.setRequired(true);
        mainLayout.addComponent(txtQuery);
        mainLayout.setExpandRatio(txtQuery, 1.0f);

        setCompositionRoot(mainLayout);
    }

}
