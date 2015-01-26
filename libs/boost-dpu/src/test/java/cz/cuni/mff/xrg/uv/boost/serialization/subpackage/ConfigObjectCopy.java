package cz.cuni.mff.xrg.uv.boost.serialization.subpackage;

import java.io.Serializable;

/**
 *
 * @author Škoda Petr
 */
public class ConfigObjectCopy implements Serializable {
    
    private int integralValue = 0;

    public ConfigObjectCopy() {
    }

    public int getIntegralValue() {
        return integralValue;
    }

    public void setIntegralValue(int integralValue) {
        this.integralValue = integralValue;
    }
    
}
