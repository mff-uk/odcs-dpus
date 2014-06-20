##Introduction
Repository for DPUs (Data processing units) for ETL tool (https://github.com/UnifiedViews/Core) for RDF data.

##For Users

###DPU List

####Domain specific
|Name      |Type      |Location   |
|----------|----------|-----------|
|ext. ares updates|Extractor|dpu-domain-specific/ares-update|
|ext. ares|Extractor|dpu-domain-specific/ares|
|ext. buyer profiles cz|Extractor|dpu-domain-specific/buyer-profiles|
|ext. cenia cz irz|Extractor|dpu-domain-specific/cenia-irz|
|ext. ehealth ndf rt|Extractor|dpu-domain-specific/ehealth-ndf-rt|
|ext. geocoder google|Extractor|dpu-domain-specific/geocoder-google|
|ext. geocoder|Extractor|dpu-domain-specific/geocoder|
|ext. gov cz agendy|Extractor|dpu-domain-specific/gov-cz-agendy|
|ext. gov cz organy|Extractor|dpu-domain-specific/gov-cz-organy|
|ext. gov cz smlouvy|Extractor|dpu-domain-specific/gov-cz-smlouvy|
|ext. isvav|Extractor|dpu-domain-specific/isvav|
|ext. krovak|Extractor|dpu-domain-specific/krovak|
|ext. legislation decisions unzipper|Extractor|dpu-domain-specific/legislation-decisions-unzipper|
|trans. legislation decisions jTagger|Transformer|dpu-domain-specific/legislation-tagger|
|trans. legislation decisions uriGenerator|Transformer|dpu-domain-specific/legislation-uri-generator|
|ext. legislation uSoud|Extractor|dpu-domain-specific/legislation-usoud|
|trans. metadataForm|Transformer|dpu-domain-specific/metadata|
|ext. mzcr prices|Extractor|dpu-domain-specific/mzcr-ceny|
|ext. mzp cz ippc|Extractor|dpu-domain-specific/mzp-ippc|
|ext. nominatim|Extractor|dpu-domain-specific/nominatim|
|ext. psp cz metadata|Extractor|dpu-domain-specific/psp-cz|
|ext. rdfa distiller|Extractor|dpu-domain-specific/rdf-distiller|
|ext. ruian|Extractor|dpu-domain-specific/ruian|
|ext. tabular czso vdb|Extractor|dpu-domain-specific/tabular-czso-vdb|
|ext. tabular|Extractor|dpu-domain-specific/tabular|
|ext. unzipper|Extractor|dpu-domain-specific/unzipper|

#General
|Name      |Type      |Location   |
|----------|----------|-----------|
|ext. file downloader|Extractor|dpu/extractor-download-file|
|ext. file local|Extractor|dpu/extractor-local-file|
|load. ftp|Loader|dpu/loader-ftp|
|load. file local|Loader|dpu/loader-local-file|
|load. scp|Loader|Loader|dpu/loader-scp|
|trans. file filter|Transformer|dpu/transformer-file-filter|
|trans. file merger|Transformer|dpu/transformer-file-merger|
|trans. rdf to csv|Transformer|dpu/transformer-rdf-csv|
|trans. rdf merger|Transformer|dpu/transformer-rdf-merger|
|trans. rdf to file|Transformer|dpu/transformer-rdf-to-file|
|trans. unzipper|Transformer|dpu/transformer-unzipper|
|trans. zipper|Transforemr|dpu/transformer-zipper|

####Deprecated
|Name      |Type      |Location   |
|----------|----------|-----------|
|trans. single file picker|Transformer|Yes|dpu-deprecated/file-picker|
|ext. jTagger old|Extractor|Yes|dpu-deprecated/intlib-tagger|
|trans. multiple files picker|Transformer|Yes|dpu-deprecated/multiple-files-picker|
|trans. simple xslt|Transformer|Yes|dpu-deprecated/xslt|

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

###New DPUs
There are three directories, where the DPUs are stored:
* dpu-deprecated - this DPUs should not be used and should be replaced by other DPUs as they are no longer mantained.
* dpu-domain-specific - domain specific DPUs, mostly extractors. 
* dpu - general DPUs. If DPU is located here then is should have potential to be used in multiple pipelines across different domains.

###Naming convention
* pom.xml artefact-id is in lowercase and use '-' instead of space. If author is sure about DPU type, then the name can be prefixed with type (extractor/transformer/loader).
* pom.xml name reflect artifact-if where '-' are replaced by spaces. Possible  extractor/transformer/loader prefix is replaced by ext./trans./load. prefix.


