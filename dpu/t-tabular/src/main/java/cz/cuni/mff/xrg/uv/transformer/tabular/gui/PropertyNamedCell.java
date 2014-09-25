package cz.cuni.mff.xrg.uv.transformer.tabular.gui;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.NamedCell_V1;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Component for {@link NamedCell_V1}.
 *
 * @author Škoda Petr
 */
public class PropertyNamedCell {

    private final TextField txtName;
    
    private final TextField txtColumn;
    
    private final TextField txtRow;

    public PropertyNamedCell(GridLayout propertiesLayout) {
        this.txtName = new TextField();
        this.txtName.setWidth("100%");
        this.txtName.setNullSettingAllowed(false);
        propertiesLayout.addComponent(this.txtName);
        
        this.txtColumn = new TextField();
        this.txtColumn.setWidth("100%");
        this.txtColumn.setNullSettingAllowed(false);
        propertiesLayout.addComponent(this.txtColumn);
        
        this.txtRow = new TextField();
        this.txtRow.setWidth("100%");
        this.txtRow.setNullSettingAllowed(false);
        propertiesLayout.addComponent(this.txtRow);
    }

    public void set(NamedCell_V1 namedCoord) {
        if (namedCoord == null) {
            this.txtColumn.setValue("");
            this.txtName.setValue("");
            this.txtRow.setValue("");
        } else {
            this.txtColumn.setValue(namedCoord.getColumnNumber().toString());
            this.txtName.setValue(namedCoord.getName());
            this.txtRow.setValue(namedCoord.getRowNumber().toString());
        }
    }

    public void set(String name, Integer column, Integer row) {
        this.txtColumn.setValue(column.toString());
        this.txtName.setValue(name);
        this.txtRow.setValue(row.toString());
    }

    public NamedCell_V1 get() throws DPUConfigException {
        int column, row;
        try {
            column = Integer.parseInt(this.txtColumn.getValue());
            row = Integer.parseInt(this.txtRow.getValue());
        } catch (NumberFormatException ex) {
            throw new DPUConfigException("column and row must be numbers", ex);
        }
        String name = this.txtName.getValue();
        if (name == null || name.isEmpty()) {
            return null;
        }
        final NamedCell_V1 namedCoord = new NamedCell_V1();
        namedCoord.setColumnNumber(column);
        namedCoord.setName(name);
        namedCoord.setRowNumber(row);

        if (namedCoord.getColumnNumber() < 1 && namedCoord.getRowNumber() < 1) {
            throw new DPUConfigException("Row and column for 'Xls mapping' must be greater then 1.");
        }

        return namedCoord;
    }

    public void clear() {
        this.txtColumn.setValue("");
        this.txtName.setValue("");
        this.txtRow.setValue("");        
    }
    
    public void setEnabled(boolean enabled) {
        this.txtColumn.setEnabled(enabled);
        this.txtName.setEnabled(enabled);
        this.txtRow.setEnabled(enabled);
    }

}
