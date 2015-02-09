#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.boost.dpu.vaadin.AbstractDialog;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Vaadin configuration dialog for ${dpu_name}.
 *
 * @author ${author}
 */
public class ${dpu_name}VaadinDialog extends AbstractDialog<${dpu_name}Config_V1> {

    public ${dpu_name}VaadinDialog() {
        super(${dpu_name}.class);
    }

    @Override
    public void setConfiguration(${dpu_name}Config_V1 c) throws DPUConfigException {

    }

    @Override
    public ${dpu_name}Config_V1 getConfiguration() throws DPUConfigException {
        final ${dpu_name}Config_V1 c = new ${dpu_name}Config_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label("DPU's configuration"));

        setCompositionRoot(mainLayout);
    }
}
