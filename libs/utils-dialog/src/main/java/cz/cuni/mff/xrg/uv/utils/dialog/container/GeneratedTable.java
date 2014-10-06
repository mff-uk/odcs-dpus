package cz.cuni.mff.xrg.uv.utils.dialog.container;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple table component that can be used to represent data in collection. As addition to normal 
 * table:
 * <ul>
 * <li>Provide ComboBox for enums.</li>
 * <li>Has add new row button.</li>
 * </ul>
 * Sample usage:
 * <pre>
 * {@code
 * GeneratedTable genTable = new GeneratedTable(Item.class);
 * // customize columns
 * genTable.setColumn(
 *     new GeneratedTable.ColumnInfo("query", "Jsoup query"),
 *     new GeneratedTable.ColumnInfo("type", "Type"));
 * // fill with data
 * genTable.setValue(c.getQueries());
 * }
 * </pre>
 * @author Škoda Petr
 * @param <T>
 */
public class GeneratedTable<T> extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratedTable.class);

    /**
     * Field factory for generating cells in table.
     */
    private class FieldFactory implements TableFieldFactory {

        @Override
        public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
            final Property containerProperty = container.getContainerProperty(itemId, propertyId);
            final Class<?> type = containerProperty.getType();

            if (type == null) {
                return null;
            }

            final Field<?> field = createFieldByPropertyType(type, propertyId);
            field.setWidth("100%");
            return field;
        }

        /**
         *
         * @param type
         * @param propertyId
         * @return Component for editing of given type.
         */
        public Field<?> createFieldByPropertyType(Class<?> type, Object propertyId) {

            if (type.isEnum()) {
                final ComboBox comboBox = new ComboBox();
                // pre-fill with options
                final Object[] values = type.getEnumConstants();
                for (Object value : values) {
                    comboBox.addItem(value);
                }
                comboBox.setNewItemsAllowed(false);
                comboBox.setNullSelectionAllowed(false);
                return comboBox;
            }

            if (Boolean.class.isAssignableFrom(type)) {
                return new CheckBox();
            }

            final TextField textField = new TextField();
            textField.setNullRepresentation("");
            // look for validator
            if (columnInfo.containsKey(propertyId)) {
                final ColumnInfo info = columnInfo.get(propertyId);
                if (info.validator != null) {
                    textField.addValidator(info.validator);
                }
            }
            return textField;
        }

    }

    /**
     * Can be used to set behaviour of this table.
     */
    public static class Policy<T> {

        /**
         *
         * @param value
         * @return False to ignore given element in {@link #getValue()} function.
         */
        public boolean isSet(T value) {
            return true;
        }

    }

    /**
     * Store informations about a single column.
     *
     */
    public static class ColumnInfo {

        private final String propertyName;

        private final String headerText;

        private final Validator validator;

        public ColumnInfo(String propertyName, String headerText) {
            this.propertyName = propertyName;
            this.headerText = headerText;
            this.validator = null;
        }

        public ColumnInfo(String propertyName, String headerText, Validator validator) {
            this.propertyName = propertyName;
            this.headerText = headerText;
            this.validator = validator;
        }

    }

    /**
     * Class for data.
     */
    private final Class<T> dataClass;

    /**
     * Underlying container.
     */
    private final Container container;

    private final Table table;

    private final Policy policy;

    private final VerticalLayout layout = new VerticalLayout();

    private final Map<Object, ColumnInfo> columnInfo = new HashMap<>();

    public GeneratedTable(Class<T> dataClass) {
        this.dataClass = dataClass;
        this.container = new BeanItemContainer<>(dataClass);
        this.table = new Table(null, container);
        this.policy = new Policy();

        buildLayout();
    }

    public GeneratedTable(Class<T> dataClass, Policy<T> policy) {
        this.dataClass = dataClass;
        this.container = new BeanItemContainer<>(dataClass);
        this.table = new Table(null, container);
        this.policy = policy;

        buildLayout();
    }

    /**
     *
     * @param columns
     */
    public void setColumn(ColumnInfo ... columns) {
        columnInfo.clear();
        final List<Object> visibleColumns = new ArrayList<>();
        for (ColumnInfo column : columns) {
            columnInfo.put(column.propertyName, column);
            // add in order to visible columns list
            visibleColumns.add(column.propertyName);
            // set header text
            table.setColumnHeader(column.propertyName, column.headerText);
        }
        // do changes
        setVisibleColumns(visibleColumns.toArray());
    }

    private void buildLayout() {
        setSizeFull();

        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(false);

        final Button btnAddRow = new Button("Add row", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                addRow(null);
            }
        });
        layout.addComponent(btnAddRow);
        layout.setExpandRatio(btnAddRow, 0.0f);

        table.setSizeFull();
        table.setEditable(true);
        table.setTableFieldFactory(new FieldFactory());

        layout.addComponent(table);
        layout.setExpandRatio(table, 1.0f);

        setCompositionRoot(layout);
    }

    /**
     *
     * @param value If null row with default values is added.
     */
    private void addRow(T value) {
        if (value == null) {
            try {
                value = dataClass.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                LOG.error("Can't create default object representation.", ex);
                return;
            }
        }
        container.addItem(value);
    }

    /**
     *
     * @param propertyId
     * @param header Columns name.
     */
    public void setColumnHeader(Object propertyId, String header) {
        table.setColumnHeader(propertyId, header);
    }

    /**
     *
     * @param visibleColumns Visible column in required order.
     */
    public void setVisibleColumns(Object... visibleColumns) {
        table.setVisibleColumns(visibleColumns);
    }

    public void setMargin(boolean enabled) {
        layout.setMargin(enabled);
    }

    public void setValue(Collection<T> data) {
        container.removeAllItems();
        for (T item : data) {
            container.addItem(item);
        }
    }

    /**
     *
     * @return Data in table filtered by {@link Policy#isSet(java.lang.Object)}.
     */
    public Collection<T> getValue() {
        List<T> result = new ArrayList<>(container.size());
        for (Object id : container.getItemIds()) {
            // we know this as we use BeanItemContainer
            T item = (T)id;
            if (policy.isSet(item)) {
                result.add(item);
            }
        }
        return result;
    }

}