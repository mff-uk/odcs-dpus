package cz.cuni.mff.xrg.uv.transformer.xslt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.boost.dpu.vaadin.AbstractDialog;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 *
 * @author Škoda Petr
 */
public class XsltVaadinDialog extends AbstractDialog<XsltConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(XsltVaadinDialog.class);

    private CheckBox checkSkipFileOnError;

    private TextField txtTemplateName;

    private TextArea txtTemplate;

    public XsltVaadinDialog() {
        super(Xslt.class);
    }

    @Override
    public void setConfiguration(XsltConfig_V2 c) throws DPUConfigException {
        this.checkSkipFileOnError.setValue(!c.isFailOnError());
        this.txtTemplateName.setValue(c.getXsltTemplateName());
        this.txtTemplate.setValue(c.getXsltTemplate());
    }

    @Override
    public XsltConfig_V2 getConfiguration() throws DPUConfigException {
        final XsltConfig_V2 c = new XsltConfig_V2();
        c.setFailOnError(this.checkSkipFileOnError.getValue());
        c.setXsltTemplateName(this.txtTemplateName.getValue());
        c.setXsltTemplate(this.txtTemplate.getValue());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        this.checkSkipFileOnError = new CheckBox(ctx.tr("dpu.dialog.skip.on.error"));
        this.checkSkipFileOnError.setWidth("100%");
        mainLayout.addComponent(this.checkSkipFileOnError);
        mainLayout.setExpandRatio(this.checkSkipFileOnError, 0.0f);

        this.txtTemplateName = new TextField(ctx.tr("dpu.dialog.template.name"));
        this.txtTemplateName.setWidth("100%");
        mainLayout.addComponent(this.txtTemplateName);
        mainLayout.setExpandRatio(this.txtTemplateName, 0.0f);

        this.txtTemplate = new TextArea(ctx.tr("dpu.dialog.template"));
        this.txtTemplate.setSizeFull();
        mainLayout.addComponent(this.txtTemplate);
        mainLayout.setExpandRatio(this.txtTemplate, 1.0f);

        setCompositionRoot(mainLayout);
    }

}
