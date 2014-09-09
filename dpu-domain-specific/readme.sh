* run update scripts by
	./update-dpu.sh {path to dpu root directory, ie. pom.xml}
* fix pom.xml if needed
* if package org.apache.commons.io.IOUtils is used then add depedency
<dependency>
	<groupId>commons-io</groupId>
	<artifactId>commons-io</artifactId>
	<version>2.4</version>
	<scope>provided</scope>
</dependency>
* simple rdf config
	AddonInitializer.create(new SimpleRdfConfigurator(Tabular.class))
	- simple rdf must be public and with anotation, example:
	
	@DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTables;

    @SimpleRdfConfigurator.Configure(dataUnitFieldName = "outRdfTables")
    public SimpleRdfWrite rdfTableWrap;	
	
	