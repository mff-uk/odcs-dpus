#!/bin/bash

script=`readlink -f $0`
basedir=`dirname $script`

find "${basedir}/.." -name "pom.xml" | sed 's/pom.xml$//' | while read dir; do
	cd "$dir"
	mvn clean install
	cd -
done
