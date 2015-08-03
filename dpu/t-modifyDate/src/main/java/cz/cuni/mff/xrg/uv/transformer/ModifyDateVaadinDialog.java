package cz.cuni.mff.xrg.uv.transformer;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for ModifyDate.
 *
 * @author Petr Škoda
 */
public class ModifyDateVaadinDialog extends AbstractDialog<ModifyDateConfig_V1> {

    private TextField txtInputPredicate;
    
    private TextField txtModifyDay;
    
    private TextField txtOutputPredicate;

    public ModifyDateVaadinDialog() {
        super(ModifyDate.class);
    }

    @Override
    public void setConfiguration(ModifyDateConfig_V1 c) throws DPUConfigException {
        txtInputPredicate.setValue(c.getInputPredicate());
        txtModifyDay.setValue(Integer.toString(c.getModifyDay()));
        txtOutputPredicate.setValue(c.getOutputPredicate());
    }

    @Override
    public ModifyDateConfig_V1 getConfiguration() throws DPUConfigException {
        final ModifyDateConfig_V1 c = new ModifyDateConfig_V1();

        c.setInputPredicate(txtInputPredicate.getValue());
        c.setOutputPredicate(txtOutputPredicate.getValue());
        try {
            c.setModifyDay(Integer.parseInt(txtModifyDay.getValue()));
        } catch (NumberFormatException ex) {
            throw new DPUConfigException("Modification value must be integer!", ex);
        }

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        txtInputPredicate = new TextField("Input predicate");
        txtInputPredicate.setWidth("100%");
        mainLayout.addComponent(txtInputPredicate);

        txtModifyDay = new TextField("Day modification");
        txtModifyDay.setWidth("100%");
        mainLayout.addComponent(txtModifyDay);

        txtOutputPredicate = new TextField("Output predicate");
        txtOutputPredicate.setWidth("100%");
        mainLayout.addComponent(txtOutputPredicate);

        setCompositionRoot(mainLayout);
    }
}
