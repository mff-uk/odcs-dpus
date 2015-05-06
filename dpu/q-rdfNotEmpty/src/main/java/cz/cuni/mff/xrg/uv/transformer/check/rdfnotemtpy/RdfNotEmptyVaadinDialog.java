package cz.cuni.mff.xrg.uv.transformer.check.rdfnotemtpy;

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class RdfNotEmptyVaadinDialog extends AbstractDialog<RdfNotEmptyConfig_V1> {

    private OptionGroup optMessageType;

    private TextField txtMessage;

    public RdfNotEmptyVaadinDialog() {
        super(RdfNotEmpty.class);
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

    @Override
    public void buildDialogLayout() {
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
