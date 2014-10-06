package cz.cuni.mff.xrg.uv.transformer;

import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.container.GeneratedTable;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class HtmlCssVaadinDialog extends AdvancedVaadinDialogBase<HtmlCssConfig_V1> {

    private GeneratedTable genTable;

    public HtmlCssVaadinDialog() {
        super(HtmlCssConfig_V1.class,
                AddonInitializer.create(new SimpleRdfConfigurator(HtmlCss.class)));

        buildLayout();
    }

    @Override
    public void setConfiguration(HtmlCssConfig_V1 c) throws DPUConfigException {
        genTable.setValue(c.getQueries());
    }

    @Override
    public HtmlCssConfig_V1 getConfiguration() throws DPUConfigException {
        final HtmlCssConfig_V1 c = new HtmlCssConfig_V1();
        c.getQueries().addAll(genTable.getValue());
        return c;
    }

    VerticalLayout layout;

    private void buildLayout() {
        setSizeFull();

        layout = new VerticalLayout();
        layout.setSizeFull();

        genTable = new GeneratedTable(HtmlCssConfig_V1.Query.class,
                new GeneratedTable.Policy<HtmlCssConfig_V1.Query>() {

                    @Override
                    public boolean isSet(HtmlCssConfig_V1.Query value) {
                        return value.getQuery() != null && !value.getQuery().isEmpty();
                    }

                });
        
        genTable.setMargin(true);
        genTable.setColumn(
                new GeneratedTable.ColumnInfo("query", "Jsoup query"),
                new GeneratedTable.ColumnInfo("type", "Type"),
                new GeneratedTable.ColumnInfo("predicate", "Predicate"),
                new GeneratedTable.ColumnInfo("attrName", "Attribute name"));

        layout.addComponent(genTable);
        setCompositionRoot(layout);
    }
}
