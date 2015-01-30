package eu.unifiedviews.cssz;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.cssz.cz.komix.xls2csv.Demo01;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.openrdf.repository.RepositoryConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * DPU, which wraps xls2csv utility
 * 
 * Input: list of XLS files
 * Output: list of CSV files
 * 
 * Note: One XLS file may generate more output CSV files
 * 
 * @author tomasknap
 */
@DPU.AsTransformer
public class Xls2csv extends DpuAdvancedBase<Xls2csvConfig_V1> {

	private static final Logger log = LoggerFactory.getLogger(Xls2csv.class);
		
        @DataUnit.AsInput(name = "input")
        public FilesDataUnit inFilesData;

        @DataUnit.AsOutput(name = "output")
        public WritableFilesDataUnit outFilesData;
        
	public Xls2csv() {
		super(Xls2csvConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
        log.info("DPU is running ...");
        
        //System.setProperty("file.encoding", "UTF-8");
        
        log.info("File encoding: {}", System.getProperty("file.encoding"));
        
//        log.info("xls2csv library is about to be executed");
//        try {
//            //Process p = Runtime.getRuntime().exec("java -DconfigFile=" + configFile.getCanonicalPath() + " -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/silk_2.5.2/silk.jar");
//
//            Process p = Runtime.getRuntime().exec("java -Dfile.encoding=utf8 -jar xls2csv.jar");
//
//            printProcessOutput(p);
//        } catch (IOException ex) {
//            log.error(ex.getLocalizedMessage());
//            context.sendMessage(DPUContext.MessageType.ERROR, "Problem executing Silk: "
//                    + ex.getMessage());
//        }
//        log.info("xls2csv library was executed");
        
        
        //
        // get file iterator
        //
        final FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = inFilesData.getIteration();
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "DPU Failed", "Can't get file iterator.", ex);
            return;
        }
        //
        // filter data here
        //
//        boolean useSymbolicName = config.getPredicate().equals(FilesFilterConfig_V1.SYMBOLIC_NAME);

        try {
            while (filesIteration.hasNext()) {
                final FilesDataUnit.Entry entry = filesIteration.next();

                
                String symbolicName = entry.getSymbolicName();
                String fileURIString = entry.getFileURIString();
                log.info("Processing file with symbolic name {} and URI {}", symbolicName, fileURIString);
                
                
                //DEMO1

                Demo01 demo = new Demo01();
                demo.souborNames = new HashMap<Integer, String>();
                demo.faktCubeNames = new HashMap<Integer, String>();
                //prepare input file based on the processed fileURIString from input file data unit
                //remove .xls", it should be original filename without .xls
                demo.fileName = fileURIString.substring(0, fileURIString.lastIndexOf("."));
                
                //remove also path, to get just the filename without extension
                demo.fileName = demo.fileName.substring(demo.fileName.lastIndexOf(File.separatorChar)+1);
                log.debug("Filename (without .xls) send to Demo01 is: {}", demo.fileName);                  
                
                //to get file path (to the dir with filename)
                demo.FILEPATH =  fileURIString.substring(0, fileURIString.lastIndexOf(File.separatorChar)+1);
                demo.FILEPATH =  demo.FILEPATH.substring("file:".length());
                log.debug("Filepath (without filename)  is {}", demo.FILEPATH);
                
                try {

                    //get from config
                    demo.TEMPLATE_PREFIX = config.getTemplate_prefix();
                    //Demo01.PREFIX = config.getPrefix();
                    //Demo01.SUFFIX = config.getSuffix();

                    demo.init();
                    demo.parse();
                    //saves the process file to .csv (to the same folder)
                    demo.save();
                } catch (IOException e) {
                    log.error("Problem processing {}: {}", fileURIString, e.getLocalizedMessage());
                }
                //end DEMO1
                

                //put the outputted CSV files (stored in Demo01.outputFilePathList) to output data unit
                int i = 0;
                for (String newOut : demo.outputFilePathList) {
                    i++;
                    newOut = "file:" + newOut;
                    try {
                        //to generate different symbolic name for each output file
                        String newSymbolicName = symbolicName + "-" + String.valueOf(i);
                        log.info("Creating output file with symbolic name {} and path {}", newSymbolicName, newOut);
                        outFilesData.addExistingFile(newSymbolicName, newOut);
                       VirtualPathHelpers.setVirtualPath(outFilesData, newSymbolicName, newOut);
                    } catch (DataUnitException ex) {
                       context.sendMessage(DPUContext.MessageType.ERROR,
                           "Problem with DataUnit", null, ex);
                    }
                }
                
                //
                // TODO here we should rather somehow copy metadata from input
                //  to output metadata graps, as otherwise we create new
                //  triples
//                outFilesData.addExistingFile(symbolicName, );
                // TODO Remove this
                // as a hack copy virtual path now
//                final String virtualPath = VirtualPathHelpers.getVirtualPath(inFilesData, entry.getSymbolicName());
//                if (virtualPath == null) {
//                    log.debug("Null virtualPath for {}", entry.getSymbolicName());
//                } else {
//                    VirtualPathHelpers.setVirtualPath(outFilesData, entry.getSymbolicName(), virtualPath);
//                }
            }
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Problem with DataUnit", "", ex);
        }
        //
        // close
        //
        try {
            filesIteration.close();
        } catch (DataUnitException ex) {
            log.warn("Error in close.", ex);
        }
        
        
        
       
             
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new Xls2csvVaadinDialog();
    }
	
}
