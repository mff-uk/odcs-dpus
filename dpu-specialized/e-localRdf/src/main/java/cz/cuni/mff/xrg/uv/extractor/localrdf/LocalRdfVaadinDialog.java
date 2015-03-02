package cz.cuni.mff.xrg.uv.extractor.localrdf;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog.
 */
public class LocalRdfVaadinDialog extends AdvancedVaadinDialogBase<LocalRdfConfig_V1> {

    private TextField txtExecution;

    private TextField txtDpu;

    private TextField txtDataUnit;

    public LocalRdfVaadinDialog() {
        super(LocalRdfConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(LocalRdfConfig_V1 c) throws DPUConfigException {
        txtExecution.setValue(c.getExecution());
        txtDpu.setValue(c.getDpu());
        txtDataUnit.setValue(c.getDataUnit());
    }

    @Override
    public LocalRdfConfig_V1 getConfiguration() throws DPUConfigException {
        final LocalRdfConfig_V1 c = new LocalRdfConfig_V1();
        c.setExecution(txtExecution.getValue());
        c.setDpu(txtDpu.getValue());
        c.setDataUnit(txtDataUnit.getValue());
        return c;
    }

    private void buildLayout() {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);

        txtExecution = new TextField("Execution ID");
        txtExecution.setWidth("100%");
        layout.addComponent(txtExecution);

        txtDpu = new TextField("DPU ID");
        txtDpu.setWidth("100%");
        layout.addComponent(txtDpu);

        txtDataUnit = new TextField("DataUnit ID");
        txtDataUnit.setWidth("100%");
        layout.addComponent(txtDataUnit);

        setCompositionRoot(layout);
    }

}
