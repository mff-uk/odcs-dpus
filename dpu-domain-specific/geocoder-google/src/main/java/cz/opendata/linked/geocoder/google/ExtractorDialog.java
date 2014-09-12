package cz.opendata.linked.geocoder.google;

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
    private TextField interval;
    private TextField limit;
    private TextField hoursToCheck;

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
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);

        limit = new TextField();
        limit.setCaption("Maximum amount of downloads:");
        mainLayout.addComponent(limit);

        hoursToCheck = new TextField();
        hoursToCheck.setCaption("Maximum amount interval:");
        mainLayout.addComponent(hoursToCheck);

        return mainLayout;
    }    
     
    @Override
    public void setConfiguration(ExtractorConfig conf) throws DPUConfigException {
        interval.setValue(Integer.toString(conf.getInterval()));
        hoursToCheck.setValue(Integer.toString(conf.getHoursToCheck()));
        limit.setValue(Integer.toString(conf.getLimit()));

    }

    @Override
    public ExtractorConfig getConfiguration() throws DPUConfigException {
        ExtractorConfig conf = new ExtractorConfig();
        conf.setInterval(Integer.parseInt(interval.getValue()));
        conf.setHoursToCheck(Integer.parseInt(hoursToCheck.getValue()));
        conf.setLimit(Integer.parseInt(limit.getValue()));
        return conf;
    }
    
}
