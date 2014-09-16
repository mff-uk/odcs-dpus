package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

/**
 *
 * @author Škoda Petr
 */
public class DpuAdvancedBaseTest {

    public static <CONFIG> void setDpuConfiguration(DpuAdvancedBase<CONFIG> dpu,CONFIG config) {
        dpu.setTestMode();
        dpu.masterContext.config = config;
    }

}
