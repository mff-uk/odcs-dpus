package cz.cuni.mff.xrg.uv.extractor.httppost;

import java.util.Collections;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.container.ComponentTable;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for HttpPost. Enables configuration of only a single post request.
 *
 * @author Petr Škoda
 */
public class HttpPostVaadinDialog extends AbstractDialog<HttpPostConfig_V2> {

    private CheckBox checkIgnoreDialog;

    private TextField txtEndpoint;

    private TextField txtFileName;
    
    private ComponentTable<HttpPostConfig_V2.Argument> genTable;

    public HttpPostVaadinDialog() {
        super(HttpPost.class);
    }

    @Override
    public void setConfiguration(HttpPostConfig_V2 c) throws DPUConfigException {
        if (c.getRequest().isEmpty()) {
            genTable.setValue(Collections.EMPTY_LIST);
            txtFileName.setValue("");
            txtEndpoint.setValue("");
            checkIgnoreDialog.setValue(true);
        } else {
            // Edit the first one.
            genTable.setValue(c.getRequest().get(0).getArguments());
            txtFileName.setValue(c.getRequest().get(0).getFileName());
            txtEndpoint.setValue(c.getRequest().get(0).getUri());
            checkIgnoreDialog.setValue(false);
        }
    }

    @Override
    public HttpPostConfig_V2 getConfiguration() throws DPUConfigException {
        final HttpPostConfig_V2 c = new HttpPostConfig_V2();

        if (!checkIgnoreDialog.getValue()) {
            final HttpPostConfig_V2.Request request = new HttpPostConfig_V2.Request();
            c.getRequest().add(request);

            request.getArguments().addAll(genTable.getValue());
            request.setUri(txtEndpoint.getValue());
            request.setFileName(txtFileName.getValue());
        }
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        checkIgnoreDialog = new CheckBox("Ignore dialog setting");
        checkIgnoreDialog.setDescription("If checked then no post request is made based on the dialog configuration."
                + "Designed to be used together with rdf configuration.");
        mainLayout.addComponent(checkIgnoreDialog);

        txtEndpoint = new TextField("Server address:");
        txtEndpoint.setWidth("100%");
        mainLayout.addComponent(txtEndpoint);

        txtFileName = new TextField("File name:");
        txtFileName.setWidth("100%");
        mainLayout.addComponent(txtFileName);

        genTable = new ComponentTable(HttpPostConfig_V2.Argument.class,
                new ComponentTable.ColumnInfo("name", "Action name", null, 0.3f),
                new ComponentTable.ColumnInfo("value", "Value", null, 0.7f));

        genTable.setPolicy(new ComponentTable.Policy<HttpPostConfig_V2.Argument>() {

            @Override
            public boolean isSet(HttpPostConfig_V2.Argument value) {
                return value.getName()!= null && !value.getName().isEmpty();
            }

        });
        mainLayout.addComponent(genTable);
        mainLayout.setExpandRatio(genTable, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
