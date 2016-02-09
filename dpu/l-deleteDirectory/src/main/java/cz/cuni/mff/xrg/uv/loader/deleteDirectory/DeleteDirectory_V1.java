package cz.cuni.mff.xrg.uv.loader.deleteDirectory;


import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

/**
 * DPU's configuration class.
 */
@EntityDescription.Entity(type = DeleteDirectoryVocabulary.STR_CONFIG_CLASS)
public class DeleteDirectory_V1 {

    @EntityDescription.Property(uri = DeleteDirectoryVocabulary.STR_DIRECTORY)
    private String directory = "/tmp/PUT_DIRECTORY_PATH_HERE";

    public DeleteDirectory_V1() {

    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
