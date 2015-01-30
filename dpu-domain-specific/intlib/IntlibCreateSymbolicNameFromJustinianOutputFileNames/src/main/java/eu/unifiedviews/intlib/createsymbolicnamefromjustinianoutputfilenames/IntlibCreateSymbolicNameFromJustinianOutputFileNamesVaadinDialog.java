package eu.unifiedviews.intlib.createsymbolicnamefromjustinianoutputfilenames;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class IntlibCreateSymbolicNameFromJustinianOutputFileNamesVaadinDialog extends AdvancedVaadinDialogBase<IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1> {

    public IntlibCreateSymbolicNameFromJustinianOutputFileNamesVaadinDialog() {
        super(IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1 c = new IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1();

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
