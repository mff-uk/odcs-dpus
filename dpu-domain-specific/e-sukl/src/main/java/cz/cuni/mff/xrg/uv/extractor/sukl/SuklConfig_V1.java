package cz.cuni.mff.xrg.uv.extractor.sukl;

/**
 * DPU's configuration class.
 */
public class SuklConfig_V1 {

    private boolean countNumberOfMissing = false;

    /**
     * If true and we can not download file then terminates DPU with exception. If false,
     * and we can not download file only log is created.
     */
    private boolean failOnDownloadError = true;

    private boolean filesToOutput = false;

    private boolean newFileToOutput = false;

    private boolean deletePagesOnError = false;

    /**
     * Path to directory where deleted fils should be stored.
     */
    private String deletedFileStorage = "~/sukl";

    public SuklConfig_V1() {

    }

    public boolean isCountNumberOfMissing() {
        return countNumberOfMissing;
    }

    public void setCountNumberOfMissing(boolean countNumberOfMissing) {
        this.countNumberOfMissing = countNumberOfMissing;
    }

    public boolean isFailOnDownloadError() {
        return failOnDownloadError;
    }

    public void setFailOnDownloadError(boolean failOnDownloadError) {
        this.failOnDownloadError = failOnDownloadError;
    }

    public boolean isFilesToOutput() {
        return filesToOutput;
    }

    public void setFilesToOutput(boolean filesToOutput) {
        this.filesToOutput = filesToOutput;
    }

    public boolean isNewFileToOutput() {
        return newFileToOutput;
    }

    public void setNewFileToOutput(boolean newFileToOutput) {
        this.newFileToOutput = newFileToOutput;
    }

    public String getDeletedFileStorage() {
        return deletedFileStorage;
    }

    public void setDeletedFileStorage(String deletedFileStorage) {
        this.deletedFileStorage = deletedFileStorage;
    }

    public boolean isDeletePagesOnError() {
        return deletePagesOnError;
    }

    public void setDeletePagesOnError(boolean deletePagesOnError) {
        this.deletePagesOnError = deletePagesOnError;
    }
    
}
