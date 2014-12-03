package cz.opendata.linked.lodcloud.loader.test;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBaseTest;
import cz.cuni.mff.xrg.uv.test.boost.rdf.InputOutput;
import cz.cuni.mff.xrg.uv.test.boost.resources.ResourceAccess;
import cz.opendata.linked.lodcloud.loader.Loader;
import cz.opendata.linked.lodcloud.loader.LoaderConfig;
import cz.opendata.linked.lodcloud.loader.LoaderConfig.LinkCount;
import cz.opendata.linked.lodcloud.loader.LoaderConfig.*;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

public class LoadTest {

    @Test
    public void constructAllTest() throws Exception {
        // prepare dpu instance and configure it
        Loader loader = new Loader();
        LoaderConfig config = new LoaderConfig();
        
        config.setDatasetID("cz-test");
        config.setApiKey("dcdc0663-864b-4233-b99a-f84c55655307");
        config.setSchemaUrl("http://ruian.linked.opendata.cz/dump/CUZK-2-RUIAN-CODELISTS.zip");
        //config.setDatasetDescription("Description");
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

        DpuAdvancedBaseTest.setDpuConfiguration(loader, config);
        
        // prepare test environment, we use system tmp directory
        TestEnvironment env = new TestEnvironment();
        // prepare input and output data units

        WritableRDFDataUnit metadata = env.createRdfInput("metadata", false);
        InputOutput.extractFromFile(ResourceAccess.getFile("cz-ic-void.ttl"), RDFFormat.TURTLE, metadata);

        // here we can simply pre-fill input data unit with content from 
        // resource file
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
