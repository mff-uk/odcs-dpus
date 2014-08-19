package cz.cuni.mff.xrg.uv.transformer.tabular;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.gui.PropertyComponentGroup;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParserType;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabularVaadinDialog extends AdvancedVaadinDialogBase<TabularConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(
            TabularVaadinDialog.class);

    private OptionGroup optionTableType;

    private TextField txtBaseUri;

    private TextField txtKeyColumnName;

    private TextField txtEncoding;

    private TextField txtRowsLimit;

    private TextField txtRowsClass;

    private CheckBox checkHasHeader;

    private CheckBox checkGenerateNew;

    private CheckBox checkIgnoreBlankCell;

    private CheckBox checkStaticRowCounter;

    private TextField txtCsvQuoteChar;

    private TextField txtCsvDelimeterChar;

    private TextField txtCsvLinesToIgnore;

    /**
     * Layout for column mapping.
     */
    private GridLayout propertiesLayout;

    private final List<PropertyComponentGroup> properties = new LinkedList<>();

    private Panel mainPanel;

    private VerticalLayout mainLayout;

    /**
     * If true then the composite root has already been set.
     */
    private boolean layoutSet = false;

	public TabularVaadinDialog() {
		super(TabularConfig_V1.class, AddonInitializer.noAddons());
        try {
            buildMainLayout();
            buildImportTab();
        } catch (Exception ex) {
            LOG.error("TabularVaadinDialog", ex);
            throw ex;
        }
	}
	
	private void buildMainLayout() {

        // ------------------------ General ------------------------

        final FormLayout generalLayout = new FormLayout();
		generalLayout.setImmediate(true);
		generalLayout.setWidth("100%");
		generalLayout.setHeight("-1px");

		this.optionTableType = new OptionGroup("Choose the input type:");
        this.optionTableType.setImmediate(true);
		this.optionTableType.addItem(ParserType.CSV);
		this.optionTableType.addItem(ParserType.DBF);
        this.optionTableType.setNullSelectionAllowed(false);
        this.optionTableType.setValue(ParserType.CSV);
		generalLayout.addComponent(this.optionTableType);

        this.txtBaseUri = new TextField("Resource URI base");
        this.txtBaseUri.setWidth("100%");
        this.txtBaseUri.setRequired(true);
        this.txtBaseUri.setRequiredError("Resource URI base must be supplied.");
        this.txtBaseUri.setDescription("This value is used as base URI for automatic column property generation "
                + "and also to create absolute URI if relative uri is provided in 'Property URI' column.");
        generalLayout.addComponent(this.txtBaseUri);

        this.txtKeyColumnName = new TextField("Key column");
        this.txtKeyColumnName.setNullRepresentation("");
        this.txtKeyColumnName.setWidth("100%");
        generalLayout.addComponent(this.txtKeyColumnName);

        this.txtEncoding = new TextField("Encoding");
//        this.txtEncoding.setInputPrompt("UTF-8, Cp1250, ...");
        this.txtEncoding.setRequired(true);
        generalLayout.addComponent(this.txtEncoding);

        this.txtRowsLimit = new TextField("Rows limit");
        this.txtRowsLimit.setNullRepresentation("");
        generalLayout.addComponent(this.txtRowsLimit);

        this.txtRowsClass = new TextField("Class for a row object");
        this.txtRowsClass.setWidth("100%");
        this.txtRowsClass.setNullRepresentation("");
        this.txtRowsClass.setNullSettingAllowed(true);
        generalLayout.addComponent(this.txtRowsClass);

        this.checkHasHeader = new CheckBox("Has header");
        this.checkHasHeader.setDescription("Uncheck if there is no header in given file. "
                        + "The columns are then accessible under names col0, col1, ..");
        generalLayout.addComponent(this.checkHasHeader);

        this.checkGenerateNew = new CheckBox("Full column mapping");
        this.checkGenerateNew.setDescription("If true then default mapping is generated for every column.");
        generalLayout.addComponent(this.checkGenerateNew);

        this.checkIgnoreBlankCell = new CheckBox("Ignore blank cells");
        this.checkIgnoreBlankCell.setDescription("If unchecked and and cell is blank then URI for blank cell is inserted else cell is ignored.");
        generalLayout.addComponent(this.checkIgnoreBlankCell);

        this.checkStaticRowCounter = new CheckBox("Use static row counter");
        this.checkStaticRowCounter.setDescription("If checked and multiple files are precessed, then those files share the same row counter."
                + "The process can be viewsed as if files are appended before parsing.");
        generalLayout.addComponent(checkStaticRowCounter);

        // -------------------------- CSV ----------------------------

        final FormLayout csvLayout = new FormLayout();
		csvLayout.setImmediate(true);
        csvLayout.setSpacing(true);
		csvLayout.setWidth("100%");
		csvLayout.setHeight("-1px");
        csvLayout.addComponent(new Label("CSV specific settings"));

        this.txtCsvQuoteChar = new TextField("Quote char");
//        this.txtCsvQuoteChar.setInputPrompt("\"");
        this.txtCsvQuoteChar.setNullRepresentation("");
        this.txtCsvQuoteChar.setRequired(true);
        csvLayout.addComponent(this.txtCsvQuoteChar);

        this.txtCsvDelimeterChar = new TextField("Delimiter char");
//        this.txtCsvDelimeterChar.setInputPrompt(",");
        this.txtCsvDelimeterChar.setNullRepresentation("");
        this.txtCsvDelimeterChar.setRequired(true);
        csvLayout.addComponent(this.txtCsvDelimeterChar);

        this.txtCsvLinesToIgnore = new TextField("Skip n first lines");
        this.txtCsvLinesToIgnore.setNullRepresentation("");
        this.txtCsvQuoteChar.setRequired(true);
        csvLayout.addComponent(this.txtCsvLinesToIgnore);

        // add change listener
        this.optionTableType.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                final ParserType value = (ParserType)event.getProperty().getValue();
                boolean csvEnabled = ParserType.CSV == value;
                txtCsvQuoteChar.setEnabled(csvEnabled);
                txtCsvDelimeterChar.setEnabled(csvEnabled);
                txtCsvLinesToIgnore.setEnabled(csvEnabled);
            }
        });

        // --------------------- Mapping - simple ---------------------

        this.propertiesLayout = new GridLayout(5,1);
		this.propertiesLayout.setWidth("100%");
		this.propertiesLayout.setHeight("-1px");
        this.propertiesLayout.setImmediate(true);
        this.propertiesLayout.setSpacing(true);
        this.propertiesLayout.setMargin(new MarginInfo(true));

        //  add headers
        this.propertiesLayout.addComponent(new Label("Column name"));
        this.propertiesLayout.setColumnExpandRatio(0, 0.3f);

        this.propertiesLayout.addComponent(new Label("Output type"));
        this.propertiesLayout.setColumnExpandRatio(1, 0.0f);

        this.propertiesLayout.addComponent(new Label("Property URI"));
        this.propertiesLayout.setColumnExpandRatio(2, 0.7f);

        this.propertiesLayout.addComponent(new Label("Use Dbf types"));
        this.propertiesLayout.setColumnExpandRatio(3, 0.0f);

        this.propertiesLayout.addComponent(new Label("Language"));
        this.propertiesLayout.setColumnExpandRatio(4, 0.0f);

        addSimplePropertyMapping(null, null);

        // -------------------------------------------------------------

        final TabSheet propertiesTab = new TabSheet();
        propertiesTab.setSizeFull();

        propertiesTab.addTab(this.propertiesLayout, "Simple");

        // -------------------------------------------------------------

        // top layout with configuration
        final HorizontalLayout configLayout = new HorizontalLayout();
		configLayout.setWidth("100%");
		configLayout.setHeight("-1px");
        configLayout.setSpacing(true);

        configLayout.addComponent(generalLayout);
        configLayout.addComponent(csvLayout);


        // main layout for whole dialog
        mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
        mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.addComponent(configLayout);
        mainLayout.setExpandRatio(configLayout, 0.0f);

        mainLayout.addComponent(new Label("Mapping"));

        mainLayout.addComponent(propertiesTab);
        mainLayout.setExpandRatio(propertiesTab, 1.0f);

        final Button btnAddMapping = new Button("Add mapping");
        btnAddMapping.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                // add empty line with mapping
                if (propertiesTab.getSelectedTab() == propertiesLayout) {
                    addSimplePropertyMapping(null, null);
                } else {
                    LOG.error("No tabs selected!");
                }
            }
        });
        mainLayout.addComponent(btnAddMapping);
        mainLayout.setExpandRatio(configLayout, 0.0f);

		mainPanel = new Panel();
        mainPanel.setContent(mainLayout);
        mainPanel.setSizeFull();

        setCompositionRoot(mainPanel);
        // composite root can be updated in
        // setConfiguration method
	}

    private void buildImportTab() {

        final FormLayout generalLayout = new FormLayout();
		generalLayout.setImmediate(true);
        generalLayout.setMargin(true);
		generalLayout.setWidth("100%");
		generalLayout.setHeight("-1px");

        generalLayout.addComponent(new Label("Here you can insert \t separated "
                + "names of columns. After pressing import given Input is "
                + "paresed and used as column names in Mapping. "
                + "Old data ARE LOST!",
                ContentMode.HTML));

        final Button btnImportColNames = new Button("Import column names");
        generalLayout.addComponent(btnImportColNames);

        final TextArea txtColNames = new TextArea("Input");
        txtColNames.setSizeFull();
        generalLayout.addComponent(txtColNames);

        btnImportColNames.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                parseStringForColumnNames(txtColNames.getValue());

                Notification.show("Import", "Column name import done.",
                        Notification.Type.HUMANIZED_MESSAGE);
            }
        });

        this.addTab(generalLayout, "Import");
    }

    private void addSimplePropertyMapping(String name, ColumnInfo_V1 setting) {
        final PropertyComponentGroup newGroup =
                new PropertyComponentGroup(propertiesLayout);
        properties.add(newGroup);

        if (name != null && setting != null) {
            newGroup.set(name, setting);
        }
    }

    /**
     * Parse given string and use it to set column (properties) names. Original
     * data are lost.
     *
     * @param str
     */
    private void parseStringForColumnNames(String str) {
        final String[] columnNames = str.split("\t");
        for (int index = 0; index < columnNames.length; ++index) {
            if (index >= properties.size()) {
                addSimplePropertyMapping(columnNames[index], 
                        new ColumnInfo_V1());
            } else {
                // use existing
                properties.get(index).set(columnNames[index], 
                        new ColumnInfo_V1());
            }
        }
    }

	@Override
	protected void setConfiguration(TabularConfig_V1 c) throws DPUConfigException {
        //
        // update dialog, as the isTempalte is decided at the begining
        // this should occure only once per dialog creation
        //
        if (!layoutSet) {
            if (getContext().isTemplate()) {
                setCompositionRoot(mainLayout);
            } else {
                setCompositionRoot(mainPanel);
            }
        }
        //
        txtKeyColumnName.setValue(c.getKeyColumnName());
        //
        // save uri
        //
        String uriStr = c.getBaseURI();
        if (!uriStr.endsWith("/")) {
            uriStr = uriStr + "/";
        }
        try {
            new java.net.URI(uriStr);
        } catch (URISyntaxException ex) {
            throw new DPUConfigException("Base URI has invalid format", ex);

        }
        txtBaseUri.setValue(uriStr);
        //
        // column info add
        //
        int index = 0;
        for (String key : c.getColumnsInfo().keySet()) {
            ColumnInfo_V1 info = c.getColumnsInfo().get(key);
            if (index >= properties.size()) {
                addSimplePropertyMapping(key, info);
            } else {
                // use existing
                properties.get(index).set(key, info);
            }
            index++;
        }
        // clear old
        for (;index < properties.size(); ++index) {
            properties.get(index).clear();
        }
        //
        // csv data
        //
        if (c.getTableType() == ParserType.CSV) {
            txtCsvQuoteChar.setValue(c.getQuoteChar());
            txtCsvDelimeterChar.setValue(c.getDelimiterChar());
            txtCsvLinesToIgnore.setValue(c.getLinesToIgnore().toString());
        } else {
            txtCsvQuoteChar.setValue("\"");
            txtCsvDelimeterChar.setValue(",");
            txtCsvLinesToIgnore.setValue("0");
        }
        //
        // other data
        //
        txtEncoding.setValue(c.getEncoding());
        if (c.getRowsLimit() == null) {
            txtRowsLimit.setValue(null);
        } else {
            txtRowsLimit.setValue(c.getRowsLimit().toString());
        }
        optionTableType.setValue(c.getTableType());
        checkHasHeader.setValue(c.isHasHeader());
        checkGenerateNew.setValue(c.isGenerateNew());
        txtRowsClass.setValue(c.getRowsClass());
        checkIgnoreBlankCell.setValue(c.isIgnoreBlankCells());
        checkStaticRowCounter.setValue(c.isStaticRowCounter());
    }

	@Override
	protected TabularConfig_V1 getConfiguration() throws DPUConfigException {
		TabularConfig_V1 cnf = new TabularConfig_V1();

        // check global validity
        if (!txtBaseUri.isValid() || !txtEncoding.isValid()) {
            throw new DPUConfigException("Configuration contains invalid inputs.");
        }
        
        cnf.setKeyColumnName(txtKeyColumnName.getValue());
        cnf.setBaseURI(txtBaseUri.getValue());
        //
        // column info
        //
        for (PropertyComponentGroup item : properties) {
            final String name = item.getColumnName();
            if (name != null) {
                cnf.getColumnsInfo().put(name, item.get());
            }
        }
        //
        // csv data
        //
        final ParserType value = (ParserType)optionTableType.getValue();
        if (value == ParserType.CSV) {

            if (!txtCsvQuoteChar.isValid() || !txtCsvDelimeterChar.isValid() ||
                    !txtCsvLinesToIgnore.isValid()) {
                throw new DPUConfigException(
                        "CSV configuration contains invalid inputs.");
            }

            cnf.setQuoteChar(txtCsvQuoteChar.getValue());
            cnf.setDelimiterChar(txtCsvDelimeterChar.getValue());
            try {
                final String linesToSkipStr = txtCsvLinesToIgnore.getValue();
                if (linesToSkipStr == null) {
                    cnf.setLinesToIgnore(0);
                } else {
                    cnf.setLinesToIgnore(
                        Integer.parseInt(linesToSkipStr));
                }
            } catch (NumberFormatException ex) {
                throw new DPUConfigException("Wrong format of lines to skip.",
                        ex);
            }
        }
        //
        // other data
        //
        cnf.setEncoding(txtEncoding.getValue());

        final String rowsLimitStr = txtRowsLimit.getValue();
        if (rowsLimitStr == null) {
            cnf.setRowsLimit(null);
        } else {
            try {
                cnf.setRowsLimit(Integer.parseInt(rowsLimitStr));
            } catch (NumberFormatException ex) {
                throw new DPUConfigException("Wrong format of row limit.", ex);
            }
        }

        cnf.setTableType((ParserType)optionTableType.getValue());
        cnf.setHasHeader(checkHasHeader.getValue());
        cnf.setGenerateNew(checkGenerateNew.getValue());
        cnf.setIgnoreBlankCells(checkIgnoreBlankCell.getValue());
        cnf.setStaticRowCounter(checkStaticRowCounter.getValue());

        if (txtRowsClass.getValue() == null) {
            cnf.setRowsClass(null);
        } else {
            // try parse URI
            try {
                new java.net.URI(txtRowsClass.getValue());
            } catch(URISyntaxException ex) {
                throw new DPUConfigException("Wrong uri for row class.", ex);
            }
            cnf.setRowsClass(txtRowsClass.getValue());
        }
        //
        // additional checks
        //
        if (!cnf.isGenerateNew() && cnf.getColumnsInfo().isEmpty()) {
            throw new DPUConfigException("Specify at least one column mapping or check 'Full column mapping' option.");
        }

		return cnf;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
				
		return desc.toString();
	}

}
