package cz.opendata.linked.lodcloud.loader.test;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.opendata.linked.lodcloud.loader.Loader;
import cz.opendata.linked.lodcloud.loader.LoaderConfig;
import cz.opendata.linked.lodcloud.loader.LoaderConfig.*;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.helpers.dpu.test.rdf.InputOutputUtils;
import eu.unifiedviews.helpers.dpu.test.resources.ResourceUtils;

public class LoadTest {

    @Test
    public void constructAllTest() throws Exception {
        // prepare dpu instance and configure it
        Loader loader = new Loader();
        LoaderConfig config = new LoaderConfig();
        
        config.setDatasetID("cz-test2");
        config.setApiKey("dcdc0663-864b-4233-b99a-f84c55655307");
        config.setSchemaUrl("http://ruian.linked.opendata.cz/dump/CUZK-2-RUIAN-CODELISTS.zip");
        config.setNamespace("http://linked.opendata.cz/resource/business-entity/");
        config.getVocabularies().add("skos");
        config.getVocabularies().add("gr");
        config.getVocabularies().add("adms");
        config.getLinks().add(new LinkCount("cz-ruian", new Long(20000)));
        config.setVocabTag(VocabTags.NoProprietaryVocab);
        config.setVocabMappingTag(VocabMappingsTags.NoVocabMappings);
        config.setPublishedTag(PublishedTags.PublishedByThirdParty);
        config.setProvenanceMetadataTag(ProvenanceMetadataTags.NoProvenanceMetadata);
        config.setLicenseMetadataTag(LicenseMetadataTags.LicenseMetadata);

        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        
        // prepare test environment, we use system tmp directory
        TestEnvironment env = new TestEnvironment();
        // prepare input and output data units

        WritableRDFDataUnit input = env.createRdfInput("metadata", false);

        // here we can simply pre-fill input data unit with content from 
        // resource file
        
        InputOutputUtils.extractFromFile(ResourceUtils.getFile("cz-ic-void.ttl"), RDFFormat.TURTLE, input,
                RdfDataUnitUtils.addGraph(input, "input"));        
        WritableRDFDataUnit metadata = env.createRdfInput("metadata", false);
        
        try {
            // run the execution
            env.run(loader);

            // verify result
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // release resources
            env.release();
        }
    }
}
