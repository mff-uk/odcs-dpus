package cz.cuni.mff.xrg.uv.transformer.unzipper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.util.Collection;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsTransformer
public class Main implements DPU {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@DataUnit.AsInput(name = "input")
	public FilesDataUnit input;

	@DataUnit.AsOutput(name = "output")
	public WritableFilesDataUnit output;

    protected DPUContext context;
    
	public Main() {
	}

    @Override
    public void execute(DPUContext context) throws DPUException {
        this.context = context;
        try {
            execute();
        } catch (DataUnitException ex) {
            throw new DPUException(ex);
        }
    }

    public void execute() throws DPUException, DataUnitException {
        final FilesDataUnit.Iteration iter = input.getIteration();
        while (iter.hasNext()) {
            final FilesDataUnit.Entry entry = iter.next();
            final String symbolicName = entry.getSymbolicName();
            
            // test if it's zip file
            if (!isZipFile(symbolicName)) {
                continue;
            }
            
            // create directory
            File outputDir = new File(output.getBaseURIString(), symbolicName);
            outputDir.mkdirs();
            
            LOG.debug("Unzipping {} into {}", symbolicName, 
                    outputDir.toString());            
            try {
                unzip(new File(entry.getFileURIString()), outputDir);
            } catch (Exception ex) {
                context.sendMessage(MessageType.ERROR, "Unzipper failed",
                        String.format("Failed to unzip '%s'", 
                                entry.getFileURIString()), ex);
                return;
            }
            
            // scan directory for new files
            final int outputDirLen = outputDir.getPath().length();
            Collection<File> files = FileUtils.listFiles(outputDir, null, true);
            for (File newFile : files) {
                String relativePath = newFile.getPath().substring(outputDirLen);
                output.addExistingFile(symbolicName + "/" + relativePath, 
                        newFile.toString());
            }
        }
	}

	/**
	 *
	 * @param handler
	 * @return True if given handler name has 'zip' extension.
	 */
	private boolean isZipFile(String symbolicName) {
		final String extension = symbolicName.substring(
                symbolicName.length() - 3);
		return extension.compareToIgnoreCase("zip") == 0;
	}

	/**
	 * Unzip given source zip file into target directory.
	 *
	 * @param source
	 * @param target
	 */
	private void unzip(File source, File target) throws Exception {
		ZipFile zipFile = new ZipFile(source);
		if (zipFile.isEncrypted()) {
			throw new Exception("Zip file is encrypted.");
		}
		zipFile.extractAll(target.toString());
	}

}
