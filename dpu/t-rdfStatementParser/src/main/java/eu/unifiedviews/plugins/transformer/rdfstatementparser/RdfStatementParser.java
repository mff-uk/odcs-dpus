package eu.unifiedviews.plugins.transformer.rdfstatementparser;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.SimpleRdf;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class RdfStatementParser extends AbstractDpu<RdfStatementParserConfig_V2> {

    /**
     * Used to log existing action pair (key and value) to prevent cycles in
     * action processing. There can be multiple actions under same name but with different value.
     */
    private class NameValuePair {

        private final String name;

        private final String value;

        public NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NameValuePair) {
                NameValuePair right = (NameValuePair) obj;
                return name.equals(right.name)
                        && value.equals(right.value);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(this.name);
            hash = 83 * hash + Objects.hashCode(this.value);
            return hash;
        }

    }

    /**
     * Cache data about compiled regular expression.
     */
    private class RegExpInfo {

        private final Pattern pattern;

        private final List<String> groupNames = new ArrayList<>(3);

        public RegExpInfo(String regExp) {
            this.pattern = Pattern.compile(regExp);
            final Matcher groups = groupNamesPattern.matcher(regExp);
            while (groups.find()) {
                groupNames.add(groups.group(1));
            }
        }
    }

    public static final String SUBJECT_BINDING = "subject";

    private static final Logger LOG = LoggerFactory.getLogger(RdfStatementParser.class);

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInData;

    @ExtensionInitializer.Init(param = "rdfInData")
    public SimpleRdf inData;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutData;

    @ExtensionInitializer.Init(param = "rdfOutData")
    public WritableSimpleRdf outData;
 
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    private ValueFactory valueFactory;

    /**
     * Regular expression used to get groups from other regular expressions.
     */
    private final Pattern groupNamesPattern
            = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    /**
     * Cache for precompiled regular expressions.
     */
    private final Map<String, RegExpInfo> regExpCache = new HashMap<>();

    public RdfStatementParser() {
        super(RdfStatementParserVaadinDialog.class, 
                ConfigHistory.history(RdfStatementParserConfig_V1.class).addCurrent(RdfStatementParserConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        valueFactory = outData.getValueFactory();
        regExpCache.clear();
        // Set output.
        final String symbolicName = DataUnitUtils.generateSymbolicName(RdfStatementParser.class);
        final RDFDataUnit.Entry output = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(rdfOutData, symbolicName);
            }
        });
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                outData.setOutput(output);
            }
        });
        // Go for per-graph mode.
        final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance, rdfInData,
                RDFDataUnit.Entry.class);
        int counter = 0;
        for (RDFDataUnit.Entry entry : entries) {
            LOG.info("Processing {}/{}", ++counter, entries.size());
            process(Arrays.asList(entry));
        }
    }

    /**
     * Process given graphs.
     *
     * @param entries
     * @throws DPUException
     */
    public void process(final List<RDFDataUnit.Entry> entries) throws DPUException {
        final SparqlUtils.QueryResultCollector colector = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(rdfInData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                final SparqlUtils.SparqlSelectObject select = 
                        SparqlUtils.createSelect(config.getSelectQuery(), entries);
                SparqlUtils.execute(connection, ctx, select, colector);
            }
        });
        // For each row of result (tuple).
        for (Map<String, Value> row : colector.getResults()) {
            // Get subject of result - ie. triple we will add statements to.
            final URI subject = (URI) row.get(SUBJECT_BINDING);
            // For each value in result tuple.
            for (String name : row.keySet()) {
                if (!name.equals(SUBJECT_BINDING)) {
                    final Value value = row.get(name);
                    // Get label if set - this allow us to transfer langugae tag.
                    String label = null;
                    if (config.isTransferLabels() && value instanceof Literal) {
                        final Literal literal = (Literal)value;
                        // try to get language or data type
                        label = literal.getLanguage();
                        if (label == null) {
                            label = literal.getDatatype().stringValue();
                        }
                    }
                    // Parse ie. apply actions from configuration.
                    applyActions(subject, new NameValuePair(name, value.stringValue()), label);
                }
            }
        }
    }

    /**
     * Apply actions on given value.
     *
     * @param subject
     * @param name
     * @param value
     * @param label Label for newly created triple, null if no label should be used.
     */
    private void applyActions(URI subject, NameValuePair initialAction, String label) throws DPUException {

        LOG.debug("applyActions('{}', '{}')", subject.stringValue(), initialAction.value);

        // Used to determine if we already wisit given state or not,
        final Set<NameValuePair> history = new HashSet<>();
        history.add(initialAction);
        // Instead of recursion we use stack.
        final Stack<NameValuePair> stack = new Stack<>();
        stack.push(initialAction);

        while (!stack.isEmpty()) {
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }

            final NameValuePair toProcess = stack.pop();
            LOG.debug("toProcess = name:'{}', value: '{}'", toProcess.name, toProcess.value);
            for (RdfStatementParserConfig_V2.ActionInfo actionInfo : config.getActions()) {
                // Check also here.
                if (ctx.canceled()) {
                    throw ContextUtils.dpuExceptionCancelled(ctx);
                }
                if (actionInfo.getName().compareTo(toProcess.name) != 0) {
                    // Does not match, skip.
                    continue;
                }
                // Apply action under given key.
                switch (actionInfo.getActionType()) {
                    case CreateTriple:
                        LOG.trace("\tcreateTriple('{}', '{}', '{}')", subject.stringValue(), 
                                actionInfo.getActionData(), toProcess.value);
                        createTriple(subject, actionInfo.getActionData(),
                                toProcess.value, label);
                        break;
                    case RegExp:
                        // Get object with prepared regular expresion.
                        if (!regExpCache.containsKey(actionInfo.getActionData())) {
                            regExpCache.put(actionInfo.getActionData(),
                                    new RegExpInfo(actionInfo.getActionData()));
                        }
                        LOG.trace("\tRegExp : '{}'", actionInfo.getActionData());
                        final RegExpInfo regExp = regExpCache.get(actionInfo.getActionData());
                        // Apply.
                        final Matcher matcher = regExp.pattern.matcher(toProcess.value);
                        while (matcher.find()) {
                            for (String groupName : regExp.groupNames) {
                                final String groupValue = matcher.group(
                                        groupName);
                                if (groupValue != null) {
                                    final NameValuePair newPair = new NameValuePair(
                                            groupName, groupValue);
                                    if (history.contains(newPair)) {
                                        // Already contains ..
                                    } else {
                                        // Add to history and on the stack.
                                        history.add(newPair);
                                        stack.push(newPair);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        LOG.warn("Unknown action type: {}", actionInfo.getActionType());
                        break;
                }
            }
        }
    }

    /**
     * Create triple and add it to output.
     *
     * @param subject
     * @param predicateStr
     * @param objectStr
     * @param label Null if no label should be used.
     * @throws OperationFailedException
     */
    private void createTriple(URI subject, String predicateStr, String objectStr, String label)
            throws DPUException {
        final URI predicate = valueFactory.createURI(predicateStr);
        final Literal object;
        if (label == null) {
            object = valueFactory.createLiteral(objectStr);
        } else {
            object = valueFactory.createLiteral(objectStr, label);
        }
        outData.add(subject, predicate, object);
    }

}
