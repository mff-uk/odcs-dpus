package cz.cuni.mff.xrg.uv.extractor.textholder;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.boost.dpu.vaadin.AbstractDialog;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * 
 * @author Škoda Petr
 */
public class TextHolderVaadinDialog extends AbstractDialog<TextHolderConfig_V1> {

    private TextField txtName;
    
    private TextArea txtValue;

    public TextHolderVaadinDialog() {
        super(TextHolder.class);
    }

    @Override
    public void setConfiguration(TextHolderConfig_V1 c) throws DPUConfigException {
        txtName.setValue(c.getFileName());
        txtValue.setValue(c.getText());
    }

    @Override
    public TextHolderConfig_V1 getConfiguration() throws DPUConfigException {
        final TextHolderConfig_V1 c = new TextHolderConfig_V1();
        c.setFileName(txtName.getValue());
        c.setText(txtValue.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
    	final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtName = new TextField(ctx.tr("dpu.dialog.output.file"));
        txtName.setWidth("100%");
        txtName.setRequired(true);
        mainLayout.addComponent(txtName);
        mainLayout.setExpandRatio(txtName, 0);

        txtValue = new TextArea(ctx.tr("dpu.dialog.output.content"));
        txtValue.setSizeFull();
        mainLayout.addComponent(txtValue);
        mainLayout.setExpandRatio(txtValue, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
