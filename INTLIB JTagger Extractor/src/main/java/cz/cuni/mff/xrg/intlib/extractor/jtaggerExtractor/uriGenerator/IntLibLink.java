package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator;

import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.JTaggerExtractor;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.link.Configuration;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.link.LawDocument;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.link.Work;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut.SparqlLoader;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut.ValidityMap;
import cz.cuni.xrg.intlib.commons.dpu.DPUContext;

import java.io.*;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Jakub Stárka
 * @author Jakub Klímek
 */
public class IntLibLink {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(IntLibLink.class);

	private static DPUContext context = null;

	private static final String VSTUPNI_SOUBOR = "base.xml";

	public static void help() {
		IntLibLink.help("");
	}

	public static void help(String error) {

		if (!error.isEmpty()) {
			logger.error(error);
			logger.info("");
		}

		System.out
				.print("Name: \n"
						+ "\tParse a input and insert URI to act, judgments, business entities, ...\n"
						+ "\n"
						+ "Synopsis: \n"
						+ "\tjava -jar IntLib.jar -inputFile=fileName -outputFile=fileName [-configFile=<configFile>]\n"
						+ "\tjava -jar IntLib.jar -inputDir=dirName -outputDir=dirName [-configFile=<configFile>]\n"
						+ "\n"
						+ "Arguments:\n"
						+ "\t -inputFile=fileName\n"
						+ "\t\tFile name of input file. Relative or absolute path accepted.\n"
						+ "\t -inputDir=dirName\n"
						+ "\t\tDir name of input directory. All XML files in the folder will be processed and stored to output directory.\n"
						+ "\t -outputFile=fileName\n"
						+ "\t\tFile name of output file. Relative or absolute path accepted.\n"
						+ "\t -outputDir=dirName\n"
						+ "\t\tDir name of output directory. The directory has to exist.\n"
						+ "\t -configFile=configFile\n"
						+ "\t\tFile name of knowledge base. If not defined \"base.xml\" is used.\n"
						+ "\n"
						+ "Usage example:\n"
						+ "\tjava -jar IntLib.jar -inputFile=act1.xml -outputFile=act1.out.xml -configFile=base.xml\n"
						+ "\tjava -jar IntLib.jar -inputDir=in -outputDir=out\n");

	}

	public static void processFiles(String input,
			String output,
			DPUContext context) {
		String[] args = new String[] { "-inputFile=" + input,
				"-outputFile=" + output };
		IntLibLink.context = context;
		main(args);
	}

	public static void processFiles(String input,
			String output,
			String configURiGen,
			DPUContext context) {
		String[] args = new String[] { "-inputFile=" + input,
				"-outputFile=" + output, "-configFile=" + configURiGen };
		IntLibLink.context = context;
		main(args);
	}

