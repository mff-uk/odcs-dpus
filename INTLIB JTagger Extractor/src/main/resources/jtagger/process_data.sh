#!/bin/bash

## Priecinok data nech je nalinkovany na root s adresarami...
## Priecinok models na modely pre aktualne spracovany sud...

echo "******************************************************************";
echo "ADRESAR DATA UKAZUJE NA DATOVY KOREN";
echo "ADRESAR MODELS UKAZUJE NA SPRAVNE MODELY";
echo "******************************************************************";

echo "LINE JOINING"
./txt_join_lines.pl data/txt_joined_lines/ data/txt_source/utf8/*

echo "TOKENIZATION"
./txt_tokenization.pl data/txt/ data/txt_joined_lines/*

echo "TXT -> VXML"
./txt2vxml.pl data/empty_vxml/ data/txt/*

echo "VXML -> HMM"
./vxml2hmm.pl data/empty_hmm/ test "" data/empty_vxml/*

echo "TAGGER"
cd data/empty_hmm
for i in *; do echo $i; cat $i | ../../../../../apps/tagger/tagger ../../../../../tools/jtagger/models/rozhodnuti.mod > ../tagged_hmm_ri/$i; done
for i in *; do echo $i; cat $i | ../../../../../apps/tagger/tagger ../../../../../tools/jtagger/models/zakon.mod > ../tagged_hmm_zu/$i; done

echo "HMM -> VXML"
cd ../../
./hmm2vxml.pl data/tagged_hmm_ri/ data/tagged_vxml_ri/ data/empty_vxml/*
./hmm2vxml.pl data/tagged_hmm_zu/ data/tagged_vxml_zu/ data/empty_vxml/*

echo "RULEBASED"
./rulebased_vxml_validation.pl data/rulebased_ri/ data/tagged_vxml_ri/*
./rulebased_vxml_validation.pl data/rulebased_zu/ data/tagged_vxml_zu/*
./rulebased_vxml_validation.pl data/rulebased_z/ data/empty_vxml/*

echo "VALIDATION"
./vxml_validation.pl data/validated_ri/ data/rulebased_ri/*
./vxml_validation.pl data/validated_zu/ data/rulebased_zu/*
./vxml_validation.pl data/validated_z/ data/rulebased_z/*

echo "LINKING"
./vxml_linker.pl data/linked_ri/ data/validated_ri/*

echo "MERGE"
./merge_vxml_dirs.pl data/txt/ data/merged/ data/validated_z/ data/validated_zu/ data/linked_ri/ data/linked_ri/*

echo "VXML -> XML"
./vxml2xml.pl data/xml data/merged/*

#echo "JAVA"
#cd ../../apps/starka
#/usr/lib/jvm/java-7-openjdk-amd64/bin/java -jar IntLib.jar -inputDir=../../tools/jtagger/data/xml -outputDir=../../tools/jtagger/data/xml_linked/