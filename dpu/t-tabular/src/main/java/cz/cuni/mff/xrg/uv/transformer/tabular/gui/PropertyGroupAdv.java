package cz.cuni.mff.xrg.uv.transformer.tabular.gui;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

/**
 * Component for setting advanced mapping.
 *
 * @author Škoda Petr
 */
public class PropertyGroupAdv {

    private final TextField txtUri;

    private final TextField txtTemplate;

    public PropertyGroupAdv(GridLayout propertiesLayout) {
        this.txtUri = new TextField();
        this.txtUri.setWidth("100%");
        this.txtUri.setNullSettingAllowed(true);
        this.txtUri.setNullRepresentation("");
        propertiesLayout.addComponent(this.txtUri);

        this.txtTemplate = new TextField();
        this.txtTemplate.setWidth("100%");
        this.txtTemplate.setNullSettingAllowed(true);
        this.txtTemplate.setNullRepresentation("");
        propertiesLayout.addComponent(this.txtTemplate);
    }

    public void set(String uri, String template) {
        txtUri.setValue(uri);
        txtTemplate.setValue(template);
    }

    public String getUri() {
        return txtUri.getValue();
    }

    public String getTemplate() {
        return txtTemplate.getValue();
    }

    public void clear() {
        set(null, null);
    }

}
