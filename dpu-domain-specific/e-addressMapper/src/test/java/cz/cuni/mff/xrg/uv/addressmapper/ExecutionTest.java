package cz.cuni.mff.xrg.uv.addressmapper;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.AddressLocalityMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.AddressRegionMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.PostalCodeMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.StreetAddressMapper;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.service.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.test.boost.dpu.DPUContextMockBase;
import cz.cuni.mff.xrg.uv.test.boost.rdf.InputOutput;
import cz.cuni.mff.xrg.uv.test.boost.rdf.RDFDataUnitFactory;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.junit.Test;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class ExecutionTest {

    private static final Logger LOG = LoggerFactory.getLogger(
            ExecutionTest.class);

    //@Test
    public void test() throws OperationFailedException, QueryEvaluationException, ExternalFailure, 
            ConfigException, RepositoryException {
        configLogger();
        parse("d:/Temp/01/input-test.ttl");
    }

    private void configLogger() {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        //loggerContext.reset();

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%date %level %logger{15} %msg%n");
        ple.setContext(loggerContext);
        ple.start();

        // prepare appender
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile("d:/Temp/address-mapper.log");
        fileAppender.setEncoder(ple);
        fileAppender.setContext(loggerContext);

        // add filter
        ThresholdFilter levelFilter = new ThresholdFilter();
        levelFilter.setLevel(Level.INFO.toString());
        levelFilter.start();
        fileAppender.addFilter(levelFilter);

        // start
        fileAppender.start();

        // add to root
        ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(
                Logger.ROOT_LOGGER_NAME);
        logbackLogger.addAppender(fileAppender);
    }

    private void parse(String fileName) throws OperationFailedException, QueryEvaluationException, 
            ExternalFailure, ConfigException, RepositoryException {
        LOG.info(">>>>> parse({})", fileName.substring(fileName.lastIndexOf("/")));

        // createInMemory data units
        WritableRDFDataUnit inUlice = RDFDataUnitFactory.createInMemory();
        WritableRDFDataUnit inObce = RDFDataUnitFactory.createInMemory();
        WritableRDFDataUnit inCastiObci = RDFDataUnitFactory.createInMemory();
        WritableRDFDataUnit inKraj = RDFDataUnitFactory.createInMemory();
        WritableRDFDataUnit address = RDFDataUnitFactory.createInMemory();
        WritableRDFDataUnit output = RDFDataUnitFactory.createInMemory();
        WritableRDFDataUnit log = RDFDataUnitFactory.createInMemory();
        // load data
        try {
            // aditional data
            InputOutput.extractFromFile(new File("d:/Temp/02/ulice.ttl"),
                    RDFFormat.TURTLE, inUlice);
            InputOutput.extractFromFile(new File("d:/Temp/02/obce.ttl"),
                    RDFFormat.TURTLE, inObce);
            InputOutput.extractFromFile(new File("d:/Temp/02/castiObci.ttl"),
                    RDFFormat.TURTLE, inCastiObci);
            InputOutput.extractFromFile(new File("d:/Temp/02/vusc.ttl"),
                    RDFFormat.TURTLE, inKraj);
            // test based data
            InputOutput.extractFromFile(new File(fileName),
                    RDFFormat.TURTLE, address);
        } catch (Exception ex) {
            LOG.error("Faield to load input data.", ex);
            return;
        }
        // execute
        
        AddressMapperConfig_V1 config = new AddressMapperConfig_V1();

        HashMap<String, List<String>> mapperConfig = new HashMap<>();
        mapperConfig.put(AddressRegionMapper.NAME,
                Arrays.asList("http://schema.org/addressRegion"));
        mapperConfig.put(PostalCodeMapper.NAME,
                Arrays.asList("http://schema.org/postalCode"));
        mapperConfig.put(StreetAddressMapper.NAME,
                Arrays.asList("http://schema.org/streetAddress"));
        mapperConfig.put(AddressLocalityMapper.NAME,
                Arrays.asList("http://schema.org/addressLocality"));
        config.setMapperConfig(mapperConfig);

        AddressMapper main = new AddressMapper() {

            @Override
            protected void innerExecute() throws DPUException, DataUnitException {
                // set configuration
                this.config = config;
                // execute
                super.innerExecute();
            }

        };

        // we are in test mode
        main.setTestMode();
        
        // bind data units
        main.inRdfUlice = inUlice;
        main.inRdfCastiObci = inCastiObci;
        main.inRdfKraje = inKraj;
        main.inRdfObce = inObce;
        main.inRdfUlice = inUlice;
        main.outRdfLog = log;
        main.outRdfMapping = output;

        try {
            main.execute(new DPUContextMockBase());
            // store results
            InputOutput.loadToFile(output, new File("d:/Temp/01/out-mapping.ttl"), 
                    RDFFormat.TURTLE);
            InputOutput.loadToFile(log, new File("d:/Temp/01/out-log.ttl"), 
                    RDFFormat.TURTLE);
        } catch (Exception ex) {
            LOG.error("DPU failed", ex);
        } finally {

        }
        
    }

}