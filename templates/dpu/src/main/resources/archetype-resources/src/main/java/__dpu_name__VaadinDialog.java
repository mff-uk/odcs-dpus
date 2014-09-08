#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class ${dpu_name}VaadinDialog extends AdvancedVaadinDialogBase<${dpu_name}Config_V1> {

    public ${dpu_name}VaadinDialog() {
        super(${dpu_name}Config_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(${dpu_name}Config_V1 c) throws DPUConfigException {

    }

    @Override
    public ${dpu_name}Config_V1 getConfiguration() throws DPUConfigException {
        final ${dpu_name}Config_V1 c = new ${dpu_name}Config_V1();

        return c;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label("DPU's configuration"));

        setCompositionRoot(mainLayout);
    }
}
