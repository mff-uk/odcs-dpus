package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 *
 * @author Škoda Petr
 */
public class Config_V1 implements VersionedConfig<Config_V2> {

    private int value = 2;

    public Config_V1() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public Config_V2 toNextVersion() {
        Config_V2 conf = new Config_V2();
        conf.setValue(Integer.toString(value));
        return conf;
    }
 
}
