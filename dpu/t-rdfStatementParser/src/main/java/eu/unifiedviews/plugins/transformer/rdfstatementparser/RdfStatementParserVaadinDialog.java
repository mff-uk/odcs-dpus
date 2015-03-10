package eu.unifiedviews.plugins.transformer.rdfstatementparser;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.container.ComponentTable;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 *
 * @author Škoda Petr
 */
public class RdfStatementParserVaadinDialog extends AbstractDialog<RdfStatementParserConfig_V2> {

    private TextArea txtQuery;

    private ComponentTable<RdfStatementParserConfig_V2.ActionInfo> tableAction;

    private CheckBox checkTransferLabels;

    public RdfStatementParserVaadinDialog() {
        super(RdfStatementParser.class);
    }

    @Override
    public void setConfiguration(RdfStatementParserConfig_V2 conf)
            throws DPUConfigException {

        txtQuery.setValue(conf.getSelectQuery());
        checkTransferLabels.setValue(conf.isTransferLabels());

        tableAction.setValue(conf.getActions());
    }

    @Override
    public RdfStatementParserConfig_V2 getConfiguration() throws DPUConfigException {
        if (!txtQuery.isValid()) {
            throw new DPUConfigException("Query must be provided.");
        }

        final RdfStatementParserConfig_V2 conf = new RdfStatementParserConfig_V2();
        conf.setSelectQuery(txtQuery.getValue());
        conf
                .setTransferLabels(checkTransferLabels.getValue() == null ? false : checkTransferLabels
                        .getValue());

        conf.getActions().addAll(tableAction.getValue());
        return conf;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(new Label(
                "Query used to get values.Query must select "
                + "URI as <b>?subject</b>, this value will be used as subject "
                + "to generated triples. Selected values for example '?o' then this "
                + "value can be refered from action table as 'o'.", ContentMode.HTML));

        txtQuery = new TextArea();
        txtQuery.setWidth("100%");
        txtQuery.setRows(3);
        txtQuery.setRequired(true);
        txtQuery.setRequiredError("Query must be provided.");
        mainLayout.addComponent(txtQuery);

        checkTransferLabels = new CheckBox("Transfer labels");
        checkTransferLabels.setDescription(
                "If checked then labels from selected statments are applied to newly created. Use to transfer for example language tags.");
        mainLayout.addComponent(checkTransferLabels);

        tableAction = new ComponentTable(RdfStatementParserConfig_V2.ActionInfo.class,
                new ComponentTable.ColumnInfo("name", "Name", null, 0.2f),
                new ComponentTable.ColumnInfo("actionType", "Action type", null, 0.2f),
                new ComponentTable.ColumnInfo("actionData", "Action data", null, 1.0f));
        tableAction.setSizeFull();
        tableAction.setPolicy(new ComponentTable.Policy<RdfStatementParserConfig_V2.ActionInfo>() {

            @Override
            public boolean isSet(RdfStatementParserConfig_V2.ActionInfo value) {
                return value.getName() != null && !value.getName().isEmpty();
            }

        });

        mainLayout.addComponent(tableAction);
        mainLayout.setExpandRatio(tableAction, 1.0f);
        
        mainLayout.addComponent(new Label(
                "Action list. Use bindings from query"
                + " as a \"Group name\" for groups. <br/>"
                + "If \"Create triple\" is checked then \"Action data\" "
                + "represents predicate in created triple, otherwise it "
                + "represents regular expresion used to parse the value.<br/>"
                + "Use named groups (?&lt;group_name&gt;.*) and same "
                + "\"Group name\" to perform additional actions on given group."
                + "Actions can be recursive ie. \"Group name\" value can be "
                + "used in respective regular expression as named group. Same group name"
                + "can be used more then once.",
                ContentMode.HTML));

        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);

        setCompositionRoot(panel);
    }
}
