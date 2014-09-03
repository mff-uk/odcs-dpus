##Introduction
Repository for DPUs (Data processing units) for UnifiedViews ETL tool (https://github.com/UnifiedViews/Core).

##For Users

###DPU List

####Domain specific
Prepared for 1.3.0

|Name      |Type       |Location   |Description |
|----------|-----------|-----------|------------|
|e-sukl    |Extractor  |dpu-domain-specific/e-sukl||
|e-isvav   |Extractor  |dpu-domain-specific/e-isvav||

#####General
|Name      |Type       |Location   |Description |
|----------|-----------|-----------|------------|
|t-tabular |Transformer|dpu/t-tabular||
|l-solr    |Loader     |dpu/l-solr   ||
|t-rdfStatementParser|Transformer|dpu/t-rdfStatementParser||

## How to create a new DPU - quick start
This manual assumes that you have already downloaded and installed libraries from https://github.com/UnifiedViews.
* download this repository
* execute tools/init.sh
* create new DPU form maven archeotype dpu-template-advanced
* if the target instance does not contain libs as osgi-bundles you must update dependency scope from provided to compile

##For developers (Please READ before contributing)
Purpose of specific directories.

###Libs
The Libs directory contains useful project that can be used by DPUs:
* boost-dpu
* service-external - provides classes for manipulation with external services (query http sparql).
* service-serializaiton-xml - provides service for xml serialization
* utils-dataunit
* utils-dataunit-rdf - wrap for RDFDataUnit, that makes work with rdf easier
* utils-test - load/store methods for RDFDataUnit.

###Dependencies
Contains DPU's dependencies (jar libraries - osgi bundles). If DPU needs some osgi-library then the library should be located here and there should be lib/libs.txt file (line oriented) that contains list of required dependencies in the DPU folder, e.g., as here: https://github.com/mff-uk/DPUs/tree/master/dpu/loader-scp.

###New DPUs
There are three directories, where the DPUs are stored:
* dpu-deprecated - this DPUs should not be used and should be replaced by other DPUs as they are no longer maintained.
* dpu-domain-specific - domain specific DPUs, mostly extractors. 
* dpu - general DPUs. If DPU is located here then is should have potential to be used in multiple pipelines across different domains.
