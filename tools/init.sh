#!/bin/bash

# build base
echo "building base projects .."
cd ../base 
 cd uk-pom-dpu
  mvn clean install
  cd ..

# build libs
echo "building libs .."
cd ../libs
 cd scraperLib
  mvn clean install
  cd ..

# go back to tools
cd ../tools
echo "done"
