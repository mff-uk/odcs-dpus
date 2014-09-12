package cz.opendata.linked.geocoder.krovak;

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

    private static final long serialVersionUID = 7003725620084616056L;
    private GridLayout mainLayout;
    private TextField interval, failinterval;
    private TextField tfNumOfRecords;
    private TextField tfSessionId;

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

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);

        failinterval = new TextField();
        failinterval.setCaption("Interval between failed downloads:");
        mainLayout.addComponent(failinterval);

        tfNumOfRecords = new TextField();
        tfNumOfRecords.setCaption("Number of records in one block:");
        mainLayout.addComponent(tfNumOfRecords);

        tfSessionId = new TextField();
        tfSessionId.setCaption("Session ID:");
        tfSessionId.setWidth("100%");
        mainLayout.addComponent(tfSessionId);

        return mainLayout;
    }    
     
    @Override
    public void setConfiguration(ExtractorConfig conf) throws DPUConfigException {
        interval.setValue(Integer.toString(conf.getInterval()));
        failinterval.setValue(Integer.toString(conf.getFailInterval()));
        tfNumOfRecords.setValue(Integer.toString(conf.getNumofrecords()));
        tfSessionId.setValue(conf.getSessionId());
    }

    @Override
    public ExtractorConfig getConfiguration() throws DPUConfigException {
        ExtractorConfig conf = new ExtractorConfig();
        conf.setInterval(Integer.parseInt(interval.getValue()));
        conf.setFailInterval(Integer.parseInt(failinterval.getValue()));
        conf.setNumofrecords(Integer.parseInt(tfNumOfRecords.getValue()));
        conf.setSessionId(tfSessionId.getValue());
        return conf;
    }
    
}
