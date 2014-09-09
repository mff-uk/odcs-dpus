package cz.cuni.mff.xrg.uv.transformer;

import com.vaadin.ui.*;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.transformer.gui.NamedQueryComponent;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.util.LinkedList;
import java.util.List;

public class HtmlCssVaadinDialog extends AdvancedVaadinDialogBase<HtmlCssConfig_V1> {

    private VerticalLayout mainLayout;
    
    private Panel mainPanel;

    private GridLayout queryLayout;

    private boolean layoutSet = false;

    private final List<NamedQueryComponent> namedQueryComponents = new LinkedList<>();

    public HtmlCssVaadinDialog() {
        super(HtmlCssConfig_V1.class, 
                AddonInitializer.create(new SimpleRdfConfigurator(HtmlCss.class)));

        buildLayout();
    }

    @Override
    public void setConfiguration(HtmlCssConfig_V1 c) throws DPUConfigException {
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
            layoutSet = true;
        }
        //
        int usedComponenets = 0;
        for (HtmlCssConfig_V1.NamedQuery q : c.getQueries()) {
            if (usedComponenets >= namedQueryComponents.size()) {
                // add new
                addNamedQuery(q);
            } else {
                // set exising
                namedQueryComponents.get(usedComponenets).setNamedQuery(q);
            }
            ++usedComponenets;
        }
        // unset all others
        for (int index = usedComponenets; index < namedQueryComponents.size(); ++index) {
            namedQueryComponents.get(index).setNamedQuery(null);
        }
    }

    @Override
    public HtmlCssConfig_V1 getConfiguration() throws DPUConfigException {
        final HtmlCssConfig_V1 c = new HtmlCssConfig_V1();

        for (NamedQueryComponent component : namedQueryComponents) {
            HtmlCssConfig_V1.NamedQuery q = component.getNamedQuery();
            if (q != null) {
                c.getQueries().add(q);
            }
        }

        return c;
    }

    private void buildLayout() {
		queryLayout = new GridLayout(4, 1);
		queryLayout.setWidth("100%");
		queryLayout.setHeight("-1px");
        queryLayout.setMargin(false);
        queryLayout.setSpacing(true);
        queryLayout.setImmediate(true);

        final Label lblPredicate = new Label("Predicate");
        lblPredicate.setWidth("20em");
        queryLayout.addComponent(lblPredicate);
        queryLayout.setColumnExpandRatio(0, 0.0f);

        final Label lblType = new Label("Type");
        lblType.setWidth("7em");
        queryLayout.addComponent(lblType);
        queryLayout.setColumnExpandRatio(1, 0.0f);

        final Label lblQuery = new Label("Jsoup query");
        queryLayout.addComponent(lblQuery);
        queryLayout.setColumnExpandRatio(2, 1.0f);

        final Label lblAttr = new Label("Attribute name");
        lblAttr.setWidth("6em");
        queryLayout.addComponent(lblAttr);
        queryLayout.setColumnExpandRatio(3, 0.0f);

        mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setImmediate(true);
        mainLayout.addComponent(queryLayout);

        final Button btnAdd = new Button("Add query", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                addNamedQuery(null);
            }
        });

        mainLayout.addComponent(btnAdd);

		mainPanel = new Panel();
        mainPanel.setContent(mainLayout);
        mainPanel.setSizeFull();
        
        setCompositionRoot(mainPanel);

        addNamedQuery(null);
    }

    /**
     *
     * @param q If null then just empty component is added.
     */
    private void addNamedQuery(HtmlCssConfig_V1.NamedQuery q) {
        NamedQueryComponent newComponent = new NamedQueryComponent(queryLayout);
        newComponent.setNamedQuery(q);
        namedQueryComponents.add(newComponent);
    }

}
