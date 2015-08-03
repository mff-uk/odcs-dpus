package cz.cuni.mff.xrg.uv.transformer;

/**
 * Configuration class for ModifyDate.
 *
 * @author Petr Škoda
 */
public class ModifyDateConfig_V1 {

    private String inputPredicate = "http://localhost/temp/ontology/date";
    
    private int modifyDay = 1;
    
    private String outputPredicate = "http://localhost/temp/ontology/date";

    public ModifyDateConfig_V1() {

    }

    public String getInputPredicate() {
        return inputPredicate;
    }

    public void setInputPredicate(String inputPredicate) {
        this.inputPredicate = inputPredicate;
    }

    public int getModifyDay() {
        return modifyDay;
    }

    public void setModifyDay(int modifyDay) {
        this.modifyDay = modifyDay;
    }

    public String getOutputPredicate() {
        return outputPredicate;
    }

    public void setOutputPredicate(String outputPredicate) {
        this.outputPredicate = outputPredicate;
    }

}
