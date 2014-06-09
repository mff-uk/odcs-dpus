#!/bin/bash

# "Zakon|Rozhodnuti_soudu|Ucinnost|Cenovy_vymer|Instituce"

if [ ! $# == 2 ]; then
    echo "./update_data.sh <OLD DATA> <NEW DATA>"
    exit 1
fi

for fold in {0..9}; do
    for i in $1/$fold/*; do
        echo cp $2/`echo $i | sed -e 's/\(.*\/\)\([^\/]\+\)/\2/'` $i;
    done
done
