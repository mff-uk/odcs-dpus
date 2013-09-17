#!/bin/bash

# "Zakon|Rozhodnuti_soudu|Ucinnost|Cenovy_vymer|Instituce"

if [ ! $# == 4 ]; then
    echo "./do_experiment.sh <DATA DIR> <EXPERIMENT NAME> <TAGGER DIR> <TAGSET>"
    exit 1
fi

if [ ! -d "$1/$2" ]; then
    echo "Creating $1/$2";
    mkdir "$1/$2";
fi

if [ ! -d "/tmp/hmm" ]; then
    echo "Creating /tmp/hmm";
    mkdir "/tmp/hmm";
fi

if [ ! -d "$1/$2/hmm" ]; then
    echo "Creating $1/$2/hmm";
    mkdir "$1/$2/hmm";
fi

if [ ! -d "$1/$2/vxml" ]; then
    echo "Creating $1/$2/vxml";
    mkdir "$1/$2/vxml";
fi

if [ ! -d "$1/$2/vxml_rulebased" ]; then
    echo "Creating $1/$2/vxml_rulebased";
    mkdir "$1/$2/vxml_rulebased";
fi

if [ ! -d "$1/$2/vxml_rulebased_validated" ]; then
    echo "Creating $1/$2/vxml_rulebased_validated";
    mkdir "$1/$2/vxml_rulebased_validated";
fi

if [ ! -d "$1/$2/vxml_linked" ]; then
    echo "Creating $1/$2/vxml_linked";
    mkdir "$1/$2/vxml_linked";
fi

for fold in {0..9}; do
    if [ ! -d "$1/$2/hmm/$fold" ]; then
        echo "Creating $1/$2/hmm/$fold";
        mkdir "$1/$2/hmm/$fold";
    fi
    if [ ! -d "$1/$2/vxml/$fold" ]; then
        echo "Creating $1/$2/vxml/$fold";
        mkdir "$1/$2/vxml/$fold";
    fi
    
    if [ ! -d "$1/$2/vxml_rulebased/$fold" ]; then
        echo "Creating $1/$2/vxml_rulebased/$fold";
        mkdir "$1/$2/vxml_rulebased/$fold";
    fi

    if [ ! -d "$1/$2/vxml_rulebased_validated/$fold" ]; then
        echo "Creating $1/$2/vxml_rulebased_validated/$fold";
        mkdir "$1/$2/vxml_rulebased_validated/$fold";
    fi

    if [ ! -d "$1/$2/vxml_linked/$fold" ]; then
        echo "Creating $1/$2/vxml_linked/$fold";
        mkdir "$1/$2/vxml_linked/$fold";
    fi
done

#echo -n "Prevod TRAIN VXML -> HMM "
#for fold in {0..9}; do
#    echo -n ".";
#    rm /tmp/hmm/* 2>/dev/null;
#    ./vxml2hmm.pl /tmp/hmm/ train $4 $1/train/vxml/$fold/*;
#    cat /tmp/hmm/* > $1/$2/train.$fold.hmm;
#done
#echo " DONE"
#
#echo -n "Trenovanie taggru        "
#for fold in {0..9}; do
#    echo -n ".";
#    cat $1/$2/train.$fold.hmm | $3/train $1/$2/model.$fold.mod 2>/dev/null;
#done
#echo " DONE"
#
#echo -n "Prevod TEST VXML -> HMM  "
#for fold in {0..9}; do
#    echo -n ".";
#    ./vxml2hmm.pl $1/test/hmm/$fold/ test $4 $1/test/vxml/$fold/*;
#done
#echo " DONE"
#
#echo -n "Tagging                  "
#for fold in {0..9}; do
#    echo -n ".";
#    for i in $1/test/vxml/$fold/*; do
#        subor=`echo $i | perl -e 'while (<>) {chomp($_); $_ =~ s/^(.*?\/?)([^\/]+)$/$2/; $_ =~ s/(.*)\.(.*)/$1/; print $_;}'`
#        #echo $subor;
#        cat $1/test/hmm/$fold/$subor.hmm | $3/tagger $1/$2/model.$fold.mod > $1/$2/hmm/$fold/$subor.hmm 2>/dev/null
#    done
#done
#echo " DONE"
#
#echo -n "Prevod HMM -> VXML       "
#for fold in {0..9}; do
#    echo -n ".";
#    ./hmm2vxml.pl $1/$2/hmm/$fold/ $1/$2/vxml/$fold/ $1/test/vxml/$fold/*
#done
#echo " DONE"

echo -n "Pravidlovy modul         "
for fold in {0..9}; do
    echo -n ".";
    ./rulebased_vxml_validation.pl $1/$2/vxml_rulebased/$fold/ $1/$2/vxml/$fold/*
done
echo " DONE"

echo "Evaluace                 ";
rm /tmp/eval_hmm.txt 2>/dev/null
for fold in {0..9}; do
    echo -n ".";
    ./evaluation.pl $4 $1/test/gs/$fold $1/$2/vxml/$fold/* >> /tmp/eval_hmm.txt
done

for tag in `echo $4 | sed -E 's/\|/ /g'`; do
    cat /tmp/eval_hmm.txt | grep -P "^$tag\t\d\."
    echo
done  | ./cross-evaluation.pl

echo "Evaluace RULEBASED       ";
rm /tmp/eval_hmm.txt 2>/dev/null
for fold in {0..9}; do
    echo -n ".";
    ./evaluation.pl $4 $1/test/gs/$fold $1/$2/vxml_rulebased/$fold/*  >> /tmp/eval_hmm.txt
done

for tag in `echo $4 | sed -E 's/\|/ /g'`; do
    cat /tmp/eval_hmm.txt | grep -P "^$tag\t\d\."
    echo
done  | ./cross-evaluation.pl

echo -n "Validace VXML            ";
for fold in {0..9}; do
    echo -n ".";
    ## Linking na goldstandard znackach
    #./vxml_validation.pl $1/$2/vxml_rulebased_validated/$fold/ $1/test/gs/$fold/*
    ./vxml_validation.pl $1/$2/vxml_rulebased_validated/$fold/ $1/$2/vxml_rulebased/$fold/*
done
echo " DONE"

echo -n "Linking VXML             ";
for fold in {0..9}; do
    echo -n ".";
    ## Linking na znackach rozpoznanych automaticky
    ./vxml_linker.pl $1/$2/vxml_linked/$fold/ $1/$2/vxml_rulebased_validated/$fold/*
done
echo " DONE"

echo -n "Linking Evaluation       ";
rm /tmp/eval_hmm.txt 2>/dev/null
for fold in {0..9}; do
    echo -n ".";
    ./linker_evaluation.pl $1/test/gs/$fold/ $1/$2/vxml_linked/$fold/*  >> /tmp/eval_hmm.txt
done
echo " DONE"

cat /tmp/eval_hmm.txt | ./linker_cross-evaluation.pl
