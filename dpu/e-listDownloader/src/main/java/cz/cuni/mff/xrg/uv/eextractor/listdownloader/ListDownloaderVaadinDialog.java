package cz.cuni.mff.xrg.uv.eextractor.listdownloader;

import com.vaadin.data.validator.RangeValidator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.cuni.dpu.vaadin.AbstractDialog;

/**
 * Vaadin configuration dialog for ListDownloader.
 *
 * @author Petr Škoda
 */
public class ListDownloaderVaadinDialog extends AbstractDialog<ListDownloaderConfig_V1> {

    private TextField txtPagePattern;
    
    private TextField txtStartIndex;
    
    private TextField txtCssSelector;

    public ListDownloaderVaadinDialog() {
        super(ListDownloader.class);
    }

    @Override
    public void setConfiguration(ListDownloaderConfig_V1 c) throws DPUConfigException {
        txtPagePattern.setValue(c.getPagePattern());
        txtStartIndex.setValue(Integer.toString(c.getStartIndex()));
        // Use first one.
        if (c.getNextPageConditions().isEmpty()) {
            txtCssSelector.setValue("");
        } else {
            txtCssSelector.setValue(c.getNextPageConditions().get(0).getNextButtonSelector());
        }
    }

    @Override
    public ListDownloaderConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtCssSelector.isValid() || !txtStartIndex.isValid() || !txtCssSelector.isValid()) {
            throw new DPUConfigException("ListDownloader.dialog.invalid");
        }
        final ListDownloaderConfig_V1 c = new ListDownloaderConfig_V1();
        c.setPagePattern(txtPagePattern.getValue());
        c.setStartIndex(Integer.parseInt(txtStartIndex.getValue()));
        // Add a single condition.
        ListDownloaderConfig_V1.NextPageCondition condition = new ListDownloaderConfig_V1.NextPageCondition();
        condition.setNextButtonSelector(txtCssSelector.getValue());
        c.getNextPageConditions().add(condition);
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        txtPagePattern = new TextField(ctx.tr("ListDownloader.dialog.pagePattern"));
        txtPagePattern.setWidth("100%");
        txtPagePattern.setRequired(true);
        mainLayout.addComponent(txtPagePattern);

        txtStartIndex = new TextField(ctx.tr("ListDownloader.dialog.startIndex"));
        txtStartIndex.setWidth("100%");
        txtStartIndex.setRequired(true);
        txtStartIndex.addValidator(new RangeValidator(ctx.tr("ListDownloader.dialog.startIndex.invalid"),
                Integer.class, 0, null));
        mainLayout.addComponent(txtStartIndex);

        txtCssSelector = new TextField(ctx.tr("ListDownloader.dialog.nextPageCss"));
        txtCssSelector.setWidth("100%");
        txtCssSelector.setRequired(true);
        mainLayout.addComponent(txtCssSelector);

        setCompositionRoot(mainLayout);
    }
}
