/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

/**
 *
 * @author Škoda Petr
 */
public class ParserDbfConfig {

    final String encoding;

    final Integer rowLimit;

    final boolean hasHeader;

    public ParserDbfConfig(String encoding, Integer rowLimit, boolean hasHeader) {
        this.encoding = encoding;
        this.rowLimit = rowLimit;
        this.hasHeader = hasHeader;
    }

}
