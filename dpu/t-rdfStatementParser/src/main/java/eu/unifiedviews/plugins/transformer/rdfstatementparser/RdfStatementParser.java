package eu.unifiedviews.plugins.transformer.rdfstatementparser;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.SelectQuery;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class RdfStatementParser extends DpuAdvancedBase<RdfStatementParserConfig_V1>
        implements SelectQuery.BindingIterator {

    /**
     * Used to log existing action pair (key and value) to prevent cycles in
     * action processing.
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
                NameValuePair left = (NameValuePair) obj;
                return name.equals(left.name)
                        && value.equals(left.value);
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

    private static final Logger LOG = LoggerFactory.getLogger(
            RdfStatementParser.class);

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInData;

    private SimpleRdfRead inData;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutData;

    private SimpleRdfWrite outData;

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
        super(RdfStatementParserConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void innerInit() throws DataUnitException {
        super.innerInit();

        inData = SimpleRdfFactory.create(rdfInData, context);

        outData = SimpleRdfFactory.create(rdfOutData, context);
        outData.setPolicy(AddPolicy.BUFFERED);

        valueFactory = outData.getValueFactory();
    }

    @Override
    protected void innerExecute() throws DPUException {
        regExpCache.clear();
        // get input and iterate over it
        try {
            SelectQuery.iterate(inData, config.getSelectQuery(), this, context);
        } catch (OperationFailedException | QueryEvaluationException ex) {
            throw new DPUException("Failed to iterate over input.", ex);
        }
        // flush data
        try {
            outData.flushBuffer();
        } catch (OperationFailedException ex) {
            throw new DPUException("Failed to flush data from buffer.", ex);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new RdfStatementParserVaadinDialog();
    }

    /**
     * Process given binding - result tuple.
     *
     * @param binding
     * @throws DPUException
     */
    @Override
    public void processStatement(BindingSet binding) throws DPUException {
        // check for subject binding
        if (!binding.getBindingNames().contains(SUBJECT_BINDING)) {
            throw new DPUException("'" + SUBJECT_BINDING
                    + "' binding is not provided.");
        }
        final URI subject = (URI) binding.getBinding(SUBJECT_BINDING).getValue();
        for (String name : binding.getBindingNames()) {
            if (!name.equals(SUBJECT_BINDING)) {
                final Value value = binding.getBinding(name).getValue();
                // parse ie. apply actions from configuration
                try {
                    applyActions(subject,
                            new NameValuePair(name, value.stringValue()));
                } catch (OperationFailedException ex) {
                    throw new DPUException("failed to process statement.", ex);
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
     */
    private void applyActions(URI subject, NameValuePair initialAction)
            throws OperationFailedException {

        // used to determine if we already wisit given state or not
        final Set<NameValuePair> history = new HashSet<>();
        history.add(initialAction);
        // instead of recursion
        final Stack<NameValuePair> stack = new Stack<>();
        stack.push(initialAction);

        while (!stack.isEmpty()) {
            final NameValuePair toProcess = stack.pop();
            for (String key : config.getActions().keySet()) {
                if (key.compareTo(toProcess.name) != 0) {
                    // does not match, skip
                    continue;
                }
                //
                // apply action under given key
                //
                final RdfStatementParserConfig_V1.ActionInfo info
                        = config.getActions().get(key);
                switch (info.getActionType()) {
                    case CreateTriple:
                        createTriple(subject, info.getActionData(),
                                toProcess.value);
                        break;
                    case RegExp:
                        // get object with prepared regular expresion
                        if (!regExpCache.containsKey(info.getActionData())) {
                            regExpCache.put(info.getActionData(),
                                    new RegExpInfo(info.getActionData()));
                        }
                        final RegExpInfo regExp = regExpCache.get(info
                                .getActionData());
                        // apply
                        final Matcher matcher = regExp.pattern.matcher(
                                toProcess.value);
                        while (matcher.find()) {
                            for (String groupName : regExp.groupNames) {
                                final String groupValue = matcher.group(
                                        groupName);
                                if (groupValue != null) {
                                    final NameValuePair newPair = new NameValuePair(
                                            groupName, groupValue);
                                    if (history.contains(newPair)) {
                                        // already contains .. 
                                    } else {
                                        // add to history and on the stact
                                        history.add(newPair);
                                        stack.push(newPair);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        LOG.warn("Unknown action type: {}", info
                                .getActionType());
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
     * @param object
     * @throws OperationFailedException
     */
    private void createTriple(URI subject, String predicateStr, String object)
            throws OperationFailedException {
        final URI predicate = valueFactory.createURI(predicateStr);
        outData.add(subject, predicate, valueFactory.createLiteral(object));
    }

}
