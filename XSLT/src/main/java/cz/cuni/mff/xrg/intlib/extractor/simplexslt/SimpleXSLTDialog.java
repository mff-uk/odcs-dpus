package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType;
import static cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType.Literal;
import static cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType.RDFXML;
import static cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType.TTL;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;
import cz.cuni.xrg.intlib.rdf.enums.WriteGraphType;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 *
 */
public class SimpleXSLTDialog extends BaseConfigDialog<SimpleXSLTConfig> {

   private static final Logger logger = LoggerFactory.getLogger(SimpleXSLTDialog.class);
   
 
    
    private static final String XML_VALUE_PREDICATE = "http://linked.opendata.cz/ontology/odcs/xmlValue";
    private static final String OUTPUT_RDFXML = "RDF/XML";
    private static final String OUTPUT_TTL = "TTL";
    
    private VerticalLayout mainLayout;
    private TextField tfInputPredicate; 
    private TextField tfOutputPredicate; 
    private OptionGroup ogOutputFormat;
    
    
    
    private TextArea taXSLTemplate;
    
    private UploadInfoWindow uploadInfoWindow;
    static int fl = 0;

    public SimpleXSLTDialog() {
        super(SimpleXSLTConfig.class);
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }

    private VerticalLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");


        //upload 
         final FileUploadReceiver fileUploadReceiver = new FileUploadReceiver();

        //Upload component
        Upload fileUpload = new Upload("Uploading Silk config file", fileUploadReceiver);
        fileUpload.setImmediate(true);
        fileUpload.setButtonCaption("Choose");
        //Upload started event listener
        fileUpload.addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(final Upload.StartedEvent event) {

                if (uploadInfoWindow.getParent() == null) {
                    UI.getCurrent().addWindow(uploadInfoWindow);
                }
                uploadInfoWindow.setClosable(false);



            }
        });
        //Upload received event listener. 
        fileUpload.addFinishedListener(
                new Upload.FinishedListener() {
            @Override
            public void uploadFinished(final Upload.FinishedEvent event) {

                uploadInfoWindow.setClosable(true);
                uploadInfoWindow.close();
                //If upload wasn't interrupt by user
                if (fl == 0) {
                    //textFieldPath.setReadOnly(false);
                    //File was upload to the temp folder. 
                    //Path to this file is setting to the textFieldPath field
                    String configText = fileUploadReceiver.getOutputStream().toString();
                    taXSLTemplate.setValue(configText);

//                   silkConfigTextArea.setValue(
//                            FileUploadReceiver.file
//                            .toString());
                    //textFieldPath.setReadOnly(true);
                } //If upload was interrupt by user
                else {
                    //textFieldPath.setReadOnly(false);
                    taXSLTemplate.setValue("");
                    //textFieldPath.setReadOnly(true);
                    fl = 0;
                }
            }

           
        });

        // The window with upload information
        uploadInfoWindow = new UploadInfoWindow(fileUpload);
        
         mainLayout.addComponent(fileUpload);
        

        tfInputPredicate = new TextField();
        tfInputPredicate.setNullRepresentation("");
        tfInputPredicate.setCaption("Apply the script to all values (one by one) of the predicate:");
        tfInputPredicate.setImmediate(false);
        tfInputPredicate.setWidth("100%");
        tfInputPredicate.setHeight("-1px");
        tfInputPredicate.setValue(XML_VALUE_PREDICATE);
        tfInputPredicate.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException("Input predicate must be filled!");
            }
        });
        mainLayout.addComponent(tfInputPredicate);



        // OptionGroup graphOption
        ogOutputFormat = new OptionGroup("Output:");

        ogOutputFormat.addItem(0);
        ogOutputFormat.setItemCaption(0, "RDF/XML");

        ogOutputFormat.addItem(1);
        ogOutputFormat.setItemCaption(1, "Turtle");

        ogOutputFormat.addItem(2);
        ogOutputFormat.setItemCaption(0, "Wrap each output of the script as a literal value of the predicate:");

        ogOutputFormat.setImmediate(true);
        
        ogOutputFormat.setNullSelectionAllowed(false);
        ogOutputFormat.setWidth("-1px");
        ogOutputFormat.setHeight("-1px");
        ogOutputFormat.setMultiSelect(false);


        ogOutputFormat.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                final String valueString = String.valueOf(event.getProperty()
                        .getValue());
                if (valueString.equals(OUTPUT_RDFXML) || valueString.equals(OUTPUT_TTL)) {
                    tfOutputPredicate.setEnabled(true);
                } else {
                    //it is not option 3, disable textbox with output pred
                    tfOutputPredicate.setEnabled(false);
                }
