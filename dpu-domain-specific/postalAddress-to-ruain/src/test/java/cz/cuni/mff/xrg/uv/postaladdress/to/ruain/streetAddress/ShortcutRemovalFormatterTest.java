package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class ShortcutRemovalFormatterTest {
    
    private final ShortcutRemovalFormatter formatter = 
            new ShortcutRemovalFormatter();
    
    @Test
    public void replaceCase0() {
        Assert.assertEquals(("Budovatelů 495"), 
                formatter.format("Budovatelů č.p. 495"));
    }

    @Test
    public void replaceCase1() {
        Assert.assertEquals(("Brožíkova 440"), 
                formatter.format("Brožíkova čp. 440"));
    }
    
    @Test
    public void replaceCase2() {
        Assert.assertEquals(("Dlouhá ulice 80"), 
                formatter.format("Dlouhá ulice č.80"));
    }
    
}

// č. č.p. čp.
// Budovatelů č.p. 495
// Brožíkova čp. 440
// Dlouhá ulice č.80
