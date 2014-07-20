package cz.cuni.mff.xrg.uv.serialization.xml;

import java.io.Serializable;

/**
 *
 * @author Škoda Petr
 */
public class ConfigObject implements Serializable {
    
    private int value = 2;

    public ConfigObject() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
    
}
