package cz.opendata.linked.ares;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends AbstractDialog<ExtractorConfig> {

    private static final long serialVersionUID = 7003725620084616056L;

    private GridLayout mainLayout;
    private CheckBox chkUseCacheOnly;
    private CheckBox chkBasic;
    private CheckBox chkOR;
    private CheckBox chkRZP;
    private CheckBox chkStdAdr;
    private CheckBox chkActive;
    private CheckBox chkGenerateOutput;
    private CheckBox chkPuvAdr;
    private TextField numDownloads;
    private TextField hoursToCheck;
    private TextField interval;
    private TextField timeout;
    
    public ExtractorDialog() {
        super(Extractor.class);
    }  

    @Override
    protected void buildDialogLayout() {
        // common part: create layout
        mainLayout = new GridLayout(1, 2);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        chkGenerateOutput = new CheckBox("Generate output:");
        chkGenerateOutput.setDescription("When selected, the cached or downloaded files will be passed as RDF literals to output.");
        chkGenerateOutput.setWidth("100%");
        
        mainLayout.addComponent(chkGenerateOutput);

        chkUseCacheOnly = new CheckBox("Use only cache:");
        chkUseCacheOnly.setDescription("When selected, DPU only goes through cache, does not download and ignores download limits.");
        chkUseCacheOnly.setWidth("100%");
        
        mainLayout.addComponent(chkUseCacheOnly);
        
        chkBasic = new CheckBox("Download Basic:");
        chkBasic.setDescription("When selected, downloads will include basic registry.");
        chkBasic.setWidth("100%");
        
        mainLayout.addComponent(chkBasic);

        chkOR = new CheckBox("Download OR:");
        chkOR.setDescription("When selected, downloads will include business registry.");
        chkOR.setWidth("100%");
        
        mainLayout.addComponent(chkOR);

        chkRZP = new CheckBox("Download RZP:");
        chkRZP.setDescription("When selected, downloads will include RZP registry.");
        chkRZP.setWidth("100%");
        
        mainLayout.addComponent(chkRZP);

        chkStdAdr = new CheckBox("Include OR stdadr:");
        chkStdAdr.setDescription("When selected, downloads will include standardized address.");
        chkStdAdr.setWidth("100%");
        
        mainLayout.addComponent(chkStdAdr);

        chkActive = new CheckBox("BASIC only active:");
        chkActive.setDescription("When selected, downloads from BASIC will include data only for currently registered ICs (not cancelled).");
        chkActive.setWidth("100%");
        
        mainLayout.addComponent(chkActive);

        chkPuvAdr = new CheckBox("BASIC PuvAdr:");
        chkPuvAdr.setDescription("When selected, downloads from BASIC will also include address without modifications.");
        chkPuvAdr.setWidth("100%");
        
        mainLayout.addComponent(chkPuvAdr);

        numDownloads = new TextField();
        numDownloads.setCaption("Number of downloads in the last X hours:");
        mainLayout.addComponent(numDownloads);
        
        hoursToCheck = new TextField();
        hoursToCheck.setCaption("Count files in cache downloaded in the last X hours:");
        mainLayout.addComponent(hoursToCheck);

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);
        
        timeout = new TextField();
        timeout.setCaption("Timeout for download:");
        
        mainLayout.addComponent(timeout);
        
        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);
        setCompositionRoot(panel);
    }    
     
    @Override
    public void setConfiguration(ExtractorConfig conf) throws DPUConfigException {
        chkUseCacheOnly.setValue(conf.isUseCacheOnly());
        chkGenerateOutput.setValue(conf.isGenerateOutput());
        chkOR.setValue(conf.isDownloadOR());
        chkRZP.setValue(conf.isDownloadRZP());
        chkBasic.setValue(conf.isDownloadBasic());
        chkPuvAdr.setValue(conf.isBas_puvadr());
        chkActive.setValue(conf.isBas_active());
        chkStdAdr.setValue(conf.isOr_stdadr());
        interval.setValue(Integer.toString(conf.getInterval()));
        timeout.setValue(Integer.toString(conf.getTimeout()));
        numDownloads.setValue(Integer.toString(conf.getPerDay()));
        hoursToCheck.setValue(Integer.toString(conf.getHoursToCheck()));
    
    }

    @Override
    public ExtractorConfig getConfiguration() throws DPUConfigException {
        ExtractorConfig conf = new ExtractorConfig();
        conf.setBas_puvadr(chkPuvAdr.getValue());
        conf.setOr_stdadr(chkStdAdr.getValue());
        conf.setDownloadOR(chkOR.getValue());
        conf.setDownloadRZP(chkRZP.getValue());
        conf.setDownloadBasic(chkBasic.getValue());
        conf.setBas_active(chkActive.getValue());
        conf.setUseCacheOnly(chkUseCacheOnly.getValue());
        conf.setGenerateOutput(chkGenerateOutput.getValue());
        
        try {
            conf.setPerDay(Integer.parseInt(numDownloads.getValue()));
        } catch (InvalidValueException e) {
            // TODO Throw something here?
        }
        try {
            conf.setHoursToCheck(Integer.parseInt(hoursToCheck.getValue()));
        } catch (InvalidValueException e) {
            
        }
        try {
            conf.setInterval(Integer.parseInt(interval.getValue()));
        } catch (InvalidValueException e) {
            
        }
        try {
            conf.setTimeout(Integer.parseInt(timeout.getValue()));
        } catch (InvalidValueException e) {
            
        }

        return conf;
    }
    
}
