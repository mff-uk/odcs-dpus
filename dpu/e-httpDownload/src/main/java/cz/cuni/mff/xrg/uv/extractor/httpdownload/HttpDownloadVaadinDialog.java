package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.ConfigurationFromRdf;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class HttpDownloadVaadinDialog extends AdvancedVaadinDialogBase<HttpDownloadConfig_V2> {

    private TextField txtSourceUrl;

    private TextField txtTarget;

    public HttpDownloadVaadinDialog() {
        super(ConfigHistory.create(HttpDownloadConfig_V1.class,
                "eu.unifiedviews.plugins.extractor.httpdownload.HttpDownloadConfig_V1")
                .addCurrent(HttpDownloadConfig_V2.class),
                AddonInitializer.create(new CachedFileDownloader(), new ConfigurationFromRdf("inRdfToDownload")));

        buildLayout();
    }

    @Override
    public void setConfiguration(HttpDownloadConfig_V2 c) throws DPUConfigException {
        if (c.getToDownload().size() > 0) {
            txtSourceUrl.setValue(c.getToDownload().get(0).getUri());
            txtTarget.setValue(c.getToDownload().get(0).getVirtualPath());
        }
    }

    @Override
    public HttpDownloadConfig_V2 getConfiguration() throws DPUConfigException {
        final HttpDownloadConfig_V2 c = new HttpDownloadConfig_V2();

        // use conversion by URI!
        c.getToDownload().add(new DownloadInfo_V1(
                txtSourceUrl.getValue(),
                txtTarget.getValue()));

        return c;
    }

    private void buildLayout() {
		setWidth("100%");
		setHeight("100%");
        
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setMargin(true);

		txtSourceUrl = new TextField();
		txtSourceUrl.setWidth("100%");
		txtSourceUrl.setHeight("-1px");
		txtSourceUrl.setCaption("URL:");
		txtSourceUrl.setRequired(false);
		txtSourceUrl.setNullRepresentation("");
		mainLayout.addComponent(txtSourceUrl);

		txtTarget = new TextField();
		txtTarget.setWidth("100%");
		txtTarget.setHeight("-1px");
		txtTarget.setCaption("Target - file name and location in output:");
		txtTarget.setRequired(true);
		mainLayout.addComponent(txtTarget);

        setCompositionRoot(mainLayout);
    }
}
