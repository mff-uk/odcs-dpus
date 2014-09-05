if [ $# -eq 0 ]
then
    echo "To show help use './upadte-java-file.sh -h'"
    exit
fi

echo -n "Updating $1 ... "
cat $1 | sed "s/\t/    /g" > "$1.tmp"
# dialog file cz.cuni.mff.xrg.odcs.commons.module.dialog -> replace with our import
cat "$1.tmp" | sed \
		-e "s/import cz\.cuni\.mff\.xrg\.odcs\.commons\.module\.dialog\.BaseConfigDialog;/"\
"import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;\n"\
"import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;/"\
		> "$1.tmp.out"		
cat "$1.tmp.out" > "$1.tmp"
# append for main file
cat "$1.tmp" | sed \
		-e "s/\(import cz\.cuni\.mff\.xrg\.odcs\.commons\.dpu\.DPU;\)/"\
"import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;\n"\
"import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;\n"\
"import eu.unifiedviews.dataunit.DataUnit;\n"\
"import eu.unifiedviews.dataunit.DataUnitException;\n"\
"import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;\n"\
"\1/"\
		-e "s/AbstractConfigDialog<.*>/AbstractConfigDialog<MasterConfigObject>/"\
		> "$1.tmp.out"
cat "$1.tmp.out" > "$1.tmp"		
# class update
cat "$1.tmp" | sed \
		-e "s/extends ConfigurableBase</extends DpuAdvancedBase</"\
		-e "s/implements .*/{/"\
		> "$1.tmp.out"
cat "$1.tmp.out" > "$1.tmp"		
# execute method
cat "$1.tmp" | sed \
		-e "s/public void execute(DPUContext ctx) throws DPUException/protected void innerExecute() throws DPUException/"\
		-e "s/ctx/context/g"\
		> "$1.tmp.out"
cat "$1.tmp.out" > "$1.tmp"		

# main dpu file
cat "$1.tmp" | sed \
		-e "s/cz\.cuni\.mff\.xrg\.odcs\.commons\.dpu\.D/eu.unifiedviews.dpu.D/"\
		-e "/import cz\.cuni\.mff\.xrg\.odcs\.commons\.dpu.annotation/d"\
		-e "/import cz\.cuni\.mff\.xrg\.odcs\.commons\.message\.MessageType/d"\
		-e "s/MessageType/DPUContext\.MessageType/"\
		-e "/import cz\.cuni\.mff\.xrg\.odcs\.commons\.module\./d"\
		-e "s/cz\.cuni\.mff\.xrg\.odcs\.commons\.web\.AbstractConfigDialog/eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog/"\
		-e "/import cz\.cuni\.mff\.xrg\.odcs\.commons\.web./d"\
		-e "s/cz\.cuni\.mff\.xrg\.odcs\.rdf\./eu.unifiedviews.dataunit.rdf./"\
		-e "s/cz\.cuni\.mff\.xrg\.uv\.rdf\.simple\./cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple./"\
		-e "s/@AsExtractor/@DPU.AsExtractor/"\
		-e "s/@AsTransformer/@DPU.AsTransformer/"\
		-e "s/@AsLoader/@DPU.AsLoader/"\
		-e "s/@OutputDataUnit/@DataUnit.AsOutput/"\
		-e "s/@InputDataUnit/@DataUnit.AsInput/"\
		> "$1.tmp.out"
cat "$1.tmp.out" > "$1.tmp"
# configuration file
cat "$1.tmp" | sed \
		-e "s/import cz\.cuni\.mff\.xrg\.odcs\.commons\.module\.config\.DPUConfigObjectBase//"\
		-e "s/extends DPUConfigObjectBase//"\
		> "$1.tmp.out"
cat "$1.tmp.out" > "$1.tmp"
# dialog file
cat "$1.tmp" | sed \
		-e "s/ConfigException/DPUConfigException/"\
		-e "s/cz\.cuni\.mff\.xrg\.odcs\.commons\.configuration/eu.unifiedviews.dpu.config/"\
		-e "s/extends BaseConfigDialog/extends AdvancedVaadinDialogBase/"\
		-e "s/super(\([^.]*\)\.class)/super(\1.class,AddonInitializer.noAddons())/"\
		> $1
rm "$1.tmp"
rm "$1.tmp.out"
echo "done"


