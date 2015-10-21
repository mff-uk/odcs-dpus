package cz.cuni.mff.xrg.uv.extractor.sukl;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class SuklVaadinDialog extends AbstractDialog<SuklConfig_V1> {

    private CheckBox checkCountMissing;

    private CheckBox checkFailOnDownloadError;

    public SuklVaadinDialog() {
        super(Sukl.class);
    }

    @Override
    public void setConfiguration(SuklConfig_V1 c) throws DPUConfigException {
        checkCountMissing.setValue(c.isCountNumberOfMissing());
        checkFailOnDownloadError.setValue(c.isFailOnDownloadError());
    }

    @Override
    public SuklConfig_V1 getConfiguration() throws DPUConfigException {
        final SuklConfig_V1 c = new SuklConfig_V1();
        c.setCountNumberOfMissing(checkCountMissing.getValue());
        c.setFailOnDownloadError(checkFailOnDownloadError.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        checkCountMissing = new CheckBox("Count missing");
        mainLayout.addComponent(checkCountMissing);

        checkFailOnDownloadError = new CheckBox("Fail on download error");
        mainLayout.addComponent(checkFailOnDownloadError);

        this.setCompositionRoot(mainLayout);
    }

}
