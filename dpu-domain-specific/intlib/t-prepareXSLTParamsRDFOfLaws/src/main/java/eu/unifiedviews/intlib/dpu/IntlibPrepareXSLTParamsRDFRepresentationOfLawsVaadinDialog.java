package eu.unifiedviews.intlib.dpu;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class IntlibPrepareXSLTParamsRDFRepresentationOfLawsVaadinDialog extends AbstractDialog<IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1> {

    public IntlibPrepareXSLTParamsRDFRepresentationOfLawsVaadinDialog() {
        super(IntlibPrepareXSLTParamsRDFRepresentationOfLaws.class);

        //        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1 c = new IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1();

        return c;
    }

    //    private void buildLayout() {
    //        final VerticalLayout mainLayout = new VerticalLayout();
    //        mainLayout.setWidth("100%");
    //        mainLayout.setHeight("-1px");
    //        mainLayout.setMargin(true);
    //
    //        mainLayout.addComponent(new Label("DPU's configuration"));
    //
    //        setCompositionRoot(mainLayout);
    //    }

    @Override
    protected void buildDialogLayout() {

        this.setSizeFull();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label("DPU's configuration"));

        setCompositionRoot(mainLayout);

    }
}
