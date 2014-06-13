#!/bin/bash

if [ ! $# == 3 ]; then
    echo "./test.sh <DATA DIR> <TAGGER DIR> <MODEL NAME>"
    exit 1
fi

echo -n "Prevod VXML -> HMM ... "
./vxml2hmm.pl test "Zakon|Rozhodnuti_soudu|Ucinnost|Cenovy_vymer|Instituce" $1/vxml/* > $1/test.hmm
echo "DONE"

echo -n "Trenovanie taggru ... "
cat $1/train.hmm | $2/train $1/$3
echo "DONE"