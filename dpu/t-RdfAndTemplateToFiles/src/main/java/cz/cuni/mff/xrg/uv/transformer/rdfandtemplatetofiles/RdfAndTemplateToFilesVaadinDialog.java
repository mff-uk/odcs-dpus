package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for RdfAndTemplateToFiles.
 *
 * @author Petr Škoda
 */
public class RdfAndTemplateToFilesVaadinDialog extends AbstractDialog<RdfAndTemplateToFilesConfig_V1> {

    private CheckBox checkSoftFail;
    
    private TextArea txtTemplate;

    public RdfAndTemplateToFilesVaadinDialog() {
        super(RdfAndTemplateToFiles.class);
    }

    @Override
    public void setConfiguration(RdfAndTemplateToFilesConfig_V1 c) throws DPUConfigException {
        checkSoftFail.setValue(c.isSoftFail());
        txtTemplate.setValue(c.getTemplate());
    }

    @Override
    public RdfAndTemplateToFilesConfig_V1 getConfiguration() throws DPUConfigException {
        final RdfAndTemplateToFilesConfig_V1 c = new RdfAndTemplateToFilesConfig_V1();

        c.setSoftFail(checkSoftFail.getValue());
        c.setTemplate(txtTemplate.getValue());

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        checkSoftFail = new CheckBox("Skip on error");
        checkSoftFail.setDescription("If cheked and some document creation fail then the document is skipped.");
        checkSoftFail.setWidth("100%");
        mainLayout.addComponent(checkSoftFail);
        mainLayout.setExpandRatio(checkSoftFail, 0.0f);

        txtTemplate = new TextArea("Template");
        txtTemplate.setSizeFull();
        mainLayout.addComponent(txtTemplate);
        mainLayout.setExpandRatio(txtTemplate, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
