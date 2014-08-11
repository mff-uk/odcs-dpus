package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 *
 * @author Škoda Petr
 */
public class Config_V2 implements VersionedConfig<Config_V3>  {

    private String value = "10";

    public Config_V2() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Config_V3 toNextVersion() {
        Config_V3 conf = new Config_V3();
        conf.setStr1(value);
        conf.setStr2("<a>" + value + "</a>");
        return conf;
    }

}
