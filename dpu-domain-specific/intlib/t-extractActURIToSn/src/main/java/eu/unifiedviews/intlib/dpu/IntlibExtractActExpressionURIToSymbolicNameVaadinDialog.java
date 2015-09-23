package eu.unifiedviews.intlib.dpu;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class IntlibExtractActExpressionURIToSymbolicNameVaadinDialog extends AbstractDialog<IntlibExtractActExpressionURIToSymbolicNameConfig_V1> {

    public IntlibExtractActExpressionURIToSymbolicNameVaadinDialog() {
        super(IntlibExtractActExpressionURIToSymbolicName.class);

        //        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibExtractActExpressionURIToSymbolicNameConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public IntlibExtractActExpressionURIToSymbolicNameConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibExtractActExpressionURIToSymbolicNameConfig_V1 c = new IntlibExtractActExpressionURIToSymbolicNameConfig_V1();

        return c;
    }

    //
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
