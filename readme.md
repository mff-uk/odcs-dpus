##Introduction
Repository for DPUs (Data processing units) for ETL tool (https://github.com/UnifiedViews/Core) for RDF data.

##For contributors

###Libs
The Libs directory contains useful project that can be used by DPUs:
* Simple RDF - made work with RDF easier.

###Dependencies
Contains DPU's dependencies (jar libraries - osgi bundles). If DPU need some osgi-library then the library should be located here and there shold be lib/libs.txt file (line oriented) that contains list of required dependencies.

###DPU template
Two templates are located here. They must be installed in maven repository before use. Once presented in maven repository, they can be used as a templates (create project from archeotype).
Templates are for:
* DPU with configuration - contains configuration class and class for configuration dialog
* nonconfigurable DPU - contains just main DPU class. This template is for DPU's that do not need configuration.

###Naming convention
DPUs are located in directory extractors/transformers/loaders based on respective DPU's type. 
* pom.xml artefact-id is in lowercase and used - instead of space. It also starts with extractor/transformer/loader
* pom.xml name reflect artifact-if where - are replaced by spaces. The extractor/transformer/loader begin is replaced by ext. /trans. /load. string.
