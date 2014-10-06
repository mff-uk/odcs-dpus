package cz.cuni.mff.xrg.uv.filesiteratorfail;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CloseCloseable;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsTransformer
public class FilesIteratorFail extends DpuAdvancedBase<FilesIteratorFailConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(FilesIteratorFail.class);
	
    @DataUnit.AsInput(name = "files")
    public FilesDataUnit inFilesTable;

    @DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTables;

    @SimpleRdfConfigurator.Configure(dataUnitFieldName = "outRdfTables")
    public SimpleRdfWrite rdfTableWrap;

	public FilesIteratorFail() {
        super(FilesIteratorFailConfig_V1.class,
                AddonInitializer.create(new CloseCloseable(), new SimpleRdfConfigurator(FilesIteratorFail.class)));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        int counter;

        counter = 0;
        try {
            try (FilesDataUnit.Iteration iterationTest = new WritableFileIteration(inFilesTable)) { // inFilesTable.getIteration()) {
                while (iterationTest.hasNext()) {
                    final FilesDataUnit.Entry entry = iterationTest.next();
                    counter++;
                }
            }
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        LOG.info("files count: {}", counter);

        Set<String> symbolicNames = new HashSet<>();

        counter = 0;
        try {
            try (FilesDataUnit.Iteration iterationTest = new WritableFileIteration(inFilesTable)) { // inFilesTable.getIteration()) {
                while (iterationTest.hasNext()) {
                    final FilesDataUnit.Entry entry = iterationTest.next();
                    final String symbolicName = entry.getSymbolicName();
                    // add graph
                    LOG.info("add: {}", symbolicName);
                    
                    // ACTION !!!
                    //outRdfTables.addNewDataGraph(symbolicName);
                    // if  + ".ttl" is added then it works fine
                    addNewDataGraph(symbolicName);

                    counter++;
                    if (symbolicNames.contains(symbolicName)) {
                        LOG.error("duplicit: {}", symbolicName);
                    } else {
                        symbolicNames.add(symbolicName);
                    }
                }
            }
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        LOG.info("files count: {}", counter);

        counter = 0;
        try {
            try (FilesDataUnit.Iteration iterationTest = new WritableFileIteration(inFilesTable)) { // inFilesTable.getIteration()) {
                while (iterationTest.hasNext()) {
                    final FilesDataUnit.Entry entry = iterationTest.next();
                    counter++;
                }
            }
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        LOG.info("files count: {}", counter);
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new FilesIteratorFailVaadinDialog();
    }

    private Integer atomicInteger = 0;

    private String baseDataGraphURI = "http://localhost/temp";

    public URI addNewDataGraph(String symbolicName) throws DataUnitException {
        URI generatedURI = new URIImpl(baseDataGraphURI + "/" + String.valueOf(atomicInteger++));
        this.addExistingDataGraph(symbolicName, generatedURI);
        return generatedURI;
    }

    public void addExistingDataGraph(String symbolicName, URI existingDataGraphURI) throws DataUnitException {
//        if (!ownerThread.equals(Thread.currentThread())) {
//            LOG.info("More than more thread is accessing this data unit");
//        }

        RepositoryConnection connection = null;
        try {
            // TODO michal.klempa think of not connecting everytime
            connection = outRdfTables.getConnection(); //getConnectionInternal();
            connection.begin();
            ValueFactory valueFactory = connection.getValueFactory();
            BNode blankNodeId = valueFactory.createBNode();
            Statement statement = valueFactory.createStatement(
                    blankNodeId,
                    valueFactory.createURI(MetadataDataUnit.PREDICATE_SYMBOLIC_NAME),
                    valueFactory.createLiteral(symbolicName)
                    );
//            Statement statement2 = valueFactory.createStatement(
//                    blankNodeId,
//                    valueFactory.createURI(RDFDataUnit.PREDICATE_DATAGRAPH_URI),
//                    existingDataGraphURI
//                    );
            LOG.info("write context graph: {}", outRdfTables.getMetadataWriteGraphname());
            connection.add(statement, outRdfTables.getMetadataWriteGraphname());
//            connection.add(statement2, outRdfTables.getMetadataWriteGraphname());
            connection.commit();
        } catch (RepositoryException ex) {
            throw new DataUnitException("Error when adding data graph.", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error when closing connection", ex);
                    // eat close exception, we cannot do anything clever here
                }
            }
        }
    }

}
