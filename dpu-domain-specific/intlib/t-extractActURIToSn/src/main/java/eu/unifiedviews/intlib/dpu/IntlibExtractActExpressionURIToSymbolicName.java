package eu.unifiedviews.intlib.dpu;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsTransformer
public class IntlibExtractActExpressionURIToSymbolicName extends AbstractDpu<IntlibExtractActExpressionURIToSymbolicNameConfig_V1> {

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;

    private static final Logger LOG = LoggerFactory.getLogger(IntlibExtractActExpressionURIToSymbolicName.class);

    public IntlibExtractActExpressionURIToSymbolicName() {
        super(IntlibExtractActExpressionURIToSymbolicNameVaadinDialog.class, ConfigHistory.noHistory(IntlibExtractActExpressionURIToSymbolicNameConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        Map<String, Boolean> preparedSNs = new HashMap();

        LOG.info("DPU is running ...");

        // RepositoryConnection connection = null;
        try {

            //FIRST ITERATION to process xml files and put them on the output
            FilesDataUnit.Iteration filesIteration = filesInput.getIteration();

            if (!filesIteration.hasNext()) {
                return;
            }

            //iterate over files 
            int i = 0;
            int processedSuccessfully = 0;
            while (filesIteration.hasNext()) {

                i++;
                FilesDataUnit.Entry entry = filesIteration.next();

                //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());

                //get URI from resource
                String fileURIString = entry.getFileURIString();
                LOG.debug("File URI is: {}", fileURIString);

                String filePath = fileURIString.substring("file:".length());

                //build new symbolic name (containing expression URI)
                String newSymbolicName = null;
                try {
                    newSymbolicName = getResourceURIFromFile(filePath);
                } catch (IOException exc) {
                    LOG.error("Error processing file: {}", exc.getLocalizedMessage());
                    LOG.info("Processing of the file skippede");
                    continue;
                }

                if (newSymbolicName == null) {
                    LOG.error("Expression URI not parsed successfully, skipped");
                    continue;
                }
                LOG.debug("Expression URI is {}", newSymbolicName);

                //OUTPUT
                if (preparedSNs.containsKey(newSymbolicName)) {
                    LOG.warn("Symbolic name {} was already used during this DPU's execution. Possible cause: Two input files represent the same full text of acts.", newSymbolicName);
                    LOG.info("Output not created for this entry.");
                }
                else {

                    //mark this sn for further checks for duplicity
                    preparedSNs.put(newSymbolicName, Boolean.TRUE);

                    filesOutput.addExistingFile(newSymbolicName, entry.getFileURIString());

                    //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
                    String outputVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
                    if (outputVirtualPath != null) {
                        VirtualPathHelpers.setVirtualPath(filesOutput, newSymbolicName, outputVirtualPath);
                    }

                    LOG.info("Output created successfully, sn: {}, file: {}", newSymbolicName, entry.getFileURIString());
                    processedSuccessfully++;
                }

                if (ctx.getExecMasterContext().getDpuContext().canceled()) {
                    LOG.info("DPU cancelled");
                    return;
                }

            }
            ctx.getExecMasterContext().getDpuContext().sendMessage(DPUContext.MessageType.INFO, "Successfully processed " + processedSuccessfully + "/" + i + " files");

        } catch (DataUnitException ex) {
            ctx.getExecMasterContext().getDpuContext().sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        }

    }

    //    @Override
    //    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
    //        return new IntlibExtractActExpressionURIToSymbolicNameVaadinDialog();
    //    }

    private String getResourceURIFromFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (line.contains("article resource=")) {
                LOG.debug("matched resource URl");
                //to get start of the resource
                String temp = line.substring(line.indexOf("article resource=") + "article resource=".length() + 1);

                //to get proper end
                return temp.substring(0, temp.indexOf("\""));

            }

        }

        return null;
    }

}
