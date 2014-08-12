package cz.cuni.mff.xrg.uv.extractor.sukl;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class SuklVaadinDialog extends AdvancedVaadinDialogBase<SuklConfig_V1> {

    public SuklVaadinDialog() {
        super(SuklConfig_V1.class, 
                AddonInitializer.create(new CachedFileDownloader()));

        buildLayout();
    }

    @Override
    public void setConfiguration(SuklConfig_V1 conf) throws DPUConfigException {

    }

    @Override
    public SuklConfig_V1 getConfiguration() throws DPUConfigException {
        final SuklConfig_V1 conf = new SuklConfig_V1();

        return conf;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label("DPU's configuration"));

        setCompositionRoot(mainLayout);
    }
}
