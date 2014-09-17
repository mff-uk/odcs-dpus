package eu.unifiedviews.plugins.transformer.rdfstatementparser;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 *
 * @author Škoda Petr
 */
public class RdfStatementParserVaadinDialog
        extends AdvancedVaadinDialogBase<RdfStatementParserConfig_V1> {

    private static final String COL_NAME = "Group name";

    private static final String COL_GEN_TRIPLE = "Create triple";

    private static final String COL_DATA = "Action data";

    private TextArea txtQuery;

    private Table tableAction;

    private CheckBox checkTransferLabels;

    private Integer rowIndex = 0;

    public RdfStatementParserVaadinDialog() {
        super(RdfStatementParserConfig_V1.class, 
                AddonInitializer.create(new SimpleRdfConfigurator(RdfStatementParser.class)));

        buildLayout();
    }

    @Override
    public void setConfiguration(RdfStatementParserConfig_V1 conf)
            throws DPUConfigException {

        txtQuery.setValue(conf.getSelectQuery());
        checkTransferLabels.setValue(conf.isTransferLabels());
        tableAction.removeAllItems();
        for (String key : conf.getActions().keySet()) {
            final RdfStatementParserConfig_V1.ActionInfo actionInfo
                    = conf.getActions().get(key);
            // add into table
            tableAction.addItem(
                    new Object[]{
                        key,
                        actionInfo.getActionType() == RdfStatementParserConfig_V1.ActionType.CreateTriple,
                        actionInfo.getActionData()},
                    rowIndex++);
        }
    }

    @Override
    public RdfStatementParserConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtQuery.isValid()) {
            throw new DPUConfigException("Query must be provided.");
        }

        final RdfStatementParserConfig_V1 conf = new RdfStatementParserConfig_V1();
        conf.setSelectQuery(txtQuery.getValue());
        conf.setTransferLabels(checkTransferLabels.getValue() == null ? false : checkTransferLabels.getValue());
        for (Object key : tableAction.getItemIds()) {
            final Item item = tableAction.getItem(key);

            final String name
                    = (String) item.getItemProperty(COL_NAME).getValue();
            final Boolean genTriples
                    = (Boolean) item.getItemProperty(COL_GEN_TRIPLE).getValue();
            final String data
                    = (String) item.getItemProperty(COL_DATA).getValue();
            // prepare
            final RdfStatementParserConfig_V1.ActionType actionType
                    = genTriples ? RdfStatementParserConfig_V1.ActionType.CreateTriple
                    : RdfStatementParserConfig_V1.ActionType.RegExp;

            if (name != null && !name.trim().isEmpty()) {

                if (data == null || data.trim().isEmpty()) {
                    // data no set
                    throw new DPUConfigException("Missing value for: " + name);
                }
                // create info and add
                final RdfStatementParserConfig_V1.ActionInfo info
                        = new RdfStatementParserConfig_V1.ActionInfo(actionType,
                                data);
                conf.getActions().put(name, info);
            } else {
                // something is missing -> ignore
            }
        }

        return conf;
    }

    private void buildLayout() {
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
        checkTransferLabels.setDescription("If checked then labels from selected statments are applied to newly created. Use to transfer for example language tags.");
        mainLayout.addComponent(checkTransferLabels);

        mainLayout.addComponent(new Label(
                "Action list. Use bindings from query"
                + " as a \"Group name\" for groups. <br/>"
                + "If \"Create triple\" is checked then \"Action data\" "
                + "represents predicate in created triple, otherwise it "
                + "represents regular expresion used to parse the value.<br/>"
                + "Use named groups (?&lt;group_name\\&gt;.*) and same "
                + "\"Group name\" to perform additional actions on given group."
                + "Actions can be recursive ie. \"Group name\" value can be "
                + "used in respective regular expression as named group.",
                ContentMode.HTML));

        final Button btnAddRow = new Button("Add new row");
        mainLayout.addComponent(btnAddRow);

        tableAction = new Table();
        tableAction.addStyleName("components-inside");
        tableAction.setSizeFull();

        tableAction.addContainerProperty(COL_NAME, String.class, null);
        tableAction.setColumnWidth(COL_NAME, 90);

        tableAction.addContainerProperty(COL_GEN_TRIPLE, Boolean.class, null);
        tableAction.setColumnWidth(COL_GEN_TRIPLE, 90);

        tableAction.addContainerProperty(COL_DATA, String.class, null);
        tableAction.setColumnExpandRatio(COL_DATA, 1.0f);
        tableAction.setColumnHeader(COL_DATA,
                "Action data - URI of predicate / regular expression");

        tableAction.setEditable(true);
        tableAction.setImmediate(true);

        tableAction.setTableFieldFactory(new DefaultFieldFactory() {

            @Override
            public Field createField(Container container, Object itemId,
                    Object propertyId, Component uiContext) {

                if (propertyId == COL_NAME || propertyId == COL_DATA) {
                    final TextField tx = new TextField();
                    tx.setWidth("100%");
                    return tx;
                } else if (propertyId == COL_GEN_TRIPLE) {
                    return new CheckBox();
                } else {
                    return super.createField(container, itemId, propertyId,
                            uiContext);
                }
            }

        });

        mainLayout.addComponent(tableAction);
        setCompositionRoot(mainLayout);

        // add action listener
        btnAddRow.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                tableAction.addItem(new Object[]{"", false, ""}, rowIndex++);
            }
        });
    }
}
