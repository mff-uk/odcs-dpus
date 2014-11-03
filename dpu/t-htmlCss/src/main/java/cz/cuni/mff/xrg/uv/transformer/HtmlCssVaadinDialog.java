package cz.cuni.mff.xrg.uv.transformer;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.container.ComponentTable;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 *
 * @author Škoda Petr
 */
public class HtmlCssVaadinDialog extends AdvancedVaadinDialogBase<HtmlCssConfig_V1> {

    private ComponentTable genTable;

    public HtmlCssVaadinDialog() {
        super(HtmlCssConfig_V1.class, AddonInitializer.create(new SimpleRdfConfigurator(HtmlCss.class)));
        buildLayout();
    }

    @Override
    public void setConfiguration(HtmlCssConfig_V1 c) throws DPUConfigException {
        genTable.setValue(c.getActions());
    }

    @Override
    public HtmlCssConfig_V1 getConfiguration() throws DPUConfigException {
        final HtmlCssConfig_V1 c = new HtmlCssConfig_V1();
        c.getActions().addAll(genTable.getValue());
        return c;
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
}
