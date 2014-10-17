package cz.cuni.mff.xrg.uv.extractor.textholder;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class TextHolderVaadinDialog extends AdvancedVaadinDialogBase<TextHolderConfig_V1> {

    private TextField txtName;
    
    private TextArea txtValue;

    public TextHolderVaadinDialog() {
        super(TextHolderConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
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

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtName = new TextField("Output file name:");
        txtName.setWidth("100%");
        txtName.setRequired(true);
        mainLayout.addComponent(txtName);
        mainLayout.setExpandRatio(txtName, 0);

        txtValue = new TextArea("File's content:");
        txtValue.setSizeFull();
        mainLayout.addComponent(txtValue);
        mainLayout.setExpandRatio(txtValue, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
