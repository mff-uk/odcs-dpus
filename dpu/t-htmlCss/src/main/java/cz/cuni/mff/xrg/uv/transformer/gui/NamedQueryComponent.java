package cz.cuni.mff.xrg.uv.transformer.gui;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import cz.cuni.mff.xrg.uv.transformer.HtmlCssConfig_V1;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Component for configuration of single {@link HtmlCssConfig_V1#NamedQuery}.
 *
 * @author Škoda Petr
 */
public class NamedQueryComponent {

    private final TextField txtPredicate;

    private final TextArea txtQuery;

    private final TextField txtAttr;

    private final ComboBox comboType;

    public NamedQueryComponent(GridLayout layout) {
        txtPredicate = new TextField();
        txtPredicate.setWidth("100%");
        layout.addComponent(txtPredicate);

        comboType = new ComboBox();
        comboType.setNewItemsAllowed(false);
        comboType.setNullSelectionAllowed(false);
        comboType.setWidth("100%");
        for (HtmlCssConfig_V1.ElementType type : HtmlCssConfig_V1.ElementType.values()) {
            comboType.addItem(type);
        }
        comboType.setItemCaption(HtmlCssConfig_V1.ElementType.TEXT, "text");
        comboType.setItemCaption(HtmlCssConfig_V1.ElementType.TABLE, "table");
        layout.addComponent(comboType);

        txtQuery = new TextArea();
        txtQuery.setWidth("100%");
        txtQuery.setHeight("3em");
        txtQuery.setWordwrap(true);        
        layout.addComponent(txtQuery);

        txtAttr = new TextField();
        txtAttr.setWidth("100%");
        txtAttr.setInputPrompt("text");
        txtAttr.setNullRepresentation("");
        txtAttr.setNullSettingAllowed(true);
        layout.addComponent(txtAttr);

    }

    /**
     *
     * @param q Null to unset values.
     */
    public void setNamedQuery(HtmlCssConfig_V1.NamedQuery q) {
        if (q == null) {
            txtPredicate.setValue("");
            txtQuery.setValue("");
            txtAttr.setValue(null);
            comboType.setValue(HtmlCssConfig_V1.ElementType.TEXT);
        } else {
            txtPredicate.setValue(q.getPredicate());
            txtQuery.setValue(q.getQuery());
            txtAttr.setValue(q.getAttrName());
            comboType.setValue(q.getType());
        }
    }

    /**
     *
     * @return Null if no complete configuration is provided.
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    public HtmlCssConfig_V1.NamedQuery getNamedQuery() throws DPUConfigException {
        if (txtPredicate.getValue().isEmpty()) {
            return null;
        }
        try {
            new URL(txtPredicate.getValue());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid predicate: " + txtPredicate.getValue());
        }

        final HtmlCssConfig_V1.NamedQuery q = new HtmlCssConfig_V1.NamedQuery();
        q.setPredicate(txtPredicate.getValue());
        q.setType((HtmlCssConfig_V1.ElementType)comboType.getValue());
        q.setQuery(txtQuery.getValue());
        q.setAttrName(txtAttr.getValue());
        return q;
    }

}
