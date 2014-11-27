package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.ConfigurationFromRdf;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.HelpHolder;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.container.ComponentTable;
import cz.cuni.mff.xrg.uv.utils.dialog.validator.UrlValidator;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class HttpDownloadVaadinDialog extends AdvancedVaadinDialogBase<HttpDownloadConfig_V2> {

    private ComponentTable<DownloadInfo_V1> table;

    public HttpDownloadVaadinDialog() {
        super(ConfigHistory.create(HttpDownloadConfig_V1.class,
                "eu.unifiedviews.plugins.extractor.httpdownload.HttpDownloadConfig_V1")
                .addCurrent(HttpDownloadConfig_V2.class),
                AddonInitializer.create(new CachedFileDownloader(),
                        new ConfigurationFromRdf("inRdfToDownload"),
                        new HelpHolder(HttpDownloadHelp.HELP)));

        buildLayout();
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

    private void buildLayout() {
        setSizeFull();

        table = new ComponentTable<>(DownloadInfo_V1.class,
                new ComponentTable.ColumnInfo("uri", "Uri", new UrlValidator(), 0.7f),
                new ComponentTable.ColumnInfo("virtualPath", "Download as (optional)", null, 0.3f)
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
