package eu.unifiedviews.intlib.prepareXsltParamsRdfRepresentationOfLaws;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class IntlibPrepareXSLTParamsRDFRepresentationOfLawsVaadinDialog extends AdvancedVaadinDialogBase<IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1> {

    public IntlibPrepareXSLTParamsRDFRepresentationOfLawsVaadinDialog() {
        super(IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1 c = new IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1();

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
