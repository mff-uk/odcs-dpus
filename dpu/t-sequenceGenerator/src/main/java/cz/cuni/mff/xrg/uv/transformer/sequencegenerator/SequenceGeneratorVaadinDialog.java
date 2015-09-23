package cz.cuni.mff.xrg.uv.transformer.sequencegenerator;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

public class SequenceGeneratorVaadinDialog extends AbstractDialog<SequenceGeneratorConfig_V1> {

    private TextField txtPredicateFrom;
    
    private TextField txtPredicateTo;

    private TextField txtPredicateOutput;

    public SequenceGeneratorVaadinDialog() {
        super(SequenceGenerator.class);
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

    @Override
    protected void buildDialogLayout() {
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
