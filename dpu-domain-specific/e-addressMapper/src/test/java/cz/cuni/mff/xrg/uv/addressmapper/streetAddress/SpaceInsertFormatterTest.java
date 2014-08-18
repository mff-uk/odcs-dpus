package cz.cuni.mff.xrg.uv.addressmapper.streetAddress;

import cz.cuni.mff.xrg.uv.addressmapper.streetAddress.SpaceInsertFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class SpaceInsertFormatterTest {
    
    private final SpaceInsertFormatter formatter = new SpaceInsertFormatter();
    
    @Test
    public void addBeforeNumber() {        
        Assert.assertEquals("Masarykovo nám. 114", 
                formatter.format("Masarykovo nám.114"));
    }

    @Test
    public void dontAdd() {        
        Assert.assertEquals("Masarykovo nám. 114", 
                formatter.format("Masarykovo nám. 114"));
    }
    
    
}
