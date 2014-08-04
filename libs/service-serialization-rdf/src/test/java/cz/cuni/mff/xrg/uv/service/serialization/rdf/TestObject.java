package cz.cuni.mff.xrg.uv.service.serialization.rdf;

/**
 *
 * @author Škoda Petr
 */
public class TestObject {

    private String text_one;

    private Integer value;

    private String text_two;

    public TestObject() {
    }

    public String getText_one() {
        return text_one;
    }

    public void setText_one(String text_one) {
        this.text_one = text_one;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getText_two() {
        return text_two;
    }

    public void setText_two(String text_two) {
        this.text_two = text_two;
    }

}
