package cz.opendata.linked.lodcloud.loader;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import eu.unifiedviews.dpu.config.DPUConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class LoaderDialog extends AdvancedVaadinDialogBase<LoaderConfig> {

    //private static final long serialVersionUID = 7003725620084616056L;
    private GridLayout mainLayout;
    private TextField interval, failinterval;
    private TextField tfNumOfRecords;
    private TextField tfSessionId;

    public LoaderDialog() {
        super(LoaderConfig.class,AddonInitializer.create(new SimpleRdfConfigurator(Loader.class)));
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

        return mainLayout;
    }    
     
    @Override
    public void setConfiguration(LoaderConfig conf) throws DPUConfigException {
//        interval.setValue(Integer.toString(conf.getInterval()));
    }

    @Override
    public LoaderConfig getConfiguration() throws DPUConfigException {
    	LoaderConfig conf = new LoaderConfig();
//        conf.setInterval(Integer.parseInt(interval.getValue()));
        return conf;
    }
    
}
