package cz.cuni.mff.xrg.uv.serialization.xml;

import java.io.Serializable;

/**
 *
 * @author Škoda Petr
 */
public class ConfigObject implements Serializable {
    
    private static final String PRIVATE_FINAL = "value";
    
    public static final String PUBLIC_FINAL = "value";

    private int integralValue = 2;

    public ConfigObject() {
    }

    public int getIntegralValue() {
        return integralValue;
    }

    public void setIntegralValue(int integralValue) {
        this.integralValue = integralValue;
    }
    
}
