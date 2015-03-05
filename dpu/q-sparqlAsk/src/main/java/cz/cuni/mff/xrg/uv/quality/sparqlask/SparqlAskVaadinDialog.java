package cz.cuni.mff.xrg.uv.quality.sparqlask;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog.
 */
public class SparqlAskVaadinDialog extends AbstractDialog<SparqlAskConfig_V1> {

    private CheckBox checkPerGraph;

    private OptionGroup optMessageType;

    private TextField txtMessage;
    
    private TextArea txtAskQuery;

    public SparqlAskVaadinDialog() {
        super(SparqlAsk.class);
    }

    @Override
    public void setConfiguration(SparqlAskConfig_V1 c) throws DPUConfigException {
        checkPerGraph.setValue(c.isPerGraph());
        optMessageType.setValue(c.getMessageType());
        txtMessage.setValue(c.getMessage());
        txtAskQuery.setValue(c.getAskQuery());
    }

    @Override
    public SparqlAskConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlAskConfig_V1 c = new SparqlAskConfig_V1();

        c.setPerGraph(checkPerGraph.getValue());
        c.setMessage(txtMessage.getValue());
        c.setMessageType((DPUContext.MessageType) optMessageType.getValue());
        c.setAskQuery(txtAskQuery.getValue());

        return c;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        checkPerGraph = new CheckBox("Per graph");
        checkPerGraph.setWidth("100%");
        mainLayout.addComponent(checkPerGraph);

        optMessageType = new OptionGroup("Message type:");
        optMessageType.addItem(DPUContext.MessageType.ERROR);
        optMessageType.addItem(DPUContext.MessageType.WARNING);
        mainLayout.addComponent(optMessageType);

        txtMessage = new TextField("Message:");
        txtMessage.setWidth("100%");
        txtMessage.setInputPrompt(SparqlAskConfig_V1.AUTO_MESSAGE);
        txtMessage.setNullRepresentation("");
        txtMessage.setNullSettingAllowed(true);
        mainLayout.addComponent(txtMessage);

        txtAskQuery = new TextArea("Ask query:");
        txtAskQuery.setSizeFull();
        txtAskQuery.setNullRepresentation("");
        txtAskQuery.setNullSettingAllowed(true);
        mainLayout.addComponent(txtAskQuery);
        mainLayout.setExpandRatio(txtAskQuery, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