	public static void processDirectories(String input,
			String output,
			DPUContext context) {
		String[] args = new String[] { "-inputDir=" + input,
				"-outputDir=" + output };
		IntLibLink.context = context;
		main(args);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		// Handler[] handlers = Logger.getLogger("").getHandlers();
		// for(Handler h : handlers){
		// h.setFormatter(new MyCustomLogFormatter());
		// }

		// TODO code application logic here
		// Logger.getLogger("intlib").log(new Work("cs", WorkType.ACT, 1994,
		// "5", null, null, null));
		String inputDir = "";
		String inputFile = "";
		String outputDir = "";
		String outputFile = "";
		String configFile = VSTUPNI_SOUBOR;

		// set the tempDir
		if (context != null) {
			SparqlLoader.tempDir = context.getWorkingDir();
			logger.info("Tmp set as: " + context.getWorkingDir());

		} else {
			SparqlLoader.tempDir = new File(
					System.getProperty("java.io.tmpdir"));
		}

		try {
			SparqlLoader.renewCache(3600);
		} catch (UnsupportedEncodingException ex) {
			logger.error(ex.getLocalizedMessage());
		}

		/*
		 * Logger.getLogger("intlib").log(System.getenv("APPDATA"));
		 * Logger.getLogger("intlib").log(SparqlLoader.loadCourts());
		 * Logger.getLogger("intlib").log(SparqlLoader.loadExpressions());
		 * Logger.getLogger("intlib").log(SparqlLoader.loadLaws());
		 */

		/*
		 * if (true) { return; }
		 */

		// nacteme vsechny argument
		for (String argument : args) {
			if (argument.startsWith("-inputFile=")) {
				inputFile = argument.replaceFirst("-inputFile=", "");
			} else if (argument.startsWith("-inputDir")) {
				inputDir = argument.replaceFirst("-inputDir=", "");
			} else if (argument.startsWith("-outputFile=")) {
				outputFile = argument.replaceFirst("-outputFile=", "");
			} else if (argument.startsWith("-outputDir")) {
				outputDir = argument.replaceFirst("-outputDir=", "");
			} else if (argument.startsWith("-configFile=")) {
				configFile = argument.replaceFirst("-configFile=", "");
			}
		}

		// zkontrolujeme jestli neni neco spatne
		if ((inputFile.isEmpty() && inputDir.isEmpty())
				|| (!inputFile.isEmpty() && !inputDir.isEmpty())
				|| (outputFile.isEmpty() && outputDir.isEmpty())
				|| (!outputFile.isEmpty() && !outputDir.isEmpty())
				|| (!inputFile.isEmpty() && outputFile.isEmpty())
				|| (!inputDir.isEmpty() && outputDir.isEmpty())) {
			IntLibLink.help();
			return;
		}

		File input = null;

		if (!inputFile.isEmpty()) {
			input = new File(inputFile);

			// zkontrolujeme existenci souboru
			if (!input.exists()) {
				IntLibLink
						.help("Input file '" + inputFile + "' does not exist");
				return;
			}
		} else {
			input = new File(inputDir);
			// pokud neexistuje vstupni adresar
			if (!input.exists()) {
				IntLibLink.help("Input dir '" + inputDir + "' does not exist");
				return;
			}

			File output = new File(outputDir);
			// pokud neexistuje vystupni adresar
			if (!output.exists()) {
				IntLibLink.help("Output dir '" + inputDir + "' does not exist");
				return;
			}
		}

		File config = new File(configFile);
		if (!config.exists()) {
			IntLibLink.help("Configuration file '" + configFile
					+ "' does not exist");
			return;
		}

		logger.debug("About to load configuration:");
		// nacteme konfiguraci
		Configuration.load(configFile);
		logger.info("Configuration loaded successfully.");
		try {
			ValidityMap.load(context);
			logger.info("ValidityMap loaded successfully");
		} catch (FileNotFoundException ex) {

			logger.error("ValidityMap error:" + ex.getLocalizedMessage());
		} catch (IOException ex) {
			logger.error("ValidityMap error:" + ex.getLocalizedMessage());
		}
		try {
			LawDocument.getGenericShortcuts().load(context);
			logger.info("Shortcuts loaded successfully");
		} catch (FileNotFoundException ex) {
			logger.error("Shortcuts error:" + ex.getLocalizedMessage());
		} catch (IOException ex) {
			logger.error("Shortcuts error:" + ex.getLocalizedMessage());
		}

		// ValidityMap.getInfo();

		// zpracujeme adresar
		if (!inputDir.isEmpty()) {
			File dir = new File(inputDir);
			File[] dirList = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});
			int count = 0;
			for (File inputXML : dirList) {
				count++;
				File outputXML = new File(outputDir, inputXML.getName());
				LawDocument doc = new LawDocument(logger);

				int lastrec = Work.recognized;
				int lastunrec = Work.unrecognized;
				int lastmiss = Work.missing;
				int lastnedost = Work.nedostatecne;
				int lastinstit = Work.institutions;
				int lastshortcut = Work.lastShortcutUsed;
				int lastnumber = Work.lastNumberUsed;
				int laststarttrimmed = Work.startShortTrimmed;
				int lastendtrimmed = Work.endShortTrimmed;
				int lastexpendtrimmed = Work.expressionEndTrimmed;
				int lastaz = Work.az_expanded;

				logger.info(
						"Parsing file {0}/{1}: {2}",
						new Object[] { Integer.toString(count),
								Integer.toString(dirList.length),
								inputXML.getName() });
				// Logger.getLogger(IntLibLink.class.getName()).log(Level.INFO,
				// "");

				doc.transform(inputXML.getAbsolutePath(),
						outputXML.getAbsolutePath());
				Work.lastFoundShortcut = null;

				// Logger.getLogger("intlib").log(Level.INFO, "");
				logger.info("Recognized:" + (Work.recognized - lastrec));
				logger.info("Unrecognized:" + (Work.unrecognized - lastunrec));
				logger.info("Missing:" + (Work.missing - lastmiss));
				logger.info("Nedostatecne:" + (Work.nedostatecne - lastnedost));
				logger.info("Last shortcut used:"
						+ (Work.lastShortcutUsed - lastshortcut));
				logger.info("Last number used:"
						+ (Work.lastNumberUsed - lastnumber));
				logger.info("Shortcuts trimmed from start:"
						+ (Work.startShortTrimmed - laststarttrimmed));
				logger.info("Shortcuts trimmed from end:"
						+ (Work.endShortTrimmed - lastendtrimmed));
				logger.info("Až:" + (Work.az_expanded - lastaz));
				logger.info("Expressions trimmed from end:"
						+ (Work.expressionEndTrimmed - lastexpendtrimmed));
				logger.info("Institutions:" + (Work.institutions - lastinstit));
				logger.info("Recognized total: "
						+ (Work.recognized + Work.institutions - lastrec - lastinstit));
				logger.info("Unrecognized total: "
						+ (Work.nedostatecne + Work.missing + Work.unrecognized
								- lastunrec - lastmiss - lastnedost));
				logger.info("Success rate: "
						+ (int) ((double) (Work.recognized + Work.institutions
								- lastrec - lastinstit)
								/ (double) (Work.recognized + Work.institutions
										- lastrec - lastinstit
										+ Work.nedostatecne + Work.missing
										+ Work.unrecognized - lastunrec
										- lastmiss - lastnedost) * 100) + "%");
				logger.info("Realistic (- institutions) success rate: "
						+ (int) ((double) (Work.recognized - lastrec)
								/ (double) (Work.recognized - lastrec
										+ Work.nedostatecne + Work.missing
										+ Work.unrecognized - lastunrec
										- lastmiss - lastnedost) * 100) + "%");
			}
		}

		// zpracujeme jeden soubor
		if (!inputFile.isEmpty()) {
			LawDocument doc = new LawDocument(logger);
			doc.transform(inputFile, outputFile);
		}

		logger.info("");
		logger.info("Total links:" + (Work.recognized + Work.unrecognized));
		logger.info("Recognized:" + Work.recognized);
		logger.info("Unrecognized:" + Work.unrecognized);
		logger.info("Missing:" + Work.missing);
		logger.info("Nedostatecne:" + Work.nedostatecne);
		logger.info("Last shortcut used:" + Work.lastShortcutUsed);
		logger.info("Last number used:" + Work.lastNumberUsed);
		logger.info("Shortcuts trimmed from start:" + Work.startShortTrimmed);
		logger.info("Shortcuts trimmed from end:" + Work.endShortTrimmed);
		logger.info("Až:" + Work.az_expanded);
		logger.info("Expressions trimmed from end:" + Work.expressionEndTrimmed);
		logger.info("Institutions:" + Work.institutions);
		logger.info("Recognized total: "
				+ (Work.recognized + Work.institutions));
		logger.info("Unrecognized total: "
				+ (Work.nedostatecne + Work.missing + Work.unrecognized));
		logger.info("Success rate: "
				+ (int) ((double) (Work.recognized + Work.institutions)
						/ (double) (Work.recognized + Work.institutions
								+ Work.nedostatecne + Work.missing + Work.unrecognized) * 100)
				+ "%");
		logger.info("Realistic success rate: "
				+ (int) ((double) (Work.recognized)
						/ (double) (Work.recognized + Work.nedostatecne
								+ Work.missing + Work.unrecognized) * 100)
				+ "%");

	}

	// public static void processFiles(String file, String output,
	// org.slf4j.Logger logger) {
	// throw new UnsupportedOperationException("Not supported yet."); //To
	// change body of generated methods, choose Tools | Templates.
	// }

}
