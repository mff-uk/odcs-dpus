package cz.cuni.mff.xrg.uv.loader.solr;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.net.URI;
import java.net.URISyntaxException;

public class SolrVaadinDialog extends AdvancedVaadinDialogBase<SolrConfig_V1> {

    private TextField txtServer;

    public SolrVaadinDialog() {
        super(SolrConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(SolrConfig_V1 conf) throws DPUConfigException {
        txtServer.setValue(conf.getServer());
    }

    @Override
    public SolrConfig_V1 getConfiguration() throws DPUConfigException {
        final SolrConfig_V1 conf = new SolrConfig_V1();

        try {
            new URI(txtServer.getValue());
        } catch (URISyntaxException ex) {
            throw new DPUConfigException("Invalid server URI.");
        }

        conf.setServer(txtServer.getValue());

        return conf;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        txtServer = new TextField("Solr:");
        txtServer.setWidth("100%");
        txtServer.setNullSettingAllowed(false);
        mainLayout.addComponent(txtServer);

        setCompositionRoot(mainLayout);
    }

    @Override
    public String getDescription() {
        return "To Solr " + txtServer.getValue() + "";
    }


}
