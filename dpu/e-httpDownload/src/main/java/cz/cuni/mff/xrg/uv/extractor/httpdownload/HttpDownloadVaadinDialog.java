package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.container.ComponentTable;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

public class HttpDownloadVaadinDialog extends AbstractDialog<HttpDownloadConfig_V2> {

    private ComponentTable<DownloadInfo_V1> table;

    public HttpDownloadVaadinDialog() {
        super(HttpDownload.class);
    }

    @Override
    public void setConfiguration(HttpDownloadConfig_V2 c) throws DPUConfigException {
        table.setValue(c.getToDownload());
    }

    @Override
    public HttpDownloadConfig_V2 getConfiguration() throws DPUConfigException {
        final HttpDownloadConfig_V2 c = new HttpDownloadConfig_V2();
        c.getToDownload().addAll(table.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
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
    }
}
