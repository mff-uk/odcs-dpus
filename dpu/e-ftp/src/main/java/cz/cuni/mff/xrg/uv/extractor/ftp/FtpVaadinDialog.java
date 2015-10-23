package cz.cuni.mff.xrg.uv.extractor.ftp;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.container.ComponentTable;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

public class FtpVaadinDialog extends AbstractDialog<FtpConfig_V1> {

    private CheckBox checkPassiveMode;

    private CheckBox checkBinaryMode;

    private ComponentTable<DownloadInfo_V1> table;

    public FtpVaadinDialog() {
        super(Ftp.class);
    }

    @Override
    public void setConfiguration(FtpConfig_V1 c) throws DPUConfigException {
        table.setValue(c.getToDownload());
        checkPassiveMode.setValue(c.isUsePassiveMode());
        checkBinaryMode.setValue(c.isUseBinaryMode());
    }

    @Override
    public FtpConfig_V1 getConfiguration() throws DPUConfigException {
        final FtpConfig_V1 c = new FtpConfig_V1();
        c.getToDownload().addAll(table.getValue());
        c.setUsePassiveMode(checkPassiveMode.getValue());
        c.setUseBinaryMode(checkBinaryMode.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        // Main page.
        setSizeFull();

        table = new ComponentTable<>(DownloadInfo_V1.class,
                new ComponentTable.ColumnInfo("uri", "Uri", new UrlValidator(), 0.5f),
                new ComponentTable.ColumnInfo("virtualPath", "Download as (optional)", null, 0.5f)
        );

        table.setPolicy(new ComponentTable.Policy<DownloadInfo_V1>() {

            @Override
            public boolean isSet(DownloadInfo_V1 value) {
                return value.getUri() != null && !value.getUri().isEmpty();
            }

        });

        setCompositionRoot(table);
        // Details.
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        checkPassiveMode = new CheckBox("Use passsive mode");
        mainLayout.addComponent(checkPassiveMode);

        checkBinaryMode = new CheckBox("Use binary mode");
        mainLayout.addComponent(checkBinaryMode);

        addTab(mainLayout, "FTP options");
    }
}
