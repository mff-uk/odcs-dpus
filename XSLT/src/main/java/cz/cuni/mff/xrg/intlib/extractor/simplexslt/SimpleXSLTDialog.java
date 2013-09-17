package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 * 
 * 
 *
 */
public class SimpleXSLTDialog extends BaseConfigDialog<SimpleXSLTConfig> {

   private static final Logger logger = LoggerFactory.getLogger(SimpleXSLTDialog.class);
   

    private static final String OUTPUT_RDFXML = "RDF/XML";
    private static final String OUTPUT_TTL = "TTL";
    private static final String OUTPUT_WRAP = "WRAP";
    
    private VerticalLayout mainLayout;
    private TextField tfInputPredicate; 
    private TextField tfOutputPredicate;
    
    //output of xslt - could be xml/text/...
    private TextField tfOutputXSLTMethod; 
    
    private TextField tfEscaped; 
    
    private OptionGroup ogOutputFormat;
    
    private Label lFileName;
    
    
    
    private TextArea taXSLTemplate;
    
    private UploadInfoWindow uploadInfoWindow;
    
    //TODO refactor
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
        Upload fileUpload = new Upload("XSLT Template: ", fileUploadReceiver);
        fileUpload.setImmediate(true);
        fileUpload.setButtonCaption("Upload");
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
                   
                    String configText = fileUploadReceiver.getOutputStream().toString();
                    taXSLTemplate.setValue(configText);
                    lFileName.setValue("File " + fileUploadReceiver.getFileName() + " was successfully uploaded.");

//                 
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
        
//label for xslt filename
         lFileName = new Label("File not uploaded");
         mainLayout.addComponent(lFileName);
         
         //empty line
         Label emptyLabel = new Label("");
        emptyLabel.setHeight("1em");
        mainLayout.addComponent(emptyLabel);
         
         Label lInput = new Label();
         lInput.setValue("Input:");
         mainLayout.addComponent(lInput);
         
         
        tfInputPredicate = new TextField();
        tfInputPredicate.setCaption("Apply the script to all values (one by one) of the predicate:");
        tfInputPredicate.setWidth("100%");
        tfInputPredicate.setReadOnly(true);
        
         tfInputPredicate.setImmediate(true);
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

        //empty line
        Label emptyLabel2 = new Label("");
        emptyLabel2.setHeight("1em");
        mainLayout.addComponent(emptyLabel2);
        

        // OptionGroup graphOption
        ogOutputFormat = new OptionGroup("Output:");

        ogOutputFormat.addItem(OUTPUT_RDFXML);
        ogOutputFormat.setItemCaption(OUTPUT_RDFXML, OUTPUT_RDFXML);

        ogOutputFormat.addItem(OUTPUT_TTL);
        ogOutputFormat.setItemCaption(OUTPUT_TTL, OUTPUT_TTL);

        ogOutputFormat.addItem(OUTPUT_WRAP);
        ogOutputFormat.setItemCaption(OUTPUT_WRAP, "Wrap each output of the script as a literal value of the predicate:");

       
        ogOutputFormat.select(2);
        ogOutputFormat.setImmediate(true);        
        ogOutputFormat.setNullSelectionAllowed(false);
        //ogOutputFormat.setWidth("-1px");
        //ogOutputFormat.setHeight("-1px");
        ogOutputFormat.setMultiSelect(false);


