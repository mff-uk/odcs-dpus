package cz.cuni.mff.xrg.uv.transformer.sequencegenerator;

/**
 * DPU's configuration class.
 */
public class SequenceGeneratorConfig_V1 {

    /**
     * Store the first number of sequence.
     */
    private String predicateStart = "http://localhost/start";

    /**
     * Store the last number in sequence included.
     */
    private String predicateEnd = "http://localhost/end";

    /**
     * Output predicate, under which store the data.
     */
    private String predicateOutput = "http://localhost/sequence";

    public SequenceGeneratorConfig_V1() {

    }

    public String getPredicateStart() {
        return predicateStart;
    }

    public void setPredicateStart(String predicateStart) {
        this.predicateStart = predicateStart;
    }

    public String getPredicateEnd() {
        return predicateEnd;
    }

    public void setPredicateEnd(String predicateEnd) {
        this.predicateEnd = predicateEnd;
    }

    public String getPredicateOutput() {
        return predicateOutput;
    }

    public void setPredicateOutput(String predicateOutput) {
        this.predicateOutput = predicateOutput;
    }

}
