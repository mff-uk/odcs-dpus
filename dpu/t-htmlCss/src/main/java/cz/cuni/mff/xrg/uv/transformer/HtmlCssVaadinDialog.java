package cz.cuni.mff.xrg.uv.transformer;

import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.utils.dialog.container.ComponentTable;
import cz.cuni.mff.xrg.uv.utils.dialog.validator.UrlValidator;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class HtmlCssVaadinDialog extends AdvancedVaadinDialogBase<HtmlCssConfig_V1> {

    private ComponentTable genTable;

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

    private void buildLayout() {
        setSizeFull();

        genTable = new ComponentTable(HtmlCssConfig_V1.Query.class,
                new ComponentTable.ColumnInfo("query", "Jsoup query", null, 0.4f),
                new ComponentTable.ColumnInfo("type", "Type", null, 0.1f),
                new ComponentTable.ColumnInfo("predicate", "Predicate", new UrlValidator(false), 0.2f),
                new ComponentTable.ColumnInfo("attrName", "Attribute name", null, 0.1f));

        genTable.setPolicy(new ComponentTable.Policy<HtmlCssConfig_V1.Query>() {

            @Override
            public boolean isSet(HtmlCssConfig_V1.Query value) {
                return value.getQuery() != null && !value.getQuery().isEmpty();
            }

        });

        setCompositionRoot(genTable);
    }
}
