package cz.cuni.mff.xrg.uv.configuration.dialog;

import com.vaadin.ui.CustomComponent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Škoda Petr
 */
public abstract class AbstractConfigDialog extends CustomComponent {
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface VaadinDialog {
        
    }    
    
    /**
     * Set context to the dialog. This method is called only once
     * before any other method.
     * 
     * @param newContext
     */
    public abstract void setContext(ConfigDialogContext newContext);

    /**
     * Configure dialog with given serialized configuration.
     * 
     * @param conf Serialized configuration object.
     * @throws ConfigException
     */
    public abstract void setConfig(String conf) throws ConfigException;

    /**
     * Return current configuration from dialog in serialized form. If the
     * configuration is invalid then throws.
     * 
     * @return Serialized configuration object.
     * @throws ConfigException
     */
    public abstract String getConfig() throws ConfigException;    
 
    /**
     * Return configuration summary that can be used as DPU description. The
     * summary should be short and as much informative as possible. Return null
     * in case of invalid configuration or it this functionality is not
     * supported. The returned string should be reasonably short.
     * 
     * @return Can be null.
     */
    @Override
    public abstract String getDescription();    
    
    /**
     * Compare last configuration and current dialog's configuration. If any
     * exception is thrown then return false.
     * The last configuration is updated in calls {@link #getConfig()} and {@link #setConfig(java.lang.String) }.
     * 
     * @return False if configurations are valid and are different.
     */
    public abstract boolean hasConfigChanged();    
    
}
