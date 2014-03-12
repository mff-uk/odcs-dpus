package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * DPU's configuration dialog. NO Dialog needed for this DPU
 *
 */
public class UriGeneratorDialog extends BaseConfigDialog<UriGeneratorConfig> {

	private static final Logger log = LoggerFactory.getLogger(
			UriGeneratorDialog.class);

	private VerticalLayout mainLayout;

	private Label lFileName;

	private TextArea taFileConf;

	private UploadInfoWindow uploadInfoWindow;

	//TODO refactor
	static int fl = 0;

	public UriGeneratorDialog() {
		super(UriGeneratorConfig.class);
		buildMainLayout();
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(mainLayout);
		setCompositionRoot(panel);
	}

	@Override
	public void setConfiguration(UriGeneratorConfig conf) throws ConfigException {

		if (!conf.getStoredXsltFilePath().isEmpty()) {
			String fileContent = DataUnitUtils.readFile(conf
					.getStoredXsltFilePath());
			taFileConf.setValue(fileContent);
			lFileName.setValue(conf.getfileNameShownInDialog());
			log.debug("Conf file loaded from {}", conf.getStoredXsltFilePath());
		}

	}

	@Override
	public UriGeneratorConfig getConfiguration() throws ConfigException {

		//check that certain xslt was uploaded
		if (taFileConf.getValue().trim().isEmpty()) {
			//no config!
			throw new ConfigException("No configuration file uploaded");

		}

          //store the file with XSL template 
		//TODO take the dpu's space
		String fileWithXSLT = System.getProperty("java.io.tmpdir") + File.separator + "xsltDPU" + UUID
				.randomUUID().toString();
		//save the config from textarea to the file 
		DataUnitUtils.storeStringToTempFile(taFileConf.getValue(), fileWithXSLT);
		log.debug("XSLT stored to {}", fileWithXSLT);

		return new UriGeneratorConfig(lFileName.getValue().trim(), fileWithXSLT);

	}

	private void buildMainLayout() {

		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100%");
		setHeight("100%");

		//upload 
		final FileUploadReceiver fileUploadReceiver = new FileUploadReceiver();

		//Upload component
		Upload fileUpload = new Upload("Config for URI Gen: ",
				fileUploadReceiver);
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

							String configText = null;
							try {
								configText = ((ByteArrayOutputStream) fileUploadReceiver
								.getOutputStream()).toString("UTF-8");
							} catch (UnsupportedEncodingException ex) {
								java.util.logging.Logger.getLogger(
										UriGeneratorDialog.class.getName()).log(
										Level.SEVERE, null, ex);
							}
							if (configText == null) {
								log.error(
										"Cannot save XSLT template with UTF-8 encoding");
								return;
							}

							taFileConf.setValue(configText);
							lFileName.setValue("File " + fileUploadReceiver
									.getFileName() + " was successfully uploaded.");

//                 
						} else {
							//textFieldPath.setReadOnly(false);
							taFileConf.setValue("");
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

		taFileConf = new TextArea();
		taFileConf.setNullRepresentation("");
		taFileConf.setImmediate(false);
		taFileConf.setWidth("100%");
		taFileConf.setHeight("100%");
		taFileConf.setVisible(true);
//		silkConfigTextArea.setInputPrompt(
//				"PREFIX br:<http://purl.org/business-register#>\nMODIFY\nDELETE { ?s pc:contact ?o}\nINSERT { ?s br:contact ?o}\nWHERE {\n\t     ?s a gr:BusinessEntity .\n\t      ?s pc:contact ?o\n}");

		mainLayout.addComponent(taFileConf);
		mainLayout.setExpandRatio(taFileConf, 1.0f);

	}
}

/**
 * Upload selected file to template directory
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

		this.fileName = filename;
		fos = new ByteArrayOutputStream();
		return fos;

	}

}

/**
 * Dialog for uploading status. Appear automatically after file upload start.
 *
 * @author tknap
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
				UriGeneratorDialog.fl = 1;
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
