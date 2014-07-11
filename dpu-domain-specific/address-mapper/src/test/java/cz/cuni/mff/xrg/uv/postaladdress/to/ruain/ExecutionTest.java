package cz.cuni.mff.xrg.uv.postaladdress.to.ruain;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.test.boost.rdf.InputOutput;
import java.io.File;
import java.util.logging.Level;
import org.junit.Test;
import org.openrdf.query.QueryEvaluationException;
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

    @Test
    public void test() throws OperationFailedException, QueryEvaluationException, ExternalFailure, RDFException, ConfigException {
        configLogger();
        parse("d:/Temp/01/input-test.ttl");
    }

    private void configLogger() {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory
                .getILoggerFactory();
        
        // remove all loggers
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

    private void parse(String fileName) throws OperationFailedException, QueryEvaluationException, ExternalFailure, RDFException, ConfigException {
        LOG.info(">>>>> parse({})", fileName.substring(fileName.lastIndexOf("/")));
        
        TestEnvironment env = new TestEnvironment();

        // create data units
        WritableRDFDataUnit inUlice = env.createRdfInput("seznamUlic", false);
        WritableRDFDataUnit inMestaUlice = env.createRdfInput("seznamMestObci", false);
        WritableRDFDataUnit inKraj = env.createRdfInput("seznamKraju", false);        
        WritableRDFDataUnit address = env.createRdfInput("postalAddress", false);        
        WritableRDFDataUnit output = env.createRdfOutput("mapping", false);
        WritableRDFDataUnit log = env.createRdfOutput("log", false);
        // load data
        try {
            // aditional data
            InputOutput.extractFromFile(new File("d:/Temp/02/ulice.ttl"),
                    RDFFormat.TURTLE, inUlice);
            InputOutput.extractFromFile(new File("d:/Temp/02/obce.ttl"),
                    RDFFormat.TURTLE, inMestaUlice);
            InputOutput.extractFromFile(new File("d:/Temp/02/vusc.ttl"),
                    RDFFormat.TURTLE, inKraj);
            // test based data
            InputOutput.extractFromFile(new File(fileName),
                    RDFFormat.TURTLE, address);
        } catch (Exception ex) {
            env.release();
            LOG.error("Faield to load input data.", ex);
            return;
        }
        // execute
        Main main = new Main();
        Configuration config = new Configuration();
        config.onDeserialize();
        main.configureDirectly(config);
        
        try {
            env.run(main);
            // store results
            InputOutput.loadToFile(output, new File("d:/Temp/01/out-mapping.ttl"), 
                    RDFFormat.TURTLE);
            InputOutput.loadToFile(log, new File("d:/Temp/01/out-log.ttl"), 
                    RDFFormat.TURTLE);
        } catch (Exception ex) {
            LOG.error("DPU failed", ex);
        } finally {
            env.release();
        }
        
    }

}