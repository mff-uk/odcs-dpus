package cz.cuni.mff.xrg.uv.transformer;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
//import eu.unifiedviews.helpers.dpu.vaadin.tabs.ConfigCopyPaste;
import eu.unifiedviews.helpers.dpu.vaadin.container.ComponentTable;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.tabs.ConfigCopyPaste;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

/**
 *
 * @author Škoda Petr
 */
public class HtmlCssVaadinDialog extends AbstractDialog<HtmlCssConfig_V1> {

    private ComponentTable genTable;

    private TextField txtClass;

    private TextField txtHasPredicate;

    private CheckBox checkGenerateSourceInfo;

    public HtmlCssVaadinDialog() {
        super(HtmlCss.class);

    }

    @Override
    public void setConfiguration(HtmlCssConfig_V1 c) throws DPUConfigException {
        genTable.setValue(c.getActions());
        txtClass.setValue(c.getClassAsStr());
        txtHasPredicate.setValue(c.getHasPredicateAsStr());
        checkGenerateSourceInfo.setValue(c.isSourceInformation());
    }

    @Override
    public HtmlCssConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtClass.isValid() || !txtHasPredicate.isValid() ) {
            throw new DPUConfigException("Wrong 'Advanced settings'");
        }

        final HtmlCssConfig_V1 c = new HtmlCssConfig_V1();
        c.getActions().addAll(genTable.getValue());
        c.setClassAsStr(txtClass.getValue());
        c.setHasPredicateAsStr(txtHasPredicate.getValue());
        c.setSourceInformation(checkGenerateSourceInfo.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        buildLayout();
        buildAdvancedLayout();
        // Add configuration tab.
        this.addTab(ConfigCopyPaste.create(ctx), "Copy&Paste");
    }

    private void buildLayout() {
        setSizeFull();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        
        mainLayout.addComponent(new Label("Use name = '" + HtmlCss.WEB_PAGE_NAME + 
                "' to match to whole web page. Names are case sensistive!"));

        genTable = new ComponentTable(HtmlCssConfig_V1.Action.class,
                new ComponentTable.ColumnInfo("name", "Action name", null, 0.15f),
                new ComponentTable.ColumnInfo("type", "Type", null, 0.2f),
                new ComponentTable.ColumnInfo("actionData", "Action data", null, 0.5f),
                new ComponentTable.ColumnInfo("outputName", "Output name", null, 0.15f));
        genTable.setPolicy(new ComponentTable.Policy<HtmlCssConfig_V1.Action>() {

            @Override
            public boolean isSet(HtmlCssConfig_V1.Action value) {
                return value.getName()!= null && !value.getName().isEmpty();
            }

        });
        mainLayout.addComponent(genTable);
        mainLayout.setExpandRatio(genTable, 1.0f);

        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);

        setCompositionRoot(panel);
    }

    private void buildAdvancedLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtClass = new TextField("Root subject class");
        txtClass.setWidth("100%");
        txtClass.addValidator(new UrlValidator(true));
        mainLayout.addComponent(txtClass);

        txtHasPredicate = new TextField("Default has predicate");
        txtHasPredicate.setDescription("If set then it's used as a default predicate for all SUBJECTS, ie. all subjects have action data set to this value by default.");
        txtHasPredicate.setWidth("100%");
        txtHasPredicate.addValidator(new UrlValidator(true));
        mainLayout.addComponent(txtHasPredicate);

        checkGenerateSourceInfo = new CheckBox("Generate source info");
        checkGenerateSourceInfo.setDescription("If checked the for each root triple generate statment with predicate " + HtmlCssOntology.PREDICATE_SOURCE + " and object equal to source symbolic name.");
        checkGenerateSourceInfo.setWidth("100%");
        mainLayout.addComponent(checkGenerateSourceInfo);

        super.addTab(mainLayout, "Advanced settings");
    }

}
