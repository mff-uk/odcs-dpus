package cz.cuni.mff.xrg.uv.transformer.xslt;

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

    private CheckBox checkSkipFileOnError;

    private TextField txtOutputExtension;

    private TextField txtNumberOfExtraThreads;

    private TextArea txtTemplate;

    public XsltVaadinDialog() {
        super(Xslt.class);
    }

    @Override
    public void setConfiguration(XsltConfig_V2 c) throws DPUConfigException {
        this.checkSkipFileOnError.setValue(!c.isFailOnError());
        this.txtOutputExtension.setValue(c.getOutputFileExtension());
        this.txtNumberOfExtraThreads.setValue(Integer.toString(c.getNumberOfExtraThreads()));
        this.txtTemplate.setValue(c.getXsltTemplate());
    }

    @Override
    public XsltConfig_V2 getConfiguration() throws DPUConfigException {
        final XsltConfig_V2 c = new XsltConfig_V2();
        c.setFailOnError(!this.checkSkipFileOnError.getValue());
        c.setOutputFileExtension(this.txtOutputExtension.getValue());
        c.setXsltTemplate(this.txtTemplate.getValue());
        // Parse
        try {
            int value = Integer.parseInt(this.txtNumberOfExtraThreads.getValue());
            if (value < 0) {
                throw new DPUConfigException(ctx.tr("dpu.dialog.extraThreads.negative"));
            }
            c.setNumberOfExtraThreads(value);
        } catch (NumberFormatException ex) {
            throw new DPUConfigException(ctx.tr("dpu.dialog.extraThreads.formatException"));
        }
        // Check file extension.
        if (!c.getOutputFileExtension().isEmpty() && !c.getOutputFileExtension().startsWith(".") ) {
            throw new DPUConfigException(ctx.tr("dpu.dialog.template.output.extension.formatExeption"));
        }
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

        this.txtOutputExtension = new TextField(ctx.tr("dpu.dialog.template.output.extension"));
        this.txtOutputExtension.setWidth("100%");
        this.txtOutputExtension.setDescription(ctx.tr("dpu.dialog.template.output.extension.desc"));
        mainLayout.addComponent(this.txtOutputExtension);
        mainLayout.setExpandRatio(this.txtOutputExtension, 0.0f);

        this.txtNumberOfExtraThreads = new TextField(ctx.tr("dpu.dialog.extraThreads"));
        this.txtNumberOfExtraThreads.setWidth("100%");
        mainLayout.addComponent(this.txtNumberOfExtraThreads);
        mainLayout.setExpandRatio(this.txtNumberOfExtraThreads, 0.0f);


        this.txtTemplate = new TextArea(ctx.tr("dpu.dialog.template"));
        this.txtTemplate.setSizeFull();
        mainLayout.addComponent(this.txtTemplate);
        mainLayout.setExpandRatio(this.txtTemplate, 1.0f);

        setCompositionRoot(mainLayout);
    }

}
