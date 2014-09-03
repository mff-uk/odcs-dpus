package cz.cuni.mff.xrg.uv.transformer.check.rdfnotemtpy;

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class RdfNotEmptyVaadinDialog extends AdvancedVaadinDialogBase<RdfNotEmptyConfig_V1> {

    private OptionGroup optMessageType;

    private TextField txtMessage;

    public RdfNotEmptyVaadinDialog() {
        super(RdfNotEmptyConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(RdfNotEmptyConfig_V1 c) throws DPUConfigException {
        optMessageType.setValue(c.getMessageType());
        txtMessage.setValue(c.getMessage());
    }

    @Override
    public RdfNotEmptyConfig_V1 getConfiguration() throws DPUConfigException {
        final RdfNotEmptyConfig_V1 c = new RdfNotEmptyConfig_V1();

        c.setMessage(txtMessage.getValue());
        c.setMessageType((DPUContext.MessageType) optMessageType.getValue());

        return c;
    }

    private void buildLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        optMessageType = new OptionGroup("Message type:");
        optMessageType.addItem(DPUContext.MessageType.ERROR);
        optMessageType.addItem(DPUContext.MessageType.WARNING);
        mainLayout.addComponent(optMessageType);

        txtMessage = new TextField("Message:");
        txtMessage.setWidth("100%");
        txtMessage.setInputPrompt(RdfNotEmptyConfig_V1.AUTO_MESSAGE);
        txtMessage.setNullRepresentation("");
        txtMessage.setNullSettingAllowed(true);
        mainLayout.addComponent(txtMessage);

        setCompositionRoot(mainLayout);
    }
}
