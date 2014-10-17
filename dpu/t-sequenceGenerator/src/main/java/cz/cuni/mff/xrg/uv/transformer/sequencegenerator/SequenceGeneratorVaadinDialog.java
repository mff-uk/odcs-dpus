package cz.cuni.mff.xrg.uv.transformer.sequencegenerator;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.validator.UrlValidator;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class SequenceGeneratorVaadinDialog extends AdvancedVaadinDialogBase<SequenceGeneratorConfig_V1> {

    private TextField txtPredicateFrom;
    
    private TextField txtPredicateTo;

    private TextField txtPredicateOutput;

    public SequenceGeneratorVaadinDialog() {
        super(SequenceGeneratorConfig_V1.class, 
                AddonInitializer.create(new SimpleRdfConfigurator(SequenceGenerator.class)));

        buildLayout();
    }

    @Override
    public void setConfiguration(SequenceGeneratorConfig_V1 c) throws DPUConfigException {
        txtPredicateFrom.setValue(c.getPredicateStart());
        txtPredicateTo.setValue(c.getPredicateEnd());
        txtPredicateOutput.setValue(c.getPredicateOutput());
    }

    @Override
    public SequenceGeneratorConfig_V1 getConfiguration() throws DPUConfigException {
        final SequenceGeneratorConfig_V1 c = new SequenceGeneratorConfig_V1();

        if (!txtPredicateFrom.isValid() || !txtPredicateTo.isValid() || !txtPredicateOutput.isValid()) {
            throw new DPUConfigException("Invalid configuration.");
        }

        c.setPredicateStart(txtPredicateFrom.getValue());
        c.setPredicateEnd(txtPredicateTo.getValue());
        c.setPredicateOutput(txtPredicateOutput.getValue());

        return c;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtPredicateFrom = new TextField("Min value (included):");
        txtPredicateFrom.setWidth("100%");
        txtPredicateFrom.setRequired(true);
        txtPredicateFrom.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtPredicateFrom);

        txtPredicateTo = new TextField("Max value (included):");
        txtPredicateTo.setWidth("100%");
        txtPredicateTo.setRequired(true);
        txtPredicateTo.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtPredicateTo);

        txtPredicateOutput = new TextField("Output predicate:");
        txtPredicateOutput.setWidth("100%");
        txtPredicateOutput.setRequired(true);
        txtPredicateOutput.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtPredicateOutput);


        setCompositionRoot(mainLayout);
    }
}
