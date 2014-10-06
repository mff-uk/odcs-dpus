package cz.cuni.mff.xrg.uv.filesiteratorfail;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class FilesIteratorFailVaadinDialog extends AdvancedVaadinDialogBase<FilesIteratorFailConfig_V1> {

    public FilesIteratorFailVaadinDialog() {
        super(FilesIteratorFailConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(FilesIteratorFailConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public FilesIteratorFailConfig_V1 getConfiguration() throws DPUConfigException {
        final FilesIteratorFailConfig_V1 c = new FilesIteratorFailConfig_V1();

        return c;
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
