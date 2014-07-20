package cz.cuni.mff.xrg.uv.external;

/**
 *
 * @author Škoda Petr
 */
public class FaultTolerantPolicy {

    private final int numberOfRetries;
    
    private final int timeDelay;

    FaultTolerantPolicy(int timeDelay) {
        this.numberOfRetries = -1;
        this.timeDelay = timeDelay;
    }
    
    FaultTolerantPolicy(int timeDelay, int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
        this.timeDelay = timeDelay;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public int getTimeDelay() {
        return timeDelay;
    }
        
}
