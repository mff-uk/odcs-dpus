package cz.cuni.mff.xrg.uv.extractor.localrdf;

/**
 * DPU's configuration class.
 */
public class LocalRdfConfig_V1 {

    private String execution = "";

    private String dpu = "";

    private String dataUnit = "1";

    public LocalRdfConfig_V1() {

    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    public String getDpu() {
        return dpu;
    }

    public void setDpu(String dpu) {
        this.dpu = dpu;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String dataUnit) {
        this.dataUnit = dataUnit;
    }

}
