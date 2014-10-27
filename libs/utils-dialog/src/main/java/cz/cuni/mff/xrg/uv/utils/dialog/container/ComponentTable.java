package cz.cuni.mff.xrg.uv.utils.dialog.container;

import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Reflection based table, that can be easily used to represent the container in configuration. Table 
 * does not use table component but rather generate layout from single components as it's provide
 * possibility to validate each cell.
 *
 * @author Škoda Petr
 * @param <T>
 */
public class ComponentTable<T> extends CustomComponent {

    /**
     * Can be used to set behaviour of this table.
     *
     * @param <T>
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
     * Informations about a single column in this table.
     */
    public static class ColumnInfo {

        private final String propertyName;

        private final String headerText;

        private final Validator validator;

        private final float expansionRatio;

        private Method readMethod;

        private Method writeMethod;

        private Class<?> type;


        public ColumnInfo(String propertyName, String headerText, Validator validator, float expansionRatio) {
            this.propertyName = propertyName;
            this.headerText = headerText;
            this.validator = validator;
            this.expansionRatio = expansionRatio;
        }

    }

    /**
     * Represent a single item = row in this table.
     */
    private class Item {

        public final Map<String, Field> fields = new HashMap<>();

    }

    private final List<Item> items = new LinkedList<>();

    private final Map<String, ColumnInfo> columnsInfo = new LinkedHashMap<>();

    /**
     * Class of represented objects.
     */
    private final Class<T> clazz;

    /**
     * Can be used to customize table behaviour.
     */
    private Policy<T> policy = new Policy<>();

    /**
     * Main layout.
     */
    private GridLayout componentLayout;

    public ComponentTable(Class<T> clazz, ColumnInfo ... columnsInfo) {
        this.clazz = clazz;
        // Prepare columnsInfo.
        for (ColumnInfo info : columnsInfo) {
            this.columnsInfo.put(info.propertyName, info);
        }
        // Build all.
        buildColumnsInfo();
        buildLayout();
    }

    /**
     *
     * @param data New table data.
     */
    public void setValue(Collection<T> data) {
        int index = 0;
        Iterator<T> iter = data.iterator();
        while (iter.hasNext()) {
            T value = iter.next();

            final Item item;
            if (index < items.size()) {
                item = items.get(index);
            } else {
                item = createNewItem();
            }
            setItemValue(item, value);

            ++index;
        }
        // Set other rows to null - empty.
        for (; index < items.size(); index++) {
            setItemValueEmpty(items.get(index));
        }
    }

    /**
     *
     * @return Non empty rows in table.
     * @throws DPUConfigException
     */
    public Collection<T> getValue() throws DPUConfigException {
        final List<T> data = new ArrayList<>(items.size());

        for (Item item : items) {
            T value = getItemValue(item);
            if (value == null) {
                continue;
            }
            if (!policy.isSet(value)) {
                continue;
            }
            data.add(value);
        }

        return data;
    }

    public void setPolicy(Policy<T> policy) {
        this.policy = policy;
    }

    private void buildLayout() {
        setSizeFull();
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        // Prepare layout.
        componentLayout = new GridLayout(columnsInfo.size(), 1);
        componentLayout.setWidth("100%");
        componentLayout.setHeight("-1px");
        componentLayout.setSpacing(true);
        // Add headers.
        int columnIndex = 0;
        for (String key : columnsInfo.keySet()) {
            final ColumnInfo info = columnsInfo.get(key);

            final Label label = new Label(info.headerText);
            componentLayout.setColumnExpandRatio(columnIndex++, info.expansionRatio);
            componentLayout.addComponent(label);
        }
        mainLayout.addComponent(componentLayout);
        
        final Button btnAddItem = new Button("Add row", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                createNewItem();
            }
        });
        mainLayout.addComponent(btnAddItem);

        // Set as composition root.
        setCompositionRoot(mainLayout);
    }

    /**
     * Finalise informations in {@link columnsInfo}.
     */
    private void buildColumnsInfo() {
        for (ColumnInfo info : columnsInfo.values()) {
            try {
                final PropertyDescriptor propDesc = new PropertyDescriptor(info.propertyName, clazz);
                // Set properties.
                info.type = propDesc.getPropertyType();
                info.readMethod = propDesc.getReadMethod();
                info.writeMethod = propDesc.getWriteMethod();
            } catch (IntrospectionException ex) {
                throw new RuntimeException("Can't prepare column info.", ex);
            }
        }
    }

    /**
     * Create field representation (Vaadin component) for given column.
     *
     * @param info
     * @return
     */
    private Field createField(ColumnInfo info) {
        final Class<?> type = info.type;

        if (type.isEnum()) {
            final ComboBox comboBox = new ComboBox();
            // Pre-fill with options - enum members.
            final Object[] values = type.getEnumConstants();
            for (Object value : values) {
                comboBox.addItem(value);
            }
            comboBox.setNewItemsAllowed(false);
            comboBox.setNullSelectionAllowed(false);
            comboBox.setWidth("100%");
            return comboBox;
        }

        if (Boolean.class.isAssignableFrom(type)) {
            return new CheckBox();
        }

        final TextField textField = new TextField();
        textField.setWidth("100%");
        textField.setNullRepresentation("");
        textField.setImmediate(true);

        if (info.validator != null) {
            textField.addValidator(info.validator);
        }

        return textField;
    }

    /**
     *
     * @param item Representation of row.
     * @param value Value that should be set to given row.
     */
    private void setItemValue(Item item, T value) {
        for (ColumnInfo info : columnsInfo.values()) {
            final Object propertyValue;
            try {
                propertyValue = info.readMethod.invoke(value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Can't get property: " + info.propertyName);
            }
            item.fields.get(info.propertyName).setValue(propertyValue);
        }
    }

    /**
     * Unset given item ie. set value to empty.
     * 
     * @param item
     */
    private void setItemValueEmpty(Item item) {
        for (Field field : item.fields.values()) {
            if (field instanceof TextField) {
                field.setValue("");
            }
        }
    }

    /**
     *
     * @param item Item ie. row.
     * @return Current value, is not null.
     * @throws DPUConfigException
     */
    private T getItemValue(Item item) throws DPUConfigException {

        final T value;
        try {
            value = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Can't create new instance.");
        }

        for (ColumnInfo info : columnsInfo.values()) {
            final Field field = item.fields.get(info.propertyName);

            if (!field.isValid()) {
                throw new DPUConfigException("Invalid value: " + field.getValue());
            }

            final Object propertyValue = field.getValue();
            try {
                info.writeMethod.invoke(value, propertyValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Can't get property: " + info.propertyName);
            }
        }

        return value;
    }

    /**
     * Create new empty item (row).
     */
    private Item createNewItem() {
        final Item newItem = new Item();
        for (String key : columnsInfo.keySet()) {
            ColumnInfo info = columnsInfo.get(key);
            final Field newField = createField(info);
            // Add component to layout and to newly emerging item.
            componentLayout.addComponent(newField);
            newItem.fields.put(key, newField);
        }
        items.add(newItem);
        return newItem;
    }

}
