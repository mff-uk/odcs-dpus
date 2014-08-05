package cz.cuni.mff.xrg.uv.transformer.tabular.gui;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnType;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.net.URISyntaxException;

/**
 *
 * @author Škoda Petr
 */
public class PropertyComponentGroup {

    private final TextField columnName;

    private final ComboBox columnType;

    private final TextField uri;

    private final CheckBox typeFromDbf;

    private final TextField language;

    public PropertyComponentGroup(GridLayout propertiesLayout) {
        this.columnName = new TextField();
        this.columnName.setWidth("100%");
        this.columnName.setNullSettingAllowed(true);
        this.columnName.setNullRepresentation("");
        propertiesLayout.addComponent(this.columnName);

        this.columnType = new ComboBox();
        this.columnType.setWidth("7em");
        this.columnType.setNullSelectionAllowed(false);
        for (ColumnType type : ColumnType.values()) {
            this.columnType.addItem(type);
        }
        this.columnType.select(ColumnType.Auto);
        this.columnType.setImmediate(true);
        this.columnType.setNewItemsAllowed(false);
        propertiesLayout.addComponent(this.columnType);

        this.uri = new TextField();
        this.uri.setWidth("100%");
        this.uri.setNullSettingAllowed(true);
        this.uri.setNullRepresentation("");
        propertiesLayout.addComponent(this.uri);

        this.typeFromDbf = new CheckBox();
        this.typeFromDbf.setWidth("7em");
        propertiesLayout.addComponent(this.typeFromDbf);

        this.language = new TextField();
        this.language.setWidth("7em");
        this.language.setNullSettingAllowed(true);
        this.language.setNullRepresentation("");
        propertiesLayout.addComponent(this.language);

        columnType.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateEnabled((ColumnType) event.getProperty()
                        .getValue());
            }
        });

        updateEnabled((ColumnType) columnType.getValue());
    }

    public void set(String columnName, ColumnInfo_V1 info) {
        this.columnName.setValue(columnName);
        this.columnType.setValue(info.getType());
        this.uri.setValue(info.getURI());
        this.typeFromDbf.setValue(info.isUseTypeFromDfb());
        this.language.setValue(info.getLanguage());

        updateEnabled(info.getType());
    }

    public ColumnInfo_V1 get() throws DPUConfigException {
        final ColumnInfo_V1 info = new ColumnInfo_V1();

        // validate URI
        try {
            if (uri.getValue() != null) {
                new java.net.URI(uri.getValue());
            }
        } catch (URISyntaxException ex) {
            throw new DPUConfigException("Invalid 'Propoerty URI'.", ex);
        }

        info.setType((ColumnType) columnType.getValue());
        info.setURI(uri.getValue());
        if (info.getType() == ColumnType.Auto) {
            info.setUseTypeFromDfb(typeFromDbf.getValue());
        } else {
            info.setUseTypeFromDfb(null);
        }

        if (info.getType() == ColumnType.String) {
            info.setLanguage(language.getValue());
        } else {
            info.setLanguage(null);
        }

        return info;
    }

    public String getColumnName() {
        return columnName.getValue();
    }

    public void clear() {
        this.columnName.setValue(null);
        columnType.setValue(ColumnType.Auto);
        uri.setValue(null);
        typeFromDbf.setValue(false);
        language.setValue(null);

        updateEnabled((ColumnType) columnType.getValue());
    }

    /**
     * Based on given value set {@link ColumnType} dependent components to
     * enabled/disabled.
     *
     * @param type
     */
    private void updateEnabled(ColumnType type) {
        language.setEnabled(type == ColumnType.String);
        typeFromDbf.setEnabled(type == ColumnType.Auto);
    }

}