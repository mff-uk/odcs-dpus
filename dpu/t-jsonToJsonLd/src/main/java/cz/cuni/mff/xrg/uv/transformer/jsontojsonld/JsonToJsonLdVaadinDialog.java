package cz.cuni.mff.xrg.uv.transformer.jsontojsonld;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for JsonToJsonLd.
 *
 * @author Škoda Petr
 */
public class JsonToJsonLdVaadinDialog extends AbstractDialog<JsonToJsonLdConfig_V1> {

    private TextField txtContext;
    
    private TextField txtEncoding;

    public JsonToJsonLdVaadinDialog() {
        super(JsonToJsonLd.class);
    }

    @Override
    public void setConfiguration(JsonToJsonLdConfig_V1 c) throws DPUConfigException {
        txtContext.setValue(c.getContext());
        txtEncoding.setValue(c.getEncoding());
    }

    @Override
    public JsonToJsonLdConfig_V1 getConfiguration() throws DPUConfigException {
        final JsonToJsonLdConfig_V1 c = new JsonToJsonLdConfig_V1();

        c.setContext(txtContext.getValue());
        c.setEncoding(txtEncoding.getValue());

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(new Label("Context must be dereferencable! 'http://schema.org' seems to be reasonble default (workaround)."));

        txtContext = new TextField("Used context:");
        txtContext.setWidth("100%");
        mainLayout.addComponent(txtContext);

        txtEncoding = new TextField("Used context:");
        txtEncoding.setWidth("100%");
        mainLayout.addComponent(txtEncoding);

        setCompositionRoot(mainLayout);
    }
}
