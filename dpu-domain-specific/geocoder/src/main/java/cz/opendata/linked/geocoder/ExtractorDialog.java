package cz.opendata.linked.geocoder;

import java.net.URI;
import java.net.URISyntaxException;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import eu.unifiedviews.dpu.config.DPUConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends AdvancedVaadinDialogBase<ExtractorConfig> {

    /**
     * 
     */
    private static final long serialVersionUID = 7003725620084616056L;
    private GridLayout mainLayout;
    private CheckBox chkRewriteCache;
    private TextField geocoderURI;
    
    public ExtractorDialog() {
        super(ExtractorConfig.class,AddonInitializer.noAddons());
        buildMainLayout();
        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);
        setCompositionRoot(panel);
    }  
    
    private GridLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new GridLayout(1, 2);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        chkRewriteCache = new CheckBox("Rewrite cache:");
        chkRewriteCache.setDescription("When selected, cache will be ignored.");
        chkRewriteCache.setWidth("100%");
        
        mainLayout.addComponent(chkRewriteCache);
        
        geocoderURI = new TextField();
        geocoderURI.setCaption("Geocoder URI:");
        mainLayout.addComponent(geocoderURI);


        return mainLayout;
    }    
     
    @Override
    public void setConfiguration(ExtractorConfig conf) throws DPUConfigException {
        chkRewriteCache.setValue(conf.isRewriteCache());
        geocoderURI.setValue(conf.getGeocoderURI());
    }

    @Override
    public ExtractorConfig getConfiguration() throws DPUConfigException {
        ExtractorConfig conf = new ExtractorConfig();
        conf.setRewriteCache((boolean) chkRewriteCache.getValue());
        try {
            conf.setGeocoderURI(new URI(geocoderURI.getValue()).toString());
        } catch (URISyntaxException e) {
            throw new DPUConfigException(e.getLocalizedMessage());
        }
        return conf;
    }
    
}
