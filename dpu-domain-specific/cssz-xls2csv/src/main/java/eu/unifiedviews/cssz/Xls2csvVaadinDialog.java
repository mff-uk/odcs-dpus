package eu.unifiedviews.cssz;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class Xls2csvVaadinDialog extends AdvancedVaadinDialogBase<Xls2csvConfig_V1> {

    private TextField tfTemplatePrefix;
    
    public Xls2csvVaadinDialog() {
        super(Xls2csvConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(Xls2csvConfig_V1 c) throws DPUConfigException {
        tfTemplatePrefix.setValue(c.getTemplate_prefix());
    }

    @Override
    public Xls2csvConfig_V1 getConfiguration() throws DPUConfigException {
        final Xls2csvConfig_V1 c = new Xls2csvConfig_V1();

        if (!tfTemplatePrefix.isValid()) {
            throw new DPUConfigException("XLS Template prefix must be specified");
        }

        c.setTemplate_prefix(tfTemplatePrefix.getValue().trim());
        
        
        return c;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label("Template prefix: "));
        
        tfTemplatePrefix = new TextField("");
        tfTemplatePrefix.setWidth("100%");
        tfTemplatePrefix.setRequired(true);
        tfTemplatePrefix.setNullRepresentation("");
        tfTemplatePrefix.setNullSettingAllowed(false);
        mainLayout.addComponent(tfTemplatePrefix);
        

        setCompositionRoot(mainLayout);
    }
    
    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("Convert input XLS files using template prefix ");
        desc.append(tfTemplatePrefix.getValue());

        return desc.toString();
    }
}
