package eu.unifiedviews.legislation;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class ProvideActTextsAsLiteralsVaadinDialog extends AdvancedVaadinDialogBase<ProvideActTextsAsLiteralsConfig_V1> {

    public ProvideActTextsAsLiteralsVaadinDialog() {
        super(ProvideActTextsAsLiteralsConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(ProvideActTextsAsLiteralsConfig_V1 conf) throws DPUConfigException {

    }

    @Override
    public ProvideActTextsAsLiteralsConfig_V1 getConfiguration() throws DPUConfigException {
        final ProvideActTextsAsLiteralsConfig_V1 conf = new ProvideActTextsAsLiteralsConfig_V1();

        return conf;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");

        mainLayout.addComponent(new Label("DPU's configuration"));

        setCompositionRoot(mainLayout);
    }
}
