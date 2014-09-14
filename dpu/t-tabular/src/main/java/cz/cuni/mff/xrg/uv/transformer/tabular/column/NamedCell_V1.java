package cz.cuni.mff.xrg.uv.transformer.tabular.column;

/**
 * Used to specify cell in sheet for xls format.
 *
 * @author Škoda Petr
 */
public class NamedCell_V1 {

    private String name = "A0";

    private Integer rowNumber = 0;
    
    private Integer columnNumber = 0;

    public NamedCell_V1() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

}
