#!/bin/bash

# build base
echo "building base projects .."
cd ../base 
 cd unifiedviews-base
  mvn clean install
 cd ../unifiedviews-dpu-base
  mvn clean install
 cd ../unifiedviews-lib-base
  mvn clean install
 cd ..

# build libs
echo "building libs .."
cd ../libs
 for f in utils-*; do
   cd $f
    mvn clean install
   cd ..
  done
 for f in service-*; do
   cd $f
    mvn clean install
   cd ..
  done
 for f in boost-*; do
   cd $f
    mvn clean install
   cd ..
  done

# build template
echo "preparing dpu template.."
cd ../templates
 cd dpu
  mvn clean install
 cd ..

# go back to tools
cd ../tools
echo "done"
