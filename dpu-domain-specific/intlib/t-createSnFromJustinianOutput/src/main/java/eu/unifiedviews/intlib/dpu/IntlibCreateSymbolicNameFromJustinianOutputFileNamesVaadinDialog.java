package eu.unifiedviews.intlib.dpu;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class IntlibCreateSymbolicNameFromJustinianOutputFileNamesVaadinDialog extends AbstractDialog<IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1> {

    public IntlibCreateSymbolicNameFromJustinianOutputFileNamesVaadinDialog() {
        super(IntlibCreateSymbolicNameFromJustinianOutputFileNames.class);

        //buildLayout();
    }

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

    @Override
    public void setConfiguration(IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1 c = new IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1();

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

}
