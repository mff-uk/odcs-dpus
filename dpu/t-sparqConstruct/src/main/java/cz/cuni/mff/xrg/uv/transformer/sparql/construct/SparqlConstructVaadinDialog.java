package cz.cuni.mff.xrg.uv.transformer.sparql.construct;

import com.vaadin.ui.TextArea;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog.
 */
public class SparqlConstructVaadinDialog extends AdvancedVaadinDialogBase<SparqlConstructConfig_V1> {

    private TextArea txtQuery;

    public SparqlConstructVaadinDialog() {
        super(SparqlConstructConfig_V1.class, AddonInitializer.noAddons());
        buildLayout();
    }

    @Override
    public void setConfiguration(SparqlConstructConfig_V1 c) throws DPUConfigException {
        txtQuery.setValue(c.getQuery());
    }

    @Override
    public SparqlConstructConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlConstructConfig_V1 c = new SparqlConstructConfig_V1();
        if (txtQuery.getValue().isEmpty()) {
            throw new DPUConfigException("Query must not be empty.");
        }
        c.setQuery(txtQuery.getValue());
        return c;
    }

    private void buildLayout() {
		txtQuery = new TextArea("SPARQL construct query");
        txtQuery.setSizeFull();
        txtQuery.setRequired(true);
        setCompositionRoot(txtQuery);
    }
    
}