//                Notification.show("Value changed:", valueString,
//                        Type.TRAY_NOTIFICATION);
            }
        });


        tfOutputPredicate = new TextField();
        tfOutputPredicate.setNullRepresentation("");
        tfOutputPredicate.setCaption("");
        tfOutputPredicate.setImmediate(false);
        tfOutputPredicate.setWidth("100%");
        tfOutputPredicate.setHeight("-1px");
        tfOutputPredicate.setValue(XML_VALUE_PREDICATE);
//        inputPredicate.addValidator(new Validator() {
//            @Override
//            public void validate(Object value) throws Validator.InvalidValueException {
//                if (value.getClass() == String.class && !((String) value).isEmpty()) {
//                    return;
//                }
//                throw new Validator.InvalidValueException("Output predicate must be filled!");
//            }
//        });

        mainLayout.addComponent(tfOutputPredicate);
        
        
         //***************
        // TEXT AREA
        //***************

         taXSLTemplate = new TextArea();

//	

        taXSLTemplate.setNullRepresentation("");
        taXSLTemplate.setImmediate(false);
        taXSLTemplate.setWidth("100%");
        taXSLTemplate.setHeight("300px");
        taXSLTemplate.setVisible(false);
//		silkConfigTextArea.setInputPrompt(
//				"PREFIX br:<http://purl.org/business-register#>\nMODIFY\nDELETE { ?s pc:contact ?o}\nINSERT { ?s br:contact ?o}\nWHERE {\n\t     ?s a gr:BusinessEntity .\n\t      ?s pc:contact ?o\n}");

        mainLayout.addComponent(taXSLTemplate);
//        mainLayout.setColumnExpandRatio(0, 0.00001f);
//        mainLayout.setColumnExpandRatio(1, 0.99999f);



        return mainLayout;
    }

    @Override
    public void setConfiguration(SimpleXSLTConfig conf) throws ConfigException {
        
        taXSLTemplate.setValue(conf.getXslTemplate());
         
        
        tfInputPredicate.setValue(conf.getInputPredicate());
         tfOutputPredicate.setValue(conf.getOutputPredicate());
         
         switch(conf.getOutputType()) {
             case RDFXML:  ogOutputFormat.select(0); tfOutputPredicate.setEnabled(false); break;
                 case TTL:  ogOutputFormat.select(1); tfOutputPredicate.setEnabled(false); break; 
                     case Literal:  ogOutputFormat.select(2); tfOutputPredicate.setEnabled(true); break; 
         }
    
         
         
  

    }

    @Override
    public SimpleXSLTConfig getConfiguration() throws ConfigException {
        //get the conf from the dialog
        //check that certain xslt was uploaded
         if (taXSLTemplate.getValue().trim().isEmpty()) {
           //no config!
           throw new ConfigException("No configuration file uploaded");
       }
        
         if (tfInputPredicate.getValue().trim().isEmpty()) {
              throw new ConfigException("No input predicate");
         }
         
         
             if (ogOutputFormat.getValue().equals("2") && tfOutputPredicate.getValue().trim().isEmpty()) {
              throw new ConfigException("No output predicate");
         }
        
         
      
          
       //prepare output type:
        SimpleXSLTConfig.OutputType ot = null;
       switch(ogOutputFormat.getId()) {
             case "0":  ot = OutputType.RDFXML; break;
              case "1":  ot = OutputType.TTL; break;  
                  case "2":  ot = OutputType.Literal; break;
         }
       
       
        //TODO check output predicate existence if choice 3 is selected
        if (!tfInputPredicate.isValid()) {
            throw new ConfigException();
        } else {
            SimpleXSLTConfig conf = new SimpleXSLTConfig(taXSLTemplate.getValue().trim(), tfInputPredicate.getValue().trim(), ot, tfOutputPredicate.getValue().trim() );
            return conf;
        }
        
        
        
        
        
    }
}

