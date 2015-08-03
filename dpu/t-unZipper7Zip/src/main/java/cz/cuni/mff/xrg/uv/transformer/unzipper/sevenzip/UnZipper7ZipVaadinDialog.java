package cz.cuni.mff.xrg.uv.transformer.unzipper.sevenzip;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class UnZipper7ZipVaadinDialog extends AbstractDialog<UnZipper7ZipConfig_V1> {

   private CheckBox checkNotPrefix;

    public UnZipper7ZipVaadinDialog() {
        super(UnZipper7Zip.class);
    }

    @Override
    public void buildDialogLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setMargin(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");

        checkNotPrefix = new CheckBox(ctx.tr("unzipper7zip.dialog.unzip.noprefix"));
        checkNotPrefix.setDescription(ctx.tr("unzipper7zip.dialog.unzip.noprefix.description"));
        mainLayout.addComponent(checkNotPrefix);

        setCompositionRoot(mainLayout);

    }

    @Override
    protected void setConfiguration(UnZipper7ZipConfig_V1 c) throws DPUConfigException {
        checkNotPrefix.setValue(c.isNotPrefixed());
    }

    @Override
    protected UnZipper7ZipConfig_V1 getConfiguration() throws DPUConfigException {
        final UnZipper7ZipConfig_V1 cnf = new UnZipper7ZipConfig_V1();
        cnf.setNotPrefixed(checkNotPrefix.getValue() == null ? false : checkNotPrefix.getValue());
        return cnf;
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        if (checkNotPrefix.getValue() == true) {
            // If true then we do not use prefixes.
            desc.append(ctx.tr("unzipper7zip.dialog.unzip.notprefixed"));
        } else {
            // If false prefix is not used.
            desc.append(ctx.tr("unzipper7zip.dialog.unzip.prefixed"));
        }

        return desc.toString();
    }


}
