package eu.unifiedviews.intlib.dpu;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class IntlibGetDataFromRextractorVaadinDialog extends AdvancedVaadinDialogBase<IntlibGetDataFromRextractorConfig_V1> {

    private TextField tfTargetRextractorServerURL; //Path

    private TextField dateTo; //Path

    private TextField dateFrom; //Path

    private TextField maxNumOfExtractedDecisions; //Path

    private CheckBox cbLast7Days;

    public IntlibGetDataFromRextractorVaadinDialog() {
        super(IntlibGetDataFromRextractorConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibGetDataFromRextractorConfig_V1 c) throws DPUConfigException {

        if (!c.getDateTo().isEmpty()) {
            dateTo.setValue(c.getDateTo());
        }
        if (!c.getDateTo().isEmpty()) {
            dateFrom.setValue(c.getDateFrom());
        }

        if (!c.getTargetRextractorServer().isEmpty()) {
            tfTargetRextractorServerURL.setValue(c.getTargetRextractorServer());
        }

        cbLast7Days.setValue(c.isLast7Days());

    }

    @Override
    public IntlibGetDataFromRextractorConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibGetDataFromRextractorConfig_V1 c = new IntlibGetDataFromRextractorConfig_V1();

        c.setDateFrom(dateFrom.getValue().trim());
        c.setDateTo(dateTo.getValue().trim());
        c.setTargetRextractorServer(tfTargetRextractorServerURL.getValue().trim());
        c.setLast7Days(cbLast7Days.getValue());

        return c;
    }

    private void buildLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        tfTargetRextractorServerURL = new TextField();
        tfTargetRextractorServerURL.setNullRepresentation("");
        tfTargetRextractorServerURL.setCaption("Predicate URL:");
        tfTargetRextractorServerURL.setImmediate(false);
        tfTargetRextractorServerURL.setWidth("100%");
        tfTargetRextractorServerURL.setHeight("-1px");
        //        tfPredicateURL.setInputPrompt("2014-11-13");
        //        dateTo.addValidator(new Validator() {
        //            @Override
        //            public void validate(Object value) throws Validator.InvalidValueException {
        //                if (value.getClass() == String.class && !((String) value).isEmpty()) {
        //                    return;
        //                }
        //                throw new Validator.InvalidValueException("Path must be filled!");
        //            }
        //        });
        mainLayout.addComponent(tfTargetRextractorServerURL);

        dateFrom = new TextField();
        dateFrom.setNullRepresentation("");
        dateFrom.setCaption("Date From (YYYY-MM-DD):");
        dateFrom.setImmediate(false);
        dateFrom.setWidth("100%");
        dateFrom.setHeight("-1px");
        dateFrom.setInputPrompt("2014-11-13");
        //        dateFrom.addValidator(new Validator() {
        //            @Override
        //            public void validate(Object value) throws Validator.InvalidValueException {
        //                if (value.getClass() == String.class && !((String) value).isEmpty()) {
        //                    return;
        //                }
        //                throw new Validator.InvalidValueException("Date from must be filled!");
        //            }
        //        });
        mainLayout.addComponent(dateFrom);

        dateTo = new TextField();
        dateTo.setNullRepresentation("");
        dateTo.setCaption("Date To (YYYY-MM-DD):");
        dateTo.setImmediate(false);
        dateTo.setWidth("100%");
        dateTo.setHeight("-1px");
        dateTo.setInputPrompt("2014-11-13");
        //        dateTo.addValidator(new Validator() {
        //            @Override
        //            public void validate(Object value) throws Validator.InvalidValueException {
        //                if (value.getClass() == String.class && !((String) value).isEmpty()) {
        //                    return;
        //                }
        //                throw new Validator.InvalidValueException("Path must be filled!");
        //            }
        //        });
        mainLayout.addComponent(dateTo);

        cbLast7Days = new CheckBox("Process last 3 days");
        cbLast7Days.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Copy the value to the other checkbox.
                boolean value = (Boolean) event.getProperty().getValue();
                dateFrom.setEnabled(!value);
                dateTo.setEnabled(!value);
            }
        });
        cbLast7Days.setValue(false);

        mainLayout.addComponent(cbLast7Days);

        //        Label lInput = new Label();
        //         lInput.setValue("Procssing last 7 days");
        //         mainLayout.addComponent(lInput);

        setCompositionRoot(mainLayout);
    }
}