/**
     * Upload selected file to template directory
     *
     * @author Maria Kukhar
     *
     */
    class FileUploadReceiver implements Upload.Receiver {

    private static final long serialVersionUID = 5099459605355200117L;
//    private static final int searchedByte = '\n';
//    private static int total = 0;
//    private boolean sleep = false;
//    public static String fileName;
//    public static File file;
//    public static Path path;
//    private DPUContext context;
    
    private OutputStream fos;
    
    public OutputStream getOutputStream() {
        return fos;
    }

    /**
     * return an OutputStream
     */
    @Override
    public OutputStream receiveUpload(final String filename,
            final String MIMEType) {
//        fileName = filename;

//        //get the dpu id
//        context
        
        
        
//        // creates file manager
//        FileManager fileManager = new FileManager(context);
//        // obtains file in sub-directory in global directory
//        file = fileManager.getGlobal().directory("silkConfs").directory("x").file(filename);
        

//            File globalDirectory = context.getGlobalDirectory();
//
//            try {
//                //create template directory
//                path = Files.createTempDirectory("SilkConfUpload");
//            } catch (IOException e) {
//                throw new RuntimeException(e.getMessage(), e);
//            }



//            file = new File("/" + path + "/" + filename); // path for upload file in temp directory

      
        fos = new ByteArrayOutputStream();
        return fos;
        
    

    }

}

/**
 * Dialog for uploading status. Appear automatically after file upload start.
 *
 * @author Maria Kukhar
 *
 */
class UploadInfoWindow extends Window implements Upload.StartedListener,
        Upload.ProgressListener, Upload.FinishedListener {

    private static final long serialVersionUID = 1L;
    private final Label state = new Label();
    private final Label fileName = new Label();
    private final Label textualProgress = new Label();
    private final ProgressIndicator pi = new ProgressIndicator();
    private final Button cancelButton;
    private final Upload upload;

    /**
     * Basic constructor
     *
     * @param upload. Upload component
     */
    public UploadInfoWindow(Upload nextUpload) {

        super("Status");
        this.upload = nextUpload;
        this.cancelButton = new Button("Cancel");

        setComponent();

    }

    private void setComponent() {
        addStyleName("upload-info");

        setResizable(false);
        setDraggable(false);

        final FormLayout formLayout = new FormLayout();
        setContent(formLayout);
        formLayout.setMargin(true);

        final HorizontalLayout stateLayout = new HorizontalLayout();
        stateLayout.setSpacing(true);
        stateLayout.addComponent(state);

        cancelButton.addClickListener(new Button.ClickListener() {
            /**
             * Upload interruption
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final Button.ClickEvent event) {
                upload.interruptUpload();
                SimpleXSLTDialog.fl = 1;
            }
        });
        cancelButton.setVisible(false);
        cancelButton.setStyleName("small");
        stateLayout.addComponent(cancelButton);

        stateLayout.setCaption("Current state");
        state.setValue("Idle");
        formLayout.addComponent(stateLayout);

        fileName.setCaption("File name");
        formLayout.addComponent(fileName);

        //progress indicator
        pi.setCaption("Progress");
        pi.setVisible(false);
        formLayout.addComponent(pi);

        textualProgress.setVisible(false);
        formLayout.addComponent(textualProgress);

        upload.addStartedListener(this);
        upload.addProgressListener(this);
        upload.addFinishedListener(this);
    }

    /**
     * this method gets called immediately after upload is finished
     */
    @Override
    public void uploadFinished(final Upload.FinishedEvent event) {
        state.setValue("Idle");
        pi.setVisible(false);
        textualProgress.setVisible(false);
        cancelButton.setVisible(false);

    }

    /**
     * this method gets called immediately after upload is started
     */
    @Override
    public void uploadStarted(final Upload.StartedEvent event) {

        pi.setValue(0f);
        pi.setVisible(true);
        pi.setPollingInterval(500); // hit server frequantly to get
        textualProgress.setVisible(true);
        // updates to client
        state.setValue("Uploading");
        fileName.setValue(event.getFilename());

        cancelButton.setVisible(true);
    }

    /**
     * this method shows update progress
     */
    @Override
    public void updateProgress(final long readBytes, final long contentLength) {
        // this method gets called several times during the update
        pi.setValue(new Float(readBytes / (float) contentLength));
        textualProgress.setValue(
                "Processed " + (readBytes / 1024) + " k bytes of "
                + (contentLength / 1024) + " k");
    }
}