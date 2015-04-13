package cz.cuni.mff.xrg.uv.transformer.filedecoder;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for FileDecoder.
 *
 * @author Petr Škoda
 */
public class FileDecoderVaadinDialog extends AbstractDialog<FileDecoderConfig_V1> {

    public FileDecoderVaadinDialog() {
        super(FileDecoder.class);
    }

    @Override
    public void setConfiguration(FileDecoderConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public FileDecoderConfig_V1 getConfiguration() throws DPUConfigException {
        final FileDecoderConfig_V1 c = new FileDecoderConfig_V1();
        return c;
    }

    @Override
    public void buildDialogLayout() {
    }
}
