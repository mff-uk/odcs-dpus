package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

/**
 * Setup {@link DpuAdvancedBase} for testing.
 *
 * @author Škoda Petr
 */
public class DpuAdvancedBaseTest {

    private DpuAdvancedBaseTest() {
        
    }

    public static <CONFIG> void setDpuConfiguration(DpuAdvancedBase<CONFIG> dpu, CONFIG config) {
        dpu.setTestMode();
        dpu.masterContext.config = config;
    }

}
