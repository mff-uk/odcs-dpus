package cz.cuni.mff.xrg.uv.extractor.httppost;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.container.ComponentTable;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for HttpPost.
 *
 * @author Petr Škoda
 */
public class HttpPostVaadinDialog extends AbstractDialog<HttpPostConfig_V1> {

    private TextField txtEndpoint;
    
    private ComponentTable genTable;

    public HttpPostVaadinDialog() {
        super(HttpPost.class);
    }

    @Override
    public void setConfiguration(HttpPostConfig_V1 c) throws DPUConfigException {
        genTable.setValue(c.getArguments());
        txtEndpoint.setValue(c.getEndpoint());
    }

    @Override
    public HttpPostConfig_V1 getConfiguration() throws DPUConfigException {
        final HttpPostConfig_V1 c = new HttpPostConfig_V1();

        c.getArguments().addAll(genTable.getValue());
        c.setEndpoint(txtEndpoint.getValue());

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtEndpoint = new TextField("Server address:");
        txtEndpoint.setWidth("100%");
        mainLayout.addComponent(txtEndpoint);

        genTable = new ComponentTable(HttpPostConfig_V1.Argument.class,
                new ComponentTable.ColumnInfo("name", "Action name", null, 0.3f),
                new ComponentTable.ColumnInfo("value", "Value", null, 0.7f));

        genTable.setPolicy(new ComponentTable.Policy<HttpPostConfig_V1.Argument>() {

            @Override
            public boolean isSet(HttpPostConfig_V1.Argument value) {
                return value.getName()!= null && !value.getName().isEmpty();
            }

        });
        mainLayout.addComponent(genTable);
        mainLayout.setExpandRatio(genTable, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