        //TODO disallow text box with output predicate when choice1 and 2 is selected
        ogOutputFormat.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                final String valueString = String.valueOf(event.getProperty()
                        .getValue());
                if (valueString.equals(OUTPUT_RDFXML) || valueString.equals(OUTPUT_TTL)) {
                    tfOutputPredicate.setEnabled(false);
                } else {
                    //it is not option 3, disable textbox with output pred
                    tfOutputPredicate.setEnabled(true);
                }
//                Notification.show("Value changed:", valueString,
//                        Type.TRAY_NOTIFICATION);
            }
        });

        mainLayout.addComponent(ogOutputFormat);

        tfOutputPredicate = new TextField();
        tfOutputPredicate.setImmediate(true);
        tfOutputPredicate.setWidth("100%");
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
        
        
         //empty line
         Label emptyLabel3 = new Label("");
        emptyLabel3.setHeight("1em");
        mainLayout.addComponent(emptyLabel3);
        
        //TODO validate input
         tfOutputXSLTMethod = new TextField();
        tfOutputXSLTMethod.setImmediate(true);
        //tfOutputMethod.setWidth("100%");
           tfOutputXSLTMethod.setCaption("Specify output XML method of XSLT (text,xml,..) ");
       tfOutputXSLTMethod.setWidth("100%");
        
        mainLayout.addComponent(tfOutputXSLTMethod);

        
         //empty line
         Label emptyLabel4 = new Label("");
        emptyLabel4.setHeight("1em");
        mainLayout.addComponent(emptyLabel4);
        
          //TODO validate input
         tfEscaped = new TextField();
        tfEscaped.setImmediate(true);
        tfEscaped.setCaption("Specify escaping mappings in the form \"string:replacement string2:replacement2\". These mappings are applied when wrapped output is produced. ");
        tfEscaped.setWidth("100%");
      

        mainLayout.addComponent(tfEscaped);
        
        
        
        
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
        lFileName.setValue(conf.getXslTemplateFileName());
         
        
         //tfInputPredicate.setValue(conf.getInputPredicate());
         tfOutputPredicate.setValue(conf.getOutputPredicate());
         
         tfOutputXSLTMethod.setValue(conf.getOutputXSLTMethod());
         tfEscaped.setValue(conf.getEscapedString());
         
         switch(conf.getOutputType()) {
             case RDFXML:  ogOutputFormat.select(OUTPUT_RDFXML); tfOutputPredicate.setEnabled(false); break;
                 case TTL:  ogOutputFormat.select(OUTPUT_TTL); tfOutputPredicate.setEnabled(false); break; 
                     case Literal:  ogOutputFormat.select(OUTPUT_WRAP); tfOutputPredicate.setEnabled(true); break; 
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
        
//         if (tfInputPredicate.getValue().trim().isEmpty()) {
//              throw new ConfigException("No input predicate");
//         }
         
        if (ogOutputFormat.getValue().equals(OUTPUT_WRAP) && tfOutputPredicate.getValue().trim().isEmpty()) {
              throw new ConfigException("No output predicate, but it has to be specied for the given output choice");
         }
        
          if (tfOutputXSLTMethod.getValue().trim().isEmpty()) {
              throw new ConfigException("No xslt output");
         }
         
         
     
          
       //prepare output type:
        SimpleXSLTConfig.OutputType ot = null;
         SimpleXSLTConfig conf = null;
       switch((String)ogOutputFormat.getValue()) {
             case OUTPUT_RDFXML:  
                    ot = OutputType.RDFXML;  
                    conf = new SimpleXSLTConfig(taXSLTemplate.getValue().trim(), lFileName.getValue().trim(), /*tfInputPredicate.getValue().trim()*/ ot, tfOutputXSLTMethod.getValue().trim(), tfEscaped.getValue().trim() );
                    break;
              case OUTPUT_TTL:  
                    ot = OutputType.TTL; 
                    conf = new SimpleXSLTConfig(taXSLTemplate.getValue().trim(), lFileName.getValue().trim(), /*tfInputPredicate.getValue().trim()*/ ot, tfOutputXSLTMethod.getValue().trim(), tfEscaped.getValue().trim());
                    break;  
                  case OUTPUT_WRAP:  
                    ot = OutputType.Literal;
                    conf = new SimpleXSLTConfig(taXSLTemplate.getValue().trim(), lFileName.getValue().trim(), /*tfInputPredicate.getValue().trim()*/ ot, tfOutputPredicate.getValue().trim(), tfOutputXSLTMethod.getValue().trim() , tfEscaped.getValue().trim() );
                    break;
                  default:  throw new ConfigException("One option for the output must be selected");  
         }
       
       

         return conf;


        
        
        
        
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
    private String fileName;
    
    private OutputStream fos;

    public String getFileName() {
        return fileName;
    }

  
    
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

        this.fileName = filename;
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