package cz.cuni.mff.xrg.uv.transformer.sparq.construct;

import com.vaadin.ui.TextArea;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog.
 */
public class SparqConstructVaadinDialog extends AdvancedVaadinDialogBase<SparqConstructConfig_V1> {

    private TextArea txtQuery;

    public SparqConstructVaadinDialog() {
        super(SparqConstructConfig_V1.class, AddonInitializer.noAddons());
        buildLayout();
    }

    @Override
    public void setConfiguration(SparqConstructConfig_V1 c) throws DPUConfigException {
        txtQuery.setValue(c.getQuery());
    }

    @Override
    public SparqConstructConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqConstructConfig_V1 c = new SparqConstructConfig_V1();
        if (txtQuery.getValue().isEmpty()) {
            throw new DPUConfigException("Query must not be empty.");
        }
        c.setQuery(txtQuery.getValue());
        return c;
    }

    private void buildLayout() {
		txtQuery = new TextArea("SPARQL Construct query");
        txtQuery.setSizeFull();
        txtQuery.setRequired(true);
        setCompositionRoot(txtQuery);
    }
}
